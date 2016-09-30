/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.utils;

import java.io.*;

/**
 * <p>A class for copying an entire input stream to an output stream.</p>
 *
 * @author	dclunie
 */
public class CopyStream {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/utils/CopyStream.java,v 1.10 2014/09/09 20:34:09 dclunie Exp $";
	
	private static final int defaultReadBufferSize = 32768;
	private static final int defaultBufferedInputStreamSizeForFileCopy  = 0;	// i.e., unbuffered
	private static final int defaultBufferedOutputStreamSizeForFileCopy = 0;	// i.e., unbuffered
	
	private CopyStream() {}

	/**
	 * <p>Skip as many bytes as requested, unless an exception occurs.</p>
	 *
	 * @param	in			the input stream in which to skip the bytes
	 * @param	length		number of bytes to read (no more and no less)
	 * @throws	IOException
	 */
	public static void skipInsistently(InputStream in,long length) throws IOException {
		long remaining = length;
		while (remaining > 0) {
//System.err.println("CopyStream.skipInsistently(): looping remaining="+remaining);
			long bytesSkipped = in.skip(remaining);
//System.err.println("CopyStream.skipInsistently(): asked for ="+remaining+" got="+bytesSkipped);
			if (bytesSkipped <= 0) throw new IOException("skip failed with "+remaining+" bytes remaining to be skipped, wanted "+length);
			remaining-=bytesSkipped;
		}
	}

	/**
	 * <p>Copy the specified even number of bytes from the current position of the input stream to an output stream,
	 * swapping adjacent pairs of bytes.</p>
	 *
	 * <p>The data is copied in chunks rather than as individual bytes, but the input and output
	 * streams are used as is, and no {@link java.io.BufferedInputStream BufferedInputStream}
	 * or {@link java.io.BufferedOutputStream BufferedOutputStream} is inserted; the caller
	 * is expected to do that if maximum performance is desired.</p>
	 *
	 * <p>Also, neither the input nor the output streams are explicitly closed after the
	 * copying has complete; the caller is expected to do that as well, since there may
	 * be occasions when there is more to be written to the output, or the input is to
	 * be rewound and reused, or whatever.</p>
	 *
	 * @param	readBufferSize	how much data to read in each request
	 * @param	in				the source
	 * @param	out				the destination
	 * @param	count			the number of bytes to copy
	 * @throws	IOException	thrown if the copying fails for any reason
	 */
	public static final void copyByteSwapped(int readBufferSize,InputStream in,OutputStream out,long count) throws IOException {
		assert count%2 == 0;
		if (readBufferSize == 0) {
			readBufferSize = defaultReadBufferSize;
		}
		byte[] readBuffer = new byte[readBufferSize];
		while (count > 1) {
			int want = count > readBufferSize ? readBufferSize : (int)count;
			assert want%2 == 0;
			int have = 0;
			while (want > 0) {
				int got = in.read(readBuffer,have,want);
				have+=got;
				want-=got;
			}
			if (have > 0) {
				for (int i=0; i<have-1; i+=2) {
					byte hold = readBuffer[i];
					readBuffer[i] = readBuffer[i+1];
					readBuffer[i+1] = hold;
				}
				out.write(readBuffer,0,have);
				count-=have;
			}
		}
		out.flush();
	}

	/**
	 * <p>Copy the specified even number of bytes from the current position of the input stream to an output stream,
	 * swapping adjacent pairs of bytes.</p>
	 *
	 * <p>The data is copied in chunks rather than as individual bytes, but the input and output
	 * streams are used as is, and no {@link java.io.BufferedInputStream BufferedInputStream}
	 * or {@link java.io.BufferedOutputStream BufferedOutputStream} is inserted; the caller
	 * is expected to do that if maximum performance is desired.</p>
	 *
	 * <p>Also, neither the input nor the output streams are explicitly closed after the
	 * copying has complete; the caller is expected to do that as well, since there may
	 * be occasions when there is more to be written to the output, or the input is to
	 * be rewound and reused, or whatever.</p>
	 *
	 * @param	in		the source
	 * @param	out		the destination
	 * @param	count	the number of bytes to copy
	 * @throws	IOException	thrown if the copying fails for any reason
	 */
	public static final void copyByteSwapped(InputStream in,OutputStream out,long count) throws IOException {
		copyByteSwapped(defaultReadBufferSize,in,out,count);
	}

	/**
	 * <p>Copy the specified number of bytes from the current position of the input stream to an output stream.</p>
	 *
	 * <p>The data is copied in chunks rather than as individual bytes, but the input and output
	 * streams are used as is, and no {@link java.io.BufferedInputStream BufferedInputStream}
	 * or {@link java.io.BufferedOutputStream BufferedOutputStream} is inserted; the caller
	 * is expected to do that if maximum performance is desired.</p>
	 *
	 * <p>Also, neither the input nor the output streams are explicitly closed after the
	 * copying has complete; the caller is expected to do that as well, since there may
	 * be occasions when there is more to be written to the output, or the input is to
	 * be rewound and reused, or whatever.</p>
	 *
	 * @param	in				the source
	 * @param	out				the destination
	 * @param	count			the number of bytes to copy
	 * @param	readBufferSize	how much data to read in each request
	 * @throws	IOException	thrown if the copying fails for any reason
	 */
	public static final void copy(int readBufferSize,InputStream in,OutputStream out,long count) throws IOException {
//System.err.println("CopyStream.copy(): start count = "+count);
		if (readBufferSize == 0) {
			readBufferSize = defaultReadBufferSize;
		}
		byte[] readBuffer = new byte[readBufferSize];
		while (count > 0) {
//System.err.println("CopyStream.copy(): looping count = "+count);
			int want = count > readBufferSize ? readBufferSize : (int)count;
//System.err.println("CopyStream.copy(): want = "+want);
			int got = in.read(readBuffer,0,want);
//System.err.println("CopyStream.copy(): got = "+got);
			if (got > 0) {
				out.write(readBuffer,0,got);
				count-=got;
			}
		}
		out.flush();
	}

	/**
	 * <p>Copy the specified number of bytes from the current position of the input stream to an output stream.</p>
	 *
	 * <p>The data is copied in chunks rather than as individual bytes, but the input and output
	 * streams are used as is, and no {@link java.io.BufferedInputStream BufferedInputStream}
	 * or {@link java.io.BufferedOutputStream BufferedOutputStream} is inserted; the caller
	 * is expected to do that if maximum performance is desired.</p>
	 *
	 * <p>Also, neither the input nor the output streams are explicitly closed after the
	 * copying has complete; the caller is expected to do that as well, since there may
	 * be occasions when there is more to be written to the output, or the input is to
	 * be rewound and reused, or whatever.</p>
	 *
	 * @param	in				the source
	 * @param	out				the destination
	 * @param	count			the number of bytes to copy
	 * @throws	IOException	thrown if the copying fails for any reason
	 */
	public static final void copy(InputStream in,OutputStream out,long count) throws IOException {
		copy(defaultReadBufferSize,in,out,count);
	}

	/**
	 * <p>Copy an entire input stream to an output stream.</p>
	 *
	 * <p>The data is copied in chunks rather than as individual bytes, but the input and output
	 * streams are used as is, and no {@link java.io.BufferedInputStream BufferedInputStream}
	 * or {@link java.io.BufferedOutputStream BufferedOutputStream} is inserted; the caller
	 * is expected to do that if maximum performance is desired.</p>
	 *
	 * <p>Also, neither the input nor the output streams are explicitly closed after the
	 * copying has complete; the caller is expected to do that as well, since there may
	 * be occasions when there is more to be written to the output, or the input is to
	 * be rewound and reused, or whatever.</p>
	 *
	 * @param	readBufferSize	how much data to read in each request
	 * @param	in				the source
	 * @param	out				the destination
	 * @throws	IOException	thrown if the copying fails for any reason
	 */
	public static final void copy(int readBufferSize,InputStream in,OutputStream out) throws IOException {
		if (readBufferSize == 0) {
			readBufferSize = defaultReadBufferSize;
		}
		byte[] readBuffer = new byte[readBufferSize];
		while (true) {
			int got = in.read(readBuffer);
			if (got > 0) {
				out.write(readBuffer,0,got);
			}
			else {
				break;
			}
		}
		out.flush();
	}

	/**
	 * <p>Copy an entire input stream to an output stream.</p>
	 *
	 * <p>The data is copied in chunks rather than as individual bytes, but the input and output
	 * streams are used as is, and no {@link java.io.BufferedInputStream BufferedInputStream}
	 * or {@link java.io.BufferedOutputStream BufferedOutputStream} is inserted; the caller
	 * is expected to do that if maximum performance is desired.</p>
	 *
	 * <p>Also, neither the input nor the output streams are explicitly closed after the
	 * copying has complete; the caller is expected to do that as well, since there may
	 * be occasions when there is more to be written to the output, or the input is to
	 * be rewound and reused, or whatever.</p>
	 *
	 * @param	in		the source
	 * @param	out		the destination
	 * @throws	IOException	thrown if the copying fails for any reason
	 */
	public static final void copy(InputStream in,OutputStream out) throws IOException {
		copy(defaultReadBufferSize,in,out);
	}

	/**
	 * <p>Copy an entire input file to an output file.</p>
	 *
	 * @param	inFile									the source
	 * @param	outFile									the destination
	 * @param	readBufferSize							how much data to read in each request
	 * @param	bufferedInputStreamSizeForFileCopy		the buffered input stream size (or zero if unbuffered)
	 * @param	bufferedOutputStreamSizeForFileCopy		and the buffered output stream size (or zero if unbuffered)
	 * @param	debugLevel
	 * @throws	IOException	thrown if the copying fails for any reason
	 */
	public static final void copy(File inFile,File outFile,int readBufferSize,int bufferedInputStreamSizeForFileCopy,int bufferedOutputStreamSizeForFileCopy,int debugLevel) throws IOException {
if (debugLevel > 0) System.err.println("Using readBufferSize of "+readBufferSize+" bytes");
		boolean useBufferedInputStream = bufferedInputStreamSizeForFileCopy > 0;
		boolean useBufferedOutputStream = bufferedOutputStreamSizeForFileCopy > 0;
if (debugLevel > 0) System.err.println((useBufferedInputStream ? "U" : "Not u")+"sing BufferedInputStream"+(useBufferedInputStream ? (" with size of " + bufferedInputStreamSizeForFileCopy + " bytes") : ""));
		InputStream in = useBufferedInputStream
						? new BufferedInputStream(new FileInputStream(inFile),bufferedInputStreamSizeForFileCopy)
						: new FileInputStream(inFile);
if (debugLevel > 0) System.err.println((useBufferedOutputStream ? "U" : "Not u")+"sing BufferedOutputStream"+(useBufferedOutputStream ? (" with size of " + bufferedOutputStreamSizeForFileCopy + " bytes") : ""));
		OutputStream out = useBufferedOutputStream
						? new BufferedOutputStream(new FileOutputStream(outFile),bufferedOutputStreamSizeForFileCopy)
						: new FileOutputStream(outFile);
		copy(readBufferSize,in,out);
		in.close();
		out.close();
	}


	/**
	 * <p>Copy an entire input file to an output file.</p>
	 *
	 * @param	inFile		the source
	 * @param	outFile		the destination
	 * @throws	IOException	thrown if the copying fails for any reason
	 */
	public static final void copy(File inFile,File outFile) throws IOException {
		copy(inFile,outFile,defaultReadBufferSize,defaultBufferedInputStreamSizeForFileCopy,defaultBufferedOutputStreamSizeForFileCopy,0/*debugLevel*/);
	}

	/**
	 * <p>Copy an entire input file to an output file.</p>
	 *
	 * @param	inFile		the source
	 * @param	outFile		the destination
	 * @throws	IOException	thrown if the copying fails for any reason
	 */
	public static final void copy(String inFile,String outFile) throws IOException {
		copy(new File(inFile),new File(outFile),defaultReadBufferSize,defaultBufferedInputStreamSizeForFileCopy,defaultBufferedOutputStreamSizeForFileCopy,0/*debugLevel*/);
	}

}




