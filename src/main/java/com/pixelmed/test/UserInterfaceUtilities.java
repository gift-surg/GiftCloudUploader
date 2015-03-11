/* Copyright (c) 2001-2010, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.event.WindowEvent;
import java.awt.event.AWTEventListener;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;

import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserInterfaceUtilities {

	protected static void dumpAllOwnedWindows(Window w,PrintStream stream,String indentSoFar,String indentOne) {
		stream.println(indentSoFar+w);
		String indentChild = indentSoFar+indentOne;
		dumpAllOwnedWindows(w.getOwnedWindows(),stream,indentChild,indentOne);
	}


	protected static void dumpAllOwnedWindows(Window[] windows,PrintStream stream,String indentSoFar,String indentOne) {
		for (Window window : windows) {
			dumpAllOwnedWindows(window,stream,indentSoFar,indentOne);
		}
	}

	protected static void dumpAllOwnedWindows(PrintStream stream,String indentSoFar,String indentOne) {
		String indentChild = indentSoFar+indentOne;
		Frame[] frames = Frame.getFrames();
		for (Frame frame: frames) {
			stream.println(indentSoFar+frame);
			dumpAllOwnedWindows(frame.getOwnedWindows(),stream,indentChild,indentOne);
		}
	}

	public static void dumpAllOwnedWindows(PrintStream stream) {
		dumpAllOwnedWindows(stream,"","\t");
	}

	protected static void dumpRootPaneAndChildren(PrintStream stream,String indentSoFar,String indentOne) {
		Set<JRootPane> done = new HashSet<JRootPane>();
		Frame[] frames = Frame.getFrames();
		for (Frame frame: frames) {
			if (frame instanceof JFrame) {
				JRootPane rootPane = ((JFrame)frame).getRootPane();
				if (!done.contains(rootPane)) {
					done.add(rootPane);
					dumpComponentAndChildren(rootPane,stream,indentSoFar,indentOne);
				}
			}
		}
	}

	public static void dumpRootPaneAndChildren(PrintStream stream) {
		dumpRootPaneAndChildren(stream,"","\t");
	}

	protected static void dumpComponentAndChildren(Component c,PrintStream stream,String indentSoFar,String indentOne) {
		stream.println(indentSoFar+c);
		if (c instanceof Container) {
			String indentChild = indentSoFar+indentOne;
			Component[] children = ((Container)c).getComponents();
			for (Component child: children) {
				dumpComponentAndChildren(child,stream,indentChild,indentOne);
			}
		}
	}

	public static void dumpComponentAndChildren(Component c,PrintStream stream) {
		dumpComponentAndChildren(c,stream,"","\t");
	}
	

	protected static void dumpAllFramesAndChildren(PrintStream stream,String indentSoFar,String indentOne) {
		Frame[] frames = Frame.getFrames();
		for (Frame frame: frames) {
			dumpComponentAndChildren(frame,stream,indentSoFar,indentOne);
		}
	}

	public static void dumpAllFramesAndChildren(PrintStream stream) {
		dumpAllFramesAndChildren(stream,"","\t");
	}
	
	protected static void findComponentsOfClassWithTextValue(ArrayList<Component> found,Component c,String className,String textValue) {
		if (className.equals(c.getClass().getName()) && c instanceof AbstractButton && textValue.equals(((AbstractButton)c).getText())) {
			found.add(c);
		}
		else if (c instanceof Container) {
			Component[] children = ((Container)c).getComponents();
			for (Component child: children) {
				findComponentsOfClassWithTextValue(found,child,className,textValue);
			}
		}
	}

	public static Component[] findComponentsOfClassWithTextValue(Component parent,String className,String textValue) {
		ArrayList<Component> found = new ArrayList<Component>();
		findComponentsOfClassWithTextValue(found,parent,className,textValue);
		return found.toArray(new Component[] {});
	}

	public static Component[] findComponentsOfClassWithTextValue(String className,String textValue) {
		ArrayList<Component> found = new ArrayList<Component>();
		Frame[] frames = Frame.getFrames();
		for (Frame frame: frames) {
			findComponentsOfClassWithTextValue(found,frame,className,textValue);
		}
		return found.toArray(new Component[] {});
	}
	
	protected static void findComponentsOfClass(ArrayList<Component> found,Component c,String className) {
		if (className.equals(c.getClass().getName())) {
			found.add(c);
		}
		else if (c instanceof Container) {
			Component[] children = ((Container)c).getComponents();
			for (Component child: children) {
				findComponentsOfClass(found,child,className);
			}
		}
	}

	public static Component[] findComponentsOfClass(Component parent,String className) {
		ArrayList<Component> found = new ArrayList<Component>();
		findComponentsOfClass(found,parent,className);
		return found.toArray(new Component[] {});
	}

	public static Component[] findComponentsOfClass(String className) {
		ArrayList<Component> found = new ArrayList<Component>();
		Frame[] frames = Frame.getFrames();
		for (Frame frame: frames) {
			findComponentsOfClass(found,frame,className);
		}
		return found.toArray(new Component[] {});
	}
	
	// Don't seem to be able to track dialogs like JFileChooser from RootPane or all frames getOwnedWindows(),
	// so track opening and closing of windows with listener ... got this trick from source code of
	// org.fest.swing.hierarchy.TransientWindowListener

	protected static Map<String,JDialog> openDialogs = new HashMap<String,JDialog>();

	public static void registerWindowOpeningAndClosingListenerToTrackDialogs() {
//System.err.println("UserInterfaceUtilities.registerWindowOpeningAndClosingListenerToTrackDialogs():");
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		AWTEventListener listener = new AWTEventListener() {
			public void eventDispatched(java.awt.AWTEvent event) {
//System.err.println("UserInterfaceUtilities.AWTEventListener(): event = "+event);
				int id = event.getID();
				Window window = ((WindowEvent)event).getWindow();
				if (window instanceof JDialog) {
					JDialog dialog = (JDialog)window;
					String title = dialog.getTitle();
					if (id == WindowEvent.WINDOW_OPENED) {
//System.err.println("UserInterfaceUtilities.AWTEventListener(): WINDOW_OPENED "+dialog);
						openDialogs.put(title,dialog);
					}
					else if (id == WindowEvent.WINDOW_CLOSED) {
//System.err.println("UserInterfaceUtilities.AWTEventListener(): WINDOW_CLOSED "+dialog);
						openDialogs.remove(title);
					}
				}
			}
		};
		toolkit.addAWTEventListener(listener,AWTEvent.WINDOW_EVENT_MASK);
	}
	
	public static JDialog getOpenDialogByTitle(String title) {
		return openDialogs.get(title);
	}
	
}

