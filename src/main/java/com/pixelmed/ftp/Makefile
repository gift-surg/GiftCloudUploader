PATHTOROOT = ../../..

OBJS = \
FTPApplicationProperties.class \
FTPClientApplicationConfigurationDialog.class \
FTPException.class \
FTPFileSender.class \
FTPRemoteHost.class \
FTPRemoteHostConfigurationDialog.class \
FTPRemoteHostInformation.class \
FTPSecurityType.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

archive:
	tar -cvf - Makefile *.java | gzip -best > ${PATHTOROOT}/ftp.`date '+%Y%m%d'`.tar.gz

testftpfilesendesecure:	FTPFileSender.class
	echo "Hello, world." >/tmp/testftpfilesendesecure.tmp
	java -cp ${PATHTOROOT}:${FTPADDITIONALJARS} com.pixelmed.ftp.FTPFileSender \
		ftpstest.forus.com ftp_testing ftp_testing - SECURE RANDOM 1 \
		/tmp/testftpfilesendesecure.tmp
	rm /tmp/testftpfilesendesecure.tmp


testremotehostconfigurationdialog:	${OBJS}
	java -cp ${PATHTOROOT}:${FTPADDITIONALJARS} \
		com.pixelmed.ftp.FTPRemoteHostConfigurationDialog

testconfigurator:	${OBJS}
	#if [ -f /tmp/testproperties ]; then mv /tmp/testproperties /tmp/testproperties.old; fi
	touch /tmp/testproperties
	java -cp ${PATHTOROOT}:${FTPADDITIONALJARS} \
		com.pixelmed.ftp.FTPClientApplicationConfigurationDialog /tmp/testproperties
	if [ -f /tmp/testproperties.old ]; then echo "Comparing old properties file with new"; diff /tmp/testproperties.old /tmp/testproperties; fi
	echo "Saved properties file contains ..."
	cat /tmp/testproperties
	#rm -f /tmp/testproperties
