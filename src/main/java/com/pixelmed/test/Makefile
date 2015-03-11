PATHTOROOT = ../../..

OBJS = \
TestCodingScheme_AllTests.class \
TestCodingSchemeIdentification.class \
TestCommonDoseObserverContext.class \
TestCompositeInstanceContext_AllTests.class \
TestCompositeInstanceContext.class \
TestCTAcquisitionParameters.class \
TestCTDose_AllTests.class \
TestCTDose.class \
TestCTDoseAcquisition.class \
TestCTIrradiationEventDataFromImages.class \
TestCTScanType.class \
TestCTPhantomType.class \
TestDeviceParticipant.class \
TestPersonParticipant.class \
TestRecordingDeviceObserverContext.class \
TestRoleInOrganization.class \
TestRoleInProcedure.class \
TestScanRange.class \
TestScopeOfDoseAccummulation.class \
TestSourceOfDoseInformation.class \
TestSafePrivate_AllTests.class \
TestSafePrivatePhilipsPETRelated.class \
TestSafePrivatePhilipsDoseRelated.class \
TestSafePrivateGEDoseRelated.class \
TestSafePrivateGEPACSRelated.class \
TestSafePrivateGEMRRelated.class \
TestSafePrivateNQResultsRelated.class \
TestAttributeList_AllTests.class \
TestPrivateCreatorValueRepresentation.class \
TestContentItem_AllTests.class \
TestCodeContentItemValueMatching.class \
TestCharacterSet_AllTests.class \
TestCharacterSetStringAttribute.class \
TestCharacterSetTextAttribute.class \
TestCleaner_AllTests.class \
TestCleanerReceiveAndClean.class \
TestAnatomy_AllTests.class \
TestAnatomyConcept.class \
TestAnatomyCombined.class \
TestAnatomyFind.class \
TestLaterality.class \
TestGeometry_AllTests.class \
TestGeometryOfSlice.class \
TestDecimalStringAttributeLocaleEffect.class \
TestDecimalStringAttribute_AllTests.class \
TestDicomDirectory_AllTests.class \
TestDicomDirectoryRecordFactory.class \
TestDicomDirectoryRecordSortOrder.class \
TestDicomNetwork_AllTests.class \
TestCStore.class \
TestFloatFormatterLocaleEffect.class \
TestFloatFormatter_AllTests.class \
TestNumericContentItemFloatingAndRational.class \
TestNumericContentItemLocaleEffect.class \
TestRemoveIdentifyingAttributes.class \
TestPatientAgeWhenRemoveIdentifyingAttributes.class \
TestRemoveIdentifyingAttributes_AllTests.class \
TestSpatialCoordinatesContentItemLocaleEffect.class \
TestSpatialCoordinates3DContentItemLocaleEffect.class \
TestSUVTransformValues.class \
TestSUVTransform_AllTests.class \
TestUnknownAttributeBinaryValueExtraction.class \
TestUnknownAttribute_AllTests.class \
TestUUIDBasedOID_AllTests.class \
TestUUIDBasedOID.class \
TestBase64_AllTests.class \
TestBase64_RoundTrip.class \
TestAgeCalculation.class \
TestDates_AllTests.class \
TestDateTimeAttribute_AllTests.class \
TestDateTimeAttributeTimeSinceEpochExtraction.class \
TestDateTimeAttributeTimeZone.class \
TestSequenceAttributeDelimitedString.class \
TestSequenceAttributeStringsWithinItems.class \
TestSequenceAttribute_AllTests.class \
TestStructuredReport_AllTests.class \
TestStructuredReport_XMLRepresentation.class \
TestThreadUtilitiesEventDispatchThread.class \
TestThreadUtilities_AllTests.class \
TestDatabase_AllTests.class \
TestDatabaseDateTimeConversionInsertion.class \
TestMultiFrameImageFactory_AllTests.class \
TestMultiFrameImageFactoryDateTime.class \
TestColorConversions_AllTests.class \
TestColorConversions_SRGB_CIELabPCS.class \
TestAttributeListReadTerminationStrategy.class \
TestRepairAttributeValues_AllTests.class \
TestRepairAttributeValues.class \
TestCodedSequenceItem_AllTests.class \
TestCodedSequenceItemParseStringTuple.class \
TestPackBits_AllTests.class \
TestPackBits_UnPack.class \
TestCapabilitiesAbsent_AllTests.class \
TestCapabilitiesPresent_AllTests.class \
TestCapabilitiesCodecsAbsent.class \
TestCapabilitiesCodecsPresent.class \
UserInterfaceUtilities.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../test.`date '+%Y%m%d'`.tar.gz

clean:
	rm -f *~ *.class core *.bak ${OBJS} testcleanerfile.dcm

alltests:	testcolorconv testsr testdatetime testsuv testfloatformatter testdecimalstring testunkown testctdose testcompositecontext testsafeprivate testremoveid testattributelist \
			testcontentitem testcodingscheme testcharset testcleaner testanatomy testgeometry testdicomdir testnetwork testuuid testbase64 testdates testsequence \
			testthread testdatabase testmultiframe testrepair testcodedsequenceitem testpackbits testcapabilitiespresent testcapabilitiesabsent

testcapabilitiespresent:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR}:${JPEGBLOCKREDACTIONJAR}:${BZIP2ADDITIONALJAR}:${JIIOADDITIONALJARS} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestCapabilitiesPresent_AllTests

testcapabilitiesabsent:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestCapabilitiesAbsent_AllTests

testpackbits:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestPackBits_AllTests
			
testcodedsequenceitem:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestCodedSequenceItem_AllTests
			
testcolorconv:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestColorConversions_AllTests
			
testmultiframe:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestMultiFrameImageFactory_AllTests

testdatetime:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestDateTimeAttribute_AllTests

testsuv:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestSUVTransform_AllTests

testfloatformatter:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestFloatFormatter_AllTests

testdecimalstring:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestDecimalStringAttribute_AllTests

testunkown:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestUnknownAttribute_AllTests

testctdose:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestCTDose_AllTests

testcompositecontext:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestCompositeInstanceContext_AllTests

testsafeprivate:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true -Dcom.pixelmed.test.filepath=${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/SafePrivateTestImages org.junit.runner.JUnitCore com.pixelmed.test.TestSafePrivate_AllTests

testremoveid:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestRemoveIdentifyingAttributes_AllTests

testattributelist:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestAttributeList_AllTests

testcontentitem:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestContentItem_AllTests

testcodingscheme:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestCodingScheme_AllTests

testcharset:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestCharacterSet_AllTests

testcleaner:	TestCleaner_AllTests.class TestCleanerReceiveAndClean.class UserInterfaceUtilities.class
	rm -f $${HOME}/.com.pixelmed.display.DicomCleaner.properties.holdfortestcleaner
	if [ -f $${HOME}/.com.pixelmed.display.DicomCleaner.properties ]; then cp $${HOME}/.com.pixelmed.display.DicomCleaner.properties $${HOME}/.com.pixelmed.display.DicomCleaner.properties.holdfortestcleaner; fi
	cp TestCleaner.properties $${HOME}/.com.pixelmed.display.DicomCleaner.properties
	mkdir -p ./tmp/TestCleanerReceiveAndClean
	rm -rf ./tmp/TestCleanerReceiveAndClean/*
	rm -f testcleanerfile_pre.dcm testcleanerfile.dcm
	dcsmpte testcleanerfile.dcm \
		-nodisclaimer \
		-r PatientName             "Test^OriginalName" \
		-r PatientID               "TESTORIGINALID" \
		-r AccessionNumber         "TESTORIGINALNUM" \
		-r StudyID			       "TESTORIGINALID" \
		-r InstitutionName         "Original Institution" \
		-r DeviceSerialNumber      "Original Serial Number" \
		-r PatientWeight           "57.5" \
		-r PatientSize             "175" \
		-r PatientSex              "F" \
		-r PatientAge              "041Y" \
		-r PatientBirthDate        "19700214" \
		-r StudyDescription        "Original Study Description" \
		-r SeriesDescription       "Original Series Description" \
		-r ImageComments           "Original Image Comment" \
		-r SOPInstanceUID          "1.3.6.1.4.1.5962.1.1.0.0.0.1298308902.23788.0" \
		-r SeriesInstanceUID       "1.3.6.1.4.1.5962.1.3.0.0.1298308902.23788.0" \
		-r StudyInstanceUID        "1.3.6.1.4.1.5962.1.2.0.1298308902.23788.0" \
		-r ClinicalTrialProtocolID "TESTORIGINALID" \
		-r '(0x0029,0x0010)'       "SIEMENS MED DISPLAY " \
		-r '(0x0029,0x1099)'        "UnsafeOriginalPrivateValue" \
		-r '(0x01E1,0x0010)'       "ELSCINT1" \
		-r '(0x01E1,0x1026)'       "SAFEPHANTOMTYPE "
	#dccp -nodisclaimer testcleanerfile_pre.dcm testcleanerfile.dcm # in order to set correct explicit VR of private attributes to allow pixelmed to see values as strings not UN
	#rm testcleanerfile_pre.dcm
	#dciodvfy testcleanerfile.dcm
	# do NOT use VIEWERADDITIONALJARS, which includes jmdns, and make cause default send target to not be the desired target (000615)
	java -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}:${DATABASEADDITIONALJARS}:${JUNITJAR} org.junit.runner.JUnitCore com.pixelmed.test.TestCleaner_AllTests
	rm testcleanerfile.dcm
	#rm -r ./tmp/TestCleanerReceiveAndClean
	if [ -f $${HOME}/.com.pixelmed.display.DicomCleaner.properties.holdfortestcleaner ]; then cp $${HOME}/.com.pixelmed.display.DicomCleaner.properties.holdfortestcleaner $${HOME}/.com.pixelmed.display.DicomCleaner.properties; else rm -f $${HOME}/.com.pixelmed.display.DicomCleaner.properties; fi

testanatomy:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestAnatomy_AllTests

testgeometry:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestGeometry_AllTests

testdicomdir:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestDicomDirectory_AllTests

testnetwork:	${OBJS}
	rm -f testnetworkfile_*.dcm
	dcsmpte testnetworkfile_scevrle.dcm
	cp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG12/GE_VingMed/GEMS_IMG/2001_DEC/11/6M163410/2929J686" testnetworkfile_usrle.dcm
	cp "$${HOME}/Pictures/Medical/MPEG/Video Endoscopic mpeg.dcm" testnetworkfile_endompeg.dcm
	mkdir -p receivedfiles
	rm -rf receivedfiles/*
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestDicomNetwork_AllTests
	rm -rf receivedfiles
	rm -f testnetworkfile_scevrle.dcm

testuuid:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestUUIDBasedOID_AllTests

testbase64:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestBase64_AllTests

testdates:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestDates_AllTests

testsequence:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.test.TestSequenceAttribute_AllTests

testthread:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} org.junit.runner.JUnitCore com.pixelmed.test.TestThreadUtilities_AllTests

testsr:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR} org.junit.runner.JUnitCore com.pixelmed.test.TestStructuredReport_AllTests

testdatabase:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR}:${DATABASEADDITIONALJARS} org.junit.runner.JUnitCore com.pixelmed.test.TestDatabase_AllTests

testrepair:	${OBJS}
	java -cp ${PATHTOROOT}:${JUNITJAR}:${DATABASEADDITIONALJARS} org.junit.runner.JUnitCore com.pixelmed.test.TestRepairAttributeValues_AllTests



