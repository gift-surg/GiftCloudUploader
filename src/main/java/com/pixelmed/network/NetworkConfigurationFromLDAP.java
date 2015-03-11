/* Copyright (c) 2001-2008, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * <p>This class provides utilities to automatically configure DICOM network parameters.</p>
 *
 * @author	dclunie
 */
public class NetworkConfigurationFromLDAP extends NetworkConfigurationSource {

	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/NetworkConfigurationFromLDAP.java,v 1.4 2008/09/24 18:54:53 dclunie Exp $";
	
	private static final String defaultInitialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
	
	//private static final String defaultProviderURL = "ldap://localhost:389";
	private static final String defaultProviderURL = "ldap://";	// uses search algorithm DNS then localhost to find LDAP server
									// See "http://java.sun.com/j2se/1.5.0/docs/guide/jndi/jndi-ldap.html#URLs"
	
	private static final String defaultdevicesDN = "cn=Devices,cn=DICOM Configuration,o=pixelmed,c=us";
	private static final String devicesRDN = "cn=Devices";
	
	private class ApplicationEntityWithDicomNetworkConnectionName extends ApplicationEntity {
		private String dicomNetworkConnectionName;

		ApplicationEntityWithDicomNetworkConnectionName(String dicomAETitle,String dicomNetworkConnectionName) {
			super(dicomAETitle);
			this.dicomNetworkConnectionName = dicomNetworkConnectionName;
		}
		public final String getDicomNetworkConnectionName() { return dicomNetworkConnectionName; }
	}


	private String getDicomDevicesRootDistinguishedName(DirContext context) {
		String dicomConfigurationDN = null;
		try {
if (debugLevel > 2) System.err.println("getDicomDevicesRootDistinguishedName: name of context = "+context.getNameInNamespace());
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration enumeration = context.search("","(cn=DICOM Configuration)",searchControls);
			while (enumeration.hasMore()) {
				SearchResult result = (SearchResult)(enumeration.next());
				dicomConfigurationDN = result.getName();
if (debugLevel > 1) System.err.println("getDicomDevicesRootDistinguishedName: found "+dicomConfigurationDN);
			}
		}
		catch (NamingException e) {
			e.printStackTrace(System.err);
		}
		String devicesDN = null;
		if (dicomConfigurationDN == null) {
			devicesDN = defaultdevicesDN;
if (debugLevel > 1) System.err.println("getDicomDevicesRootDistinguishedName: not found  - using default name = "+devicesDN);
		}
		else {
			devicesDN = devicesRDN + "," + dicomConfigurationDN;
		}
		return devicesDN;
	}

	protected class GetNetworkApplicationInformation extends TimerTask {
	
		private int interval;
		
		GetNetworkApplicationInformation(int interval) {
			this.interval = interval;
		}
		
		public void run() {
			getNetworkApplicationInformation().removeAll();
			getNetworkConfiguration();
		}
		
		void start() {
			timer.schedule(this,0/*no delay to start*/,interval);
		}
	}
	
	protected GetNetworkApplicationInformation getter;
	
	public void activateDiscovery(int refreshInterval) {
		if (refreshInterval == 0) {
			getNetworkConfiguration();	// run once only
		}
		else {
			if (getter == null) {
				getter = new GetNetworkApplicationInformation(refreshInterval);
			}
			getter.start();
		}
	}
	
	public void deActivateDiscovery() {
		if (getter != null) {
			getter.cancel();	// needed, since otherwise application will not exit when main thread finished
		}
	}
		
	protected void getNetworkConfiguration() {
		try {
			//DirContext context = new InitialDirContext();
			//context.addToEnvironment(Context.INITIAL_CONTEXT_FACTORY,defaultInitialContextFactory);
			//context.addToEnvironment(Context.PROVIDER_URL,defaultProviderURL);
			
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY,defaultInitialContextFactory);
			env.put(Context.PROVIDER_URL,defaultProviderURL);
			DirContext context = new InitialDirContext(env);
			
			String devicesDN = getDicomDevicesRootDistinguishedName(context);
			
			NamingEnumeration listOfDevices = context.search(devicesDN,null/*all attributes*/);
			while (listOfDevices.hasMore()) {
				Attributes adev = ((SearchResult)(listOfDevices.next())).getAttributes();
				BasicAttribute aDicomDeviceName = (BasicAttribute)adev.get("dicomDeviceName");
				String vDicomDeviceName = aDicomDeviceName == null ? "" : aDicomDeviceName.get(0).toString();
if (debugLevel > 1) System.err.println("dicomDeviceName: "+vDicomDeviceName);
				if (aDicomDeviceName != null) {
					Map mapOfDicomNetworkConnectionsForThisDevice = new HashMap();	// key is String, value is PresentationAddress
					List listOfApplicationEntitiesForThisDevice = new ArrayList();	// value is ApplicationEntity
					NamingEnumeration listOfDeviceChildren = context.search("dicomDeviceName="+vDicomDeviceName+","+devicesDN,null/*all attributes*/);
					while (listOfDeviceChildren.hasMore()) {
						Attributes adevchildren = ((SearchResult)(listOfDeviceChildren.next())).getAttributes();
						BasicAttribute aObjectClass = (BasicAttribute)adevchildren.get("objectClass");
						String vObjectClass = aObjectClass == null ? "" : aObjectClass.get(0).toString();
if (debugLevel > 1) System.err.println("\tvObjectClass: "+vObjectClass);
						if (vObjectClass != null) {
							if (vObjectClass.equals("dicomNetworkAE")) {
								BasicAttribute aDicomAETitle = (BasicAttribute)adevchildren.get("dicomAETitle");
								String vDicomAETitle = aDicomAETitle == null ? "" : aDicomAETitle.get(0).toString();
if (debugLevel > 1) System.err.println("\t\tdicomAETitle: "+vDicomAETitle);
								BasicAttribute aDicomNetworkConnectionReference = (BasicAttribute)adevchildren.get("dicomNetworkConnectionReference");
								String vDicomNetworkConnectionReference = aDicomNetworkConnectionReference == null
									? "" : aDicomNetworkConnectionReference.get(0).toString();
if (debugLevel > 1) System.err.println("\t\tdicomNetworkConnectionReference: "+vDicomNetworkConnectionReference);
								String dicomNetworkConnectionCommonNameValue = null;
								if (vDicomNetworkConnectionReference != null) {
									int firstDelimiter = vDicomNetworkConnectionReference.indexOf(",");
									if (firstDelimiter >= 0) {
										vDicomNetworkConnectionReference=vDicomNetworkConnectionReference.substring(0,firstDelimiter);
if (debugLevel > 1) System.err.println("\t\tdicomNetworkConnectionReference first part: "+vDicomNetworkConnectionReference);
									}
									dicomNetworkConnectionCommonNameValue = vDicomNetworkConnectionReference.replaceFirst("[cC][nN]=","");
if (debugLevel > 1) System.err.println("\t\tdicomNetworkConnectionCommonNameValue: "+dicomNetworkConnectionCommonNameValue);
								}
								if (vDicomAETitle != null && vDicomAETitle.length() > 0
								 && dicomNetworkConnectionCommonNameValue != null && dicomNetworkConnectionCommonNameValue.length() > 0) {
									listOfApplicationEntitiesForThisDevice.add(
										new ApplicationEntityWithDicomNetworkConnectionName(vDicomAETitle,dicomNetworkConnectionCommonNameValue));
								}
							}
							else if (vObjectClass.equals("dicomNetworkConnection")) {
								BasicAttribute aCN = (BasicAttribute)adevchildren.get("cn");
								String vCN = aCN == null ? "" : aCN.get(0).toString();
if (debugLevel > 1) System.err.println("\t\tcn: "+vCN);
								BasicAttribute aDicomHostname = (BasicAttribute)adevchildren.get("dicomHostname");
								String vDicomHostname = aDicomHostname == null ? "" : aDicomHostname.get(0).toString();
if (debugLevel > 1) System.err.println("\t\tdicomHostname: "+vDicomHostname);
								BasicAttribute aDicomPort = (BasicAttribute)adevchildren.get("dicomPort");
								String vDicomPort = aDicomPort == null ? "" : aDicomPort.get(0).toString();
if (debugLevel > 1) System.err.println("\t\tdicomPort: "+vDicomPort);
								if (vCN != null && vCN.length() > 0
								 && vDicomHostname != null && vDicomHostname.length() > 0
								 && vDicomPort != null && vDicomPort.length() > 0) {
									mapOfDicomNetworkConnectionsForThisDevice.put(vCN,new PresentationAddress(vDicomHostname,Integer.parseInt(vDicomPort)));
								}
							}
						}
					}
					Iterator iaes = listOfApplicationEntitiesForThisDevice.iterator();
					while (iaes.hasNext()) {
						ApplicationEntityWithDicomNetworkConnectionName ae = (ApplicationEntityWithDicomNetworkConnectionName)(iaes.next());
						String dicomNetworkConnectionName = ae.getDicomNetworkConnectionName();
						ae.setPresentationAddress((PresentationAddress)(mapOfDicomNetworkConnectionsForThisDevice.get(dicomNetworkConnectionName)));
						getNetworkApplicationInformation().add(dicomNetworkConnectionName,ae);	// not vDicomDeviceName as localName, since may be multiple
					}
					iaes = listOfApplicationEntitiesForThisDevice.iterator();
					while (iaes.hasNext()) {
						ApplicationEntity ae = (ApplicationEntity)(iaes.next());
if (debugLevel > 1) System.err.println("\tApplicationEntity: "+ae);
					}
				}
			}
		}
		catch (javax.naming.CommunicationException e) {
if (debugLevel > 1) System.err.println("NetworkConfigurationFromLDAP.getNetworkConfiguration(): LDAP service not available (Could not contact server)");
if (debugLevel > 2) e.printStackTrace(System.err);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public NetworkConfigurationFromLDAP(int debugLevel) {
		super(debugLevel);
	}
	
	/**
	 * <p>Test method that periodically queries an LDAP server and dumps its contents periodically.</p>
	 *
	 * @param	arg	none
	 */
	public static void main(String arg[]) {
		NetworkConfigurationFromLDAP networkConfiguration = new NetworkConfigurationFromLDAP(99);
		//networkConfiguration.activateDiscovery(0);
		networkConfiguration.activateDiscovery(5000);
		//System.err.println(networkConfiguration.getNetworkApplicationInformation().toString());
		networkConfiguration.activateDumper(1000);
		Thread mainThread = Thread.currentThread();
		try {
			while (true) {
				mainThread.sleep(10000);
			}
		}
		catch (InterruptedException e) {
			networkConfiguration.close();
		}
	}
}

