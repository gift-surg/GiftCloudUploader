/* Copyright (c) 2001-2015, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.utils.*;
import com.pixelmed.dicom.*;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Set;
import java.io.*;

/**
 * <p>This class implements the SCU role of SOP Classes of the Storage Service Class.</p>
 *
 * <p>The class has no methods other than the constructor (and a main method for testing). The
 * constructor establishes an association, sends the C-STORE request, and releases the
 * association.</p>
 *
 * <p>Debugging messages with a varying degree of verbosity can be activated.</p>
 *
 * <p>For example:</p>
 * <pre>
try {
    new StorageSOPClassSCU("theirhost",11112,"STORESCP","STORESCU","/tmp/testfile.dcm","1.2.840.10008.5.1.4.1.1.7","1.3.6.1.4.1.5962.1.1.0.0.0.1064923879.2077.3232235877",0,0);
}
catch (Exception e) {
    e.printStackTrace(System.err);
}
 * </pre>
 *
 * <p>From the command line, sending multiple files:</p>
 * <pre>
find /tmp -name '*.dcm' | java -cp pixelmed.jar:lib/additional/commons-codec-1.3.jar:lib/additional/commons-compress-1.9.jar com.pixelmed.network.StorageSOPClassSCU theirhost 11112 STORESCP STORESCU -  0 0
 * </pre>
 *
 *
 * @author	dclunie
 */
public class StorageSOPClassSCU extends SOPClass {

	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/StorageSOPClassSCU.java,v 1.55 2015/01/03 21:56:04 dclunie Exp $";

	/***/
	protected int debugLevel;
	
	/***/
	protected boolean trappedExceptions;
	
	/**
	 * @return	true if in multiple instance constructors exceptions were trapped, e.g., connection or association failure before transfers attempted
	 */
	public boolean encounteredTrappedExceptions() { return trappedExceptions; } 
	
	/***/
	protected class CStoreResponseHandler extends CompositeResponseHandler {
		/**
		 * @param	debugLevel
		 */
		CStoreResponseHandler(int debugLevel) {
			super(debugLevel);
		}
		
		/**
		 * @param	list
		 */
		protected void evaluateStatusAndSetSuccess(AttributeList list) {
			// could check all sorts of things, like:
			// - AffectedSOPClassUID is what we sent
			// - CommandField is 0x8001 C-STORE-RSP
			// - MessageIDBeingRespondedTo is what we sent
			// - DataSetType is 0101 (no data set)
			// - Status is success and consider associated elements
			// - AffectedSOPInstanceUID is what we sent
			//
			// for now just treat success or warning as success (and absence as failure)
			int status = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Status,0xffff);
			success =  status == 0x0000	// success
				|| status == 0xB000	// coercion of data element
				|| status == 0xB007	// data set does not match SOP Class
				|| status == 0xB006;	// element discarded
		}
	}
	
	/**
	 * @param	association
	 * @param	affectedSOPClass
	 * @param	affectedSOPInstance
	 * @param	inputTransferSyntaxUID
	 * @param	din
	 * @param	presentationContextID
	 * @param	outputTransferSyntaxUID
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 * @throws	AReleaseException
	 */
	protected boolean sendOneSOPInstance(Association association,
			String affectedSOPClass,String affectedSOPInstance,
			String inputTransferSyntaxUID,DicomInputStream din,
			byte presentationContextID,String outputTransferSyntaxUID
			) throws AReleaseException, DicomNetworkException, DicomException, IOException {
		return sendOneSOPInstance(association,affectedSOPClass,affectedSOPInstance,inputTransferSyntaxUID,din,
			presentationContextID,outputTransferSyntaxUID,null,-1);
	}
	
	/**
	 * @param	association
	 * @param	affectedSOPClass
	 * @param	affectedSOPInstance
	 * @param	inputTransferSyntaxUID
	 * @param	din
	 * @param	presentationContextID
	 * @param	outputTransferSyntaxUID
	 * @param	moveOriginatorApplicationEntityTitle	the AET of the C-MOVE that originated this C-STORE, or null if none
	 * @param	moveOriginatorMessageID					the MessageID of the C-MOVE that originated this C-STORE, or -1 if none
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 * @throws	AReleaseException
	 */
	protected boolean sendOneSOPInstance(Association association,
			String affectedSOPClass,String affectedSOPInstance,
			String inputTransferSyntaxUID,DicomInputStream din,
			byte presentationContextID,String outputTransferSyntaxUID,
			String moveOriginatorApplicationEntityTitle,int moveOriginatorMessageID) throws AReleaseException, DicomNetworkException, DicomException, IOException {
		byte cStoreRequestCommandMessage[] = new CStoreRequestCommandMessage(affectedSOPClass,affectedSOPInstance,moveOriginatorApplicationEntityTitle,moveOriginatorMessageID).getBytes();
		CStoreResponseHandler receivedDataHandler = new CStoreResponseHandler(debugLevel);
		association.setReceivedDataHandler(receivedDataHandler);
		association.send(presentationContextID,cStoreRequestCommandMessage,null);
		OutputStream out = association.getAssociationOutputStream(presentationContextID);
		if (inputTransferSyntaxUID.equals(outputTransferSyntaxUID)) {
		//if (false) {
//if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU.sendOneSOPInstance(): same transfer syntax so raw binary copy");
			CopyStream.copy(din,out);		// be careful ... this will not remove DataSetTrailingPadding, which will kill GE AW
//if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU.sendOneSOPInstance(): back from raw binary copy");
			out.close();
		}
		else {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU.sendOneSOPInstance(): different transfer syntaxes; converting "+inputTransferSyntaxUID+" to "+outputTransferSyntaxUID);
			// din will already be positioned after meta-header and set for reading data set
			// copier will push any transfer syntax specific decompression filter onto the stream before reading
			DicomOutputStream dout = new DicomOutputStream(out,null/*meta*/,outputTransferSyntaxUID/*dataset*/);
			new DicomStreamCopier(din,dout);
			// Do not need dout.close() since DicomStreamCopier always closes output stream itself
		}
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU.sendOneSOPInstance(): about to wait for PDUs");
		association.waitForCommandPDataPDUs();
		return receivedDataHandler.wasSuccessful();
	}
	
	/**
	 * @param	association
	 * @param	affectedSOPClass
	 * @param	affectedSOPInstance
	 * @param	list
	 * @param	presentationContextID
	 * @param	outputTransferSyntaxUID
	 * @param	moveOriginatorApplicationEntityTitle	the AET of the C-MOVE that originated this C-STORE, or null if none
	 * @param	moveOriginatorMessageID					the MessageID of the C-MOVE that originated this C-STORE, or -1 if none
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 * @throws	AReleaseException
	 */
	protected boolean sendOneSOPInstance(Association association,
			String affectedSOPClass,String affectedSOPInstance,
			AttributeList list,
			byte presentationContextID,String outputTransferSyntaxUID,
			String moveOriginatorApplicationEntityTitle,int moveOriginatorMessageID) throws AReleaseException, DicomNetworkException, DicomException, IOException {
		byte cStoreRequestCommandMessage[] = new CStoreRequestCommandMessage(affectedSOPClass,affectedSOPInstance,moveOriginatorApplicationEntityTitle,moveOriginatorMessageID).getBytes();
		CStoreResponseHandler receivedDataHandler = new CStoreResponseHandler(debugLevel);
		association.setReceivedDataHandler(receivedDataHandler);
		association.send(presentationContextID,cStoreRequestCommandMessage,null);
		OutputStream out = association.getAssociationOutputStream(presentationContextID);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU.sendOneSOPInstance(): writing attribute list as "+outputTransferSyntaxUID);
		list.write(out,outputTransferSyntaxUID,false/*useMeta*/,true/*useBufferedStream*/,false/*closeAfterWrite*/);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU.sendOneSOPInstance(): about to wait for PDUs");
		association.waitForCommandPDataPDUs();
		return receivedDataHandler.wasSuccessful();
	}
	
	/**
	 * <p>Dummy constructor allows testing subclasses to use different constructor.</p>
	 *
	 */
	protected StorageSOPClassSCU() throws DicomNetworkException, DicomException, IOException {
	}
	
	/**
	 * <p>Establish an association to the specified AE, send the instance contained in the file, and release the association.</p>
	 *
	 * @param	hostname					their hostname or IP address
	 * @param	port						their port
	 * @param	calledAETitle				their AE Title
	 * @param	callingAETitle				our AE Title
	 * @param	fileName					the name of the file containing the data set to send
	 * @param	affectedSOPClass			must be the same as the SOP Class UID contained within the data set, may be null if file has a meta information header
	 * @param	affectedSOPInstance			must be the same as the SOP Instance UID contained within the data set, may be null if file has a meta information header
	 * @param	compressionLevel			0=none,1=propose deflate,2=propose deflate and bzip2
	 * @param	debugLevel					zero for no debugging messages, higher values more verbose messages
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	public StorageSOPClassSCU(String hostname,int port,String calledAETitle,String callingAETitle,String fileName,
			String affectedSOPClass,String affectedSOPInstance,int compressionLevel,
			int debugLevel) throws DicomNetworkException, DicomException, IOException {
		this(hostname,port,calledAETitle,callingAETitle,fileName,affectedSOPClass,affectedSOPInstance,compressionLevel,null,-1,debugLevel);
	}
	
	/**
	 * <p>Establish an association to the specified AE, send the instance contained in the file, and release the association.</p>
	 *
	 * @param	hostname								their hostname or IP address
	 * @param	port									their port
	 * @param	calledAETitle							their AE Title
	 * @param	callingAETitle							our AE Title
	 * @param	fileName								the name of the file containing the data set to send
	 * @param	affectedSOPClass						must be the same as the SOP Class UID contained within the data set, may be null if file has a meta information header
	 * @param	affectedSOPInstance						must be the same as the SOP Instance UID contained within the data set, may be null if file has a meta information header
	 * @param	compressionLevel						0=none,1=propose deflate,2=propose deflate and bzip2
	 * @param	moveOriginatorApplicationEntityTitle	the AET of the C-MOVE that originated this C-STORE, or null if none
	 * @param	moveOriginatorMessageID					the MessageID of the C-MOVE that originated this C-STORE, or -1 if none
	 * @param	debugLevel								zero for no debugging messages, higher values more verbose messages
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	public StorageSOPClassSCU(String hostname,int port,String calledAETitle,String callingAETitle,String fileName,
			String affectedSOPClass,String affectedSOPInstance,int compressionLevel,String moveOriginatorApplicationEntityTitle,int moveOriginatorMessageID,
			int debugLevel) throws DicomNetworkException, DicomException, IOException {
		this.debugLevel=debugLevel;
		
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU: storing "+fileName);
		// Don't even begin until we know we can open the file ...
		InputStream in = new BufferedInputStream(new FileInputStream(fileName));
		try {
			String inputTransferSyntax;
			DicomInputStream din = new DicomInputStream(in);
			if (din.haveMetaHeader()) {
				AttributeList metaList = new AttributeList();
				metaList.readOnlyMetaInformationHeader(din);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Meta header information = "+metaList);
				affectedSOPClass=Attribute.getSingleStringValueOrNull(metaList,TagFromName.MediaStorageSOPClassUID);
				affectedSOPInstance=Attribute.getSingleStringValueOrNull(metaList,TagFromName.MediaStorageSOPInstanceUID);
				inputTransferSyntax=Attribute.getSingleStringValueOrNull(metaList,TagFromName.TransferSyntaxUID);
			}
			else {
				inputTransferSyntax=din.getTransferSyntaxToReadDataSet().getUID();
			}
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Using inputTransferSyntax "+inputTransferSyntax);

			if (affectedSOPClass == null || affectedSOPClass.length() == 0) {
				throw new DicomNetworkException("Can't C-STORE SOP Instance - can't determine Affected SOP Class UID");
			}
			if (SOPClass.isDirectory(affectedSOPClass)) {
				throw new DicomNetworkException("Can't C-STORE Media Storage Directory Storage SOP Class (DICOMDIR)");
			}
			if (affectedSOPInstance == null || affectedSOPInstance.length() == 0) {
				throw new DicomNetworkException("Can't C-STORE SOP Instance - can't determine Affected SOP Instance UID");
			}
		
			PresentationContextListFactory presentationContextListFactory = new PresentationContextListFactory();
			LinkedList presentationContexts = presentationContextListFactory.createNewPresentationContextList(affectedSOPClass,inputTransferSyntax,compressionLevel);

			Association association = AssociationFactory.createNewAssociation(hostname,port,calledAETitle,callingAETitle,presentationContexts,null,false,debugLevel);
if (debugLevel > 1) System.err.println(association);
			// Decide which presentation context we are going to use ...
			byte presentationContextID = association.getSuitablePresentationContextID(affectedSOPClass);
			//int presentationContextID = association.getSuitablePresentationContextID(affectedSOPClass,TransferSyntax.Default);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Using context ID "+presentationContextID);
			String outputTransferSyntax = association.getTransferSyntaxForPresentationContextID(presentationContextID);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Using outputTransferSyntax "+outputTransferSyntax);
			if (outputTransferSyntax == null || outputTransferSyntax.length() == 0) {
				throw new DicomNetworkException("Can't C-STORE SOP Instance - can't determine Transfer Syntax (no Presentation Context for Affected SOP Class UID)");
			}
			boolean success = false;
			try {
				success = sendOneSOPInstance(association,affectedSOPClass,affectedSOPInstance,
					inputTransferSyntax,din,
					presentationContextID,outputTransferSyntax,
					moveOriginatorApplicationEntityTitle,moveOriginatorMessageID);
				// State 6
				association.release();
			}
			catch (AReleaseException e) {
				// State 1
				// the other end released and didn't wait for us to do it
			}
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": Send "+fileName+" "+(success ? "succeeded" : "failed"));
		}
		finally {
			in.close();
		}
	}
	
	/**
	 * <p>Send the specified instances contained in the files over an existing association.</p>
	 *
	 * @param	association				already existing association to SCP
	 * @param	dicomFiles				the set of DICOM files containing names, SOP Class UIDs, SOP Instance UIDs and optionally Transfer Syntaxes
	 * @param	debugLevel				zero for no debugging messages, higher values more verbose messages
	 */
	public StorageSOPClassSCU(Association association,SetOfDicomFiles dicomFiles,MultipleInstanceTransferStatusHandler multipleInstanceTransferStatusHandler,
			int debugLevel) {
		this(association,dicomFiles,multipleInstanceTransferStatusHandler,null,-1,debugLevel);
	}

	/**
	 * <p>Send the specified instances contained in the files over an existing association.</p>
	 *
	 * @param	association								already existing association to SCP
	 * @param	dicomFiles								the set of DICOM files containing names, SOP Class UIDs, SOP Instance UIDs and optionally Transfer Syntaxes
	 * @param	moveOriginatorApplicationEntityTitle	the AET of the C-MOVE that originated this C-STORE, or null if none
	 * @param	moveOriginatorMessageID					the MessageID of the C-MOVE that originated this C-STORE, or -1 if none
	 * @param	debugLevel								zero for no debugging messages, higher values more verbose messages
	 */
	public StorageSOPClassSCU(Association association,SetOfDicomFiles dicomFiles,MultipleInstanceTransferStatusHandler multipleInstanceTransferStatusHandler,
			String moveOriginatorApplicationEntityTitle,int moveOriginatorMessageID,int debugLevel) {
		this.debugLevel=debugLevel;
		try {
			sendMultipleSOPInstances(association,dicomFiles,multipleInstanceTransferStatusHandler,moveOriginatorApplicationEntityTitle,moveOriginatorMessageID);
		}
		catch (AReleaseException e) {
			// State 1
			// the other end released
		}
		catch (DicomNetworkException e) {
			trappedExceptions = true;
			e.printStackTrace(System.err);
		}
		catch (IOException e) {
			trappedExceptions = true;
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * <p>Establish an association to the specified AE, send the instances contained in the files, and release the association.</p>
	 *
	 * @param	hostname								their hostname or IP address
	 * @param	port									their port
	 * @param	calledAETitle							their AE Title
	 * @param	callingAETitle							our AE Title
	 * @param	dicomFiles								the set of DICOM files containing names, SOP Class UIDs, SOP Instance UIDs and optionally Transfer Syntaxes
	 * @param	compressionLevel						0=none,1=propose deflate,2=propose deflate and bzip2
	 * @param	multipleInstanceTransferStatusHandler	transfer handler for reporting pending status (may be null if not required)
	 * @param	debugLevel								zero for no debugging messages, higher values more verbose messages
	 */
	public StorageSOPClassSCU(String hostname,int port,String calledAETitle,String callingAETitle,
			SetOfDicomFiles dicomFiles,int compressionLevel,MultipleInstanceTransferStatusHandler multipleInstanceTransferStatusHandler,
			int debugLevel) {
		this(hostname,port,calledAETitle,callingAETitle,dicomFiles,compressionLevel,multipleInstanceTransferStatusHandler,null,-1,debugLevel);
	}

	/**
	 * <p>Establish an association to the specified AE, send the instances contained in the files, and release the association.</p>
	 *
	 * @param	hostname								their hostname or IP address
	 * @param	port									their port
	 * @param	calledAETitle							their AE Title
	 * @param	callingAETitle							our AE Title
	 * @param	dicomFiles								the set of DICOM files containing names, SOP Class UIDs, SOP Instance UIDs and optionally Transfer Syntaxes
	 * @param	compressionLevel						0=none,1=propose deflate,2=propose deflate and bzip2
	 * @param	multipleInstanceTransferStatusHandler	transfer handler for reporting pending status (may be null if not required)
	 * @param	moveOriginatorApplicationEntityTitle	the AET of the C-MOVE that originated this C-STORE, or null if none
	 * @param	moveOriginatorMessageID					the MessageID of the C-MOVE that originated this C-STORE, or -1 if none
	 * @param	debugLevel								zero for no debugging messages, higher values more verbose messages
	 */
	public StorageSOPClassSCU(String hostname,int port,String calledAETitle,String callingAETitle,
			SetOfDicomFiles dicomFiles,int compressionLevel,MultipleInstanceTransferStatusHandler multipleInstanceTransferStatusHandler,
			String moveOriginatorApplicationEntityTitle,int moveOriginatorMessageID,int debugLevel) {
//long startTime=System.currentTimeMillis();
		this.debugLevel=debugLevel;
		if (!dicomFiles.isEmpty()) {
			try {
				PresentationContextListFactory presentationContextListFactory = new PresentationContextListFactory();
				LinkedList presentationContexts = presentationContextListFactory.createNewPresentationContextList(dicomFiles,compressionLevel);
				Association association = AssociationFactory.createNewAssociation(hostname,port,calledAETitle,callingAETitle,presentationContexts,null,false,debugLevel);
//System.err.println("StorageSOPClassSCU.StorageSOPClassSCU() established association in "+(System.currentTimeMillis()-startTime)+" ms");
			
				sendMultipleSOPInstances(association,dicomFiles,multipleInstanceTransferStatusHandler,moveOriginatorApplicationEntityTitle,moveOriginatorMessageID);
			
				association.release();
			}
			catch (AReleaseException e) {
				// State 1
				// the other end released and didn't wait for us to do it
			}
			catch (DicomNetworkException e) {
				trappedExceptions = true;
				e.printStackTrace(System.err);
			}
			catch (IOException e) {
				trappedExceptions = true;
				e.printStackTrace(System.err);
			}
		}
		else {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Not opening an association since no instances to send");
		}
	}

	/**
	 * <p>Establish an association to the specified AE, send the instances contained in the attribute lists, and release the association.</p>
	 *
	 * <p>Deprecated because establishing presentation contexts based on the set of SOP Classes without knowledge of encoded Transfer Syntax may lead to failure during C-STORE
	 * because of inability to convert.</p>
	 *
	 * @param	hostname								their hostname or IP address
	 * @param	port									their port
	 * @param	calledAETitle							their AE Title
	 * @param	callingAETitle							our AE Title
	 * @param	setOfSOPClassUIDs						the set of SOP Classes contained in the attribute lists
	 * @param	lists									the attribute lists to send
	 * @param	compressionLevel						0=none,1=propose deflate,2=propose deflate and bzip2
	 * @param	multipleInstanceTransferStatusHandler	transfer handler for reporting pending status (may be null if not required)
	 * @param	moveOriginatorApplicationEntityTitle	the AET of the C-MOVE that originated this C-STORE, or null if none
	 * @param	moveOriginatorMessageID					the MessageID of the C-MOVE that originated this C-STORE, or -1 if none
	 * @param	debugLevel								zero for no debugging messages, higher values more verbose messages
	 * @deprecated use  {@link #StorageSOPClassSCU(String,int,String,String,SetOfDicomFiles,int,MultipleInstanceTransferStatusHandler,int) StorageSOPClassSCU(String hostname,int port,String calledAETitle,String callingAETitle,
			SetOfDicomFiles dicomFiles,int compressionLevel,MultipleInstanceTransferStatusHandler multipleInstanceTransferStatusHandler,
			String moveOriginatorApplicationEntityTitle,int moveOriginatorMessageID,int debugLevel)} instead
	 */
	public StorageSOPClassSCU(String hostname,int port,String calledAETitle,String callingAETitle,
			Set setOfSOPClassUIDs,AttributeList[] lists,
			int compressionLevel,MultipleInstanceTransferStatusHandler multipleInstanceTransferStatusHandler,
			String moveOriginatorApplicationEntityTitle,int moveOriginatorMessageID,int debugLevel) {
//long startTime=System.currentTimeMillis();
		this.debugLevel=debugLevel;
		if (lists.length > 0) {
			try {
				PresentationContextListFactory presentationContextListFactory = new PresentationContextListFactory();
				LinkedList presentationContexts = presentationContextListFactory.createNewPresentationContextList(setOfSOPClassUIDs,compressionLevel);
				Association association = AssociationFactory.createNewAssociation(hostname,port,calledAETitle,callingAETitle,presentationContexts,null,false,debugLevel);
//System.err.println("StorageSOPClassSCU.StorageSOPClassSCU() established association in "+(System.currentTimeMillis()-startTime)+" ms");
			
				sendMultipleSOPInstances(association,lists,multipleInstanceTransferStatusHandler,moveOriginatorApplicationEntityTitle,moveOriginatorMessageID);
			
				association.release();
			}
			catch (AReleaseException e) {
				// State 1
				// the other end released and didn't wait for us to do it
			}
			catch (DicomNetworkException e) {
				trappedExceptions = true;
				e.printStackTrace(System.err);
			}
			catch (IOException e) {
				trappedExceptions = true;
				e.printStackTrace(System.err);
			}
		}
		else {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Not opening an association since no instances to send");
		}
	}
			
	/**
	 * <p>Send the specified instances contained in the files over an existing association.</p>
	 *
	 * @param	association								already existing association to SCP
	 * @param	dicomFiles								the set of DICOM files containing names, SOP Class UIDs, SOP Instance UIDs and optionally Transfer Syntaxes
	 * @param	multipleInstanceTransferStatusHandler	handler called after each transfer (may be null if not required)
	 * @param	moveOriginatorApplicationEntityTitle	the AET of the C-MOVE that originated this C-STORE, or null if none
	 * @param	moveOriginatorMessageID					the MessageID of the C-MOVE that originated this C-STORE, or -1 if none
	 * @throws	AReleaseException
	 * @throws	DicomNetworkException
	 * @throws	IOException
	 */
	protected void sendMultipleSOPInstances(Association association,SetOfDicomFiles dicomFiles,MultipleInstanceTransferStatusHandler multipleInstanceTransferStatusHandler,
				String moveOriginatorApplicationEntityTitle,int moveOriginatorMessageID)
			throws AReleaseException, DicomNetworkException, IOException {
//long startTime=System.currentTimeMillis();
		int nRemaining = dicomFiles.size();
		int nCompleted = 0;
		int nFailed = 0;
		int nWarning = 0;
		{
if (debugLevel > 1) System.err.println(association);
			Iterator fi = dicomFiles.iterator();
			while (fi.hasNext()) {
				--nRemaining;
				++nCompleted;
				SetOfDicomFiles.DicomFile dicomFile = (SetOfDicomFiles.DicomFile)(fi.next());
				String fileName = dicomFile.getFileName();
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Sending "+fileName);
				boolean success = false;
				String affectedSOPInstance = null;
				try {
					InputStream in = new BufferedInputStream(new FileInputStream(fileName));
					try {
						String inputTransferSyntax = null;
						String affectedSOPClass = null;
						DicomInputStream din = new DicomInputStream(in);
						if (din.haveMetaHeader()) {
							AttributeList metaList = new AttributeList();
							metaList.readOnlyMetaInformationHeader(din);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Meta header information = "+metaList);
							affectedSOPClass=Attribute.getSingleStringValueOrNull(metaList,TagFromName.MediaStorageSOPClassUID);
							affectedSOPInstance=Attribute.getSingleStringValueOrNull(metaList,TagFromName.MediaStorageSOPInstanceUID);
							inputTransferSyntax=Attribute.getSingleStringValueOrNull(metaList,TagFromName.TransferSyntaxUID);
						}
						else {
							affectedSOPClass=dicomFile.getSOPClassUID();
							affectedSOPInstance=dicomFile.getSOPInstanceUID();
							inputTransferSyntax=din.getTransferSyntaxToReadDataSet().getUID();
						}
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU(): affectedSOPClass = "+affectedSOPClass);
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU(): affectedSOPInstance = "+affectedSOPInstance);
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU(): inputTransferSyntax = "+inputTransferSyntax);

						if (affectedSOPClass == null || affectedSOPClass.length() == 0) {
							throw new DicomNetworkException("Can't C-STORE SOP Instance - can't determine Affected SOP Class UID");
						}
						if (SOPClass.isDirectory(affectedSOPClass)) {
							throw new DicomNetworkException("Can't C-STORE Media Storage Directory Storage SOP Class (DICOMDIR)");
						}
						if (affectedSOPInstance == null || affectedSOPInstance.length() == 0) {
							throw new DicomNetworkException("Can't C-STORE SOP Instance - can't determine Affected SOP Instance UID");
						}

						// Decide which presentation context we are going to use ...
						byte presentationContextID = association.getSuitablePresentationContextID(affectedSOPClass);
						//int presentationContextID = association.getSuitablePresentationContextID(affectedSOPClass,TransferSyntax.Default);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Using context ID "+presentationContextID);
						String outputTransferSyntax = association.getTransferSyntaxForPresentationContextID(presentationContextID);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Using outputTransferSyntax "+outputTransferSyntax);
						if (outputTransferSyntax == null || outputTransferSyntax.length() == 0) {
							throw new DicomNetworkException("Can't C-STORE SOP Instance - can't determine Transfer Syntax (no Presentation Context for Affected SOP Class UID)");
						}
				
						success = sendOneSOPInstance(association,affectedSOPClass,affectedSOPInstance,
							inputTransferSyntax,din,
							presentationContextID,outputTransferSyntax,moveOriginatorApplicationEntityTitle,moveOriginatorMessageID);
						// State 6
					}
					finally {
						in.close();
					}
				}
				catch (DicomNetworkException e) {
					e.printStackTrace(System.err);
					success=false;
				}
				catch (DicomException e) {
					e.printStackTrace(System.err);
					success=false;
				}
				catch (IOException e) {
					e.printStackTrace(System.err);
					success=false;
				}
				if (!success) {
					++nFailed;
					trappedExceptions = true;
				}
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": Send "+fileName+" "+(success ? "succeeded" : "failed")+" between "+association.getEndpointDescription());
				if (multipleInstanceTransferStatusHandler != null) {
					if (multipleInstanceTransferStatusHandler instanceof MultipleInstanceTransferStatusHandlerWithFileName) {
						((MultipleInstanceTransferStatusHandlerWithFileName)multipleInstanceTransferStatusHandler).updateStatus(nRemaining,nCompleted,nFailed,nWarning,affectedSOPInstance,fileName,success);
					}
					else {
						multipleInstanceTransferStatusHandler.updateStatus(nRemaining,nCompleted,nFailed,nWarning,affectedSOPInstance);
					}
				}
			}
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU(): Finished sending all files nRemaining="+nRemaining+" nCompleted="+nCompleted+" nFailed="+nFailed+" nWarning="+nWarning+" between "+association.getEndpointDescription());
		}
//System.err.println("StorageSOPClassSCU.sendMultipleSOPInstances() sent "+nCompleted+" files in "+(System.currentTimeMillis()-startTime)+" ms");
	}
			
	/**
	 * <p>Send the specified instances contained in the attribute lists over an existing association.</p>
	 *
	 * @param	association								already existing association to SCP
	 * @param	lists									the array of attribute lists to send
	 * @param	multipleInstanceTransferStatusHandler	handler called after each transfer (may be null if not required)
	 * @param	moveOriginatorApplicationEntityTitle	the AET of the C-MOVE that originated this C-STORE, or null if none
	 * @param	moveOriginatorMessageID					the MessageID of the C-MOVE that originated this C-STORE, or -1 if none
	 * @throws	AReleaseException
	 * @throws	DicomNetworkException
	 * @throws	IOException
	 */
	protected void sendMultipleSOPInstances(Association association,AttributeList[] lists,MultipleInstanceTransferStatusHandler multipleInstanceTransferStatusHandler,
				String moveOriginatorApplicationEntityTitle,int moveOriginatorMessageID)
			throws AReleaseException, DicomNetworkException, IOException {
//long startTime=System.currentTimeMillis();
		int nRemaining = lists.length;
		int nCompleted = 0;
		int nFailed = 0;
		int nWarning = 0;
		{
if (debugLevel > 1) System.err.println(association);
			for (int i=0; i<lists.length; ++i) {
				--nRemaining;
				++nCompleted;
				AttributeList list = lists[i];
				boolean success = false;
				String affectedSOPInstance = null;
				try {
					String affectedSOPClass = null;
					affectedSOPClass=Attribute.getSingleStringValueOrNull(list,TagFromName.SOPClassUID);
					affectedSOPInstance=Attribute.getSingleStringValueOrNull(list,TagFromName.SOPInstanceUID);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Sending "+affectedSOPInstance);
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU(): affectedSOPClass = "+affectedSOPClass);
//if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU(): affectedSOPInstance = "+affectedSOPInstance);

					if (affectedSOPClass == null) {
						throw new DicomNetworkException("Can't C-STORE SOP Instance - can't determine Affected SOP Class UID");
					}
					if (affectedSOPInstance == null) {
						throw new DicomNetworkException("Can't C-STORE SOP Instance - can't determine Affected SOP Instance UID");
					}

					// Decide which presentation context we are going to use ...
					byte presentationContextID = association.getSuitablePresentationContextID(affectedSOPClass);
					//int presentationContextID = association.getSuitablePresentationContextID(affectedSOPClass,TransferSyntax.Default);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Using context ID "+presentationContextID);
					String outputTransferSyntax = association.getTransferSyntaxForPresentationContextID(presentationContextID);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Using outputTransferSyntax "+outputTransferSyntax);
					if (outputTransferSyntax == null || outputTransferSyntax.length() == 0) {
						throw new DicomNetworkException("Can't C-STORE SOP Instance - can't determine Transfer Syntax (no Presentation Context for Affected SOP Class UID)");
					}
				
					success = sendOneSOPInstance(association,affectedSOPClass,affectedSOPInstance,
						list,
						presentationContextID,outputTransferSyntax,moveOriginatorApplicationEntityTitle,moveOriginatorMessageID);
					// State 6
				}
				catch (DicomNetworkException e) {
					e.printStackTrace(System.err);
					success=false;
				}
				catch (DicomException e) {
					e.printStackTrace(System.err);
					success=false;
				}
				catch (IOException e) {
					e.printStackTrace(System.err);
					success=false;
				}
				if (!success) {
					++nFailed;
					trappedExceptions = true;
				}
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": Send "+affectedSOPInstance+" "+(success ? "succeeded" : "failed")+" between "+association.getEndpointDescription());
				if (multipleInstanceTransferStatusHandler != null) {
					multipleInstanceTransferStatusHandler.updateStatus(nRemaining,nCompleted,nFailed,nWarning,affectedSOPInstance);
				}
			}
if (debugLevel > 0) System.err.println(new java.util.Date().toString()+": StorageSOPClassSCU(): Finished sending all files nRemaining="+nRemaining+" nCompleted="+nCompleted+" nFailed="+nFailed+" nWarning="+nWarning+" between "+association.getEndpointDescription());
		}
//System.err.println("StorageSOPClassSCU.sendMultipleSOPInstances() sent "+nCompleted+" instances in "+(System.currentTimeMillis()-startTime)+" ms");
	}

	/**
	 * <p>For testing, establish an association to the specified AE and send one or more DICOM instances (C-STORE requests).</p>
	 *
	 * @param	arg	array of seven or nine strings - their hostname, their port, their AE Title, our AE Title,
	 *			the filename containing the instance to send (or a hyphen '-' if a list of one or more filenames is to be read from stdin)
	 * 			optionally the SOP Class and the SOP Instance (otherwise will be read from the file(s); if multiple files use an empty string for the SOP Instance),
	 *			the compression level (0=none,1=propose deflate,2=propose deflate and bzip2) and the debugging level
	 */
	public static void main(String arg[]) {
		try {
			String      theirHost=null;
			int         theirPort=-1;
			String   theirAETitle=null;
			String     ourAETitle=null;
			String       fileName=null;
			String    SOPClassUID=null;
			String SOPInstanceUID=null;
			int  compressionLevel=0;
			int        debugLevel=0;
	
			if (arg.length == 9) {
				     theirHost=arg[0];
				     theirPort=Integer.parseInt(arg[1]);
				  theirAETitle=arg[2];
				    ourAETitle=arg[3];
				      fileName=arg[4];
				   SOPClassUID=arg[5];
				SOPInstanceUID=arg[6];
			      compressionLevel=Integer.parseInt(arg[7]);
				    debugLevel=Integer.parseInt(arg[8]);
			}
			else if (arg.length == 7) {
				     theirHost=arg[0];
				     theirPort=Integer.parseInt(arg[1]);
				  theirAETitle=arg[2];
				    ourAETitle=arg[3];
				      fileName=arg[4];
				   SOPClassUID=null;			// figured out by StorageSOPClassSCU() by reading the metaheader
				SOPInstanceUID=null;			// figured out by StorageSOPClassSCU() by reading the metaheader
			      compressionLevel=Integer.parseInt(arg[5]);
				    debugLevel=Integer.parseInt(arg[6]);
			}
			else {
				throw new Exception("Argument list must be 7 or 9 values");
			}
			if (fileName.equals("-")) {
				SetOfDicomFiles setOfDicomFiles = new SetOfDicomFiles();
				BufferedReader dicomFileNameReader = new BufferedReader(new InputStreamReader(System.in));
				String dicomFileName = dicomFileNameReader.readLine();
				while (dicomFileName != null) {
					if (SOPClassUID == null) {
						setOfDicomFiles.add(dicomFileName);
					}
					else {
						setOfDicomFiles.add(dicomFileName,SOPClassUID,null,null);	// OK to leave instance and transfer syntax uids as null; only need SOP Class to negotiate
					}
					dicomFileName = dicomFileNameReader.readLine();
				}
//System.err.println(setOfDicomFiles.toString());
				new StorageSOPClassSCU(theirHost,theirPort,theirAETitle,ourAETitle,setOfDicomFiles,compressionLevel,null,null,0,debugLevel);
			}
			else {
				new StorageSOPClassSCU(theirHost,theirPort,theirAETitle,ourAETitle,fileName,SOPClassUID,SOPInstanceUID,compressionLevel,debugLevel);
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(0);
		}
	}
}




