OBJS = AgeStringAttribute.class \
ApplicationEntityAttribute.class \
ArrayCopyUtilities.class \
AsynchronousOutputStream.class \
Attribute.class \
AttributeFactory.class \
AttributeList.class \
AttributeListTableBrowser.class \
AttributeListTableModel.class \
AttributeListFunctionalGroupsTableModel.class \
AttributeListFunctionalGroupsTableModelOneFrame.class \
AttributeListFunctionalGroupsTableModelAllFrames.class \
AttributeTag.class \
AttributeTagAttribute.class \
AttributeTest.class \
AttributeTree.class \
AttributeTreeBrowser.class \
AttributeTreeRecord.class \
BinaryInputStream.class \
BinaryOutputStream.class \
ClinicalTrialsAttributes.class \
CodedSequenceItem.class \
CodeStringAttribute.class \
CodingSchemeIdentification.class \
CodingSchemeIdentificationItem.class \
CoercionModel.class \
ColorPalette.class \
ColorPaletteHotIron.class \
ColorPaletteHotMetalBlue.class \
ColorPalettePET.class \
ColorPalettePET20Step.class \
CompositeInstanceContext.class \
ContentItem.class \
ContentItemFactory.class \
CrossSectionalImageLibrary.class \
DateAttribute.class \
DateTimeAttribute.class \
DecimalStringAttribute.class \
DescriptionFactory.class \
DicomDictionary.class \
DicomDictionaryBase.class \
DicomDirectory.class \
DicomDirectoryBrowser.class \
DicomDirectoryRecord.class \
DicomDirectoryRecordFactory.class \
DicomDirectoryRecordType.class \
DicomException.class \
DicomFileUtilities.class \
DicomInputStream.class \
DicomOutputStream.class \
DicomStreamCopier.class \
DisplayShutter.class \
EncapsulatedInputStream.class \
FileMetaInformation.class \
FloatDoubleAttribute.class \
FloatSingleAttribute.class \
FrameSet.class \
FunctionalGroupUtilities.class \
GeometryOfSliceFromAttributeList.class \
GeometryOfVolumeFromAttributeList.class \
HierarchicalImageReference.class \
HierarchicalSOPInstanceReference.class \
ImageLibrary.class \
ImageReference.class \
ImageToDicom.class \
InformationEntity.class \
IntegerStringAttribute.class \
LongStringAttribute.class \
LongTextAttribute.class \
LossyImageCompression.class \
MediaImporter.class \
ModalityTransform.class \
MoveDicomFilesIntoHierarchy.class \
MultiFrameImageFactory.class \
MultiFramePixelData.class \
NullOutputStream.class \
OtherAttributeOnDisk.class \
OtherByteAttribute.class \
OtherByteAttributeMultipleCompressedFilesOnDisk.class \
OtherByteAttributeMultipleCompressedFrames.class \
OtherByteAttributeMultipleFilesOnDisk.class \
OtherByteAttributeOnDisk.class \
OtherDoubleAttribute.class \
OtherFloatAttribute.class \
OtherWordAttribute.class \
OtherWordAttributeOnDisk.class \
Overlay.class \
PDFToDicomImage.class \
PersonIdentification.class \
PersonNameAttribute.class \
PixelSpacing.class \
PrivatePixelData.class \
ProjectionXRayImageLibrary.class \
RealWorldValueTransform.class \
SequenceAttribute.class \
SequenceItem.class \
SetOfDicomFiles.class \
SetOfFrameSets.class \
ShortStringAttribute.class \
ShortTextAttribute.class \
SignedLongAttribute.class \
SignedShortAttribute.class \
SOPClass.class \
SOPClassDescriptions.class \
SOPInstanceReference.class \
SpatialCoordinateAndImageReference.class \
SpecificCharacterSet.class \
SpectroscopyVolumeLocalization.class \
StackOfSlices.class \
StandaloneAttributeTreeBrowser.class \
StoredFilePathStrategyHashSubFolders.class \
StoredFilePathStrategySingleFolder.class \
StoredFilePathStrategyComponentFolders.class \
StoredFilePathStrategy.class \
StringAttribute.class \
StringAttributeAffectedBySpecificCharacterSet.class \
StructuredReport.class \
StructuredReportBrowser.class \
StructuredReportTreeBrowser.class \
SUVTransform.class \
TagFromName.class \
TextAttribute.class \
TimeAttribute.class \
TransferSyntax.class \
TransferSyntaxFromName.class \
UIDGenerator.class \
UniqueIdentifierAttribute.class \
UniversalResourceAttribute.class \
UnknownAttribute.class \
UnlimitedTextAttribute.class \
UniversalResourceAttribute.class \
UnPackBits.class \
UnsignedLongAttribute.class \
UnsignedShortAttribute.class \
ValueRepresentation.class \
VersionAndConstants.class \
Veterinary.class \
VOITransform.class \
XMLRepresentationOfDicomObjectFactory.class \
XMLRepresentationOfStructuredReportObjectFactory.class

all:	${OBJS}

PATHTOROOT = ../../..

include ${PATHTOROOT}/Makefile.common.mk

clean:
	rm -f *~ *.class core *.bak \
		/tmp/testDicomFile_MEVRLE_EVRLE_8.dcm \
		/tmp/testDicomFile_MEVRLE_IVRLE_8.dcm \
		/tmp/testDicomFile_MEVRLE_EVRLE_12.dcm \
		/tmp/testDicomFile_MEVRLE_IVRLE_12.dcm \
		/tmp/testDicomFile_NOM_IVRLE_12.dcm \
		/tmp/testDicomFile_NOM_EVRLE_12.dcm \
		/tmp/testsr1.dcm \
		/tmp/testfl1.dcm \
		/tmp/testfl2.dcm

archive:
	tar -cvf - Makefile *.java | gzip -best > ${PATHTOROOT}/dicom.`date '+%Y%m%d'`.tar.gz

createAttributeClasses:
	./createAttributeClasses.sh

test:	testBinaryInputStream testAttributeList_read testAttributeList_readseq


testBinaryInputStream:	BinaryInputStream.class
	touch /tmp/testBinaryInputStream
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.BinaryInputStream /tmp/testBinaryInputStream
	rm -f /tmp/testBinaryInputStream

/tmp/testDicomFile_MEVRLE_EVRLE_8.dcm:
	dcsmpte /tmp/testDicomFile_MEVRLE_EVRLE_8.dcm

/tmp/testDicomFile_MEVRLE_IVRLE_8.dcm:
	dcsmpte /tmp/testDicomFile_MEVRLE_IVRLE_8.dcm -endian little -vr implicit

/tmp/testDicomFile_MEVRLE_EVRLE_12.dcm:
	dcsmpte /tmp/testDicomFile_MEVRLE_EVRLE_12.dcm -bits 12 -minval 0 -maxval 4095 -endian little -vr explicit

/tmp/testDicomFile_MEVRLE_IVRLE_12.dcm:
	dcsmpte /tmp/testDicomFile_MEVRLE_IVRLE_12.dcm -bits 12 -minval 0 -maxval 4095 -endian little -vr implicit

/tmp/testDicomFile_NOM_IVRLE_12.dcm:
	dcsmpte /tmp/testDicomFile_NOM_IVRLE_12.dcm -bits 12 -minval 0 -maxval 4095 -endian little -vr implicit -nometa

/tmp/testDicomFile_NOM_EVRLE_12.dcm:
	dcsmpte /tmp/testDicomFile_NOM_EVRLE_12.dcm -bits 12 -minval 0 -maxval 4095 -endian little -vr explicit -nometa

testAttributeList_read:		AttributeList.class \
				/tmp/testDicomFile_MEVRLE_EVRLE_8.dcm \
				/tmp/testDicomFile_MEVRLE_IVRLE_8.dcm \
				/tmp/testDicomFile_MEVRLE_EVRLE_12.dcm \
				/tmp/testDicomFile_MEVRLE_IVRLE_12.dcm \
				/tmp/testDicomFile_NOM_IVRLE_12.dcm \
				/tmp/testDicomFile_NOM_EVRLE_12.dcm
	@echo "############# testDicomFile_MEVRLE_EVRLE_8"
	dcfile /tmp/testDicomFile_MEVRLE_EVRLE_8.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList /tmp/testDicomFile_MEVRLE_EVRLE_8.dcm /tmp/crap TEST
	@echo "comparing what was read with written"
	ls -l /tmp/testDicomFile_MEVRLE_EVRLE_8.dcm /tmp/crap
	dcdiff /tmp/testDicomFile_MEVRLE_EVRLE_8.dcm /tmp/crap
	dccmp /tmp/testDicomFile_MEVRLE_EVRLE_8.dcm /tmp/crap
	exit 0
	@echo "############# testDicomFile_MEVRLE_IVRLE_8"
	dcfile /tmp/testDicomFile_MEVRLE_IVRLE_8.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList /tmp/testDicomFile_MEVRLE_IVRLE_8.dcm /tmp/crap TEST
	@echo "comparing what was read with written"
	ls -l /tmp/testDicomFile_MEVRLE_IVRLE_8.dcm /tmp/crap
	dcdiff /tmp/testDicomFile_MEVRLE_IVRLE_8.dcm /tmp/crap
	dccmp /tmp/testDicomFile_MEVRLE_IVRLE_8.dcm /tmp/crap
	@echo "############# testDicomFile_MEVRLE_EVRLE_12"
	dcfile /tmp/testDicomFile_MEVRLE_EVRLE_12.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList /tmp/testDicomFile_MEVRLE_EVRLE_12.dcm /tmp/crap TEST
	@echo "comparing what was read with written"
	ls -l /tmp/testDicomFile_MEVRLE_EVRLE_12.dcm /tmp/crap
	dcdiff /tmp/testDicomFile_MEVRLE_EVRLE_12.dcm /tmp/crap
	dccmp /tmp/testDicomFile_MEVRLE_EVRLE_12.dcm /tmp/crap
	@echo "############# testDicomFile_MEVRLE_IVRLE_12"
	dcfile /tmp/testDicomFile_MEVRLE_IVRLE_12.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList /tmp/testDicomFile_MEVRLE_IVRLE_12.dcm /tmp/crap TEST
	@echo "comparing what was read with written"
	ls -l /tmp/testDicomFile_MEVRLE_IVRLE_12.dcm /tmp/crap
	dcdiff /tmp/testDicomFile_MEVRLE_IVRLE_12.dcm /tmp/crap
	dccmp /tmp/testDicomFile_MEVRLE_IVRLE_12.dcm /tmp/crap
	@echo "############# testDicomFile_NOM_IVRLE_12"
	dcfile /tmp/testDicomFile_NOM_IVRLE_12.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList /tmp/testDicomFile_NOM_IVRLE_12.dcm /tmp/crap TEST
	@echo "comparing what was read with written"
	ls -l /tmp/testDicomFile_NOM_IVRLE_12.dcm /tmp/crap
	dcdiff /tmp/testDicomFile_NOM_IVRLE_12.dcm /tmp/crap
	dccmp /tmp/testDicomFile_NOM_IVRLE_12.dcm /tmp/crap
	@echo "############# testDicomFile_NOM_EVRLE_12"
	dcfile /tmp/testDicomFile_NOM_EVRLE_12.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList /tmp/testDicomFile_NOM_EVRLE_12.dcm /tmp/crap TEST
	@echo "comparing what was read with written"
	ls -l /tmp/testDicomFile_NOM_EVRLE_12.dcm /tmp/crap
	dcdiff /tmp/testDicomFile_NOM_EVRLE_12.dcm /tmp/crap
	dccmp /tmp/testDicomFile_NOM_EVRLE_12.dcm /tmp/crap

testAttributeList_readseq:	AttributeList.class \
				${PATHTOHOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/DICOMDIR
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList ${PATHTOHOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/DICOMDIR /tmp/crap TEST
	@echo "comparing what was read with written"
	ls -l ${PATHTOHOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/DICOMDIR /tmp/crap
	dcdiff ${PATHTOHOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/DICOMDIR /tmp/crap
	dccmp ${PATHTOHOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/DICOMDIR /tmp/crap

testattribute:	AttributeTest.class
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeTest

testattributetag:	AttributeTag.class
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeTag

testDICOMDIR:	DicomDirectory.class DicomDirectoryRecord.class DicomDirectoryRecordFactory.class DicomDirectoryBrowser.class \
				${PATHTOHOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/DICOMDIR
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.DicomDirectoryBrowser ${PATHTOHOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/DICOMDIR


testDICOMDIR2:	DicomDirectory.class DicomDirectoryRecord.class DicomDirectoryRecordFactory.class DicomDirectoryBrowser.class \
				${PATHTOHOME}/Work/dctool.support/images/dicom/ge/opal/dd_opal
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.DicomDirectoryBrowser ${PATHTOHOME}/Work/dctool.support/images/dicom/ge/opal/dd_opal

testDICOMDIR3:	DicomDirectory.class DicomDirectoryRecord.class DicomDirectoryRecordFactory.class DicomDirectoryBrowser.class \
				${PATHTOHOME}/Work/dctool.support/images/dicom/dicomdirs/dicomdir.carestreammissingimagelowerleveloffset
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.DicomDirectoryBrowser ${PATHTOHOME}/Work/dctool.support/images/dicom/dicomdirs/dicomdir.carestreammissingimagelowerleveloffset

/tmp/testDICOMDIR4.tmp:	/tmp/testDicomFile_MEVRLE_EVRLE_8.dcm
	dcdirmk -novalidatename -noicons -noinstancechildoffset -of $@ /tmp/testDicomFile_MEVRLE_EVRLE_8.dcm

testDICOMDIR4:	DicomDirectory.class DicomDirectoryRecord.class DicomDirectoryRecordFactory.class DicomDirectoryBrowser.class \
				/tmp/testDICOMDIR4.tmp
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.DicomDirectoryBrowser /tmp/testDICOMDIR4.tmp

/tmp/testfl1.dcm:	${PATHTOHOME}/Work/dctool.support/images/dicom/rsna99.image.19991117/tests/test513.pres
	dccp $< $@

testfl1:	AttributeList.class /tmp/testfl1.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList /tmp/testfl1.dcm "" TEST

/tmp/testfl2.dcm:	${PATHTOHOME}/Work/dctool.support/images/dicom/rsna99.image.19991117/tests/test515.pres
	dccp $< $@

testfl2:	AttributeList.class /tmp/testfl2.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList /tmp/testfl2.dcm "" TEST

/tmp/testsr1.dcm:	${PATHTOHOME}/Work/dctool.support/images/dicom/sr/examplef9/examplef9.dcm
	dccp $< $@

testsr1:	StructuredReport.class ContentItem.class ContentItemFactory.class StructuredReportBrowser.class \
				/tmp/testsr1.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.StructuredReportBrowser /tmp/testsr1.dcm

testsr1dump:	StructuredReport.class ContentItem.class ContentItemFactory.class StructuredReportBrowser.class \
				/tmp/testsr1.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.StructuredReport /tmp/testsr1.dcm

testsr1tb:	StructuredReport.class ContentItem.class ContentItemFactory.class StructuredReportTreeBrowser.class \
				/tmp/testsr1.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} StructuredReportTreeBrowser /tmp/testsr1.dcm

${PATHTOHOME}/Work/dctool.support/images/dicom/sr/mytests/testscoord.dcm:	${PATHTOHOME}/Work/dctool.support/images/dicom/sr/mytests/testscoord.source
	ancreate < $< > $@.tmp
	dccp $@.tmp $@
	rm -f $@.tmp

testsr2:	StructuredReport.class ContentItem.class ContentItemFactory.class StructuredReportBrowser.class \
				${PATHTOHOME}/Work/dctool.support/images/dicom/sr/mytests/testscoord.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.StructuredReportBrowser ${PATHTOHOME}/Work/dctool.support/images/dicom/sr/mytests/testscoord.dcm

testsrhierref:	HierarchicalSOPInstanceReference.class \
				${PATHTOHOME}/Work/dctool.support/images/dicom/sr/mytests/testscoord.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.HierarchicalSOPInstanceReference ${PATHTOHOME}/Work/dctool.support/images/dicom/sr/mytests/testscoord.dcm

testsrhierref2:	HierarchicalSOPInstanceReference.class \
				${PATHTOHOME}/Work/dctool.support/images/dicom/sr/mytests/testscoord.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.HierarchicalSOPInstanceReference \
		"${PATHTOHOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/Recontexted/107166 [RIDER 2151469008]/20070208 000000/Series 90002 [SR - Completed]/1.3.6.1.4.1.5962.99.1.50.9533.1271360773857.134.1.0.2.1.dcm"

testattrlistbrowser:	AttributeListTableBrowser.class AttributeListTableModel.class \
				${PATHTOHOME}/Work/dctool.support/images/dicom/sr/mytests/testscoord.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeListTableBrowser ${PATHTOHOME}/Work/dctool.support/images/dicom/sr/mytests/testscoord.dcm

testdeflate:
	rm -f /tmp/crap /tmp/crap2 /tmp/crap3
	@echo "try AttributeList of EVRLE with meta to deflate and back"
	dcsmpte /tmp/crap -bits 12 -nometa -output-vr explicit -output-endian little
	@echo "first deflate (with metaheader)"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList "1.2.840.10008.1.2.1" /tmp/crap "1.2.840.10008.1.2.1.99" /tmp/crap2 YES
	@echo "then decompress"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList "" /tmp/crap2 "1.2.840.10008.1.2.1" /tmp/crap3 NO
	@echo "expect no difference between original and decompressed"
	andiff /tmp/crap /tmp/crap3
	@echo "expect binary comparison of original and decompressed to succeed"
	dccmp /tmp/crap /tmp/crap3
	@echo ""
	rm -f /tmp/crap /tmp/crap2 /tmp/crap3
	@echo "try DicomStreamCopier of EVRLE with meta to deflate and back"
	dcsmpte /tmp/crap -bits 12 -nometa -output-vr explicit -output-endian little
	@echo "first deflate (with metaheader)"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.DicomStreamCopier "1.2.840.10008.1.2.1" /tmp/crap "1.2.840.10008.1.2.1.99" /tmp/crap2
	@echo "then decompress"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.DicomStreamCopier "1.2.840.10008.1.2.1.99" /tmp/crap2 "1.2.840.10008.1.2.1" /tmp/crap3
	@echo "expect no difference between original and decompressed"
	andiff /tmp/crap /tmp/crap3
	@echo "expect binary comparison of original and decompressed to succeed"
	dccmp /tmp/crap /tmp/crap3

testbzip2:
	rm -f /tmp/crap /tmp/crap2 /tmp/crap3
	@echo "try AttributeList of EVRLE with meta to bzip2 and back"
	dcsmpte /tmp/crap -bits 12 -nometa -output-vr explicit -output-endian little
	@echo "first bzip2 (with metaheader)"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList "1.2.840.10008.1.2.1" /tmp/crap "1.3.6.1.4.1.5962.300.1" /tmp/crap2 YES
	@echo "then decompress"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList "" /tmp/crap2 "1.2.840.10008.1.2.1" /tmp/crap3 NO
	@echo "expect no difference between original and decompressed"
	andiff /tmp/crap /tmp/crap3
	@echo "expect binary comparison of original and decompressed to succeed"
	dccmp /tmp/crap /tmp/crap3
	@echo ""
	rm -f /tmp/crap /tmp/crap2 /tmp/crap3
	@echo "try DicomStreamCopier of EVRLE without meta to bzip2 and back"
	dcsmpte /tmp/crap -bits 12 -nometa -output-vr explicit -output-endian little
	@echo "first deflate (with metaheader)"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.DicomStreamCopier "1.2.840.10008.1.2.1" /tmp/crap "1.3.6.1.4.1.5962.300.1" /tmp/crap2
	@echo "then decompress"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.DicomStreamCopier "1.3.6.1.4.1.5962.300.1" /tmp/crap2 "1.2.840.10008.1.2.1" /tmp/crap3
	@echo "expect no difference between original and decompressed"
	andiff /tmp/crap /tmp/crap3
	@echo "expect binary comparison of original and decompressed to succeed"
	dccmp /tmp/crap /tmp/crap3

# need to invokve with make -i, else expected errors will stop progress
teststreamcopier:	DicomStreamCopier.class
	make -i COPIERCLASS=com.pixelmed.dicom.DicomStreamCopier testcopierusing

# need to invokve with make -i, else expected errors will stop progress
testattributelistcopier:	AttributeList.class
	make -i COPIERCLASS=com.pixelmed.dicom.AttributeList COPIERNOMETA=NO COPIERMETA=YES testcopierusing

testcopierusing:
	@echo "try endian conversions of 16 bit pixel data"
	dcsmpte /tmp/crap -bits 12 -nometa -output-vr explicit -output-endian little -addlengths -addlengthtoend -tiff
	@echo "explicit little to explicit big copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "1.2.840.10008.1.2.1" /tmp/crap "1.2.840.10008.1.2.2" /tmp/crap2 ${COPIERNOMETA}
	@echo "expect only differences to be group lengths and length to end and data set trailing padding"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to fail"
	cmp /tmp/crap.raw /tmp/crap2.raw
	dd if=/tmp/crap2.raw of=/tmp/crap2.raw.swab conv=swab 2>/dev/null
	@echo "expect binary comparison of raw pixel data after swab to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw.swab
	@echo "==========================================================================="
	dcsmpte /tmp/crap -bits 12 -nometa -output-vr explicit -output-endian big -addlengths -addlengthtoend -tiff
	@echo "explicit big to explicit little copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "1.2.840.10008.1.2.2" /tmp/crap "1.2.840.10008.1.2.1" /tmp/crap2 ${COPIERNOMETA}
	@echo "expect only differences to be group lengths and length to end and data set trailing padding"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to fail"
	cmp /tmp/crap.raw /tmp/crap2.raw
	dd if=/tmp/crap2.raw of=/tmp/crap2.raw.swab conv=swab 2>/dev/null
	@echo "expect binary comparison of raw pixel data after swab to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw.swab
	@echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
	@echo 16 bit pixel data
	dccp $${HOME}/work/nemamfmr/DISCIMG/IMAGES/DYNMAMMO /tmp/crap -output-vr explicit -output-endian little -addlengths -addlengthtoend -tiff
	@echo "==========================================================================="
	@echo "explicit with meta to implicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "" /tmp/crap "1.2.840.10008.1.2" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be metaheader, group lengths and length to end and data set trailing padding, and OX for output implicit PixelData from andump"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "==========================================================================="
	@echo "explicit with meta to explicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "" /tmp/crap "1.2.840.10008.1.2.1" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be metaheader, group lengths and length to end and data set trailing padding"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "==========================================================================="
	dccp $${HOME}/work/nemamfmr/DISCIMG/IMAGES/DYNMAMMO /tmp/crap -nometa -output-vr explicit -output-endian little -addlengths -addlengthtoend -tiff
	@echo "explicit to implicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "1.2.840.10008.1.2.1" /tmp/crap "1.2.840.10008.1.2" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be group lengths and length to end and data set trailing padding, and OX for output implicit PixelData from andump"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "==========================================================================="
	@echo "explicit to explicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "1.2.840.10008.1.2.1" /tmp/crap "1.2.840.10008.1.2.1" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be group lengths and length to end and data set trailing padding"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "==========================================================================="
	dccp $${HOME}/work/nemamfmr/DISCIMG/IMAGES/DYNMAMMO /tmp/crap -nometa -output-vr implicit -output-endian little -addlengths -addlengthtoend -tiff
	@echo "implicit to implicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "1.2.840.10008.1.2" /tmp/crap "1.2.840.10008.1.2" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be group lengths and length to end and data set trailing padding"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "==========================================================================="
	@echo "implicit to explicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "1.2.840.10008.1.2" /tmp/crap "1.2.840.10008.1.2.1" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be group lengths and length to end and data set trailing padding, and OX for input implicit PixelData from andump"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
	@echo 8 bit pixel data
	@echo "==========================================================================="
	dcsmpte /tmp/crap -output-vr explicit -output-endian little -addlengths -addlengthtoend -tiff
	@echo "explicit with meta to implicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "" /tmp/crap "1.2.840.10008.1.2" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be metaheader, group lengths and length to end and data set trailing padding, and OX for output implicit PixelData from andump"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "==========================================================================="
	@echo "explicit with meta to explicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "" /tmp/crap "1.2.840.10008.1.2.1" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be metaheader, group lengths and length to end and data set trailing padding"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "==========================================================================="
	dcsmpte /tmp/crap -nometa -output-vr explicit -output-endian little -addlengths -addlengthtoend -tiff
	@echo "explicit to implicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "1.2.840.10008.1.2.1" /tmp/crap "1.2.840.10008.1.2" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be group lengths and length to end and data set trailing padding, and OX for output implicit PixelData from andump"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "==========================================================================="
	@echo "explicit to explicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "1.2.840.10008.1.2.1" /tmp/crap "1.2.840.10008.1.2.1" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be group lengths and length to end and data set trailing padding"
	andiff /tmp/crap /tmp/crap2
	@echo "==========================================================================="
	dcsmpte /tmp/crap -nometa -output-vr implicit -output-endian little -addlengths -addlengthtoend -tiff
	@echo "implicit to implicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "1.2.840.10008.1.2" /tmp/crap "1.2.840.10008.1.2" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be group lengths and length to end and data set trailing padding"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "==========================================================================="
	@echo "implicit to explicit copy"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "1.2.840.10008.1.2" /tmp/crap "1.2.840.10008.1.2.1" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be group lengths and length to end and data set trailing padding, and OX for input implicit PixelData from andump"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw
	@echo "==========================================================================="
	@echo "explicit to implicit copy with fixed length sequences"
	cp $${HOME}/work/dctool.support/images/dicom/problems/fixedlengthsequencesevrle.dcm /tmp/crap
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  ${COPIERCLASS} "" /tmp/crap "1.2.840.10008.1.2" /tmp/crap2 ${COPIERNOMETA}
	dcfile /tmp/crap
	dcfile /tmp/crap2
	dcrmmeta /tmp/crap2 /tmp/crap3
	dcfile /tmp/crap3
	@echo "expect only differences to be group lengths and length to end and data set trailing padding, and sequence and item lengths made undefined and delimiters added"
	andiff /tmp/crap /tmp/crap2
	dctoraw -quiet /tmp/crap /tmp/crap.raw
	dctoraw -quiet /tmp/crap2 /tmp/crap2.raw
	@echo "expect binary comparison of raw pixel data to succeed"
	cmp /tmp/crap.raw /tmp/crap2.raw

testmeta:	FileMetaInformation.class
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.FileMetaInformation

testdictionary:	DicomDictionaryBase.class DicomDictionary.class
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.DicomDictionaryBase

testxml:	XMLRepresentationOfDicomObjectFactory.class \
				${PATHTOHOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/DICOMDIR
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.XMLRepresentationOfDicomObjectFactory ${PATHTOHOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/DICOMDIR | tidy -xml -indent -wrap 1000

testxmlback:	XMLRepresentationOfDicomObjectFactory.class /tmp/testxmlback.orig.dcm
	@echo "Creating xml"
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.XMLRepresentationOfDicomObjectFactory /tmp/testxmlback.orig.dcm >/tmp/testxmlback.xml
	@echo "Creating dicom"
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.XMLRepresentationOfDicomObjectFactory toDICOM /tmp/testxmlback.xml >/tmp/testxmlback.dcm
	dcdump /tmp/testxmlback.dcm
	dcdiff /tmp/testxmlback.orig.dcm /tmp/testxmlback.dcm
	rm -f /tmp/testxmlback.xml /tmp/testxmlback.dcm

testxmlbackdir:	${PATHTOHOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/DICOMDIR
	cp $< /tmp/testxmlback.orig.dcm
	make testxmlback
	rm -f /tmp/testxmlback.orig.dcm

testxmlbackimg:	${PATHTOHOME}/work/nemamfmr/DISCIMG/IMAGES/ONEBRAIN
	cp $< /tmp/testxmlback.orig.dcm
	make testxmlback
	rm -f /tmp/testxmlback.orig.dcm

testxmlbackimgwithat:	${PATHTOHOME}/work/dctool.support/images/dicom/philips/philipsnmbonescan.dcm
	#cp $< /tmp/testxmlback.orig.dcm
	dccp -nodisclaimer -removeprivate -d PixelData $< /tmp/testxmlback.orig.dcm
	make testxmlback
	rm -f /tmp/testxmlback.orig.dcm

testsrxml:	XMLRepresentationOfStructuredReportObjectFactory.class \
				${PATHTOHOME}/Pictures/Medical/R2CAD/Old/175255_4860_0000001449.sr
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.XMLRepresentationOfStructuredReportObjectFactory ${PATHTOHOME}/Pictures/Medical/R2CAD/Old/175255_4860_0000001449.sr | tidy -q -xml -indent -wrap 1000

testsrxmlroundtrip:	XMLRepresentationOfStructuredReportObjectFactory.class \
				${PATHTOHOME}/Pictures/Medical/R2CAD/Old/175255_4860_0000001449.sr
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.XMLRepresentationOfStructuredReportObjectFactory toXML ${PATHTOHOME}/Pictures/Medical/R2CAD/Old/175255_4860_0000001449.sr | tidy -q -xml -indent -wrap 1000 >/tmp/crap.xml
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.XMLRepresentationOfStructuredReportObjectFactory toDICOM /tmp/crap.xml >/tmp/crap.dcm
	@echo "Comparing original with round trip DICOM encoding"
	dcdiff ${PATHTOHOME}/Pictures/Medical/R2CAD/Old/175255_4860_0000001449.sr /tmp/crap.dcm
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.XMLRepresentationOfStructuredReportObjectFactory toXML /tmp/crap.dcm | tidy -q -xml -indent -wrap 1000 >/tmp/crap2.xml
	@echo "Comparing original xml with xml of round trip DICOM encoding"
	diff /tmp/crap.xml /tmp/crap2.xml
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.XMLRepresentationOfStructuredReportObjectFactory toDICOM /tmp/crap2.xml >/tmp/crap2.dcm
	@echo "Comparing second round trip DICOM encoding"
	dcdiff /tmp/crap.dcm /tmp/crap2.dcm

testsrdump:	StructuredReport.class \
				${PATHTOHOME}/Pictures/Medical/R2CAD/Old/175255_4860_0000001449.sr
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.StructuredReport ${PATHTOHOME}/Pictures/Medical/R2CAD/Old/175255_4860_0000001449.sr

testkowithtemplatexml:	XMLRepresentationOfStructuredReportObjectFactory.class \
				${PATHTOHOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000004/KO000000
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.XMLRepresentationOfStructuredReportObjectFactory ${PATHTOHOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000004/KO000000 | tidy -q -xml -indent -wrap 1000

testkowithtemplatedump:	StructuredReport.class \
				${PATHTOHOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000004/KO000000
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.StructuredReport ${PATHTOHOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000004/KO000000

testsrwithrefxml:	XMLRepresentationOfStructuredReportObjectFactory.class \
				${PATHTOHOME}/Documents/Medical/stuff/ftp.ihe.net/Mammography/SBI2007Demo/DICOMImages/Corrected/IMAGES/SBIPT000/20061030/SR1/12946
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.XMLRepresentationOfStructuredReportObjectFactory ${PATHTOHOME}/Documents/Medical/stuff/ftp.ihe.net/Mammography/SBI2007Demo/DICOMImages/Corrected/IMAGES/SBIPT000/20061030/SR1/12946 | tidy -q -xml -indent -wrap 1000

testsrwithrefdump:	StructuredReport.class \
				${PATHTOHOME}/Documents/Medical/stuff/ftp.ihe.net/Mammography/SBI2007Demo/DICOMImages/Corrected/IMAGES/SBIPT000/20061030/SR1/12946
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.dicom.StructuredReport ${PATHTOHOME}/Documents/Medical/stuff/ftp.ihe.net/Mammography/SBI2007Demo/DICOMImages/Corrected/IMAGES/SBIPT000/20061030/SR1/12946



testpatientnames:	SpecificCharacterSet.class
	mkdir -p $${HOME}/Pictures/medical/charsettests
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.SpecificCharacterSet $${HOME}/Pictures/medical/charsettests
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID H31EXAMPLE -r StudyID H31EXAMPLE
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/h31example.dcm $${HOME}/Pictures/medical/charsettests/SCSH31 -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/h31example.dcm
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID H32EXAMPLE -r StudyID H32EXAMPLE
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/h32example.dcm $${HOME}/Pictures/medical/charsettests/SCSH32 -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/h32example.dcm
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID I2EXAMPLE -r StudyID I2EXAMPLE
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/i2example.dcm $${HOME}/Pictures/medical/charsettests/SCSI2 -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/i2example.dcm
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID X1EXAMPLE -r StudyID X1EXAMPLE
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/x1example.dcm $${HOME}/Pictures/medical/charsettests/SCSX1 -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/x1example.dcm
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID X2EXAMPLE -r StudyID X2EXAMPLE
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/x2example.dcm $${HOME}/Pictures/medical/charsettests/SCSX2 -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/x2example.dcm
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID SCSGREEK -r StudyID SCSGREEK
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/greek.dcm $${HOME}/Pictures/medical/charsettests/SCSGREEK -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/greek.dcm
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID SCSFREN -r StudyID SCSFREN
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/french.dcm $${HOME}/Pictures/medical/charsettests/SCSFREN -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/french.dcm
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID SCSGERM -r StudyID SCSGERM
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/german.dcm $${HOME}/Pictures/medical/charsettests/SCSGERM -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/german.dcm
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID SCSARAB -r StudyID SCSARAB
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/arabic.dcm $${HOME}/Pictures/medical/charsettests/SCSARAB -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/arabic.dcm
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID SCSRUSS -r StudyID SCSRUSS
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/russian.dcm $${HOME}/Pictures/medical/charsettests/SCSRUSS -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/russian.dcm
	dcsmpte $${HOME}/Pictures/medical/charsettests/crap -d PatientName -d SpecificCharacterSet -nodisclaimer -r SeriesNumber 1 -r InstanceNumber 1 -r PatientID SCSHBRW -r StudyID SCSHBRW
	dcmerge $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/hebrew.dcm $${HOME}/Pictures/medical/charsettests/SCSHBRW -nodisclaimer
	rm $${HOME}/Pictures/medical/charsettests/crap $${HOME}/Pictures/medical/charsettests/hebrew.dcm
	(cd $${HOME}/Pictures/medical/charsettests; $${HOME}/Work/dicom3tools/appsrc/dcfile/dcdirmk -noicons -of DICOMDIR SCSH31 SCSH32 SCSI2 SCSX1 SCSX2 SCSGREEK SCSFREN SCSGERM SCSARAB SCSRUSS SCSHBRW)
	(cd $${HOME}/Pictures/medical/; tar -cvf - charsettests/DICOMDIR charsettests/SCS* | bzip2 >charsettests.`date +%Y%m%d`.tar.bz2)

testclinicaltrials:	ClinicalTrialsAttributes.class
	#dcsmpte /tmp/crap
	#cp $${HOME}/Work/nemamfmr/DISCIMG/IMAGES/DYNMAMMO /tmp/crap
	dcunjpeg $${HOME}/Work/dctool.support/images/dicom/lightspeed.qxi/10 /tmp/crap
	#cp $${HOME}/Work/nemamfmr/srcimages/GEYokogawaCD/ST_1050/SE_311/IM000165 /tmp/crap
	#cp $${HOME}/Work/nemamfmr/srcimages/PhilipsDemoCDs/EV51/CD3/DICOM/IM_02330 /tmp/crap
	dciodvfy /tmp/crap
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.dicom.ClinicalTrialsAttributes /tmp/crap /tmp/crap2
	dcdiff /tmp/crap /tmp/crap2
	dciodvfy /tmp/crap2
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.dicom.ClinicalTrialsAttributes /tmp/crap2 /tmp/crap3
	dcdiff /tmp/crap2 /tmp/crap3
	dciodvfy /tmp/crap3

testremoveunsafeprivate:	AttributeList.class ClinicalTrialsAttributes.class
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.dicom.ClinicalTrialsAttributes $${HOME}/work/dctool.support/images/dicom/philips/philipspetwithprivatesuv.dcm /tmp/crap
	dcdiff $${HOME}/work/dctool.support/images/dicom/philips/philipspetwithprivatesuv.dcm /tmp/crap
	rm -f /tmp/crap

testimagetodicom:	ImageToDicom.class
	java -Djava.awt.headless=true -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.dicom.ImageToDicom \
		${PATHTOROOT}/../../experiments/davwork.jpg \
		/tmp/crap \
		"Smith^Joan" "P001" "1234" "001" "027"
	dciodvfy /tmp/crap
	#rm -f /tmp/crap

testimagetodicom_mfrgb:	ImageToDicom.class
	ppmpat -quiet -squig 512 512 | pnmtotiff -quiet > /tmp/crap1.tif
	ppmpat -quiet -poles 512 512 | pnmtotiff -quiet > /tmp/crap2.tif
	ppmpat -quiet -squig 512 512 | pnmtotiff -quiet > /tmp/crap3.tif
	tiffcp /tmp/crap1.tif /tmp/crap2.tif /tmp/crap3.tif /tmp/crapmulti.tif
	tiffinfo /tmp/crapmulti.tif
	java -Djava.awt.headless=true -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.dicom.ImageToDicom \
		/tmp/crapmulti.tif /tmp/crapmulti.dcm \
		"Smith^Joan" "P001" "1234" "001" "027"
	rm -f /tmp/crap1.tif /tmp/crap2.tif /tmp/crap3.tif /tmp/crapmulti.tif
	dciodvfy /tmp/crapmulti.dcm
	#rm -f /tmp/crapmulti.dcm


testimagetodicom_mfgray:	ImageToDicom.class
	ppmpat -quiet -camo 512 512 | pnmtotiff -quiet > /tmp/crap1.tif
	ppmpat -quiet -tartan 512 512 | pnmtotiff -quiet > /tmp/crap2.tif
	ppmpat -quiet -anticamo 512 512 | pnmtotiff -quiet > /tmp/crap3.tif
	tiffcp /tmp/crap1.tif /tmp/crap2.tif /tmp/crap3.tif /tmp/crapmulti.tif
	tiffinfo /tmp/crapmulti.tif
	java -Djava.awt.headless=true -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.dicom.ImageToDicom \
		/tmp/crapmulti.tif /tmp/crapmulti.dcm \
		"Smith^Joan" "P001" "1234" "001" "027"
	rm -f /tmp/crap1.tif /tmp/crap2.tif /tmp/crap3.tif /tmp/crapmulti.tif
	dciodvfy /tmp/crapmulti.dcm
	#rm -f /tmp/crapmulti.dcm

testimagetodicomicc:	ImageToDicom.class
	java -Djava.awt.headless=true -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.dicom.ImageToDicom \
		"${HOME}/Documents/Medical/stuff/Color/ICC/TestImages/Is your system ICC Version 4 ready?_files/Upper_Right.jpg" \
		/tmp/crap \
		"Smith^Joan" "P001" "1234" "001" "027"
	dciodvfy /tmp/crap
	#rm -f /tmp/crap

/tmp/crap1:
	dcsmpte /tmp/crap1

/tmp/crap2:
	dcsmpte /tmp/crap2

testdicomfile:	SetOfDicomFiles.class  /tmp/crap1 /tmp/crap2
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.SetOfDicomFiles /tmp/crap1 /tmp/crap2

testpdftodicom:	PDFToDicomImage.class
	# may also need :${PATHTOROOT}/jdom.jar:${PATHTOROOT}/bcprov-jdk14-119.jar:${PATHTOROOT}/pjes.jar and JAI IIO for embedded JPEG 2000 images
	java -Djava.awt.headless=true -Xmx512m -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar:${PATHTOADDITIONAL}/jpedalSTD.jar com.pixelmed.dicom.PDFToDicomImage ${PATHTOROOT}/../../experiments/test.pdf /tmp/crap \
		"Smith^Joan" "P001" "1234" "001" "027" 144
	dciodvfy /tmp/crap
	#rm -f /tmp/crap

testuidgen:	UIDGenerator.class
	java -cp ${PATHTOROOT} com.pixelmed.dicom.UIDGenerator 1000

testtransynfromname:	TransferSyntaxFromName.class
	java -cp ${PATHTOROOT} com.pixelmed.dicom.TransferSyntaxFromName

testmediaimporter:	MediaImporter.class
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.MediaImporter

testphoneticpersonname:	PersonNameAttribute.class
	dcsmpte /tmp/crap -r PatientName "Jane Smith"
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.PersonNameAttribute /tmp/crap
	rm /tmp/crap

testmakedicomdir:	DicomDirectory.class
	mkdir -p tmp
	rm -f tmp/*
	dcsmpte tmp/crap1a -r PatientID 1 -r PatientName One -r StudyID 11 -stamp 9999
	dcsmpte tmp/crap1b -r PatientID 1 -r PatientName One -r StudyID 11 -stamp 9999
	dcsmpte tmp/crap1c -r PatientID 1 -r PatientName One -r StudyID 12 -stamp 9999
	dcsmpte tmp/crap2a -r PatientID 2 -r PatientName Two -r StudyID 21 -stamp 9999
	dcsmpte tmp/crap2b -r PatientID 2 -r PatientName Two -r StudyID 22 -stamp 9999
	dcsmpte tmp/crap2c -r PatientID 2 -r PatientName Two -r StudyID 23 -stamp 9999
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.DicomDirectory \
		tmp.DICOMDIR \
		tmp/crap1a tmp/crap1b tmp/crap2a tmp/crap1c tmp/crap2b tmp/crap2c
	dcdirdmp tmp.DICOMDIR
	dciodvfy tmp.DICOMDIR
	andump -showoffset-hex tmp.DICOMDIR
	rm -f tmp/crap1a tmp/crap1b tmp/crap2a tmp/crap1c tmp/crap2b tmp/crap2c
	rm -f tmp.DICOMDIR
	rm -rf tmp

testmakedicomdirwithdir:	DicomDirectory.class
	mkdir -p tmp
	rm -f tmp/*
	dcsmpte tmp/crap1a -r PatientID 1 -r PatientName One -r StudyID 11 -stamp 9999
	dcsmpte tmp/crap1b -r PatientID 1 -r PatientName One -r StudyID 11 -stamp 9999
	dcsmpte tmp/crap1c -r PatientID 1 -r PatientName One -r StudyID 12 -stamp 9999
	dcsmpte tmp/crap2a -r PatientID 2 -r PatientName Two -r StudyID 21 -stamp 9999
	dcsmpte tmp/crap2b -r PatientID 2 -r PatientName Two -r StudyID 22 -stamp 9999
	dcsmpte tmp/crap2c -r PatientID 2 -r PatientName Two -r StudyID 23 -stamp 9999
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.DicomDirectory \
		tmp \
		DICOMDIR \
		crap1a crap1b crap2a crap1c crap2b crap2c
	dcdirdmp tmp/DICOMDIR
	dciodvfy tmp/DICOMDIR
	andump -showoffset-hex tmp/DICOMDIR
	rm -f tmp/crap1a tmp/crap1b tmp/crap2a tmp/crap1c tmp/crap2b tmp/crap2c
	rm -f tmp/DICOMDIR
	rm -rf tmp

testmakedicomdirscenarios:	DicomDirectory.class
	mkdir -p tmp
	(cd tmp; rm -f IMAGES; ln -s $${HOME}/Documents/Medical/stuff/ftp.ihe.net/Mammography/SBI2007Demo/DICOMImages/Scenarios/IMAGES IMAGES)
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.DicomDirectory \
		tmp.DICOMDIR \
		`find tmp/IMAGES -follow -type f -a -name '[0-9]*'`
	dcdirdmp tmp.DICOMDIR
	dciodvfy tmp.DICOMDIR
	andump -showoffset-hex tmp.DICOMDIR
	rm -f tmp.DICOMDIR
	rm -rf tmp

testmakedicomdirko:	DicomDirectory.class
	mkdir -p tmp
	(cd tmp; rm -f IMAGES; ln -s $${HOME}/Pictures/Medical/KOFromPDI2004 KOFromPDI2004)
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.DicomDirectory \
		tmp.DICOMDIR \
		`find tmp/KOFromPDI2004 -follow -type f -a -name '[0-9A-Z]*'`
	dcdirdmp tmp.DICOMDIR
	dciodvfy tmp.DICOMDIR
	andump -showoffset-hex tmp.DICOMDIR
	rm -f tmp.DICOMDIR
	rm -rf tmp

testisdicom:	DicomFileUtilities.class
	dcsmpte /tmp/crapevrlemeta -output-vr implicit -output-endian little
	dcsmpte /tmp/crapivrlenometa -output-vr implicit -output-endian little -nometa
	dcsmpte /tmp/crapevrlenometa -output-vr explicit -output-endian little -nometa
	dcsmpte /tmp/crapevrbenometa -output-vr explicit -output-endian big -nometa
	#hexdump -C /tmp/crapivrlenometa | head -1
	echo "Hello" >/tmp/crapnotdicom
	java -cp ${PATHTOROOT} com.pixelmed.dicom.DicomFileUtilities \
		/tmp/crapevrlemeta /tmp/crapivrlenometa /tmp/crapevrlenometa /tmp/crapevrbenometa /tmp/crapnotdicom
	rm -f /tmp/crapevrlemeta /tmp/crapivrlenometa /tmp/crapevrlenometa /tmp/crapevrbenometa /tmp/crapnotdicom

testoverlay:	Overlay.class
	java -cp ${PATHTOROOT} com.pixelmed.dicom.Overlay $${HOME}/work/dctool.support/images/dicom/overlays/nonzerohighbitstopogram.dcm
	java -cp ${PATHTOROOT} com.pixelmed.dicom.Overlay $${HOME}/work/dctool.support/images/dicom/overlays/spectrumlocalizerinoverlaydata.dcm
	java -cp ${PATHTOROOT} com.pixelmed.dicom.Overlay $${HOME}/work/dctool.support/images/dicom/overlays/oneoverlayinpixeldata_philips.dcm
	java -cp ${PATHTOROOT} com.pixelmed.dicom.Overlay $${HOME}/work/dctool.support/images/dicom/overlays/oneoverlaylocalizerinoverlaydata_siemens.dcm
	java -cp ${PATHTOROOT} com.pixelmed.dicom.Overlay $${HOME}/work/dctool.support/images/dicom/overlays/oneoverlayinpixeldata_siemens.dcm

testintegerstring:	IntegerStringAttribute.class
	java -cp ${PATHTOROOT} com.pixelmed.dicom.IntegerStringAttribute

testcolorpalette_hotiron:	ColorPaletteHotIron.class ColorPalette.class
	mkdir -p "$${HOME}/work/palette/PalettesDICOMPixelMed"
	rm -f "$${HOME}/work/palette/PalettesDICOMPixelMed/hotiron.dcm"
	java -cp ${PATHTOROOT} com.pixelmed.dicom.ColorPaletteHotIron "$${HOME}/work/palette/PalettesDICOMPixelMed/hotiron.dcm" sRGBColorSpaceProfileInputDevice.icc
	dciodvfy "$${HOME}/work/palette/PalettesDICOMPixelMed/hotiron.dcm"

testcolorpalette_hotmetalblue:	ColorPaletteHotMetalBlue.class ColorPalette.class
	mkdir -p "$${HOME}/work/palette/PalettesDICOMPixelMed"
	rm -f "$${HOME}/work/palette/PalettesDICOMPixelMed/hotmetalblue.dcm"
	java -cp ${PATHTOROOT} com.pixelmed.dicom.ColorPaletteHotMetalBlue "$${HOME}/work/palette/PalettesDICOMPixelMed/hotmetalblue.dcm" sRGBColorSpaceProfileInputDevice.icc
	dciodvfy "$${HOME}/work/palette/PalettesDICOMPixelMed/hotmetalblue.dcm"

testcolorpalette_pet:	ColorPalettePET.class ColorPalette.class
	mkdir -p "$${HOME}/work/palette/PalettesDICOMPixelMed"
	rm -f "$${HOME}/work/palette/PalettesDICOMPixelMed/pet.dcm"
	java -cp ${PATHTOROOT} com.pixelmed.dicom.ColorPalettePET "$${HOME}/work/palette/PalettesDICOMPixelMed/pet.dcm" sRGBColorSpaceProfileInputDevice.icc
	dciodvfy "$${HOME}/work/palette/PalettesDICOMPixelMed/pet.dcm"

testcolorpalette_pet20step:	ColorPalettePET20Step.class ColorPalette.class
	mkdir -p "$${HOME}/work/palette/PalettesDICOMPixelMed"
	rm -f "$${HOME}/work/palette/PalettesDICOMPixelMed/pet20step.dcm"
	java -cp ${PATHTOROOT} com.pixelmed.dicom.ColorPalettePET20Step "$${HOME}/work/palette/PalettesDICOMPixelMed/pet20step.dcm" sRGBColorSpaceProfileInputDevice.icc
	dciodvfy "$${HOME}/work/palette/PalettesDICOMPixelMed/pet20step.dcm"

testcolorpalettes:	testcolorpalette_hotiron testcolorpalette_hotmetalblue testcolorpalette_pet testcolorpalette_pet20step

testframeset:	FrameSet.class SetOfFrameSets.class
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.SetOfFrameSets \
		"$${HOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/Recontexted/107166 [RIDER 2151469008]/20070208 000000/Series 103 [CT]"
		#"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0526090412/DICOM/ST00001"
		#"$${HOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000000" "$${HOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000001" "$${HOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000002" "$${HOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000003"

testtoenhancedmr:	MultiFrameImageFactory.class FrameSet.class SetOfFrameSets.class AttributeList.class
	mkdir -p /tmp/testtoenhanced
	rm -rf /tmp/testtoenhanced/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.MultiFrameImageFactory \
		"$${HOME}/Pictures/Medical/MRDualEcho/BrainSpinEcho/deid/DICOM" /tmp/testtoenhanced
		#"$${HOME}/Pictures/Medical/QIICRDICOMSampleData/BWH-QIN 2/9-3D_DCE" /tmp/testtoenhanced
		#"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/PCAMPMRI-0241-DCE" /tmp/testtoenhanced
		#"$${HOME}/Pictures/Medical/japanese/JP_CharSetSample/MRI_JPsample" /tmp/testtoenhanced

testtoenhancedctpet:	MultiFrameImageFactory.class FrameSet.class SetOfFrameSets.class AttributeList.class
	mkdir -p /tmp/testtoenhanced
	rm -rf /tmp/testtoenhanced/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.MultiFrameImageFactory \
		"$${HOME}/Pictures/Medical/PETCTPhilips/PET-CT"  /tmp/testtoenhanced

testtoenhancedct:	MultiFrameImageFactory.class FrameSet.class SetOfFrameSets.class AttributeList.class
	mkdir -p /tmp/testtoenhanced
	rm -rf /tmp/testtoenhanced/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.MultiFrameImageFactory \
		"$${HOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/Recontexted/277654 [RIDER 2357766186]" /tmp/testtoenhanced
		#"$${HOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/Recontexted/107166 [RIDER 2151469008]/20070208 000000/Series 103 [CT]" /tmp/testtoenhanced
		#"$${HOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000000" "$${HOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000001" "$${HOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000002" "$${HOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000003" /tmp/testtoenhanced
		#"/Volumes/CluniePortable1TB1_Enc800/Hold/VeryThin03RI/20080259/Original/25917613004 [25917613004]/20110609 123538/Series 003 [CT_Body 0_5]" /tmp/testtoenhanced
		#"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0526090412/DICOM/ST00001" /tmp/testtoenhanced

testpetforseg:	MultiFrameImageFactory.class FrameSet.class SetOfFrameSets.class AttributeList.class
	mkdir -p /tmp/petforseg
	rm -rf /tmp/petforseg/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/commons-codec-1.3.jar com.pixelmed.dicom.MultiFrameImageFactory \
		"$${HOME}/Pictures/Medical/QIICRDICOMSampleData/Iowa-QIN/PET"  /tmp/petforseg

testimagelibrary.create:	ImageLibrary.class
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary
	rm -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/imagelibrary.compare.dcm
	java -cp .:${PATHTOROOT}:${PATHTOADDITIONAL}/vecmath1.2-1.14.jar com.pixelmed.dicom.ImageLibrary \
		${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/imagelibrary.compare.dcm \
		/Volumes/CluniePortable1TB1/Pictures/Medical/TCIA/LIDCIDRI/LIDC-IDRI/LIDC-IDRI-0068/1.3.6.1.4.1.14519.5.2.1.6279.6001.709632090821449989953075380168
	dcsrdump ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/imagelibrary.compare.dcm

testimagelibrary:	ImageLibrary.class
	rm -f /tmp/imagelibrary.dcm
	java -cp .:${PATHTOROOT}:${PATHTOADDITIONAL}/vecmath1.2-1.14.jar com.pixelmed.dicom.ImageLibrary \
		/tmp/imagelibrary.dcm \
		/Volumes/CluniePortable1TB1/Pictures/Medical/TCIA/LIDCIDRI/LIDC-IDRI/LIDC-IDRI-0068/1.3.6.1.4.1.14519.5.2.1.6279.6001.709632090821449989953075380168
	dcsrdump /tmp/imagelibrary.dcm
	echo "Comparing ..."
	dcsrdiff ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/imagelibrary.compare.dcm /tmp/imagelibrary.dcm
	dcdiff ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/imagelibrary.compare.dcm /tmp/imagelibrary.dcm

testcrosssectionalimagelibrary.create:	CrossSectionalImageLibrary.class ImageLibrary.class
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary
	rm -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/crosssectionalimagelibrary.compare.dcm
	java -cp .:${PATHTOROOT}:${PATHTOADDITIONAL}/vecmath1.2-1.14.jar com.pixelmed.dicom.CrossSectionalImageLibrary \
		${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/crosssectionalimagelibrary.compare.dcm \
		/Volumes/CluniePortable1TB1/Pictures/Medical/TCIA/LIDCIDRI/LIDC-IDRI/LIDC-IDRI-0068/1.3.6.1.4.1.14519.5.2.1.6279.6001.709632090821449989953075380168
	dcsrdump ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/crosssectionalimagelibrary.compare.dcm

testcrosssectionalimagelibrary:	CrossSectionalImageLibrary.class ImageLibrary.class
	rm -f /tmp/crosssectionalimagelibrary.dcm
	java -cp .:${PATHTOROOT}:${PATHTOADDITIONAL}/vecmath1.2-1.14.jar com.pixelmed.dicom.CrossSectionalImageLibrary \
		/tmp/crosssectionalimagelibrary.dcm \
		/Volumes/CluniePortable1TB1/Pictures/Medical/TCIA/LIDCIDRI/LIDC-IDRI/LIDC-IDRI-0068/1.3.6.1.4.1.14519.5.2.1.6279.6001.709632090821449989953075380168
	dcsrdump /tmp/crosssectionalimagelibrary.dcm
	echo "Comparing ..."
	dcsrdiff ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/crosssectionalimagelibrary.compare.dcm /tmp/crosssectionalimagelibrary.dcm
	dcdiff ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/crosssectionalimagelibrary.compare.dcm /tmp/crosssectionalimagelibrary.dcm

testprojectionxrayimagelibrary.create:	ProjectionXRayImageLibrary.class ImageLibrary.class
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary
	rm -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/projectionxrayimagelibrary.compare.dcm
	java -cp .:${PATHTOROOT}:${PATHTOADDITIONAL}/vecmath1.2-1.14.jar com.pixelmed.dicom.ProjectionXRayImageLibrary \
		${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/projectionxrayimagelibrary.compare.dcm \
		"/Volumes/CluniePortable1TB1/Pictures/Medical/INbreast/INbreast_Fixed/20586908_6c613a14b80a8591_MG_R_CC_ANON_FIXED.dcm"
	dcsrdump ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/projectionxrayimagelibrary.compare.dcm

testprojectionxrayimagelibrary:	ProjectionXRayImageLibrary.class ImageLibrary.class
	rm -f /tmp/projectionxrayimagelibrary.dcm
	java -cp .:${PATHTOROOT}:${PATHTOADDITIONAL}/vecmath1.2-1.14.jar com.pixelmed.dicom.ProjectionXRayImageLibrary \
		/tmp/projectionxrayimagelibrary.dcm \
		"/Volumes/CluniePortable1TB1/Pictures/Medical/INbreast/INbreast_Fixed/20586908_6c613a14b80a8591_MG_R_CC_ANON_FIXED.dcm"
	dcsrdump /tmp/projectionxrayimagelibrary.dcm
	echo "Comparing ..."
	dcsrdiff ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/projectionxrayimagelibrary.compare.dcm /tmp/projectionxrayimagelibrary.dcm
	dcdiff ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testimagelibrary/projectionxrayimagelibrary.compare.dcm /tmp/projectionxrayimagelibrary.dcm

testreadmfjpeg:
	rm -f /tmp/crap
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList \
		"$${HOME}/Work/dctool.support/images/dicom/jpeg/jpegmfecho.dcm" \
		/tmp/crap

testreadencapnometaheaderguessjpeg:
	rm -f /tmp/crap
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.dicom.AttributeList \
		"$${HOME}/Work/dctool.support/images/dicom/jpeg/nometa.jpeg.50.dcm" \
		/tmp/crap
