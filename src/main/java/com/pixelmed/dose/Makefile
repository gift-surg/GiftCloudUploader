PATHTOROOT = ../../..

OBJS = \
CommonDoseObserverContext.class \
CTAcquisitionParameters.class \
CTDose.class \
CTDoseAcquisition.class \
CTDoseCheckDetails.class \
CTIrradiationEventDataFromImages.class \
CTPhantomType.class \
CTScanType.class \
DeviceParticipant.class \
DoseCompositeInstanceContext.class \
PersonParticipant.class \
RadiationDoseStructuredReport.class \
RadiationDoseStructuredReportFactory.class \
RecordingDeviceObserverContext.class \
RoleInOrganization.class \
RoleInProcedure.class \
ScanRange.class \
ScopeOfDoseAccummulation.class \
SourceOfDoseInformation.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../dose.`date '+%Y%m%d'`.tar.gz

clean:
	rm -f *~ *.class core *.bak ${OBJS}

testCTIrradiationEventDataFromImages:	${OBJS}
	@echo "Using standard attributes ..."
	java -Xmx512m -cp .:${PATHTOROOT}:./jai_imageio.jar -Djava.awt.headless=true com.pixelmed.dose.CTIrradiationEventDataFromImages \
		${PATHTOROOT}/testpaths/DoseImages/IOImages/cdsWithSeries999/CD0807141045/DICOM/F001/S000003/I000076.DCM
	@echo "Using private attributes ..."
	dccp -nodisclaimer -ignorereaderrors ${PATHTOROOT}/testpaths/DoseImages/IOImages/cdsWithSeries999/CD0807141045/DICOM/F001/S000003/I000076.DCM /tmp/testCTIrradiationEventDataFromImages.dcm -d '(0x0009,0x1110)' -d SingleCollimationWidth -d TotalCollimationWidth -d SpiralPitchFactor
	java -Xmx512m -cp .:${PATHTOROOT}:./jai_imageio.jar -Djava.awt.headless=true com.pixelmed.dose.CTIrradiationEventDataFromImages /tmp/testCTIrradiationEventDataFromImages.dcm
	rm -f /tmp/testCTIrradiationEventDataFromImages.dcm
	@echo "Computing pitch factor ..."
	dccp -nodisclaimer -ignorereaderrors ${PATHTOROOT}/testpaths/DoseImages/IOImages/cdsWithSeries999/CD0807141045/DICOM/F001/S000003/I000076.DCM /tmp/testCTIrradiationEventDataFromImages.dcm -d '(0x0009,0x1110)' -d SpiralPitchFactor -d '(0x0043,0x1027)'
	java -Xmx512m -cp .:${PATHTOROOT}:./jai_imageio.jar -Djava.awt.headless=true com.pixelmed.dose.CTIrradiationEventDataFromImages /tmp/testCTIrradiationEventDataFromImages.dcm
	rm -f /tmp/testCTIrradiationEventDataFromImages.dcm
