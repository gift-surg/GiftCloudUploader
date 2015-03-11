/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.MapEntity
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract class MapEntity implements Entity {
	private final Map<Attribute,Object> m = new HashMap<Attribute,Object>();

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ucl.cs.cmic.giftcloud.dicom.Entity#get(uk.ac.ucl.cs.cmic.giftcloud.dicom.Attribute)
	 */
	public Object get(final Attribute a) {
		return m.get(a);
	}
	
	public Object get(final int tag) {
		return m.get(Attribute.Simple.getInstance(tag));
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ucl.cs.cmic.giftcloud.dicom.Entity#getAttributes()
	 */
	public Map<Attribute,Object> getAttributes() {
		return Collections.unmodifiableMap(m);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return m.hashCode();
	}
	
	protected final Object put(final Attribute attr, final Object value) {
		return m.put(attr, value);
	}
	
	protected final Object put(final int tag, final Object value) {
		return m.put(Attribute.Simple.getInstance(tag), value);
	}
}
