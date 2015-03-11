/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display.event;

import com.pixelmed.event.EventContext;

public class WellKnownContext {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/event/WellKnownContext.java,v 1.2 2013/02/01 13:53:20 dclunie Exp $";

	static public EventContext MAINPANEL                   = new EventContext("MAINPANEL");
	static public EventContext REFERENCEPANEL              = new EventContext("REFERENCEPANEL");
	static public EventContext SPECTROSCOPYBACKGROUNDIMAGE = new EventContext("SPECTROSCOPYBACKGROUNDIMAGE");
}
