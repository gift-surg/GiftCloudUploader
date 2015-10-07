package uk.ac.ucl.cs.cmic.giftcloud.workers;

import uk.ac.ucl.cs.cmic.giftcloud.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.MasterFileImporter;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;

public class ImportWorker implements Runnable {
    private final UploaderStatusModel uploaderStatusModel;
    private final GiftCloudReporterFromApplication reporter;
    private final String pathName;
    private final Progress progress;
    private final MasterFileImporter masterFileImporter;

    public ImportWorker(String pathName, final Progress progress, final boolean acceptAnyTransferSyntax, final GiftCloudUploader giftCloudUploader, final boolean importAsReference, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporterFromApplication reporter) {
        this.uploaderStatusModel = uploaderStatusModel;
        this.reporter = reporter;
        this.progress = progress;
        masterFileImporter = new MasterFileImporter(acceptAnyTransferSyntax, giftCloudUploader, importAsReference, reporter);
        this.pathName=pathName;
    }

    public void run() {
        uploaderStatusModel.setImportingStatusMessage("Importing files...");
        reporter.startProgressBar();
        boolean anyFiles = false;

        try {
            anyFiles = masterFileImporter.importFiles(pathName, progress);
        } catch (Exception e) {
            uploaderStatusModel.setImportingStatusMessage("Failure when importing files" , e);
            reporter.silentLogException(e, "Failure when importing files");
        }

        reporter.endProgressBar();
        final String statusMessage = anyFiles ? "Files have been imported and are ready for upload" : "Ready";
        uploaderStatusModel.setImportingStatusMessage(statusMessage);
        // importer sends its own completion message to log, so do not need another one
    }
}
