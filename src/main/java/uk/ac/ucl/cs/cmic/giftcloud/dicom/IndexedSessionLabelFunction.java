/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.IndexedSessionLabelFunction
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.nrg.dcm.edit.AbstractIndexedLabelFunction;
import org.nrg.dcm.edit.ScriptFunction;

public final class IndexedSessionLabelFunction
extends AbstractIndexedLabelFunction implements ScriptFunction {
	private final Future<Map<String,String>> sessions;
	
	/**
	 * 
	 */
	public IndexedSessionLabelFunction(final Future<Map<String,String>> sessions) {
		this.sessions = sessions;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.dcm.edit.AbstractIndexedLabelFunction#isAvailable(java.lang.String)
	 * This returns true in the case where the session label retrieval failed; this is
	 * sort of broken but prevents an ugly infinite loop situation.
	 */
	protected boolean isAvailable(final String label) {
		try {
			final Map<String,String> m = sessions.get();
			return !m.containsKey(label) && !m.containsValue(label);
		} catch (InterruptedException e) {
			return true;
		} catch (ExecutionException e) {
			return true;
		}
	}
}
