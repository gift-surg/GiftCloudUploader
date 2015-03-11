/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.utils.*;
import com.pixelmed.dicom.*;
import com.pixelmed.query.QueryResponseGenerator;
import com.pixelmed.query.QueryResponseGeneratorFactory;
import com.pixelmed.query.RetrieveResponseGenerator;
import com.pixelmed.query.RetrieveResponseGeneratorFactory;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.*;
import java.net.Socket;

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
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.CMovePendingResponseSender.updateStatus(): Bulding C-MOVE pending response");
				if (nRemaining > 0) {
					try {
						byte cMovePendingResponseCommandMessage[] = new CMoveResponseCommandMessage(
							cmrq.getAffectedSOPClassUID(),
							cmrq.getMessageID(),
							ResponseStatus.SubOperationsAreContinuing,	// status is pending
							false,				// no dataset
							nRemaining,nCompleted,nFailed,nWarning
							).getBytes();
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.CMovePendingResponseSender.updateStatus(): C-MOVE pending response = "+CompositeResponseHandler.dumpAttributeListFromCommandOrData(cMovePendingResponseCommandMessage,TransferSyntax.Default));

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
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.CGetPendingResponseSender.updateStatus(): Bulding C-GET pending response");
				if (nRemaining > 0) {
					try {
						byte cGetPendingResponseCommandMessage[] = new CGetResponseCommandMessage(
							cgrq.getAffectedSOPClassUID(),
							cgrq.getMessageID(),
							ResponseStatus.SubOperationsAreContinuing,	// status is pending
							false,				// no dataset
							nRemaining,nCompleted,nFailed,nWarning
							).getBytes();
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.CGetPendingResponseSender.updateStatus(): C-GET pending response = "+CompositeResponseHandler.dumpAttributeListFromCommandOrData(cGetPendingResponseCommandMessage,TransferSyntax.Default));

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

//long startReceivedFile;
//long wroteMetaReceivedFile;
//long wroteLastFragmentReceivedFile;
//long endReceivedFile;
//long accumulatedWritePDVTime;

		/**
		 * @param	pdata
		 * @param	association
		 * @throws	IOException
		 * @throws	DicomException
		 * @throws	DicomNetworkException
		 */
		public void sendPDataIndication(PDataPDU pdata,Association association) throws DicomNetworkException, DicomException, IOException {
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): sendPDataIndication()");
if (debugLevel > 3) super.dumpPDVList(pdata.getPDVList());
if (debugLevel > 3) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): finished dumping PDV list from PDU");
			// append to command ...
			LinkedList pdvList = pdata.getPDVList();
			ListIterator i = pdvList.listIterator();
			while (i.hasNext()) {
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): have another fragment");
				PresentationDataValue pdv = (PresentationDataValue)i.next();
				presentationContextIDUsed = pdv.getPresentationContextID();
				if (pdv.isCommand()) {
					receivedFile=null;
					commandReceived=ByteArray.concatenate(commandReceived,pdv.getValue());	// handles null cases
					if (pdv.isLastFragment()) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): last fragment of data seen");
if (debugLevel > 1) System.err.println(HexDump.dump(commandReceived));
						commandList = new AttributeList();
						commandList.read(new DicomInputStream(new ByteArrayInputStream(commandReceived),TransferSyntax.Default,false));
if (debugLevel > 1) System.err.print(commandList);
						command = Attribute.getSingleIntegerValueOrDefault(commandList,TagFromName.CommandField,0xffff);
						if (command == MessageServiceElementCommand.C_ECHO_RQ) {	// C-ECHO-RQ
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): C-ECHO-RQ between "+association.getEndpointDescription());
							cerq = new CEchoRequestCommandMessage(commandList);
							buildCEchoResponse();
							setDone(true);
							setRelease(false);
						}
						else if (command == MessageServiceElementCommand.C_STORE_RQ) {
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): C-STORE-RQ between "+association.getEndpointDescription());
							csrq = new CStoreRequestCommandMessage(commandList);
						}
						else if (command == MessageServiceElementCommand.C_FIND_RQ && queryResponseGeneratorFactory != null) {
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): C-FIND-RQ between "+association.getEndpointDescription());
							cfrq = new CFindRequestCommandMessage(commandList);
						}
						else if (command == MessageServiceElementCommand.C_MOVE_RQ && retrieveResponseGeneratorFactory != null) {
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): C-MOVE-RQ between "+association.getEndpointDescription());
							cmrq = new CMoveRequestCommandMessage(commandList);
						}
						else if (command == MessageServiceElementCommand.C_GET_RQ && retrieveResponseGeneratorFactory != null) {
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): C-GET-RQ between "+association.getEndpointDescription());
							cgrq = new CGetRequestCommandMessage(commandList);
						}
						else {
							throw new DicomNetworkException("Unexpected command 0x"+Integer.toHexString(command)+" "+MessageServiceElementCommand.toString(command));
						}
						// 2004/06/08 DAC removed break that was here to resolve [bugs.mrmf] (000113) StorageSCP failing when data followed command in same PDU
if (debugLevel > 1 && i.hasNext()) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler: Data after command in same PDU");
					}
				}
				else {
					 if (command == MessageServiceElementCommand.C_STORE_RQ) {
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Storing data fragment");
						if (out == null && savedImagesFolder != null) {		// lazy opening
//startReceivedFile=System.currentTimeMillis();
//accumulatedWritePDVTime=0;
							FileMetaInformation fmi = new FileMetaInformation(
								csrq.getAffectedSOPClassUID(),
								csrq.getAffectedSOPInstanceUID(),
								association.getTransferSyntaxForPresentationContextID(presentationContextIDUsed),
								association.getCallingAETitle());
							temporaryReceivedFile=new File(savedImagesFolder,FileUtilities.makeTemporaryFileName());
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Receiving and storing into temporary "+temporaryReceivedFile);
							out = new FileOutputStream(temporaryReceivedFile);
							if (useBufferedOutputStreamForCStoreFileWrite) {
								out = new BufferedOutputStream(out,bufferedOutputStreamSizeForCStoreFileWrite);
							}
							DicomOutputStream dout = new DicomOutputStream(out,TransferSyntax.ExplicitVRLittleEndian,null);
							fmi.getAttributeList().write(dout);
							dout.flush();
//wroteMetaReceivedFile=System.currentTimeMillis();
							if (useAsynchronousOutputStreamForCStoreFileWrite) {
								out = new AsynchronousOutputStream(out);
							}
						}
						if (out != null) {
//long startWritePDV=System.currentTimeMillis();
							byte[] bytesToWrite = pdv.getValue();
							out.write(bytesToWrite);
//accumulatedWritePDVTime+=(System.currentTimeMillis()-startWritePDV);
						}
						if (pdv.isLastFragment()) {
//wroteLastFragmentReceivedFile=System.currentTimeMillis();
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Finished storing data");
							if (out != null) {
								out.close();
								receivedFile=storedFilePathStrategy.makeReliableStoredFilePathWithFoldersCreated(savedImagesFolder,csrq.getAffectedSOPInstanceUID());
								if (!temporaryReceivedFile.renameTo(receivedFile)) {
/*if (debugLevel > 1)*/ System.err.println("StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Could not move temporary file into place ... copying instead");
									CopyStream.copy(temporaryReceivedFile,receivedFile);
									if (!temporaryReceivedFile.delete()) {
/*if (debugLevel > 1)*/ System.err.println("StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Could not delete temporary file after copying");
									}
								}
								out=null;
//endReceivedFile=System.currentTimeMillis();
//System.err.println("StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): time until metaheader written    "+(wroteMetaReceivedFile-startReceivedFile)+" ms");
//System.err.println("StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): time until last fragment written "+(wroteLastFragmentReceivedFile-startReceivedFile)+" ms");
//System.err.println("StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): time until file stored           "+(endReceivedFile-startReceivedFile)+" ms");
//System.err.println("StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): accumulated write PDV time       "+accumulatedWritePDVTime+" ms");
							}
							buildCStoreResponse();
							setDone(true);
							setRelease(false);
						}
					}
					else if (command == MessageServiceElementCommand.C_FIND_RQ && queryResponseGeneratorFactory != null) {
						QueryResponseGenerator queryResponseGenerator = queryResponseGeneratorFactory.newInstance();
						dataReceived=ByteArray.concatenate(dataReceived,pdv.getValue());	// handles null cases
						if (pdv.isLastFragment()) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): last fragment of data seen");
if (debugLevel > 1) System.err.println(HexDump.dump(dataReceived));
							dataList = new AttributeList();
							dataList.read(new DicomInputStream(new ByteArrayInputStream(dataReceived),
								association.getTransferSyntaxForPresentationContextID(presentationContextIDUsed),false));
if (debugLevel > 1) System.err.print(dataList);
							queryResponseGenerator.performQuery(cfrq.getAffectedSOPClassUID(),dataList,false/*relational*/);
							int status = queryResponseGenerator.getStatus();
							if (status != ResponseStatus.Success) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Query failed, status = 0x"+Integer.toHexString(status));
								response = new CFindResponseCommandMessage(
									cfrq.getAffectedSOPClassUID(),
									cfrq.getMessageID(),
									status,
									false,				// no dataset
									queryResponseGenerator.getOffendingElement(),
									null				// no ErrorComment
								).getBytes();
								queryResponseGenerator.close();
							}
							else {
								AttributeList responseIdentifierList;
								while ((responseIdentifierList = queryResponseGenerator.next()) != null) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Building and sending pending response "+responseIdentifierList.toString());
									byte presentationContextIDForResponse = association.getSuitablePresentationContextID(cfrq.getAffectedSOPClassUID());
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Using context ID for response "+presentationContextIDForResponse);
									byte cFindResponseCommandMessage[] = new CFindResponseCommandMessage(
											cfrq.getAffectedSOPClassUID(),
											cfrq.getMessageID(),
											(queryResponseGenerator.allOptionalKeysSuppliedWereSupported() ? ResponseStatus.MatchesAreContinuingOptionalKeysSupported : ResponseStatus.MatchesAreContinuingOptionalKeysNotSupported),	// pending
											//ResponseStatus.MatchesAreContinuingOptionalKeysSupported,	// pending ... temporary workaround for [bugs.mrmf] (000213) K-PACS freaked out by valid unsupported optional keys pending response during C-FIND
											true														// dataset present
										).getBytes();
									byte cFindIdentifier[] = new IdentifierMessage(
											responseIdentifierList,
											association.getTransferSyntaxForPresentationContextID(presentationContextIDForResponse)
										).getBytes();
									//association.setReceivedDataHandler(new CXXXXResponseHandler(debugLevel));
									association.send(presentationContextIDForResponse,cFindResponseCommandMessage,null);
									association.send(presentationContextIDForResponse,null,cFindIdentifier);
								}
								queryResponseGenerator.close();
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Bulding final C-FIND success response");
								response = new CFindResponseCommandMessage(
										cfrq.getAffectedSOPClassUID(),
										cfrq.getMessageID(),
										ResponseStatus.Success,				// success status matching is complete
										false								// no dataset
									).getBytes();
							}
							setDone(true);
							setRelease(false);
						}
					}
					else if (command == MessageServiceElementCommand.C_MOVE_RQ && retrieveResponseGeneratorFactory != null && applicationEntityMap != null) {
						RetrieveResponseGenerator retrieveResponseGenerator = retrieveResponseGeneratorFactory.newInstance();
						dataReceived=ByteArray.concatenate(dataReceived,pdv.getValue());	// handles null cases
						if (pdv.isLastFragment()) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): last fragment of data seen");
if (debugLevel > 1) System.err.println(HexDump.dump(dataReceived));
							dataList = new AttributeList();
							dataList.read(new DicomInputStream(new ByteArrayInputStream(dataReceived),
								association.getTransferSyntaxForPresentationContextID(presentationContextIDUsed),false));
if (debugLevel > 1) System.err.print(dataList);
							retrieveResponseGenerator.performRetrieve(cmrq.getAffectedSOPClassUID(),dataList,false/*relational*/);
							SetOfDicomFiles dicomFiles = retrieveResponseGenerator.getDicomFiles();
							int status = retrieveResponseGenerator.getStatus();
							retrieveResponseGenerator.close();
							if (status != ResponseStatus.Success || dicomFiles == null) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): retrieve failed or contains nothing, status = 0x"+Integer.toHexString(status));
								response = new CMoveResponseCommandMessage(
									cmrq.getAffectedSOPClassUID(),
									cmrq.getMessageID(),
									status,
									false,				// no dataset
									retrieveResponseGenerator.getOffendingElement(),
									null				// no ErrorComment
								).getBytes();
							}
							else {
								CMovePendingResponseSender pendingResponseSender = new CMovePendingResponseSender(association,cmrq);
								pendingResponseSender.nRemaining = dicomFiles.size();		// in case fails immediately with no status updates
							
								String moveDestinationAETitle = cmrq.getMoveDestination();
								PresentationAddress moveDestinationPresentationAddress = applicationEntityMap.getPresentationAddress(moveDestinationAETitle);
								if (moveDestinationPresentationAddress == null && moveDestinationAETitle.equals(association.getCallingAETitle())) {
									String callingHostName = association.getCallingAEHostName();
									if (callingHostName != null && callingHostName.length() > 0) {
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): unrecognized moveDestinationAETitle="+moveDestinationAETitle+" but matches callingAETitle so trying some likely ports on host "+callingHostName);
										// probably probe this first to see if it is possible to connect and C-ECHO, before proceeeding ...
										for (int guessedPortNumber : NetworkDefaultValues.commonPortNumbers) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): trying guessed port number "+guessedPortNumber);
											try {
												int timeout = 5000;	// ms
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): testing ability to connect socket to port "+guessedPortNumber+" with timeout "+timeout+" ms");
												if (ProbeCapability.canConnectToPort(callingHostName,guessedPortNumber,timeout)) {
													// this uses the default java.net.Socket() with connection constructor timeout, which can be quite long, hence the preceeding check ...
													new VerificationSOPClassSCU(callingHostName,guessedPortNumber,moveDestinationAETitle,association.getCalledAETitle(),false/*secureTransport*/,debugLevel);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): successful C-ECHO to guessed port number "+guessedPortNumber);
													// if we get here without an exception, we succeeded
													moveDestinationPresentationAddress = new PresentationAddress(callingHostName,guessedPortNumber);
													break;	// can stop now, since we have a port that responds
												}
											}
											catch (Exception e) {
												e.printStackTrace(System.err);
											}
										}
									}
								}
								if (moveDestinationPresentationAddress != null) {
									String moveDestinationHostname = moveDestinationPresentationAddress.getHostname();
									int moveDestinationPort = moveDestinationPresentationAddress.getPort();
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): moveDestinationAETitle="+moveDestinationAETitle);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): moveDestinationHostname="+moveDestinationHostname);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): moveDestinationPort="+moveDestinationPort);
									{
										new StorageSOPClassSCU(
											moveDestinationHostname,
											moveDestinationPort,
											moveDestinationAETitle,	// the C-STORE called AET
											calledAETitle,		// use ourselves (the C-MOVE called AET) as the C-STORE calling AET
											dicomFiles,
											0,			// compressionLevel
											pendingResponseSender,
											calledAETitle,			// use ourselves (the C-MOVE called AET) as the MoveOriginatorApplicationEntityTitle
											cmrq.getMessageID(),	// MoveOriginatorMessageID
											debugLevel);
									}
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): after all stored: nRemaining="+pendingResponseSender.nRemaining+" nCompleted="+pendingResponseSender.nCompleted+" nFailed="+pendingResponseSender.nFailed+" nWarning="+pendingResponseSender.nWarning);
									if (pendingResponseSender.nRemaining > 0) {
										pendingResponseSender.nFailed+=pendingResponseSender.nRemaining;
										pendingResponseSender.nRemaining=0;
									}
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): after setting remaining to zero: nRemaining="+pendingResponseSender.nRemaining+" nCompleted="+pendingResponseSender.nCompleted+" nFailed="+pendingResponseSender.nFailed+" nWarning="+pendingResponseSender.nWarning);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Bulding final C-MOVE success response");
									response = new CMoveResponseCommandMessage(
										cmrq.getAffectedSOPClassUID(),
										cmrq.getMessageID(),
										pendingResponseSender.nFailed > 0
											? ResponseStatus.SubOperationsCompleteOneOrMoreFailures
											: ResponseStatus.SubOperationsCompleteNoFailures,
										false,				// no dataset, unless there was failure, then add Failed SOP Instance UID List (0008,0058) :(
										pendingResponseSender.nRemaining,
										pendingResponseSender.nCompleted,
										pendingResponseSender.nFailed,
										pendingResponseSender.nWarning
									).getBytes();
								}
								else {
									status=ResponseStatus.RefusedMoveDestinationUnknown;
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Unrecognized move destination "+moveDestinationAETitle+", status = 0x"+Integer.toHexString(status));
									response = new CMoveResponseCommandMessage(
										cmrq.getAffectedSOPClassUID(),
										cmrq.getMessageID(),
										status,
										false,				// no dataset
										null,				// no OffendingElement
										moveDestinationAETitle		// ErrorComment
									).getBytes();
								}
							}
							setDone(true);
							setRelease(false);
						}
					}
					else if (command == MessageServiceElementCommand.C_GET_RQ && retrieveResponseGeneratorFactory != null) {
						RetrieveResponseGenerator retrieveResponseGenerator = retrieveResponseGeneratorFactory.newInstance();
						dataReceived=ByteArray.concatenate(dataReceived,pdv.getValue());	// handles null cases
						if (pdv.isLastFragment()) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): last fragment of data seen");
if (debugLevel > 1) System.err.println(HexDump.dump(dataReceived));
							dataList = new AttributeList();
							dataList.read(new DicomInputStream(new ByteArrayInputStream(dataReceived),
								association.getTransferSyntaxForPresentationContextID(presentationContextIDUsed),false));
if (debugLevel > 1) System.err.print(dataList);
							retrieveResponseGenerator.performRetrieve(cgrq.getAffectedSOPClassUID(),dataList,false/*relational*/);
							SetOfDicomFiles dicomFiles = retrieveResponseGenerator.getDicomFiles();
							int status = retrieveResponseGenerator.getStatus();
							retrieveResponseGenerator.close();
							if (status != ResponseStatus.Success || dicomFiles == null) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): retrieve failed or contains nothing, status = 0x"+Integer.toHexString(status));
								response = new CGetResponseCommandMessage(
									cgrq.getAffectedSOPClassUID(),
									cgrq.getMessageID(),
									status,
									false,				// no dataset
									retrieveResponseGenerator.getOffendingElement(),
									null				// no ErrorComment
								).getBytes();
							}
							else {
								CGetPendingResponseSender pendingResponseSender = new CGetPendingResponseSender(association,cgrq);
								pendingResponseSender.nRemaining = dicomFiles.size();		// in case fails immediately with no status updates
								{
									// WARNING - StorageSOPClassSCU will override the current ReceivedDataHandler set on the association
									// do NOT send MoveOriginatorApplicationEntityTitle or MoveOriginatorMessageID - that is only for C-MOVE
									new StorageSOPClassSCU(
										association,
										dicomFiles,
										pendingResponseSender,
										debugLevel);
									association.setReceivedDataHandler(this);	// re-establish ourselves as the handler to send done response
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): after all stored: nRemaining="+pendingResponseSender.nRemaining+" nCompleted="+pendingResponseSender.nCompleted+" nFailed="+pendingResponseSender.nFailed+" nWarning="+pendingResponseSender.nWarning);
									if (pendingResponseSender.nRemaining > 0) {
										pendingResponseSender.nFailed+=pendingResponseSender.nRemaining;
										pendingResponseSender.nRemaining=0;
									}
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): after setting remaining to zero: nRemaining="+pendingResponseSender.nRemaining+" nCompleted="+pendingResponseSender.nCompleted+" nFailed="+pendingResponseSender.nFailed+" nWarning="+pendingResponseSender.nWarning);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Bulding final C-GET success response");
									response = new CGetResponseCommandMessage(
										cgrq.getAffectedSOPClassUID(),
										cgrq.getMessageID(),
										pendingResponseSender.nFailed > 0
											? ResponseStatus.SubOperationsCompleteOneOrMoreFailures
											: ResponseStatus.SubOperationsCompleteNoFailures,
										false,				// no dataset, unless there was failure, then add Failed SOP Instance UID List (0008,0058)
										pendingResponseSender.nRemaining,
										pendingResponseSender.nCompleted,
										pendingResponseSender.nFailed,
										pendingResponseSender.nWarning
									).getBytes();
								}
							}
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Setting done flag for C-GET response");
							setDone(true);
							setRelease(false);
						}
					}
					else {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): Unexpected data fragment for command 0x"+Integer.toHexString(command)+" "+MessageServiceElementCommand.toString(command)+" - ignoring");
					}
				}
			}
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCP.CompositeCommandReceivedPDUHandler.sendPDataIndication(): finished; isDone()="+isDone());
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
//System.err.println("Association.run(): AReleaseException: "+association.getAssociationNumber()+" from "+association.getCallingAETitle()+" released");
				if (associationStatusHandler != null) {
					associationStatusHandler.sendAssociationReleaseIndication(association);
				}
			}
		}
		catch (Exception e) {
			//System.err.println(e);
			e.printStackTrace(System.err);
		}
	}
}




