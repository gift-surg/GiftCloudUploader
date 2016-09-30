/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class representing the status of a collection of files being uploaded
 */
public class FileStatusGroup {
    private final String description;
    private final String modality;
    private final String date;
    private Set<String> filesToDo = new HashSet<String>();
    private Set<String> filesDone = new HashSet<String>();

    /**
     * Create a new status item for a group of files
     * @param description the viisble name for this group of files
     * @param fileUids a set of unique itentifiers representing the files
     */
    public FileStatusGroup(final String date, final String modality, final String description, final List<String> fileUids) {
        this.date = date;
        this.modality = modality;
        this.description = description;
        add(fileUids);
    }

    /**
     * @return the user-visible group description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return a string (x/y) where x is the number of files uploaded and y is the total number of files
     */
    public String getFileNumbers() {
        int numFilesToDo = filesToDo.size();
        int numFilesDone = filesDone.size();
        if (numFilesToDo == 0 && numFilesDone > 0) {
            return "(" + Integer.toString(numFilesDone) + "/" + Integer.toString(numFilesDone + numFilesToDo) + ")";
        } else if (numFilesToDo > 0 && numFilesDone > 0) {
            return "(" + Integer.toString(numFilesDone) + "/" + Integer.toString(numFilesDone + numFilesToDo) + ")";
        } else if (numFilesToDo > 0 && numFilesDone == 0) {
            return "(" + Integer.toString(numFilesDone) + "/" + Integer.toString(numFilesDone + numFilesToDo) + ")";
        } else {
            return "(" + Integer.toString(numFilesDone) + "/" + Integer.toString(numFilesDone + numFilesToDo) + ")";
        }
    }

    /**
     * @return a status string describing the current state of uploading
     */
    public String getStatus() {
        int numFilesToDo = filesToDo.size();
        int numFilesDone = filesDone.size();
        if (numFilesToDo == 0 && numFilesDone > 0) {
            return "Done";
        } else if (numFilesToDo > 0 && numFilesDone > 0) {
            return "Uploading";
        } else if (numFilesToDo > 0 && numFilesDone == 0) {
            return "Waiting";
        } else {
            return "Nothing to do";
        }
    }

    /**
     * Adds a new file to the list in this collection
     *
     * @param fileUids
     */
    public void add(final List<String> fileUids) {
        for (final String uid : fileUids) {
            filesToDo.add(uid);

            // If a file is re-uploaded then we need to remove it from the already done list, otherwise the visible counts will change in non-intuitive ways
            if (filesDone.contains(uid)) {
                filesDone.remove(uid);
            }
        }
    }

    /**
     * Call this to indicate that a file has completed uploading
     *
     * @param fileUid
     */
    public void done(final String fileUid) {
        if (filesToDo.contains(fileUid)) {
            filesToDo.remove(fileUid);
        }
        filesDone.add(fileUid);
    }

    /**
     * @return a @String indicating the date when this group was added
     */
    public String getDate() {
        return date;
    }

    /**
     * @return a @String describing the modality of the grup
     */
    public String getModality() {
        return modality;
    }
}
