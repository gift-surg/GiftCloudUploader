/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.workers;

import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudUploaderConfiguration;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderController;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

/**
 * A worker for starting the Dicom listener and uploading services in a non-interactive (daemon) context
 */
public class DaemonStartupWorker implements Runnable {
    private GiftCloudUploaderConfiguration appConfiguration;
    private UploaderController uploaderController;
    private final GiftCloudReporter reporter;

    /**
     * @param appConfiguration
     * @param uploaderController
     * @param reporter
     */
    public DaemonStartupWorker(final GiftCloudUploaderConfiguration appConfiguration, final UploaderController uploaderController, final GiftCloudReporter reporter) {
        this.appConfiguration = appConfiguration;
        this.uploaderController = uploaderController;
        this.reporter = reporter;
    }

    public void run() {
        Optional<Throwable> dicomNodeFailureException = Optional.empty();
        try {
            uploaderController.startDicomListener();
        } catch (Throwable e) {
            reporter.silentLogException(e, appConfiguration.getResourceBundle().getString("dicomNodeFailureMessageWithDetails") + e.getLocalizedMessage());
        }

        // Add any leftover files from the last session to the upload queue
        try {
            uploaderController.importPendingFiles();
        } catch (Throwable e) {
            System.out.println("Error when importing: " + e.getLocalizedMessage());
            reporter.silentLogException(e, "Error when importing pending files");
        }

        try {
            // Initiate the process that moves files from the uploading queue to the uploading process
            uploaderController.startUploading();
        } catch (Throwable e) {
            reporter.reportErrorToUser("Could not start the upload service. Please check the settings and start the service from the menu.", e);
        }
    }
}
