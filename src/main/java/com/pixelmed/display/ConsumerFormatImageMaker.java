/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.WritableRaster;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DecimalStringAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.GeometryOfVolumeFromAttributeList;
import com.pixelmed.dicom.ModalityTransform;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.VOITransform;

import com.pixelmed.geometry.GeometryOfVolume;

import com.pixelmed.utils.ColorUtilities;

/**
 * <p>A class of static methods to make consumer format images from DICOM images.</p>
 *
 * @author	dclunie
 */
public class ConsumerFormatImageMaker {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/ConsumerFormatImageMaker.java,v 1.31 2014/12/17 15:09:03 dclunie Exp $";
	
	public static final String ALL_ANNOTATIONS = "all";
	public static final String ICON_ANNOTATIONS = "icon";
	public static final String COLOR_ANNOTATIONS = "color";
	public static final String NO_ANNOTATIONS = "none";
	
	/**
	 * <p>Create a single frame 8-bit per channel image (windowed if grayscale) from the first, or only, frame.</p>
	 *
	 * <p>Uses the window center and width in the attribute list.</p>
	 *
	 * @param	list				the DICOM attributes
	 * @param	debugLevel	
	 * @return						an 8 bit BufferedImage
	 * @throws	DicomException	if attribute is not an image
	 */
	public static final BufferedImage makeEightBitImage(AttributeList list,int debugLevel) throws DicomException {
		return makeEightBitImages(list,debugLevel)[0];
	}
	
	/**
	 * <p>Create an array of single frame 8-bit per channel image (windowed if grayscale) from the from the only or all frames.</p>
	 *
	 * <p>Uses the window center and width in the attribute list.</p>
	 *
	 * @param	list		the DICOM attributes
	 * @param	debugLevel	
	 * @return				an array of 8 bit BufferedImages
	 * @throws	DicomException	if attribute is not an image
	 */
	public static final BufferedImage[] makeEightBitImages(AttributeList list,int debugLevel) throws DicomException {
		return makeEightBitImages(list,(GeometryOfVolume)null,(Vector<SuperimposedImage>)null,(Vector<Shape>[])null,0,-1/*all frames*/,debugLevel);
	}
	
	
	/**
	 * <p>Create an array of single frame 8-bit per channel image (windowed if grayscale) from the from the only or all frames.</p>
	 *
	 * <p>Uses the window center and width in the attribute list.</p>
	 *
	 * @param	list							the DICOM attributes
	 * @param	imageGeometry					the geometry already extracted from the list, or null if need to extract it (only needed if have superimposedImages)
	 * @param	superimposedImages				images to to be displayed superimposed on the appropriate frames, or null if none
	 * @param	arrayOfPerFrameDrawingShapes	per-frame shapes to be displayed on the respective frame
	 * @param	firstFrame						the first frame to include, numbered from 0 (not 1)
	 * @param	lastFrame						the last frame to include, numbered from 0 (not 1), or -1 if all frames
	 * @param	debugLevel	
	 * @return									an array of 8 bit BufferedImages
	 * @throws	DicomException				if attribute is not an image
	 */
	public static final BufferedImage[] makeEightBitImages(AttributeList list,GeometryOfVolume imageGeometry,Vector<SuperimposedImage> superimposedImages,Vector<Shape>[] arrayOfPerFrameDrawingShapes,int firstFrame,int lastFrame,int debugLevel) throws DicomException {
		return makeEightBitImages(list,imageGeometry,superimposedImages,SuperimposedImage.DEFAULT_CLOSEST_SLICE_TOLERANCE_DISTANCE,arrayOfPerFrameDrawingShapes,firstFrame,lastFrame,debugLevel);
	}
	
	/**
	 * <p>Create an array of single frame 8-bit per channel image (windowed if grayscale) from the from the only or all frames.</p>
	 *
	 * <p>Uses the window center and width in the attribute list.</p>
	 *
	 * @param	list							the DICOM attributes
	 * @param	imageGeometry					the geometry already extracted from the list, or null if need to extract it (only needed if have superimposedImages)
	 * @param	superimposedImages				images to to be displayed superimposed on the appropriate frames, or null if none
	 * @param	superimposedToleranceDistance	difference in distance along normal to orientation for underlying and superimposed frames to be close enough to superimpose, in mm
	 * @param	arrayOfPerFrameDrawingShapes	per-frame shapes to be displayed on the respective frame
	 * @param	firstFrame						the first frame to include, numbered from 0 (not 1)
	 * @param	lastFrame						the last frame to include, numbered from 0 (not 1), or -1 if all frames
	 * @param	debugLevel	
	 * @return									an array of 8 bit BufferedImages
	 * @throws	DicomException				if attribute is not an image
	 */
	public static final BufferedImage[] makeEightBitImages(AttributeList list,GeometryOfVolume imageGeometry,Vector<SuperimposedImage> superimposedImages,double superimposedToleranceDistance,Vector<Shape>[] arrayOfPerFrameDrawingShapes,int firstFrame,int lastFrame,int debugLevel) throws DicomException {
		String sopClassUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPClassUID);
		if (!SOPClass.isImageStorage(sopClassUID)) {
			throw new DicomException("SOP Class is not an image");
		}
		
		SourceImage sImg = new SourceImage(list);
		int nFrames = sImg.getNumberOfFrames();
		if (lastFrame < 0) {
			firstFrame = 0;
			lastFrame = nFrames-1;
		}
		BufferedImage[] renderedImages = new BufferedImage[lastFrame-firstFrame+1];
		for (int f=firstFrame; f<=lastFrame; ++f) {
			BufferedImage useSrcImage = sImg.getBufferedImage(f);
			BufferedImage renderedImage = null;
				
			if (useSrcImage.getColorModel().getNumComponents() == 1) {
				ModalityTransform modalityTransform = sImg.getModalityTransform();
				VOITransform           voiTransform = sImg.getVOITransform();
				boolean                      signed = sImg.isSigned();
				boolean                    inverted = sImg.isInverted();
				boolean                      usePad = sImg.isPadded();
				int                             pad = sImg.getPadValue();
				double						 imgMin = sImg.getMinimum();
				double						 imgMax = sImg.getMaximum();
				int                     largestGray = sImg.getPaletteColorLargestGray();
				int                firstvalueMapped = sImg.getPaletteColorFirstValueMapped();
				int                 numberOfEntries = sImg.getPaletteColorNumberOfEntries();
				int                    bitsPerEntry = sImg.getPaletteColorBitsPerEntry();
				short                    redTable[] = sImg.getPaletteColorRedTable();
				short                  greenTable[] = sImg.getPaletteColorGreenTable();
				short                   blueTable[] = sImg.getPaletteColorBlueTable();

				double useSlope=1;
				double useIntercept=0;
				if (modalityTransform != null) {
				        useSlope = modalityTransform.getRescaleSlope    (f);
					useIntercept = modalityTransform.getRescaleIntercept(f);
				}

				double windowWidth=0;
				double windowCenter=0;
				if (voiTransform != null && voiTransform.getNumberOfTransforms(f) > 0) {
					 windowWidth = voiTransform.getWidth(f,0);								// (first) transform
					windowCenter = voiTransform.getCenter(f,0);
				}
				if (windowWidth <= 0) {			// use supplied window only if there was one, and if its width was not zero (center may legitimately be zero); indeed, it is forbidden to be -ve also
if (debugLevel > 2) System.err.println("For statistically derived window: imgMin = "+imgMin);
if (debugLevel > 2) System.err.println("For statistically derived window: imgMax = "+imgMax);
					double ourMin = imgMin*useSlope+useIntercept;
					double ourMax = imgMax*useSlope+useIntercept;
if (debugLevel > 2) System.err.println("For statistically derived window: rescaled min = "+ourMin);
if (debugLevel > 2) System.err.println("For statistically derived window: rescaled min = "+ourMax);
					windowWidth=(ourMax-ourMin);
					windowCenter=(ourMax+ourMin)/2.0;
if (debugLevel > 1) System.err.println("Using statistically derived center "+windowCenter+" and width "+windowWidth);
				}
				
if (debugLevel > 1) System.err.println("Using rescale slope "+useSlope+" and intercept "+useIntercept+" and window center "+windowCenter+" and width "+windowWidth);

				int useVOIFunction = 0;
			
				renderedImage = (numberOfEntries == 0 || redTable == null)
					? (useVOIFunction == 1
						? WindowCenterAndWidth.applyWindowCenterAndWidthLogistic(useSrcImage,windowCenter,windowWidth,signed,inverted,useSlope,useIntercept,usePad,pad)
						: WindowCenterAndWidth.applyWindowCenterAndWidthLinear(useSrcImage,windowCenter,windowWidth,signed,inverted,useSlope,useIntercept,usePad,pad)
						)
					: WindowCenterAndWidth.applyWindowCenterAndWidthWithPaletteColor(useSrcImage,windowCenter,windowWidth,sImg.isSigned(),inverted,useSlope,useIntercept,usePad,pad,
						largestGray,bitsPerEntry,numberOfEntries,redTable,greenTable,blueTable);
			}
			else if (useSrcImage.getColorModel().getNumComponents() == 3) {
				VOITransform voiTransform = sImg.getVOITransform();
				double windowWidth=0;
				double windowCenter=0;
				if (voiTransform != null && voiTransform.getNumberOfTransforms(f) > 0) {
					 windowWidth = voiTransform.getWidth(f,0);								// (first) transform
					windowCenter = voiTransform.getCenter(f,0);
				}
				if (windowWidth <= 0) {			// use supplied window only if there was one, and if its width was not zero (center may legitimately be zero); indeed, it is forbidden to be -ve also
if (debugLevel > 1) System.err.println("Color image without windowing");
					renderedImage=BufferedImageUtilities.convertToMostFavorableImageType(useSrcImage);
				}
				else {
if (debugLevel > 1) System.err.println("Color image with window center "+windowCenter+" and width "+windowWidth);
					// No rescaling for color images
					// use only linear voiTransform ... and no rescaling, no sign, no inversion (for now), no padding
					renderedImage = WindowCenterAndWidth.applyWindowCenterAndWidthLinearToColorImage(useSrcImage,windowCenter,windowWidth);
				}
			}
			else {
				try {
					renderedImage=BufferedImageUtilities.convertToMostFavorableImageType(useSrcImage);
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
					renderedImage=useSrcImage;
				}
			}

			// superimposedImages application is derived from same approach in SingleImagePanel ... ideally should refactor into utility class :(
			if (superimposedImages != null) {
				if (imageGeometry == null) {
					try {
						imageGeometry = new GeometryOfVolumeFromAttributeList(list);
					}
					catch (Exception e) {
						// don't print exception, because it is legitimate for images to be missing this information
						//e.printStackTrace(System.err);
					}
				}
				if (imageGeometry != null) {
					for (SuperimposedImage superimposedImage : superimposedImages) {
						SuperimposedImage.AppliedToUnderlyingImage superimposedImageAppliedToUnderlyingImage = superimposedImage.getAppliedToUnderlyingImage(imageGeometry,f,superimposedToleranceDistance);
						if (superimposedImageAppliedToUnderlyingImage != null) {
							BufferedImage superimposedBufferedImage = superimposedImageAppliedToUnderlyingImage.getBufferedImage();
							if (superimposedBufferedImage != null) {
if (debugLevel > 0) System.err.println("ConsumerFormatImageMaker.makeEightBitImages(): have superimposed image for underlying frame "+f);
								double rowOrigin = superimposedImageAppliedToUnderlyingImage.getRowOrigin();
								double columnOrigin = superimposedImageAppliedToUnderlyingImage.getColumnOrigin();
						
								java.awt.image.RescaleOp applyColor = null;
								if (superimposedBufferedImage.getType() == BufferedImage.TYPE_INT_ARGB) {
//System.err.println("ConsumerFormatImageMaker.makeEightBitImages(): have ARGB superimposed image so can change color and use transparency");
									// BufferedImageOp ... use this for transparency with ARGB BufferedImage and RescaleOp to mess with alpha channel (see "http://docs.oracle.com/javase/tutorial/2d/images/drawimage.html")
									int[] cieLabScaled = superimposedImage.getIntegerScaledCIELabPCS();
									float redScale = 1f;
									float greenScale = 1f;
									float blueScale = 0f;
									if (cieLabScaled != null) {
										int[] srgb = ColorUtilities.getSRGBFromIntegerScaledCIELabPCS(cieLabScaled);
										redScale = srgb[0] / 255f;
										greenScale = srgb[1] / 255f;
										blueScale = srgb[2] / 255f;
									}
									float[] scales = { redScale, greenScale, blueScale, 1f /* color and alpha */ };
									float[] offsets = new float[4];
									applyColor = new java.awt.image.RescaleOp(scales,offsets,null/*RenderingHints*/);
								}
								else {
System.err.println("ConsumerFormatImageMaker.makeEightBitImages(): not ARGB superimposed image so cannot change color and use transparency");
								}
								renderedImage=BufferedImageUtilities.convertToMostFavorableImageType(renderedImage);	// need to do this else will not draw in color
								Graphics2D g2d=(Graphics2D)(renderedImage.getGraphics());
								g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER,0.5f));	// set transparency
								g2d.drawImage(
									superimposedBufferedImage,
									applyColor,
									(int)columnOrigin,	// aaargh ! ... really need to indirect the original doubles through the affine transform in case upsampled for displayed, since usually are +0.5 sub-pixel resolution :(
									(int)rowOrigin
								);
							}
							else {
if (debugLevel > 0) System.err.println("ConsumerFormatImageMaker.makeEightBitImages(): have no superimposed image for underlying frame "+f);
							}
						}
					}
				}
			}
			if (arrayOfPerFrameDrawingShapes != null && arrayOfPerFrameDrawingShapes.length > f && arrayOfPerFrameDrawingShapes[f] != null) {
if (debugLevel > 0) System.err.println("ConsumerFormatImageMaker.makeEightBitImages(): draw per-frame shapes");
				renderedImage=BufferedImageUtilities.convertToMostFavorableImageType(renderedImage);	// need to do this else will not draw in color
				Graphics2D g2d=(Graphics2D)(renderedImage.getGraphics());
				Color perFrameDrawingColor = Color.red;
				g2d.setColor(perFrameDrawingColor);
				//int lineWidth = 2;	// in display (not source image) pixels
				//g2d.setStroke(new BasicStroke((float)(lineWidth/useScaleFactor)));

				Vector<Shape> shapes = arrayOfPerFrameDrawingShapes[f];
//com.pixelmed.geometry.LocalizerPosterFactory.dumpShapes(shapes);
				Iterator<Shape> i = shapes.iterator();
				while (i.hasNext()) {
					g2d.draw(i.next());
				}
			}

			renderedImages[f-firstFrame] = renderedImage;
		}
		return renderedImages;
	}
	
	/**
	 * <p>Read a DICOM image input file, and create single frame 8-bit per channel images (windowed if grayscale) from the only or all frames.</p>
	 *
	 * <p>If the input file has multiple frames, the output file name will be postfixed with the frame number before the format extension, e.g., "output.jpg" will become "output_001.jpg", etc.</p>
	 *
	 * <p>Uses the window center and width in the file.</p>
	 *
	 * @param	dicomFileName	the input file name
	 * @param	outputFileName	the output file name (or basis of names for multiple frames)
	 * @param	outputFormat	the output file format name that a JIIO SPI will recognize (e.g. "jpeg")
	 * @param	debugLevel
	 * @return					a String[] of the output filenames ordered by the frame order of the input image
	 */
	public static String[] convertFileToEightBitImage(String dicomFileName,String outputFileName,String outputFormat,int debugLevel) throws DicomException, IOException {
		 return convertFileToEightBitImage(dicomFileName,outputFileName,outputFormat,0,0,0,0,-1,ALL_ANNOTATIONS,debugLevel);
	}
	
	private static void replaceWindowWidthAndCenterInAttributeList(AttributeList list,Attribute aWindowWidth,Attribute aWindowCenter) {
		list.put(aWindowCenter);
		list.put(aWindowWidth);
	}
	
	private static void replaceWindowWidthAndCenterInFunctionalGroupSequences(AttributeList list,int nFrames,Attribute aWindowWidth,Attribute aWindowCenter) {
		{
			AttributeList sharedList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(list,TagFromName.SharedFunctionalGroupsSequence);
			if (sharedList != null) {
				AttributeList frameVOILUTList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(sharedList,TagFromName.FrameVOILUTSequence);
				if (frameVOILUTList != null) {
					replaceWindowWidthAndCenterInAttributeList(frameVOILUTList,aWindowWidth,aWindowCenter);
				}
			}
		}
		{
			Attribute aPerFrameFunctionalGroupsSequence = list.get(TagFromName.PerFrameFunctionalGroupsSequence);
			if (aPerFrameFunctionalGroupsSequence != null && aPerFrameFunctionalGroupsSequence instanceof SequenceAttribute) {
				for (int f=0; f<nFrames; ++f) {
					AttributeList frameList = SequenceAttribute.getAttributeListFromSelectedItemWithinSequence((SequenceAttribute)aPerFrameFunctionalGroupsSequence,f);
					if (frameList != null) {
						AttributeList frameVOILUTList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(frameList,TagFromName.FrameVOILUTSequence);
						if (frameVOILUTList != null) {
							replaceWindowWidthAndCenterInAttributeList(frameVOILUTList,aWindowWidth,aWindowCenter);
						}
					}
				}
			}
		}
	}
	
	/**
	 * <p>Read a DICOM image input file, and create single frame 8-bit per channel images (windowed if grayscale) from the only or all frames.</p>
	 *
	 * <p>If the input file has multiple frames, the output file name will be postfixed with the frame number before the format extension, e.g., "output.jpg" will become "output_001.jpg", etc.</p>
	 *
	 * @param	dicomFileName	the input file name
	 * @param	outputFileName	the output file name (or basis of names for multiple frames)
	 * @param	outputFormat	the output file format name that a JIIO SPI will recognize (e.g. "jpeg")
	 * @param	windowCenter	the window center to use
	 * @param	windowWidth		the window width to use, or 0 if to use the width and center in the DICOM file
	 * @param	imageWidth		the width (number of columns) to make, or &lt;= 0 if default (the width in the DICOM file, or scaled by height with pixel aspect ratio preserved)
	 * @param	imageHeight		the height (number of rows) to make, or &lt;= 0 if default (the height in the DICOM file, or scaled by width with pixel aspect ratio preserved)
	 * @param	imageQuality	the image quality from 1 to 100 (best), or -1 if absent
	 * @param	annotation		the type of annotation to apply (choice is "all"; anything else means no annotation), or null if absent
	 * @param	debugLevel	
	 * @return					a String[] of the output filenames ordered by the frame order of the input image
	 */
	public static String[] convertFileToEightBitImage(String dicomFileName,String outputFileName,String outputFormat,
			double windowCenter,double windowWidth,int imageWidth,int imageHeight,int imageQuality,String annotation,
			int debugLevel) throws DicomException, IOException {
		return convertFileToEightBitImage(dicomFileName,outputFileName,outputFormat,windowCenter,windowWidth,imageWidth,imageHeight,0,0,0,0,0,-1/*all frames*/,imageQuality,annotation,(Vector<SuperimposedImage>)null,SuperimposedImage.DEFAULT_CLOSEST_SLICE_TOLERANCE_DISTANCE,(Vector<Shape>[])null,debugLevel);
	}

	
	/**
	 * <p>Read a DICOM image input file, and create single frame 8-bit per channel images (windowed if grayscale) from the only or all frames.</p>
	 *
	 * <p>If the input file has multiple frames, the output file name will be postfixed with the frame number before the format extension, e.g., "output.jpg" will become "output_001.jpg", etc.</p>
	 *
	 * <p>The aspect ratio of the sub-region width and height, if any, should be the same as that
	 * of any explicitly specified image width and height, to avoid distortion.</p>
	 *
	 * @param	dicomFileName	the input file name
	 * @param	outputFileName	the output file name (or basis of names for multiple frames)
	 * @param	outputFormat	the output file format name that a JIIO SPI will recognize (e.g. "jpeg")
	 * @param	windowCenter	the window center to use
	 * @param	windowWidth		the window width to use, or 0 if to use the width and center in the DICOM file
	 * @param	imageWidth		the width (number of columns) to make, or &lt;= 0 if default (the width in the DICOM file, or scaled by height with pixel aspect ratio preserved)
	 * @param	imageHeight		the height (number of rows) to make, or &lt;= 0 if default (the height in the DICOM file, or scaled by width with pixel aspect ratio preserved)
	 * @param	regionX			the x (along row) integer offset (from 0 being the TLHC pixel) of the sub-region to write
	 * @param	regionY			the y (down column) integer offset (from 0 being the TLHC pixel) of the sub-region to write
	 * @param	regionWidth		the width (number of columns) to write, or &lt;= 0 if no sub-region selected (i.e., write the whole image)
	 * @param	regionHeight	the height (number of rows) to write, or &lt;= 0 if no sub-region selected (i.e., write the whole image)
	 * @param	firstFrame		the first frame to include, numbered from 0 (not 1)
	 * @param	lastFrame		the last frame to include, numbered from 0 (not 1), or -1 if all frames
	 * @param	imageQuality	the image quality from 1 to 100 (best), or -1 if absent
	 * @param	annotation		the type of annotation to apply (choice is "all"; anything else means no annotation), or null if absent
	 * @param	debugLevel	
	 * @return					a String[] of the output filenames ordered by the frame order of the input image
	 */
	public static String[] convertFileToEightBitImage(String dicomFileName,String outputFileName,String outputFormat,
			double windowCenter,double windowWidth,int imageWidth,int imageHeight,int regionX,int regionY,int regionWidth,int regionHeight,int firstFrame,int lastFrame,int imageQuality,String annotation,
			int debugLevel) throws DicomException, IOException {
		return convertFileToEightBitImage(dicomFileName,outputFileName,outputFormat,windowCenter,windowWidth,imageWidth,imageHeight,regionX,regionY,regionWidth,regionHeight,firstFrame,lastFrame,imageQuality,annotation,(Vector<SuperimposedImage>)null,SuperimposedImage.DEFAULT_CLOSEST_SLICE_TOLERANCE_DISTANCE,(Vector<Shape>[])null,debugLevel);
	}
							
	/**
	 * <p>Read a DICOM image input file, and create single frame 8-bit per channel images (windowed if grayscale) from the only or all frames.</p>
	 *
	 * <p>If the input file has multiple frames, the output file name will be postfixed with the frame number before the format extension, e.g., "output.jpg" will become "output_001.jpg", etc.</p>
	 *
	 * @param	dicomFileName					the input file name
	 * @param	outputFileName					the output file name (or basis of names for multiple frames)
	 * @param	outputFormat					the output file format name that a JIIO SPI will recognize (e.g. "jpeg")
	 * @param	windowCenter					the window center to use
	 * @param	windowWidth						the window width to use, or 0 if to use the width and center in the DICOM file
	 * @param	imageWidth						the width (number of columns) to make, or &lt;= 0 if default (the width in the DICOM file, or scaled by height with pixel aspect ratio preserved)
	 * @param	imageHeight						the height (number of rows) to make, or &lt;= 0 if default (the height in the DICOM file, or scaled by width with pixel aspect ratio preserved)
	 * @param	imageQuality					the image quality from 1 to 100 (best), or -1 if absent
	 * @param	annotation						the type of annotation to apply (choice is "all"; anything else means no annotation), or null if absent
	 * @param	superimposedImages				images to to be displayed superimposed on the appropriate frames, or null if none
	 * @param	superimposedToleranceDistance	difference in distance along normal to orientation for underlying and superimposed frames to be close enough to superimpose, in mm
	 * @param	arrayOfPerFrameDrawingShapes	per-frame shapes to be displayed on the respective frame, or null if none
	 * @param	debugLevel	
	 * @return									a String[] of the output filenames ordered by the frame order of the input image
	 */
	public static String[] convertFileToEightBitImage(String dicomFileName,String outputFileName,String outputFormat,
			double windowCenter,double windowWidth,int imageWidth,int imageHeight,int imageQuality,String annotation,Vector<SuperimposedImage> superimposedImages,double superimposedToleranceDistance,Vector<Shape>[] arrayOfPerFrameDrawingShapes,
			int debugLevel) throws DicomException, IOException {
		return convertFileToEightBitImage(dicomFileName,outputFileName,outputFormat,windowCenter,windowWidth,imageWidth,imageHeight,0,0,0,0,0,-1/*all frames*/,imageQuality,annotation,superimposedImages,superimposedToleranceDistance,arrayOfPerFrameDrawingShapes,debugLevel);
	}
							
	/**
	 * <p>Read a DICOM image input file, and create single frame 8-bit per channel images (windowed if grayscale) from the only or all frames.</p>
	 *
	 * <p>If the input file has multiple frames, the output file name will be postfixed with the frame number before the format extension, e.g., "output.jpg" will become "output_001.jpg", etc.</p>
	 *
	 * @param	dicomFileName					the input file name
	 * @param	outputFileName					the output file name (or basis of names for multiple frames)
	 * @param	outputFormat					the output file format name that a JIIO SPI will recognize (e.g. "jpeg")
	 * @param	windowCenter					the window center to use
	 * @param	windowWidth						the window width to use, or 0 if to use the width and center in the DICOM file
	 * @param	imageWidth						the width (number of columns) to make, or &lt;= 0 if default (the width in the DICOM file, or scaled by height with pixel aspect ratio preserved)
	 * @param	imageHeight						the height (number of rows) to make, or &lt;= 0 if default (the height in the DICOM file, or scaled by width with pixel aspect ratio preserved)
	 * @param	imageQuality					the image quality from 1 to 100 (best), or -1 if absent
	 * @param	annotation						the type of annotation to apply (choice is "all"; anything else means no annotation), or null if absent
	 * @param	superimposedImages				images to to be displayed superimposed on the appropriate frames, or null if none
	 * @param	arrayOfPerFrameDrawingShapes	per-frame shapes to be displayed on the respective frame, or null if none
	 * @param	debugLevel	
	 * @return									a String[] of the output filenames ordered by the frame order of the input image
	 */
	public static String[] convertFileToEightBitImage(String dicomFileName,String outputFileName,String outputFormat,
			double windowCenter,double windowWidth,int imageWidth,int imageHeight,int imageQuality,String annotation,Vector<SuperimposedImage> superimposedImages,Vector<Shape>[] arrayOfPerFrameDrawingShapes,
			int debugLevel) throws DicomException, IOException {
		return convertFileToEightBitImage(dicomFileName,outputFileName,outputFormat,windowCenter,windowWidth,imageWidth,imageHeight,0,0,0,0,0,-1/*all frames*/,imageQuality,annotation,superimposedImages,SuperimposedImage.DEFAULT_CLOSEST_SLICE_TOLERANCE_DISTANCE,arrayOfPerFrameDrawingShapes,debugLevel);
	}
							
	/**
	 * <p>Read a DICOM image input file, and create single frame 8-bit per channel images (windowed if grayscale) from the only or all frames.</p>
	 *
	 * <p>If the input file has multiple frames, the output file name will be postfixed with the frame number before the format extension, e.g., "output.jpg" will become "output_001.jpg", etc.</p>
	 *
	 * <p>The aspect ratio of the sub-region width and height, if any, should be the same as that
	 * of any explicitly specified image width and height, to avoid distortion.</p>
	 *
	 * @param	dicomFileName					the input file name
	 * @param	outputFileName					the output file name (or basis of names for multiple frames)
	 * @param	outputFormat					the output file format name that a JIIO SPI will recognize (e.g. "jpeg")
	 * @param	windowCenter					the window center to use
	 * @param	windowWidth						the window width to use, or 0 if to use the width and center in the DICOM file
	 * @param	imageWidth						the width (number of columns) to make, or &lt;= 0 if default (the width in the DICOM file, or scaled by height with pixel aspect ratio preserved)
	 * @param	imageHeight						the height (number of rows) to make, or &lt;= 0 if default (the height in the DICOM file, or scaled by width with pixel aspect ratio preserved)
	 * @param	regionX							the x (along row) integer offset (from 0 being the TLHC pixel) of the sub-region to write
	 * @param	regionY							the y (down column) integer offset (from 0 being the TLHC pixel) of the sub-region to write
	 * @param	regionWidth						the width (number of columns) to write, or &lt;= 0 if no sub-region selected (i.e., write the whole image)
	 * @param	regionHeight					the height (number of rows) to write, or &lt;= 0 if no sub-region selected (i.e., write the whole image)
	 * @param	firstFrame						the first frame to include, numbered from 0 (not 1)
	 * @param	lastFrame						the last frame to include, numbered from 0 (not 1), or -1 if all frames
	 * @param	imageQuality					the image quality from 1 to 100 (best), or -1 if absent
	 * @param	annotation						the type of annotation to apply (choice is "all"; anything else means no annotation), or null if absent
	 * @param	superimposedImages				images to to be displayed superimposed on the appropriate frames, or null if none
	 * @param	arrayOfPerFrameDrawingShapes	per-frame shapes to be displayed on the respective frame, or null if none
	 * @param	debugLevel	
	 * @return									a String[] of the output filenames ordered by the frame order of the input image
	 */
	public static String[] convertFileToEightBitImage(String dicomFileName,String outputFileName,String outputFormat,
			double windowCenter,double windowWidth,int imageWidth,int imageHeight,int regionX,int regionY,int regionWidth,int regionHeight,int firstFrame,int lastFrame,int imageQuality,String annotation,Vector<SuperimposedImage> superimposedImages,Vector<Shape>[] arrayOfPerFrameDrawingShapes,
			int debugLevel) throws DicomException, IOException {
		return convertFileToEightBitImage(dicomFileName,outputFileName,outputFormat,windowCenter,windowWidth,imageWidth,imageHeight,regionX,regionY,regionWidth,regionHeight,firstFrame,lastFrame,imageQuality,annotation,superimposedImages,SuperimposedImage.DEFAULT_CLOSEST_SLICE_TOLERANCE_DISTANCE,arrayOfPerFrameDrawingShapes,debugLevel);
	}
	
	/**
	 * <p>Read a DICOM image input file, and create single frame 8-bit per channel images (windowed if grayscale) from the only or all frames.</p>
	 *
	 * <p>If the input file has multiple frames, the output file name will be postfixed with the frame number before the format extension, e.g., "output.jpg" will become "output_001.jpg", etc.</p>
	 *
	 * <p>The aspect ratio of the sub-region width and height, if any, should be the same as that
	 * of any explicitly specified image width and height, to avoid distortion.</p>
	 *
	 * @param	dicomFileName					the input file name
	 * @param	outputFileName					the output file name (or basis of names for multiple frames)
	 * @param	outputFormat					the output file format name that a JIIO SPI will recognize (e.g. "jpeg")
	 * @param	windowCenter					the window center to use
	 * @param	windowWidth						the window width to use, or 0 if to use the width and center in the DICOM file
	 * @param	imageWidth						the width (number of columns) to make, or &lt;= 0 if default (the width in the DICOM file, or scaled by height with pixel aspect ratio preserved)
	 * @param	imageHeight						the height (number of rows) to make, or &lt;= 0 if default (the height in the DICOM file, or scaled by width with pixel aspect ratio preserved)
	 * @param	regionX							the x (along row) integer offset (from 0 being the TLHC pixel) of the sub-region to write
	 * @param	regionY							the y (down column) integer offset (from 0 being the TLHC pixel) of the sub-region to write
	 * @param	regionWidth						the width (number of columns) to write, or &lt;= 0 if no sub-region selected (i.e., write the whole image)
	 * @param	regionHeight					the height (number of rows) to write, or &lt;= 0 if no sub-region selected (i.e., write the whole image)
	 * @param	firstFrame						the first frame to include, numbered from 0 (not 1)
	 * @param	lastFrame						the last frame to include, numbered from 0 (not 1), or -1 if all frames
	 * @param	imageQuality					the image quality from 1 to 100 (best), or -1 if absent
	 * @param	annotation						the type of annotation to apply (choice is "all"; anything else means no annotation), or null if absent
	 * @param	superimposedImages				images to to be displayed superimposed on the appropriate frames, or null if none
	 * @param	superimposedToleranceDistance	difference in distance along normal to orientation for underlying and superimposed frames to be close enough to superimpose, in mm
	 * @param	arrayOfPerFrameDrawingShapes	per-frame shapes to be displayed on the respective frame, or null if none
	 * @param	debugLevel	
	 * @return									a String[] of the output filenames ordered by the frame order of the input image
	 */
	public static String[] convertFileToEightBitImage(String dicomFileName,String outputFileName,String outputFormat,
			double windowCenter,double windowWidth,int imageWidth,int imageHeight,int regionX,int regionY,int regionWidth,int regionHeight,int firstFrame,int lastFrame,int imageQuality,String annotation,Vector<SuperimposedImage> superimposedImages,double superimposedToleranceDistance,Vector<Shape>[] arrayOfPerFrameDrawingShapes,
			int debugLevel) throws DicomException, IOException {
		AttributeList list = new AttributeList();
		DicomInputStream in = new DicomInputStream(new BufferedInputStream(new FileInputStream(dicomFileName)));
		list.read(in);
		in.close();
		
		int nFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);
		if (lastFrame < 0) {
			firstFrame = 0;
			lastFrame = nFrames-1;
		}
		{
			int actuallyDoFrames = lastFrame - firstFrame + 1;
			if (actuallyDoFrames > nFrames) {
				throw new DicomException("Requested frame range from "+firstFrame+" to "+lastFrame+" exceeds actual number of frames "+nFrames);
			}
			nFrames = actuallyDoFrames;
		}
		String[] outputFileNames = new String[nFrames];
		
		if (windowWidth != 0) {
			Attribute aWindowWidth = new DecimalStringAttribute(TagFromName.WindowWidth);
			aWindowWidth.addValue(windowWidth);
			Attribute aWindowCenter = new DecimalStringAttribute(TagFromName.WindowCenter);
			aWindowCenter.addValue(windowCenter);
			replaceWindowWidthAndCenterInAttributeList(list,aWindowWidth,aWindowCenter);
			replaceWindowWidthAndCenterInFunctionalGroupSequences(list,nFrames,aWindowWidth,aWindowCenter);
		}
		
		GeometryOfVolume imageGeometry = null;
		try {
			imageGeometry = new GeometryOfVolumeFromAttributeList(list);
		}
		catch (Exception e) {
			// don't print exception, because it is legitimate for images to be missing this information
			//e.printStackTrace(System.err);
		}

		boolean useColorForAnnotations = false;
		DemographicAndTechniqueAnnotations demographicAndTechniqueAnnotations = null;
		if (annotation != null) {
			annotation = annotation.toLowerCase();
			if (annotation.contains(ALL_ANNOTATIONS) || annotation.contains(ICON_ANNOTATIONS)) {
				demographicAndTechniqueAnnotations =  annotation.contains(ICON_ANNOTATIONS) ? new IconDemographicAndTechniqueAnnotations(list) : new DemographicAndTechniqueAnnotations(list,imageGeometry);
			}
			if (annotation.contains(COLOR_ANNOTATIONS)) {
				useColorForAnnotations = true;
			}
		}
		
		BufferedImage[] renderedImages = makeEightBitImages(list,imageGeometry,superimposedImages,superimposedToleranceDistance,arrayOfPerFrameDrawingShapes,firstFrame,lastFrame,debugLevel);
		if (nFrames == 1) {
			outputFileNames[0] = outputFileName;
			Iterator<TextAnnotationPositioned> frameDemographicAndTechniqueAnnotations = demographicAndTechniqueAnnotations == null ? null : demographicAndTechniqueAnnotations.iterator(firstFrame);
			writeEightBitImageForFrame(renderedImages[0],frameDemographicAndTechniqueAnnotations,outputFileName,outputFormat,imageWidth,imageHeight,regionX,regionY,regionWidth,regionHeight,imageQuality,useColorForAnnotations,debugLevel);
		}
		else {
			String frameOutputFilePrefix = outputFileName;
			String frameOutputFileSuffix = "";
			{
				java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(.+)[.]([a-zA-Z0-9]+)$");
				java.util.regex.Matcher m = p.matcher(outputFileName);
				if (m.matches()) {
//System.err.println("matches");
					int groupCount = m.groupCount();
					if (groupCount == 2) {
						frameOutputFilePrefix = m.group(1);		// first group is not 0, which is the entire match
						frameOutputFileSuffix = m.group(2);
					}
				}
			}
if (debugLevel > 1) System.err.println("frameOutputFilePrefix = "+frameOutputFilePrefix);
if (debugLevel > 1) System.err.println("frameOutputFileSuffix = "+frameOutputFileSuffix);
			java.text.NumberFormat zeroPaddedFrameNumberFormatter = new java.text.DecimalFormat("0000000000");	// theoretically 10 digits is IS max !
			for (int f=0; f<nFrames; ++f) {
				// frame in file name will start from 1 not 0 to follow DICOM frame numbering convention, and will be the actual frame number (not the first one if firstFrame > 0)
				String frameOutputFileName = frameOutputFilePrefix + "_" + zeroPaddedFrameNumberFormatter.format(firstFrame+f+1) + "." + frameOutputFileSuffix;
				outputFileNames[f] = frameOutputFileName;
				Iterator<TextAnnotationPositioned> frameDemographicAndTechniqueAnnotations = demographicAndTechniqueAnnotations == null ? null : demographicAndTechniqueAnnotations.iterator(f+firstFrame);
				writeEightBitImageForFrame(renderedImages[f],frameDemographicAndTechniqueAnnotations,frameOutputFileName,outputFormat,imageWidth,imageHeight,regionX,regionY,regionWidth,regionHeight,imageQuality,useColorForAnnotations,debugLevel);
			}
		}
		return outputFileNames;
	}
	
	/**
	 * <p>Write a single frame 8-bit per channel BufferedImage.</p>
	 *		
	 * <p>The aspect ratio of the sub-region width and height, if any, should be the same as that
	 * of any explicitly specified image width and height, to avoid distortion.</p>
	 *
	 * @param	renderedImage			the 8 bit image (already windowed if grayscale)
	 * @param	annotations				an Iterator of annotations to apply, or null if none
	 * @param	outputFileName			the output file name
	 * @param	outputFormat			the output file format name that a JIIO SPI will recognize (e.g. "jpeg")
	 * @param	imageWidth				the width (number of columns) to write, or &lt;= 0 if default (the width in the DICOM file, or scaled by height with pixel aspect ratio preserved)
	 * @param	imageHeight				the height (number of rows) to weite, or &lt;= 0 if default (the height in the DICOM file, or scaled by width with pixel aspect ratio preserved)
	 * @param	regionX					the x (along row) integer offset (from 0 being the TLHC pixel) of the sub-region to write
	 * @param	regionY					the y (down column) integer offset (from 0 being the TLHC pixel) of the sub-region to write
	 * @param	regionWidth				the width (number of columns) to write, or &lt;= 0 if no sub-region selected (i.e., write the whole image)
	 * @param	regionHeight			the height (number of rows) to write, or &lt;= 0 if no sub-region selected (i.e., write the whole image)
	 * @param	imageQuality			the image quality from 1 to 100 (best), or -1 if absent
	 * @param	useColorForAnnotations	use color for annotations (i.e., produce color output image even if input is grayscale)
	 * @param	debugLevel	
	 */
	public static void writeEightBitImageForFrame(BufferedImage renderedImage,Iterator<TextAnnotationPositioned> annotations,String outputFileName,String outputFormat,
			int imageWidth,int imageHeight,int regionX,int regionY,int regionWidth,int regionHeight,int imageQuality,boolean useColorForAnnotations,
			int debugLevel) throws DicomException, IOException {

		if (regionWidth > 0 && regionHeight > 0) {
			renderedImage = renderedImage.getSubimage(regionX,regionY,regionWidth,regionHeight);
		}
		
		try {
if (debugLevel > 0) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Requested width = "+imageWidth+" height = "+imageHeight);
			int srcWidth = renderedImage.getWidth();
			int srcHeight = renderedImage.getHeight();
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Source width = "+srcWidth+" width = "+srcHeight);
			if (imageWidth <= 0 && imageHeight > 0 && imageHeight != srcHeight) {
				// specified desired height only and different from source - preserve pixel aspect ratio
				double scale = ((double)imageHeight)/srcHeight;
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Resizing - specified desired height only and different from source, scale = "+scale);
				renderedImage = BufferedImageUtilities.resampleWithAffineTransformOp(renderedImage,scale,scale);
			}
			else if (imageHeight <= 0 && imageWidth > 0 && imageWidth != srcWidth) {
				// specified desired width only and different from source - preserve pixel aspect ratio
				double scale = ((double)imageWidth)/srcWidth;
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Resizing - specified desired width only and different from source, scale = "+scale);
				renderedImage = BufferedImageUtilities.resampleWithAffineTransformOp(renderedImage,scale,scale);
			}
			else if (imageWidth > 0 && imageHeight > 0 && (imageWidth != srcWidth || imageHeight != srcHeight)) {
				// specified both height and width and different from source ... implies possible pixel aspect ratio change
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Resizing - specified desired width and height and different from source ");
				renderedImage = BufferedImageUtilities.resampleWithAffineTransformOp(renderedImage,imageWidth,imageHeight);
			}
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Resized width = "+renderedImage.getWidth()+" height = "+renderedImage.getHeight());
		}
		catch (Exception e) {	// such as java.awt.image.ImagingOpException, java.awt.HeadlessException
			e.printStackTrace(System.err);
			// and leave it alone unresized
		}
		
		if (useColorForAnnotations || renderedImage.getColorModel().getNumComponents() > 1) {
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Need color or is already color, so converting to most favorable image type");
			renderedImage = BufferedImageUtilities.convertToMostFavorableImageType(renderedImage);		// Otherwise will not draw color on grayscale images
		}
		if (annotations != null) {
			Graphics2D g2d = renderedImage.createGraphics();
		
			//g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			//g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			//g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			//g2d.drawImage(windowedImage,0,0,windowedImage.getWidth(),windowedImage.getHeight(),null/*no observer*/);
		
			String demographicAndTechniqueFontName = "";
			int demographicAndTechniqueFontStyle = Font.PLAIN;
			int demographicAndTechniqueFontSize = 10;
			Font demographicAndTechniqueFont = new Font(demographicAndTechniqueFontName,demographicAndTechniqueFontStyle,demographicAndTechniqueFontSize);
			Color demographicAndTechniqueColor = Color.pink;
		
			g2d.setColor(demographicAndTechniqueColor);
			g2d.setFont(demographicAndTechniqueFont);

			while (annotations.hasNext()) {
				TextAnnotationPositioned.drawPositionedString(annotations.next(),g2d,renderedImage.getWidth(),renderedImage.getHeight(),5,5);
			}
		}
		if (renderedImage.getColorModel().getNumComponents() > 3) {
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Converting RGBA to RGB");
			// Before writing, make it RGB again because codecs might otherwise write extra component for alpha channel
			renderedImage = BufferedImageUtilities.convertToThreeChannelImageTypeIfFour(renderedImage);
		}
		//if (!ImageIO.write(renderedImage,outputFormat,new File(outputFileName))) {
		//	throw new DicomException("Cannot find writer for format"+outputFormat);
		//}
		// See also "http://www.oracle.com/technetwork/java/iio-141084.html"
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Attempting to write format = "+outputFormat);
		Iterator writers = ImageIO.getImageWritersByFormatName(outputFormat);
		if (writers != null && writers.hasNext()) {
			ImageWriter writer = (ImageWriter)writers.next();
			if (writer != null) {
				ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(new File(outputFileName));
				writer.setOutput(imageOutputStream);
				ImageWriteParam writeParameters;
				try {
					writeParameters = writer.getDefaultWriteParam();
					if (writeParameters.canWriteCompressed()) {
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Default compression mode = "+writeParameters.getCompressionMode());
						String[] compressionTypesAvailable = writeParameters.getCompressionTypes();
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Compression types available = "+Arrays.toString(compressionTypesAvailable));
						//if (compressionTypesAvailable != null && compressionTypesAvailable.length > 0) {
//if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Setting compression type to = "+compressionTypesAvailable[0]);
						//	writeParameters.setCompressionType(compressionTypesAvailable[0]);
						//}
						if (imageQuality >= 0 && imageQuality <= 100) {		// -1 is flag that it was not specified
							float quality = ((float)imageQuality)/100f;
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Setting quality = "+quality);
							writeParameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
							writeParameters.setCompressionQuality(quality);
						}
					}
					if (writeParameters.canWriteProgressive()) {
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Setting progressive mode");
						writeParameters.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
					}
					writer.getDefaultWriteParam();
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
					writeParameters=null;		// Ignore inability to alter parameters
				}
				IIOMetadata metadata = null;
				writer.write(metadata,new IIOImage(renderedImage,null/*no thumbnails*/,metadata),writeParameters);
				imageOutputStream.flush();
				imageOutputStream.close();
				try {
if (debugLevel > 1) System.err.println("ConsumerFormatImageMaker.convertFileToEightBitImage(): Calling dispose() on writer");
					writer.dispose();
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
			else {
				throw new DicomException("Cannot find writer for format"+outputFormat);
			}
		}
		else {
			throw new DicomException("Cannot find writer for format"+outputFormat);
		}
	}
	
	/**
	 * <p>Read a DICOM image input file, and create single frame 8-bit per channel images (windowed if grayscale) from the only or all frames.</p>
	 *
	 * <p>If the input file has multiple frames, the output file name will be postfixed with the frame number before the format extension, e.g., "output.jpg" will become "output_001.jpg", etc.</p>
	 *
	 * <p>The aspect ratio of the sub-region width and height, if any, should be the same as that
	 * of any explicitly specified image width and height, to avoid distortion.</p>
	 *
	 * @param	arg	two required parameters, the input file name, output file name (or basis of names for multiple frames), optionally the format (defaults to jpeg), then optionally the window center and width,
	 *			then optionally the desired width and height (or 0 if to use the width and center in the DICOM file and more arguments), then optionally the image quality from 0 to 100,
	 *			then optionally whether or not to include annotation (choice is "all" or "icon" +/- "color", e.g., "all_color"; anything else means no annotation; default is "all_color")
	 *			then optionally the debug level
	 *			then optionally the sub region x, y, width and height
	 *			then optionally the first and last frame (-1 if all frames)
	 */
	public static void main(String arg[]) {
		String dicomFileName = arg[0];
		String outputFileName = arg[1];
		String outputFormat = arg.length >  2 ? arg[2] : "jpeg";
		double windowCenter = arg.length >  3 ? Double.parseDouble(arg[3]) : 0;
		double windowWidth  = arg.length >  4 ? Double.parseDouble(arg[4]) : 0;
		int      imageWidth = arg.length >  5 ? Integer.parseInt(arg[5]) : 0;
		int     imageHeight = arg.length >  6 ? Integer.parseInt(arg[6]) : 0;
		int    imageQuality = arg.length >  7 ? Integer.parseInt(arg[7]) : -1;
		String   annotation = arg.length >  8 ? arg[8] : (ALL_ANNOTATIONS + "_" + COLOR_ANNOTATIONS);
		int     debugLevel  = arg.length >  9 ? Integer.parseInt(arg[9]) : -1;
		int        regionX  = arg.length > 13 ? Integer.parseInt(arg[10]) : 0;
		int        regionY  = arg.length > 13 ? Integer.parseInt(arg[11]) : 0;
		int    regionWidth  = arg.length > 13 ? Integer.parseInt(arg[12]) : 0;
		int   regionHeight  = arg.length > 13 ? Integer.parseInt(arg[13]) : 0;
		int     firstFrame  = arg.length > 15 ? Integer.parseInt(arg[14]) : 0;
		int      lastFrame  = arg.length > 15 ? Integer.parseInt(arg[15]) : -1;
		
		try {
			convertFileToEightBitImage(dicomFileName,outputFileName,outputFormat,windowCenter,windowWidth,imageWidth,imageHeight,regionX,regionY,regionWidth,regionHeight,firstFrame,lastFrame,imageQuality,annotation,debugLevel);
		}
		catch (Exception e) {
			//System.err.println(e);
			e.printStackTrace(System.err);
		}
	}
}
