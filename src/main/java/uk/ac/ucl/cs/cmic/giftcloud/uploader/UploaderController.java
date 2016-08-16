package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.DicomException;
import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.DicomListener;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudPropertiesFromApplication;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.MenuController;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.ProjectListModel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.workers.ImportWorker;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UploaderController {
    private final GiftCloudUploader giftCloudUploader;
    private final DicomListener dicomListener;
    private final GiftCloudPropertiesFromApplication giftCloudProperties;
    private final GiftCloudReporter reporter;
    private final UploaderStatusModel uploaderStatusModel = new UploaderStatusModel();

    public UploaderController(final RestServerFactory restServerFactory, final GiftCloudPropertiesFromApplication giftCloudProperties, final UserCallback userCallback, final GiftCloudReporter reporter) throws DicomException {
        this.giftCloudProperties = giftCloudProperties;
        this.reporter = reporter;
        giftCloudUploader = new GiftCloudUploader(restServerFactory, giftCloudProperties, uploaderStatusModel, userCallback, reporter);
        dicomListener = new DicomListener(giftCloudUploader, giftCloudProperties, uploaderStatusModel, reporter);
    }

    public void startUploading() {
        giftCloudUploader.setUploadServiceRunningState(true);
    }

    public void pauseUploading() {
        giftCloudUploader.setUploadServiceRunningState(false);
    }

    public void runImport(List<File> fileList, final boolean importAsReference, final Progress progress) {
        new Thread(new ImportWorker(fileList, progress, giftCloudProperties.acceptAnyTransferSyntax(), giftCloudUploader, importAsReference, uploaderStatusModel, reporter)).start();
    }

    public void invalidateServerAndRestartUploader() {
        pauseUploading();
        giftCloudUploader.invalidateServer();
        startUploading();
    }

    public void exportPatientList() {
        giftCloudUploader.exportPatientList();
    }

    public PixelDataAnonymiserFilterCache getPixelDataAnonymiserFilterCache() {
        return giftCloudUploader.getPixelDataAnonymiserFilterCache();
    }

    public void addBackgroundAddToUploaderServiceListener(final MenuController.BackgroundAddToUploaderServiceListener backgroundAddToUploaderServiceListener) {
        giftCloudUploader.getBackgroundAddToUploaderService().addListener(backgroundAddToUploaderServiceListener);
    }

    public void startDicomListener() throws IOException {
        dicomListener.activateStorageSCP();
    }

    public void stopDicomListener() {
        dicomListener.shutdownStorageSCPAndWait(giftCloudProperties.getShutdownTimeoutMs());
    }

    public UploaderStatusModel getUploaderStatusModel() {
        return uploaderStatusModel;
    }

    public ProjectListModel getProjectListModel() {
        return giftCloudUploader.getProjectListModel();
    }

    public TableModel getTableModel() {
        return giftCloudUploader.getTableModel();
    }

    public void importPendingFiles() {
        final File pendingUploadFolder = giftCloudProperties.getUploadFolder(reporter);
        runImport(Arrays.asList(pendingUploadFolder), false, reporter);
    }
}
