/*
 * SubjectInformation
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */

package uk.ac.ucl.cs.cmic.giftcloud.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SubjectInformation {
	private final RestServerHelper restServerHelper;
	private final Project project;

	private String label;

	public SubjectInformation(RestServerHelper restServerHelper, Project project) {
		this.restServerHelper = restServerHelper;
		this.project = project;

	}

	public Subject uploadTo() throws UploadSubjectException {
		try {
			final InputStream xmlStream = createProcessorStream();

			final String response = restServerHelper.uploadSubject(project.toString(), xmlStream);

			// parse out id from response
			return new Subject(label, parseId(response));

		} catch (Exception e) {
			throw new UploadSubjectException("Error submitting new subject XML to XNAT.", e);
		}
	}


	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public String toString() {
		return getLabel();
	}

	protected Document buildXML() throws ParserConfigurationException {
		final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final Document document = builder.getDOMImplementation().createDocument("http://nrg.wustl.edu/xnat", "xnat:Subject", null);

		final Element root = document.getDocumentElement();
		root.setAttribute("project", project.toString());
		root.setAttribute("label", label);
		return document;
	}

	private InputStream createProcessorStream() throws UploadSubjectException, ParserConfigurationException, TransformerException {
		return transformXML(buildXML());
	}

	private InputStream transformXML(Document document) throws TransformerConfigurationException, TransformerException {
		// temporary buffer
		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		// perform the transform
		final DOMSource source = new DOMSource(document);
		final StreamResult result = new StreamResult(out);
		final Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(source, result);

		// convert OutputStream to InputStream (could use
		// Piped{Input,Output}Stream, but would require spawning a thread, plus
		// we should have enough memory for the subject XML document)
		return new ByteArrayInputStream(out.toByteArray());
	}

	private String parseId(String response) {
		// response should contain a URI to the newly created subject, we take
		// the last part of that URI as the subject's ID
		String[] parts = response.trim().split("/");
		return parts[parts.length - 1];
	}

	public static final class UploadSubjectException extends Exception {
		private static final long serialVersionUID = -1331357997499624104L;

		public UploadSubjectException(String message, Throwable e) {
			super(message, e);
		}
	}
}
