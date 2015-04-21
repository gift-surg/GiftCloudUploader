package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.pixelmed.display.EmptyProgress;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServer;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerHelper;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudAutoUploader;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

public class GiftCloudServer {

    private final String giftCloudServerUrl;
    private final MultiUploadReporter reporter;
    private final RestServerHelper restServerHelper;
    private final Container container;
    private final GiftCloudAutoUploader autoUploader;
    private final BackgroundUploader backgroundUploader;
    private final URI giftCloudUri;

    public GiftCloudServer(final String giftCloudServerUrl, final Container container, final GiftCloudProperties giftCloudProperties, final MultiUploadReporter reporter) throws MalformedURLException {
        this.giftCloudServerUrl = giftCloudServerUrl;
        this.reporter = reporter;
        this.container = container;

        if (StringUtils.isBlank(giftCloudServerUrl)) {
            throw new MalformedURLException("Please set the URL for the GIFT-Cloud server.");
        }

        try {
            giftCloudUri = new URI(giftCloudServerUrl);
        } catch (URISyntaxException e) {
            throw new MalformedURLException("The GIFT-Cloud server name " + giftCloudServerUrl + " is not a valid URL.");
        }

        final RestServer restServer = new RestServer(giftCloudProperties, giftCloudServerUrl, reporter);
        restServerHelper = new RestServerHelper(restServer, reporter);
        autoUploader = new GiftCloudAutoUploader(restServerHelper, giftCloudServerUrl, container, reporter);

        final EmptyProgress emptyProgress = new EmptyProgress();

        backgroundUploader = new BackgroundUploader(new BackgroundUploadersInProgressList(), restServerHelper, emptyProgress, reporter);
    }

    public void tryAuthentication() throws IOException {
        restServerHelper.tryAuthentication();
    }

    public Vector<Object> getListOfProjects() throws IOException {
        return restServerHelper.getListOfProjects();
    }

    public boolean uploadToGiftCloud(Vector<String> paths, final String projectName) throws IOException {
        return autoUploader.uploadToGiftCloud(paths, projectName);
    }

    public boolean appendToGiftCloud(Vector<String> paths, final String projectName) throws IOException {
        return autoUploader.appendToGiftCloud(paths, projectName);
    }

    public void resetCancellation() {
        restServerHelper.resetCancellation();
    }

    public boolean matchesServer(final String giftCloudUrl) throws MalformedURLException {
        try {
            final URI uri = new URI(giftCloudUrl);
            return (uri.equals(giftCloudUri));
        } catch (URISyntaxException e) {
            throw new MalformedURLException("The GIFT-Cloud server name " + giftCloudUrl + " is not a valid URL.");
        }
    }

    public RestServerHelper getRestServerHelper() {
        return restServerHelper;
    }

    public String getGiftCloudServerUrl() {
        return giftCloudServerUrl;
    }

    public void addFileInstanceToUploadQueue(String dicomFileName, String projectName) {
    }
}
