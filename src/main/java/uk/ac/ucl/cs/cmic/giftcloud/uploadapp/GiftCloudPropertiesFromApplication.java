/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.network.NetworkDefaultValues;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PropertyStore;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.LoggingReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;


public class GiftCloudPropertiesFromApplication implements GiftCloudProperties {

    String propertyName_JsessionId = "jsessionid";

    protected static String KEYSTORE_UPLOAD_PASSWORD_KEY = "GiftCloud.UploadPassword";
    protected static String KEYSTORE_PATIENT_LIST_SPREDSHEET_PASSWORD_KEY = "GiftCloud.PatientListSpreadsheetPassword";

    private final String userAgentString;
    private final String anonymisationMethodString;
    private final String defaultUrl;

    private final PropertyStore properties;
    private final LoggingReporter reporter;


    public GiftCloudPropertiesFromApplication(final PropertyStore properties, final ResourceBundle resourceBundle, final LoggingReporter reporter) {
        this.reporter = reporter;
        this.properties = properties;

        // Set the user agent string for the application
        final String nameString = resourceBundle.getString("userAgentNameApplication");
        final String anonymisationMethodPrefix = resourceBundle.getString("anonymisationMethodPrefix");
        final String versionString = resourceBundle.getString("mavenVersion");
        userAgentString = (nameString != null ? nameString : "") + (versionString != null ? versionString : "");
        anonymisationMethodString =  (anonymisationMethodPrefix != null ? anonymisationMethodPrefix : "") + (versionString != null ? versionString : "");
        defaultUrl = resourceBundle.getString("defaultServerUrl") ;
    }

    @Override
    public Optional<String> getGiftCloudUrl() {
        return Optional.of(getStringWithDefault(propertyName_GiftCloudServerUrl, defaultUrl));
    }

    public void setGiftCloudUrl(final String giftCloudUrl) {
        final Optional<String> currentUrl = getGiftCloudUrl();
        if (!currentUrl.isPresent() || !giftCloudUrl.equals(currentUrl.get())) {
            properties.setProperty(propertyName_GiftCloudServerUrl, giftCloudUrl);
        }
    }

    @Override
    public Optional<String> getLastUserName() {
        return getOptionalProperty(propertyName_GiftCloudLastUsername);
    }


    @Override
    public void setLastUserName(final String lastUserName) {
        final Optional<String> current = getLastUserName();
        if (!current.isPresent() || !lastUserName.equals(current.get())) {
            properties.setProperty(propertyName_GiftCloudLastUsername, lastUserName);
        }
    }

    @Override
    public Optional<String> getLastProject() {
        return getOptionalProperty(propertyName_GiftCloudLastProject);
    }

    @Override
    public void setLastProject(final String lastProjectName) {
        final Optional<String> current = getLastProject();
        if (!current.isPresent() || !lastProjectName.equals(current.get())) {
            properties.setProperty(propertyName_GiftCloudLastProject, lastProjectName);
        }
    }

    @Override
    public File getUploadFolder(final LoggingReporter reporter) {
        final String uploadFolderString = properties.getProperty(propertyName_GiftCloudLocalUploadFolder);
        if (StringUtils.isNotBlank(uploadFolderString)) {
            return new File(uploadFolderString);
        } else {
            return GiftCloudUtils.createOrGetLocalUploadCacheDirectory(reporter);
        }
    }

    @Override
    public String getUserAgentString() {
        return userAgentString;
    }

    @Override
    public String getAnonymisationMethodString() {
        return anonymisationMethodString;
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
    public int getShortTimeout() {
        return getIntegerWithDefault(propertyName_ShortTimeoutMs, 2000);
    }

    @Override
    public int getLongTimeout() {
        return getIntegerWithDefault(propertyName_LongTimeoutMs, 30000);
    }

    @Override
    public Optional<char[]> getPatientListPassword() {
        return properties.getPassword(KEYSTORE_PATIENT_LIST_SPREDSHEET_PASSWORD_KEY);
    }

    @Override
    public Optional<String> getSubjectPrefix() {
        return getOptionalProperty(propertyName_SubjectNamePrefix);
    }

    @Override
    public void setSubjectPrefix(final String subjectNamePredix) {
        setPropertyString(propertyName_SubjectNamePrefix, subjectNamePredix);
    }

    @Override
    public void setPatientListPassword(char[] patientListPassword) {
        properties.setPassword(KEYSTORE_PATIENT_LIST_SPREDSHEET_PASSWORD_KEY, patientListPassword);
    }

    @Override
    public Optional<char[]> getLastPassword() {
        return properties.getPassword(KEYSTORE_UPLOAD_PASSWORD_KEY);
    }

    @Override
    public void setLastPassword(char[] lastPassword) {
        properties.setPassword(KEYSTORE_UPLOAD_PASSWORD_KEY, lastPassword);
    }

    @Override
    public Optional<String> getSessionCookie() {
        return getOptionalProperty(propertyName_JsessionId);
    }

    public int getQueryDebugLevel() {
        return getIntegerWithDefault(propertyName_QueryDebugLevel, 0);
    }

    @Override
    public String getListenerAETitle() {
        return getStringWithDefault(propertyName_ListenerAeTitle, "GiftUploader");
    }

    public int getListeningPort() {
        return getIntegerWithDefault(propertyName_ListenerPort, NetworkDefaultValues.StandardDicomReservedPortNumber);
    }

    @Override
    public void save() {
        try {
            properties.save("Saving properties");
        } catch (IOException e) {
            reporter.silentLogException(e, "The following error occurred while saving the properties file:" + e.getLocalizedMessage());
        }
    }

    @Override
    public boolean getBurnInOverlays() {
        return false;
    }

    @Override
    public boolean getUseZeroBlackoutValue() {
        return false;
    }

    @Override
    public boolean getUsePixelPaddingBlackoutValue() {
        return true;
    }

    public Optional<String> getLastTemplateImageSourceDirectory() {
        final String lastImportDirectory = properties.getProperty(propertyName_LastTemplateImageImportDirectory);
        if (StringUtils.isNotBlank(lastImportDirectory)) {
            return Optional.of(lastImportDirectory);
        } else {
            return Optional.empty();
        }
    }

    public void setLastTemplateImageSourceDirectory(final String lastTemplateImageSourceDirectory) {
        final Optional<String> current = getLastTemplateImageSourceDirectory();
        if (!current.isPresent() || !lastTemplateImageSourceDirectory.equals(current.get())) {
            properties.setProperty(propertyName_LastTemplateImageImportDirectory, lastTemplateImageSourceDirectory);
        }
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
        final Optional<String> current = getLastImportDirectory();
        if (!current.isPresent() || !lastImportDirectory.equals(current.get())) {
            properties.setProperty(propertyName_LastImportDirectory, lastImportDirectory);
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

    @Override
    public File getFilterDirectory() {
        final String templateFolderString = properties.getProperty(propertyName_GiftCloudLocalUploadFolder);
        if (StringUtils.isNotBlank(templateFolderString)) {
            return new File(templateFolderString);
        } else {
            return GiftCloudUtils.createOrGetTemplateDirectory(reporter);
        }
    }

}
