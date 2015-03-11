/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import java.io.*;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.HexDump;

/**
 * <p>A specialized java.io.OutputStream which buffers and fragments
 * data which is written to it into PDUs and sends them over
 * the supplied OutputStream which is (presumably) that of the java.net.Socket
 * of an established {@link Association Association}.</p>
 *
 * <p>This stream buffers data that is written to it and when it is either
 * flushed or closed or reaches the specified maximum PDU size, writes
 * data (not command) PDU's to the supplied output stream.</p>
 *
 * <p>Need to take care with "last fragment" flag ... that cannot be set
 * until close() is called, and if the buffer is empty at that time,
 * a zero length PDU will be sent.</p>
 *
 * @author	dclunie
 */
public class AssociationOutputStream extends OutputStream {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/AssociationOutputStream.java,v 1.17 2014/09/09 20:34:09 dclunie Exp $";
	
	private static final int ourMaxPDUSize = 0x10000;		// must be less than or equal to the maximum unsigned int value for Java
	//private static final int ourMaxPDUSize = Integer.MAX_VALUE;	// must be less than or equal to the maximum unsigned int value for Java

	private static final int ourMinPDUSize = 8;		// one PDV takes at least 6 bytes (headers + length), must be even length, so min is 2 bytes (!)
								// note that the max PDU length does not include the PDU header and length itself,
								// i.e. it is really the maximum length of the PDV Items
	
	private OutputStream out;
	private int presentationContextID;
	
	private byte dataBuffer[];
	private int dataBufferSize;
	private int dataBufferIndex;
	
	private boolean isCommand;
	private boolean isLastFragment;
	
	private int debugLevel;
	
	/**
	 * Construct a PDU buffering OutputStream on top of another OutputStream
	 *
	 * @param	out			where to send the buffered output
	 * @param	maxPDUSize		how large to make the buffer (i.e. the PDU) size
	 * @param	presentationContextID	included in the header of each PDU
	 * @param	debugLevel			0 for no debugging, &gt; 0 for increasingly verbose debugging
	 * @throws	DicomNetworkException
	 */
	public AssociationOutputStream(OutputStream out,int maxPDUSize,int presentationContextID,int debugLevel) throws DicomNetworkException {
		this.out=out;
		this.presentationContextID=presentationContextID;
		this.debugLevel=debugLevel;

if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:maxPDUSize="+maxPDUSize);
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:ourMinPDUSize="+ourMinPDUSize);
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:ourMaxPDUSize="+ourMaxPDUSize);
		if (maxPDUSize != 0 && maxPDUSize < ourMinPDUSize) throw new DicomNetworkException("Maximum PDU Size too small to be usable ("+maxPDUSize+" bytes");
		dataBufferSize=((maxPDUSize == 0 || maxPDUSize > ourMaxPDUSize) ? ourMaxPDUSize : maxPDUSize)-6;
if (debugLevel > 1) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:dataBufferSize="+dataBufferSize);
		dataBuffer = new byte[dataBufferSize];
		dataBufferIndex=0;
		
		isCommand=false;	// always Data for now
		isLastFragment=false;	// will be set by close() before close() calls flush()
	}

	/**
	 * Buffer the supplied data, flushing (actually sending) PDUs
	 * when the buffer is filled.
	 *
	 * @param	i
	 * @throws	IOException
	 */
	public void write(int i) throws IOException {
		// inefficient, but won't be doing it much ...
		byte b[] = new byte[1];
		b[0]=(byte)i;
		write(b,0,1);
	}

	/**
	 * Buffer the supplied data, flushing (actually sending) PDUs
	 * when the buffer is filled.
	 *
	 * @param	b
	 * @throws	IOException
	 */
	public void write(byte b[]) throws IOException {
		write(b,0,b.length);
	}

	/**
	 * Buffer the supplied data, flushing (actually sending) PDUs
	 * when the buffer is filled.
	 *
	 * @param	b
	 * @param	off
	 * @param	len
	 * @throws	IOException
	 */
	public void write(byte b[], int off, int len) throws IOException {
if (debugLevel > 4) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:write() start: len="+len);
		// copy exception generation stuff from OutputStream.java ...
		if (b == null) {
			throw new NullPointerException();
		}
		else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		
		while (dataBufferIndex+len > dataBufferSize) {
			int useLen=dataBufferSize-dataBufferIndex;
if (debugLevel > 4) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:write() looping to write useLen="+useLen);
			System.arraycopy(b,off,dataBuffer,dataBufferIndex,useLen);
			dataBufferIndex+=useLen;
			sendPDV();
			off+=useLen;
			len-=useLen;
		}
		if (len > 0) {
if (debugLevel > 4) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:write() residual write len="+len);
			System.arraycopy(b,off,dataBuffer,dataBufferIndex,len);
			dataBufferIndex+=len;
		}
if (debugLevel > 4) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:write() done");
	}

	/**
	 * Actually send the PDU buffered so far as one PDV.
	 *
	 * @throws	IOException
	 */
	private void sendPDV() throws IOException {		// would like to send DicomException, but can't change method signature
if (debugLevel > 3) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:flush() start");
		// actually send the PDU with one PDV
		
		if (dataBufferIndex%2 != 0) throw new IOException("PDV must be even length");
		
if (debugLevel > 3) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:flush()="+HexDump.dump(dataBuffer,dataBufferIndex));

		int pdvItemLength = 2 + dataBufferIndex;	// the pcID and the command/data flag are included in the PDV length
		int pduLength = pdvItemLength + 4;		// just the length field of the PDV
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:flush() pduLength="+pduLength);
		
		out.write(0x04);				// P-DATA-TF PDU Type
		out.write(0x00);				// reserved
		out.write((pduLength>>24)&0xff);		// Big endian
		out.write((pduLength>>16)&0xff);
		out.write((pduLength>>8)&0xff);
		out.write(pduLength&0xff);

		out.write((pdvItemLength>>24)&0xff);		// Big endian
		out.write((pdvItemLength>>16)&0xff);
		out.write((pdvItemLength>>8)&0xff);
		out.write(pdvItemLength&0xff);
		
		out.write(presentationContextID);
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:flush() isLastFragment="+isLastFragment);
		int messageControlHeader = ((isLastFragment ? 1 : 0) << 1) | (isCommand ? 1 : 0);
		out.write(messageControlHeader);

if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:flush() writing data length="+dataBufferIndex);
		if (dataBufferIndex > 0) out.write(dataBuffer,0,dataBufferIndex);
		//while (dataBufferIndex > 0) {
		//	int offset=0;
		//	int count = dataBufferIndex > 1024 ? 1024 : dataBufferIndex;
//System.err.println("AssociationOutputStream:flush() writing count="+count);
		//	if (count > 0) {
		//		out.write(dataBuffer,offset,count);
		//		offset+=count;
		//		dataBufferIndex-=count;
		//	}
		//}
		
		out.flush();					// Actually send it
		
		dataBufferIndex=0;
if (debugLevel > 3) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:flush() done");
	}
	
	/**
	 * Sets the last fragment flag and flushes (which sends a zero
	 * length PDU if necessary, and pads to an even length, if necessary).
	 *
	 * Does NOT actually close the underlying stream, since that
	 * may well be used for other operations later.
	 *
	 * @throws	IOException
	 */
	public void close() throws IOException {
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:close() start");
		isLastFragment=true;
		if (dataBufferIndex%2 != 0) {
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:close() padding with an extra null byte to get to even length");
			dataBuffer[dataBufferIndex++]=0;	// pad to even length
		}
		sendPDV();
		//out.close();		// don't really want to close the underlying stream
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:close() done");
	}
	
	/**
	 * Do nothing.
	 *
	 * @throws	IOException
	 */
	public void flush() throws IOException {
if (debugLevel > 2) System.err.println(new java.util.Date().toString()+": AssociationOutputStream:flush() does nothing");
	}
}

