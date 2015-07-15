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
    String propertyName_PatientListLocalCacheDirectory = "GiftCloud_PatientListLocalCacheFolder";
    String propertyName_PatientListExportDirectory = "GiftCloud_PatientListExportFolder";
    String propertyName_ListenerAeTitle = "GiftCloud_ListenerAeTitle";
    String propertyName_ListenerPort = "GiftCloud_ListenerPort";
    String propertyName_PacsAeTitle = "GiftCloud_RemoteAeTitle";
    String propertyName_PacsHostName = "GiftCloud_RemoteHostName";
    String propertyName_PacsQueryModel = "GiftCloud_RemoteQueryModel";
    String propertyName_PacsPrimaryDeviceType = "GiftCloud_RemotePrimaryDeviceType";
    String propertyName_HideWindowOnStartup = "GiftCloud_HideWindowOnStartup";
    String propertyName_PacsPort = "GiftCloud_RemotePort";
    String propertyName_Shutdowntimeout = "GiftCloud_ShutdownTimeout";
    String propertyName_QueryDebugLevel = "GiftCloud_QueryDebugLevel";
    String propertyName_StorageSCPDebugLevel = "GiftCloud_StorageSCPDebugLevel";
    String propertyName_ShortTimeoutMs = "GiftCloud_QuickAuthenticationTimeout";
    String propertyName_LongTimeoutMs = "GiftCloud_RequestTimeout";


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

    void setPacsAeTitle(final String pacsAeTitle);

    Optional<String> getPacsHostName();

    void setPacsHostName(final String pacsHostName);

    int getPacsPort();

    void setPacsPort(final int port);

    Optional<String> getPacsQueryModel();

    Optional<String> getPacsPrimaryDeviceType();

    Optional<Boolean> getHideWindowOnStartup();

    Optional<String> getPatientListLocalCacheFolder();

    void setPatientListExportFolder(final String exportFolder);

    Optional<String> getPatientListExportFolder();

    int getShortTimeout();

    int getLongTimeout();

    void setPatientListPassword(char[] patientListPassword);

    Optional<char[]> getPatientListPassword();
}
