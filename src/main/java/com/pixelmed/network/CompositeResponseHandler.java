/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.utils.*;
import com.pixelmed.dicom.*;

import java.util.*;
import java.io.*;

/**
 * <p>This abstract class provides a mechanism to process each PDU of a composite response as it is received,
 * such as for evaluating the status of the response for success.</p>
 *
 * <p>Typically a private sub-class would be declared and instantiated with
 * overriding methods to evaluate the success or failure of a
 * storage or query or retrieve response.</p>
 *
 * @see com.pixelmed.network.ReceivedDataHandler
 * @see com.pixelmed.network.StorageSOPClassSCU
 * @see com.pixelmed.network.FindSOPClassSCU
 *
 * @author	dclunie
 */
abstract public class CompositeResponseHandler extends ReceivedDataHandler {
	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/CompositeResponseHandler.java,v 1.16 2014/09/09 20:34:09 dclunie Exp $";

	/***/
	protected byte[] commandReceived;
	/***/
	protected byte[] dataReceived;
	/***/
	protected boolean success;
	/***/
	protected boolean allowData;
	/***/
	protected int status;

	/**
	 * Construct a handler to process each PDU of a composite response as it is received,
	 * evaluating the status of the response for success.
	 *
	 * @param	debugLevel	0 for no debugging, &gt; 0 for increasingly verbose debugging
	 */
	public CompositeResponseHandler(int debugLevel) {
		super(debugLevel);
		commandReceived=null;
		dataReceived=null;
		success=false;
		done=false;
		allowData=false;
	}
	
	/**
	 * Extract an {@link AttributeList AttributeList} from the concatenated bytes
	 * that have been assembled from one or more PDUs and which make up an entire
	 * Command or Dataset.
	 *
	 * @param	bytes			the concatenated PDU bytes up to and including the last fragment
	 * @param	transferSyntaxUID	the Transfer Syntax to use to interpret the bytes
	 * @param	debugLevel		integer debug level
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	public static AttributeList getAttributeListFromCommandOrData(byte[] bytes,String transferSyntaxUID,int debugLevel) throws DicomNetworkException, DicomException, IOException {
if (debugLevel > 2) System.err.println(HexDump.dump(bytes));
		AttributeList list = new AttributeList();
		list.read(new DicomInputStream(new ByteArrayInputStream(bytes),transferSyntaxUID,false));
if (debugLevel > 2) System.err.print(list);
		return list;
	}

	/**
	 * Extract an {@link AttributeList AttributeList} from the concatenated bytes
	 * that have been assembled from one or more PDUs and which make up an entire
	 * Command or Dataset.
	 *
	 * @param	bytes			the concatenated PDU bytes up to and including the last fragment
	 * @param	transferSyntaxUID	the Transfer Syntax to use to interpret the bytes
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	private AttributeList getAttributeListFromCommandOrData(byte[] bytes,String transferSyntaxUID) throws DicomNetworkException, DicomException, IOException {
		return getAttributeListFromCommandOrData(bytes,transferSyntaxUID,debugLevel);
	}

	/**
	 * Extract an {@link AttributeList AttributeList} from the concatenated bytes
	 * that have been assembled from one or more PDUs and which make up an entire
	 * Command or Dataset.
	 *
	 * @param	bytes			the concatenated PDU bytes up to and including the last fragment
	 * @param	transferSyntaxUID	the Transfer Syntax to use to interpret the bytes
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	static public String dumpAttributeListFromCommandOrData(byte[] bytes,String transferSyntaxUID) throws DicomNetworkException, DicomException, IOException {
		String dump = null;
		try {
			AttributeList list = new AttributeList();
			list.read(new DicomInputStream(new ByteArrayInputStream(bytes),transferSyntaxUID,false));
			dump = list.toString();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			dump = null;
		}
		return dump;
	}

	/**
	 * The code handling the reception of data on an {@link Association Association} calls
	 * this method to indicate that a PDU has been received (a P-DATA-INDICATION).
	 *
	 * @param	pdata		the PDU that was received
	 * @param	association	the association on which the PDU was received
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	public void sendPDataIndication(PDataPDU pdata,Association association) throws DicomNetworkException, DicomException, IOException {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": CompositeResponseHandler:");
if (debugLevel > 1) super.dumpPDVList(pdata.getPDVList());
		// append to command ...
		LinkedList pdvList = pdata.getPDVList();
		ListIterator i = pdvList.listIterator();
		while (i.hasNext()) {
			PresentationDataValue pdv = (PresentationDataValue)i.next();
			if (pdv.isCommand()) {
				commandReceived=ByteArray.concatenate(commandReceived,pdv.getValue());	// handles null cases
				if (pdv.isLastFragment()) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": CompositeResponseHandler: last fragment of command seen");
					AttributeList list = getAttributeListFromCommandOrData(commandReceived,TransferSyntax.Default);
					commandReceived=null;
					evaluateStatusAndSetSuccess(list);
					//break;
				}
			}
			else {
				if (allowData) {
					dataReceived=ByteArray.concatenate(dataReceived,pdv.getValue());	// handles null cases
					if (pdv.isLastFragment()) {
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": CompositeResponseHandler: last fragment of data seen");
						AttributeList list = getAttributeListFromCommandOrData(dataReceived,
							association.getTransferSyntaxForPresentationContextID(pdv.getPresentationContextID()));
						makeUseOfDataSet(list);
						dataReceived=null;
						//break;
					}
				}
				else {
					throw new DicomNetworkException("Unexpected data fragment in response PDU");
				}
			}
		}
	}

	/**
	 * Extract the status information from a composite response
	 * and set the status flag accordingly.
	 *
	 * @param	list	the list of Attributes extracted from the bytes of the PDU(s)
	 */
	abstract protected void evaluateStatusAndSetSuccess(AttributeList list);
	
	/**
	 * Ignore any data set in the composite response (unless this method is overridden).
	 *
	 * @param	list	the list of Attributes extracted from the bytes of the PDU(s)
	 */
	protected void makeUseOfDataSet(AttributeList list) {}	// default is to ignore it
		
	/**
	 * Does the response include an indication of success ?
	 *
	 */
	public boolean wasSuccessful() { return success; }

	/**
	 * Get the response status
	 *
	 * Valid only after first calling {@link #evaluateStatusAndSetSuccess(AttributeList) evaluateStatusAndSetSuccess()}
	 *
	 */
	public int getStatus() { return status; }
}



