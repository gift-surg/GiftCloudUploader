OBJS = \
	ConvertAmicasJPEG2000FilesetToDicom.class \
	NIfTI1ToDicom.class \
	NIfTI1Header.class \
	NIfTI1Exception.class \
	NRRDToDicom.class \
	NRRDHeader.class \
	NRRDException.class \
	RawRGBInformation.class \
	RawRGBToDicomMultiFrame.class \
	UnencapsulateCompressedPixelData.class \
	EncapsulateCompressedPixelData.class \
	IndexedLabelMapToSegmentation.class

all:	${OBJS}

PATHTOROOT = ../../..

include ${PATHTOROOT}/Makefile.common.mk

testamicas:	ConvertAmicasJPEG2000FilesetToDicom.class
	mkdir -p /tmp/todicomMR1
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom "${PATHTOHOME}/Pictures/Medical/AmicasJ2K/ELMR20030206" /tmp/todicomMR1
	mkdir -p /tmp/todicomCT1
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom "${PATHTOHOME}/Pictures/Medical/AmicasJ2K/CD0105153529" /tmp/todicomCT1
	mkdir -p /tmp/todicomCT2
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom "${PATHTOHOME}/Pictures/Medical/AmicasJ2K/CD0114154520" /tmp/todicomCT2

testamicasdialog:	ConvertAmicasJPEG2000FilesetToDicom.class
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom


testnifti1header:	NIfTI1Header.class NIfTI1Exception.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1Header \
		"$${HOME}/Pictures/Medical/ParametricMaps/DICOM_FP/GE_Normalized_Boxerman_rBFMap_LeakageCorrected.nii"
		#"$${HOME}/Pictures/Medical/ParametricMaps/DICOM_FP/dti_adc.nii"

testnifti1convert:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f /tmp/testnifti1fp.dcm
	rm -f /tmp/testnifti1fp_scaledtoint.dcm
	rm -f /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"$${HOME}/Pictures/Medical/ParametricMaps/DICOM_FP/DICOM_FP/GE_Normalized_Boxerman_rBFMap_LeakageCorrected.nii" \
		/tmp/testnifti1fp.dcm \
		"Doe^Jane" 1234 5678 100 1
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		/tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtoint.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toFLOAT /tmp/testnifti1fp_scaledtoint.dcm /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm
	andump /tmp/testnifti1fp.dcm
	#dciodvfy /tmp/testnifti1fp.dcm
	andump /tmp/testnifti1fp_scaledtoint.dcm
	dciodvfy /tmp/testnifti1fp_scaledtoint.dcm
	#dchist -h /tmp/testnifti1fp_scaledtoint.dcm
	#andiff /tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtoint.dcm
	@echo "Checking round trip to int and back to float ..."
	#dccmp /tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm
	andiff /tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm

testnifti1talairach:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "$${HOME}/Documents/Medical/stuff/Atlases/Talairach/talairach.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"$${HOME}/Documents/Medical/stuff/Atlases/Talairach/talairach.nii" \
		"$${HOME}/Documents/Medical/stuff/Atlases/Talairach/talairach.dcm" \
		"Talairach" 1234 5678 100 1 MR

testnifti1dti:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f /tmp/dti20.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/My Book/Pictures/Medical/dcm2nii_TestDataSets/dti20/dti20.nii" \
		"/tmp/dti20.dcm" \
		"DTI" 1234 5678 100 1 MR
	if [ -f "/Volumes/My Book/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/My Book/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm" "/tmp/dti20.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/My Book/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm" "/tmp/dti20.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1test4d1:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/filtered_func_data.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.nii" \
		"/tmp/filtered_func_data.dcm" \
		"4D" 1234 5678 100 1 MR
	if [ -f "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm" "/tmp/filtered_func_data.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm" "/tmp/filtered_func_data.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testLR:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/avg152T1_LR_nifti.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.nii" \
		"/tmp/avg152T1_LR_nifti.dcm" \
		"LR" 1234 5678 100 1 MR
	if [ -f "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" "/tmp/avg152T1_LR_nifti.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" "/tmp/avg152T1_LR_nifti.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testRL:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/avg152T1_RL_nifti.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.nii" \
		"/tmp/avg152T1_RL_nifti.dcm" \
		"RL" 1234 5678 100 1 MR
	if [ -f "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm" "/tmp/avg152T1_RL_nifti.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm" "/tmp/avg152T1_RL_nifti.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testZ:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/zstat1.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.nii" \
		"/tmp/zstat1.dcm" \
		"RL" 1234 5678 100 1 MR
	if [ -f "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm" "/tmp/zstat1.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm" "/tmp/zstat1.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testfunc:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/newsirp_final_XML.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.nii" \
		"/tmp/newsirp_final_XML.dcm" \
		"RL" 1234 5678 100 1 MR
	if [ -f "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm" "/tmp/newsirp_final_XML.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm" "/tmp/newsirp_final_XML.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1compare:	testnifti1dti testnifti1test4d1 testnifti1testLR testnifti1testRL testnifti1testZ testnifti1testfunc

testnifti1create:
	rm -f "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm"
	rm -f "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm"
	rm -f "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm"
	rm -f "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm"
	rm -f "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm"
	rm -f "/Volumes/My Book/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm"
	make testnifti1compare
	cp /tmp/newsirp_final_XML.dcm "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm"
	cp /tmp/zstat1.dcm "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm"
	cp /tmp/avg152T1_RL_nifti.dcm "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm"
	cp /tmp/avg152T1_LR_nifti.dcm "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm"
	cp /tmp/filtered_func_data.dcm "/Volumes/My Book/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm"
	cp /tmp/dti20.dcm "/Volumes/My Book/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm"

testnrrdheaderseparate:	NRRDHeader.class NRRDException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDHeader \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume.nhdr"

testnrrdheadercombined:	NRRDHeader.class NRRDException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDHeader \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume frame 21.nrrd"

testnrrdconvertseparate:	NRRDToDicom.class NRRDHeader.class NRRDException.class
	rm -f /tmp/testnrrdfp.dcm
	rm -f /tmp/testnrrdfp_scaledtoint.dcm
	rm -f /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	java -Xmx2g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume.nhdr" \
		/tmp/testnrrdfp.dcm \
		"Doe^Jane" 1234 5678 100 1
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		/tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtoint.dcm
	java -Xmx1g -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toDOUBLE /tmp/testnrrdfp_scaledtoint.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	#andump /tmp/testnrrdfp.dcm
	#dciodvfy /tmp/testnrrdfp.dcm
	andump /tmp/testnrrdfp_scaledtoint.dcm
	dciodvfy /tmp/testnrrdfp_scaledtoint.dcm
	#dchist -h /tmp/testnrrdfp_scaledtoint.dcm
	#andiff /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtoint.dcm
	#@echo "Checking round trip to int and back to float ..."
	#dccmp /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm		#dccmp fails (hangs for ever during dctoraw ... ? why :()
	andiff /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm

testnrrdconvertcombined:	NRRDToDicom.class NRRDHeader.class NRRDException.class
	rm -f /tmp/testnrrdfp.dcm
	rm -rf /tmp/testnrrdfp_withrwvm
	mkdir -p /tmp/testnrrdfp_withrwvm
	rm -f /tmp/testnrrdfp_scaledtoint.dcm
	rm -f /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/OncoQuant-AutoAIF-Results/PCAMPMRI-0241_GE-Ktrans.nrrd" \
		/tmp/testnrrdfp_norwvm.dcm \
		"Doe^Jane" 1234 5678 100 1
	#"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume frame 21.nrrd"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.InsertRealWorldValueMap \
			0 5 0 1 "Ktrans" "Ktrans" \
			"/min" "UCUM" "" "/min" \
			CODE "(G-C1C6, SRT, Quantity)" "(126312, DCM, Ktrans)" \
			CODE '(G-C036, SRT, "Measurement Method")' '(126340, DCM, "Standard Tofts Model")' \
			/tmp/testnrrdfp_norwvm.dcm \
			/tmp/testnrrdfp_withrwvm
	@echo "Comparing before and after adding RWVM ..."
	andiff /tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_norwvm.dcm
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		/tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_scaledtoint.dcm
	java -Xmx1g -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toFLOAT /tmp/testnrrdfp_scaledtoint.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	#andump /tmp/testnrrdfp_withrwvm/*
	#dciodvfy /tmp/testnrrdfp_withrwvm/*
	andump /tmp/testnrrdfp_scaledtoint.dcm
	dciodvfy /tmp/testnrrdfp_scaledtoint.dcm
	#dchist -h /tmp/testnrrdfp_scaledtoint.dcm
	#andiff /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtoint.dcm
	@echo "Checking round trip to int and back to float ..."
	#dccmp /tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	andiff /tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm

testnrrdheaderseparate:	NRRDHeader.class NRRDException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDHeader \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume.nhdr"

testrawrgbheader:	RawRGBInformation.class
	if [ ! -f "/tmp/vhm/testfiles/README" ]; \
	then \
		mkdir -p "/tmp/vhm/testfiles"; \
		cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/README" "/tmp/vhm/testfiles/README"; \
	fi
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBInformation \
		"/tmp/vhm/testfiles/README"

testrawrgbvhmuncompressed:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/uncompressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/README" "/tmp/vhm/testfiles/"
	gzcat <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2000.rgb.gz" >"/tmp/vhm/testfiles/srcimages/uncompressed/2000.rgb"
	gzcat <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2001.rgb.gz" >"/tmp/vhm/testfiles/srcimages/uncompressed/2001.rgb"
	gzcat <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2002.rgb.gz" >"/tmp/vhm/testfiles/srcimages/uncompressed/2002.rgb"
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/uncompressed" \
		".rgb" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmcompressed:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/compressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/README" "/tmp/vhm/testfiles/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2000.rgb.gz" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2001.rgb.gz" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2002.rgb.gz" "/tmp/vhm/testfiles/srcimages/compressed/"
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/compressed" \
		".rgb.gz" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmuncompressedsmall:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/uncompressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" "/tmp/vhm/testfiles/"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1455.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1455.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1456.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1456.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1457.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1457.raw"
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/uncompressed" \
		".raw" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmunixcompressed:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/compressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" "/tmp/vhm/testfiles/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1455.raw.Z" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1456.raw.Z" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1457.raw.Z" "/tmp/vhm/testfiles/srcimages/compressed/"
	# BZIP2ADDITIONALJAR is now commons-compress, which contains Unix Z decompressor ...
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/compressed" \
		".raw.Z" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmbzip2:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/uncompressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" "/tmp/vhm/testfiles/"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1455.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1455.raw"
	bzip2 "/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1455.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1456.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1456.raw"
	bzip2 "/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1456.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1457.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1457.raw"
	bzip2 "/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1457.raw"
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/uncompressed" \
		".raw.bz2" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmentiremaleabdomensmall:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" \
		"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen" \
		".raw.Z" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmsubsetmaleabdomensmall:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/compressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	for i in \
		$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm14[0-9][0-9].raw.Z \
		$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm15[0-9][0-9].raw.Z \
		$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm16[0-9][0-9].raw.Z \
		; do cp $$i /tmp/vhm/testfiles/srcimages/compressed/; done
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" \
		"/tmp/vhm/testfiles/srcimages/compressed" \
		".raw.Z" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100


testunencap:	UnencapsulateCompressedPixelData.class
	mkdir -p /tmp/crapunencap
	rm -f /tmp/crapunencap/*
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.UnencapsulateCompressedPixelData \
			"$${HOME}/Pictures/Medical/MultiframeEncapsulated/JPEGBaseline/Acuson/ACUSON-24-YBR_FULL_422-Jpeg_Baseline_1.dcm" \
			/tmp/crapunencap/frame_

testreencap:	UnencapsulateCompressedPixelData.class EncapsulateCompressedPixelData.class
	cp "$${HOME}/Pictures/Medical/MultiframeEncapsulated/JPEGBaseline/Acuson/ACUSON-24-YBR_FULL_422-Jpeg_Baseline_1.dcm" /tmp/srcfile.dcm
	mkdir -p /tmp/crapunencap
	rm -f /tmp/crapunencap/*
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.UnencapsulateCompressedPixelData \
			/tmp/srcfile.dcm \
			/tmp/crapunencap/frame_
	rm -f /tmp/crapreencap.dcm
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.EncapsulateCompressedPixelData \
			/tmp/crapreencap.dcm \
			/tmp/srcfile.dcm \
			/tmp/crapunencap/frame_*
	#rm -f /tmp/srcfile.dcm

/tmp/grayscale2008.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2008/SPL_PNL_Brain_Atlas2008/01-MRI/grayscale.nrrd" \
		/tmp/grayscale2008.dcm \
		"Brain^Atlas SPL-PNL 2008" 2008 5678 100 1 \
		MR

/tmp/labelmap2008.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2008/SPL_PNL_Brain_Atlas2008/02-labels/seg.nrrd" \
		/tmp/labelmap2008.dcm \
		"Brain^Atlas SPL-PNL 2008" 2008 5678 101 1

# use 2007 label map since none for 2008 ... some values are missing (and some in map are unused, mostly duplicates, but also pituitary)
testlabelmapbrainatlas2008:	IndexedLabelMapToSegmentation.class /tmp/labelmap2008.dcm /tmp/grayscale2008.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2008.dcm \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2007/SPL-BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2008.dcm \
		/tmp/grayscale2008.dcm \
		102 "Segmented Brain" BRAIN_ATLAS_2008 "SPL-PNL Brain Atlas 2008" "SPL-PNL^Talos et al"
	dcentvfy /tmp/seg2008.dcm /tmp/grayscale2008.dcm



/tmp/grayscale2013_10.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-brain-atlas-1.0/volumes/A1_grayT1.nrrd" \
		/tmp/grayscale2013_10.dcm \
		"Brain^Atlas NAC 2013" 2013 5678 100 1 \
		MR

/tmp/labelmap2013_10.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-brain-atlas-1.0/volumes/A1_labels.nrrd" \
		/tmp/labelmap2013_10.dcm \
		"Brain^Atlas NAC 2013" 2013 5678 101 1

testlabelmapbrainatlas2013_10:	IndexedLabelMapToSegmentation.class /tmp/labelmap2013_10.dcm /tmp/grayscale2013_10.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2013_10.dcm \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2013_10.dcm \
		/tmp/grayscale2013_10.dcm \
		102 "Segmented Brain" BRAIN_ATLAS_2013 "NAC Brain Atlas 2013" "NAC^Halle et al"
	dcentvfy /tmp/seg2013_10.dcm /tmp/grayscale2013_10.dcm



/tmp/grayscale2013_nac_hncma.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-hncma-atlas2013-Slicer4Version/Data/A1_grayT1.nrrd" \
		/tmp/grayscale2013_nac_hncma_fp.dcm \
		"Brain^Atlas NAC HNCMA 2013" 2013 5678 100 1 \
		MR
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		/tmp/grayscale2013_nac_hncma_fp.dcm /tmp/grayscale2013_nac_hncma.dcm

/tmp/labelmap2013_nac_hncma.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-hncma-atlas2013-Slicer4Version/Data/hncma-atlas.nrrd" \
		/tmp/labelmap2013_nac_hncma.dcm \
		"Brain^Atlas NAC HNCMA 2013" 2013 5678 101 1

testlabelmapbrainatlas2013_nac_hncma:	IndexedLabelMapToSegmentation.class /tmp/labelmap2013_nac_hncma.dcm /tmp/grayscale2013_nac_hncma.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2013_nac_hncma.dcm \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2013_nac_hncma.dcm \
		/tmp/grayscale2013_nac_hncma.dcm \
		102 "Segmented Brain Head Neck Muscles" BRAIN_ATLAS_2013 "NAC HNCMA Brain Atlas 2013" "NAC^Halle et al"
	#dcentvfy /tmp/seg2013_nac_hncma.dcm /tmp/grayscale2013_nac_hncma.dcm



/tmp/labelmap2013_nac_hncma_skin.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-hncma-atlas2013-Slicer4Version/Data/skin.nrrd" \
		/tmp/labelmap2013_nac_hncma_skin.dcm \
		"Brain^Atlas NAC HNCMA 2013" 2013 5678 101 1

testlabelmapbrainatlas2013_nac_hncma_skin:	IndexedLabelMapToSegmentation.class /tmp/labelmap2013_nac_hncma_skin.dcm /tmp/grayscale2013_nac_hncma.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2013_nac_hncma_skin.dcm \
		"${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2013_nac_hncma_skin.dcm \
		/tmp/grayscale2013_nac_hncma.dcm \
		102 "Segmented Brain Head Neck Muscles" BRAIN_ATLAS_2013 "NAC HNCMA Brain Atlas 2013" "NAC^Halle et al"
	#dcentvfy /tmp/seg2013_nac_hncma_skin.dcm /tmp/grayscale2013_nac_hncma_skin.dcm



/tmp/grayscaleprostate.dcm:
	mkdir -p /tmp/grayscaleprostate
	rm -rf /tmp/grayscaleprostate/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.MultiFrameImageFactory \
		"$${HOME}/Pictures/Medical/NCI-ISBI_2013_ProstateChallenge/TrainingFromTCIA/PROSTATE-DIAGNOSIS/ProstateDx-01-0006/1.3.6.1.4.1.14519.5.2.1.4792.2002.208539110710641195855467414500/000000" \
		/tmp/grayscaleprostate
	mv /tmp/grayscaleprostate/* /tmp/grayscaleprostate.dcm

/tmp/labelmapprostate.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Pictures/Medical/NCI-ISBI_2013_ProstateChallenge/ProstateDx-NRRD-T2W_TSE_AX-05-07-2012/ProstateDx-01-0006_T2W_TSE_AX.nrrd" \
		/tmp/labelmapprostate.dcm \
		ProstateDx-01-0006 ProstateDx-01-0006 "" 100 1

testllabelmapprostate:	IndexedLabelMapToSegmentation.class /tmp/labelmapprostate.dcm /tmp/grayscaleprostate.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmapprostate.dcm \
		"${HOME}/Pictures/Medical/NCI-ISBI_2013_ProstateChallenge/labelmapfortruth_diagnosis.csv" \
		/tmp/segprostate.dcm \
		/tmp/grayscaleprostate.dcm \
		102 "Segmented Prostate" PROSTATE_DIAGNOSIS "Prostate Diagnosis Collection Segmentations" "ProstateDx-01-0006"
	#dcentvfy /tmp/segprostate.dcm /tmp/grayscaleprostate.dcm

