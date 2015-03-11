/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.utils.ByteArray;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>This class provides utilities to probe the capabilities of potential DICOM Application Entities,
 * used for example to maintain a cache of potential C-MOVE targets.</p>
 *
 * <p>The constructors perform the probe and may be used to determine the capabilities of
 * an AE listening on a specified port, or to probe for plausible ports.</p>
 *
 * <p>The Verification SOP Class must be supported by the AE being tested for it to be
 * detected.</p>
 *
 * <p>The supported SOP Classes are tested by proposing associations with the default
 * transfer syntax.</p>
 *
 * <p>The main method may be useful in its own right as a command line utility.</p>
 *
 * @author	dclunie
 */
public class ProbeCapability implements Runnable {

	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/ProbeCapability.java,v 1.13 2014/09/09 20:34:09 dclunie Exp $";
	
	/***/
	private int debugLevel;
	/***/
	private boolean knowHostIsReachable;
	/***/
	private boolean knowCalledAET;
	/***/
	private boolean knowPort;
	/***/
	private String callingAETitle;
	/***/
	private boolean done;
	
	/***/
	private String hostname;
	/***/
	private int port;
	/***/
	private String calledAETitle;
	/***/
	private List supportedStorageSOPClasses;
	/***/
	private List supportedQuerySOPClasses;
	/***/
	private List supportedRetrieveWithMoveSOPClasses;
	/***/
	private List supportedRetrieveWithGetSOPClasses;
	/***/
	private String queryModel;
	
	/**
	 * <p>Get the Called AET.</p>
	 *
	 * @return	the Called AET
	 */
	public String getCalledAETitle() { return calledAETitle; }
	
	/**
	 * <p>Get the Presentation Address.</p>
	 *
	 * @return	the Presentation Address
	 */
	public PresentationAddress getPresentationAddress() { return hostname != null && hostname.length() > 0 && port != 0 ? new PresentationAddress(hostname,port) : null; }
	
	/**
	 * <p>Get the hostname or IP address.</p>
	 *
	 * @return	the hostname or IP address
	 */
	public String getHostname() { return hostname; }
	
	/**
	 * <p>Get the port.</p>
	 *
	 * @return	the port
	 */
	public int getPort() { return port; }

	/**
	 * <p>Get the supported Storage SOP Classes.</p>
	 *
	 * @return	a list of the supported Storage SOP Class UID strings
	 */
	public List getSupportedStorageSOPClasses() { return supportedStorageSOPClasses; }

	/**
	 * <p>Are any Storage SOP Classes supported ?</p>
	 *
	 * @return	true if any Storage SOP Classes are supported
	 */
	public boolean isStorageSupported() { return supportedStorageSOPClasses != null && supportedStorageSOPClasses.size() > 0; }

	/**
	 * <p>Get the supported Query SOP Classes.</p>
	 *
	 * @return	a list of the supported Query SOP Class UID strings
	 */
	public List getSupportedQuerySOPClasses() { return supportedQuerySOPClasses; }

	/**
	 * <p>Are any Query SOP Classes supported ?</p>
	 *
	 * @return	true if any Query SOP Classes are supported
	 */
	public boolean isQuerySupported() { return supportedQuerySOPClasses != null && supportedQuerySOPClasses.size() > 0; }

	/**
	 * <p>Get the supported Retrieve with Move SOP Classes.</p>
	 *
	 * @return	a list of the supported Retrieve with Move SOP Class UID strings
	 */
	public List getSupportedRetrieveWithMoveSOPClasses() { return supportedRetrieveWithMoveSOPClasses; }

	/**
	 * <p>Are any Retrieve with Move SOP Classes supported ?</p>
	 *
	 * @return	true if any Retrieve with Move SOP Classes are supported
	 */
	public boolean isRetrieveWithMoveSupported() { return supportedRetrieveWithMoveSOPClasses != null && supportedRetrieveWithMoveSOPClasses.size() > 0; }

	/**
	 * <p>Get the supported Retrieve with Get SOP Classes.</p>
	 *
	 * @return	a list of the supported Retrieve with Get SOP Class UID strings
	 */
	public List getSupportedRetrieveWithGetSOPClasses() { return supportedRetrieveWithGetSOPClasses; }

	/**
	 * <p>Are any Retrieve with Get SOP Classes supported ?</p>
	 *
	 * @return	true if any Retrieve with Get SOP Classes are supported
	 */
	public boolean isRetrieveWithGetSupported() { return supportedRetrieveWithGetSOPClasses != null && supportedRetrieveWithGetSOPClasses.size() > 0; }

	/**
	 * <p>Get the supported query model.</p>
	 *
	 * <p>Note that only a single model is returned, even if more than one is supported;
	 * in the later case, study root is preferred over patient root over patient/study only.</p>
	 *
	 * <p>Further, the query model of the query (find) is returned, in preference to checking the
	 * model of the retrieve (move or get), which is only done of no find SOP classes are supported.</p>
	 *
	 * @return	the (preferred) query model supported; one of {@link NetworkApplicationProperties#StudyRootQueryModel NetworkApplicationProperties.StudyRootQueryModel}
	 * or {@link NetworkApplicationProperties#PatientRootQueryModel NetworkApplicationProperties.PatientRootQueryModel}
	 * or {@link NetworkApplicationProperties#PatientStudyOnlyQueryModel NetworkApplicationProperties.PatientStudyOnlyQueryModel}.
	 */
	public String getQueryModel() { return queryModel; }


	/**
	 * <p>Has thread finished probing ?</p>
	 *
	 * @return	true if probe has finished
	 */
	public boolean isDone() { return done; }
	
	/**
	 * <p>Establish an association to the specified AE, and probe its capabilities with respect to those SOP Classes supported.</p>
	 *
	 * @param	arrayOfSOPClasses	the SOP Classes to test for
	 * @param	hostname		their hostname or IP address
	 * @param	port			their port
	 * @param	calledAETitle		their AE Title
	 * @param	callingAETitle		our AE Title
	 * @param	debugLevel		zero for no debugging messages, higher values more verbose messages
	 * @return				a list of Strings each being a SOP Class that is supported, or an empty list
	 * @throws	DicomNetworkException
	 * @throws	IOException
	 */
	public static List probeSupportedSOPClasses(String[] arrayOfSOPClasses,String hostname,int port,String calledAETitle,String callingAETitle,
			int debugLevel) throws DicomNetworkException, IOException {
		ArrayList supportedSOPClasses = new ArrayList();
		LinkedList presentationContexts = new LinkedList();
		LinkedList tslist = new LinkedList();
		tslist.add(TransferSyntax.Default);
		for (int i=0,contextID=1; i<arrayOfSOPClasses.length; ++i,contextID+=2) {
			presentationContexts.add(new PresentationContext((byte)contextID,arrayOfSOPClasses[i],tslist));
		}
		Association association = AssociationFactory.createNewAssociation(hostname,port,calledAETitle,callingAETitle,presentationContexts,null,false,debugLevel);
if (debugLevel > 2) System.err.println("Storage test association "+association);
		for (int i=0; i<arrayOfSOPClasses.length; ++i) {
			try {
				byte contextID = association.getSuitablePresentationContextID(arrayOfSOPClasses[i]);
				supportedSOPClasses.add(arrayOfSOPClasses[i]);
			}
			catch (DicomNetworkException e) {
				// no suitable presentation context ... SOP class not supported
			}
		}
		association.release();
		return supportedSOPClasses;
	}

	/**
	 * <p>Establish an association to the specified AE, and probe its capabilities with respect to which SOP Classes are supported.</p>
	 *
	 * <p>Currently storage and query/retrieval SOP class support is evaluated.</p>
	 *
	 * <p>A side effect of calling this method is to establish the query model capability.</p>
	 *
	 * @param	hostname		their hostname or IP address
	 * @param	port			their port
	 * @param	calledAETitle		their AE Title
	 * @param	callingAETitle		our AE Title
	 * @param	debugLevel		zero for no debugging messages, higher values more verbose messages
	 * @throws	DicomNetworkException
	 * @throws	IOException
	 */
	private void probeSupportedSOPClasses(String hostname,int port,String calledAETitle,String callingAETitle,int debugLevel)  {
		try {
			supportedStorageSOPClasses = probeSupportedSOPClasses(
				SOPClass.arrayOfStorageSOPClasses,
				hostname,port,calledAETitle,callingAETitle,debugLevel);
if (debugLevel > 1) System.err.println("Supported Storage SOP Classes "+supportedStorageSOPClasses);
		}
		catch (Exception e) {
if (debugLevel > 2) e.printStackTrace(System.err);
			// else quietly accept that it didn't work
		}
		try {
			supportedQuerySOPClasses = probeSupportedSOPClasses(
				SOPClass.arrayOfQuerySOPClasses,
				hostname,port,calledAETitle,callingAETitle,debugLevel);
if (debugLevel > 1) System.err.println("Supported Query SOP Classes "+supportedQuerySOPClasses);
			if (supportedQuerySOPClasses != null) {
				// sequence of check prefers study root over patient root
				if (supportedQuerySOPClasses.contains(SOPClass.StudyRootQueryRetrieveInformationModelFind)) {
					queryModel = NetworkApplicationProperties.StudyRootQueryModel;
				}
				else if (supportedQuerySOPClasses.contains(SOPClass.PatientRootQueryRetrieveInformationModelFind)) {
					queryModel = NetworkApplicationProperties.PatientRootQueryModel;
				}
				else if (supportedQuerySOPClasses.contains(SOPClass.PatientStudyOnlyQueryRetrieveInformationModelFind)) {
					queryModel = NetworkApplicationProperties.PatientStudyOnlyQueryModel;
				}
			}
		}
		catch (Exception e) {
if (debugLevel > 2) e.printStackTrace(System.err);
			// else quietly accept that it didn't work
		}
		try {
			supportedRetrieveWithMoveSOPClasses = probeSupportedSOPClasses(
				SOPClass.arrayOfRetrieveWithMoveSOPClasses,
				hostname,port,calledAETitle,callingAETitle,debugLevel);
if (debugLevel > 1) System.err.println("Supported Retrieve with Move SOP Classes "+supportedRetrieveWithMoveSOPClasses);
			if (supportedRetrieveWithMoveSOPClasses != null
			 && queryModel == null) {		// only check if not determined from query SOP Classes supported, else assume same model as find
				// sequence of check prefers study root over patient root
				if (supportedQuerySOPClasses.contains(SOPClass.StudyRootQueryRetrieveInformationModelMove)) {
					queryModel = NetworkApplicationProperties.StudyRootQueryModel;
				}
				else if (supportedQuerySOPClasses.contains(SOPClass.PatientRootQueryRetrieveInformationModelMove)) {
					queryModel = NetworkApplicationProperties.PatientRootQueryModel;
				}
				else if (supportedQuerySOPClasses.contains(SOPClass.PatientStudyOnlyQueryRetrieveInformationModelMove)) {
					queryModel = NetworkApplicationProperties.PatientStudyOnlyQueryModel;
				}
			}
		}
			catch (Exception e) {
if (debugLevel > 2) e.printStackTrace(System.err);
			// else quietly accept that it didn't work
		}
		try {
			supportedRetrieveWithGetSOPClasses = probeSupportedSOPClasses(
				SOPClass.arrayOfRetrieveWithGetSOPClasses,
				hostname,port,calledAETitle,callingAETitle,debugLevel);
if (debugLevel > 1) System.err.println("Supported Retrieve with Get SOP Classes "+supportedRetrieveWithGetSOPClasses);
			if (supportedRetrieveWithGetSOPClasses != null
			 && queryModel == null) {		// only check if not determined from query SOP Classes supported, else assume same model as find or move
				// sequence of check prefers study root over patient root
				if (supportedQuerySOPClasses.contains(SOPClass.StudyRootQueryRetrieveInformationModelGet)) {
					queryModel = NetworkApplicationProperties.StudyRootQueryModel;
				}
				else if (supportedQuerySOPClasses.contains(SOPClass.PatientRootQueryRetrieveInformationModelGet)) {
					queryModel = NetworkApplicationProperties.PatientRootQueryModel;
				}
				else if (supportedQuerySOPClasses.contains(SOPClass.PatientStudyOnlyQueryRetrieveInformationModelGet)) {
					queryModel = NetworkApplicationProperties.PatientStudyOnlyQueryModel;
				}
			}
		}
			catch (Exception e) {
if (debugLevel > 2) e.printStackTrace(System.err);
			// else quietly accept that it didn't work
		}
	}
	
	/**
	 * <p>Establish an association to the specified host, find a suitable port and AE Title, and probe its capabilities.</p>
	 *
	 * <p>The parameters are established but the work is deferred until run() is called.</p>
	 *
	 * @param	hostname		their hostname or IP address
	 * @param	callingAETitle	our AE Title
	 * @param	debugLevel		zero for no debugging messages, higher values more verbose messages
	 * @throws	DicomNetworkException
	 */
	public ProbeCapability(String hostname,String callingAETitle,int debugLevel) throws DicomNetworkException {
		done=false;
		knowHostIsReachable=true;
		knowCalledAET=false;
		knowPort=false;
		this.debugLevel=debugLevel;
		this.calledAETitle=null;
		this.callingAETitle=callingAETitle;
		this.hostname=hostname;
		this.port=0;
	}
	
	/**
	 * <p>Establish an association to the specified AE, find a suitable port, and probe its capabilities.</p>
	 *
	 * <p>The parameters are established but the work is deferred until run() is called.</p>
	 *
	 * @param	hostname		their hostname or IP address
	 * @param	calledAETitle	their AE Title
	 * @param	callingAETitle	our AE Title
	 * @param	debugLevel		zero for no debugging messages, higher values more verbose messages
	 * @throws	DicomNetworkException
	 */
	public ProbeCapability(String hostname,String calledAETitle,String callingAETitle,int debugLevel) throws DicomNetworkException {
		done=false;
		knowHostIsReachable=true;
		knowCalledAET=true;
		knowPort=false;
		this.debugLevel=debugLevel;
		this.calledAETitle=calledAETitle;
		this.callingAETitle=callingAETitle;
		this.hostname=hostname;
		this.port=0;
	}
	
	/**
	 * <p>Establish an association to the specified AE and using the specified port, and probe its capabilities.</p>
	 *
	 * <p>The parameters are established but the work is deferred until run() is called.</p>
	 *
	 * @param	hostname		their hostname or IP address
	 * @param	port			their port
	 * @param	calledAETitle	their AE Title
	 * @param	callingAETitle	our AE Title
	 * @param	debugLevel		zero for no debugging messages, higher values more verbose messages
	 */
	public ProbeCapability(String hostname,int port,String calledAETitle,String callingAETitle,int debugLevel) {
		done=false;
		knowHostIsReachable=true;
		knowCalledAET=true;
		knowPort=true;
		this.debugLevel=debugLevel;
		this.calledAETitle=calledAETitle;
		this.callingAETitle=callingAETitle;
		this.hostname=hostname;
		this.port=port;
	}
	
	protected class StringPermutationGenerator {
		protected final char[] firstCharacterPossibilities;
		protected final char[] subsequentCharacterPossibilities;
		protected final int maximumLength;
		protected final int[] indicesIntoCharacterPossibilities;
		protected final int maximumFirstCharacterIndex;
		protected final int maximumSubsequentCharacterIndex;
		protected boolean done;
		
		public StringPermutationGenerator(char[] firstCharacterPossibilities,char[] subsequentCharacterPossibilities,int maximumLength) {
			this.firstCharacterPossibilities = firstCharacterPossibilities;
			this.subsequentCharacterPossibilities = subsequentCharacterPossibilities;
			this.maximumLength = maximumLength;
			indicesIntoCharacterPossibilities = new int[maximumLength];
			for (int i=0; i<maximumLength; ++i) {
				indicesIntoCharacterPossibilities[i] = -1;
			}
			maximumFirstCharacterIndex = firstCharacterPossibilities.length - 1;
			maximumSubsequentCharacterIndex = subsequentCharacterPossibilities.length - 1;
			done = false;
		}

		public String next() {
			if (!done) {
				boolean found = false;
				int maximumCharacterIndex = maximumFirstCharacterIndex;
				int i = 0;
				while (i < maximumLength) {
//System.err.println("StringPermutationGenerator.next(): testing character position "+i);
					int index = indicesIntoCharacterPossibilities[i];
//System.err.println("StringPermutationGenerator.next(): testing character index "+index);
//System.err.println("StringPermutationGenerator.next(): maximumCharacterIndex "+maximumCharacterIndex);
					if (index < maximumCharacterIndex) {										// including -1 if not used at all yet
//System.err.println("StringPermutationGenerator.next(): use next character in current position");
						// use next character in current position
						++indicesIntoCharacterPossibilities[i];
						found=true;
						break;
					}
					else {
//System.err.println("StringPermutationGenerator.next(): moving to next position");
						indicesIntoCharacterPossibilities[i] = 0;								// recycle characters in current position
						maximumCharacterIndex = maximumSubsequentCharacterIndex;
					}
					++i;
				}
				if (found) {
					StringBuffer sb = new StringBuffer();
					char[] possibilities = firstCharacterPossibilities;
					i = 0;
					while (i < maximumLength) {
//System.err.println("StringPermutationGenerator.next(): string - adding character position "+i);
						int index = indicesIntoCharacterPossibilities[i];
//System.err.println("StringPermutationGenerator.next(): string - checking index "+index);
						if (index != -1) {
							char c = possibilities[index];
							sb.append(c);
							index = indicesIntoCharacterPossibilities[++i];
							possibilities = subsequentCharacterPossibilities;
						}
						else {
							break;
						}
					}
					return sb.toString();
				}
				else {
					done = true;
					return null;
				}
			}
			else {
				return null;
			}
		}
	}
	
	static final protected char[] upperCase = { 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z' };
	static final protected char[] upperCaseNumericUnderscore = { 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
											'_', '0','1','2','3','4','5','6','7','8','9','0' };
		
	protected class AETGenerator {
	
		protected boolean usedHostnameAsSupplied;
		protected boolean usedHostnameAsAllUpperCase;
		protected boolean usedAllUpperCaseNumericUnderscore;
		
		protected StringPermutationGenerator upperCaseNumericUnderscorePermutationGenerator;
		
		protected final String hostname;
		protected final int port;
		
		public AETGenerator(String hostname,int port) {
			this.port=port;
			if (hostname != null && hostname.length() > 0) {
				usedHostnameAsSupplied = false;
				usedHostnameAsAllUpperCase = false;
				// should check here for dotted quad IPV4 address and reverse lookup
				// should check here and remove any bad characters
				this.hostname=hostname;
			}
			else {
				usedHostnameAsSupplied = false;
				usedHostnameAsAllUpperCase = false;
				this.hostname=null;
			}
			usedAllUpperCaseNumericUnderscore=false;
			upperCaseNumericUnderscorePermutationGenerator=null;
		}
		
		public String next() {
			if (!usedHostnameAsSupplied) {
				usedHostnameAsSupplied=true;
				return hostname;
			}
			if (!usedHostnameAsAllUpperCase) {
				usedHostnameAsAllUpperCase=true;
				return hostname.toUpperCase(java.util.Locale.US);
			}
			if (!usedAllUpperCaseNumericUnderscore) {
				if (upperCaseNumericUnderscorePermutationGenerator == null) {
					upperCaseNumericUnderscorePermutationGenerator = new StringPermutationGenerator(upperCase,upperCaseNumericUnderscore,16);
				}
				String s = upperCaseNumericUnderscorePermutationGenerator.next();
				if (s != null) {
					return s;
				}
				else {
					usedAllUpperCaseNumericUnderscore=true;
				}
			}
			return null;
		}
	}
	
	/**
	 * <p>Actually do the work to probe for the requested information.</p>
	 */
	public void run() {
System.err.println("ProbeCapability.run()");
		try {
			if (!knowPort) {
				port=0;
				for (int i=0; port == 0 && i<NetworkDefaultValues.commonPortNumbers.length; ++i) {
					int p = NetworkDefaultValues.commonPortNumbers[i];
if (debugLevel > 2) System.err.println("Trying port "+p);
					try {
						if (canConnectToPort(hostname,p)) {
							if (!knowCalledAET) {
								calledAETitle=null;
								AETGenerator aetGenerator = new AETGenerator(hostname,p);
								String aet = aetGenerator.next();
								while (aet != null && calledAETitle == null) {
									try {
System.err.println("Trying Called AET "+aet);
										new VerificationSOPClassSCU(hostname,p,aet,callingAETitle,false/*secureTransprt*/,debugLevel);
										// worked otherwise would have thrown an exception
										port=p;
										calledAETitle = aet;
									}
									catch (Exception e) {
if (debugLevel > 2) e.printStackTrace(System.err);
System.err.println(e);
										// else quietly accept that it didn't work
									}
									aet = aetGenerator.next();
								}
							}
							else {
								new VerificationSOPClassSCU(hostname,p,calledAETitle,callingAETitle,false/*secureTransprt*/,debugLevel);
								// worked otherwise would have thrown an exception
								port=p;
							}
						}
					}
					catch (Exception e) {
if (debugLevel > 2) e.printStackTrace(System.err);
e.printStackTrace(System.err);
						// else quietly accept that it didn't work
					}
				}
				if (port != 0) {
if (debugLevel > 1) System.err.println("ProbeCapability: successful verification on port "+port);
				}
			}
		
			if (hostname != null && port != 0) {
				probeSupportedSOPClasses(hostname,port,calledAETitle,callingAETitle,debugLevel);
			}
		}
		catch (Exception e) {
			// No exceptions anticipated, but just in case, we don't want to run forever
			e.printStackTrace(System.err);
		}
		done=true;
	}
	
	/**
	 * <p>Actually do the work to probe for the requested information, and do not come back until done.</p>
	 */
	public void runUntilDone() throws InterruptedException {
		new Thread(this).start();
		while (!done) {
			Thread.sleep(10);
		}
	}
	
	/**
	 * <p>Return a String representing this object's value.</p>
	 *
	 * @return	a string representation of the value of this object
	 */
	public String toString() {
		StringBuffer strbuf = new StringBuffer();
		strbuf.append("AE [");
		strbuf.append(calledAETitle);
		strbuf.append("], host [");
		strbuf.append(hostname);
		strbuf.append("], port [");
		strbuf.append(port);
		strbuf.append("], query model [");
		strbuf.append(queryModel);
		strbuf.append("]\n");
		strbuf.append("\tSupported Storage SOP Classes = ");
		strbuf.append(supportedStorageSOPClasses);
		strbuf.append("\n");
		strbuf.append("\tSupported Query SOP Classes = ");
		strbuf.append(supportedQuerySOPClasses);
		strbuf.append("\n");
		strbuf.append("\tSupported Retrieve with Move SOP Classes = ");
		strbuf.append(supportedRetrieveWithMoveSOPClasses);
		strbuf.append("\n");
		strbuf.append("\tSupported Retrieve with Get SOP Classes = ");
		strbuf.append(supportedRetrieveWithGetSOPClasses);
		strbuf.append("\n");
		return strbuf.toString();
	}
	
	/**
	 * <p>Can a connection to the specified address and port be established?</p>
	 *
	 * <p>Will block until connected or an error occurs.</p>
	 *
	 * @param	hostAddress
	 * @param	port
	 * @return					true if can connect
	 */
	public static boolean canConnectToPort(String hostAddress,int port) {
		return canConnectToPort(hostAddress,port,0);
	}
	
	/**
	 * <p>Can a connection to the specified address and port be established?</p>
	 *
	 * @param	hostAddress
	 * @param	port
	 * @param	timeout			in milliseconds, or zero for no timeout (will block until connected or an error occurs)
	 * @return					true if can connect within specified timeout period
	 */
	public static boolean canConnectToPort(String hostAddress,int port,int timeout) {
		boolean success = false;
		//String hostAddress = address.getHostAddress();		// should just return IP address without blocking for reverse lookup
//System.err.println("Trying to connect to host "+hostAddress+" on port "+port);
		try {
			//Socket socket = new Socket(hostAddress,port);
			
			InetSocketAddress endpoint = new InetSocketAddress(hostAddress,port);
			Socket socket = new java.net.Socket();
			socket.connect(endpoint,timeout);
			// will throw exception if cannot connect within timeout period
			// worked otherwise would have thrown an exception
//System.err.println("Got a socket");
			socket.close();
//System.err.println("Back from socket close");
			success=true;
		}
		catch (Exception e) {
//System.err.println(e);
		}
		return success;
	}

	static boolean isReachable(InetAddress address) {
//System.err.println("Starting isReachable() check for "+address);
		String hostAddress = address.getHostAddress();		// should just return IP address without blocking for reverse lookup
		int port = 0;
		// could try ping, but (see also http://forum.java.sun.com/thread.jspa?threadID=275511&tstart=195)
		// - ICMP might be blocked anyway
		// - Java does not provide access to what is necessary to write ICMP code
		// - there is a ping or TCP ECHO based isReachable in 1.5 only, see http://java.sun.com/j2se/1.5.0/docs/api/java/net/InetAddress.html#isReachable(int)
		// - so just try connecting to plausible ports
		// - no point in trying non-DICOM ports (?)
		for (int i=0; port == 0 && i<NetworkDefaultValues.commonPortNumbers.length; ++i) {
			int p = NetworkDefaultValues.commonPortNumbers[i];
			//if (canConnectToPort(address,p)) {
			if (canConnectToPort(hostAddress,p)) {
				port=p;
			}
		}
		return port > 0;
	}
	
	static void probeHostOfUnknownDICOMIdentity(InetAddress address,String callingAETitle) {
		// can we even reach them (before worrying about guessing AETs and association acceptance)
	}
	
	static void probeRangeOfHostsInLocalSubnet(int localAddress,int netmask,String callingAETitle) {
		int baseAddress = localAddress & netmask;
		int startAddress = baseAddress+1;
		int endAddress = ((baseAddress | ~netmask) & 0xffffffff) - 1;
		//for (int i=startAddress; i<=endAddress; ++i) {
		{
			int i = localAddress;
			try {
				// Supposedly this next call does NOT block to perform a reverse lookup !
				InetAddress address = InetAddress.getByAddress(ByteArray.intToBigEndianArray(i,4));
//System.err.println("Testing address "+address);
//System.err.print("Testing address "+describeIPAddress(address));
				if (isReachable(address)) {
System.err.println("Can reach address "+address);
				}
				else {
System.err.println("Cannot reach address "+address);
				}
			}
			catch (java.net.UnknownHostException e) {
				// should not happen, since only occurs if byte array is wrong length
				e.printStackTrace(System.err);
			}
		}
	}
	
	static InetAddress[] getAllLocalHosts() {
		// See "http://www.geocities.com/andythwong/java_programming_info.htm"
		
		InetAddress[] allHostInfo = null;
		try {
			InetAddress oneAddress = InetAddress.getLocalHost();
			if (oneAddress != null) {
				String hostName = oneAddress.getHostName();
				if (hostName != null) {
					allHostInfo = InetAddress.getAllByName(hostName);
				}
			}
		}
		catch (java.net.UnknownHostException e) {
			e.printStackTrace(System.err);
		}
		return allHostInfo;
	}
	
	public static void probeRangeAllHostsOnLocalSubnet(String callingAETitle) {
		InetAddress[] allHostInfo = getAllLocalHosts();
		if (allHostInfo != null) {
			for (int i=0; i<allHostInfo.length; ++i) {
				InetAddress address = allHostInfo[i];
System.err.print("Got address "+describeIPAddress(address));
				if (address != null) {
					if (address instanceof Inet4Address) {
						Inet4Address ip4Address = (Inet4Address)address;
						int localAddress = ByteArray.bigEndianToUnsignedInt(ip4Address.getAddress());
						int netmask = 0xffffff00;			// java provides no means to get this :(
						probeRangeOfHostsInLocalSubnet(localAddress,netmask,callingAETitle);
					}
					// else we do not handle IPV6 probing yet :(
				}
			}
		}
	}
	
	public static String describeIPAddress(InetAddress address) {
		StringBuffer sb = new StringBuffer();
		sb.append("InetAddress "+address+"\n");
		if (address != null) {
			sb.append("\tis Inet4Address = "+(address instanceof Inet4Address)+"\n");
			sb.append("\tis Inet6Address = "+(address instanceof Inet6Address)+"\n");
			sb.append("\tgetCanonicalHostName() = "+address.getCanonicalHostName()+"\n");
			sb.append("\tgetHostAddress() = "+address.getHostAddress()+"\n");
			sb.append("\tgetHostName() = "+address.getHostName()+"\n");
			sb.append("\tisMulticastAddress() = "+address.isMulticastAddress()+"\n");
			sb.append("\tisAnyLocalAddress() = "+address.isAnyLocalAddress()+"\n");
			sb.append("\tisLoopbackAddress() = "+address.isLoopbackAddress()+"\n");
			sb.append("\tisLinkLocalAddress() = "+address.isLinkLocalAddress()+"\n");
			sb.append("\tisSiteLocalAddress() = "+address.isSiteLocalAddress()+"\n");
			sb.append("\tisMCGlobal() = "+address.isMCGlobal()+"\n");
			sb.append("\tisMCNodeLocal() = "+address.isMCNodeLocal()+"\n");
			sb.append("\tisMCLinkLocal() = "+address.isMCLinkLocal()+"\n");
			sb.append("\tisMCSiteLocal() = "+address.isMCSiteLocal()+"\n");
			sb.append("\tisMCOrgLocal() = "+address.isMCOrgLocal()+"\n");
		}
		return sb.toString();
	}

	/**
	 * <p>For testing, establish an association to the specified AE, find a suitable port if necessary, and probe its capabilities.</p>
	 *
	 * @param	arg	array of three, four or five values - their hostname, optionally their port, optionally their AE Title, our AE Title, and the debug level
	 */
	public static void main(String arg[]) {
		try {
			ProbeCapability capability = null;
			if (arg.length == 1) {
				probeRangeAllHostsOnLocalSubnet(arg[0]);	// our AE Title
			}
			else if (arg.length == 3) {
				capability = new ProbeCapability(arg[0],arg[1],Integer.parseInt(arg[2]));
			}
			else if (arg.length == 4) {
				capability = new ProbeCapability(arg[0],arg[1],arg[2],Integer.parseInt(arg[3]));
			}
			else if (arg.length == 5) {
				capability = new ProbeCapability(arg[0],Integer.parseInt(arg[1]),arg[2],arg[3],Integer.parseInt(arg[4]));
			}
			else {
				System.err.println("Usage: hostname [[port] calledAET] callingAET debugLevel");
			}
			if (capability != null) {
				try {
					capability.runUntilDone();
					//new Thread(capability).start();
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
				}
				//while (!capability.isDone()) {
					//sleep(1000);
				//}
				System.out.print(capability);
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(0);
		}
	}

}
