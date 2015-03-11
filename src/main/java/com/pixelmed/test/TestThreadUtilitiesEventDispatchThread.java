/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.utils.ThreadUtilities;

import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import junit.framework.*;

public class TestThreadUtilitiesEventDispatchThread extends TestCase {
	
	protected static final int    waitIntervalWhenSleeping = 10;	// in ms

	// constructor to support adding tests to suite ...
	
	public TestThreadUtilitiesEventDispatchThread(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestThreadUtilitiesEventDispatchThread.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestThreadUtilitiesEventDispatchThread");
		
		suite.addTest(new TestThreadUtilitiesEventDispatchThread("TestThreadUtilitiesEventDispatchThread_MainThread"));
		suite.addTest(new TestThreadUtilitiesEventDispatchThread("TestThreadUtilitiesEventDispatchThread_JFrameSubClassThread"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestThreadUtilitiesEventDispatchThread_MainThread() {
		boolean exceptionThrown = false;
		try {
			ThreadUtilities.checkIsEventDispatchThreadElseException();
		}
		catch (RuntimeException e) {
			exceptionThrown = true;
		}
		assertTrue("Not on EDT",exceptionThrown);
	}
	
	private volatile boolean windowHasBeenPainted = false;

	private class JFrameSubClass extends JFrame {
		JFrameSubClass() {
			super();
//System.err.println("JFrameSubClass():");
			boolean exceptionThrown = false;
			try {
				ThreadUtilities.checkIsEventDispatchThreadElseException();
			}
			catch (RuntimeException e) {
				exceptionThrown = true;
			}
			assertTrue("Constructor not on EDT",exceptionThrown);
		}
		
		public void paint(Graphics g) {
			System.err.println("JFrameSubClass.paint():");
			super.paint(g);	// not really necessary for purpose of test
			boolean exceptionThrown = false;
			try {
				try {
					ThreadUtilities.checkIsEventDispatchThreadElseException();
				}
				catch (RuntimeException e) {
					exceptionThrown = true;
				}
				assertTrue("Paint() is on EDT",!exceptionThrown);
			}
			finally {
				windowHasBeenPainted = true;	// need to stop even if assertTrue() fails 
			}
		}

	}
	
	public void TestThreadUtilitiesEventDispatchThread_JFrameSubClassThread() {
		windowHasBeenPainted = false;
		JFrameSubClass f = new JFrameSubClass();
		f.setVisible(true);		// "realizes" the object
//System.err.println("TestThreadUtilitiesEventDispatchThread_JFrameSubClassThread(): back from setVisible");
		try {
			while (!windowHasBeenPainted) {
				Thread.currentThread().sleep(waitIntervalWhenSleeping);
//System.err.println("TestThreadUtilitiesEventDispatchThread_JFrameSubClassThread(): windowHasBeenPainted = "+windowHasBeenPainted);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace(System.err);
		}
	}
	
}
