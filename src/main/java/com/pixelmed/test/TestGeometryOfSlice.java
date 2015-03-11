/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.geometry.*;

import junit.framework.*;

public class TestGeometryOfSlice extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestGeometryOfSlice(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestGeometryOfSlice.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestGeometryOfSlice");
		
		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_3D2DConversions1"));
		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_3D2DConversions2"));
		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_3D2DConversions3"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	protected boolean epsilonEquals(double value1,double value2,double epsilon) {
		return Math.abs(value1-value2) < epsilon;
	}

	protected void check3D2DConversion(GeometryOfSlice geometry,double column,double row,double expectX,double expectY,double expectZ) {
		double[] location3D = geometry.lookupImageCoordinate(column,row);
		//System.err.println("check3D2DConversion(): lookup 2d ("+column+","+row+") returns 3d ("+location3D[0]+","+location3D[1]+","+location3D[2]+")");
		assertTrue("Lookup 2d("+column+","+row+")",epsilonEquals(expectX,location3D[0],0.0001d));
		assertTrue("Lookup 2d("+column+","+row+")",epsilonEquals(expectY,location3D[1],0.0001d));
		assertTrue("Lookup 2d("+column+","+row+")",epsilonEquals(expectZ,location3D[2],0.0001d));
			
		double[] roundTripLocation2D = geometry.lookupImageCoordinate(location3D);
		//System.err.println("check3D2DConversion(): lookup 2d ("+column+","+row+") round trip returns 2d ("+roundTripLocation2D[0]+","+roundTripLocation2D[1]+")");
		assertTrue("Round trip 2d("+column+","+row+") column",epsilonEquals(column,roundTripLocation2D[0],0.0001d));
		assertTrue("Round trip 2d("+column+","+row+") row",   epsilonEquals(   row,roundTripLocation2D[1],0.0001d));
	}
	
	public void TestGeometryOfSlice_3D2DConversions1() throws Exception {
		double[] rowArray          = { 1, 0, 0 };
		double[] columnArray       = { 0, 1, 0 };
		double[] tlhcArray         = { 1, 1, 1 };	// set the X and Y origins to 1 to match the center of the voxel of TLHC "pixel number" 1
		double[] voxelSpacingArray = { 1, 1, 1 };
		double   sliceThickness    =   1;
		double[] dimensions        = { 512, 512, 1 };
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		check3D2DConversion(geometry,0d,    0d,      0.5d,  0.5d,1.0d);	// expectZ will always just be the specified Z origin
		check3D2DConversion(geometry,0.5d,  0.5d,    1.0d,  1.0d,1.0d);
		check3D2DConversion(geometry,1d,    1d,      1.5d,  1.5d,1.0d);
		check3D2DConversion(geometry,511d,  511d,  511.5d,511.5d,1.0d);
		check3D2DConversion(geometry,511.5d,511.5d,512.0d,512.0d,1.0d);
		check3D2DConversion(geometry,512d,  512d,  512.5d,512.5d,1.0d);
	}
	
	public void TestGeometryOfSlice_3D2DConversions2() throws Exception {
		double[] rowArray          = { 1, 0, 0 };
		double[] columnArray       = { 0, 1, 0 };
		double[] tlhcArray         = { 0, 0, 0 };
		double[] voxelSpacingArray = { 1, 1, 1 };
		double   sliceThickness    =   1;
		double[] dimensions        = { 512, 512, 1 };
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		check3D2DConversion(geometry,0d,    0d,     -0.5d, -0.5d,0.0d);	// expectZ will always just be the specified Z origin
		check3D2DConversion(geometry,0.5d,  0.5d,    0.0d,  0.0d,0.0d);
		check3D2DConversion(geometry,1d,    1d,      0.5d,  0.5d,0.0d);
		check3D2DConversion(geometry,511d,  511d,  510.5d,510.5d,0.0d);
		check3D2DConversion(geometry,511.5d,511.5d,511.0d,511.0d,0.0d);
		check3D2DConversion(geometry,512d,  512d,  511.5d,511.5d,0.0d);
	}
	
	public void TestGeometryOfSlice_3D2DConversions3() throws Exception {
		double[] rowArray          = { 1, 0, 0 };
		double[] columnArray       = { 0, 1, 0 };
		double[] tlhcArray         = { 0, 0, 0 };
		double[] voxelSpacingArray = { 0.4d, 0.4d, 0.4d };
		double   sliceThickness    =   1;
		double[] dimensions        = { 512, 512, 1 };
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		check3D2DConversion(geometry,0d,    0d,     -0.2d, -0.2d,0.0d);
		check3D2DConversion(geometry,0.5d,  0.5d,    0.0d,  0.0d,0.0d);
		check3D2DConversion(geometry,1d,    1d,      0.2d,  0.2d,0.0d);
		check3D2DConversion(geometry,511d,  511d,  204.2d,204.2d,0.0d);
		check3D2DConversion(geometry,511.5d,511.5d,204.4d,204.4d,0.0d);
		check3D2DConversion(geometry,512d,  512d,  204.6d,204.6d,0.0d);
	}
}
