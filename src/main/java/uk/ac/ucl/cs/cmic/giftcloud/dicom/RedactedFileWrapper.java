/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Pparts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUploaderError;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;

/**
 * This class stores the result of an attempted pixel data redaction. This abstracts the requirement to create and
 * delete the redacted version of the file. If a redaction was required then it is performed and the redacted file is
 * returned by getFileToProcess(). If no redaction was required then the original file is returned by
 * getFileToProcess(). The cleanup() method is used to delete a redacted file if it was created. If a redaction is
 * required but no appropriate filter is available, an exception is thrown.
 */
public class RedactedFileWrapper {

    private final FileRedactionStatus redactionStatus;
    private Optional<File> redactedFile;
    private final File originalFile;

    public RedactedFileWrapper(final File originalFile, final Optional<File> redactedFile, final FileRedactionStatus redactionStatus) {
        this.originalFile = originalFile;
        this.redactedFile = redactedFile;
        this.redactionStatus = redactionStatus;
    }

    /**
     * Fetches the file that should be uploaded, which may be a file with the pixel data redacted, or the original file
     *
     * @return a File pointing to the file which is ready to be uploaded
     * @throws GiftCloudException if redaction could not be performed because no appropriate filter was available
     */
    public File getFileToProcess() throws GiftCloudException {
        switch (redactionStatus) {
            case REDACTED:
                return redactedFile.get();
            case REDACTION_NOT_REQUIRED:
                return originalFile;
            case NO_APPROPRIATE_FILTER_FOUND:
                throw new GiftCloudException(GiftCloudUploaderError.NO_REDACTION_FILTER);
            default:
                throw new RuntimeException("Internal error: unknown redaction status");
        }
    }

    /**
     * This method will delete any redacted file, it if was created
     */
    public void cleanup() {
        if (redactedFile.isPresent()) {
            redactedFile.get().delete();
            redactedFile = Optional.empty();
        }
    }

    /**
     * Returns the type of redaction performed, which is dependent on the type of file and the available anonymsiation filters
     */
    public enum FileRedactionStatus {
        REDACTION_NOT_REQUIRED,
        NO_APPROPRIATE_FILTER_FOUND,
        REDACTED
    }
}
