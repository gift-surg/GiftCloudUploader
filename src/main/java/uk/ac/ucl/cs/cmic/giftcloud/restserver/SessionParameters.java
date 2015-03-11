package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.AutoArchive;

import java.net.URL;
import java.util.Collection;

public interface SessionParameters {
    String getSessionLabel();

    String getVisit();

    String getProtocol();

    String getAdminEmail();

    URL getBaseURL();

    boolean getUseFixedSize();

    int getNumberOfThreads();

    AutoArchive getAutoArchive();

    Collection<?> getSessionVariables();

    String getScanLabel();
}
