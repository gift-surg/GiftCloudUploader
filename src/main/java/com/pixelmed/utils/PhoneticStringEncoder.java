/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.utils;

/**
 * @author	dclunie
 */
public class PhoneticStringEncoder {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/utils/PhoneticStringEncoder.java,v 1.3 2013/02/01 13:53:20 dclunie Exp $";

	protected Class encoderClass;
	protected Object encoder;
	
	public PhoneticStringEncoder() throws PhoneticStringEncoderException {
		try {
			encoderClass = Thread.currentThread().getContextClassLoader().loadClass("org.apache.commons.codec.language.DoubleMetaphone");
			
			Class [] argTypes  = {};
			Object[] argValues = {};
			encoder = encoderClass.getConstructor(argTypes).newInstance(argValues);
		}
		catch (Exception e) {	// may be ClassNotFoundException,NoSuchMethodException,InstantiationException,IllegalAccessException,InvocationTargetException
			throw new PhoneticStringEncoderException("Could not instantiate org.apache.commons.codec.language.DoubleMetaphone - "+e);
		}
	}
	
	public String encode(String s) throws PhoneticStringEncoderException {
		String returnValue = null;
		try {
			Class [] argTypes  = { java.lang.String.class };
			Object[] argValues = { s };
			returnValue = (String)(encoderClass.getMethod("encode",argTypes).invoke(encoder,argValues));
		}
		catch (Exception e) { // may be NullPointerexception, NoSuchMethodException, IllegalAccessException, InvocationTargetException, org.apache.commons.codec.EncoderException
			throw new PhoneticStringEncoderException("Could not invoke org.apache.commons.codec.StringEncoder.encode() - "+e);
		}
		return returnValue;
	}
}
	
