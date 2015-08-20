package uk.ac.ucl.cs.cmic.giftcloud.uploader;

public enum GiftCloudUploaderError {
    OK(0, "Completed successfully.", "OK", true),
    SERVER_INVALID(21, "Unable to connect to the GIFT-Cloud server. Please verify that the GIFT-Cloud server is running, and that its URL is correctly specified in the uploader settings.", "GIFT-Cloud host name is incorrect or the server is down", true),
    QUERY_RETRIEVE_STILL_IN_PROGRESS(31, "A previous query/retrieve operation is still in progress. Please wait until this completes.", "Query/retrieve in progress", true),
    NO_QUERY_OR_QUERY_FAILED(32, "A query operation must be performed before a retrieve.", "No query", false),
    EMPTY_AE(33, "The PACS AE title has not been set. Please check the settings in the configuration dialog.", "No AE title has been specified in the settings.", false),
    NETWORK_PROPERTIES_INVALID(34, "The query failed due to a problem with the PACS settings. Please check the settings in the configuration dialog.", "The PACS settings are invalid", false),
    QUERY_CANNOT_DETERMINE_PRESENTATION_ADDRESS(35, "The query failed due to a problem with the PACS settings. Please check the settings in the configuration dialog.", "The PACS settings are invalid", false),  // TODO: remove
    QUERY_MODEL_NOT_SUPPORTED(36, "The query failed due to a problem with the PACS settings. Please check the settings in the configuration dialog.", "The PACS settings are invalid", false),
    QUERY_NO_HOST(37, "The query cannot be performed because the PACS host name has not been set.", "The PACS host name has not been set", false),
    QUERY_NO_LISTENER_CALLING_AE(38, "The query cannot be performed because the listener calling AE title has not been set.", "The listener AE title has not been set", false),
    QUERY_NO_PORT(39, "The query cannot be performed because the PACS port has not been set.", "The PACS port has not been set", false),
    QUERY_NO_CALLED_AE_TITLE(40, "The AE title has not been set.", "AE title has not been set", false),
    QUERY_FAILURE(41, "The PACS query failed. Please check that the PACS settings are correct and ensure the PACS server is running.", "PACS query failed.", false),
    EMPTY_LISTENER_PORT(51, "The Dicom node listener port has not been set.",  "The Dicom node listener port has not been set", false),
    EMPTY_LISTENER_AE_TITLE(52, "The Dicom node listener AE title has not been set.",  "The Dicom node listener AE title has not been set", false),
    SERVER_CERTIFICATE_FAILURE(61, "Cannot connect to the GIFT-Cloud server because the server certificate has not been added to your local keystore", "GIFT-Cloud Server certificate needs to be added to local keystore", false),
    ANONYMISATION_UNACCEPTABLE(71, "Uploading is not permitted because the anonymisation scripts for this project have not been configured to remove identifiable patient information. Please configure the project anonymation scripts on the GIFT-Cloud server and re-start the GIFT-Cloud Uploader.", "Uploading disabled because anonymisation is not configured correctly", false),
    MODALITY_UNSUPPORTED(80, "File could not be uploaded because the modality is not currently supported by GIFT-Cloud", "Modality is not supported", false),
    MODALITY_UNSUPPORTED_US(81, "File could not be uploaded because ultrasound is not currently supported by GIFT-Cloud", "Ultrasound images are not currently supported", false),
    NO_UPLOAD_PERMISSIONS(90, "Uploading is not permitted becasue your user does not have permission to 'Upload Additional Scans' for this project. Please ask your system administrator to enable this permission for your project.", "User not permitted to upload", false);

    private final int errorCode;
    private final String userVisibleMessage;
    private final String pithyMessage;
    private final boolean allowRetry;

    GiftCloudUploaderError(final int errorCode, final String userVisibleMessage, final String pithyMessage, final boolean allowRetry) {
        this.errorCode = errorCode;
        this.userVisibleMessage = userVisibleMessage;
        this.pithyMessage = pithyMessage;
        this.allowRetry = allowRetry;
    }

    public String getUserVisibleMessage() {
        return userVisibleMessage;
    }

    public String getPithyMessage() {
        return pithyMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessageWithErrorCode() {
        return errorCode + ": " + userVisibleMessage;
    }

    @Override
    public String toString() {
        return "GIFT-Cloud Uploader error " + getMessageWithErrorCode();
    }

    public boolean allowRetry() {
        return allowRetry;
    }
}