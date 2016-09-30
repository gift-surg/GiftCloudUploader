/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg


  Parts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.dicom.DicomException;
import uk.ac.ucl.cs.cmic.giftcloud.util.Progress;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.io.IOException;

/**
 * Parses through a directory structure adding files to the database
 */
public class DicomFileImporter {
    private final UploaderMediaImporter mediaImporter;

    public DicomFileImporter(final boolean acceptAnyTransferSyntax, final GiftCloudUploader giftCloudUploader, final boolean importAsReference, final GiftCloudReporter reporter) {
        mediaImporter = new UploaderMediaImporter(acceptAnyTransferSyntax, giftCloudUploader, importAsReference, reporter);
    }

    public boolean importFiles(final File fileOrDirectory, final Progress progress) throws IOException, DicomException {
        return mediaImporter.importDicomFileOrPath(fileOrDirectory, progress);
    }
}
