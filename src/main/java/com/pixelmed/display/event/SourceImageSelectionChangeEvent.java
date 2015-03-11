/* Copyright (c) 2001-2004, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display.event;

import java.awt.image.BufferedImage;

import com.pixelmed.display.SourceImage;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.event.Event;
import com.pixelmed.event.EventContext;
import com.pixelmed.geometry.GeometryOfVolume;

/**
 * @author	dclunie
 */
public class SourceImageSelectionChangeEvent extends Event {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/event/SourceImageSelectionChangeEvent.java,v 1.9 2006/02/02 17:42:20 dclunie Exp $";

	private SourceImage sourceImage;
	private int index;
	private int[] sortOrder;
	private AttributeList attributeList;
	private GeometryOfVolume imageGeometry;
	
	/**
	 * @param	eventContext
	 * @param	sourceImage
	 * @param	sortOrder
	 * @param	index
	 * @param	attributeList
	 * @param	imageGeometry
	 */
	public SourceImageSelectionChangeEvent(EventContext eventContext,SourceImage sourceImage,int[] sortOrder,int index,AttributeList attributeList,GeometryOfVolume imageGeometry) {
		super(eventContext);
		this.sourceImage=sourceImage;
		this.sortOrder=sortOrder;
		this.index=index;
		this.attributeList=attributeList;
		this.imageGeometry=imageGeometry;;
	}

	/***/
	public SourceImage getSourceImage() { return sourceImage; }
	/***/
	public int getNumberOfBufferedImages() { return sourceImage == null ? 0 : sourceImage.getNumberOfBufferedImages(); }
	/***/
	public int[] getSortOrder() { return sortOrder; }
	/***/
	public int getIndex() { return index; }
	/***/
	public AttributeList getAttributeList() { return attributeList; }
	/***/
	public GeometryOfVolume getGeometryOfVolume() { return imageGeometry; }
}

