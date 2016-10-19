/* Copyright (c) 2006-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import com.pixelmed.dicom.*;

import java.awt.*;
import java.awt.geom.RectangularShape;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>A class of utility methods for editing image pixel data.</p>
 *
 * @author	dclunie
 */

public class ImageEditUtilities {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/ImageEditUtilities.java,v 1.19 2014/11/26 14:50:49 dclunie Exp $";
	
	private ImageEditUtilities() {}
	
//	static protected boolean isInShapes(Vector shapes,int x,int y) {
//		Iterator it = shapes.iterator();
//		while (it.hasNext()) {
//			Shape shape = (Shape)it.next();
//			if (shape.contains(x,y)) {
//System.err.println("ImageEditUtilities.isInShapes(): found ("+x+","+y+")");
//				return true;
//			}
//		}
//		return false;
//	}
	
	/**
	 * <p>Blackout JPEG encoded blocks of specified regions in an image, for example to remove burned in identification.</p>
	 *
	 * <p>Other JPEG blocks remain untouched, i.e., to avoid loss involved in decompression and recompression of blocks that do not intersect with the specified regions.</p>
	 *
	 * <p>Overlays are not burned in.</p>
	 *
	 * <p>The replacement pixel value is not controllable</p>
	 *
	 * <p>The accompanying attribute list will be updated with new Pixel Data and other Image Pixel Module attributes will be unchanged.</p>
	 *
	 * @param	srcFile	the DICOM file containing the JPEG compressed image to be blacked out
	 * @param	dstFile	the DICOM file containing the JPEG compressed image with the blocks intersecting the specified regions blacked out
	 * @param	shapes	a {@link java.util.Vector java.util.Vector} of {@link java.awt.Shape java.awt.Shape}, specifed in image-relative coordinates
	 * @throws	DicomException	if something bad happens handling the attribute list
	 * @throws	IOException	if something bad happens reading or writing the files
	 * @throws	Exception	if something bad happens during processing of the JPEG blocks
	 */
	static public void blackoutJPEGBlocks(File srcFile,File dstFile,Vector shapes) throws DicomException, IOException, Exception {
		DicomInputStream i = new DicomInputStream(srcFile);
		AttributeList list = new AttributeList();
		list.setDecompressPixelData(false);
		list.read(i);
		String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.TransferSyntaxUID);
		if (!transferSyntaxUID.equals(TransferSyntax.JPEGBaseline)) {
			throw new DicomException("ImageEditUtilties.blackoutJPEGBlocks() can only be applied to DICOM files in JPEG Baseline TransferSyntax");
		}
		i.close();
					
		Attribute aPixelData = list.getPixelData();
		if (aPixelData != null) {
			if (shapes != null && shapes.size() > 0) {
				if (aPixelData instanceof OtherByteAttributeMultipleCompressedFrames) {
					// Pixel Data was not decompressed so can redact it without loss outside the redacted regions
					byte[][] frames = ((OtherByteAttributeMultipleCompressedFrames)aPixelData).getFrames();
					for (int f=0; f<frames.length; ++f) {
						ByteArrayInputStream fbis = new ByteArrayInputStream(frames[f]);
						ByteArrayOutputStream fbos = new ByteArrayOutputStream();
						com.pixelmed.codec.jpeg.Parse.parse(fbis,fbos,shapes);
						frames[f] = fbos.toByteArray();		// hmmm :(
					}
				}
				else {
					throw new DicomException("Unable to obtain compressed JPEG bit stream");
				}
			}
			else {
				throw new DicomException("No redaction shapes specified");
			}
		}
		else {
			throw new DicomException("Not an image");
		}
		
		list.removeGroupLengthAttributes();
		String aeTitle = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SourceApplicationEntityTitle);
		list.removeMetaInformationHeaderAttributes();
		FileMetaInformation.addFileMetaInformation(list,transferSyntaxUID,aeTitle);
		list.write(dstFile,transferSyntaxUID,true/*useMeta*/,true/*useBufferedStream*/);
	}
	
	/**
	 * <p>Blackout specified regions in an image, for example to remove burned in identification.</p>
	 *
	 * <p>Overlays are not burned in.</p>
	 *
	 * <p>The replacement pixel value is the smallest possible pixel value based on signedness and bit depth.</p>
	 *
	 * <p>The accompanying attribute list will be updated with new Pixel Data and related Image Pixel Module attributes.</p>
	 *
	 * <p>Note that original PhotometricInterpretation will be retained; care should be taken by the caller
	 * to change this as appropriate, e.g., from YBR_FULL_422 if read as JPEG to RGB if written as uncompressed.
	 * See, for example, {@link com.pixelmed.dicom.AttributeList#correctDecompressedImagePixelModule() AttributeList.correctDecompressedImagePixelModule()}.</p>
	 *
	 * @param	srcImg	the image
	 * @param	list	the attribute list corresponding image 
	 * @param	shapes	a {@link java.util.Vector java.util.Vector} of {@link java.awt.Shape java.awt.Shape}, specifed in image-relative coordinates
	 * @throws	DicomException	if something bad happens handling the attribute list
	 */
	static public void blackout(SourceImage srcImg,AttributeList list,Vector shapes) throws DicomException {
		blackout(srcImg,list,shapes,false,false,false,0);
	}
	
	/**
	 * <p>Blackout specified regions in an image, for example to remove burned in identification.</p>
	 *
	 * <p>Overlays may be burned in (and their corresponding attribues removed from the AttributeList).</p>
	 *
	 * <p>The replacement pixel value is the smallest possible pixel value based on signedness and bit depth.</p>
	 *
	 * <p>The accompanying attribute list will be updated with new Pixel Data and related Image Pixel Module attributes.</p>
	 *
	 * <p>Note that original PhotometricInterpretation will be retained; care should be taken by the caller
	 * to change this as appropriate, e.g., from YBR_FULL_422 if read as JPEG to RGB if written as uncompressed.
	 * See, for example, {@link com.pixelmed.dicom.AttributeList#correctDecompressedImagePixelModule() AttributeList.correctDecompressedImagePixelModule()}.</p>
	 *
	 * @param	srcImg			the image
	 * @param	list			the attribute list corresponding image 
	 * @param	shapes			a {@link java.util.Vector java.util.Vector} of {@link java.awt.Shape java.awt.Shape}, specifed in image-relative coordinates
	 * @param	burnInOverlays	whether or not to burn in overlays
	 * @throws	DicomException	if something bad happens handling the attribute list
	 */
	static public void blackout(SourceImage srcImg,AttributeList list,Vector shapes,boolean burnInOverlays) throws DicomException {
		blackout(srcImg,list,shapes,burnInOverlays,false,false,0);
	}
	
	/**
	 * <p>Blackout specified regions in an image, for example to remove burned in identification.</p>
	 *
	 * <p>Overlays may be burned in (and their corresponding attribues removed from the AttributeList).</p>
	 *
	 * <p>The replacement pixel value may be constrained to a specific value (typically zero), rather than the
	 * using the pixel padding value, if present, or the default, which is the smallest possible pixel value based on signedness and bit depth.</p>
	 *
	 * <p>The accompanying attribute list will be updated with new Pixel Data and related Image Pixel Module attributes.</p>
	 *
	 * <p>Note that original PhotometricInterpretation will be retained; care should be taken by the caller
	 * to change this as appropriate, e.g., from YBR_FULL_422 if read as JPEG to RGB if written as uncompressed.
	 * See, for example, {@link com.pixelmed.dicom.AttributeList#correctDecompressedImagePixelModule() AttributeList.correctDecompressedImagePixelModule()}.</p>
	 *
	 * @param	srcImg			the image
	 * @param	list			the attribute list corresponding image 
	 * @param	shapes			a {@link java.util.Vector java.util.Vector} of {@link java.awt.Shape java.awt.Shape}, specifed in image-relative coordinates
	 * @param	burnInOverlays	whether or not to burn in overlays
	 * @param	usePixelPaddingValue	whether or not to use any pixel paddding value
	 * @param	useSpecifiedBlackoutValue	whether or not to use the specifiedBlackoutValue or the default based on signedness and bit depth (overrides usePixelPaddingValue)
	 * @param	specifiedBlackoutValue	the value used to replace blacked out pixel values, only used if useSpecifiedBlackoutValue is true
	 * @throws	DicomException	if something bad happens handling the attribute list
	 */
	static public void blackout(SourceImage srcImg,AttributeList list,Vector shapes,boolean burnInOverlays,boolean usePixelPaddingValue,boolean useSpecifiedBlackoutValue,int specifiedBlackoutValue) throws DicomException {
//System.err.println("ImageEditUtilities.blackout():");
//System.err.println("ImageEditUtilities.blackout(): burnInOverlays = "+burnInOverlays);
//System.err.println("ImageEditUtilities.blackout(): Integer.MAX_VALUE "+Integer.MAX_VALUE);

//long elapsedDrawingTime = 0;
//long elapsedCopyingTime = 0;
//long elapsedReconstructionTime = 0;

		int                bitsAllocated = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsAllocated,0);
		int                   bitsStored = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsStored,0);
		int                      highBit = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.HighBit,bitsStored - 1);
		int              samplesPerPixel = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,1);
		int          pixelRepresentation = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.PixelRepresentation,0);
		String photometricInterpretation = Attribute.getSingleStringValueOrNull(list,TagFromName.PhotometricInterpretation);
		int          planarConfiguration = 0;	// 0 is color-by-pixel, 1 is color-by-plane

		int rows = 0;
		int columns = 0;

		byte   byteDstPixels[] = null;
		short shortDstPixels[] = null;
		Attribute pixelData = null;
		
		Overlay overlay  = srcImg.getOverlay();
		boolean inverted = srcImg.isInverted();
		boolean signed   = srcImg.isSigned();
		int mask         = srcImg.getMaskValue();
		boolean ybr      = srcImg.isYBR();
	
		int dstOffsetToStartOfCurrentFrame=0;
		int numberOfFrames = srcImg.getNumberOfBufferedImages();
		boolean needToCopyEachFrame = true;
		for (int frame=0; frame<numberOfFrames; ++frame) {
			BufferedImage src = srcImg.getBufferedImage(frame);
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"]");
//BufferedImageUtilities.describeImage(src,System.err);
			columns = src.getWidth();
//System.err.println("ImageEditUtilities.blackout(): columns = "+columns);
			rows = src.getHeight();
//System.err.println("ImageEditUtilities.blackout(): rows = "+rows);
			SampleModel srcSampleModel = src.getSampleModel();
//System.err.println("ImageEditUtilities.blackout(): srcSampleModel = "+srcSampleModel);
			int srcDataType = srcSampleModel.getDataType();
//System.err.println("ImageEditUtilities.blackout(): srcDataType = "+srcDataType);
			Raster srcRaster = src.getRaster();
			DataBuffer srcDataBuffer = srcRaster.getDataBuffer();
			int srcDataBufferType = srcDataBuffer.getDataType();
//System.err.println("ImageEditUtilities.blackout(): srcDataBufferType = "+srcDataBufferType);
			int srcNumBands = srcRaster.getNumBands();
//System.err.println("ImageEditUtilities.blackout(): srcNumBands = "+srcNumBands);
			int srcPixelStride = srcNumBands;
			int srcScanlineStride = columns*srcNumBands;
			if (srcNumBands > 1 && srcSampleModel instanceof ComponentSampleModel) {
				ComponentSampleModel srcComponentSampleModel = (ComponentSampleModel)srcSampleModel;
				srcPixelStride = srcComponentSampleModel.getPixelStride();			// should be either srcNumBands if color-by-pixel, or 1 if color-by-plane
				srcScanlineStride = srcComponentSampleModel.getScanlineStride();	// should be either columns*srcNumBands if color-by-pixel, or columns if color-by-plane
				planarConfiguration = srcPixelStride == srcNumBands ? 0 : 1;
			}
//System.err.println("ImageEditUtilities.blackout(): srcPixelStride = "+srcPixelStride);
//System.err.println("ImageEditUtilities.blackout(): srcScanlineStride = "+srcScanlineStride);
//System.err.println("ImageEditUtilities.blackout(): planarConfiguration = "+planarConfiguration);
			int srcDataBufferOffset = srcDataBuffer.getOffset();
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] srcDataBufferOffset = "+srcDataBufferOffset);
			int srcFrameLength = rows*columns*srcNumBands;
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] srcFrameLength = "+srcFrameLength);
			int srcDataBufferNumBanks = srcDataBuffer.getNumBanks();
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] srcDataBufferNumBanks = "+srcDataBufferNumBanks);
			
			if (srcDataBufferNumBanks > 1) {
				throw new DicomException("Unsupported type of image - DataBuffer number of banks is > 1, is "+srcDataBufferNumBanks);
			}

			int dstPixelStride = planarConfiguration == 0 ? srcNumBands : 1;
			int dstBandStride  = planarConfiguration == 0 ? 1 : rows*columns;
//System.err.println("ImageEditUtilities.blackout(): dstPixelStride = "+dstPixelStride);
//System.err.println("ImageEditUtilities.blackout(): dstBandStride = "+dstBandStride);

			if (srcDataBufferType == DataBuffer.TYPE_BYTE) {

				int backgroundValueBasis = (int)(
					useSpecifiedBlackoutValue
					? specifiedBlackoutValue
					:	(usePixelPaddingValue && srcImg.isPadded()
						? srcImg.getPadValue()
						:	(inverted
							? (signed ? (mask>>1) : mask)			// largest value (will always be +ve)
							: (signed ? (((mask>>1)+1)) : 0)		// smallest value (will be -ve if signed, but do NOT need to extend into integer, since only used to set value in PixelData array)
							)
						)
					) & 0xff;
//System.err.println("ImageEditUtilities.blackout(): backgroundValueBasis = "+backgroundValueBasis);

				int foregroundValueBasis = (int)(inverted
					? (signed ? (((mask>>1)+1)) : 0)		// smallest value (will be -ve if signed, but do NOT need to extend into integer, since only used to set value in PixelData array)
					: (signed ? (mask>>1) : mask)			// largest value (will always be +ve)
					) & 0xff;
//System.err.println("ImageEditUtilities.blackout(): foregroundValueBasis = "+foregroundValueBasis);

				byte[] backgroundValue = new byte[srcNumBands];
				byte[] foregroundValue = new byte[srcNumBands];
				if (ybr && srcNumBands >=3) {
					backgroundValue[0] = (byte)( .2990*backgroundValueBasis + .5870*backgroundValueBasis +	.1140*backgroundValueBasis);
					backgroundValue[1] = (byte)(-.1687*backgroundValueBasis - .3313*backgroundValueBasis +	.5000*backgroundValueBasis + 128);
					backgroundValue[2] = (byte)( .5000*backgroundValueBasis - .4187*backgroundValueBasis -	.0813*backgroundValueBasis + 128);
					
					foregroundValue[0] = (byte)( .2990*foregroundValueBasis + .5870*foregroundValueBasis +	.1140*foregroundValueBasis);
					foregroundValue[1] = (byte)(-.1687*foregroundValueBasis - .3313*foregroundValueBasis +	.5000*foregroundValueBasis + 128);
					foregroundValue[2] = (byte)( .5000*foregroundValueBasis - .4187*foregroundValueBasis -	.0813*foregroundValueBasis + 128);

					for (int bandIndex=3; bandIndex<srcNumBands; ++bandIndex) {
						backgroundValue[bandIndex] = -1;
						foregroundValue[bandIndex] = -1;
					} 
				}
				else {
					for (int bandIndex=0; bandIndex<srcNumBands; ++bandIndex) {
						backgroundValue[bandIndex] = (byte)backgroundValueBasis;
						foregroundValue[bandIndex] = (byte)foregroundValueBasis;
					} 
				}
//for (int bandIndex=0; bandIndex<srcNumBands; ++bandIndex) { System.err.println("ImageEditUtilities.blackout(): backgroundValue["+bandIndex+"] = "+backgroundValue[bandIndex]+" foregroundValue["+bandIndex+"] = "+foregroundValue[bandIndex]); }

				byte[][] srcPixelBanks = null;
				if (srcDataBuffer instanceof DataBufferByte) {
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] DataBufferByte");
					srcPixelBanks = ((DataBufferByte)srcDataBuffer).getBankData();
				}
				else {
					throw new DicomException("Unsupported type of image - DataBuffer is TYPE_BYTE but not instance of DataBufferByte, is "+srcDataBuffer.getClass().getName());
				}
				int srcPixelBankLength = srcPixelBanks[0].length;
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] srcPixelBankLength = "+srcPixelBankLength);
				if (byteDstPixels == null) {
					if (bitsAllocated > 8) {
						bitsAllocated = 8;
					}
					if (bitsStored > 8) {
						bitsStored = 8;
					}
					if (highBit > 7) {
						highBit = 7;
					}
					samplesPerPixel=srcNumBands;
					// leave photometricInterpretation alone
					// leave planarConfiguration alone ... already determined from srcPixelStride if srcNumBands > 1
					int dstPixelsLength = srcFrameLength*numberOfFrames;
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] dstPixelsLength = "+dstPixelsLength);
					if (dstPixelsLength == srcPixelBankLength) {
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] optimizing by using entire multi-frame array rather than copying");
						// optimize for special case of entire multi-frame image data in single array, shared by multiple BufferedImages using srcDataBufferOffset for frames
						// assumes that offsets are in same order as frames
						byteDstPixels = srcPixelBanks[0];
						needToCopyEachFrame = false;		// rather than break, since still need to draw regions
					}
					else {
						byteDstPixels = new byte[dstPixelsLength];
					}
					pixelData = new OtherByteAttribute(TagFromName.PixelData);
					pixelData.setValues(byteDstPixels);
				}
//long startCopyingTime = System.currentTimeMillis();
				if (needToCopyEachFrame) {
					System.arraycopy(srcPixelBanks[0],srcDataBufferOffset,byteDstPixels,dstOffsetToStartOfCurrentFrame,srcFrameLength);
				}
//elapsedCopyingTime+=System.currentTimeMillis()-startCopyingTime;
//long startDrawingTime = System.currentTimeMillis();
				if (burnInOverlays && overlay != null && overlay.getNumberOfOverlays(frame) > 0) {
//System.err.println("ImageEditUtilities.blackout(): Drawing overlays for frame "+frame);
					for (int o=0; o<16; ++o) {
						BufferedImage overlayImage = overlay.getOverlayAsBinaryBufferedImage(frame,o);
						if (overlayImage != null) {
//System.err.println("ImageEditUtilities.blackout(): Drawing overlay number "+o);
							int rowOrigin = overlay.getRowOrigin(frame,o);
							int columnOrigin = overlay.getColumnOrigin(frame,o);
							// first "draw" "shadow" offset one pixel down and right
							for (int overlayRow = 0; overlayRow < overlayImage.getHeight(); ++overlayRow) {
								for (int overlayColumn = 0; overlayColumn < overlayImage.getWidth(); ++overlayColumn) {
									int value = overlayImage.getRGB(overlayColumn,overlayRow);
									if (value != 0) {
										int x = columnOrigin + overlayColumn + 1;
										int y = rowOrigin + overlayRow + 1;
										if (x < columns && y < rows) {
											int pixelIndexWithinFrame = y*columns + x;
//System.err.println("ImageEditUtilities.blackout(): Drawing overlay -  setting shadow overlay ("+overlayColumn+","+overlayRow+") at image ("+x+","+y+")");
											for (int bandIndex=0; bandIndex<srcNumBands; ++ bandIndex) {
												int sampleIndex = dstOffsetToStartOfCurrentFrame + pixelIndexWithinFrame*dstPixelStride+bandIndex*dstBandStride;
//System.err.println("ImageEditUtilities.blackout(): frame="+frame+" y="+y+" x="+x+" pixelIndexWithinFrame="+pixelIndexWithinFrame+" bandIndex="+bandIndex+" sampleIndex="+sampleIndex);
												byteDstPixels[sampleIndex] = backgroundValue[bandIndex];
											}
										}
									}
								}
							}
							// now "draw" "image"
							for (int overlayRow = 0; overlayRow < overlayImage.getHeight(); ++overlayRow) {
								for (int overlayColumn = 0; overlayColumn < overlayImage.getWidth(); ++overlayColumn) {
									int value = overlayImage.getRGB(overlayColumn,overlayRow);
									if (value != 0) {
										int x = columnOrigin + overlayColumn;
										int y = rowOrigin + overlayRow;
										if (x < columns && y < rows) {
											int pixelIndexWithinFrame = y*columns + x;
//System.err.println("ImageEditUtilities.blackout(): Drawing overlay -  setting foreground overlay ("+overlayColumn+","+overlayRow+") at image ("+x+","+y+")");
											for (int bandIndex=0; bandIndex<srcNumBands; ++ bandIndex) {
												int sampleIndex = dstOffsetToStartOfCurrentFrame + pixelIndexWithinFrame*dstPixelStride+bandIndex*dstBandStride;
//System.err.println("ImageEditUtilities.blackout(): frame="+frame+" y="+y+" x="+x+" pixelIndexWithinFrame="+pixelIndexWithinFrame+" bandIndex="+bandIndex+" sampleIndex="+sampleIndex);
												byteDstPixels[sampleIndex] = foregroundValue[bandIndex];
											}
										}
									}
								}
							}
						}
					}
				}
				// do shapes AFTER overlays, because want to blackout overlays "under" chapes
				if (shapes != null) {
					Iterator it = shapes.iterator();
					while (it.hasNext()) {
						Shape shape = (Shape)it.next();
						if (shape instanceof RectangularShape) {	// this includes Rectangle and Rectangle2D (but also some other things that would need special handling :( )
							RectangularShape rect = (RectangularShape)shape;
//System.err.println("ImageEditUtilities.blackout(): shape is RectangularShape "+rect);
							int startX = (int)rect.getX();
							int startY = (int)rect.getY();
							int stopX = (int)(startX + rect.getWidth());
							int stopY = (int)(startY + rect.getHeight());
							for (int y=startY; y<stopY; ++y) {
								int pixelIndexWithinFrame = y*columns + startX;
//System.err.println("ImageEditUtilities.blackout(): row "+y+" startX "+startX+" pixelIndexWithinFrame "+pixelIndexWithinFrame);
								for (int x=startX; x<stopX; ++x) {
//System.err.println("ImageEditUtilities.blackout(): before set - getRGB("+x+","+y+")=0x"+Integer.toHexString(srcImg.getBufferedImage(frame).getRGB(x,y)));
									for (int bandIndex=0; bandIndex<srcNumBands; ++ bandIndex) {
										int sampleIndex = dstOffsetToStartOfCurrentFrame + pixelIndexWithinFrame*dstPixelStride+bandIndex*dstBandStride;
//System.err.println("ImageEditUtilities.blackout(): frame="+frame+" y="+y+" x="+x+" pixelIndexWithinFrame="+pixelIndexWithinFrame+" bandIndex="+bandIndex+" sampleIndex="+sampleIndex);
										byteDstPixels[sampleIndex] = backgroundValue[bandIndex];
									}
									++pixelIndexWithinFrame;
//System.err.println("ImageEditUtilities.blackout(): after set - getRGB("+x+","+y+")=0x"+Integer.toHexString(srcImg.getBufferedImage(frame).getRGB(x,y)));
								}
							}
						}
					}
				}
//elapsedDrawingTime+=System.currentTimeMillis()-startDrawingTime;
				dstOffsetToStartOfCurrentFrame+=srcFrameLength;
			}
			else if (srcDataBufferType == DataBuffer.TYPE_USHORT || srcDataBufferType == DataBuffer.TYPE_SHORT) {

				short backgroundValue = (short)(
					useSpecifiedBlackoutValue
					? specifiedBlackoutValue
					:	(usePixelPaddingValue && srcImg.isPadded()
						? srcImg.getPadValue()
						:	(inverted
							? (signed ? (mask>>1) : mask)			// largest value (will always be +ve)
							: (signed ? (((mask>>1)+1)) : 0)		// smallest value (will be -ve if signed, but do NOT need to extend into integer, since only used tp set value in PixelData array)
							)
						)
					);
//System.err.println("ImageEditUtilities.blackout(): backgroundValue = "+backgroundValue);

				short foregroundValue = (short)(inverted
					? (signed ? (((mask>>1)+1)) : 0)		// smallest value (will be -ve if signed, but do NOT need to extend into integer, since only used tp set value in PixelData array)
					: (signed ? (mask>>1) : mask)			// largest value (will always be +ve)
					);
//System.err.println("ImageEditUtilities.blackout(): foregroundValue = "+foregroundValue);

				short[][] srcPixelBanks = null;
				if (srcDataBuffer instanceof DataBufferShort) {
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] DataBufferShort");
					srcPixelBanks = ((DataBufferShort)srcDataBuffer).getBankData();
				}
				else if (srcDataBuffer instanceof DataBufferUShort) {
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] DataBufferUShort");
					srcPixelBanks =  ((DataBufferUShort)srcDataBuffer).getBankData();
				}
				else {
					throw new DicomException("Unsupported type of image - DataBuffer is TYPE_USHORT or TYPE_SHORT but not instance of DataBufferShort, is "+srcDataBuffer.getClass().getName());
				}
				int srcPixelBankLength = srcPixelBanks[0].length;
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] srcPixelBankLength = "+srcPixelBankLength);
				if (shortDstPixels == null) {
					if (bitsAllocated > 16) {
						bitsAllocated = 16;
					}
					if (bitsStored > 16) {
						bitsStored = 16;
					}
					if (highBit > 15) {
						highBit = 15;
					}
					samplesPerPixel=srcNumBands;
					// leave photometricInterpretation alone
					// leave planarConfiguration alone ... already determined from srcPixelStride if srcNumBands > 1
					int dstPixelsLength = srcFrameLength*numberOfFrames;
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] dstPixelsLength = "+dstPixelsLength);
					if (dstPixelsLength == srcPixelBankLength) {
//System.err.println("ImageEditUtilities.blackout(): Frame ["+frame+"] optimizing by using entire multi-frame array rather than copying");
						// optimize for special case of entire multi-frame image data in single array, shared by multiple BufferedImages using srcDataBufferOffset for frames
						// assumes that offsets are in same order as frames
						shortDstPixels = srcPixelBanks[0];
						needToCopyEachFrame = false;		// rather than break, since still need to draw regions
					}
					else {
						shortDstPixels = new short[dstPixelsLength];
					}
					pixelData = new OtherWordAttribute(TagFromName.PixelData);
					pixelData.setValues(shortDstPixels);
				}
//long startCopyingTime = System.currentTimeMillis();
				if (needToCopyEachFrame) {
					System.arraycopy(srcPixelBanks[0],srcDataBufferOffset,shortDstPixels,dstOffsetToStartOfCurrentFrame,srcFrameLength);
				}
//elapsedCopyingTime+=System.currentTimeMillis()-startCopyingTime;
//long startDrawingTime = System.currentTimeMillis();
				if (burnInOverlays && overlay != null && overlay.getNumberOfOverlays(frame) > 0) {
//System.err.println("ImageEditUtilities.blackout(): Drawing overlays for frame "+frame);
					for (int o=0; o<16; ++o) {
						BufferedImage overlayImage = overlay.getOverlayAsBinaryBufferedImage(frame,o);
						if (overlayImage != null) {
//System.err.println("ImageEditUtilities.blackout(): Drawing overlay number "+o);
							int rowOrigin = overlay.getRowOrigin(frame,o);
							int columnOrigin = overlay.getColumnOrigin(frame,o);
							// first "draw" "shadow" offset one pixel down and right
							for (int overlayRow = 0; overlayRow < overlayImage.getHeight(); ++overlayRow) {
								for (int overlayColumn = 0; overlayColumn < overlayImage.getWidth(); ++overlayColumn) {
									int value = overlayImage.getRGB(overlayColumn,overlayRow);
									if (value != 0) {
										int x = columnOrigin + overlayColumn + 1;
										int y = rowOrigin + overlayRow + 1;
										if (x < columns && y < rows) {
											int pixelIndexWithinFrame = y*columns + x;
//System.err.println("ImageEditUtilities.blackout(): Drawing overlay -  setting shadow overlay ("+overlayColumn+","+overlayRow+") at image ("+x+","+y+")");
											for (int bandIndex=0; bandIndex<srcNumBands; ++ bandIndex) {
												int sampleIndex = dstOffsetToStartOfCurrentFrame + pixelIndexWithinFrame*dstPixelStride+bandIndex*dstBandStride;
//System.err.println("ImageEditUtilities.blackout(): frame="+frame+" y="+y+" x="+x+" pixelIndexWithinFrame="+pixelIndexWithinFrame+" bandIndex="+bandIndex+" sampleIndex="+sampleIndex);
												shortDstPixels[sampleIndex] = backgroundValue;
											}
										}
									}
								}
							}
							// now "draw" "image"
							for (int overlayRow = 0; overlayRow < overlayImage.getHeight(); ++overlayRow) {
								for (int overlayColumn = 0; overlayColumn < overlayImage.getWidth(); ++overlayColumn) {
									int value = overlayImage.getRGB(overlayColumn,overlayRow);
									if (value != 0) {
										int x = columnOrigin + overlayColumn;
										int y = rowOrigin + overlayRow;
										if (x < columns && y < rows) {
											int pixelIndexWithinFrame = y*columns + x;
//System.err.println("ImageEditUtilities.blackout(): Drawing overlay -  setting foreground overlay ("+overlayColumn+","+overlayRow+") at image ("+x+","+y+")");
											for (int bandIndex=0; bandIndex<srcNumBands; ++ bandIndex) {
												int sampleIndex = dstOffsetToStartOfCurrentFrame + pixelIndexWithinFrame*dstPixelStride+bandIndex*dstBandStride;
//System.err.println("ImageEditUtilities.blackout(): frame="+frame+" y="+y+" x="+x+" pixelIndexWithinFrame="+pixelIndexWithinFrame+" bandIndex="+bandIndex+" sampleIndex="+sampleIndex);
												shortDstPixels[sampleIndex] = foregroundValue;
											}
										}
									}
								}
							}
						}
					}
				}
				// do shapes AFTER overlays, because want to blackout overlays "under" chapes
				if (shapes != null) {
					Iterator it = shapes.iterator();
					while (it.hasNext()) {
						Shape shape = (Shape)it.next();
						if (shape instanceof RectangularShape) {	// this includes Rectangle and Rectangle2D (but also some other things that would need special handling :( )
							RectangularShape rect = (RectangularShape)shape;
//System.err.println("ImageEditUtilities.blackout(): shape is RectangularShape "+rect);
							int startX = (int)rect.getX();
							int startY = (int)rect.getY();
							int stopX = (int)(startX + rect.getWidth());
							int stopY = (int)(startY + rect.getHeight());
							for (int y=startY; y<stopY; ++y) {
								int pixelIndexWithinFrame = y*columns + startX;
								for (int x=startX; x<stopX; ++x) {
									for (int bandIndex=0; bandIndex<srcNumBands; ++ bandIndex) {
										int sampleIndex = dstOffsetToStartOfCurrentFrame + pixelIndexWithinFrame*dstPixelStride+bandIndex*dstBandStride;
//System.err.println("ImageEditUtilities.blackout(): frame="+frame+" y="+y+" x="+x+" pixelIndexWithinFrame="+pixelIndexWithinFrame+" bandIndex="+bandIndex+" sampleIndex="+sampleIndex);
										shortDstPixels[sampleIndex] = backgroundValue;
									}
									++pixelIndexWithinFrame;
								}
							}
						}
					}
				}
//elapsedDrawingTime+=System.currentTimeMillis()-startDrawingTime;
				dstOffsetToStartOfCurrentFrame+=srcFrameLength;
			}
			else {
				throw new DicomException("Unsupported pixel data form - DataBufferType = "+srcDataBufferType);
			}
		}

		list.remove(TagFromName.PixelData);
		list.remove(TagFromName.BitsAllocated);
		list.remove(TagFromName.BitsStored);
		list.remove(TagFromName.HighBit);
		list.remove(TagFromName.SamplesPerPixel);
		list.remove(TagFromName.PixelRepresentation);
		list.remove(TagFromName.PhotometricInterpretation);
		list.remove(TagFromName.PlanarConfiguration);
		boolean numberOfFramesWasPresentBefore = list.get(TagFromName.NumberOfFrames) != null;
//System.err.println("ImageEditUtilities.blackout(): numberOfFramesWasPresentBefore = "+numberOfFramesWasPresentBefore);
		list.remove(TagFromName.NumberOfFrames);
		
		if (burnInOverlays) {
//System.err.println("ImageEditUtilities.blackout(): removeOverlayAttributes");
			list.removeOverlayAttributes();
		}

		list.put(pixelData);
		{ Attribute a = new UnsignedShortAttribute(TagFromName.BitsAllocated); a.addValue(bitsAllocated); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.BitsStored); a.addValue(bitsStored); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.HighBit); a.addValue(highBit); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(rows); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(columns); list.put(a); }
		if (numberOfFrames > 1 || numberOfFramesWasPresentBefore) {
			Attribute a = new IntegerStringAttribute(TagFromName.NumberOfFrames); a.addValue(numberOfFrames); list.put(a);
		}
		{ Attribute a = new UnsignedShortAttribute(TagFromName.SamplesPerPixel); a.addValue(samplesPerPixel); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.PixelRepresentation); a.addValue(pixelRepresentation); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PhotometricInterpretation); a.addValue(photometricInterpretation); list.put(a); }
		if (samplesPerPixel > 1) {
			Attribute a = new UnsignedShortAttribute(TagFromName.PlanarConfiguration); a.addValue(planarConfiguration); list.put(a);
		}
		
//long startReconstructionTime = System.currentTimeMillis();
		srcImg.constructSourceImage(list);
//elapsedReconstructionTime+=System.currentTimeMillis()-startReconstructionTime;

//System.err.println("ImageEditUtilities.blackout(): elapsedDrawingTime = "+elapsedDrawingTime);
//System.err.println("ImageEditUtilities.blackout(): elapsedCopyingTime = "+elapsedCopyingTime);
//System.err.println("ImageEditUtilities.blackout(): elapsedReconstructionTime = "+elapsedReconstructionTime);
//System.err.println("ImageEditUtilities.blackout(): done");
	}

	public static final int getOffsetIntoMatrix(int fixedOffset,int row,int column,int height,int width,int rotation,boolean horizontal_flip) {
		int offset = 0;
		if (rotation == 0) {
			offset = fixedOffset + row                  * width  + (horizontal_flip ? (width - column - 1) : column);
		}
		else if (rotation == 90) {
			offset = fixedOffset + column               * height + (horizontal_flip ? row                  : (height - row - 1));
		}
		else if (rotation == 180) {
			offset = fixedOffset + (height - row - 1)   * width  + (horizontal_flip ? column               : (width - column - 1));
		}
		else if (rotation == 270) {
			offset = fixedOffset + (width - column - 1) * height + (horizontal_flip ? (height - row - 1)   : row);
		}
		return offset;
	}

	public static final int getOffsetIntoMatrix(int offset,int row,int column,int width) {
		return getOffsetIntoMatrix(offset,row,column,0/*height not needed*/,width,0,false/*no flip*/);
	}

	/**
	 * <p>Rotate an image in 90 degree increments, optionally followed by a horizontal flip.</p>
	 *
	 * <p>The accompanying attribute list will be updated with new Pixel Data and related Image Pixel Module attributes.</p>
	 *
	 * <p>Note that original PhotometricInterpretation will be retained; care should be taken by the caller
	 * to change this as appropriate, e.g., from YBR_FULL_422 if read as JPEG to RGB if written as uncompressed.
	 * See, for example, {@link com.pixelmed.dicom.AttributeList#correctDecompressedImagePixelModule() AttributeList.correctDecompressedImagePixelModule()}.</p>
	 *
	 * @param	srcImg				the image
	 * @param	list				the attribute list corresponding image 
	 * @param	rotation			multiple of 90 degrees 
	 * @param	horizontal_flip		whether or not to flip horizontally AFTER rotation 
	 * @throws	DicomException	if something bad happens handling the attribute list
	 */
	static public void rotateAndFlip(SourceImage srcImg,AttributeList list,int rotation,boolean horizontal_flip) throws DicomException {
//System.err.println("ImageEditUtilities.rotate(): requested rotation "+rotation);
		if (rotation % 90 != 0) {
			throw new DicomException("Rotation of "+rotation+" not supported");
		}
		while (rotation >= 360) {
			rotation-= 360;
		}
		while (rotation < 0) {
			rotation+= 360;
		}
//System.err.println("ImageEditUtilities.rotate(): actual rotation "+rotation);
//System.err.println("ImageEditUtilities.rotate(): horizontal_flip "+horizontal_flip);

		int                bitsAllocated = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsAllocated,0);
		int                   bitsStored = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsStored,0);
		int                      highBit = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.HighBit,bitsStored - 1);
		int              samplesPerPixel = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,1);
		int          pixelRepresentation = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.PixelRepresentation,0);
		String photometricInterpretation = Attribute.getSingleStringValueOrNull(list,TagFromName.PhotometricInterpretation);
		int          planarConfiguration = 0;	// 0 is color-by-pixel, 1 is color-by-plane

		int srcRows = 0;
		int srcColumns = 0;

		int dstRows = 0;
		int dstColumns = 0;

		byte   byteDstPixels[] = null;
		short shortDstPixels[] = null;
		Attribute pixelData = null;
	
		int dstOffsetToStartOfCurrentFrame=0;
		int numberOfFrames = srcImg.getNumberOfBufferedImages();
		for (int frame=0; frame<numberOfFrames; ++frame) {
			BufferedImage src = srcImg.getBufferedImage(frame);
			srcColumns = src.getWidth();
			srcRows = src.getHeight();
			
			dstColumns = rotation == 90 || rotation == 270 ? srcRows    : srcColumns;
			dstRows    = rotation == 90 || rotation == 270 ? srcColumns : srcRows;
			
			SampleModel srcSampleModel = src.getSampleModel();
			int srcDataType = srcSampleModel.getDataType();
			Raster srcRaster = src.getRaster();
			DataBuffer srcDataBuffer = srcRaster.getDataBuffer();
			int srcDataBufferType = srcDataBuffer.getDataType();
			int srcNumBands = srcRaster.getNumBands();
			int srcPixelStride = srcNumBands;
			int srcScanlineStride = srcColumns*srcNumBands;
			if (srcNumBands > 1 && srcSampleModel instanceof ComponentSampleModel) {
				ComponentSampleModel srcComponentSampleModel = (ComponentSampleModel)srcSampleModel;
				srcPixelStride = srcComponentSampleModel.getPixelStride();			// should be either srcNumBands if color-by-pixel, or 1 if color-by-plane
				srcScanlineStride = srcComponentSampleModel.getScanlineStride();	// should be either srcColumns*srcNumBands if color-by-pixel, or srcColumns if color-by-plane
				planarConfiguration = srcPixelStride == srcNumBands ? 0 : 1;
			}
			int srcDataBufferOffset = srcDataBuffer.getOffset();
			int srcFrameLength = srcRows*srcColumns*srcNumBands;
			int srcDataBufferNumBanks = srcDataBuffer.getNumBanks();
			
			if (srcDataBufferNumBanks > 1) {
				throw new DicomException("Unsupported type of image - DataBuffer number of banks is > 1, is "+srcDataBufferNumBanks);
			}

//System.err.println("ImageEditUtilities.rotateAndFlip(): srcPixelStride = "+srcPixelStride);
			int srcBandStride  = planarConfiguration == 0 ? 1 : srcRows*srcColumns;
//System.err.println("ImageEditUtilities.rotateAndFlip(): srcBandStride = "+srcBandStride);

			int dstPixelStride = planarConfiguration == 0 ? srcNumBands : 1;
//System.err.println("ImageEditUtilities.rotateAndFlip(): dstPixelStride = "+dstPixelStride);
			int dstBandStride  = planarConfiguration == 0 ? 1 : srcRows*srcColumns;
//System.err.println("ImageEditUtilities.rotateAndFlip(): dstBandStride = "+dstBandStride);

			if (srcDataBufferType == DataBuffer.TYPE_BYTE) {
				byte[][] srcPixelBanks = null;
				if (srcDataBuffer instanceof DataBufferByte) {
					srcPixelBanks = ((DataBufferByte)srcDataBuffer).getBankData();
				}
				else {
					throw new DicomException("Unsupported type of image - DataBuffer is TYPE_BYTE but not instance of DataBufferByte, is "+srcDataBuffer.getClass().getName());
				}
				int srcPixelBankLength = srcPixelBanks[0].length;
				if (byteDstPixels == null) {
					if (bitsAllocated > 8) {
						bitsAllocated = 8;
					}
					if (bitsStored > 8) {
						bitsStored = 8;
					}
					if (highBit > 7) {
						highBit = 7;
					}
					samplesPerPixel=srcNumBands;
					// leave photometricInterpretation alone
					// leave planarConfiguration alone ... already determined from srcPixelStride if srcNumBands > 1
					int dstPixelsLength = srcFrameLength*numberOfFrames;
					byteDstPixels = new byte[dstPixelsLength];
					pixelData = new OtherByteAttribute(TagFromName.PixelData);
					pixelData.setValues(byteDstPixels);
				}
				{
					for (int srcRow = 0; srcRow<srcRows; ++srcRow) {
						for (int srcColumn = 0; srcColumn<srcColumns; ++srcColumn) {
							int srcOffset = getOffsetIntoMatrix(0,srcRow,srcColumn,srcColumns);
							int dstOffset = getOffsetIntoMatrix(0,srcRow,srcColumn,srcRows,srcColumns,rotation,horizontal_flip);
							for (int bandIndex=0; bandIndex<srcNumBands; ++ bandIndex) {
								byteDstPixels[dstOffsetToStartOfCurrentFrame + dstOffset*dstPixelStride+bandIndex*dstBandStride] = srcPixelBanks[0][srcDataBufferOffset + srcOffset*srcPixelStride + bandIndex*srcBandStride];
							}
						}
					}
					dstOffsetToStartOfCurrentFrame+=srcFrameLength;
				}
			}
			else if (srcDataBufferType == DataBuffer.TYPE_USHORT || srcDataBufferType == DataBuffer.TYPE_SHORT) {
				short[][] srcPixelBanks = null;
				if (srcDataBuffer instanceof DataBufferShort) {
					srcPixelBanks = ((DataBufferShort)srcDataBuffer).getBankData();
				}
				else if (srcDataBuffer instanceof DataBufferUShort) {
					srcPixelBanks =  ((DataBufferUShort)srcDataBuffer).getBankData();
				}
				else {
					throw new DicomException("Unsupported type of image - DataBuffer is TYPE_USHORT or TYPE_SHORT but not instance of DataBufferShort, is "+srcDataBuffer.getClass().getName());
				}
				int srcPixelBankLength = srcPixelBanks[0].length;
				if (shortDstPixels == null) {
					if (bitsAllocated > 16) {
						bitsAllocated = 16;
					}
					if (bitsStored > 16) {
						bitsStored = 16;
					}
					if (highBit > 15) {
						highBit = 15;
					}
					samplesPerPixel=srcNumBands;
					// leave photometricInterpretation alone
					// leave planarConfiguration alone ... already determined from srcPixelStride if srcNumBands > 1
					int dstPixelsLength = srcFrameLength*numberOfFrames;
					shortDstPixels = new short[dstPixelsLength];
					pixelData = new OtherWordAttribute(TagFromName.PixelData);
					pixelData.setValues(shortDstPixels);
				}
				{
					for (int srcRow = 0; srcRow<srcRows; ++srcRow) {
						for (int srcColumn = 0; srcColumn<srcColumns; ++srcColumn) {
							int srcOffset = getOffsetIntoMatrix(0,srcRow,srcColumn,srcColumns);
							int dstOffset = getOffsetIntoMatrix(0,srcRow,srcColumn,srcRows,srcColumns,rotation,horizontal_flip);
							for (int bandIndex=0; bandIndex<srcNumBands; ++ bandIndex) {
								shortDstPixels[dstOffsetToStartOfCurrentFrame + dstOffset*dstPixelStride+bandIndex*dstBandStride] = srcPixelBanks[0][srcDataBufferOffset + srcOffset*srcPixelStride + bandIndex*srcBandStride];
							}
						}
					}
					dstOffsetToStartOfCurrentFrame+=srcFrameLength;
				}
			}
			else {
				throw new DicomException("Unsupported pixel data form - DataBufferType = "+srcDataBufferType);
			}
		}

		list.remove(TagFromName.PixelData);
		list.remove(TagFromName.BitsAllocated);
		list.remove(TagFromName.BitsStored);
		list.remove(TagFromName.HighBit);
		list.remove(TagFromName.SamplesPerPixel);
		list.remove(TagFromName.PixelRepresentation);
		list.remove(TagFromName.PhotometricInterpretation);
		list.remove(TagFromName.PlanarConfiguration);
		boolean numberOfFramesWasPresentBefore = list.get(TagFromName.NumberOfFrames) != null;
		list.remove(TagFromName.NumberOfFrames);

		list.put(pixelData);
		{ Attribute a = new UnsignedShortAttribute(TagFromName.BitsAllocated); a.addValue(bitsAllocated); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.BitsStored); a.addValue(bitsStored); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.HighBit); a.addValue(highBit); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(dstRows); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(dstColumns); list.put(a); }
		if (numberOfFrames > 1 || numberOfFramesWasPresentBefore) {
			Attribute a = new IntegerStringAttribute(TagFromName.NumberOfFrames); a.addValue(numberOfFrames); list.put(a);
		}
		{ Attribute a = new UnsignedShortAttribute(TagFromName.SamplesPerPixel); a.addValue(samplesPerPixel); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.PixelRepresentation); a.addValue(pixelRepresentation); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PhotometricInterpretation); a.addValue(photometricInterpretation); list.put(a); }
		if (samplesPerPixel > 1) {
			Attribute a = new UnsignedShortAttribute(TagFromName.PlanarConfiguration); a.addValue(planarConfiguration); list.put(a);
		}
		
		srcImg.constructSourceImage(list);
	}

}
