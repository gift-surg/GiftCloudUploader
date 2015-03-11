/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;
import com.pixelmed.dicom.*;

import java.util.LinkedList;
import java.io.IOException;

/**
 * <p>This class implements the SCU role of C-FIND SOP Classes.</p>
 *
 * <p>The class has no methods other than the constructor (and a main method for testing). The
 * constructor establishes an association, sends the C-FIND request, and releases the
 * association. Any identifiers received are handled by the supplied
 * {@link com.pixelmed.network.IdentifierHandler IdentifierHandler}.</p>
 *
 * <p>Debugging messages with a varying degree of verbosity can be activated.</p>
 *
 * <p>For example:</p>
 * <pre>
try {
    SpecificCharacterSet specificCharacterSet = new SpecificCharacterSet((String[])null);
    AttributeList identifier = new AttributeList();
    { AttributeTag t = TagFromName.QueryRetrieveLevel; Attribute a = new CodeStringAttribute(t); a.addValue("STUDY"); identifier.put(t,a); }
    { AttributeTag t = TagFromName.PatientID; Attribute a = new LongStringAttribute(t,specificCharacterSet); a.addValue(""); identifier.put(t,a); }
    { AttributeTag t = TagFromName.StudyInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); a.addValue(""); identifier.put(t,a); }
    new FindSOPClassSCU("theirhost","104","FINDSCP","FINDSCU",SOPClass.StudyRootQueryRetrieveInformationModelFind,identifier,new IdentifierHandler(),1);
}
catch (Exception e) {
    e.printStackTrace(System.err);
}
 * </pre>
 *
 * @see com.pixelmed.network.IdentifierHandler
 *
 * @author	dclunie
 */
public class FindSOPClassSCU extends SOPClass {

	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/FindSOPClassSCU.java,v 1.26 2014/09/09 20:34:09 dclunie Exp $";

	/***/
	private int debugLevel;

	/***/
	private class CFindResponseHandler extends CompositeResponseHandler {
		/***/
		private IdentifierHandler identifierHandler;

		/**
		 * @param	identifierHandler
		 * @param	debugLevel
		 */
		CFindResponseHandler(IdentifierHandler identifierHandler,int debugLevel) {
			super(debugLevel);
			this.identifierHandler=identifierHandler;
			allowData=true;
		}
		
		/**
		 * @param	list
		 */
		protected void evaluateStatusAndSetSuccess(AttributeList list) {
if (debugLevel > 1) System.err.println("FindSOPClassSCU.CFindResponseHandler.evaluateStatusAndSetSuccess:");
if (debugLevel > 1) System.err.println(list);
			// could check all sorts of things, like:
			// - AffectedSOPClassUID is what we sent
			// - CommandField is 0x8020 C-Find-RSP
			// - MessageIDBeingRespondedTo is what we sent
			// - DataSetType is 0101 for success (no data set) or other for pending
			// - Status is success and consider associated elements
			//
			// for now just treat success or warning as success (and absence as failure)
			status = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Status,0xffff);
if (debugLevel > 1) System.err.println("FindSOPClassSCU.CFindResponseHandler.evaluateStatusAndSetSuccess: status = 0x"+Integer.toHexString(status));
			// possible statuses at this point are:
			// A700 Refused - Out of Resources	
			// A900 Failed - Identifier does not match SOP Class	
			// Cxxx Failed - Unable to process	
			// FE00 Cancel - Matching terminated due to Cancel request	
			// 0000 Success - Matching is complete - No final Identifier is supplied.	
			// FF00 Pending - Matches are continuing - Current Match is supplied and any Optional Keys were supported in the same manner as Required Keys.
			// FF01 Pending - Matches are continuing - Warning that one or more Optional Keys were not supported for existence and/or matching for this Identifier.

			success = status == 0x0000;	// success
			
			if (status != 0xFF00 && status != 0xFF01) {
if (debugLevel > 1) System.err.println("FindSOPClassSCU.CFindResponseHandler.evaluateStatusAndSetSuccess: status no longer pending, so stop");
				setDone(true);
			}
		}
		
		/**
		 * @param	list
		 */
		protected void makeUseOfDataSet(AttributeList list) {
if (debugLevel > 1) System.err.println("FindSOPClassSCU.CFindResponseHandler.makeUseOfDataSet:");
if (debugLevel > 1) System.err.print(list);
			try {
				identifierHandler.doSomethingWithIdentifier(list);
			}
			catch (DicomException e) {
				// do not stop ... other identifiers may be OK
				e.printStackTrace(System.err);
			}
		}
	}
	
	/**
	 * @param	hostname			their hostname or IP address
	 * @param	port				their port
	 * @param	calledAETitle		their AE Title
	 * @param	callingAETitle		our AE Title
	 * @param	affectedSOPClass	the SOP Class defining which query model, e.g. {@link com.pixelmed.dicom.SOPClass#StudyRootQueryRetrieveInformationModelFind SOPClass.StudyRootQueryRetrieveInformationModelFind}
	 * @param	debugLevel			zero for no debugging messages, higher values more verbose messages
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	public static Association getSuitableAssociation(String hostname,int port,String calledAETitle,String callingAETitle,String affectedSOPClass,int debugLevel) throws DicomNetworkException, DicomException, IOException {
		LinkedList presentationContexts = new LinkedList();
		{
			LinkedList tslist = new LinkedList();
			tslist.add(TransferSyntax.Default);
			tslist.add(TransferSyntax.ExplicitVRLittleEndian);
			presentationContexts.add(new PresentationContext((byte)0x01,affectedSOPClass,tslist));
		}
		presentationContexts.add(new PresentationContext((byte)0x03,affectedSOPClass,TransferSyntax.ImplicitVRLittleEndian));
		presentationContexts.add(new PresentationContext((byte)0x05,affectedSOPClass,TransferSyntax.ExplicitVRLittleEndian));

		Association association = AssociationFactory.createNewAssociation(hostname,port,calledAETitle,callingAETitle,presentationContexts,null,false,debugLevel);
if (debugLevel > 1) System.err.println(association);
		return association;
	}

	/**
	 * @param	association			the already established Association to use
	 * @param	affectedSOPClass	the SOP Class defining which query model, e.g. {@link com.pixelmed.dicom.SOPClass#StudyRootQueryRetrieveInformationModelFind SOPClass.StudyRootQueryRetrieveInformationModelFind}
	 * @param	identifier			the list of matching and return keys
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 * @throws	AReleaseException
	 */
	public void performFind(Association association,String affectedSOPClass,AttributeList identifier) throws DicomNetworkException, DicomException, IOException, AReleaseException {
if (debugLevel > 1) System.err.println("FindSOPClassSCU(): request identifier");
if (debugLevel > 1) System.err.print(identifier);

		// Decide which presentation context we are going to use ...
		byte usePresentationContextID = association.getSuitablePresentationContextID(affectedSOPClass);
if (debugLevel > 1) System.err.println("Using context ID "+usePresentationContextID);
		byte cFindRequestCommandMessage[] = new CFindRequestCommandMessage(affectedSOPClass).getBytes();
		byte cFindIdentifier[] = new IdentifierMessage(identifier,association.getTransferSyntaxForPresentationContextID(usePresentationContextID)).getBytes();
		// for some reason association.send(usePresentationContextID,cFindRequestCommandMessage,cFindIdentifier) fails with Oldenburg imagectn
		// so send the command and the identifier separately ...
		// (was probably because wasn't setting the last fragment flag on the command in Association.send() DAC. 2004/06/10)
		// (see [bugs.mrmf] (000114) Failing to set last fragment on command when sending command and data in same PDU)
		association.send(usePresentationContextID,cFindRequestCommandMessage,null);
		association.send(usePresentationContextID,null,cFindIdentifier);
if (debugLevel > 1) System.err.println("FindSOPClassSCU: waiting for PDUs");
		association.waitForPDataPDUsUntilHandlerReportsDone();
if (debugLevel > 1) System.err.println("FindSOPClassSCU: got PDU");
		// State 6
	}

	/**
	 * @param	association			the already established Association to use
	 * @param	affectedSOPClass	the SOP Class defining which query model, e.g. {@link com.pixelmed.dicom.SOPClass#StudyRootQueryRetrieveInformationModelFind SOPClass.StudyRootQueryRetrieveInformationModelFind}
	 * @param	identifier			the list of matching and return keys
	 * @param	identifierHandler	the handler to use for each returned identifier
	 * @param	debugLevel			zero for no debugging messages, higher values more verbose messages
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	public FindSOPClassSCU(Association association,
			String affectedSOPClass,AttributeList identifier,IdentifierHandler identifierHandler,
			int debugLevel) throws DicomNetworkException, DicomException, IOException {
		this.debugLevel=debugLevel;
		CFindResponseHandler responseHandler = new CFindResponseHandler(identifierHandler,debugLevel);
		association.setReceivedDataHandler(responseHandler);
		try {
			performFind(association,affectedSOPClass,identifier);
			// State 6
		}
		catch (AReleaseException e) {
			// State 1
			// the other end released and didn't wait for us to do it
			association = null;
		}
		if (!responseHandler.wasSuccessful()) {
			throw new DicomNetworkException("C-FIND reports failure status 0x"+Integer.toString(responseHandler.getStatus()&0xFFFF,16));
		}
	}

	/**
	 * @param	hostname			their hostname or IP address
	 * @param	port				their port
	 * @param	calledAETitle		their AE Title
	 * @param	callingAETitle		our AE Title
	 * @param	affectedSOPClass	the SOP Class defining which query model, e.g. {@link com.pixelmed.dicom.SOPClass#StudyRootQueryRetrieveInformationModelFind SOPClass.StudyRootQueryRetrieveInformationModelFind}
	 * @param	identifier			the list of matching and return keys
	 * @param	identifierHandler	the handler to use for each returned identifier
	 * @param	debugLevel			zero for no debugging messages, higher values more verbose messages
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	public FindSOPClassSCU(String hostname,int port,String calledAETitle,String callingAETitle,
			String affectedSOPClass,AttributeList identifier,IdentifierHandler identifierHandler,
			int debugLevel) throws DicomNetworkException, DicomException, IOException {
		this.debugLevel=debugLevel;
		Association association = getSuitableAssociation(hostname,port,calledAETitle,callingAETitle,affectedSOPClass,debugLevel);
		CFindResponseHandler responseHandler = new CFindResponseHandler(identifierHandler,debugLevel);
		association.setReceivedDataHandler(responseHandler);
		try {
			performFind(association,affectedSOPClass,identifier);
if (debugLevel > 1) System.err.println("FindSOPClassSCU: releasing association");
			// State 6
			association.release();
		}
		catch (AReleaseException e) {
			// State 1
			// the other end released and didn't wait for us to do it
			association = null;
		}
		if (!responseHandler.wasSuccessful()) {
			throw new DicomNetworkException("C-FIND reports failure status 0x"+Integer.toString(responseHandler.getStatus()&0xFFFF,16));
		}
	}

	/**
	 * <p>For testing, establish an association to the specified AE and perform a study root query (send a C-FIND request),
	 * for all studies.</p>
	 *
	 * @param	arg	array of four or five strings - their hostname, their port, their AE Title, our AE Title, and optionally an integer debug level
	 */
	public static void main(String arg[]) {
		int debugLevel = arg.length > 4 ? Integer.parseInt(arg[4]) : 1;
		try {
			SpecificCharacterSet specificCharacterSet = new SpecificCharacterSet((String[])null);
			AttributeList identifier = new AttributeList();
			
			boolean testimagelevel = true;
			
			if (!testimagelevel) {
				identifier.putNewAttribute(TagFromName.QueryRetrieveLevel).addValue("STUDY");
				identifier.putNewAttribute(TagFromName.PatientName,specificCharacterSet);
				identifier.putNewAttribute(TagFromName.PatientID,specificCharacterSet);
				identifier.putNewAttribute(TagFromName.PatientBirthDate);
				identifier.putNewAttribute(TagFromName.PatientSex);
				identifier.putNewAttribute(TagFromName.StudyInstanceUID);
				identifier.putNewAttribute(TagFromName.ReferringPhysicianName,specificCharacterSet);
				identifier.putNewAttribute(TagFromName.ModalitiesInStudy);
				identifier.putNewAttribute(TagFromName.StudyDescription,specificCharacterSet);
				identifier.putNewAttribute(TagFromName.StudyID,specificCharacterSet);
				identifier.putNewAttribute(TagFromName.AccessionNumber,specificCharacterSet);
			
				identifier.putNewAttribute(TagFromName.QueryRetrieveLevel).addValue("SERIES");
				{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue("1.2.840.113704.1.111.5740.1224249944.1"); identifier.put(a); }
				identifier.putNewAttribute(TagFromName.SeriesInstanceUID);
			}
			else {
				identifier.putNewAttribute(TagFromName.QueryRetrieveLevel).addValue("IMAGE");
				{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue("1.2.840.113619.2.5.1762386977.1328.985934491.590"); identifier.put(a); }
				{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue("1.2.840.113619.2.5.1762386977.1328.985934491.643"); identifier.put(a); }
				
				identifier.putNewAttribute(TagFromName.SOPInstanceUID);
				identifier.putNewAttribute(TagFromName.AlternateRepresentationSequence);	// (000671)
				identifier.putNewAttribute(TagFromName.InstanceNumber);
			}
			
			new FindSOPClassSCU(arg[0],Integer.parseInt(arg[1]),arg[2],arg[3],SOPClass.StudyRootQueryRetrieveInformationModelFind,identifier,new IdentifierHandler(),debugLevel);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(0);
		}
	}
}




