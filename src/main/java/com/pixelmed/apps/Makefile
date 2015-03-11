PATHTOROOT = ../../..

OBJS = \
SynchronizeFromRemoteSCP.class \
WatchFolderAndSend.class \
RotateFlipSetOfImages.class \
DoseUtility.class \
InsertCodeSequence.class \
MergeCompositeContext.class \
DoseReporterWithLegacyOCRAndAutoSendToRegistry.class \
MultiModalitySimulator.class \
ConvertPaletteColorToGrayscale.class \
ConvertPlanarConfiguration.class \
ConvertNonRGBToRGB.class \
UIDMapByMatchingPixelData.class \
DownloadOrTransmit.class \
RemapUIDs.class \
CopyOnlyHeadOfMultipleStructuredReportsAndReferences.class \
CheckAllUIDReferencesResolve.class \
AddHierarchicalEvidenceSequencetoStructuredReports.class \
IntegerScalingOfFloatingPointPixelData.class \
RemapFrameOfReferenceUIDs.class \
TranslateImagePositionPatient.class \
KeepOnlyImagePixelModule.class \
DecompressDicomFiles.class \
Convert8To16Bits.class \
InsertRealWorldValueMap.class \
DeidentifyAndRedact.class \
DeidentifyAndRedactWithOriginalFileName.class \
FindAndCopySelectedDicomFiles.class \
FindAndCopySelectedDicomFilesUsingDatabase.class \
StudyReceiver.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

# override javac command in Makefile.common.mk to add codec to classpath, until folded into main trunk :(
.java.class:
	export JAVAVERSIONTARGETJARFILE=`/usr/libexec/java_home -v ${JAVAVERSIONTARGET} | tail -1`/jre/lib/rt.jar; javac ${JAVACOPTIONS} \
		-classpath ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${FTPADDITIONALJARS}:${JPEGBLOCKREDACTIONJAR} \
		-sourcepath ${PATHTOROOT} $<

archive:
	tar -cvf - Makefile *.java | gzip -best > ${PATHTOROOT}/apps.`date '+%Y%m%d'`.tar.gz

testsync:	SynchronizeFromRemoteSCP.class
	mkdir -p /tmp/testimages
	java -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} -Djava.awt.headless=true \
		com.pixelmed.apps.SynchronizeFromRemoteSCP \
			/tmp/testdatabase \
			/tmp/testimages \
			localhost 4006 GRAYTOO_DIV_4006 \
			4007 LOCAL4007 1

testrotateflip90H:	RotateFlipSetOfImages.class
	rm -rf /tmp/testrotateflip90H
	mkdir -p /tmp/testrotateflip90H
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.RotateFlipSetOfImages \
			270 Y Y \
			$${HOME}/work/dctool.support/images/dicom/forjpeg2000/discimg \
			/tmp/testrotateflip90H

testwatchandsend:	WatchFolderAndSend.class
	@rm -rf /tmp/watchthisfolder
	@mkdir -p /tmp/watchthisfolder
	../network/runprocessinbackground.sh \
		/tmp/watchandsend.pid /tmp/watchandsend.stdout /tmp/watchandsend.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.apps.WatchFolderAndSend \
			/tmp/watchthisfolder \
			localhost 11112 STORESCP US 1 0
	#tail -f /tmp/watchandsend.stderr &
	@rm -rf /tmp/tesstoragescp.receivedfiles
	@mkdir -p /tmp/tesstoragescp.receivedfiles
	../network/runprocessinbackground.sh \
		/tmp/tesstoragescp.pid /tmp/tesstoragescp.stdout /tmp/tesstoragescp.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.network.StorageSOPClassSCPDispatcher 11112 STORESCP /tmp/tesstoragescp.receivedfiles INSECURE 0
	#tail -f /tmp/tesstoragescp.stderr &
	dcsmpte /tmp/testsmpte.dcm -r PatientName Test^Watch
	#java -cp ${PATHTOROOT} com.pixelmed.network.StorageSOPClassSCU localhost 11112 STORESCP US /tmp/testsmpte.dcm 0 0
	cp /tmp/testsmpte.dcm /tmp/watchthisfolder
	@echo "Sleeping for longer than two watch intervals in order to allow wait for being modified to expire"
	@sleep 25
	@echo "Back from sleep ... folder content should have been processed by now"
	@echo "Dumping logger messages"
	@cat /tmp/watchandsend.stderr
	@echo "List contents of watched directory ... expect it to be empty"
	@ls -lL /tmp/watchthisfolder
	@echo "List done"
	@echo "Comparing sent and received files - expect no difference except meta header"
	# the -a option to grep is necessary since sometimes it thinks the stderr file is binary and fails
	@receivedfilename=`grep -a 'sendReceivedObjectIndication() fileName' /tmp/tesstoragescp.stderr | tail -1 | sed -e 's/^.*fileName: //' -e 's/ from .*$$//'`; \
		andiff /tmp/testsmpte.dcm "$${receivedfilename}"; \
		dccmp /tmp/testsmpte.dcm "$${receivedfilename}"
	@echo "Comparison done"
	@kill -9 `cat /tmp/watchandsend.pid`
	@kill -9 `cat /tmp/tesstoragescp.pid`
	@rm -f /tmp/testsmpte.dcm
	@rm -rf /tmp/watchthisfolder
	@rm -f /tmp/watchandsend.pid /tmp/watchandsend.stdout /tmp/watchandsend.stderr
	@rm -rf /tmp/tesstoragescp.receivedfiles
	@rm -f /tmp/tesstoragescp.pid /tmp/tesstoragescp.stdout /tmp/tesstoragescp.stderr

testwatchandsenddialog:	WatchFolderAndSend.class
	java -cp ${PATHTOROOT} com.pixelmed.apps.WatchFolderAndSend

testdoseutility:	DoseUtility.class
	java -Dcom.sun.management.jmxremote -Xmx2g -XX:+UseCompressedStrings -XX:+UseStringCache -XX:+OptimizeStringConcat -XX:+UseCompressedOops -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${FTPADDITIONALJARS} com.pixelmed.apps.DoseUtility

testinsertcode:	InsertCodeSequence.class
	rm -f /tmp/testinsertcodesrc.dcm
	dcsmpte /tmp/testinsertcodesrc.dcm
	rm -rf /tmp/testinsertcodedst
	mkdir -p /tmp/testinsertcodedst
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.InsertCodeSequence \
			AnatomicRegionSequence "T-A0100" "SRT" "" "Brain" \
			/tmp/testinsertcodesrc.dcm \
			/tmp/testinsertcodedst
	dcdiff /tmp/testinsertcodesrc.dcm /tmp/testinsertcodedst/*
	rm -f /tmp/testinsertcodesrc.dcm
	rm -rf /tmp/testinsertcodedst

testinsertrwvm:	InsertRealWorldValueMap.class
	rm -f /tmp/testinsertrwvmsrc.dcm
	dcsmpte /tmp/testinsertrwvmsrc.dcm
	rm -rf /tmp/testinsertrwvmdst
	mkdir -p /tmp/testinsertrwvmdst
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.InsertRealWorldValueMap \
			0 255 0 1 "SUVbw" "GML" \
			"g/l" "UCUM" "" "g/l" \
			/tmp/testinsertrwvmsrc.dcm \
			/tmp/testinsertrwvmdst
	dcdiff /tmp/testinsertrwvmsrc.dcm /tmp/testinsertrwvmdst/*
	dciodvfy /tmp/testinsertrwvmdst/*
	rm -f /tmp/testinsertrwvmsrc.dcm
	rm -rf /tmp/testinsertrwvmdst

testinsertrwvmwithquantity:	InsertRealWorldValueMap.class
	rm -f /tmp/testinsertrwvmsrc.dcm
	dcsmpte /tmp/testinsertrwvmsrc.dcm
	rm -rf /tmp/testinsertrwvmdst
	mkdir -p /tmp/testinsertrwvmdst
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.InsertRealWorldValueMap \
			0 255 0 1 "SUVbw" "GML" \
			"{SUVbw}g/ml" "UCUM" "" "SUVbw in g/ml" \
			CODE "(G-C1C6, SRT, Quantity)" '(126400, DCM, "Standardized Uptake Value")' \
			CODE '(G-C036, SRT, "Measurement Method")' '(126410, DCM, "SUV body weight calculation method")' \
			NUMERIC '(G-7174, SRT, "Delay time")' "60" '(s, UCUM, "s")' \
			TEXT '(121401, DCM, "Derivation")' "My funky method" \
			/tmp/testinsertrwvmsrc.dcm \
			/tmp/testinsertrwvmdst
	dcdiff /tmp/testinsertrwvmsrc.dcm /tmp/testinsertrwvmdst/*
	dciodvfy /tmp/testinsertrwvmdst/*
	rm -f /tmp/testinsertrwvmsrc.dcm
	rm -rf /tmp/testinsertrwvmdst

#first set up /tmp/craq to contain a pair to be merged, e.g., "QIBA_CT_1B/Blinded/200320 [RIDER1500037140]" and "QIBA_CT_1B/Results/MSKCC0001 [MSKCC0001]"
testmergecontext:	MergeCompositeContext.class
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContext \
			/tmp/crapq/QIBA_CT_1B \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat

testmergecontexttwo:	MergeCompositeContext.class
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContext \
			/tmp/crapq/QIBA_CT_1B/Blinded \
			/tmp/crapq/QIBA_CT_1B/Results \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat

testmergecontextsronly:	MergeCompositeContext.class
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	(find /tmp/crapq/QIBA_CT_1B/Results -name '*.sr' -exec cp '{}' /tmp/crapx/ ';')
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContext \
			/tmp/crapx \
			/tmp/crapq/QIBA_CT_1B/Blinded \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat

testmergecontextthree:	MergeCompositeContext.class
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	dcsmpte /tmp/crapx/initialcontext.dcm \
		-nodisclaimer \
		-r PatientName "MSKCC0001" \
		-r PatientID "MSKCC0001" \
		-r ClinicalTrialSubjectID "MSKCC0001" \
		-r ClinicalTrialSponsorName "QIBA" \
		-r ClinicalTrialProtocolID "QIBA_CT_1B" \
		-r ClinicalTrialProtocolName " " \
		-r ClinicalTrialSiteID "001" \
		-r ClinicalTrialSiteName " " \
		-r ClinicalTrialSubjectID "MSKCC0001" \
		-r ClinicalTrialSubjectReadingID "MSKCC0001" \
		-r StudyInstanceUID "1.3.6.1.4.1.9328.50.1.336855045631083138173485506844321353749"
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContext \
			/tmp/crapq/QIBA_CT_1B/Blinded \
			/tmp/crapq/QIBA_CT_1B/Results \
			/tmp/crapx \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat

testdosereporter:	DoseReporterWithLegacyOCRAndAutoSendToRegistry.class
	java -server -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${DATABASEADDITIONALJARS}:${FTPADDITIONALJARS} \
		com.pixelmed.apps.DoseReporterWithLegacyOCRAndAutoSendToRegistry \
		testdosereporter.properties

testdosereporterclean:
	rm -rf testdosereporterdb.*
	rm -rf ~/testdosereporterimages

teststudyreceiver:	StudyReceiver.class
	mkdir -p /tmp/teststudyreceiverimages
	mkdir -p /tmp/teststudyreceivercompletedstudyimages
	java -server -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${DATABASEADDITIONALJARS} \
		com.pixelmed.apps.StudyReceiver \
		teststudyreceiver.properties

teststudyreceiverclean:
	rm -rf /tmp/teststudyreceiverreporterdb.*
	rm -rf /tmp/teststudyreceiverimages
	rm -rf /tmp/teststudyreceivercompletedstudyimages

testmodsim:	MultiModalitySimulator.class
	java -server -Djava.awt.headless=true -Xmx384m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${DATABASEADDITIONALJARS} \
		com.pixelmed.apps.MultiModalitySimulator \
			localhost 4011 PACS \
			/Volumes/CluniePortable1TB1_Enc800/database \
			1

testconvertpalette:	ConvertPaletteColorToGrayscale.class
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertPaletteColorToGrayscale \
		$${HOME}/Pictures/Medical/Color/PaletteColorOfGrayscale/OBJ_0001 \
		/tmp/palette_converted_to_grayscale

testconvertplanar:	ConvertPlanarConfiguration.class
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertPlanarConfiguration \
		$${HOME}/Pictures/Medical/Color/YBRFULLBYPLANE/ybrfullbyplanecleaned.dcm \
		/tmp/ybrfull_planar_converted_to_pixel.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertPlanarConfiguration \
		/tmp/ybrfull_planar_converted_to_pixel.dcm \
		/tmp/ybrfull_planar_converted_back.dcm
	dccmp \
		$${HOME}/Pictures/Medical/Color/YBRFULLBYPLANE/ybrfullbyplanecleaned.dcm \
		/tmp/ybrfull_planar_converted_back.dcm

testconvertybr:	ConvertNonRGBToRGB.class testconvertplanar
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertNonRGBToRGB \
		$${HOME}/Pictures/Medical/Color/YBRFULLBYPLANE/ybrfullbyplanecleaned.dcm \
		/tmp/ybrfull_planar_converted_to_rgb.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertNonRGBToRGB \
		/tmp/ybrfull_planar_converted_to_pixel.dcm \
		/tmp/ybrfull_pixel_converted_to_rgb.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertPlanarConfiguration \
		/tmp/ybrfull_pixel_converted_to_rgb.dcm \
		/tmp/ybrfull_pixel_converted_to_rgb_converted_back_to_planar.dcm
	dccmp \
		/tmp/ybrfull_planar_converted_to_rgb.dcm \
		/tmp/ybrfull_pixel_converted_to_rgb_converted_back_to_planar.dcm

testuidmappixeldata8:	UIDMapByMatchingPixelData.class
	dcsmpte /tmp/crap1
	dcsmpte /tmp/crap2
	dctable -describe -k SOPInstanceUID -k SeriesInstanceUID -k StudyInstanceUID /tmp/crap1 /tmp/crap2
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.UIDMapByMatchingPixelData \
		/tmp/crap1 /tmp/crap2

testuidmappixeldata16:	UIDMapByMatchingPixelData.class
	dcsmpte -bits 16 /tmp/crap1
	dcsmpte -bits 16 /tmp/crap2
	dctable -describe -k SOPInstanceUID -k SeriesInstanceUID -k StudyInstanceUID /tmp/crap1 /tmp/crap2
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.UIDMapByMatchingPixelData \
		/tmp/crap1 /tmp/crap2

testdownloadortx:	DownloadOrTransmit.class
	java -Dcom.sun.management.jmxremote -Xmx2g -XX:+UseCompressedStrings -XX:+UseStringCache -XX:+OptimizeStringConcat -XX:+UseCompressedOops -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${FTPADDITIONALJARS} com.pixelmed.apps.DownloadOrTransmit

testremapuids:	RemapUIDs.class
	rm -rf /tmp/crap_first
	rm -rf /tmp/crap_second
	rm -rf /tmp/crap_dst
	mkdir /tmp/crap_first
	mkdir /tmp/crap_second
	mkdir /tmp/crap_dst
	dcsmpte /tmp/crap_first/crap1
	dcsmpte /tmp/crap_second/crap2
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.UIDMapByMatchingPixelData \
		/tmp/crap_first/crap1 /tmp/crap_second/crap2 \
		>/tmp/crapuidmap 2>&1
	cat /tmp/crapuidmap
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.RemapUIDs \
		/tmp/crapuidmap \
		/tmp/crap_second \
		/tmp/crap_dst
	# UIDs in second file should have been mapped to and now match those in first file that was used to generate the map
	outputfile=`find /tmp/crap_dst -name '*.dcm'`; dctable -describe -k SOPInstanceUID -k SeriesInstanceUID -k StudyInstanceUID /tmp/crap_first/crap1 /tmp/crap_second/crap2 "$${outputfile}"; dcdiff /tmp/crap_first/crap1 "$${outputfile}"

testcopysrhead:	CopyOnlyHeadOfMultipleStructuredReportsAndReferences.class
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.CopyOnlyHeadOfMultipleStructuredReportsAndReferences \
			/tmp/crapq/QIBA_CT_1B/Results \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm'

testresolve:	CheckAllUIDReferencesResolve.class
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.CheckAllUIDReferencesResolve \
			/tmp/crapq/QIBA_CT_1B

testaddevidence:	AddHierarchicalEvidenceSequencetoStructuredReports.class
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.AddHierarchicalEvidenceSequencetoStructuredReports \
			/tmp/crapq/QIBA_CT_1B \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm'

testfloatscaling:	IntegerScalingOfFloatingPointPixelData.class
	rm -f /tmp/testsyntheticfloatscaledtoint.dcm
	rm -f /tmp/testsyntheticfloatscaledtointthenmadefloatagain.dcm
	rm -f /tmp/testsyntheticfloatscaledtointthenmadefloatagainintagain.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		$${HOME}/work/experiments/testsyntheticfloat10.dcm /tmp/testsyntheticfloatscaledtoint.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toFLOAT /tmp/testsyntheticfloatscaledtoint.dcm /tmp/testsyntheticfloatscaledtointthenmadefloatagain.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toINT /tmp/testsyntheticfloatscaledtointthenmadefloatagain.dcm /tmp/testsyntheticfloatscaledtointthenmadefloatagainintagain.dcm
	#dccmp /tmp/testsyntheticfloatscaledtoint.dcm /tmp/testsyntheticfloatscaledtointthenmadefloatagainintagain.dcm
	andiff /tmp/testsyntheticfloatscaledtoint.dcm /tmp/testsyntheticfloatscaledtointthenmadefloatagainintagain.dcm

testdoublescaling:	IntegerScalingOfFloatingPointPixelData.class
	rm -f /tmp/testsyntheticdoublescaledtoint.dcm
	rm -f /tmp/testsyntheticdoublescaledtointthenmadedoubleagain.dcm
	rm -f /tmp/testsyntheticdoublescaledtointthenmadedoubleagainintagain.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		$${HOME}/work/experiments/testsyntheticdouble10.dcm /tmp/testsyntheticdoublescaledtoint.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toDOUBLE /tmp/testsyntheticdoublescaledtoint.dcm /tmp/testsyntheticdoublescaledtointthenmadedoubleagain.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toINT /tmp/testsyntheticdoublescaledtointthenmadedoubleagain.dcm /tmp/testsyntheticdoublescaledtointthenmadedoubleagainintagain.dcm
	#dccmp /tmp/testsyntheticdoublescaledtoint.dcm /tmp/testsyntheticdoublescaledtointthenmadedoubleagainintagain.dcm
	andiff /tmp/testsyntheticdoublescaledtoint.dcm /tmp/testsyntheticdoublescaledtointthenmadedoubleagainintagain.dcm

testremapforuids:	RemapFrameOfReferenceUIDs.class
	rm -rf /tmp/testremapforuids
	mkdir -p /tmp/testremapforuids/input/series1
	mkdir -p /tmp/testremapforuids/input/series2
	mkdir -p /tmp/testremapforuids/output
	dcsmpte /tmp/testremapforuids/input/series1/image1 -r StudyInstanceUID 0.1.1.1 -r SeriesInstanceUID 1.1.1.1 -r FrameOfReferenceUID 2.1.1.1
	dcsmpte /tmp/testremapforuids/input/series1/image2 -r StudyInstanceUID 0.1.1.1 -r SeriesInstanceUID 1.1.1.1 -r FrameOfReferenceUID 2.1.1.2
	dcsmpte /tmp/testremapforuids/input/series2/image3 -r StudyInstanceUID 0.1.1.1 -r SeriesInstanceUID 1.1.1.2 -r FrameOfReferenceUID 2.1.1.3
	dcsmpte /tmp/testremapforuids/input/series2/image4 -r StudyInstanceUID 0.1.1.1 -r SeriesInstanceUID 1.1.1.2 -r FrameOfReferenceUID 2.1.1.4
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.RemapFrameOfReferenceUIDs \
		SERIES \
		/tmp/testremapforuids/input \
		/tmp/testremapforuids/output

testtranslateposn:	TranslateImagePositionPatient.class
	rm -rf /tmp/craptranslate
	mkdir -p /tmp/craptranslate/TCGA-GBM_FixedBadExplicitVR/TCGA-14-3477_Results
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.TranslateImagePositionPatient \
		XYZ 0.026248860744917658 0.08305511807304704 1.2504172730164407 \
		$${HOME}/Documents/Work/NCI/AIM/Emory/images/TCGA-GBM_FixedBadExplicitVR/TCGA-14-3477_Results/1.2.276.0.7230010.3.1.4.1564572511.2560.1353374333.316.seg \
		/tmp/craptranslate/TCGA-GBM_FixedBadExplicitVR/TCGA-14-3477_Results

testkeeponlyimage:	KeepOnlyImagePixelModule.class
	rm -rf /tmp/testkeeponlyimage
	mkdir -p /tmp/testkeeponlyimage
	dcsmpte /tmp/testkeeponlyimage/input.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.KeepOnlyImagePixelModule \
		/tmp/testkeeponlyimage/input.dcm \
		/tmp/testkeeponlyimage/output.dcm
	dcdiff /tmp/testkeeponlyimage/input.dcm \
		   /tmp/testkeeponlyimage/output.dcm

testdecompress:	DecompressDicomFiles.class
	rm -rf /tmp/testdecompress.smpte.dcm /tmp/testdecompress.smpte.jpeg.dcm /tmp/testdecompress.output
	dcsmpte /tmp/testdecompress.smpte.dcm
	dcjpeg /tmp/testdecompress.smpte.dcm /tmp/testdecompress.smpte.jpeg.dcm 1.2.840.10008.1.2.4.50 -q 1
	mkdir -p /tmp/testdecompress.output
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.smpte.jpeg.dcm /tmp/testdecompress.output
	dcdiff /tmp/testdecompress.smpte.jpeg.dcm /tmp/testdecompress.output/*
	rm -rf /tmp/testdecompress.smpte.dcm /tmp/testdecompress.smpte.jpeg.dcm /tmp/testdecompress.output

testdecompressrle:	DecompressDicomFiles.class
	cp "$${HOME}/Pictures/Medical/RLE/US16BitsPaletteColorRLE.dcm" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -f /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	dcdiff /tmp/testdecompress.dcm /tmp/testdecompress.output/*
	#rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output

testconvert8to16:	Convert8To16Bits.class
	dcsmpte /tmp/crap8.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.Convert8To16Bits \
		/tmp/crap8.dcm \
		/tmp/crap12.dcm \
		12
	dcstats /tmp/crap8.dcm
	dcstats /tmp/crap12.dcm
	dchist -h /tmp/crap8.dcm >/tmp/crap8.hist 2>&1
	dchist -h /tmp/crap12.dcm >/tmp/crap12.hist 2>&1
	diff /tmp/crap8.hist /tmp/crap12.hist
	rm -f /tmp/crap8.dcm /tmp/crap8.hist /tmp/crap12.dcm /tmp/crap12.hist

testdeidentifyjpeg:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "/Volumes/My Book/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitblock:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "/Volumes/My Book/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		BLOCK \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitblockandallprivate:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "/Volumes/My Book/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		BLOCK \
		KEEPALLPRIVATE \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitblockandsafeprivate:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "/Volumes/My Book/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		BLOCK \
		KEEPSAFEPRIVATE \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitallprivate:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "/Volumes/My Book/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		KEEPALLPRIVATE \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitallprivatenoreplacements:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "/Volumes/My Book/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		KEEPALLPRIVATE
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitsafeprivate:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "/Volumes/My Book/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		KEEPSAFEPRIVATE \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithdecompress:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "/Volumes/My Book/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		DECOMPRESS \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyuncompressed:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "512x512 = (0,0,511,150)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	dcsmpte /tmp/testdeidentifysource/testfile.dcm -r PatientName "Smith^Mary"
	touch /tmp/testdeidentifysource/emptyfile.dcm	# a bad one to check it is rejected and reported
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyuncompressedivrlemh:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "512x512 = (0,0,511,150)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	dcsmpte /tmp/testdeidentifysource/testfile.dcm -r PatientName "Smith^Mary" -output-endian little -output-vr  implicit
	dcfile /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifynoredaction:	DeidentifyAndRedact.class
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	dcsmpte /tmp/testdeidentifysource/testfile.dcm -r PatientName "Smith^Mary"
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		""
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyrle:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "800x600 = (0,0,569,59)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "$${HOME}/Pictures/Medical/RLE/US16BitsPaletteColorRLE.dcm" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifycoordinatecheck:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "1024x768 = (10, 9, 520, 62)" >/tmp/testdeidentifedregions.txt
	echo "1024x768 = (530, 35, 340, 36)" >>/tmp/testdeidentifedregions.txt
	echo "800x600 = (10, 2, 400, 52)" >>/tmp/testdeidentifedregions.txt
	echo "800x600 = (510, 35, 265, 19)" >>/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		"$${HOME}/Documents/Clients/MDDX/20140611_Coordinates/13395/397511552810" \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		PatientName "Doe^Jane" \
		PatientID "000000"

testdeidentifycoordinatecheckwrongmf:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "800x600 = (10, 9, 520, 62)" >/tmp/testdeidentifedregions.txt
	echo "800x600 = (530, 35, 340, 36)" >>/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		"$${HOME}/Documents/Clients/MDDX/20140611_Coordinates/13395/397511552810/BOV549E8" \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		PatientName "Doe^Jane" \
		PatientID "000000"

testdeidentifycoordinatecheckanothersf:	DeidentifyAndRedact.class
	echo "1024x768 = (10, 9, 520, 62)" >/tmp/testdeidentifedregions.txt
	echo "1024x768 = (530, 35, 340, 36)" >>/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		"$${HOME}/Documents/Clients/MDDX/20140611_Coordinates/file9_anon" \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		PatientName "Doe^Jane" \
		PatientID "000000"

testfindandcopysrbyuid:	FindAndCopySelectedDicomFiles.class
	mkdir -p /tmp/crap_src
	rm -f /tmp/crap_src/*
	mkdir -p /tmp/crap_dst
	rm -f /tmp/crap_dst/*
	dcsmpte /tmp/crap_src/sc.dcm
	dcsmpte /tmp/crap_src/sr.dcm -r SOPClassUID 1.2.840.10008.5.1.4.1.1.88.11
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.FindAndCopySelectedDicomFiles \
		/tmp/crap_src \
		/tmp/crap_dst \
		1.2.840.10008.5.1.4.1.1.88.11
	ls -l /tmp/crap_dst

testfindandcopysrbymodality:	FindAndCopySelectedDicomFiles.class
	mkdir -p /tmp/crap_src
	rm -f /tmp/crap_src/*
	mkdir -p /tmp/crap_dst
	rm -f /tmp/crap_dst/*
	dcsmpte /tmp/crap_src/sc.dcm
	dcsmpte /tmp/crap_src/sr.dcm -r SOPClassUID 1.2.840.10008.5.1.4.1.1.88.11
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.FindAndCopySelectedDicomFiles \
		/tmp/crap_src \
		/tmp/crap_dst \
		SR
	ls -l /tmp/crap_dst
