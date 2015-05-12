/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

public class NetworkApplicationProperties {

	public static final String StudyRootQueryModel = "STUDYROOT";
	public static final String PatientRootQueryModel = "PATIENTROOT";
	public static final String PatientStudyOnlyQueryModel = "PATIENTSTUDYONLY";
	
	/**
	 * <p>Is the model Study Root ?</p>
	 *
	 * @param	model	the string value describing the model, as used in the query model remote AE property
	 * @return		true if Study Root
	 */
	public static final boolean isStudyRootQueryModel(String model) { return model != null && model.equals(StudyRootQueryModel); }
	
}

