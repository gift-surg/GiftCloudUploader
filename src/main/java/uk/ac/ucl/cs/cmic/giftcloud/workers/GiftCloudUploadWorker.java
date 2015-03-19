package uk.ac.ucl.cs.cmic.giftcloud.workers;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudHttpException;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudBridge;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporter;

import java.util.Vector;

public class GiftCloudUploadWorker implements Runnable {
    Vector<String> sourceFilePathSelections;
    private GiftCloudReporter reporter;
    private GiftCloudBridge giftCloudBridge;

    public GiftCloudUploadWorker(Vector<String> sourceFilePathSelections, final GiftCloudBridge giftCloudBridge, final GiftCloudReporter reporter) {
        this.sourceFilePathSelections = sourceFilePathSelections;
        this.reporter = reporter;
        this.giftCloudBridge = giftCloudBridge;
    }

    public void run() {
        if (giftCloudBridge == null) {
            reporter.showError("An error occurred which prevents the uploader from connecting to the server. Please restart GIFT-Cloud uploader.");
            return;
        }

        reporter.setWaitCursor();

        if (sourceFilePathSelections == null) {
            reporter.updateProgress("No files selected for upload.");
            reporter.showError("No files were selected for uploading.");
        } else {
            reporter.sendLn("GIFT-Cloud upload started");
            reporter.startProgressBar();
            try {
                if (giftCloudBridge.uploadToGiftCloud(sourceFilePathSelections)) {
                    reporter.updateProgress("GIFT-Cloud upload complete");
                } else {
                    reporter.updateProgress("GIFT-Cloud upload failed");
                }
            } catch (GiftCloudHttpException e) {
                reporter.updateProgress("GIFT-Cloud upload failed with the following error: " + e.getHtmlText());
                e.printStackTrace(System.err);
            } catch (Exception e) {
                reporter.updateProgress("GIFT-Cloud upload failed with the following error: " + e.toString());
                e.printStackTrace(System.err);
            }
            reporter.endProgress();
        }
        reporter.restoreCursor();
    }
}
