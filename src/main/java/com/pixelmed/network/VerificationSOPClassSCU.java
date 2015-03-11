/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.TagFromName;

import java.util.LinkedList;
import java.io.IOException;

/**
 * <p>This class implements the SCU role of the Verification SOP Class.</p>
 *
 * <p>The class has no methods other than the constructor (and a main method for testing). The
 * constructor establishes an association, sends the C-ECHO request, and releases the
 * association.</p>
 *
 * <p>Debugging messages with a varying degree of verbosity can be activated.</p>
 *
 * <p>For example:</p>
 * <pre>
try {
    new VerificationSOPClassSCU("theirhost","104","ECHOSCP","ECHOSCU",0);
}
catch (Exception e) {
    e.printStackTrace(System.err);
}
 * </pre>
 *
 * @author	dclunie
 */
public class VerificationSOPClassSCU extends SOPClass {

	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/VerificationSOPClassSCU.java,v 1.20 2014/09/09 20:34:09 dclunie Exp $";

	/***/
	private int debugLevel;

	/***/
	protected class CEchoResponseHandler extends CompositeResponseHandler {
		/**
		 * @param	debugLevel
		 */
		CEchoResponseHandler(int debugLevel) {
			super(debugLevel);
		}
		/**
		 * @param	list
		 */
		protected void evaluateStatusAndSetSuccess(AttributeList list) {
			// could check all sorts of things, like:
			// - AffectedSOPClassUID is what we sent
			// - CommandField is 0x8030 C-ECHO-RSP
			// - MessageIDBeingRespondedTo is what we sent
			// - DataSetType is 0101 (no data set)
			// - Status is success and consider associated elements
			//
			// for now just treat success or warning as success (and absence as failure)
			status = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Status,0xffff);
			success =  status == 0x0000;	// success
		}
	}
	
	/**
	 * <p>Establish an association to the specified AE, perform verification (send a C-ECHO request), and release the association.</p>
	 *
	 * @param	hostname		their hostname or IP address
	 * @param	port			their port
	 * @param	calledAETitle		their AE Title
	 * @param	callingAETitle		our AE Title
	 * @param	secureTransport
	 * @param	debugLevel		zero for no debugging messages, higher values more verbose messages
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	public VerificationSOPClassSCU(String hostname,int port,String calledAETitle,String callingAETitle,boolean secureTransport,
			int debugLevel) throws DicomNetworkException, DicomException, IOException {
		this(hostname,port,calledAETitle,callingAETitle,secureTransport,null,null,debugLevel);
	}

	/**
	 * <p>Establish an association to the specified AE, perform verification (send a C-ECHO request), and release the association.</p>
	 *
	 * <p>Successful connection, association negotiation and C-ECHO command succsess status is indicated by the lack of an exception.</p>
	 *
	 * @param	hostname		their hostname or IP address
	 * @param	port			their port
	 * @param	calledAETitle		their AE Title
	 * @param	callingAETitle		our AE Title
	 * @param	secureTransport
	 * @param	username		may be null if no user identity
	 * @param	password		may be null if no user identity or no password required
	 * @param	debugLevel		zero for no debugging messages, higher values more verbose messages
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException	if the connection is refused, the association reqeust is reject, or the C-ECHO command reports other than a success status
	 */
	public VerificationSOPClassSCU(String hostname,int port,String calledAETitle,String callingAETitle,boolean secureTransport,
			String username,String password,
			int debugLevel) throws DicomNetworkException, DicomException, IOException {

		this.debugLevel=debugLevel;
		
		LinkedList presentationContexts = new LinkedList();
		{
			LinkedList tslist = new LinkedList();
			tslist.add(TransferSyntax.Default);
			tslist.add(TransferSyntax.ExplicitVRLittleEndian);
			presentationContexts.add(new PresentationContext((byte)0x01,SOPClass.Verification,tslist));
		}
		presentationContexts.add(new PresentationContext((byte)0x03,SOPClass.Verification,TransferSyntax.ImplicitVRLittleEndian));
		presentationContexts.add(new PresentationContext((byte)0x05,SOPClass.Verification,TransferSyntax.ExplicitVRLittleEndian));

		Association association = AssociationFactory.createNewAssociation(hostname,port,calledAETitle,callingAETitle,presentationContexts,null,secureTransport,username,password,debugLevel);
if (debugLevel > 1) System.err.println(association);
		// Decide which presentation context we are going to use ...
		byte usePresentationContextID = association.getSuitablePresentationContextID(SOPClass.Verification);
		//int usePresentationContextID = association.getSuitablePresentationContextID(SOPClass.Verification,TransferSyntax.Default);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": Using context ID "+usePresentationContextID);
		byte cEchoRequestCommandMessage[] = new CEchoRequestCommandMessage().getBytes();
		CEchoResponseHandler responseHandler = new CEchoResponseHandler(debugLevel);
		association.setReceivedDataHandler(responseHandler);
		association.send(usePresentationContextID,cEchoRequestCommandMessage,null);
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": VerificationSOPClass: waiting for one PDU");
		try {
			association.waitForCommandPDataPDUs();
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": VerificationSOPClass: got PDU, now releasing association");
			// State 6
			association.release();
		}
		catch (AReleaseException e) {
			// State 1
			// the other end released and didn't wait for us to do it
		}
		if (!responseHandler.wasSuccessful()) {
			throw new DicomNetworkException("C-ECHO reports failure status 0x"+Integer.toString(responseHandler.getStatus()&0xFFFF,16));
		}
	}
		
	/**
	 * <p>For testing, establish an association to the specified AE and perform verification (send a C-ECHO request).</p>
	 *
	 * @param	arg	array of four to seven strings - their hostname, their port, their AE Title, our AE Title, and optionally a string flag valued SECURE or NONSECURE, an optional username and an optional password
	 */
	public static void main(String arg[]) {
		boolean secure = arg.length >= 5 && arg[4].toUpperCase(java.util.Locale.US).equals("SECURE");
		String username = arg.length >= 6 ? arg[5] : null;
		String password = arg.length >= 7 ? arg[6] : null;
		VerificationSOPClassSCU v = null;
		try {
			v = new VerificationSOPClassSCU(arg[0],Integer.parseInt(arg[1]),arg[2],arg[3],secure,username,password,2/*debugLevel*/);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
		System.err.println("VerificationSOPClass: was "+(v == null ? "not " : "")+"successful");
	}
}




