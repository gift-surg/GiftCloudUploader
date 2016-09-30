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

import uk.ac.ucl.cs.cmic.giftcloud.util.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.MasterFileImporter;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploaderStatusModel;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.util.List;

public class ImportWorker implements Runnable {
    private final UploaderStatusModel uploaderStatusModel;
    private final GiftCloudReporter reporter;
    private final List<File> fileList;
    private final Progress progress;
    private final MasterFileImporter masterFileImporter;

    public ImportWorker(final List<File> fileList, final Progress progress, final boolean acceptAnyTransferSyntax, final GiftCloudUploader giftCloudUploader, final boolean importAsReference, final UploaderStatusModel uploaderStatusModel, final GiftCloudReporter reporter) {
        this.uploaderStatusModel = uploaderStatusModel;
        this.reporter = reporter;
        this.progress = progress;
        masterFileImporter = new MasterFileImporter(acceptAnyTransferSyntax, giftCloudUploader, importAsReference, reporter);
        this.fileList = fileList;
    }

    public void run() {
        uploaderStatusModel.setImportingStatusMessage("Importing files...");
        reporter.startProgressBar();

        try {
            boolean anyFiles = masterFileImporter.importFiles(fileList, progress);
            final String statusMessage = anyFiles ? "Files have been imported and added to the upload queue." : "Waiting to receive files.";
            uploaderStatusModel.setImportingStatusMessage(statusMessage);
        } catch (Exception e) {
            uploaderStatusModel.setImportingStatusMessage("Failure when importing files" , e);
            reporter.silentLogException(e, "Failure when importing files");
        }

        reporter.endProgressBar();
        // importer sends its own completion message to log, so do not need another one
    }
}
