/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.ftp;

import java.io.FileInputStream;

import java.util.Properties; 

/**
 * <p>This class provides common support to applications requiring properties related to FTP network services.</p>
 *
 * @author	dclunie
 */
public class FTPApplicationProperties {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/ftp/FTPApplicationProperties.java,v 1.1 2010/11/15 20:30:23 dclunie Exp $";
	
	public static final String propertyName_ClientDebugLevel = "Ftp.ClientDebugLevel";
	
	protected int clientDebugLevel;
	protected FTPRemoteHostInformation ftpRemoteHostInformation;

	/**
	 * <p>Create default properties.</p>
	 */
	public FTPApplicationProperties() throws FTPException {
//System.err.println("FTPApplicationProperties():");
		clientDebugLevel = 0;
		ftpRemoteHostInformation = new FTPRemoteHostInformation();
	}
	
	/**
	 * <p>Extract the ftp properties from the supplied properties.</p>
	 *
	 * @param	properties
	 */
	public FTPApplicationProperties(Properties properties) throws FTPException {
//System.err.println("FTPApplicationProperties(Properties): properties ="+properties);
		clientDebugLevel = Integer.valueOf(properties.getProperty(propertyName_ClientDebugLevel,"0")).intValue();

		ftpRemoteHostInformation = new FTPRemoteHostInformation(properties);
	}
	
	/**
	 * <p>Retrieve the ftp properties.</p>
	 *
	 * param	properties	the existing properties to add to (replacing corresponding properties already there), or null if none
	 *
	 * @return	the updated properties or a new set of properties if none supplied
	 */
	public Properties getProperties(Properties properties) {
//System.err.println("FTPApplicationProperties.getProperties(): at start, properties = \n"+properties);
		if (properties == null) {
			properties = new Properties();
		}
		
		properties.setProperty(propertyName_ClientDebugLevel,Integer.toString(clientDebugLevel));
		
		ftpRemoteHostInformation.getProperties(properties);	// remove any existing entries in properties, and add properties for all in ftpRemoteHostInformation

//System.err.println("FTPApplicationProperties.getProperties(): at end, properties = \n"+properties);
		return properties;
	}

	/**
	 * <p>Return the client debug level.</p>
	 *
	 * @return	the client debug level
	 */
	public int getClientDebugLevel() { return clientDebugLevel; }
	
	/**
	 * <p>Return the network application information.</p>
	 *
	 * @return	the network application information
	 */
	public FTPRemoteHostInformation getFTPRemoteHostInformation() { return ftpRemoteHostInformation; }
	
	/**
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("clientDebugLevel: "+clientDebugLevel+"\n");
		str.append("Remote applications:\n"+ftpRemoteHostInformation+"\n");
		return str.toString();
	}

	/**
	 * <p>Test the parsing of network properties from the specified file, by reading them and printing them.</p>
	 *
	 * @param	arg	one argument, a single file name that is the properties file
	 */
	public static void main(String arg[]) {
		String propertiesFileName = arg[0];
		try {
			FileInputStream in = new FileInputStream(propertiesFileName);
			Properties properties = new Properties(/*defaultProperties*/);
			properties.load(in);
			in.close();
			System.err.println("properties="+properties);
		}
		catch (Exception e) {
			System.err.println(e);
		}


	}
}

