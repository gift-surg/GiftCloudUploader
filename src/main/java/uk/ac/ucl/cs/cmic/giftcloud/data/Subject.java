/*
 * Subject
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

import com.google.common.base.Objects;


public final class Subject {
	private final String label, id;

	public Subject(final String label, final String id) {
		this.label = label;
		this.id = id;
	}

	public String getLabel() { return label; }

	public String getId() { return id; }

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object o) {
		return o instanceof Subject
		&& Objects.equal(label, ((Subject)o).label)
		&& Objects.equal(id, ((Subject)o).id);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() { return Objects.hashCode(label, id); }

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() { return label; }
}
