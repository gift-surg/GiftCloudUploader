PATHTOROOT = ../../..

OBJS = \
DatabaseApplicationProperties.class \
DatabaseInformationModel.class \
DatabaseMediaImporter.class \
DatabaseTreeModel.class \
DatabaseTreeBrowser.class \
DatabaseTreeRecord.class \
DateTimeRangeMatch.class \
DeleteFromDatabase.class \
DicomDatabaseInformationModel.class \
DicomDatabaseQueryResponseGenerator.class \
DicomDatabaseQueryResponseGeneratorFactory.class \
DicomDatabaseRetrieveResponseGenerator.class \
DicomDatabaseRetrieveResponseGeneratorFactory.class \
DicomDictionaryForMinimalPatientStudySeriesInstanceModel.class \
DicomDictionaryForPatientStudySeriesConcatenationInstanceModel.class \
DicomDictionaryForStudySeriesInstanceModel.class \
MinimalPatientStudySeriesInstanceModel.class \
PatientStudySeriesConcatenationInstanceModel.class \
MapTableBrowser.class \
MapTableModel.class \
RebuildDatabaseFromInstanceFiles.class \
StudySeriesInstanceModel.class \
StudySeriesInstanceSelectiveMatchModel.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

archive:
	tar -cvf - Makefile *.java | gzip -best > ${PATHTOROOT}/database.`date '+%Y%m%d'`.tar.gz

test.empty:
	rm -f test.*
	java -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.PatientStudySeriesConcatenationInstanceModel

test.existing:
	java -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.PatientStudySeriesConcatenationInstanceModel

test.addjpeg2000:
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.PatientStudySeriesConcatenationInstanceModel \
		${PATHTOROOT}/../../dctool.support/images/dicom/forjpeg2000/discimg/[A-Z][A-Z][0-9]

test.addnemamfmr:
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.PatientStudySeriesConcatenationInstanceModel \
		${PATHTOROOT}/../../nemamfmr/DISCIMG/IMAGES/*

test.datetime:	DateTimeRangeMatch.class
	java -cp ${PATHTOROOT} com.pixelmed.database.DateTimeRangeMatch

rebuilddatabase:	RebuildDatabaseFromInstanceFiles.class
	rm -f ${PATHTOHOME}/.com.pixelmed.display.DicomImageViewer.database.*
	java -Xmx512m -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.RebuildDatabaseFromInstanceFiles \
		PatientStudySeriesConcatenationInstanceModel \
		${PATHTOHOME}/.com.pixelmed.display.DicomImageViewer.database \
		${PATHTOHOME}/.com.pixelmed.display.DicomImageViewer.images

testdeletepatient:	DeleteFromDatabase.class RebuildDatabaseFromInstanceFiles.class
	rm -rf /tmp/testdeletepatient
	mkdir -p /tmp/testdeletepatient/images
	mkdir -p /tmp/testdeletepatient/database
	dcsmpte -r PatientID REMOVEME /tmp/testdeletepatient/images/crap.dcm
	java -Xmx512m -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.RebuildDatabaseFromInstanceFiles \
		PatientStudySeriesConcatenationInstanceModel \
		/tmp/testdeletepatient/database \
		/tmp/testdeletepatient/images
	java -Xmx512m -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.DeleteFromDatabase \
		PatientStudySeriesConcatenationInstanceModel \
		/tmp/testdeletepatient/database \
		PATIENT REMOVEME
	ls -l /tmp/testdeletepatient/images
	# try it again ... should do nothing ...
	java -Xmx512m -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.DeleteFromDatabase \
		PatientStudySeriesConcatenationInstanceModel \
		/tmp/testdeletepatient/database \
		PATIENT REMOVEME

testdeletestudy:	DeleteFromDatabase.class RebuildDatabaseFromInstanceFiles.class
	rm -rf /tmp/testdeletestudy
	mkdir -p /tmp/testdeletestudy/images
	mkdir -p /tmp/testdeletestudy/database
	dcsmpte -r PatientID REMOVEME /tmp/testdeletestudy/images/crap.dcm
	java -Xmx512m -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.RebuildDatabaseFromInstanceFiles \
		PatientStudySeriesConcatenationInstanceModel \
		/tmp/testdeletestudy/database \
		/tmp/testdeletestudy/images
	studyinstanceuid=`dckey -k StudyInstanceUID /tmp/testdeletestudy/images/crap.dcm 2>&1`; \
	java -Xmx512m -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.DeleteFromDatabase \
		PatientStudySeriesConcatenationInstanceModel \
		/tmp/testdeletestudy/database \
		STUDY $${studyinstanceuid}
	ls -l /tmp/testdeletestudy/images
	# try it again ... should do nothing ...
	java -Xmx512m -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
		com.pixelmed.database.DeleteFromDatabase \
		PatientStudySeriesConcatenationInstanceModel \
		/tmp/testdeletestudy/database \
		STUDY $${studyinstanceuid}

	
	
