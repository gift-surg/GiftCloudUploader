package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;


import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.XnatModalityParams;

import java.util.Collection;

public class UploadParameters {

    private GiftCloudLabel.ExperimentLabel experimentLabel = null;
    private GiftCloudLabel.ScanLabel scanLabel = null;
    private Collection<SessionVariable> sessionVariables = null;
    private String projectName = null;
    private GiftCloudLabel.SubjectLabel subjectLabel;
    private FileCollection fileCollection;
    private XnatModalityParams xnatModalityParams;
    private Iterable<ScriptApplicator> projectApplicators;

    public String getProjectName() {
        return projectName;
    }

    public GiftCloudLabel.ExperimentLabel getExperimentLabel() {
        return experimentLabel;
    }

    public Collection<?> getSessionVariables() {
        return sessionVariables;
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

    public void setSessionVariables(final Collection<SessionVariable> sessionVariables) {
        this.sessionVariables = sessionVariables;
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

    public void setProjectApplicators(Iterable<ScriptApplicator> projectApplicators) {
        this.projectApplicators = projectApplicators;
    }

    public Iterable<ScriptApplicator> getProjectApplicators() {
        return projectApplicators;
    }
}