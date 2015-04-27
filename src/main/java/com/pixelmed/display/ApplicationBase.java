/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import com.pixelmed.utils.FileUtilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

//import com.pixelmed.display.event.*; 

/**
 * <p>This class provides the infrastructure for creating applications (which extend
 * this class) and provides them with utilities for creating a main window with a
 * title and default close and dispose behavior, as well as access to properties,
 * and a window snapshot function.</p>
 * 
 * @author	dclunie
 */
public class ApplicationBase {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/ApplicationFrame.java,v 1.31 2014/09/09 20:34:09 dclunie Exp $";

	/**
	 * <p>Given a file name, such as the properties file name, make a path to it in the user's home directory.</p>
	 *
	 * @param	fileName	 the file name to make a path to
	 */
	protected static String makePathToFileInUsersHomeDirectory(String fileName) {
		return FileUtilities.makePathToFileInUsersHomeDirectory(fileName);
	}

	private Properties applicationProperties;
	private String applicationPropertyFileName;

	/**
	 * <p>Store the properties from the current properties file.</p>
	 */
	protected void loadProperties() {
		applicationProperties = new Properties(/*defaultProperties*/);
		if (applicationPropertyFileName != null) {
			String whereFrom = makePathToFileInUsersHomeDirectory(applicationPropertyFileName);
			try {
				// load properties from last invocation
				FileInputStream in = new FileInputStream(whereFrom);
				applicationProperties.load(in);
				in.close();
			}
			catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	/**
	 * <p>Store the current properties in the current properties file.</p>
	 *
	 * @param	comment		the description to store as the header of the properties file
	 * @throws	java.io.IOException
	 */
	protected void storeProperties(String comment) throws IOException {
		if (applicationPropertyFileName == null) {
			throw new IOException("asked to store properties but no applicationPropertyFileName was ever set");
		}
		else {
			String whereTo = makePathToFileInUsersHomeDirectory(applicationPropertyFileName);
			FileOutputStream out = new FileOutputStream(whereTo);
			applicationProperties.store(out,comment);
			out.close();
		}
	}

	/**
	 * <p>Get the properties for the application that have already been loaded (see {@link #loadProperties() loadProperties()}).</p>
	 *
	 * @return	the properties
	 */
	protected Properties getProperties() { return applicationProperties; }

	/**
	 * <p>Get the name of the property file set for the application.</p>
	 *
	 * @return	the property file name
	 */
	protected String getApplicationPropertyFileName () { return applicationPropertyFileName; }

	/**
	 * <p>Set the name of the property file set for the application.</p>
	 *
	 * @param	applicationPropertyFileName	the property file name
	 */
	protected void setApplicationPropertyFileName (String applicationPropertyFileName) { this.applicationPropertyFileName=applicationPropertyFileName; }

	/**
	 * <p>Searches for the property with the specified key in the specified property list, insisting on a value.</p>
	 *
	 * @param	properties	the property list to search
	 * @param	key		the property name
	 * @throws	Exception	if there is no such property or it has no value
	 */
	static public String getPropertyInsistently(Properties properties,String key) throws Exception {
		String value = properties.getProperty(key);
		if (value == null || value.length() == 0) {
			throw new Exception("Properties do not contain value for "+key);
		}
		return value;
	}

	/**
	 * <p>Searches for the property with the specified key in this application's property list, insisting on a value.</p>
	 *
	 * @param	key		the property name
	 * @throws	Exception	if there is no such property or it has no value
	 */
	public String getPropertyInsistently(String key) throws Exception {
		return getPropertyInsistently(applicationProperties,key);
	}

	/**
	 * <p>Store a JPEG snapshot of the specified window in the user's home directory.</p>
	 *
	 * @param	extent		the rectangle to take a snapshot of (typically <code>this.getBounds()</code> for whole application)
	 */
	protected File takeSnapShot(Rectangle extent) {
		File snapShotFile = null;
		try {
			snapShotFile = File.createTempFile("snap",".jpg",new File(System.getProperty("user.home")));
			java.awt.image.BufferedImage snapShotImage = new Robot().createScreenCapture(extent);
			javax.imageio.ImageIO.write(snapShotImage,"jpeg",snapShotFile);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return snapShotFile;
	}

//	/**
//	 * <p>Construct a window with the default size and title and no property source.</p>
//	 *
//	 * <p>Does not show the window.</p>
//	 *
//	 * <p>Will exit the application when the window closes.</p>
//	 */
//	public ApplicationBase() {
//		//this("Application Frame",640,480);
//		this("Application Frame",null);
//	}

//	/**
//	 * <p>Construct a window with the default size and title and no property source.</p>
//	 *
//	 * <p>Does not show the window.</p>
//	 *
//	 * @param	closeOperation	argument to {@link javax.swing.JFrame#setDefaultCloseOperation(int) setDefaultCloseOperation()}
//	 */
//	public ApplicationBase(int closeOperation) {
//		//this("Application Frame",640,480,closeOperation);
//		this("Application Frame",null,closeOperation);
//	}

//	/**
//	 * <p>Construct a window with the default size, specified title and no property source.</p>
//	 *
//	 * <p>Does not show the window.</p>
//	 *
//	 * <p>Will exit the application when the window closes.</p>
//	 *
//	 * @param	title				the title for the top bar decoration
//	 */
//	public ApplicationBase(String title) {
//		//this(title,640,480);
//		this(title,null);
//	}

//	/**
//	 * <p>Construct a window with the default size, specified title and no property source.</p>
//	 *
//	 * <p>Does not show the window.</p>
//	 *
//	 * @param	title					the title for the top bar decoration
//	 * @param	closeOperation			argument to {@link javax.swing.JFrame#setDefaultCloseOperation(int) setDefaultCloseOperation()}
//	 */
//	public ApplicationBase(String title, int closeOperation) {
//		//this(title,640,480,closeOperation);
//		this(title,null,closeOperation);
//	}

	/**
	 * <p>Construct a window with the default size, and specified title and property sources.</p>
	 *
	 * <p>Does not show the window.</p>
	 *
	 * <p>Will exit the application when the window closes.</p>
	 *
	 * @param	applicationPropertyFileName	the name of the properties file
	 */
	public ApplicationBase(String applicationPropertyFileName) {
        this.applicationPropertyFileName = applicationPropertyFileName;
        loadProperties();
        createGUI();
	}


//	/**
//	 * <p>Construct a window with the specified size, title and property sources.</p>
//	 *
//	 * <p>Does not show the window.</p>
//	 *
//	 * <p>Will exit the application when the window closes.</p>
//	 *
//	 * @param	title						the title for the top bar decoration
//	 * @param	applicationPropertyFileName	the name of the properties file
//	 * @param	w							width
//	 * @param	h							height
//	 */
//	public ApplicationBase(String title, String applicationPropertyFileName, int w, int h) {
//		this(title,applicationPropertyFileName,w,h,JFrame.EXIT_ON_CLOSE);
//	}

//	/**
//	 * <p>Construct a window with the specified size, title and property sources.</p>
//	 *
//	 * <p>Does not show the window.</p>
//	 *
//	 * @param	title						the title for the top bar decoration
//	 * @param	applicationPropertyFileName	the name of the properties file
//	 * @param	w							width
//	 * @param	h							height
//	 * @param	closeOperation				argument to {@link javax.swing.JFrame#setDefaultCloseOperation(int) setDefaultCloseOperation()}
//	 */
//	public ApplicationBase(String title, String applicationPropertyFileName, int w, int h, int closeOperation) {
//		setApplicationPropertyFileName(applicationPropertyFileName);
//		loadProperties();
//		if (title != null) setTitle(title);
//		createGUI();
//		setSize(w,h);
//		setDefaultCloseOperation(closeOperation);
//	}

	/**
	 * <p>Setup internationalized fonts if possible.</p>
	 *
	 * <p>Invoked by {@link com.pixelmed.display.ApplicationBase#createGUI() createGUI()}.</p>
	 */
	public static void setInternationalizedFontsForGUI() {
//System.err.println("ApplicationFrame.setInternationalizedFontsForGUI()");

		Font font = new Font("Arial Unicode MS",Font.PLAIN,12);
		if (font == null || !font.getFamily().equals("Arial Unicode MS")) {
			font = new Font("Bitstream Cyberbit",Font.PLAIN,13);
			if (font == null || !font.getFamily().equals("Bitstream Cyberbit")) {
				font=null;
			}
		}
		if (font == null) {
			System.err.println("Warning: couldn't set internationalized font: non-Latin values may not display properly");
		}
		else {
//System.err.println("Using internationalized font "+font);
			UIManager.put("Tree.font",font);
			UIManager.put("Table.font",font);
			//UIManager.put("Label.font",font);
		}

	}

	/**
	 * <p>Setup background for UI.</p>
	 *
	 * <p>Invoked by {@link com.pixelmed.display.ApplicationBase#createGUI() createGUI()}.</p>
	 */
	public static void setBackgroundForGUI() {
		String laf = UIManager.getLookAndFeel().getClass().getName();
//System.err.println("setBackgroundForGUI(): L&F is "+laf);
		if (UIManager.getLookAndFeel().getClass().getName().equals("com.apple.laf.AquaLookAndFeel")) {
			// we want the darker gray than is the default
			// note that the JFrame.setBackground(Color.lightGray) that we used to use does not reliably propagate
			UIManager.put("Panel.background",Color.lightGray);
			UIManager.put("CheckBox.background",Color.lightGray);
			UIManager.put("SplitPane.background",Color.lightGray);
		}
	}

	/**
	 * <p>Setup preferred Look and Feel.</p>
	 *
	 * <p>Invoked by {@link com.pixelmed.display.ApplicationBase#createGUI() createGUI()}.</p>
	 */
	public static void setPreferredLookAndFeelForPlatform() {
		try {
			String osName = System.getProperty("os.name");
			if (osName != null && osName.toLowerCase(Locale.US).startsWith("windows")) {	// see "http://lopica.sourceforge.net/os.html" for list of values
//System.err.println("ApplicationFrame.setPreferredLookAndFeelForPlatform(): detected Windows - using Windows LAF");
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * <p>Do what is necessary to build an application window.</p>
	 *
	 * <p>Invoked by constructors.</p>
	 *
	 * <p>Sub-classes should call this if they do not use the super() constructors,
	 * but should NOT usually need to override it, but rather should
	 * override the methods that it calls.</p>
	 */
	protected void createGUI() {
//System.err.println("ApplicationFrame.createGUI()");
		setPreferredLookAndFeelForPlatform();
		setBackgroundForGUI();
		setInternationalizedFontsForGUI();
	} 
	
//	/**
//	 * <p>For testing.</p>
//	 *
//	 * <p>Shows an empty default sized window.</p>
//	 *
//	 * @param	arg	ignored
//	 */
//	public static void main(String arg[]) {
//		ApplicationBase af = new ApplicationBase();
//		af.setVisible(true);
//	}
 
}

