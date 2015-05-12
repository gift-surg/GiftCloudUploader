package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.io.File;
import java.util.Optional;

public interface GiftCloudProperties {

    String propertyName_GiftCloudServerUrl = "GiftCloud_ServerUrl";
    String propertyName_GiftCloudLastUsername = "GiftCloud_LastUsername";
    String propertyName_GiftCloudLastProject = "GiftCloud_LastUploadProject";
    String propertyName_GiftCloudLocalUploadFolder = "GiftCloud_LocalUploadFolder";
    String propertyName_LastImportDirectory = "LastImportDirectory";
    String propertyName_LastExportDirectory = "LastExportDirectory";
    String propertyName_ListenerCallingAeTitle = "GiftCloud_ListenerCallingAeTitle";
    String propertyName_ListenerCalledAeTitle = "GiftCloud_ListenerCalledAeTitle";
    String propertyName_ListenerPort = "GiftCloud_ListenerPort";
    String propertyName_PacsAeTitle = "GiftCloud_RemoteAeTitle";
    String propertyName_PacsHostName = "GiftCloud_RemoteHostName";
    String propertyName_PacsQueryModel = "GiftCloud_RemoteQueryModel";
    String propertyName_PacsPrimaryDeviceType = "GiftCloud_RemotePrimaryDeviceType";
    String propertyName_PacsPort = "GiftCloud_RemotePort";
    String propertyName_Shutdowntimeout = "GiftCloud_ShutdownTimeout";


    Optional<String> getGiftCloudUrl();

    Optional<String> getLastUserName();

    void setLastUserName(final String lastUserName);

    Optional<char[]> getLastPassword();

    void setLastPassword(final char[] text);

    Optional<String> getSessionCookie();

    Optional<String> getLastProject();

    void setLastProject(final String lastProjectName);

    File getUploadFolder(final GiftCloudReporter reporter);

    String getUserAgentString();

    long getShutdownTimeoutMs();

    Optional<String> getPacsAeTitle();

    Optional<String> getPacsHostName();

    int getPacsPort();

    Optional<String> getPacsQueryModel();

    Optional<String> getPacsPrimaryDeviceType();
}
