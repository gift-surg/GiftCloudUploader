/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.dicom;


import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.XnatModalityParams;

/**
 * Stores parameters for uploading data to the GIFT-Cloud server
 */
public class UploadParameters {
    private GiftCloudLabel.ExperimentLabel experimentLabel = null;
    private GiftCloudLabel.ScanLabel scanLabel = null;
    private String projectName = null;
    private GiftCloudLabel.SubjectLabel subjectLabel;
    private FileCollection fileCollection;
    private XnatModalityParams xnatModalityParams;

    public String getProjectName() {
        return projectName;
    }

    public GiftCloudLabel.ExperimentLabel getExperimentLabel() {
        return experimentLabel;
    }

    public GiftCloudLabel.ScanLabel getScanLabel() {
        return scanLabel;
    }

    public void setExperimentLabel(GiftCloudLabel.ExperimentLabel experimentLabel) {
        this.experimentLabel = experimentLabel;
    }

    public void setScanLabel(final GiftCloudLabel.ScanLabel scanLabel) {
        this.scanLabel = scanLabel;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setSubjectLabel(final GiftCloudLabel.SubjectLabel subjectLabel) {
        this.subjectLabel = subjectLabel;
    }

    public GiftCloudLabel.SubjectLabel getSubjectLabel() {
        return subjectLabel;
    }

    public void setFileCollection(final FileCollection fileCollection) {
        this.fileCollection = fileCollection;
    }

    public FileCollection getFileCollection() {
        return fileCollection;
    }

    public void setXnatModalityParams(final XnatModalityParams xnatModalityParams) {
        this.xnatModalityParams = xnatModalityParams;
    }

    public XnatModalityParams getXnatModalityParams() {
        return xnatModalityParams;
    }
}
