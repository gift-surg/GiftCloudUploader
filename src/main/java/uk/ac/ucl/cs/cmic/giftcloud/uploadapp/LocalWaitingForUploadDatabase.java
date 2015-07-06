package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.database.DeleteFromDatabase;
import com.pixelmed.database.PatientStudySeriesConcatenationInstanceModel;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Observable;

public class LocalWaitingForUploadDatabase extends Observable {

    private UploaderStatusModel uploaderStatusModel;
    private GiftCloudReporter reporter;
    private DatabaseInformationModel srcDatabase;


    public LocalWaitingForUploadDatabase(final String databaseRootTitle, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporter reporter) throws DicomException {
        this.uploaderStatusModel = uploaderStatusModel;
        this.reporter = reporter;

        // Start database for the "source" instances.
        srcDatabase = new PatientStudySeriesConcatenationInstanceModel("mem:src", null, databaseRootTitle);
    }

    public DatabaseInformationModel getSrcDatabase() {
        return srcDatabase;
    }

    public void deleteFileFromDatabase(final File file) {
        final String filename = file.getPath();
        try {
            DeleteFromDatabase.deleteRecordChildrenAndFilesByFilename(srcDatabase, filename);
        } catch (DicomException e) {
            reporter.silentLogException(e, "Could not delete the file or database entry due to the following error:" + e.getLocalizedMessage());
        }

        // Send a notification that the database has changed
        setChanged();
        notifyObservers(filename);
    }

    public void importFileIntoDatabase(String dicomFileName,String fileReferenceType) throws IOException, DicomException {
        uploaderStatusModel.setImportingStatusMessage("Added file " + dicomFileName + " to the list of files for upload");
        FileInputStream fis = new FileInputStream(dicomFileName);
        DicomInputStream i = new DicomInputStream(new BufferedInputStream(fis));
        AttributeList list = new AttributeList();
        list.read(i, TagFromName.PixelData);
        i.close();
        fis.close();
        srcDatabase.insertObject(list, dicomFileName, fileReferenceType);

        // Send a notification that the database has changed
        setChanged();
        notifyObservers(dicomFileName);
    }

}
