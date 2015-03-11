/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import java.awt.DisplayMode; 
import java.awt.GraphicsDevice;
import java.awt.Rectangle;

import javax.swing.JFrame;

public class DisplayDeviceArea {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/DisplayDeviceArea.java,v 1.2 2013/02/01 13:53:20 dclunie Exp $";

	GraphicsDevice gd;
	Rectangle bounds;
	JFrame frame;
		
	public DisplayDeviceArea(GraphicsDevice gd,int x,int y,int w,int h) {
		this.gd=gd;
		this.bounds=new Rectangle(x,y,w,h);
		this.frame=null;
	}
		
	public DisplayDeviceArea(GraphicsDevice gd) {
		this.gd=gd;
		DisplayMode dm = gd.getDisplayMode();
		this.bounds = gd.getDefaultConfiguration().getBounds();
		this.frame=null;
	}
		
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("DisplayDeviceArea: ");
		buf.append("GraphicsDevice = "); buf.append(gd);
		buf.append("; bounds ="); buf.append(bounds);
		return buf.toString();
	}
	
	public JFrame getFrame() {
		if (frame == null) {
			frame = new JFrame(gd.getDefaultConfiguration());
			frame.setBounds(bounds);
			frame.setUndecorated(true);
		}
		return frame;
	}
}

