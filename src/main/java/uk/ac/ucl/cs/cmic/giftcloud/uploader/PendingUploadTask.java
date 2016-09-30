/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.util.List;

/**
 * A class representing a set of files waiting to be uploaded
 */
public class PendingUploadTask {

    enum Append {
        APPEND,
        REPLACE
    }

    public enum DeleteAfterUpload {
        DELETE_AFTER_UPLOAD,
        DO_NOT_DELETE_AFTER_UPLOAD
    }

    private final DeleteAfterUpload deleteAfterUpload;
    private final List<String> paths;
    private final Append append;
    private final Optional<String> projectName;

    public PendingUploadTask(final List<String> paths, final Optional<String> projectName, final Append append, final DeleteAfterUpload deleteAfterUpload) {
        this.paths = paths;
        this.projectName = projectName;
        this.append = append;
        this.deleteAfterUpload = deleteAfterUpload;
    }

    public List<String> getPaths() {
        return paths;
    }

    public boolean shouldAppend() {
        return append == Append.APPEND;
    }

    public DeleteAfterUpload getDeleteAfterUpload() {
        return deleteAfterUpload;
    }

    public Optional<String> getProjectName() {
        return projectName;
    }
}
