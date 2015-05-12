PATHTOROOT = ../../..

OBJS = \
AAbortPDU.class \
AnyExplicitTransferSyntaxSelectionPolicy.class \
AnyExplicitStorePresentationContextSelectionPolicy.class \
AnyExplicitStoreFindMoveGetPresentationContextSelectionPolicy.class \
ApplicationEntity.class \
ApplicationEntityConfigurationDialog.class \
ApplicationEntityMap.class \
AReleaseException.class \
AReleasePDU.class \
AssociateRejectPDU.class \
AssociateAcceptPDU.class \
AssociateRequestPDU.class \
AssociateRequestAcceptPDU.class \
Association.class \
AssociationAcceptor.class \
AssociationInitiator.class \
AssociationFactory.class \
AssociationOutputStream.class \
AssociationStatusHandler.class \
CEchoRequestCommandMessage.class \
CEchoResponseCommandMessage.class \
CFindRequestCommandMessage.class \
CFindResponseCommandMessage.class \
CGetRequestCommandMessage.class \
CGetResponseCommandMessage.class \
CMoveRequestCommandMessage.class \
CMoveResponseCommandMessage.class \
CommandMessage.class \
CompositeInstanceStoreAbstractSyntaxSelectionPolicy.class \
CompositeInstanceStoreFindMoveGetAbstractSyntaxSelectionPolicy.class \
CompositeResponseHandler.class \
CStoreRequestCommandMessage.class \
CStoreResponseCommandMessage.class \
DicomNetworkException.class \
FindSOPClassSCU.class \
GetSOPClassSCU.class \
IdentifierHandler.class \
IdentifierMessage.class \
MessageServiceElementCommand.class \
MoveSOPClassSCU.class \
MultipleInstanceTransferStatusHandler.class \
MultipleInstanceTransferStatusHandlerWithFileName.class \
NetworkApplicationInformation.class \
NetworkApplicationInformationFederated.class \
NetworkApplicationProperties.class \
NetworkConfigurationFromLDAP.class \
NetworkConfigurationFromMulticastDNS.class \
NetworkConfigurationSource.class \
NetworkDefaultValues.class \
NetworkMediaImporter.class \
PDataPDU.class \
PresentationAddress.class \
PresentationContext.class \
PresentationContextListFactory.class \
PresentationDataValue.class \
ProbeCapability.class \
ReceivedDataHandler.class \
ReceivedFilePathStrategy.class \
ReceivedObjectHandler.class \
RequestCommandMessage.class \
ResponseStatus.class \
SCUSCPRoleSelection.class \
StorageSOPClassSCP.class \
StorageSOPClassSCPDispatcher.class \
StorageSOPClassSCU.class \
StorageSOPClassSCUPerformanceTest.class \
TestSendingCommandAndDataInOnePDU.class \
TransferSyntaxSelectionPolicy.class \
UnencapsulatedExplicitTransferSyntaxSelectionPolicy.class \
UnencapsulatedExplicitStorePresentationContextSelectionPolicy.class \
UnencapsulatedExplicitStoreFindMoveGetPresentationContextSelectionPolicy.class \
VerificationSOPClassSCU.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../network.`date '+%Y%m%d'`.tar.gz

testprobe:	ProbeCapability.class
	java -cp ${PATHTOROOT} com.pixelmed.network.ProbeCapability localhost 4006 HELGRAY US 0
	java -cp ${PATHTOROOT} com.pixelmed.network.ProbeCapability localhost HELGRAY US 0

testconfig:	NetworkConfigurationFromLDAP.class ApplicationEntity.class
	java -cp ${PATHTOROOT} com.pixelmed.network.NetworkConfigurationFromLDAP

testproperties:	NetworkApplicationProperties.class
	java -cp ${PATHTOROOT} com.pixelmed.network.NetworkApplicationProperties ../server/testserver.properties "o=pixelmed,c=us"

testconfigmdns:	NetworkConfigurationFromLDAP.class ApplicationEntity.class
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jmdns.jar com.pixelmed.network.NetworkConfigurationFromMulticastDNS HELGRAY 4006 WSD

testscuperf: StorageSOPClassSCUPerformanceTest.class
	dcsmpte /tmp/testscuperf
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jmdns.jar com.pixelmed.network.StorageSOPClassSCUPerformanceTest localhost 4007 LOCAL4007 STORESCU 0 0 0 100 1 0 0 0 /tmp/testscuperf
	rm /tmp/testscuperf

testimporter:	NetworkMediaImporter.class
	mkdir -p /tmp/testimport
	dcsmpte /tmp/testimport/crap
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkMediaImporter \
		localhost 4006 HELGRAY IMPORTER /tmp/testimport
	rm -rf /tmp/testimport

testaedialog:	ApplicationEntityConfigurationDialog.class
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.ApplicationEntityConfigurationDialog

testconfigurator:	NetworkApplicationConfigurationDialog.class
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar:${BZIP2ADDITIONALJAR} \
		uk.ac.ucl.cs.cmic.giftcloud.uploadapp.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testreceivedfilestrategysingle:	ReceivedFilePathStrategySingleFolder.class
	java -cp ${PATHTOROOT} com.pixelmed.network.ReceivedFilePathStrategySingleFolder

testreceivedfilestrategycomponent:	ReceivedFilePathStrategyComponentFolders.class
	java -cp ${PATHTOROOT} com.pixelmed.network.ReceivedFilePathStrategyComponentFolders

testreceivedfilestrategyhash:	ReceivedFilePathStrategyHashSubFolders.class
	java -cp ${PATHTOROOT} com.pixelmed.network.ReceivedFilePathStrategyHashSubFolders

testreceivedfilestrategy:	\
	testreceivedfilestrategysingle \
	testreceivedfilestrategycomponent \
	testreceivedfilestrategyhash

testpixelmedsendtopixelmed:
	@rm -rf /tmp/testpixelmedsendtopixelmed.receivedfiles
	@mkdir -p /tmp/testpixelmedsendtopixelmed.receivedfiles
	./runprocessinbackground.sh \
		/tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.network.StorageSOPClassSCPDispatcher 4006 STORESCP /tmp/testpixelmedsendtopixelmed.receivedfiles INSECURE 0
	@#echo "PID of com.pixelmed.network.StorageSOPClassSCPDispatcher is "`cat /tmp/testpixelmedsendtopixelmed.pid`
	dcsmpte -nodisclaimer -r PatientName testpixelmedsendtopixelmed.1 /tmp/testpixelmedsendtopixelmed.1.dcm
	java -cp ${PATHTOROOT} com.pixelmed.network.StorageSOPClassSCU localhost 4006 STORESCP US /tmp/testpixelmedsendtopixelmed.1.dcm 0 0
	@echo "Comparing sent and received files - expect no difference except meta header"
	@receivedfilename=`grep 'sendReceivedObjectIndication() fileName' /tmp/testpixelmedsendtopixelmed.stderr | tail -1 | sed -e 's/^.*fileName: //' -e 's/ from .*$$//'`; \
		andiff /tmp/testpixelmedsendtopixelmed.1.dcm "$${receivedfilename}"; \
		dccmp /tmp/testpixelmedsendtopixelmed.1.dcm "$${receivedfilename}"
	@echo "Comparison done"
	@kill -9 `cat /tmp/testpixelmedsendtopixelmed.pid`
	@rm -rf /tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		/tmp/testpixelmedsendtopixelmed.receivedfiles \
		/tmp/testpixelmedsendtopixelmed.1.dcm

testpixelmedsendmultitopixelmed:
	@rm -rf /tmp/testpixelmedsendtopixelmed.receivedfiles
	@mkdir -p /tmp/testpixelmedsendtopixelmed.receivedfiles
	./runprocessinbackground.sh \
		/tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.network.StorageSOPClassSCPDispatcher 4006 STORESCP /tmp/testpixelmedsendtopixelmed.receivedfiles INSECURE 0
	@#echo "PID of com.pixelmed.network.StorageSOPClassSCPDispatcher is "`cat /tmp/testpixelmedsendtopixelmed.pid`
	dcsmpte -nodisclaimer -r PatientName testpixelmedsendtopixelmed.1 /tmp/testpixelmedsendtopixelmed.1.dcm
	dcsmpte -nodisclaimer -r PatientName testpixelmedsendtopixelmed.2 /tmp/testpixelmedsendtopixelmed.2.dcm
	@echo "/tmp/testpixelmedsendtopixelmed.1.dcm" >/tmp/testpixelmedsendtopixelmed.filelist
	@echo "/tmp/testpixelmedsendtopixelmed.2.dcm" >>/tmp/testpixelmedsendtopixelmed.filelist
	java -cp ${PATHTOROOT} com.pixelmed.network.StorageSOPClassSCU localhost 4006 STORESCP US - 0 0 </tmp/testpixelmedsendtopixelmed.filelist
	#ls -l /tmp/testpixelmedsendtopixelmed.receivedfiles
	#cat /tmp/testpixelmedsendtopixelmed.stderr
	@echo "Comparing sent and received 1st files - expect no difference except meta header"
	@receivedfilename=`grep 'sendReceivedObjectIndication() fileName' /tmp/testpixelmedsendtopixelmed.stderr | tail -1 | sed -e 's/^.*fileName: //' -e 's/ from .*$$//'`; \
		andiff /tmp/testpixelmedsendtopixelmed.1.dcm "$${receivedfilename}"; \
		dccmp /tmp/testpixelmedsendtopixelmed.1.dcm "$${receivedfilename}"
	@echo "Comparison done"
	@echo "Comparing sent and received 2nd files - expect no difference except meta header"
	@receivedfilename=`grep 'sendReceivedObjectIndication() fileName' /tmp/testpixelmedsendtopixelmed.stderr | head -1 | sed -e 's/^.*fileName: //' -e 's/ from .*$$//'`; \
		andiff /tmp/testpixelmedsendtopixelmed.2.dcm "$${receivedfilename}"; \
		dccmp /tmp/testpixelmedsendtopixelmed.2.dcm "$${receivedfilename}"
	@echo "Comparison done"
	@kill -9 `cat /tmp/testpixelmedsendtopixelmed.pid`
	@rm -rf /tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		/tmp/testpixelmedsendtopixelmed.receivedfiles \
		/tmp/testpixelmedsendtopixelmed.1.dcm \
		/tmp/testpixelmedsendtopixelmed.2.dcm \
		/tmp/testpixelmedsendtopixelmed.filelist

testpixelmedsendmanytopixelmed:
	@rm -rf /tmp/testpixelmedsendtopixelmed.receivedfiles
	@mkdir -p /tmp/testpixelmedsendtopixelmed.receivedfiles
	./runprocessinbackground.sh \
		/tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.network.StorageSOPClassSCPDispatcher 4006 STORESCP /tmp/testpixelmedsendtopixelmed.receivedfiles INSECURE 0
	@#echo "PID of com.pixelmed.network.StorageSOPClassSCPDispatcher is "`cat /tmp/testpixelmedsendtopixelmed.pid`
	@rm -f /tmp/testpixelmedsendtopixelmed.filelist
	count=0; while [ $${count} -lt 5000 ] ; do dcsmpte -nodisclaimer -r PatientName "testpixelmedsendtopixelmed.$${count}" "/tmp/testpixelmedsendtopixelmed.$${count}.dcm" ; echo "/tmp/testpixelmedsendtopixelmed.$${count}.dcm" >>/tmp/testpixelmedsendtopixelmed.filelist ; count=`expr $${count} + 1` ; done
	#ls -l "/tmp/testpixelmedsendtopixelmed.*.dcm"
	java -cp ${PATHTOROOT} com.pixelmed.network.StorageSOPClassSCU localhost 4006 STORESCP US - 0 0 </tmp/testpixelmedsendtopixelmed.filelist
	@sent=`wc -l </tmp/testpixelmedsendtopixelmed.filelist | sed -e 's/[ ]*//'` ; received=`ls -1 /tmp/testpixelmedsendtopixelmed.receivedfiles | wc -l | sed -e 's/[ ]*//'` ; echo "Sent $${sent}, received $${received} files"

testpixelmedcgetscutopixelmedcgetscp:
	@rm -rf /tmp/testpixelmedcgetscutopixelmedcgetscp.files
	@mkdir -p /tmp/testpixelmedcgetscutopixelmedcgetscp.files
	@rm -rf /tmp/testpixelmedcgetscutopixelmedcgetscp.db.*
	@rm -f testpixelmedcgetscutopixelmedcgetscp.properties
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Application.DatabaseFileName=/tmp/testpixelmedcgetscutopixelmedcgetscp.db"
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Application.SavedImagesFolderName=/tmp/testpixelmedcgetscutopixelmedcgetscp.files"
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Dicom.ListeningPort=4006"
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Dicom.CalledAETitle=TESTGETSCP"
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Dicom.CallingAETitle=TESTGETSCP"
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Dicom.StorageSCPDebugLevel=1"
	./runprocessinbackground.sh \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.pid /tmp/testpixelmedcgetscutopixelmedcgetscp.stdout /tmp/testpixelmedcgetscutopixelmedcgetscp.stderr \
		java -cp ${PATHTOROOT}:${SERVERADDITIONALJARS} \
			com.pixelmed.server.DicomAndWebStorageServer /tmp/testpixelmedcgetscutopixelmedcgetscp.properties
	sleep 15
	@#echo "PID of com.pixelmed.server.DicomAndWebStorageServer is "`cat /tmp/testpixelmedcgetscutopixelmedcgetscp.pid`
	dcsmpte -nodisclaimer -r PatientName testpixelmedcgetscutopixelmedcgetscp /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm
	java -cp ${PATHTOROOT} com.pixelmed.network.StorageSOPClassSCU localhost 4006 TESTGETSCP US /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 0 0
	@rm -rf /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files
	@mkdir -p /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files
	java -cp ${PATHTOROOT} com.pixelmed.network.GetSOPClassSCU localhost 4006 TESTGETSCP US \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.get.files \
		STUDY \
		`dckey -k StudyInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		2> /tmp/testpixelmedcgetscutopixelmedcgetscp.get.stderr
	@echo "Comparing sent and received files with STUDY C-GET - expect no difference except meta header"
	@andiff /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@dccmp  /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@rm -f /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@echo "Comparison done"
	java -cp ${PATHTOROOT} com.pixelmed.network.GetSOPClassSCU localhost 4006 TESTGETSCP US \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.get.files \
		SERIES \
		`dckey -k StudyInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		`dckey -k SeriesInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		2> /tmp/testpixelmedcgetscutopixelmedcgetscp.get.stderr
	@echo "Comparing sent and received files with SERIES C-GET - expect no difference except meta header"
	@andiff /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@dccmp  /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@rm -f /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@echo "Comparison done"
	java -cp ${PATHTOROOT} com.pixelmed.network.GetSOPClassSCU localhost 4006 TESTGETSCP US \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.get.files \
		IMAGE \
		`dckey -k StudyInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		`dckey -k SeriesInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		`dckey -k SOPInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		2> /tmp/testpixelmedcgetscutopixelmedcgetscp.get.stderr
	@echo "Comparing sent and received files with IMAGE C-GET - expect no difference except meta header"
	@andiff /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@dccmp  /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@rm -f /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@echo "Comparison done"
	@kill -9 `cat /tmp/testpixelmedcgetscutopixelmedcgetscp.pid`
	@rm -rf /tmp/testpixelmedcgetscutopixelmedcgetscp.pid /tmp/testpixelmedcgetscutopixelmedcgetscp.stdout /tmp/testpixelmedcgetscutopixelmedcgetscp.stderr \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.files \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.db.* \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.properties \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.dcm \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.get.files \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.get.stderr

