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

import javax.swing.JLabel;

public final class AssignedSessionVariable extends AbstractSessionVariable {
	private final String value;
	private final JLabel label;
	private final boolean hidden;
	
	public AssignedSessionVariable(final String name, final String value, final boolean hidden) {
		super(name);
		this.value = value;
		this.label = new JLabel(value);
		this.hidden = hidden;
	}
	
	public AssignedSessionVariable(final String name, final String value) {
		this(name, value, false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see SessionVariable#getEditor()
	 */
	public JLabel getEditor() { return label; }
	
	/*
	 * (non-Javadoc)
	 * @see SessionVariable#getValue()
	 */
	public String getValue() { return value; }
	
	/*
	 * (non-Javadoc)
	 * @see SessionVariable#getValueMessage()
	 */
	public String getValueMessage() { return null; }
	
	/*
	 * (non-Javadoc)
	 * @see SessionVariable#isHidden()
	 */
	public boolean isHidden() { return hidden; }
	
	/*
	 * (non-Javadoc)
	 * @see SessionVariable#refresh()
	 */
	public void refresh() {}
	
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
