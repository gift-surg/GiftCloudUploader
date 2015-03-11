/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.Entity
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import java.util.Collection;
import java.util.Map;

public interface Entity {
	Collection<Study> getStudies();
	Collection<Series> getSeries();
	Map<Attribute,Object> getAttributes();
	Object get(Attribute a);
}
