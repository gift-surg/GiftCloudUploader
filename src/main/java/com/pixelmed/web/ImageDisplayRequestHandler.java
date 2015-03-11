/* Copyright (c) 2004-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.web;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.dicom.InformationEntity;
import com.pixelmed.utils.FileUtilities;
import com.pixelmed.utils.FloatFormatter;

/**
 * <p>The {@link com.pixelmed.web.ImageDisplayRequestHandler ImageDisplayRequestHandler} creates a response to an HTTP request for
 * a page that displays all the images in a specified series.</p>
 *
 * @author	dclunie
 */
class ImageDisplayRequestHandler extends RequestHandler {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/web/ImageDisplayRequestHandler.java,v 1.11 2014/12/17 14:22:56 dclunie Exp $";
	
	private String imageDisplayTemplateFileName;

	protected ImageDisplayRequestHandler(String stylesheetPath,String imageDisplayTemplateFileName,int webServerDebugLevel) {
		super(stylesheetPath,webServerDebugLevel);
		this.imageDisplayTemplateFileName=imageDisplayTemplateFileName;
	}
	
	private static final double[] getDoubleArrayOrNullFromDatabaseStringValue(String stringValue) {
		double[] values = null;
		try {
			if (stringValue != null) {
				values=FloatFormatter.fromString(stringValue,'\\');
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			values=null;
		}
		return values;
	}
	
	private class CompareDatabaseAttributesByInstanceNumber implements Comparator {
		public int compare(Object o1,Object o2) {
			int returnValue = 0;
			String si1 = (String)(((Map)o1).get("INSTANCENUMBER"));
			String si2 = (String)(((Map)o2).get("INSTANCENUMBER"));
			if (si1 == null) si1="";
			if (si2 == null) si2="";
			try {
				int i1 = si1.length() > 0 ? Integer.parseInt(si1) : 0;
				int i2 = si2.length() > 0 ? Integer.parseInt(si2) : 0;
				returnValue = i1 - i2;
			}
			catch (NumberFormatException e) {
				e.printStackTrace(System.err);
				returnValue = si1.compareTo(si2);
			}
			return returnValue;
		}
	}
	
	private Comparator compareDatabaseAttributesByInstanceNumber = new CompareDatabaseAttributesByInstanceNumber();

	protected void generateResponseToGetRequest(DatabaseInformationModel databaseInformationModel,String rootURL,String requestURI,WebRequest request,String requestType,OutputStream out) throws IOException {
		try {
			Map parameters = request.getParameters();
			if (parameters == null) {
				throw new Exception("Missing parameters for requestType \""+requestType+"\"");
			}
			String parentPrimaryKey = (String)(parameters.get("primaryKey"));
			if (parentPrimaryKey == null || parentPrimaryKey.length() == 0) {
				throw new Exception("Missing primaryKey parameter for requestType \""+requestType+"\"");
			}
			String studyInstanceUID = (String)(parameters.get("studyUID"));
			if (studyInstanceUID == null || studyInstanceUID.length() == 0) {
				throw new Exception("Missing studyUID parameter for requestType \""+requestType+"\"");
			}
			String seriesInstanceUID = (String)(parameters.get("seriesUID"));
			if (seriesInstanceUID == null || seriesInstanceUID.length() == 0) {
				throw new Exception("Missing seriesUID parameter for requestType \""+requestType+"\"");
			}
						
			InputStream fileStream = RequestTypeServer.class.getResourceAsStream("/com/pixelmed/web/"+imageDisplayTemplateFileName);
			if (fileStream == null) {
				throw new Exception("No page template \""+imageDisplayTemplateFileName+"\"");
			}
						
			String template = FileUtilities.readFile(fileStream);
if (webServerDebugLevel > 2) System.err.println("ImageDisplayRequestHandler.generateResponseToGetRequest(): Template is "+template);

			// replace ####REPLACEMEWITHLISTOFSOPINSTANCEUIDS#### with list of
			// the form:
			//	"uid1"
			//	,"uid2"
			//	...
			//	,"uidn"
						
			StringBuffer sopInstanceUIDReplacementStrbuf = new StringBuffer();
			StringBuffer windowCenterReplacementStrbuf = new StringBuffer();
			StringBuffer windowWidthReplacementStrbuf = new StringBuffer();
			String prefix = "";
			String primaryKeyColumnName = databaseInformationModel.getLocalPrimaryKeyColumnName(InformationEntity.INSTANCE);
			ArrayList instances = databaseInformationModel.findAllAttributeValuesForAllRecordsForThisInformationEntityWithSpecifiedParent(
				InformationEntity.INSTANCE,parentPrimaryKey);
				
			Collections.sort(instances,compareDatabaseAttributesByInstanceNumber);
			
			int numberOfInstance = instances.size();
			for (int s=0; s<numberOfInstance; ++s) {
				Map instance = (Map)(instances.get(s));
							
				String sopInstanceUID = (String)(instance.get("SOPINSTANCEUID"));
				sopInstanceUIDReplacementStrbuf.append(prefix);
				sopInstanceUIDReplacementStrbuf.append("\"");
				sopInstanceUIDReplacementStrbuf.append(sopInstanceUID);
				sopInstanceUIDReplacementStrbuf.append("\"");
							
							
				double[] windowCenters = getDoubleArrayOrNullFromDatabaseStringValue((String)(instance.get("WINDOWCENTER")));
				double windowCenter = windowCenters == null || windowCenters.length == 0 ? 0 : windowCenters[0];
if (webServerDebugLevel > 1) System.err.println("ImageDisplayRequestHandler.generateResponseToGetRequest(): instance "+s+" windowCenter="+windowCenter);
				windowCenterReplacementStrbuf.append(prefix);
				//windowCenterReplacementStrbuf.append("\"");
				windowCenterReplacementStrbuf.append(windowCenter);
				//windowCenterReplacementStrbuf.append("\"");

				double[] windowWidths  = getDoubleArrayOrNullFromDatabaseStringValue((String)(instance.get("WINDOWWIDTH")));
				double windowWidth = windowWidths == null || windowWidths.length == 0 ? 0 : windowWidths[0];
if (webServerDebugLevel > 1) System.err.println("ImageDisplayRequestHandler.generateResponseToGetRequest(): instance "+s+" windowWidth="+windowWidth);
				windowWidthReplacementStrbuf.append(prefix);
				//windowWidthReplacementStrbuf.append("\"");
				windowWidthReplacementStrbuf.append(windowWidth);
				//windowWidthReplacementStrbuf.append("\"");
							
				prefix="\n,";
			}
			sopInstanceUIDReplacementStrbuf.append("\n");
			windowCenterReplacementStrbuf.append("\n");
			windowWidthReplacementStrbuf.append("\n");
						
			String sopInstanceUIDReplacement = sopInstanceUIDReplacementStrbuf.toString();
if (webServerDebugLevel > 2) System.err.println("ImageDisplayRequestHandler.generateResponseToGetRequest(): sopInstanceUIDReplacement is "+sopInstanceUIDReplacement);
			template = template.replaceFirst("####REPLACEMEWITHLISTOFSOPINSTANCEUIDS####",sopInstanceUIDReplacement);
						
			String windowCenterReplacement = windowCenterReplacementStrbuf.toString();
if (webServerDebugLevel > 2) System.err.println("ImageDisplayRequestHandler.generateResponseToGetRequest(): windowCenterReplacement is "+windowCenterReplacement);
			template = template.replaceFirst("####REPLACEMEWITHWINDOWCENTERS####",windowCenterReplacement);
						
			String windowWidthReplacement = windowWidthReplacementStrbuf.toString();
if (webServerDebugLevel > 2) System.err.println("ImageDisplayRequestHandler.generateResponseToGetRequest(): windowWidthReplacement is "+windowWidthReplacement);
			template = template.replaceFirst("####REPLACEMEWITHWINDOWWIDTHS####",windowWidthReplacement);
						
if (webServerDebugLevel > 2) System.err.println("ImageDisplayRequestHandler.generateResponseToGetRequest(): Response after replacement is "+template);
			sendHeaderAndBodyText(out,template,"imagedisplay.html","text/html");
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
if (webServerDebugLevel > 0) System.err.println("ImageDisplayRequestHandler.generateResponseToGetRequest(): Sending 404 Not Found");
			send404NotFound(out,e.getMessage());
		}
	}
}

