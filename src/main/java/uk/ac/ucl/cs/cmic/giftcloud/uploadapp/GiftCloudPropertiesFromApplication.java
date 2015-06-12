package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.network.NetworkDefaultValues;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GiftCloudPropertiesFromApplication extends Observable implements GiftCloudProperties {

    protected static String KEYSTORE_UPLOAD_PASSWORD_KEY = "GiftCloud.UploadPassword";

    private Properties properties;

    final private Optional<String> sessionCookie = Optional.empty();

    private Optional<char[]> lastPassword = Optional.empty(); // Currently this is not stored anywhere, but consider putting it in a keystore
    private final GiftCloudUploaderApplicationBase applicationBase;

    private final String userAgentString;

    private Optional<PasswordStore> passwordStore = null;


    public GiftCloudPropertiesFromApplication(final GiftCloudUploaderApplicationBase applicationBase, final ResourceBundle resourceBundle) {
        this.applicationBase = applicationBase;
        this.properties = applicationBase.getPropertiesFromApplicationBase();

        try {
            passwordStore = Optional.of(new PasswordStore(new File(System.getProperty("user.home"), ".giftcloudkeys"), "k>9TG*"));
        } catch (Throwable t) {
        }


        // Set the user agent string for the application
        final String nameString = resourceBundle.getString("userAgentNameApplication");
        final String versionString = resourceBundle.getString("mavenVersion");
        userAgentString = nameString + (versionString != null ? versionString : "");

        addObserver(new GiftCloudPropertiesListener());
    }


    @Override
    public Optional<String> getGiftCloudUrl() {
        return getOptionalProperty(propertyName_GiftCloudServerUrl);
    }

    public void setGiftCloudUrl(final String giftCloudUrl) {
        if (!giftCloudUrl.equals(getGiftCloudUrl())) {
            properties.setProperty(propertyName_GiftCloudServerUrl, giftCloudUrl);
            setChanged();
            notifyObservers();
        }
    }

    @Override
    public Optional<String> getLastUserName() {
        return getOptionalProperty(propertyName_GiftCloudLastUsername);
    }


    @Override
    public void setLastUserName(final String lastUserName) {
        if (!lastUserName.equals(getLastUserName())) {
            properties.setProperty(propertyName_GiftCloudLastUsername, lastUserName);
            setChanged();
            notifyObservers();
        }
    }

    @Override
    public Optional<String> getLastProject() {
        return getOptionalProperty(propertyName_GiftCloudLastProject);
    }

    @Override
    public void setLastProject(final String lastProjectName) {
        if (!lastProjectName.equals(getLastProject())) {
            properties.setProperty(propertyName_GiftCloudLastProject, lastProjectName);
            setChanged();
            notifyObservers();
        }
    }

    @Override
    public File getUploadFolder(final GiftCloudReporter reporter) {
        final String uploadFolderString = properties.getProperty(propertyName_GiftCloudLocalUploadFolder);
        if (StringUtils.isNotBlank(uploadFolderString)) {
            return new File(uploadFolderString);
        } else {
            return MultiUploaderUtils.createOrGetLocalUploadCacheDirectory(reporter);
        }
    }

    @Override
    public String getUserAgentString() {
        return userAgentString;
    }

    @Override
    public long getShutdownTimeoutMs() {
        return getLongWithDefault(propertyName_Shutdowntimeout, 30000);
    }

    @Override
    public Optional<String> getPacsAeTitle() {
        return getOptionalProperty(propertyName_PacsAeTitle);
    }

    @Override
    public void setPacsAeTitle(final String pacsAeTitle) {
        setPropertyString(propertyName_PacsAeTitle, pacsAeTitle);
    }

    @Override
    public Optional<String> getPacsHostName() {
        return getOptionalProperty(propertyName_PacsHostName);
    }

    @Override
    public void setPacsHostName(String pacsHostName) {
        setPropertyString(propertyName_PacsHostName, pacsHostName);
    }

    @Override
    public int getPacsPort() {
        return getIntegerWithDefault(propertyName_PacsPort, NetworkDefaultValues.StandardDicomReservedPortNumber);
    }

    @Override
    public void setPacsPort(int port) {
        setPropertyString(propertyName_PacsPort, Integer.toString(port));
    }

    @Override
    public Optional<String> getPacsQueryModel() {
        return getOptionalProperty(propertyName_PacsQueryModel);
    }

    @Override
    public Optional<String> getPacsPrimaryDeviceType() {
        return getOptionalProperty(propertyName_PacsPrimaryDeviceType);
    }

    @Override
    public Optional<Boolean> getHideWindowOnStartup() {
        return getOptionalBoolean(propertyName_HideWindowOnStartup);
    }

    @Override
    public Optional<String> getPatientListLocalCacheFolder() {
        return getOptionalProperty(propertyName_PatientListLocalCacheDirectory);
    }

    @Override
    public void setPatientListExportFolder(final String exportFolder) {
        setPropertyString(propertyName_PatientListExportDirectory, exportFolder);
    }

    @Override
    public Optional<String> getPatientListExportFolder() {
        return getOptionalProperty(propertyName_PatientListExportDirectory);
    }

    @Override
    public Optional<char[]> getLastPassword() {
        if (passwordStore.isPresent()) {
            try {
                return Optional.of(passwordStore.get().retrieve(KEYSTORE_UPLOAD_PASSWORD_KEY));
            } catch (Throwable t) {
                return lastPassword;
            }
        }
        return lastPassword;
    }

    @Override
    public void setLastPassword(char[] lastPassword) {
        this.lastPassword = Optional.of(lastPassword);
        if (passwordStore.isPresent()) {
            try {
                passwordStore.get().store(KEYSTORE_UPLOAD_PASSWORD_KEY, lastPassword);
            } catch (Throwable t) {
            }
        }
    }

    @Override
    public Optional<String> getSessionCookie() {
        return sessionCookie;
    }

    public int getQueryDebugLevel() {
        return getIntegerWithDefault(propertyName_QueryDebugLevel, 0);
    }

    public String getListenerAETitle() {
        return getStringWithDefault(propertyName_ListenerAeTitle, "GiftUploader");
    }

    public int getListeningPort() {
        return getIntegerWithDefault(propertyName_ListenerPort, NetworkDefaultValues.StandardDicomReservedPortNumber);
    }

    public int getStorageSCPDebugLevel() {
        return getIntegerWithDefault(propertyName_StorageSCPDebugLevel, 0);
    }

    protected void storeProperties(String comment) throws IOException {
        applicationBase.storePropertiesToApplicationBase(comment);
    }

    public void setListeningPort(final int listeningPort) {
        setPropertyInteger(propertyName_ListenerPort, listeningPort);
    }

    public void setListenerAETitle(final String calledAETitle) {
        setPropertyString(propertyName_ListenerAeTitle, calledAETitle);
    }

    // ToDo: Previously this was supported via a checkbox
    public boolean acceptAnyTransferSyntax() {
        return true;
    }

    public Optional<String> getLastImportDirectory() {
        final String lastImportDirectory = properties.getProperty(propertyName_LastImportDirectory);
        if (StringUtils.isNotBlank(lastImportDirectory)) {
            return Optional.of(lastImportDirectory);
        } else {
            return Optional.empty();
        }
    }

    public void setLastImportDirectory(final String lastImportDirectory) {
        if (!lastImportDirectory.equals(getLastImportDirectory())) {
            properties.setProperty(propertyName_LastImportDirectory, lastImportDirectory);
            setChanged();
            notifyObservers();
        }
    }

    public Optional<String> getLastExportDirectory() {
        final String lastExportDirectory = properties.getProperty(propertyName_LastExportDirectory);
        if (StringUtils.isNotBlank(lastExportDirectory)) {
            return Optional.of(lastExportDirectory);
        } else {
            return Optional.empty();
        }
    }

    public void setLastExportDirectory(final String lastExportDirectory) {
        if (!lastExportDirectory.equals(getLastExportDirectory())) {
            properties.setProperty(propertyName_LastExportDirectory, lastExportDirectory);
            setChanged();
            notifyObservers();
        }
    }

    // ToDo: Previously this was supported via a checkbox
    public boolean hierarchicalExport() {
        return true;
    }

    // ToDo: Previously this was supported via a checkbox
    public boolean zipExport() {
        return false;
    }

    private class GiftCloudPropertiesListener implements Observer {
        @Override
        public void update(Observable o, Object arg) {
            try {
                System.out.println("** SAVING PROPERTIES **"); // ToDo
                storeProperties("Auto-save after property change");
            } catch (IOException e) {
                // ToDo
                e.printStackTrace();
            }

        }
    }

    private void setPropertyString(final String propertyName, final String propertyValue) {
        properties.setProperty(propertyName, propertyValue);
    }

    private void setPropertyInteger(String propertyName, int propertyValue) {
        properties.setProperty(propertyName, Integer.toString(propertyValue));
    }

    private final Optional<String> getOptionalProperty(final String propertyName) {
        final String propertyValue = getPropertyValue(propertyName);
        if (StringUtils.isNotBlank(propertyValue)) {
            return Optional.of(propertyValue);
        } else {
            return Optional.empty();
        }
    }

    private final Optional<Boolean> getOptionalBoolean(final String propertyName) {
        final String propertyValue = getPropertyValue(propertyName);
        if (StringUtils.isNotBlank(propertyValue)) {
            try {
                return Optional.of(Boolean.parseBoolean(propertyValue));
            } catch (Throwable t) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private Optional<Integer> getOptionalInteger(final String propertyName) {
        final String propertyValue = getPropertyValue(propertyName);
        if (StringUtils.isNotBlank(propertyValue)) {
            try {
                return Optional.of(Integer.parseInt(propertyValue));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private int getIntegerWithDefault(final String propertyName, final int defaultValue) {
        final String propertyValue = getPropertyValue(propertyName);
        if (StringUtils.isNotBlank(propertyValue)) {
            try {
                return Integer.parseInt(propertyValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    private String getStringWithDefault(final String propertyName, final String defaultValue) {
        final String propertyValue = getPropertyValue(propertyName);
        if (StringUtils.isNotBlank(propertyValue)) {
            return propertyValue;
        } else {
            return defaultValue;
        }
    }

    private long getLongWithDefault(final String propertyName, final long defaultValue) {
        final String propertyValue = getPropertyValue(propertyName);
        if (StringUtils.isNotBlank(propertyValue)) {
            try {
                return Long.parseLong(propertyValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    private String getPropertyValue(final String propertyName) {
        return properties.getProperty(propertyName);
    }

}
