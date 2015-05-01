package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.io.File;
import java.util.Optional;

public interface GiftCloudProperties {

    String propertyName_GiftCloudServerUrl = "GiftCloud_ServerUrl";
    String propertyName_GiftCloudLastUsername = "GiftCloud_LastUsername";
    String propertyName_GiftCloudLastProject = "GiftCloud_LastUploadProject";
    String propertyName_GiftCloudLocalUploadFolder = "GiftCloud_LocalUploadFolder";
    String propertyName_LastImportDirectory = "LastImportDirectory";
    String propertyName_LastExportDirectory = "LastExportDirectory";

    Optional<String> getGiftCloudUrl();

    Optional<String> getLastUserName();

    void setLastUserName(final String lastUserName);

    Optional<char[]> getLastPassword();

    void setLastPassword(final char[] text);

    Optional<String> getSessionCookie();

    Optional<String> getLastProject();

    void setLastProject(final String lastProjectName);

    File getUploadFolder(final MultiUploadReporter reporter);

    String getUserAgentString();
}
