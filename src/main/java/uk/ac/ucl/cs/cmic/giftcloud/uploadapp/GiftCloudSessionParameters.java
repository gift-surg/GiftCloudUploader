package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;


import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SessionParameters;
import uk.ac.ucl.cs.cmic.giftcloud.util.AutoArchive;

import java.net.URL;
import java.util.Collection;

public class GiftCloudSessionParameters implements SessionParameters {

    private String sessionLabel = null;
    private String visit = null;
    private GiftCloudLabel.ScanLabel scanLabel = null;
    private String protocol = null;
    private String adminEmail = null;
    private URL baseUrl = null;
    private Boolean useFixedSize = null;
    private Integer numberOfThreads = null;
    private AutoArchive autoArchive = null;
    private Collection<SessionVariable> sessionVariables = null;

    public GiftCloudSessionParameters() {
    }

    @Override
    public String getSessionLabel() {
        return sessionLabel;
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
    public String getAdminEmail() {
        return adminEmail;
    }

    @Override
    public URL getBaseURL() {
        return baseUrl;
    }

    @Override
    public boolean getUseFixedSize() {
        return useFixedSize;
    }

    @Override
    public int getNumberOfThreads() {
        return numberOfThreads;
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

    public void setSessionLabel(String sessionLabel) {
        this.sessionLabel = sessionLabel;
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
    public void setAdminEmail(final String adminEmail) {
        this.adminEmail = adminEmail;
    }
    public void setBaseUrl(final URL baseUrl) {
        this.baseUrl = baseUrl;
    }
    public void setUsedFixedSize(final Boolean useFixedSize) {
        this.useFixedSize = useFixedSize;
    }
    public void setNumberOfThreads(final Integer numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }
    public void setAutoArchive(final AutoArchive autoArchive) {
        this.autoArchive = autoArchive;
    }
    public void setSessionVariables(Collection<SessionVariable> sessionVariables) {
        this.sessionVariables = sessionVariables;
    }
}
