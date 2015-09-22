package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;


import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SessionParameters;
import uk.ac.ucl.cs.cmic.giftcloud.util.AutoArchive;

import java.util.Collection;

public class GiftCloudSessionParameters implements SessionParameters {

    private GiftCloudLabel.ExperimentLabel experimentLabel = null;
    private String visit = null;
    private GiftCloudLabel.ScanLabel scanLabel = null;
    private String protocol = null;
    private AutoArchive autoArchive = null;
    private Collection<SessionVariable> sessionVariables = null;

    public GiftCloudSessionParameters() {
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

    public void setUsedFixedSize(final Boolean useFixedSize) {
    }

    public void setSessionVariables(Collection<SessionVariable> sessionVariables) {
        this.sessionVariables = sessionVariables;
    }
}
