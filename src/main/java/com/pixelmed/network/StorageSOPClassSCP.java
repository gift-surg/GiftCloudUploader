/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.dicom.*;
import com.pixelmed.query.QueryResponseGeneratorFactory;
import com.pixelmed.query.RetrieveResponseGeneratorFactory;
import com.pixelmed.utils.ByteArray;
import com.pixelmed.utils.CopyStream;
import com.pixelmed.utils.FileUtilities;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * <p>This class implements the SCP role of SOP Classes of the Storage Service Class,
 * the Study Root Query Retrieve Information Model Find, Get and Move SOP Classes,
 * and the Verification SOP Class.</p>
 *
 * <p>The class has a constructor and a <code>run()</code> method. The
 * constructor is passed a socket on which has been received a transport
 * connection open indication. The <code>run()</code> method waits for an association to be initiated
 * (i.e. acts as an association acceptor), then waits for storage or
 * verification commands, storing data sets in Part 10 files in the specified folder.</p>
 *
 * <p>Debugging messages with a varying degree of verbosity can be activated.</p>
 *
 * <p>This class is not normally used directly, but rather is instantiated by the
 * {@link com.pixelmed.network.StorageSOPClassSCPDispatcher StorageSOPClassSCPDispatcher},
 * which takes care of listening for transport connection open indications, and
 * creates new threads and starts them to handle each incoming association request.</p>
 *
 * @see com.pixelmed.network.StorageSOPClassSCPDispatcher
 *
 * @author	dclunie
 */
public class StorageSOPClassSCP extends SOPClass implements Runnable {

	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/StorageSOPClassSCP.java,v 1.69 2014/09/09 20:34:09 dclunie Exp $";
	
	private static final int bufferedOutputStreamSizeForCStoreFileWrite = 65536;
	private static final boolean useBufferedOutputStreamForCStoreFileWrite = false;
	private static final boolean useAsynchronousOutputStreamForCStoreFileWrite = true;
	
	/***/
	private class CompositeCommandReceivedPDUHandler extends ReceivedDataHandler {
		/***/
		private int command;
		/***/
		private byte[] commandReceived;
		/***/
		private AttributeList commandList;
		/***/
		private byte[] dataReceived;
		/***/
		private AttributeList dataList;
		/***/
		private OutputStream out;
		/***/
		private CStoreRequestCommandMessage csrq;
		/***/
		private CEchoRequestCommandMessage cerq;
		/***/
		private CFindRequestCommandMessage cfrq;
		/***/
		private CMoveRequestCommandMessage cmrq;
		/***/
		private CGetRequestCommandMessage cgrq;
		/***/
		private byte[] response;
		/***/
		private byte presentationContextIDUsed;
		//private Association association;
		/***/
		private File receivedFile;
		/***/
		private File temporaryReceivedFile;
		/***/
		private File savedImagesFolder;
		/***/
		private QueryResponseGeneratorFactory queryResponseGeneratorFactory;
		/***/
		private RetrieveResponseGeneratorFactory retrieveResponseGeneratorFactory;

		/**
		 * @throws	IOException
		 * @throws	DicomException
		 */
		private void buildCEchoResponse() throws DicomException, IOException {
			response = new CEchoResponseCommandMessage(
				cerq.getAffectedSOPClassUID(),
				cerq.getMessageID(),
				ResponseStatus.Success
				).getBytes();
		}
		
		/**
		 * @throws	IOException
		 * @throws	DicomException
		 */
		private void buildCStoreResponse() throws DicomException, IOException {
			response = new CStoreResponseCommandMessage(
				csrq.getAffectedSOPClassUID(),
				csrq.getAffectedSOPInstanceUID(),
				csrq.getMessageID(),
				ResponseStatus.Success
				).getBytes();
		}
		
		/**
		 * @param	savedImagesFolder		null if we do not want to actually save received data (i.e., we want to discard it for testing)
		 * @param	queryResponseGeneratorFactory		a factory to make handlers to generate query responses from a supplied query message
		 * @param	retrieveResponseGeneratorFactory	a factory to make handlers to generate retrieve responses from a supplied retrieve message
		 * @param	debugLevel
		 */
		public CompositeCommandReceivedPDUHandler(File savedImagesFolder,QueryResponseGeneratorFactory queryResponseGeneratorFactory,RetrieveResponseGeneratorFactory retrieveResponseGeneratorFactory,int debugLevel) {
			super(debugLevel);
			command=MessageServiceElementCommand.NOCOMMAND;
			commandReceived=null;
			commandList=null;
			dataReceived=null;
			dataList=null;
			out=null;
			csrq=null;
			receivedFile=null;
			this.savedImagesFolder=savedImagesFolder;
			this.queryResponseGeneratorFactory=queryResponseGeneratorFactory;
			this.retrieveResponseGeneratorFactory=retrieveResponseGeneratorFactory;
		}

		private class CMovePendingResponseSender extends MultipleInstanceTransferStatusHandler {
		
			private Association association;
			private CMoveRequestCommandMessage cmrq;
			
			int nRemaining;
			int nCompleted;
			int nFailed;
			int nWarning;
			
			CMovePendingResponseSender(Association association,CMoveRequestCommandMessage cmrq) {
				this.association = association;
				this.cmrq = cmrq;
				nRemaining = 0;
				nCompleted = 0;
				nFailed = 0;
				nWarning = 0;
			}
			
			public void updateStatus(int nRemaining,int nCompleted,int nFailed,int nWarning,String sopInstanceUID) {
				this.nRemaining = nRemaining;
				this.nCompleted = nCompleted;
				this.nFailed = nFailed;
				this.nWarning = nWarning;
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.CMovePendingResponseSender.updateStatusText(): Bulding C-MOVE pending response");
				if (nRemaining > 0) {
					try {
						byte cMovePendingResponseCommandMessage[] = new CMoveResponseCommandMessage(
							cmrq.getAffectedSOPClassUID(),
							cmrq.getMessageID(),
							ResponseStatus.SubOperationsAreContinuing,	// status is pending
							false,				// no dataset
							nRemaining,nCompleted,nFailed,nWarning
							).getBytes();
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.CMovePendingResponseSender.updateStatusText(): C-MOVE pending response = "+CompositeResponseHandler.dumpAttributeListFromCommandOrData(cMovePendingResponseCommandMessage,TransferSyntax.Default));

						byte presentationContextIDForResponse = association.getSuitablePresentationContextID(cmrq.getAffectedSOPClassUID());
						association.send(presentationContextIDForResponse,cMovePendingResponseCommandMessage,null);
					}
					catch (DicomNetworkException e) {
						e.printStackTrace(System.err);
					}
					catch (DicomException e) {
						e.printStackTrace(System.err);
					}
					catch (IOException e) {
						e.printStackTrace(System.err);
					}
				}
				// else do not send pending message if nothing remaining; just update counts
			}
		}


		private class CGetPendingResponseSender extends MultipleInstanceTransferStatusHandler {
		
			private Association association;
			private CGetRequestCommandMessage cgrq;
			
			int nRemaining;
			int nCompleted;
			int nFailed;
			int nWarning;
			
			CGetPendingResponseSender(Association association,CGetRequestCommandMessage cgrq) {
				this.association = association;
				this.cgrq = cgrq;
				nRemaining = 0;
				nCompleted = 0;
				nFailed = 0;
				nWarning = 0;
			}
			
			public void updateStatus(int nRemaining,int nCompleted,int nFailed,int nWarning,String sopInstanceUID) {
				this.nRemaining = nRemaining;
				this.nCompleted = nCompleted;
				this.nFailed = nFailed;
				this.nWarning = nWarning;
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.CGetPendingResponseSender.updateStatusText(): Bulding C-GET pending response");
				if (nRemaining > 0) {
					try {
						byte cGetPendingResponseCommandMessage[] = new CGetResponseCommandMessage(
							cgrq.getAffectedSOPClassUID(),
							cgrq.getMessageID(),
							ResponseStatus.SubOperationsAreContinuing,	// status is pending
							false,				// no dataset
							nRemaining,nCompleted,nFailed,nWarning
							).getBytes();

						byte presentationContextIDForResponse = association.getSuitablePresentationContextID(cgrq.getAffectedSOPClassUID());
						association.send(presentationContextIDForResponse,cGetPendingResponseCommandMessage,null);
					}
					catch (DicomNetworkException e) {
						e.printStackTrace(System.err);
					}
					catch (DicomException e) {
						e.printStackTrace(System.err);
					}
					catch (IOException e) {
						e.printStackTrace(System.err);
					}
				}
				// else do not send pending message if nothing remaining; just update counts
			}
		}

		/**
		 * @param	pdata
		 * @param	association
		 * @throws	IOException
		 * @throws	DicomException
		 * @throws	DicomNetworkException
		 */
		public void sendPDataIndication(PDataPDU pdata,Association association) throws DicomNetworkException, DicomException, IOException {
			// append to command ...
			LinkedList pdvList = pdata.getPDVList();
			ListIterator i = pdvList.listIterator();
			while (i.hasNext()) {
				PresentationDataValue pdv = (PresentationDataValue)i.next();
				presentationContextIDUsed = pdv.getPresentationContextID();
				if (pdv.isCommand()) {
					receivedFile=null;
					commandReceived=ByteArray.concatenate(commandReceived,pdv.getValue());	// handles null cases
					if (pdv.isLastFragment()) {
						commandList = new AttributeList();
						commandList.read(new DicomInputStream(new ByteArrayInputStream(commandReceived),TransferSyntax.Default,false));
						command = Attribute.getSingleIntegerValueOrDefault(commandList,TagFromName.CommandField,0xffff);

						// C_ECHO processing
						if (command == MessageServiceElementCommand.C_ECHO_RQ) {	// C-ECHO-RQ
							cerq = new CEchoRequestCommandMessage(commandList);
							buildCEchoResponse();
							setDone(true);
							setRelease(false);
						}
						else if (command == MessageServiceElementCommand.C_STORE_RQ) {
							csrq = new CStoreRequestCommandMessage(commandList);
						}
						else if (command == MessageServiceElementCommand.C_FIND_RQ) {
							throw new UnsupportedOperationException("The DICOM node received a C_FIND command, but this is not supported.");
						}
						else if (command == MessageServiceElementCommand.C_MOVE_RQ) {
							throw new UnsupportedOperationException("The DICOM node received a C_MOVE command, but this is not supported.");
						}
						else if (command == MessageServiceElementCommand.C_GET_RQ) {
							throw new UnsupportedOperationException("The DICOM node received a C_GET command, but this is not supported.");
						}
						else {
							throw new DicomNetworkException("Unexpected command 0x"+Integer.toHexString(command)+" "+MessageServiceElementCommand.toString(command));
						}
						// 2004/06/08 DAC removed break that was here to resolve [bugs.mrmf] (000113) StorageSCP failing when data followed command in same PDU
					}
				}
				else {
					// This is where the C_STORE command is processed
					 if (command == MessageServiceElementCommand.C_STORE_RQ) {

						 // out is initially null - this means we are receiving the first fragment, so create the file and write the DICOM header
						 if (out == null && savedImagesFolder != null) {
							FileMetaInformation fmi = new FileMetaInformation(
								csrq.getAffectedSOPClassUID(),
								csrq.getAffectedSOPInstanceUID(),
								association.getTransferSyntaxForPresentationContextID(presentationContextIDUsed),
								association.getCallingAETitle());
							temporaryReceivedFile=new File(savedImagesFolder,FileUtilities.makeTemporaryFileName());
							out = new FileOutputStream(temporaryReceivedFile);
							if (useBufferedOutputStreamForCStoreFileWrite) {
								out = new BufferedOutputStream(out,bufferedOutputStreamSizeForCStoreFileWrite);
							}
							DicomOutputStream dout = new DicomOutputStream(out,TransferSyntax.ExplicitVRLittleEndian,null);
							fmi.getAttributeList().write(dout);
							dout.flush();
							if (useAsynchronousOutputStreamForCStoreFileWrite) {
								out = new AsynchronousOutputStream(out);
							}
						}

						 // Write the next fragment
						if (out != null) {
							byte[] bytesToWrite = pdv.getValue();
							out.write(bytesToWrite);
						}

						 // For the last fragment, close and tidy up
						if (pdv.isLastFragment()) {
							if (out != null) {
								out.close();
								receivedFile=storedFilePathStrategy.makeReliableStoredFilePathWithFoldersCreated(savedImagesFolder,csrq.getAffectedSOPInstanceUID());
								if (!temporaryReceivedFile.renameTo(receivedFile)) {
									System.err.println("StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Could not move temporary file into place ... copying instead");
									CopyStream.copy(temporaryReceivedFile,receivedFile);
									if (!temporaryReceivedFile.delete()) {
										System.err.println("StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Could not delete temporary file after copying");
									}
								}
								out=null;
							}
							buildCStoreResponse();
							setDone(true);
							setRelease(false);
						}
					}
					else if (command == MessageServiceElementCommand.C_MOVE_RQ) {
						 // Unsupported
					}
					else if (command == MessageServiceElementCommand.C_GET_RQ) {
						 // Unsupported
					}
					else {
					}
				}
			}
		}
		
		/***/
		public AttributeList getCommandList() { return commandList; }
		/***/
		public byte[] getResponse() { return response; }
		/***/
		public byte getPresentationContextIDUsed() { return presentationContextIDUsed; }
		/***/
		public File getReceivedFile() { return receivedFile; }
		/***/
		public String getReceivedFileName() { return receivedFile == null ? null : receivedFile.getPath(); }
	}

	class UnsupportedOperationException extends DicomNetworkException {

		/** An exception for unsupported DICOM operations
		 * @param    msg
		 */
		public UnsupportedOperationException(String msg) {
			super(msg);
		}
	}
	/**
	 * @param	association
	 * @throws	IOException
	 * @throws	AReleaseException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	private boolean receiveAndProcessOneRequestMessage(Association association) throws AReleaseException, DicomNetworkException, DicomException, IOException {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.receiveAndProcessOneRequestMessage(): start");
		CompositeCommandReceivedPDUHandler receivedPDUHandler = new CompositeCommandReceivedPDUHandler(savedImagesFolder,queryResponseGeneratorFactory,retrieveResponseGeneratorFactory,debugLevel);
		association.setReceivedDataHandler(receivedPDUHandler);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.receiveAndProcessOneRequestMessage(): waitForPDataPDUsUntilHandlerReportsDone");
		association.waitForPDataPDUsUntilHandlerReportsDone();	// throws AReleaseException if release request instead
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.receiveAndProcessOneRequestMessage(): back from waitForPDataPDUsUntilHandlerReportsDone");
		{
			String receivedFileName=receivedPDUHandler.getReceivedFileName();	// null if C-ECHO
			if (receivedFileName != null) {
//long startTime=System.currentTimeMillis();
				byte pcid = receivedPDUHandler.getPresentationContextIDUsed();
				String ts = association.getTransferSyntaxForPresentationContextID(pcid);
				String callingAE = association.getCallingAETitle();
				receivedObjectHandler.sendReceivedObjectIndication(receivedFileName,ts,callingAE);
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.receiveAndProcessOneRequestMessage(): received file "+receivedFileName+" from "+callingAE+" in "+ts);
//long endTime=System.currentTimeMillis();
//System.err.println("StorageSOPClassSCP.receiveAndProcessOneRequestMessage(): call to sendReceivedObjectIndication() time "+(endTime-startTime)+" ms");
			}
		}
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.receiveAndProcessOneRequestMessage(): sending (final) response");
		byte[] response = receivedPDUHandler.getResponse();
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.receiveAndProcessOneRequestMessage(): response = "+CompositeResponseHandler.dumpAttributeListFromCommandOrData(response,TransferSyntax.Default));
		association.send(receivedPDUHandler.getPresentationContextIDUsed(),response,null);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.receiveAndProcessOneRequestMessage(): end");
		boolean moreExpected;
		if (receivedPDUHandler.isToBeReleased()) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.receiveAndProcessOneRequestMessage(): explicitly releasing association");
			association.release();
			moreExpected = false;
		}
		else {
			moreExpected = true;
		}
		return moreExpected;
	}
	
	/***/
	private Socket socket;
	/***/
	private String calledAETitle;
	/***/
	private int ourMaximumLengthReceived;
	/***/
	private int socketReceiveBufferSize;
	/***/
	private int socketSendBufferSize;
	/***/
	private File savedImagesFolder;
	/***/
	protected StoredFilePathStrategy storedFilePathStrategy;
	/***/
	private ReceivedObjectHandler receivedObjectHandler;
	/***/
	private AssociationStatusHandler associationStatusHandler;
	/***/
	private QueryResponseGeneratorFactory queryResponseGeneratorFactory;
	/***/
	private RetrieveResponseGeneratorFactory retrieveResponseGeneratorFactory;
	/***/
	private ApplicationEntityMap applicationEntityMap;
	/***/
	private PresentationContextSelectionPolicy presentationContextSelectionPolicy;
	/***/
	private int debugLevel;

	/**
	 * <p>Construct an instance of an association acceptor and storage, query, retrieve and verification SCP
	 * to be passed to the constructor of a thread that will be started.</p>
	 *
	 * @param	socket								the socket on which a transport connection open indication has been received
	 * @param	calledAETitle						our AE Title
	 * @param	ourMaximumLengthReceived			the maximum PDU length that we will offer to receive
	 * @param	socketReceiveBufferSize				the TCP socket receive buffer size to set (if possible), 0 means leave at the default
	 * @param	socketSendBufferSize				the TCP socket send buffer size to set (if possible), 0 means leave at the default
	 * @param	savedImagesFolder					the folder in which to store received data sets (may be null, to ignore received data for testing)
	 * @param	storedFilePathStrategy				the strategy to use for naming received files and folders
	 * @param	receivedObjectHandler				the handler to call after each data set has been received and stored
	 * @param	associationStatusHandler			the handler to call when the Association is closed
	 * @param	queryResponseGeneratorFactory		a factory to make handlers to generate query responses from a supplied query message
	 * @param	retrieveResponseGeneratorFactory	a factory to make handlers to generate retrieve responses from a supplied retrieve message
	 * @param	applicationEntityMap				a map of application entity titles to presentation addresses
	 * @param	presentationContextSelectionPolicy	which SOP Classes and Transfer Syntaxes to accept and reject
	 * @param	debugLevel							zero for no debugging messages, higher values more verbose messages
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	public StorageSOPClassSCP(Socket socket, String calledAETitle,
			int ourMaximumLengthReceived,int socketReceiveBufferSize,int socketSendBufferSize,
			File savedImagesFolder,StoredFilePathStrategy storedFilePathStrategy,
			ReceivedObjectHandler receivedObjectHandler,
			AssociationStatusHandler associationStatusHandler,
			QueryResponseGeneratorFactory queryResponseGeneratorFactory,RetrieveResponseGeneratorFactory retrieveResponseGeneratorFactory,
			ApplicationEntityMap applicationEntityMap,
			PresentationContextSelectionPolicy presentationContextSelectionPolicy,
			int debugLevel) throws DicomNetworkException, DicomException, IOException {
//System.err.println("StorageSOPClassSCP()");
		this.socket=socket;
		this.calledAETitle=calledAETitle;
		this.ourMaximumLengthReceived=ourMaximumLengthReceived;
		this.socketReceiveBufferSize=socketReceiveBufferSize;
		this.socketSendBufferSize=socketSendBufferSize;
		this.savedImagesFolder=savedImagesFolder;
		this.storedFilePathStrategy=storedFilePathStrategy;
		this.receivedObjectHandler=receivedObjectHandler;
		this.associationStatusHandler=associationStatusHandler;
		this.queryResponseGeneratorFactory=queryResponseGeneratorFactory;
		this.retrieveResponseGeneratorFactory=retrieveResponseGeneratorFactory;
		this.applicationEntityMap=applicationEntityMap;
		this.presentationContextSelectionPolicy=presentationContextSelectionPolicy;
		this.debugLevel=debugLevel;
		storedFilePathStrategy.setDebugLevel(debugLevel);
	}
	
	/**
	 * <p>Waits for an association to be initiated (acts as an association acceptor), then waits for storage or
	 * verification commands, storing data sets in Part 10 files in the specified folder, until the association
	 * is released or the transport connection closes.</p>
	 */
	public void run() {
//System.err.println("StorageSOPClassSCP.run()");
		try {
			Association association = AssociationFactory.createNewAssociation(socket,calledAETitle,
				ourMaximumLengthReceived,socketReceiveBufferSize,socketSendBufferSize,
				presentationContextSelectionPolicy,
				debugLevel);
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": Association received "+association.getEndpointDescription());
if (debugLevel > 2) System.err.println(association);
			try {
				while (receiveAndProcessOneRequestMessage(association));
			}
			catch (AReleaseException e) {
//System.err.println("Association.run(): AReleaseException: "+association.getAssociationNumber()+" from "+association.getListenerCallingAETitle()+" released");
				if (associationStatusHandler != null) {
					associationStatusHandler.sendAssociationReleaseIndication(association);
				}
			}
		}
		catch (Exception e) {
			if (!(e.getCause() instanceof UnsupportedOperationException)) {
				e.printStackTrace(System.err);
			}
		}
	}
}




