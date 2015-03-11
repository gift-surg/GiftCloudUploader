PATHTOROOT = ../../..

OBJS = \
FilterPanel.class \
QueryInformationModel.class \
QueryResponseGenerator.class \
QueryTreeModel.class \
QueryTreeBrowser.class \
QueryTreeRecord.class \
RetrieveResponseGenerator.class \
StudyRootQueryInformationModel.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../query.`date '+%Y%m%d'`.tar.gz

testquery: StudyRootQueryInformationModel.class
	java -cp ${PATHTOROOT} com.pixelmed.query.StudyRootQueryInformationModel localhost 4006 OSIRIX_GRAYTOO us
	#java -cp ${PATHTOROOT} com.pixelmed.query.StudyRootQueryInformationModel localhost 4006 GRAYTOO_DIV_4006 us
	#java -cp ${PATHTOROOT} com.pixelmed.query.StudyRootQueryInformationModel rpleo 104 rpleo GRAYTOO_DIV_4006
	#java -cp ${PATHTOROOT} com.pixelmed.query.StudyRootQueryInformationModel 10.217.142.23 3010 EV1 GRAYTOO_DIV_4006
	#java -cp ${PATHTOROOT} com.pixelmed.query.StudyRootQueryInformationModel 10.217.142.22 104 rpprovDISK GRAYTOO_DIV_4006
