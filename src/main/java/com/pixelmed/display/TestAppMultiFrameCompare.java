/* Copyright (c) 2001-2005, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import java.awt.GridLayout; 
import java.awt.Color; 
import java.awt.Container; 
import java.awt.Dimension;
import java.io.FileInputStream; 
import javax.swing.JComponent; 
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.pixelmed.event.ApplicationEventDispatcher; 
import com.pixelmed.event.Event; 
import com.pixelmed.event.EventContext; 
import com.pixelmed.event.SelfRegisteringListener; 
import com.pixelmed.display.event.FrameSelectionChangeEvent; 
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.GeometryOfVolumeFromAttributeList;
import com.pixelmed.geometry.GeometryOfVolume;

/**
 * @author	dclunie
 */
class TestAppMultiFrameCompare extends ApplicationFrame {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/TestAppMultiFrameCompare.java,v 1.9 2013/02/20 19:05:52 dclunie Exp $";
	
	private static final int findClosestPositionMatch(double source,double[] target) {
		double lowestDifference = 999999999.0;
		int lowestIndex=-1;
		for (int i=0; i<target.length; ++i) {
			double difference = Math.abs(source-target[i]);
			if (difference < lowestDifference) {
				lowestDifference=difference;
				lowestIndex=i;
			}
		}
System.err.println("findClosestPositionMatch: source "+source+" target "+target[lowestIndex]+" difference "+lowestDifference);
		return lowestIndex;
	}
	
	double[][] distancesAlongNormal = new double[2][];
	
	EventContext eventContextOfSingleImagePanel[];

	// implement FrameSelectionChangeListener to respond to events from self or elsewhere ...
	
	private OurFrameSelectionChangeListener ourFrameSelectionChangeListener;

	class OurFrameSelectionChangeListener extends SelfRegisteringListener {
	
		private int whichSource;
		private int whichTarget;
		private EventContext targetEventContext;
		private int lastIndex=-1;

		public OurFrameSelectionChangeListener(EventContext sourceEventContext,EventContext targetEventContext) {
			super("com.pixelmed.display.event.FrameSelectionChangeEvent",sourceEventContext);
//System.err.println("OurFrameSelectionChangeListener():");
			// if there were more than one could look up a hash table, but for now ...
			if (sourceEventContext == eventContextOfSingleImagePanel[0]) {
				// assert targetEventContext == eventContextOfSingleImagePanel[0]
				whichSource=0;
				whichTarget=1;
			}
			else {
				// assert sourceEventContext == eventContextOfSingleImagePanel[1]
				// assert targetEventContext == eventContextOfSingleImagePanel[0]
				whichSource=1;
				whichTarget=0;
			}
			this.targetEventContext=targetEventContext;
		}
		
		/**
		 * @param	e
		 */
		public void changed(Event e) {
			FrameSelectionChangeEvent fse = (FrameSelectionChangeEvent)e;
//System.err.println("OurFrameSelectionChangeListener.changed(): source="+whichSource+"("+fse+") target="+whichTarget+" ("+targetEventContext+")");
			int srcImageIndex = fse.getIndex();
System.err.println("source slice index "+srcImageIndex);
			if (srcImageIndex != lastIndex) {	// don't keep doing it if already done
				lastIndex=srcImageIndex;
				// assert fse.getEventContext() == sourceEventContext;
				// assert fse.getEventContext() == eventContextOfSingleImagePanel[whichSource];
				double sourcePosition = distancesAlongNormal[whichSource][srcImageIndex];
System.err.println("source slice geometry distance cached "+sourcePosition);
				int match = findClosestPositionMatch(sourcePosition,distancesAlongNormal[whichTarget]);
System.err.println("target slice match index "+match);
				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(
					new FrameSelectionChangeEvent(targetEventContext/*eventContextOfSingleImagePanel[whichTarget]*/,match));
			}
		}
		
	}

	/**
	 */
	void addInterleavedFrameSelectionListeners() {
			new OurFrameSelectionChangeListener(eventContextOfSingleImagePanel[0],eventContextOfSingleImagePanel[1]);
			new OurFrameSelectionChangeListener(eventContextOfSingleImagePanel[1],eventContextOfSingleImagePanel[0]);
	}

	/**
	 * @param	arg
	 */
	public static void main(String arg[]) { 
		TestAppMultiFrameCompare af = new TestAppMultiFrameCompare();
		
		int imagesPerCol=1;
		int imagesPerRow=2;
		JPanel multiPanel = new JPanel();
		multiPanel.setLayout(new GridLayout(imagesPerCol,imagesPerRow));
		multiPanel.setBackground(Color.black);
		af.eventContextOfSingleImagePanel = new EventContext[2];
		for (int i=0; i<2; ++i) {
//System.err.println("looping for next image");
			try {
				DicomInputStream in = new DicomInputStream(new FileInputStream(arg[i]));
				AttributeList list = new AttributeList();
				list.read(in);
				SourceImage sImg = new SourceImage(list);
				GeometryOfVolume geometry = new GeometryOfVolumeFromAttributeList(list);
				af.distancesAlongNormal[i] = geometry.getDistanceAlongNormalFromOrigin();
				af.eventContextOfSingleImagePanel[i] = new EventContext(Integer.toString(i));
//System.err.println("making new SingleImagePanel");
				SingleImagePanel ip = new SingleImagePanel(sImg,
					af.eventContextOfSingleImagePanel[i]);
				ip.setPreferredSize(new Dimension(sImg.getWidth(),sImg.getHeight()));
//System.err.println("adding to multiPanel");
				multiPanel.add(ip);
			} catch (Exception e) {
				System.err.println(e);
				e.printStackTrace(System.err);
				System.exit(0);
			}
		}
		af.addInterleavedFrameSelectionListeners();

		JScrollPane scrollPane = new JScrollPane(multiPanel);

		Container content = af.getContentPane();
		content.setLayout(new GridLayout(1,1));
		content.add(scrollPane);

		af.pack();

		int frameHeight=scrollPane.getHeight()+24;
		if (frameHeight>1024) frameHeight=1024;
		int frameWidth=scrollPane.getWidth()+24;
		if (frameWidth>1280) frameWidth=1280;
		af.setSize(frameWidth,frameHeight);
		af.setVisible(true);
       } 

}








