/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.util.Iterator;

/**
 * <p>A class contain useful methods for manipulating Functional Group Sequences.</p>
 *
 * @author	dclunie
 */
public class FunctionalGroupUtilities {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/FunctionalGroupUtilities.java,v 1.5 2014/11/13 15:58:20 dclunie Exp $";
	
	/**
	 * <p>Create shared functional group sequences if not already present.</p>
	 *
	 * @param	list			an existing (possibly empty) attribute list, if null, a new one will be created; may already shared and per-frame functional group sequences or they will be added
	 * @return					attribute list with empty per-frame and shared functional group sequences added
	 */
	public static AttributeList createSharedFunctionalGroupsIfNotPresent(AttributeList list) {
		if (list == null) {
			list = new AttributeList();
		}

		SequenceAttribute aSharedFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.SharedFunctionalGroupsSequence);
		if (aSharedFunctionalGroupsSequence == null) {
			aSharedFunctionalGroupsSequence = new SequenceAttribute(TagFromName.SharedFunctionalGroupsSequence);
			list.put(aSharedFunctionalGroupsSequence);
			aSharedFunctionalGroupsSequence.addItem(new AttributeList());
		}

		return list;
	}
	
	/**
	 * <p>Create shared and per-frame functional group sequences if not already present.</p>
	 *
	 * @param	list			an existing (possibly empty) attribute list, if null, a new one will be created; may already shared and per-frame functional group sequences or they will be added
	 * @param	numberOfFrames	number of frames
	 * @return					attribute list with empty per-frame and shared functional group sequences added
	 */
	public static AttributeList createFunctionalGroupsIfNotPresent(AttributeList list,int numberOfFrames) {
		list = createSharedFunctionalGroupsIfNotPresent(list);

		SequenceAttribute aPerFrameFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.PerFrameFunctionalGroupsSequence);
		if (aPerFrameFunctionalGroupsSequence == null) {
			aPerFrameFunctionalGroupsSequence = new SequenceAttribute(TagFromName.PerFrameFunctionalGroupsSequence);
			list.put(aPerFrameFunctionalGroupsSequence);
			for (int f=0; f<numberOfFrames; ++f) {
				aPerFrameFunctionalGroupsSequence.addItem(new AttributeList());
			}
		}

		return list;
	}

	/**
	 * <p>Insert a shared functional group sequence Pixel Value Transformation Sequence entry.</p>
	 *
	 * @param	list			an existing (possibly empty) attribute list, if null, a new one will be created; may already shared and per-frame functional group sequences or they will be added
	 * @param	numberOfFrames	number of frames
	 * @param	rescaleSlope	rescale slope
	 * @param	rescaleIntercept	rescale intercept
	 * @param	rescaleType		rescale type
	 * @return					attribute list with per-frame and shared functional group sequences for VOI added
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static AttributeList generatePixelValueTransformationFunctionalGroup(AttributeList list,int numberOfFrames,double rescaleSlope,double rescaleIntercept,String rescaleType) throws DicomException {
		list = createFunctionalGroupsIfNotPresent(list,numberOfFrames);
		SequenceAttribute aSharedFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.SharedFunctionalGroupsSequence);
		AttributeList sharedFunctionalGroupsSequenceList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(aSharedFunctionalGroupsSequence);

		{
			SequenceAttribute aPixelValueTransformationSequence = new SequenceAttribute(TagFromName.PixelValueTransformationSequence);
			sharedFunctionalGroupsSequenceList.put(aPixelValueTransformationSequence);
			AttributeList itemList = new AttributeList();
			aPixelValueTransformationSequence.addItem(itemList);
			
			{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleSlope); a.addValue(rescaleSlope); itemList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleIntercept); a.addValue(rescaleIntercept); itemList.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.RescaleType); a.addValue(rescaleType); itemList.put(a); }
		}

		return list;
	}

	/**
	 * <p>Insert a shared functional group sequence Frame VOI LUT Sequence entry.</p>
	 *
	 * @param	list			an existing (possibly empty) attribute list, if null, a new one will be created; may already shared and per-frame functional group sequences or they will be added
	 * @param	numberOfFrames	number of frames
	 * @param	windowWidth		window width
	 * @param	windowCenter	window center
	 * @param	voiLUTFunction	VOI LUT function
	 * @return					attribute list with per-frame and shared functional group sequences for VOI added
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static AttributeList generateVOILUTFunctionalGroup(AttributeList list,int numberOfFrames,double windowWidth,double windowCenter,String voiLUTFunction) throws DicomException {
		list = createFunctionalGroupsIfNotPresent(list,numberOfFrames);
		SequenceAttribute aSharedFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.SharedFunctionalGroupsSequence);
		AttributeList sharedFunctionalGroupsSequenceList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(aSharedFunctionalGroupsSequence);

		{
			SequenceAttribute aFrameVOILUTSequence = new SequenceAttribute(TagFromName.FrameVOILUTSequence);
			sharedFunctionalGroupsSequenceList.put(aFrameVOILUTSequence);
			AttributeList itemList = new AttributeList();
			aFrameVOILUTSequence.addItem(itemList);
			
			{ Attribute a = new DecimalStringAttribute(TagFromName.WindowWidth); a.addValue(windowWidth); itemList.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.WindowCenter); a.addValue(windowCenter); itemList.put(a); }
			{ Attribute a = new CodeStringAttribute(TagFromName.VOILUTFunction); a.addValue(voiLUTFunction); itemList.put(a); }
		}

		return list;
	}

	/**
	 * <p>Insert a shared functional group sequence FrameTypeSequence entry.</p>
	 *
	 * @param	list				an existing (possibly empty) attribute list, if null, a new one will be created; may already shared and per-frame functional group sequences or they will be added
	 * @param	tFrameTypeSequence	the Functional Group Sequence tag (e.g., TagFromName.ParametricMapFrameTypeSequence)
	 * @param	aFrameType			a FrameType attribute with values
	 * @return						attribute list with per-frame and shared functional group sequences for FrameTypeSequence added
	 * @throws	DicomException	if error in DICOM encoding
	 */

	public static AttributeList generateFrameTypeSharedFunctionalGroup(AttributeList list,AttributeTag tFrameTypeSequence,Attribute aFrameType) throws DicomException {
		list = FunctionalGroupUtilities.createSharedFunctionalGroupsIfNotPresent(list);
		SequenceAttribute aSharedFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.SharedFunctionalGroupsSequence);
		AttributeList sharedFunctionalGroupsSequenceList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(aSharedFunctionalGroupsSequence);
		
		{
			SequenceAttribute aframeTypeSequence = new SequenceAttribute(tFrameTypeSequence);
			sharedFunctionalGroupsSequenceList.put(aframeTypeSequence);
			AttributeList itemList = new AttributeList();
			aframeTypeSequence.addItem(itemList);
			itemList.put(aFrameType);
		}
		
		return list;
	}
	
	/**
	 * <p>Remove a specified functional group sequences from the shared and per-frame functional group sequences.</p>
	 *
	 * @param	list						an attribute list
	 * @param	functionalGroupSequenceTag	functional group to remove
	 */
	public static void removeFunctionalGroup(AttributeList list,AttributeTag functionalGroupSequenceTag) {
		SequenceAttribute aPerFrameFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.PerFrameFunctionalGroupsSequence);
		if (aPerFrameFunctionalGroupsSequence != null) {
			int nFrames = aPerFrameFunctionalGroupsSequence.getNumberOfItems();
			int frameNumber = 0;
			Iterator pfitems = aPerFrameFunctionalGroupsSequence.iterator();
			while (pfitems.hasNext()) {
				SequenceItem fitem = (SequenceItem)pfitems.next();
				AttributeList flist = fitem.getAttributeList();
				if (flist != null) {
					flist.remove(functionalGroupSequenceTag);
				}
				++frameNumber;
			}
		}
		SequenceAttribute aSharedFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.SharedFunctionalGroupsSequence);
		if (aSharedFunctionalGroupsSequence != null) {
			// assert aSharedFunctionalGroupsSequence.getNumberOfItems() == 1
			Iterator sitems = aSharedFunctionalGroupsSequence.iterator();
			if (sitems.hasNext()) {
				SequenceItem sitem = (SequenceItem)sitems.next();
				AttributeList slist = sitem.getAttributeList();
				if (slist != null) {
					slist.remove(functionalGroupSequenceTag);
				}
			}
		}
	}
}

