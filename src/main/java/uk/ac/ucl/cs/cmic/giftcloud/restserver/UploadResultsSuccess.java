package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.net.URL;

public class UploadResultsSuccess extends UploadResult {
    private final String uri;
    private final String sessionLabel;
    private final URL sessionViewUrl;

    UploadResultsSuccess(final String uri, final String sessionLabel, final URL sessionViewUrl) {
        super(true);
        this.uri = uri;
        this.sessionLabel = sessionLabel;
        this.sessionViewUrl = sessionViewUrl;
    }

    public String getsessionLabel() {
        return sessionLabel;
    }

    public URL getSessionViewUrl() {
        return sessionViewUrl;
    }

    public String getUri() {
        return uri;
    }
}
