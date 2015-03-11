/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.geometry.GeometryOfSlice;
import com.pixelmed.geometry.GeometryOfVolume;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

class StackOfSlices {
	
	protected String frameOfReferenceUID;
	protected double[] rowOrientation;
	protected double[] columnOrientation;
	protected SortedMap<Double,SortedSet<Integer>> mapOfDistanceToSetOfSlices;
	protected int[] inStackPositionBySlice;
			
	public StackOfSlices(AttributeList list) throws DicomException {
		frameOfReferenceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.FrameOfReferenceUID);
		mapOfDistanceToSetOfSlices = null;

		GeometryOfVolume geometryOfVolume = new GeometryOfVolumeFromAttributeList(list);					// this works even if there are multiple passes
		GeometryOfSlice[] geometryOfSlices = geometryOfVolume.getGeometryOfSlices();
		if (geometryOfSlices != null) {
			int numberOfFrames = geometryOfSlices.length;
			if (numberOfFrames > 1 && geometryOfVolume.areAllSlicesParallel()) {							// do not make stacks if only a single slice
System.err.println("StackOfSlices.addStack(): are parallel");
				rowOrientation = geometryOfSlices[0].getRowArray();			// need these for matching equals stacks
				columnOrientation = geometryOfSlices[0].getColumnArray();
				// Sort the slices (frames) by distance from origin, recognizing that there may be multiples at the same distance ...
				mapOfDistanceToSetOfSlices = new TreeMap<Double,SortedSet<Integer>>();
				for (int f=0; f<numberOfFrames; ++f) {
					Double distance = new Double(geometryOfSlices[f].getDistanceAlongNormalFromOrigin());
					SortedSet<Integer> slicesAtSameDistance = mapOfDistanceToSetOfSlices.get(distance);
					if (slicesAtSameDistance == null) {
						slicesAtSameDistance =  new TreeSet<Integer>();
						mapOfDistanceToSetOfSlices.put(distance,slicesAtSameDistance);
					}
					slicesAtSameDistance.add(new Integer(f));
				}
				// Now walk through the sorted distances, incrementing the inStackPosition as we go, recording the inStackPosition for each slice (frame) at that distance ...
				inStackPositionBySlice = new int[numberOfFrames];
				int inStackPosition = 1;
				for (Double distance : mapOfDistanceToSetOfSlices.keySet()) {
System.err.println("StackOfSlices.addStack(): distance="+distance);
					SortedSet<Integer> slicesAtSameDistance = mapOfDistanceToSetOfSlices.get(distance);
					for (Integer fI : slicesAtSameDistance) {
						int f = fI.intValue();
						if (inStackPositionBySlice[f] != 0) {
System.err.println("StackOfSlices.addStack(): ERROR - distance="+distance+" inStackPositionBySlice[f] already exists="+inStackPositionBySlice[f]+" frame="+f);
						}
						inStackPositionBySlice[f] = inStackPosition;
System.err.println("StackOfSlices.addStack(): distance="+distance+" inStackPosition="+inStackPosition+" frame="+f);
					}
					++inStackPosition;
				}
			}
		}
	}
	
	boolean isValid() {
		return frameOfReferenceUID != null
			&& rowOrientation != null
			&& mapOfDistanceToSetOfSlices != null
			&& columnOrientation != null
			&& inStackPositionBySlice != null;
	}
		
	public void addStackAttributesToExistingFrameContentSequence(AttributeList list,String stackID) throws DicomException {
System.err.println("StackOfSlices.addStack(): adding stack "+stackID);
		int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);
		SequenceAttribute perFrameFunctionalGroupsSequence = (SequenceAttribute)(list.get(TagFromName.PerFrameFunctionalGroupsSequence));
		for (int f=0; f<numberOfFrames; ++f) {
			SequenceAttribute frameContentSequence = (SequenceAttribute)(perFrameFunctionalGroupsSequence.getItem(f).getAttributeList().get(TagFromName.FrameContentSequence));
			AttributeList frameContentList = frameContentSequence.getItem(0).getAttributeList();
			{ Attribute a = new ShortStringAttribute(TagFromName.StackID); a.addValue(stackID); frameContentList.put(a); }
//System.err.println("addStack(): inStackPositionBySlice[f]="+inStackPositionBySlice[f]+" frame="+f);
			{ Attribute a = new UnsignedLongAttribute(TagFromName.InStackPositionNumber); a.addValue(inStackPositionBySlice[f]); frameContentList.put(a); }
		}
	}
	
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof StackOfSlices) {
			StackOfSlices os = (StackOfSlices)o;
			result = frameOfReferenceUID.equals(os.frameOfReferenceUID)
				  && rowOrientation[0] == os.rowOrientation[0] && rowOrientation[1] == os.rowOrientation[1] && rowOrientation[2] == os.rowOrientation[2]
				  && columnOrientation[0] == os.columnOrientation[0] && columnOrientation[1] == os.columnOrientation[1] && columnOrientation[2] == os.columnOrientation[2]
				  && mapOfDistanceToSetOfSlices.equals(os.mapOfDistanceToSetOfSlices);
		}
		return result;
	}
		
	public int hashCode() {
		return frameOfReferenceUID.hashCode() + mapOfDistanceToSetOfSlices.hashCode();
	}
}


