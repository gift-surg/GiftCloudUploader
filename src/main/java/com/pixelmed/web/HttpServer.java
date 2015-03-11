/* Copyright (c) 2004-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * <p>The {@link com.pixelmed.web.HttpServer HttpServer} class is an abstract class that implements
 * a minimal GET method for a web server, primarily as a basis to implement {@link com.pixelmed.web.WadoServer WadoServer}.</p>
 *
 * <p>An abstract inner class, such as {@link com.pixelmed.web.HttpServer.Worker HttpServer.Worker},
 * needs to be extended by any concrete sub-class, and in particular its {@link Worker#generateResponseToGetRequest(String,OutputStream) generateResponseToGetRequest()} implemented.</p>
 *
 * @see com.pixelmed.web.WadoServer
 *
 * @author	dclunie
 */
public abstract class HttpServer implements Runnable {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/web/HttpServer.java,v 1.8 2012/07/31 15:35:08 dclunie Exp $";

	private int port;
	private int numberOfWorkers;
	private Vector threadPool;
	
	protected int webServerDebugLevel;

	protected static int defaultNumberOfWorkers = 20;
	
	public HttpServer(int webServerDebugLevel) {
		// constructor to support derived classes ... will call initializeThreadPool(port) later, after other constructor business done
		this.webServerDebugLevel=webServerDebugLevel;
	}
	
	public HttpServer(int port,int webServerDebugLevel) {
		this.webServerDebugLevel=webServerDebugLevel;
		initializeThreadPool(port,defaultNumberOfWorkers);
	}
	
	public HttpServer(int port,int webServerDebugLevel,int numberOfWorkers) {
		this.webServerDebugLevel=webServerDebugLevel;
		initializeThreadPool(port,numberOfWorkers);
	}
	
	public void initializeThreadPool(int port,int numberOfWorkers) {
if (webServerDebugLevel > 1) System.err.println("HttpServer.initializeThreadPool(): start on port "+port+" with "+numberOfWorkers+" workers");
		this.port=port;
		this.numberOfWorkers=numberOfWorkers;
		threadPool=new Vector();
		for (int i=0; i<numberOfWorkers; ++i) {
			Worker w = createWorker();
			(new Thread(w, "worker #"+i)).start();
			threadPool.addElement(w);
		}
if (webServerDebugLevel > 1) System.err.println("HttpServer.initializeThreadPool(): end");
	}

	
	public void initializeThreadPool(int port) {
		initializeThreadPool(port,defaultNumberOfWorkers);
	}
	
	public synchronized void run() {
if (webServerDebugLevel > 1) System.err.println("HttpServer.run(): start");
		try {
			ServerSocket ss = new ServerSocket(port);
			while (true) {
				Socket s = ss.accept();
				Worker w = null;
				synchronized (threadPool) {
					if (threadPool.isEmpty()) {
if (webServerDebugLevel > 1) System.err.println("HttpServer.run(): additional worker");
						Worker ws = createWorker();
						ws.setSocket(s);
						(new Thread(ws, "additional worker")).start();
					}
					else {
						w = (Worker) threadPool.elementAt(0);
						threadPool.removeElementAt(0);
						w.setSocket(s);
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected abstract class Worker implements Runnable {
		private Socket socket;
		
		private synchronized void setSocket(Socket socket) {
			this.socket = socket;
			notify();
		}
		
		public synchronized void run() {
if (webServerDebugLevel > 1) System.err.println("HttpServer.Worker.run(): start");
			while(true) {
				if (socket == null) {
					/* nothing to do */
					try {
						wait();
					} catch (InterruptedException e) {
						/* should not happen */
						continue;
					}
				}
				try {
					handleConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}
				// go back in wait queue if there's fewer than numHandler connections.
if (webServerDebugLevel > 1) System.err.println("HttpServer.Worker.run(): done");
				socket = null;
				synchronized (threadPool) {
					if (threadPool.size() >= numberOfWorkers) {
						/* too many threads, exit this one */
if (webServerDebugLevel > 1) System.err.println("HttpServer.Worker.run(): not needed");
						return;
					}
					else {
if (webServerDebugLevel > 1) System.err.println("HttpServer.Worker.run(): going back into pool");
						threadPool.addElement(this);
					}
				}
			}
		}
		
		private void handleConnection() {
if (webServerDebugLevel > 1) System.err.println("HttpServer.Worker.handleConnection():");
			try {
			    //int transactionCount=1;
			    String line;
			    //do {
//if (webServerDebugLevel > 1) System.err.println("HttpServer.Worker.handleConnection(): transactionCount="+transactionCount);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
				Vector requestAndHeaderLines = new Vector();
				while ((line=reader.readLine()) != null && line.length() > 0) {	// loop until empty line
if (webServerDebugLevel > 1) System.err.println("HttpServer.Worker.handleConnection(): read line=\""+line+"\"");
					requestAndHeaderLines.add(line);
				}
				if (requestAndHeaderLines.size() > 0 ) {
					String requestLine = (String)requestAndHeaderLines.get(0);
if (webServerDebugLevel > 1) System.err.println("HttpServer.Worker.handleConnection(): requestLine=\""+requestLine+"\"");
					StringTokenizer st = new StringTokenizer(requestLine," ");
					if (st.countTokens() != 3) {
						writer.print("HTTP/1.1 400 Bad Request\r\n");
						writer.flush();
					}
					else {
						String method = st.nextToken();
						String requestURI = st.nextToken();
						String httpVersion = st.nextToken();
						if (httpVersion == null || !(httpVersion.equals("HTTP/1.0") || httpVersion.equals("HTTP/1.1"))) {
							writer.print("HTTP/1.1 505 HTTP Version Not Supported\r\n");
							writer.flush();
						}
						else if (method != null) {
							if (method.equals("GET")) {
								//try {
									generateResponseToGetRequest(requestURI,socket.getOutputStream());
								//}
								//catch (Exception e) {
								//	e.printStackTrace(System.err);
								//}
							}
							else {
if (webServerDebugLevel > 1) System.err.println("HttpServer.Worker.handleConnection(): Not Implemented method=\""+method+"\"");
								writer.print("HTTP/1.1 501 Not Implemented\r\n");
								writer.flush();
							}
						}
					}
				}
				//++transactionCount;
			    //} while (line != null);
			}
			catch (IOException e) {
				e.printStackTrace(System.err);
			}
			finally {
				try {
if (webServerDebugLevel > 1) System.err.println("HttpServer.Worker.handleConnection(): closing socket");
					socket.close();
				}
				catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}
		}

		abstract protected void generateResponseToGetRequest(String requestURI,OutputStream out) throws IOException;
	}

	abstract protected Worker createWorker();
}
