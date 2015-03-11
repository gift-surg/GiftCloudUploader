PATHTOROOT = ../../..

OBJS = \
HuffmanTable.class \
DefaultHuffmanTable.class \
HuffmanDecoder.class \
Section.class \
Section0.class \
Section1.class \
Section2.class \
Section3.class \
Section4.class \
Section5Or6.class \
Section7.class \
Section8Or11.class \
Section10.class \
SectionHeader.class \
RecordHeader.class \
SCPECG.class \
SCPTree.class \
SCPTreeBrowser.class \
SCPTreeRecord.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

TESTRESULTDIR = ${PATHTOROOT}/testresults/scpecg

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../scpecg.`date '+%Y%m%d'`.tar.gz

testhuffman:	HuffmanDecoder.class
	java -cp ${PATHTOROOT} com.pixelmed.scpecg.HuffmanDecoder

test:	${OBJS}
	java -cp ${PATHTOROOT} com.pixelmed.scpecg.SCPECG \
		${PATHTOROOT}/${PATHTOSCPECGSAMPLESFROMROOT}/ECGSamples/scp_redred/PFE105.scp \
		/tmp/scp_redred_PFE105.raw \
		/tmp/scp_redred_PFE105.txt
	java -cp ${PATHTOROOT} com.pixelmed.scpecg.SCPECG \
		${PATHTOROOT}/${PATHTOSCPECGSAMPLESFROMROOT}/ECGSamplesc/scp_redredc/PFE105c.scp \
		/tmp/scp_redredc_PFE105c.raw \
		/tmp/scp_redredc_PFE105c.txt
	java -cp ${PATHTOROOT} com.pixelmed.scpecg.SCPECG \
		${PATHTOROOT}/${PATHTOSCPECGSAMPLESFROMROOT}/ECGSamples/scp_high/PFE105.scp \
		/tmp/scp_high_PFE105.raw \
		/tmp/scp_high_PFE105.txt
	java -cp ${PATHTOROOT} com.pixelmed.scpecg.SCPECG \
		${PATHTOROOT}/${PATHTOSCPECGSAMPLESFROMROOT}/ECGSamplesc/scp_highc/PFE105c.scp \
		/tmp/scp_highc_PFE105c.raw \
		/tmp/scp_highc_PFE105c.txt

testallscpecghigh:	${OBJS}
	mkdir -p ${TESTRESULTDIR}/scp_highc
	for i in \
		${PATHTOROOT}/${PATHTOSCPECGSAMPLESFROMROOT}/ECGsamplesc/scp_highc/*.scp; \
	do \
		echo $$i; \
		java -cp ${PATHTOROOT} com.pixelmed.scpecg.SCPECG $$i ${TESTRESULTDIR}/scp_highc/`basename $$i .scp`.raw ${TESTRESULTDIR}/scp_highc/`basename $$i .scp`.txt ; \
	done

testallscpecgredred:	${OBJS}
	mkdir -p ${TESTRESULTDIR}/scp_redredc
	for i in \
		${PATHTOROOT}/${PATHTOSCPECGSAMPLESFROMROOT}/ECGsamplesc/scp_redredc/*.scp; \
	do \
		echo $$i; \
		java -cp ${PATHTOROOT} com.pixelmed.scpecg.SCPECG $$i ${TESTRESULTDIR}/scp_redredc/`basename $$i .scp`.raw ${TESTRESULTDIR}/scp_redredc/`basename $$i .scp`.txt ; \
	done

testallscpecg:	testallscpecghigh testallscpecgredred


