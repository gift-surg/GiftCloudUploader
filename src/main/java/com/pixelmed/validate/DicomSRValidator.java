/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.validate;

import com.pixelmed.dicom.*;

// JAXP packages
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.Document;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The {@link com.pixelmed.validate.DicomSRValidator DicomSRValidator} class is
 * for validating SR instances against the standard IOD for the corresponding SOP Class.</p>
 *
 * <p>Typically used by reading the list of attributes that comprise an object, validating them
 * and displaying the resulting string results to the user on the standard output, in a dialog
 * box or whatever. The basic implementation of the {@link #main main} method (that may be useful as a
 * command line utility in its own right) is as follows:</p>
 *
 * <pre>
 * 	AttributeList list = new AttributeList();
 * 	list.read(arg[0],null,true,true);
 * 	DicomSRValidator validator = new DicomSRValidator();
 * 	System.err.print(validator.validate(list));
 * </pre>
 *
 * @see com.pixelmed.dicom.AttributeList
 *
 * @author	dclunie
 */
public class DicomSRValidator {
	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/validate/DicomSRValidator.java,v 1.6 2014/09/09 20:34:09 dclunie Exp $";

	/***/
	private Transformer transformerPass1;
	private Transformer transformerPass2;
	
	private class OurURIResolver implements URIResolver {
	
		String foundItems;
		
		/**
		 * @param	href
		 * @param	base
		 */
		public Source resolve(String href,String base) throws TransformerException {
//System.err.println("OurURIResolver.resolve() href="+href+" base="+base);
			StreamSource streamSource = null;
			if (href.equals("FoundItems.xml")) {			// this is the name that is hard-wired in CheckSRContentItemsUsed.xsl
				streamSource = new StreamSource(new StringReader(foundItems));
			}
			else {
				streamSource = new StreamSource(DicomSRValidator.class.getResourceAsStream("/com/pixelmed/validate/"+href));
			}
			return streamSource;
		}
		
		public void setFoundItems(String foundItems) {
			this.foundItems = foundItems;
		}
	}
	
	OurURIResolver ourURIResolver = new OurURIResolver();
	
	/**
	 * <p>Whether or not to describe the details of the validation procedure step by step.</p>
	 *
	 * <p>Default after construction is not to.</p>
	 *
	 * @param	option		true if the steps are to be described
	 */
	public void setOptionDescribeChecking(boolean option) {
		transformerPass1.setParameter("optionDescribeChecking",option ? "T" : "F");
	}
	
	/**
	 * <p>Whether or not to match the case of code meanings when validating them against the expected values in context groups and templates.</p>
	 *
	 * <p>Default after construction is true, i.e., to be case sensitive.</p>
	 *
	 * @param	option		true if matching is to be case sensitive
	 */
	public void setOptionMatchCaseOfCodeMeaning(boolean option) {
		transformerPass1.setParameter("optionMatchCaseOfCodeMeaning",option ? "T" : "F");
	}
	
	/**
	 * <p>Create an instance of validator.</p>
	 *
	 * <p>Once created, a validator may be reused for as many validations as desired.</p>
	 *
	 * @throws	javax.xml.transform.TransformerConfigurationException
	 */
	public DicomSRValidator() throws javax.xml.transform.TransformerConfigurationException {
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setURIResolver(ourURIResolver);					// this helps us find the common rules in the jar file, and the foundItems in the second pass
		transformerPass1 = tf.newTransformer(new StreamSource(DicomSRValidator.class.getResourceAsStream("/com/pixelmed/validate/"+"DicomSRDescriptionsCompiled.xsl")));
		setOptionDescribeChecking(false);
		setOptionMatchCaseOfCodeMeaning(true);
		transformerPass2 = tf.newTransformer(new StreamSource(DicomSRValidator.class.getResourceAsStream("/com/pixelmed/validate/"+"CheckSRContentItemsUsed.xsl")));
	}
	
	/**
	 * <p>Perform the first pass of the validation.</p>
	 *
	 * <p>Checks the document against the stylesheet and tracks which nodes matched templates.</p>
	 *
	 * @param	inputDocument		the XML representation of the DICOM SR instance to be validated
	 * @return						a string describing the results of the first pass of the validation
	 * @throws					javax.xml.parsers.ParserConfigurationException
	 * @throws					javax.xml.transform.TransformerException
	 * @throws					java.io.UnsupportedEncodingException
	 */
	protected String validateFirstPass(Document inputDocument) throws
			javax.xml.parsers.ParserConfigurationException,
			javax.xml.transform.TransformerException,
			java.io.UnsupportedEncodingException {

		Source inputSource = new DOMSource(inputDocument);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		StreamResult outputResult = new StreamResult(outputStream);
		transformerPass1.transform(inputSource,outputResult);
		
		return outputStream.toString("UTF-8");
	}
	
	/**
	 * <p>Perform the second pass of the validation.</p>
	 *
	 * <p>Checks for unused content items.</p>
	 *
	 * @param	inputDocument		the XML representation of the DICOM SR instance to be validated
	 * @param	firstOutputString	the list of attributes comprising the DICOM SR instance to be validated
	 * @return						a string describing the results of the validation
	 * @throws					javax.xml.parsers.ParserConfigurationException
	 * @throws					javax.xml.transform.TransformerException
	 * @throws					java.io.UnsupportedEncodingException
	 * @throws					java.io.IOException
	 */
	protected String validateSecondPass(Document inputDocument,String firstOutputString) throws
			javax.xml.parsers.ParserConfigurationException,
			javax.xml.transform.TransformerException,
			java.io.UnsupportedEncodingException,
			java.io.IOException {
		
		// parse the output of the first pass and separate the report from the list of found content items ...
		
		LineNumberReader firstOutputStringReader = new LineNumberReader(new StringReader(firstOutputString));
		StringBuffer reportOutputBuffer = new StringBuffer();
		StringBuffer foundItemsBuffer = new StringBuffer();

		foundItemsBuffer.append("<founditems>\n");
		String lineOfText;
		while ((lineOfText = firstOutputStringReader.readLine()) != null) {
			if (lineOfText.trim().startsWith("<item")) {
				foundItemsBuffer.append(lineOfText);
				foundItemsBuffer.append("\n");
			}
			else {
				reportOutputBuffer.append(lineOfText);
				reportOutputBuffer.append("\n");
			}
		}
		foundItemsBuffer.append("</founditems>\n");

		String foundItems = foundItemsBuffer.toString();
//System.err.print(foundItems);

		// now perform a second pass of the input document to report unused content items ...
		
		ourURIResolver.setFoundItems(foundItems);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		transformerPass2.transform(new DOMSource(inputDocument),new StreamResult(outputStream));
		
		String secondOutputString =  outputStream.toString("UTF-8");
//System.err.print(secondOutputString);
		reportOutputBuffer.append(secondOutputString);
		reportOutputBuffer.append("IOD validation complete\n");
		
		return reportOutputBuffer.toString();
	}

	/**
	 * <p>Validate a DICOM SR instance against the standard IOD for the appropriate SOP Class and templates.</p>
	 *
	 * @param	list	the list of attributes comprising the DICOM SR instance to be validated
	 * @param	describe	whether or not to describe the details of the validation procedure step by step
	 * @param	matchCase	whether or not to match the case of code meanings when validating them against the expected values in context groups and templates
	 * @return		a string describing the results of the validation
	 * @throws	javax.xml.parsers.ParserConfigurationException
	 * @throws	javax.xml.transform.TransformerException
	 * @throws	java.io.UnsupportedEncodingException
	 * @throws	java.io.IOException
	 */
	public String validate(AttributeList list,boolean describe,boolean matchCase) throws
			javax.xml.parsers.ParserConfigurationException,
			javax.xml.transform.TransformerException,
			java.io.UnsupportedEncodingException,
			java.io.IOException {
		
		setOptionDescribeChecking(describe);
		setOptionMatchCaseOfCodeMeaning(matchCase);
		Document inputDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(list);
		return validateSecondPass(inputDocument,validateFirstPass(inputDocument));
	}

	/**
	 * <p>Validate a DICOM SR instance against the standard IOD for the appropriate SOP Class and templates.</p>
	 *
	 * <p>Does not describe the details of the validation procedure step by step, and does match the case of code meanings when validating them against the expected values in context groups and templates.</p>
	 *
	 * @param	list	the list of attributes comprising the DICOM SR instance to be validated
	 * @return		a string describing the results of the validation
	 * @throws	javax.xml.parsers.ParserConfigurationException
	 * @throws	javax.xml.transform.TransformerException
	 * @throws	java.io.UnsupportedEncodingException
	 * @throws	java.io.IOException
	 */
	public String validate(AttributeList list) throws
			javax.xml.parsers.ParserConfigurationException,
			javax.xml.transform.TransformerException,
			java.io.UnsupportedEncodingException,
			java.io.IOException {
		
		return validate(list,false/*describe*/,true/*matchCase*/);
	}

	/**
	 * <p>Validate a DICOM SR instance against the standard IOD for the appropriate SOP Class and templates.</p>
	 *
	 * @param	filename	the DICOM SR instance to be validated
	 * @param	describe	whether or not to describe the details of the validation procedure step by step
	 * @param	matchCase	whether or not to match the case of code meanings when validating them against the expected values in context groups and templates
	 * @return		a string describing the results of the validation
	 * @throws	javax.xml.parsers.ParserConfigurationException
	 * @throws	javax.xml.transform.TransformerException
	 * @throws	java.io.UnsupportedEncodingException
	 * @throws	java.io.IOException
	 * @throws	DicomException
	 */
	public String validate(String filename,boolean describe,boolean matchCase) throws
			javax.xml.parsers.ParserConfigurationException,
			javax.xml.transform.TransformerException,
			java.io.UnsupportedEncodingException,
			java.io.IOException,
			DicomException {
		
		AttributeList list = new AttributeList();
		list.read(filename);
		return validate(list,describe,matchCase);
	}

	/**
	 * <p>Validate a DICOM SR instance against the standard IOD for the appropriate SOP Class and templates.</p>
	 *
	 * <p>Does not describe the details of the validation procedure step by step, and does match the case of code meanings when validating them against the expected values in context groups and templates.</p>
	 *
	 * @param	filename	the DICOM SR instance to be validated
	 * @return		a string describing the results of the validation
	 * @throws	javax.xml.parsers.ParserConfigurationException
	 * @throws	javax.xml.transform.TransformerException
	 * @throws	java.io.UnsupportedEncodingException
	 * @throws	java.io.IOException
	 * @throws	DicomException
	 */
	public String validate(String filename) throws
			javax.xml.parsers.ParserConfigurationException,
			javax.xml.transform.TransformerException,
			java.io.UnsupportedEncodingException,
			java.io.IOException,
			DicomException {
		
		return validate(filename,false/*describe*/,true/*matchCase*/);
	}

	/**
	 * <p>Read the DICOM file specified on the command line and validate it against the standard IOD for the appropriate storage SOP Class.</p>
	 *
	 * <p>The result of the validation is printed to the standard output.</p>
	 *
	 * @param	arg	optionally -describe, optionally -donotmatchcase, then the name of the file containing the DICOM SR instance to be validated
	 */
	public static void main(String arg[]) {
		try {
			DicomSRValidator validator = new DicomSRValidator();
			boolean describe = false;
			boolean matchCase = true;
			List<String> argList = new ArrayList<String>();
			for (String a : arg) {
				String cleanArg = a.toLowerCase().trim();
				if (cleanArg.equals("-describe")) {
					describe = true;
				}
				else if (cleanArg.equals("-donotmatchcase")) {
					matchCase = false;
				}
				else {
					argList.add(a);
				}
			}
			if (argList.size() == 1) {
				System.out.print(validator.validate(argList.get(0),describe,matchCase));
			}
			else {
				System.err.print("Usage: java com.pixelmed.validate.DicomSRValidator [-describe] [-donotmatchcase] filename");
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}

