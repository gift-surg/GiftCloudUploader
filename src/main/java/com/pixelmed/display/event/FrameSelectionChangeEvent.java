/* Copyright (c) 2001-2004, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display.event;

import com.pixelmed.event.Event;
import com.pixelmed.event.EventContext;

/**
 * @author	dclunie
 */
public class FrameSelectionChangeEvent extends Event {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/event/FrameSelectionChangeEvent.java,v 1.3 2004/09/30 12:51:52 dclunie Exp $";

	/***/
	private int index;

	/**
	 * @param	eventContext
	 * @param	index
	 */
	public FrameSelectionChangeEvent(EventContext eventContext,int index) {
		super(eventContext);
		this.index=index;
	}

	/**
	 * @return	the index of the frame selected
	 */
	public int getIndex() { return index; }

	/**
	 * @return	description of the event
	 */
	public String toString() {
		return ("FrameSelectionChangeEvent: eventContext="+getEventContext()+" index="+index);
	}
}

