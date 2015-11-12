/*
 * AssignedSessionVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

public final class AssignedSessionVariable extends AbstractSessionVariable {
	private final String value;

	public AssignedSessionVariable(final String name, final String value) {
		super(name);
		this.value = value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see SessionVariable#getValue()
	 */
	public String getValue() { return value; }

	/*
	 * (non-Javadoc)
	 * @see SessionVariable#setValue(java.lang.String)
	 */
	public String setValue(final String v) {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" ").append(getName()).append(" = ").append(value);
		return sb.toString();
	}
}
