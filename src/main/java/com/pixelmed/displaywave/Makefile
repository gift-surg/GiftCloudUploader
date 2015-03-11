PATHTOROOT = ../../..

OBJS =	ECGPanel.class \
	ECGViewer.class \
	DicomSourceECG.class \
	RawSourceECG.class \
	SCPSourceECG.class \
	SourceECG.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

archive:
	tar -cvf - Makefile *.java | gzip -best > ${PATHTOROOT}/displaywave.`date '+%Y%m%d'`.tar.gz

testrawecg:	${OBJS}
	java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGPanel RAW PFE105.dat 8 5000 4 2 2 0.001 0

testscphighecg:	${OBJS}
	java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGPanel SCPECG PFE105_high.scp 4 3 0

testscpredredecg:	${OBJS}
	java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGPanel SCPECG PFE105_redred.scp 4 3 0

testdicomexcelecg:	${OBJS} 
	java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGPanel DICOM 39da47b7.dcm 4 3 0

testdicomscpconvecg:	${OBJS} 
	java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGPanel DICOM 1074466202.fixed.dcm 4 3 0

testviewer:	${OBJS}
	java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGViewer

testviewscphighecg:	${OBJS}
	java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGViewer PFE105_high.scp

testviewscpredredecg:	${OBJS}
	java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGViewer PFE105_redred.scp

testviewdicomxcelecg:	${OBJS} 
	java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGViewer 39da47b7.dcm

testviewdicomscpconvecg:	${OBJS} 
	java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGViewer 1074466202.fixed.dcm
	
testallscpecghigh:	${OBJS}
	for i in \
		${PATHTOROOT}/${PATHTOSCPECGSAMPLESFROMROOT}/ECGsamplesc/scp_highc/*.scp; \
	do \
		echo $$i; \
		java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGViewer $$i; \
	done

testallscpecgredred:	${OBJS}
	for i in \
		${PATHTOROOT}/${PATHTOSCPECGSAMPLESFROMROOT}/ECGsamplesc/scp_redredc/*.scp; \
	do \
		echo $$i; \
		java -cp ${PATHTOROOT} com.pixelmed.displaywave.ECGViewer $$i; \
	done

testallscpecg:	testallscpecghigh testallscpecgredred


