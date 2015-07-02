/*
 * SessionVariableNames
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/16/14 2:00 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

public final class SessionVariableNames {
	private SessionVariableNames() {}
	
	public static final String PROJECT = "project";
	public static final String SUBJECT = "subject";
	public static final String SESSION_LABEL = "session";
	public static final String PREDEF_SESSION = "predef-session";
	public static final String MODALITY_LABEL = "modalityLabel";
	public static final String VISIT_LABEL = "*visit*";  //asterisks to avoid any problems with the anon script. this prevents visit from becoming a variable.
	public static final String PROTOCOL_LABEL = "*protocol*";  //asterisks to avoid any problems with the anon script. this prevents protocol from becoming a variable.
	public static final String WARN_ON_DUPE_SESSION_LABELS = "warn-on-dupe-session-labels";
	public static final String ALLOW_OVERWRITE_ON_DUPE_SESSION_LABELS = "allow-overwrite-on-dupe-session-labels";
    public static final String ALLOW_APPEND_ON_DUPE_SESSION_LABELS = "allow-append-on-dupe-session-labels";

    public static final String TRACER = "tracer";
    public static final String TRACER_PATH = "xnat:petSessionData/tracer/name";
}
