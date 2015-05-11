package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.network.NetworkApplicationInformation;
import com.pixelmed.network.NetworkApplicationProperties;
import com.pixelmed.network.NetworkConfigurationSource;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GiftCloudPropertiesFromApplication extends Observable implements GiftCloudProperties {

    protected static String propertyName_DicomCurrentlySelectedQueryTargetAE = "Dicom.CurrentlySelectedQueryTargetAE";
    protected static String KEYSTORE_UPLOAD_PASSWORD_KEY = "GiftCloud.UploadPassword";

    private Properties properties;

    final private Optional<String> sessionCookie = Optional.empty();

    private Optional<char[]> lastPassword = Optional.empty(); // Currently this is not stored anywhere, but consider putting it in a keystore
    private final GiftCloudUploaderApplicationBase applicationBase;

    private NetworkApplicationProperties networkApplicationProperties;
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

        try {
            networkApplicationProperties = new NetworkApplicationProperties(properties);

        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            networkApplicationProperties = null;
        }

        addObserver(new GiftCloudPropertiesListener());
    }


    @Override
    public Optional<String> getGiftCloudUrl() {
        final String giftCloudUrl = properties.getProperty(propertyName_GiftCloudServerUrl);
        if (StringUtils.isNotBlank(giftCloudUrl)) {
            return Optional.of(giftCloudUrl);
        } else {
            return Optional.empty();
        }
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
        final String lastUserName = properties.getProperty(propertyName_GiftCloudLastUsername);
        if (StringUtils.isNotBlank(lastUserName)) {
            return Optional.of(lastUserName);
        } else {
            return Optional.empty();
        }
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
        final String lastProject = properties.getProperty(propertyName_GiftCloudLastProject);
        if (StringUtils.isNotBlank(lastProject)) {
            return Optional.of(lastProject);
        } else {
            return Optional.empty();
        }
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
        return 30000;
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

    public boolean areNetworkPropertiesValid() {
        return networkApplicationProperties != null;
    }

    public String getCallingAETitle() {
        return networkApplicationProperties.getCallingAETitle();
    }

    public int getQueryDebugLevel() {
        return networkApplicationProperties.getQueryDebugLevel();
    }

    public String getCalledAETitle() {
        return networkApplicationProperties.getCalledAETitle();
    }

    public int getListeningPort() {
        return networkApplicationProperties.getListeningPort();
    }

    public int getStorageSCPDebugLevel() {
        return networkApplicationProperties.getStorageSCPDebugLevel();
    }

    protected void storeProperties(String comment) throws IOException {
        applicationBase.storePropertiesToApplicationBase(comment);
    }

    public NetworkConfigurationSource getNetworkConfigurationSource() {
        return networkApplicationProperties.getNetworkConfigurationSource();
    }

    public String getPrimaryDeviceType() {
        return networkApplicationProperties.getPrimaryDeviceType();
    }

    public int getNetworkDynamicConfigurationDebugLevel() {
        return networkApplicationProperties.getNetworkDynamicConfigurationDebugLevel();
    }

    public NetworkApplicationInformation getNetworkApplicationInformation() {
        return networkApplicationProperties.getNetworkApplicationInformation();
    }

    public void setListeningPort(int listeningPort) {
        networkApplicationProperties.setListeningPort(listeningPort);
    }

    public void setCalledAETitle(String calledAETitle) {
        networkApplicationProperties.setCalledAETitle(calledAETitle);
    }

    public void setCallingAETitle(String callingAETitle) {
        networkApplicationProperties.setCallingAETitle(callingAETitle);
    }

    public void updatePropertiesWithNetworkProperties() {
        networkApplicationProperties.addToProperties(properties);
    }

    public String getCurrentlySelectedQueryTargetAE() {
        return properties.getProperty(propertyName_DicomCurrentlySelectedQueryTargetAE);
    }

    public void setCurrentlySelectedQueryTargetAE(String ae) {
        properties.setProperty(propertyName_DicomCurrentlySelectedQueryTargetAE, ae);
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


}
