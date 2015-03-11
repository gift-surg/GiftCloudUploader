/*
 * uk.ac.ucl.cs.cmic.giftcloud.util.JSEval
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.util;

import netscape.javascript.JSObject;

import java.util.concurrent.Callable;

public final class JSEval implements Callable<Object> {
	private final JSObject context;
	private final String code;
	
	public JSEval(final JSObject context, final String code) {
		this.context = context;
		this.code = code;
	}
	
	public Object call() {
		return context.eval(code);
	}
}
