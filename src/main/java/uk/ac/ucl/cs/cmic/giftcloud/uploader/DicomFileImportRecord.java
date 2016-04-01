package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.AttributeList;

import java.util.Vector;

/**
 *
 */
public class DicomFileImportRecord extends FileImportRecord {

    private AttributeList attributeList;

    public DicomFileImportRecord(Vector<String> fileNames, final PendingUploadTask.DeleteAfterUpload deleteAfterUpload, final AttributeList attributeList) {
        super(fileNames, deleteAfterUpload);
        this.attributeList = attributeList;
    }

    public DicomFileImportRecord(String dicomFileName, final PendingUploadTask.DeleteAfterUpload deleteAfterUpload, final AttributeList attributeList) {
        this(new Vector<String>(), deleteAfterUpload, attributeList);
        fileNames.add(dicomFileName);
    }
}
