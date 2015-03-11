/* Copyright (c) 2007, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.utils;

/**
 * <p>The {@link com.pixelmed.utils.XPathQuery XPathQuery} class provides
 * a command line utility for performing XPath queries against an XML file.</p>
 *
 * <p>For example, one can apply it to the XML representation of a DICOM object, as follows:</p>
 *
 * <pre>
 * java -cp pixelmed.jar com.pixelmed.utils.XPathQuery dicomfile.xml "//ImageType/value[@number=1] = 'ORIGINAL'"
 * </pre>
 *
 * @see com.pixelmed.dicom.XMLRepresentationOfDicomObjectFactory
 *
 * @author	dclunie
 */
public class XPathQuery {
	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/utils/XPathQuery.java,v 1.2 2008/01/17 22:06:28 dclunie Exp $";
	
	public static String getNamedAttributeValueOfElementNode(org.w3c.dom.Node node,String attributeName) {
		String value = null;
		if (node != null && node.hasAttributes()) {
			org.w3c.dom.Node attribute = node.getAttributes().getNamedItem(attributeName);
			if (attribute != null) {
				value = attribute.getNodeValue();
			}
		}
		return value;
	}

	/*
	 * @param	arg
	 */
	public static void main(String arg[]) {
		try {
			System.err.println(javax.xml.xpath.XPathFactory.newInstance().newXPath().evaluate(arg[1],new org.xml.sax.InputSource(new java.io.FileInputStream(arg[0]))));
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
