/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * <p>This class provides common support to applications requiring properties related to DICOM network services.</p>
 *
 * <p>Also contains a main method that can be used, for example, to convert information previously statically configured by properties on each
 * device, to assemble LDIF files to be loaded into an LDAP server for use via the DICOM Network Configuration
 * Management service.</p>
 *
 * <p>The following properties are supported:</p>
 *
 * <p><code>Network.DynamicConfigurationDebugLevel</code> -  for no debugging (silent), &gt; 0 for more verbose levels of debugging</p>
 * <p><code>Dicom.ListeningPort</code> - the port that an association acceptor will listen on for incoming connections</p>
 * <p><code>Dicom.CalledAETitle</code> - what the AE expects to be called when accepting an association</p>
 * <p><code>Dicom.CallingAETitle</code> - what the AE will call itself when initiating an association</p>
 * <p><code>Dicom.PrimaryDeviceType</code> - what our own primary device type is</p>
 * <p><code>Dicom.QueryDebugLevel</code> -  for no debugging (silent), &gt; 0 for more verbose levels of debugging</p>
 * <p><code>Dicom.StorageSCUDebugLevel</code> -  for no debugging (silent), &gt; 0 for more verbose levels of debugging</p>
 * <p><code>Dicom.StorageSCPDebugLevel</code> -  for no debugging (silent), &gt; 0 for more verbose levels of debugging</p>
 * <p><code>Dicom.StorageSCUCompressionLevel</code> - determines what types of compressed Transfer Syntaxes are proposed by a Storage SCU; 0 = uncompressed transfer syntaxes only; 1 = propose deflate as well; 2 = propose deflate and bzip2 (if bzip2 codec is available)</p>
 * <p><code>Dicom.RemoteAEs</code> - a space or comma separated list of the local names all the available remote AEs; each local name may be anything unique (in this file) without a space or comma; the local name does not need to be the same as the remote AE's called AE title</p>
 * <p><code>Dicom.XXXX.CalledAETitle</code> - for the remote AE with local name XXXX, what that AE expects to be called when accepting an association</p>
 * <p><code>Dicom.XXXX.HostNameOrIPAddress</code> - for the remote AE with local name XXXX, what hostname or IP addess that AE will listen on for incoming connections</p>
 * <p><code>Dicom.XXXX.Port</code> - for the remote AE with local name XXXX, what port that AE will listen on for incoming connections</p>
 * <p><code>Dicom.XXXX.QueryModel</code> - for the remote AE with local name XXXX, what query model is supported; values are STUDYROOT or PATIENTROOT; leave absent if query/retrieve not supported by the remote AE</p>
 * <p><code>Dicom.XXXX.PrimaryDeviceType</code> - for the remote AE with local name XXXX, what the primary device type is (see DICOM PS 3.15 and PS 3.16)</p>
 *
 * @author	dclunie
 */
public class NetworkApplicationProperties {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/NetworkApplicationProperties.java,v 1.18 2014/09/09 20:34:09 dclunie Exp $";

	public static final String propertyName_DicomListeningPort = "Dicom.ListeningPort";
	public static final String propertyName_DicomCalledAETitle = "Dicom.CalledAETitle";
	public static final String propertyName_DicomCallingAETitle = "Dicom.CallingAETitle";
	public static final String propertyName_PrimaryDeviceType = "Dicom.PrimaryDeviceType";		// "WSD","ARCHIVE"
	
	public static final String StudyRootQueryModel = "STUDYROOT";
	public static final String PatientRootQueryModel = "PATIENTROOT";
	public static final String PatientStudyOnlyQueryModel = "PATIENTSTUDYONLY";
	
	/**
	 * <p>Is the model Study Root ?</p>
	 *
	 * @param	model	the string value describing the model, as used in the query model remote AE property
	 * @return		true if Study Root
	 */
	public static final boolean isStudyRootQueryModel(String model) { return model != null && model.equals(StudyRootQueryModel); }
	
	/**
	 * <p>Is the model Patient Root ?</p>
	 *
	 * @param	model	the string value describing the model, as used in the query model remote AE property
	 * @return		true if Patient Root
	 */
	public static final boolean isPatientRootQueryModel(String model) { return model != null && model.equals(PatientRootQueryModel); }
	
	/**
	 * <p>Is the model Patient/Study Only ?</p>
	 *
	 * @param	model	the string value describing the model, as used in the query model remote AE property
	 * @return		true if Patient/Study Only
	 */
	public static final boolean isPatientStudyOnlyQueryModel(String model) { return model != null && model.equals(PatientStudyOnlyQueryModel); }
	
	public static final String propertyName_QueryDebugLevel = "Dicom.QueryDebugLevel";
	public static final String propertyName_StorageSCUDebugLevel = "Dicom.StorageSCUDebugLevel";
	public static final String propertyName_StorageSCUCompressionLevel = "Dicom.StorageSCUCompressionLevel";
	public static final String propertyName_StorageSCPDebugLevel = "Dicom.StorageSCPDebugLevel";
	public static final String propertyName_NetworkDynamicConfigurationDebugLevel = "Network.DynamicConfigurationDebugLevel";
	
	private int port;
	private String calledAETitle;
	private String callingAETitle;
	private String primaryDeviceType;
	private int storageSCUCompressionLevel;
	private int storageSCUDebugLevel;
	private int storageSCPDebugLevel;
	private int queryDebugLevel;
	private int networkDynamicConfigurationDebugLevel;
	private NetworkApplicationInformation networkApplicationInformation;

	/**
	 * <p>Create default properties.</p>
	 */
	public NetworkApplicationProperties() throws DicomNetworkException {
//System.err.println("NetworkApplicationProperties():");
		port = NetworkDefaultValues.StandardDicomReservedPortNumber;
		calledAETitle = NetworkDefaultValues.getDefaultApplicationEntityTitle(port);
		callingAETitle = calledAETitle;
		primaryDeviceType = NetworkDefaultValues.getDefaultPrimaryDeviceType();
		storageSCUCompressionLevel = 0;
		storageSCUDebugLevel = 0;
		storageSCPDebugLevel = 0;
		queryDebugLevel = 0;
		networkDynamicConfigurationDebugLevel = 0;
		networkApplicationInformation = new NetworkApplicationInformation();
	}
	
	/**
	 * <p>Extract the DICOM network properties from the supplied properties.</p>
	 *
	 * @param	properties
	 */
	public NetworkApplicationProperties(Properties properties) throws DicomNetworkException, IOException {
		this(properties,false/*addPublicStorageSCPsIfNoRemoteAEsConfigured*/);
	}
	
	/**
	 * <p>Extract the DICOM network properties from the supplied properties.</p>
	 *
	 * @param	properties
	 * @param	addPublicStorageSCPsIfNoRemoteAEsConfigured
	 */
	public NetworkApplicationProperties(Properties properties,boolean addPublicStorageSCPsIfNoRemoteAEsConfigured) throws DicomNetworkException, IOException {
		String portString=properties.getProperty(propertyName_DicomListeningPort);
		if (portString == null || portString.length() == 0) {
			port=NetworkDefaultValues.StandardDicomReservedPortNumber;
		}
		else {
			port=Integer.parseInt(portString);
		}
		calledAETitle=properties.getProperty(propertyName_DicomCalledAETitle);
		if (calledAETitle == null || calledAETitle.length() == 0) {
			calledAETitle=NetworkDefaultValues.getDefaultApplicationEntityTitle(port);
		}
		callingAETitle=properties.getProperty(propertyName_DicomCallingAETitle);
		if (callingAETitle == null || callingAETitle.length() == 0) {
			callingAETitle=calledAETitle;
		}
		
		primaryDeviceType = properties.getProperty(propertyName_PrimaryDeviceType);
		
		storageSCUCompressionLevel = Integer.valueOf(properties.getProperty(propertyName_StorageSCUCompressionLevel,"0")).intValue();
		
		storageSCUDebugLevel = Integer.valueOf(properties.getProperty(propertyName_StorageSCUDebugLevel,"0")).intValue();
		
		storageSCPDebugLevel = Integer.valueOf(properties.getProperty(propertyName_StorageSCPDebugLevel,"0")).intValue();
		
		queryDebugLevel = Integer.valueOf(properties.getProperty(propertyName_QueryDebugLevel,"0")).intValue();

		networkDynamicConfigurationDebugLevel = Integer.valueOf(properties.getProperty(propertyName_NetworkDynamicConfigurationDebugLevel,"0")).intValue();

		networkApplicationInformation = new NetworkApplicationInformation(properties);
		
		if (addPublicStorageSCPsIfNoRemoteAEsConfigured) {
			Set<String> aets = networkApplicationInformation.getListOfApplicationEntityTitlesOfApplicationEntities();
			if (aets == null || aets.size() == 0 || (aets.size() == 1 && aets.contains(calledAETitle))) {
			}
		}
	}
	
	/**
	 * <p>Retrieve the DICOM network properties.</p>
	 *
	 * param	properties	the existing properties to add to (replacing corresponding properties already there), or null if none
	 *
	 * @return	the updated properties or a new set of properties if none supplied
	 */
	public Properties addToProperties(Properties properties) {
		if (properties == null) {
			properties = new Properties();
		}
		
		properties.setProperty(propertyName_DicomListeningPort,Integer.toString(port));
		properties.setProperty(propertyName_DicomCalledAETitle,calledAETitle);
		properties.setProperty(propertyName_DicomCallingAETitle,callingAETitle);
		properties.setProperty(propertyName_StorageSCUCompressionLevel,Integer.toString(storageSCUCompressionLevel));
		properties.setProperty(propertyName_StorageSCUDebugLevel,Integer.toString(storageSCUDebugLevel));
		properties.setProperty(propertyName_StorageSCPDebugLevel,Integer.toString(storageSCPDebugLevel));
		properties.setProperty(propertyName_QueryDebugLevel,Integer.toString(queryDebugLevel));
		properties.setProperty(propertyName_NetworkDynamicConfigurationDebugLevel,Integer.toString(networkDynamicConfigurationDebugLevel));
		
		networkApplicationInformation.getProperties(properties);	// remove any existing entries in properties, and add properties for all in  networkApplicationInformation

		return properties;
	}

	/**
	 * <p>Return the listening port.</p>
	 *
	 * @return	the listening port
	 */
	public int getListeningPort() { return port; }
	
	/**
	 * <p>Set the listening port.</p>
	 *
	 * param		port	the listening port
	 */
	public void setListeningPort(int port) { this.port = port; }
	
	/**
	 * <p>Return the called AET.</p>
	 *
	 * @return	the called AET
	 */
	public String getCalledAETitle() { return calledAETitle; }
	
	/**
	 * <p>Set the called AET.</p>
	 *
	 * param	calledAETitle	the called AET
	 */
	public void setCalledAETitle(String calledAETitle) { this.calledAETitle = calledAETitle; }
	
	/**
	 * <p>Return the calling AET.</p>
	 *
	 * @return	the calling AET
	 */
	public String getCallingAETitle() { return callingAETitle; }
	
	/**
	 * <p>Set the calling AET.</p>
	 *
	 * param	callingAETitle	the calling AET
	 */
	public void setCallingAETitle(String callingAETitle) { this.callingAETitle = callingAETitle; }
	
	/**
	 * <p>Return the primary device type.</p>
	 *
	 * @return	the primary device type
	 */
	public String getPrimaryDeviceType() { return primaryDeviceType; }
	

	/**
	 * <p>Return the storage SCP debug level.</p>
	 *
	 * @return	the storage SCP debug level
	 */
	public int getStorageSCPDebugLevel() { return storageSCPDebugLevel; }
	
	/**
	 * <p>Return the query debug level.</p>
	 *
	 * @return	the query debug level
	 */
	public int getQueryDebugLevel() { return queryDebugLevel; }
	
	/**
	 * <p>Return the network dynamic configuration debug level.</p>
	 *
	 * @return	the network dynamic configuration debug level
	 */
	public int getNetworkDynamicConfigurationDebugLevel() { return networkDynamicConfigurationDebugLevel; }
	
	/**
	 * <p>Return the network application information.</p>
	 *
	 * @return	the network application information
	 */
	public NetworkApplicationInformation getNetworkApplicationInformation() { return networkApplicationInformation; }
	
	protected class OurNetworkConfigurationSource extends NetworkConfigurationSource {
		private NetworkApplicationInformation ourNetworkApplicationInformation;
		OurNetworkConfigurationSource(NetworkApplicationInformation networkApplicationInformation) {
			super(networkDynamicConfigurationDebugLevel);
			ourNetworkApplicationInformation = networkApplicationInformation;
		}
		public synchronized NetworkApplicationInformation getNetworkApplicationInformation() {
			return ourNetworkApplicationInformation;
		}
		public void activateDiscovery(int refreshInterval) {}
		public void deActivateDiscovery() {}
	}
	
	protected NetworkConfigurationSource networkConfigurationSource = null;

	/**
	 * <p>Return a network configuration source that will supply the network application information.</p>
	 *
	 * @return	the network configuration source
	 */
	public NetworkConfigurationSource getNetworkConfigurationSource() {
		if (networkConfigurationSource == null) {
			networkConfigurationSource = new OurNetworkConfigurationSource(networkApplicationInformation);
		}
		return networkConfigurationSource;
	}
	
	/**
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Our port: "+port+"\n");
		str.append("Our calledAETitle: "+calledAETitle+"\n");
		str.append("Our callingAETitle: "+callingAETitle+"\n");
		str.append("Our primaryDeviceType: "+primaryDeviceType+"\n");
		str.append("storageSCUCompressionLevel: "+storageSCUCompressionLevel+"\n");
		str.append("storageSCUDebugLevel: "+storageSCUDebugLevel+"\n");
		str.append("storageSCPDebugLevel: "+storageSCPDebugLevel+"\n");
		str.append("queryDebugLevel: "+queryDebugLevel+"\n");
		str.append("networkDynamicConfigurationDebugLevel: "+networkDynamicConfigurationDebugLevel+"\n");
		str.append("Remote applications:\n"+networkApplicationInformation+"\n");
		
		return str.toString();
	}


}

