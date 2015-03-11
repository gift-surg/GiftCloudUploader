PATHTOROOT = ../../..

OBJS = \
DicomAndWebStorageServer.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../server.`date '+%Y%m%d'`.tar.gz

testserver:	${OBJS}
	java  -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}:${SERVERADDITIONALJARS} com.pixelmed.server.DicomAndWebStorageServer testserver.properties

testservernoargs:	${OBJS}
	java  -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}:${SERVERADDITIONALJARS} com.pixelmed.server.DicomAndWebStorageServer

testserverwithjaronly:
	java  -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}/pixelmed.jar:${PATHTOADDITIONAL}/aiviewer.jar:${SERVERADDITIONALJARS} com.pixelmed.server.DicomAndWebStorageServer testserver.properties

graymaxserver:
	java  -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}/pixelmed.jar:${PATHTOADDITIONAL}/aiviewer.jar:${SERVERADDITIONALJARS} com.pixelmed.server.DicomAndWebStorageServer graymax.properties
