/* Copyright (c) 2004-2005, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;

import com.pixelmed.database.DatabaseInformationModel;

/**
 * <p>The {@link com.pixelmed.web.PathRequestHandler PathRequestHandler} creates a response to an HTTP request for
 * a named path to a file.</p>
 *
 * @author	dclunie
 */
class PathRequestHandler extends RequestHandler {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/web/PathRequestHandler.java,v 1.8 2012/02/01 23:02:12 dclunie Exp $";
	
	private static final String faviconPath = "favicon.ico";
	private static final String actualIndexPath = "index.html";

	protected PathRequestHandler(String stylesheetPath,int webServerDebugLevel) {
		super(stylesheetPath,webServerDebugLevel);
	}

	protected void generateResponseToGetRequest(DatabaseInformationModel databaseInformationModel,String rootURL,String requestURI,WebRequest request,String requestType,OutputStream out) throws IOException {
		try {
			// assert (requestType == null);
			String requestPath = request.getPath();
if (webServerDebugLevel > 0) System.err.println("PathRequestHandler.generateResponseToGetRequest(): Was asked for requestPath "+requestPath);
			if (requestPath == null) {
				throw new Exception("No such path - path is null - =\""+requestPath+"\"");
			}
			if (requestPath.equals("/") || requestPath.toLowerCase(java.util.Locale.US).equals("/index.html") || requestPath.toLowerCase(java.util.Locale.US).equals("/index.htm")) {
if (webServerDebugLevel > 1) System.err.println("PathRequestHandler.generateResponseToGetRequest(): root path");
				requestPath="/"+actualIndexPath;
			}
			if (requestPath.equals("/"+stylesheetPath) || requestPath.equals("/"+faviconPath) || requestPath.equals("/"+actualIndexPath) || requestPath.startsWith("/dicomviewer")) {
if (webServerDebugLevel > 1) System.err.println("PathRequestHandler.generateResponseToGetRequest(): Was asked for file "+requestPath);
				String baseNameOfRequestedFile = new File(requestPath).getName();
				if (requestPath.startsWith("/dicomviewer")) {
					baseNameOfRequestedFile = "dicomviewer/" + baseNameOfRequestedFile;
				}
if (webServerDebugLevel > 1) System.err.println("PathRequestHandler.generateResponseToGetRequest(): Trying to find amongst resources "+baseNameOfRequestedFile);
				String tryRequestedFile = "/"+baseNameOfRequestedFile;
if (webServerDebugLevel > 2) System.err.println("PathRequestHandler.generateResponseToGetRequest(): Looking for "+tryRequestedFile);
				InputStream fileStream = PathRequestHandler.class.getResourceAsStream(tryRequestedFile);
				if (fileStream == null) {
					tryRequestedFile = "/com/pixelmed/web/"+baseNameOfRequestedFile;
if (webServerDebugLevel > 2) System.err.println("PathRequestHandler.generateResponseToGetRequest(): Failed; so look instead for "+tryRequestedFile);
					fileStream = PathRequestHandler.class.getResourceAsStream(tryRequestedFile);
					if (fileStream == null) {
						throw new Exception("No such resource as "+requestPath);
					}
				}
				
				boolean isText = false;
				String contentType;
				if (baseNameOfRequestedFile.matches(".*[.][cC][sS][sS]$")) {
					contentType = "text/css";
					isText = true;
				}
				else if (baseNameOfRequestedFile.matches(".*[.][hH][tT][mM][lL]*$")) {
					contentType = "text/html";
					isText = true;
				}
				else if (baseNameOfRequestedFile.matches(".*[.][iI][cC][oO]$")) {
					contentType = "image/x-icon";
				}
				else {
					contentType = "application/octet-stream";
				}
if (webServerDebugLevel > 1) System.err.println("PathRequestHandler.generateResponseToGetRequest(): contentType "+contentType);

				if (isText) {
					// read the whole thing into a string so that we can know its length for Content-Length; blech :(
					InputStreamReader reader = new InputStreamReader(new BufferedInputStream(fileStream),"UTF-8");
					StringBuffer strbuf =  new StringBuffer();
					char[] buffer = new char[1024];
					int count;
					while ((count=reader.read(buffer,0,1024)) > 0) {
if (webServerDebugLevel > 2) System.err.println("PathRequestHandler.generateResponseToGetRequest(): Read "+count+" chars");
						strbuf.append(buffer,0,count);
					}
					sendHeaderAndBodyText(out,strbuf.toString(),baseNameOfRequestedFile,contentType);
				}
				else {
					sendHeaderAndBodyOfStream(out,fileStream,baseNameOfRequestedFile,contentType);
				}
			}
			else {
				throw new Exception("No such path is permitted =\""+requestPath+"\"");
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
if (webServerDebugLevel > 0) System.err.println("PathRequestHandler.generateResponseToGetRequest(): Sending 404 Not Found");
			send404NotFound(out,e.getMessage());
		}
	}
}

