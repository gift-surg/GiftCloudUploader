/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.util.Iterator;
import java.util.Map;

/**
 * <p>A class to represent the study, series and instance identifiers necessary to retrieve a specific instance using the hierarchical model.</p>
 *
 * <p>Used, for example, when extracting a map of instance uids to hierarchical references from an SR evidence sequence.</p>
 *
 * @author	dclunie
 */
public class HierarchicalSOPInstanceReference {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/HierarchicalSOPInstanceReference.java,v 1.3 2014/09/09 20:34:09 dclunie Exp $";

	protected String studyInstanceUID;
	protected String seriesInstanceUID;
	protected String sopInstanceUID;
	protected String sopClassUID;

	/**
	 * <p>Construct an instance of a reference to an instance, with its hierarchy.</p>
	 *
	 * @param	studyInstanceUID	the Study Instance UID
	 * @param	seriesInstanceUID	the Series Instance UID
	 * @param	sopInstanceUID		the SOP Instance UID
	 * @param	sopClassUID			the SOP Class UID
	 */
	public HierarchicalSOPInstanceReference(String studyInstanceUID,String seriesInstanceUID,String sopInstanceUID,String sopClassUID) {
		this.studyInstanceUID=studyInstanceUID;
		this.seriesInstanceUID=seriesInstanceUID;
		this.sopInstanceUID=sopInstanceUID;
		this.sopClassUID=sopClassUID;
	}

	/**
	 * <p>Construct an instance of a reference to an instance, with its hierarchy.</p>
	 *
	 * @param	reference			an existing reference to clone
	 */
	public HierarchicalSOPInstanceReference(HierarchicalSOPInstanceReference reference) {
		this.studyInstanceUID  = reference.getStudyInstanceUID();
		this.seriesInstanceUID = reference.getSeriesInstanceUID();
		this.sopInstanceUID    = reference.getSOPInstanceUID();
		this.sopClassUID       = reference.getSOPClassUID();
	}

	/**
	 * <p>Construct an instance of a reference from the attributes of the referenced instance itself.</p>
	 *
	 * @param	list			the attributes of an instance
	 */
	public HierarchicalSOPInstanceReference(AttributeList list) {
		this.studyInstanceUID  = Attribute.getSingleStringValueOrNull(list,TagFromName.StudyInstanceUID);
		this.seriesInstanceUID = Attribute.getSingleStringValueOrNull(list,TagFromName.SeriesInstanceUID);
		this.sopInstanceUID    = Attribute.getSingleStringValueOrNull(list,TagFromName.SOPInstanceUID);
		this.sopClassUID       = Attribute.getSingleStringValueOrNull(list,TagFromName.SOPClassUID);
	}

	/**
	 * <p>Get the Study Instance UID.</p>
	 *
	 * @return		the Study Instance UID, or null
	 */
	public String getStudyInstanceUID() { return studyInstanceUID; }

	/**
	 * <p>Get the Series Instance UID.</p>
	 *
	 * @return		the Series Instance UID, or null
	 */
	public String getSeriesInstanceUID() { return seriesInstanceUID; }

	/**
	 * <p>Get the SOP Instance UID.</p>
	 *
	 * @return		the SOP Instance UID, or null
	 */
	public String getSOPInstanceUID() { return sopInstanceUID; }

	/**
	 * <p>Get the SOP Class UID.</p>
	 *
	 * @return		the SOP Class UID, or null
	 */
	public String getSOPClassUID()    { return sopClassUID; }

	/**
	 * <p>Find hierarchical references to instances that may be referenced anywhere in any dataset regardless of depth of nesting.</p>
	 *
	 * <p>Detects any occurence of ReferencedSOPInstanceUID and then uses surround context to establish hierarchy.</p>
	 *
	 * @param	list									the top level dataset of an instance
	 * @param	hierarchicalInstancesBySOPInstanceUID	a {@link java.util.Map Map} of {@link java.lang.String String} SOPInstanceUIDs to {@link com.pixelmed.dicom.HierarchicalSOPInstanceReference HierarchicalSOPInstanceReference} that is added to as references are found
	 */
	public static void addToHierarchicalReferencesToSOPInstances(AttributeList list,Map<String,HierarchicalSOPInstanceReference> hierarchicalInstancesBySOPInstanceUID) {
		addToHierarchicalReferencesToSOPInstances(list,hierarchicalInstancesBySOPInstanceUID,null/*parentStudyInstanceUID*/,null/*parentSeriesInstanceUID*/,true/*topLevelDataSet*/);
	}
		
	/**
	 * <p>Find hierarchical references to instances that may be referenced anywhere in any dataset regardless of depth of nesting.</p>
	 *
	 * <p>Detects any occurence of ReferencedSOPInstanceUID and then uses surrounding context to establish hierarchy.</p>
	 *
	 * <p>Surrounding contex is updated during descent, i.e., closer SeriesInstanceUID inside ReferencedSeriesSequence would override top level SeriesInstanceUID.</p>
	 *
	 * <p>Invoked recursively to descend through the any nested sequences adding to hierarchicalInstancesBySOPInstanceUID.</p>
	 *
	 * @param	list									either the top level dataset of an instance or an item of a Sequence
	 * @param	hierarchicalInstancesBySOPInstanceUID	a {@link java.util.Map Map} of {@link java.lang.String String} SOPInstanceUIDs to {@link com.pixelmed.dicom.HierarchicalSOPInstanceReference HierarchicalSOPInstanceReference} that is added to as references are found
	 */
	private static void addToHierarchicalReferencesToSOPInstances(AttributeList list,
			Map<String,HierarchicalSOPInstanceReference> hierarchicalInstancesBySOPInstanceUID,
			String parentStudyInstanceUID,
			String parentSeriesInstanceUID,
			boolean topLevelDataSet) {
		if (list != null) {
			// need to set the following BEFORE recursing into sequences, because sequence may occur in list before these attributes do (so can't just pick them up as iterating through the list attributes)
			String referencedSOPClassUID      = Attribute.getSingleStringValueOrNull   (list,TagFromName.ReferencedSOPClassUID);
			String referencedSOPInstanceUID   = Attribute.getSingleStringValueOrNull   (list,TagFromName.ReferencedSOPInstanceUID);
			if (!topLevelDataSet) {
				parentSeriesInstanceUID       = Attribute.getSingleStringValueOrDefault(list,TagFromName.SeriesInstanceUID,parentSeriesInstanceUID);
				parentStudyInstanceUID        = Attribute.getSingleStringValueOrDefault(list,TagFromName.StudyInstanceUID, parentStudyInstanceUID);
			}
			// else do NOT assume that context of reference is same study or series as the current instance itself
			
			// insert a reference if found
			
			if (referencedSOPInstanceUID != null && referencedSOPInstanceUID.length() > 0) {
				String useSOPClassUID       = referencedSOPClassUID;
				String useSeriesInstanceUID = parentSeriesInstanceUID;
				String useStudyInstanceUID  = parentStudyInstanceUID;
				// check it isn't already there and merge if necessary, taking care not to overwrite any valid data by reusing it if current context is null
				HierarchicalSOPInstanceReference existingReference = hierarchicalInstancesBySOPInstanceUID.get(referencedSOPInstanceUID);
				if (existingReference != null) {
					// NB. take care to use these existing values ONLY for the current insert operation - it is NOT valid for updating the context, e.g., other instances may be in different series (like PS).
					if (useSOPClassUID == null) {
						useSOPClassUID = existingReference.getSOPClassUID();
					}
					if (useSeriesInstanceUID == null) {
						useSeriesInstanceUID = existingReference.getSeriesInstanceUID();
					}
					if (useStudyInstanceUID == null) {
						useStudyInstanceUID = existingReference.getStudyInstanceUID();
					}
				}
				// replace anything there already with "merged" information (if any) ...
				HierarchicalSOPInstanceReference newReference = new HierarchicalSOPInstanceReference(useStudyInstanceUID,useSeriesInstanceUID,referencedSOPInstanceUID,useSOPClassUID);
				hierarchicalInstancesBySOPInstanceUID.put(referencedSOPInstanceUID,newReference);
			}

			// now we can descend into sequences ... do this even if we found a reference in current list, since may be "qualifiers" (e.g., presentation states and RWV maps in SR IMAGE references)
			
			for (Attribute a : list.values()) {
				if (a instanceof SequenceAttribute) {
					SequenceAttribute s = (SequenceAttribute)a;
					Iterator i = s.iterator();
					while (i.hasNext()) {
						SequenceItem item = (SequenceItem)(i.next());
						AttributeList itemList = item.getAttributeList();
						if (itemList != null) {
							addToHierarchicalReferencesToSOPInstances(itemList,hierarchicalInstancesBySOPInstanceUID,parentStudyInstanceUID,parentSeriesInstanceUID,false/*topLevelDataSet*/);
						}
					}
				}
			}
		}
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Study: ");
		str.append(studyInstanceUID);
		str.append(", ");
		str.append("Series: ");
		str.append(seriesInstanceUID);
		str.append(", ");
		str.append("Instance: ");
		str.append(sopInstanceUID);
		str.append(", ");
		str.append("Class: ");
		str.append(sopClassUID);
		return str.toString();
	}


}

