/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.Attribute
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import java.util.Map;
import java.util.WeakHashMap;

public interface Attribute {
	public static class Simple implements Attribute {
		private final static Map<Integer,Attribute> attrs = new WeakHashMap<Integer,Attribute>();
		
		public static Attribute getInstance(final int tag) {
			final Attribute a = attrs.get(tag);
			if (null == a) {
				final Attribute na = new Simple(tag);
				attrs.put(tag, na);
				return na;
			} else {
				return a;
			}
		}
		
		private final int tag;
		
		private Simple(final int tag) { this.tag = tag; }
		
		public int hashCode() { return tag; }
		
		public boolean equals(final Object o) {
			return o instanceof Simple && tag == ((Simple)o).tag;
		}
	}
}
