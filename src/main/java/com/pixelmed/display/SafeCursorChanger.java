/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import com.pixelmed.utils.ThreadUtilities;

import java.awt.Component;
import java.awt.Cursor;

import java.lang.reflect.InvocationTargetException;

public class SafeCursorChanger {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/SafeCursorChanger.java,v 1.1 2013/01/20 20:30:37 dclunie Exp $";
	
	protected Cursor was;
	protected Component component;
	
	public SafeCursorChanger(Component component) {
		this.component = component;
	}
	
	public class SafeCursorGetterThread implements Runnable {
		protected Cursor cursor;
		
		public SafeCursorGetterThread() {
		}
		
		public void run() {
			cursor = component.getCursor();
		}
		
		public Cursor getCursor() { return cursor; }
	}
	
	public class SafeCursorSetterThread implements Runnable {
		protected Cursor cursor;
		
		public SafeCursorSetterThread(Cursor cursor) {
			this.cursor = cursor;
		}
		
		public void run() {
			component.setCursor(cursor);
		}
	}
	
	public void saveCursor() {
		if (java.awt.EventQueue.isDispatchThread()) {
			was = component.getCursor();
		}
		else {
			SafeCursorGetterThread getter = new SafeCursorGetterThread();
			try {
				java.awt.EventQueue.invokeAndWait(getter);		// NB. need to wait, since we want the value, and also can't be called on EDT thread
				was = getter.getCursor();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public void setWaitCursor() {
		if (java.awt.EventQueue.isDispatchThread()) {
			component.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		else {
			java.awt.EventQueue.invokeLater(new SafeCursorSetterThread(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)));
		}
	}
	
	public void restoreCursor() {
		if (java.awt.EventQueue.isDispatchThread()) {
			component.setCursor(was);
		}
		else {
			java.awt.EventQueue.invokeLater(new SafeCursorSetterThread(was));
		}
	}
}