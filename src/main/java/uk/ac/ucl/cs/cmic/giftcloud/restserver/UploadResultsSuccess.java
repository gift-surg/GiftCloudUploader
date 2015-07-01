package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.net.URL;

public class UploadResultsSuccess extends UploadResult {
    private final String uri;
    private final GiftCloudLabel.ExperimentLabel experimentLabel;
    private final URL sessionViewUrl;

    UploadResultsSuccess(final String uri, final GiftCloudLabel.ExperimentLabel experimentLabel, final URL sessionViewUrl) {
        super(true);
        this.uri = uri;
        this.experimentLabel = experimentLabel;
        this.sessionViewUrl = sessionViewUrl;
    }

    public GiftCloudLabel.ExperimentLabel getsessionLabel() {
        return experimentLabel;
    }

    public URL getSessionViewUrl() {
        return sessionViewUrl;
    }

    public String getUri() {
        return uri;
    }
}
