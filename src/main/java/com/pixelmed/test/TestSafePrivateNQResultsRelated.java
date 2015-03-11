/* Copyright (c) 2001-2012, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestSafePrivateNQResultsRelated extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSafePrivateNQResultsRelated(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSafePrivateNQResultsRelated.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSafePrivateNQResultsRelated");
		
		suite.addTest(new TestSafePrivateNQResultsRelated("TestSafePrivateNQResultsRelated_FromTag"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private String headerOwner = "NQHeader";
	
	private AttributeTag[] safeHeaderTags = {
		new AttributeTag(0x0099,0x1001),	// Version
		new AttributeTag(0x0099,0x1004),	// Return Code
		new AttributeTag(0x0099,0x1005),	// Return Message
		new AttributeTag(0x0099,0x1010),	// MI
		new AttributeTag(0x0099,0x1020),	// Units
		new AttributeTag(0x0099,0x1021)		// ICV
	};
	
	private AttributeTag[] unsafeHeaderTags = {
		new AttributeTag(0x0099,0x1002),	// Analyzed Series UID
		new AttributeTag(0x0099,0x1003)		// License
	};
	
	private String leftOwner = "NQLeft";

	private AttributeTag[] safeLeftTags = {
		new AttributeTag(0x0199,0x1001),	// Left Cortical White Matter
		new AttributeTag(0x0199,0x1002),	// Left Cortical Gray Matter
		new AttributeTag(0x0199,0x1003),	// Left 3rd Ventricle
		new AttributeTag(0x0199,0x1004),	// Left 4th Ventricle
		new AttributeTag(0x0199,0x1005),	// Left 5th Ventricle
		new AttributeTag(0x0199,0x1006),	// Left Lateral Ventricle
		new AttributeTag(0x0199,0x1007),	// Left Inferior Lateral Ventricle
		new AttributeTag(0x0199,0x1008),	// Left Inferior CSF
		new AttributeTag(0x0199,0x1009),	// Left Cerebellar White Matter
		new AttributeTag(0x0199,0x100a),	// Left Cerebellar Gray Matter
		new AttributeTag(0x0199,0x100b),	// Left Hippocampus
		new AttributeTag(0x0199,0x100c),	// Left Amygdala
		new AttributeTag(0x0199,0x100d),	// Left Thalamus
		new AttributeTag(0x0199,0x100e),	// Left Caudate
		new AttributeTag(0x0199,0x100f),	// Left Putamen
		new AttributeTag(0x0199,0x1010),	// Left Pallidum
		new AttributeTag(0x0199,0x1011),	// Left Ventral Diencephalon
		new AttributeTag(0x0199,0x1012),	// Left Nucleus Accumbens
		new AttributeTag(0x0199,0x1013),	// Left Brain Stem
		new AttributeTag(0x0199,0x1014),	// Left Exterior CSF
		new AttributeTag(0x0199,0x1015),	// Left WM Hypo
		new AttributeTag(0x0199,0x1016),	// Left Other
		new AttributeTag(0x0199,0x1017),	// Left Cortex Unkown
		new AttributeTag(0x0199,0x1018),	// Left Cortex Bankssts
		new AttributeTag(0x0199,0x1019),	// Left Cortex Caudal Anterior Cingulate
		new AttributeTag(0x0199,0x101a),	// Left Cortex Caudal Middle Frontal
		new AttributeTag(0x0199,0x101b),	// Left Cortex Corpus Callosum
		new AttributeTag(0x0199,0x101c),	// Left Cortex Cuneus
		new AttributeTag(0x0199,0x101d),	// Left Cortex Entorhinal
		new AttributeTag(0x0199,0x101e),	// Left Cortex Fusiform
		new AttributeTag(0x0199,0x101f),	// Left Cortex Inferior Parietal
		new AttributeTag(0x0199,0x1020),	// Left Cortex Inferior Temporal
		new AttributeTag(0x0199,0x1021),	// Left Cortex Isthmus Cingulate
		new AttributeTag(0x0199,0x1022),	// Left Cortex Lateral Occipital
		new AttributeTag(0x0199,0x1023),	// Left Cortex Lateral Orbito Frontal
		new AttributeTag(0x0199,0x1024),	// Left Cortex Lingual
		new AttributeTag(0x0199,0x1025),	// Left Cortex Medial Orbito Frontal
		new AttributeTag(0x0199,0x1026),	// Left Cortex Middle Temporal
		new AttributeTag(0x0199,0x1027),	// Left Cortex Parahippocampal
		new AttributeTag(0x0199,0x1028),	// Left Cortex Paracentral
		new AttributeTag(0x0199,0x1029),	// Left Cortex Pars Opercularis
		new AttributeTag(0x0199,0x102a),	// Left Cortex Pars Orbitalis
		new AttributeTag(0x0199,0x102b),	// Left Cortex Pars Triangularis
		new AttributeTag(0x0199,0x102c),	// Left Cortex Pericalcarine
		new AttributeTag(0x0199,0x102d),	// Left Cortex Postcentral
		new AttributeTag(0x0199,0x102e),	// Left Cortex Posterior Cingulate
		new AttributeTag(0x0199,0x102f),	// Left Cortex Precentral
		new AttributeTag(0x0199,0x1030),	// Left Cortex Precuneus
		new AttributeTag(0x0199,0x1031),	// Left Cortex Rostral Anterior Cingulate
		new AttributeTag(0x0199,0x1032),	// Left Cortex Rostral Middle Frontal
		new AttributeTag(0x0199,0x1033),	// Left Cortex Superior Frontal
		new AttributeTag(0x0199,0x1034),	// Left Cortex Superior Parietal
		new AttributeTag(0x0199,0x1035),	// Left Cortex Superior Temporal
		new AttributeTag(0x0199,0x1036),	// Left Cortex Supramarginal
		new AttributeTag(0x0199,0x1037),	// Left Cortex Frontal Pole
		new AttributeTag(0x0199,0x1038),	// Left Cortex Temporal Pole
		new AttributeTag(0x0199,0x1039),	// Left Cortex Transvere Temporal
		new AttributeTag(0x0199,0x103a)		// Left Meningie
	};
		
	private String rightOwner = "NQRight";

	private AttributeTag[] safeRightTags = {
		new AttributeTag(0x0299,0x1001),	// Right Cortical White Matter
		new AttributeTag(0x0299,0x1002),	// Right Cortical Gray Matter
		new AttributeTag(0x0299,0x1003),	// Right 3rd Ventricle
		new AttributeTag(0x0299,0x1004),	// Right 4th Ventricle
		new AttributeTag(0x0299,0x1005),	// Right 5th Ventricle
		new AttributeTag(0x0299,0x1006),	// Right Lateral Ventricle
		new AttributeTag(0x0299,0x1007),	// Right Inferior Lateral Ventricle
		new AttributeTag(0x0299,0x1008),	// Right Inferior CSF
		new AttributeTag(0x0299,0x1009),	// Right Cerebellar White Matter
		new AttributeTag(0x0299,0x100a),	// Right Cerebellar Gray Matter
		new AttributeTag(0x0299,0x100b),	// Right Hippocampus
		new AttributeTag(0x0299,0x100c),	// Right Amygdala
		new AttributeTag(0x0299,0x100d),	// Right Thalamus
		new AttributeTag(0x0299,0x100e),	// Right Caudate
		new AttributeTag(0x0299,0x100f),	// Right Putamen
		new AttributeTag(0x0299,0x1010),	// Right Pallidum
		new AttributeTag(0x0299,0x1011),	// Right Ventral Diencephalon
		new AttributeTag(0x0299,0x1012),	// Right Nucleus Accumbens
		new AttributeTag(0x0299,0x1013),	// Right Brain Stem
		new AttributeTag(0x0299,0x1014),	// Right Exterior CSF
		new AttributeTag(0x0299,0x1015),	// Right WM Hypo
		new AttributeTag(0x0299,0x1016),	// Right Other
		new AttributeTag(0x0299,0x1017),	// Right Cortex Unkown
		new AttributeTag(0x0299,0x1018),	// Right Cortex Bankssts
		new AttributeTag(0x0299,0x1019),	// Right Cortex Caudal Anterior Cingulate
		new AttributeTag(0x0299,0x101a),	// Right Cortex Caudal Middle Frontal
		new AttributeTag(0x0299,0x101b),	// Right Cortex Corpus Callosum
		new AttributeTag(0x0299,0x101c),	// Right Cortex Cuneus
		new AttributeTag(0x0299,0x101d),	// Right Cortex Entorhinal
		new AttributeTag(0x0299,0x101e),	// Right Cortex Fusiform
		new AttributeTag(0x0299,0x101f),	// Right Cortex Inferior Parietal
		new AttributeTag(0x0299,0x1020),	// Right Cortex Inferior Temporal
		new AttributeTag(0x0299,0x1021),	// Right Cortex Isthmus Cingulate
		new AttributeTag(0x0299,0x1022),	// Right Cortex Lateral Occipital
		new AttributeTag(0x0299,0x1023),	// Right Cortex Lateral Orbito Frontal
		new AttributeTag(0x0299,0x1024),	// Right Cortex Lingual
		new AttributeTag(0x0299,0x1025),	// Right Cortex Medial Orbito Frontal
		new AttributeTag(0x0299,0x1026),	// Right Cortex Middle Temporal
		new AttributeTag(0x0299,0x1027),	// Right Cortex Parahippocampal
		new AttributeTag(0x0299,0x1028),	// Right Cortex Paracentral
		new AttributeTag(0x0299,0x1029),	// Right Cortex Pars Opercularis
		new AttributeTag(0x0299,0x102a),	// Right Cortex Pars Orbitalis
		new AttributeTag(0x0299,0x102b),	// Right Cortex Pars Triangularis
		new AttributeTag(0x0299,0x102c),	// Right Cortex Pericalcarine
		new AttributeTag(0x0299,0x102d),	// Right Cortex Postcentral
		new AttributeTag(0x0299,0x102e),	// Right Cortex Posterior Cingulate
		new AttributeTag(0x0299,0x102f),	// Right Cortex Precentral
		new AttributeTag(0x0299,0x1030),	// Right Cortex Precuneus
		new AttributeTag(0x0299,0x1031),	// Right Cortex Rostral Anterior Cingulate
		new AttributeTag(0x0299,0x1032),	// Right Cortex Rostral Middle Frontal
		new AttributeTag(0x0299,0x1033),	// Right Cortex Superior Frontal
		new AttributeTag(0x0299,0x1034),	// Right Cortex Superior Parietal
		new AttributeTag(0x0299,0x1035),	// Right Cortex Superior Temporal
		new AttributeTag(0x0299,0x1036),	// Right Cortex Supramarginal
		new AttributeTag(0x0299,0x1037),	// Right Cortex Frontal Pole
		new AttributeTag(0x0299,0x1038),	// Right Cortex Temporal Pole
		new AttributeTag(0x0299,0x1039),	// Right Cortex Transvere Temporal
		new AttributeTag(0x0299,0x103a)		// Right Meningie
	};

	public void TestSafePrivateNQResultsRelated_FromTag() throws Exception {
		for (AttributeTag t : safeHeaderTags) {
			assertTrue("Checking "+t+" is safe",ClinicalTrialsAttributes.isSafePrivateAttribute(headerOwner,t));
		}
		for (AttributeTag t : unsafeHeaderTags) {
			assertTrue("Checking "+t+" is not safe",!ClinicalTrialsAttributes.isSafePrivateAttribute(headerOwner,t));
		}
		for (AttributeTag t : safeLeftTags) {
			assertTrue("Checking "+t+" is safe",ClinicalTrialsAttributes.isSafePrivateAttribute(leftOwner,t));
		}
		for (AttributeTag t : safeRightTags) {
			assertTrue("Checking "+t+" is safe",ClinicalTrialsAttributes.isSafePrivateAttribute(rightOwner,t));
		}
	}
	
}
