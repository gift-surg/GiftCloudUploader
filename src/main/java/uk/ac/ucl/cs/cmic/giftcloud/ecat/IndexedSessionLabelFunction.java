/*
 * uk.ac.ucl.cs.cmic.giftcloud.ecat.IndexedSessionLabelFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.ecat;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.nrg.ecat.edit.AbstractIndexedLabelFunction;
import org.nrg.ecat.edit.ScriptFunction;

public class IndexedSessionLabelFunction
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
	 */
	@Override
	protected boolean isAvailable(String label) {
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
