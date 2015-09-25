package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;


import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SessionParameters;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.XnatModalityParams;
import uk.ac.ucl.cs.cmic.giftcloud.util.AutoArchive;

import java.util.Collection;
import java.util.List;

public class UploadParameters implements SessionParameters {

    private GiftCloudLabel.ExperimentLabel experimentLabel = null;
    private String visit = null;
    private GiftCloudLabel.ScanLabel scanLabel = null;
    private String protocol = null;
    private AutoArchive autoArchive = null;
    private Collection<SessionVariable> sessionVariables = null;
    private String projectName = null;
    private GiftCloudLabel.SubjectLabel subjectLabel;
    private List<FileCollection> fileCollections;
    private XnatModalityParams xnatModalityParams;

    public String getProjectName() {
        return projectName;
    }

    @Override
    public GiftCloudLabel.ExperimentLabel getExperimentLabel() {
        return experimentLabel;
    }

    @Override
    public String getVisit() {
        return visit;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public AutoArchive getAutoArchive() {
        return autoArchive;
    }

    @Override
    public Collection<?> getSessionVariables() {
        return sessionVariables;
    }

    @Override
    public GiftCloudLabel.ScanLabel getScanLabel() {
        return scanLabel;
    }

    public void setExperimentLabel(GiftCloudLabel.ExperimentLabel experimentLabel) {
        this.experimentLabel = experimentLabel;
    }

    public void setScanLabel(final GiftCloudLabel.ScanLabel scanLabel) {
        this.scanLabel = scanLabel;
    }

    public void setVisit(final String visit) {
        this.visit = visit;
    }
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public void setSessionVariables(Collection<SessionVariable> sessionVariables) {
        this.sessionVariables = sessionVariables;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setSubjectLabel(GiftCloudLabel.SubjectLabel subjectLabel) {
        this.subjectLabel = subjectLabel;
    }

    public GiftCloudLabel.SubjectLabel getSubjectLabel() {
        return subjectLabel;
    }

    public void setFileCollections(List<FileCollection> fileCollections) {
        this.fileCollections = fileCollections;
    }

    public List<FileCollection> getFileCollections() {
        return fileCollections;
    }

    public void setXnatModalityParams(XnatModalityParams xnatModalityParams) {
        this.xnatModalityParams = xnatModalityParams;
    }

    public XnatModalityParams getXnatModalityParams() {
        return xnatModalityParams;
    }
}
