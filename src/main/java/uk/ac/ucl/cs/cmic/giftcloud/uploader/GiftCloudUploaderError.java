package uk.ac.ucl.cs.cmic.giftcloud.uploader;

public enum GiftCloudUploaderError {
    OK(0, "Completed successfully."),
    SERVER_INVALID(21, "Unable to connect to the GIFT-Cloud server. Please verify that the GIFT-Cloud server is running, and that its URL is correctly specified in the uploader settings."),
    QUERY_RETRIEVE_STILL_IN_PROGRESS(31, "A previous query/retrieve operation is still in progress. Please wait until this completes."),
    NO_QUERY_OR_QUERY_FAILED(32, "A query operation must be performed before a retrieve."),
    EMPTY_AE(33, "The PACS AE title has not been set. Please check the settings in the configuration dialog."),
    NETWORK_PROPERTIES_INVALID(34, "The query failed due to a problem with the PACS settings. Please check the settings in the configuration dialog."),
    QUERY_CANNOT_DETERMINE_PRESENTATION_ADDRESS(35, "The query failed due to a problem with the PACS settings. Please check the settings in the configuration dialog."),  // TODO: remove
    QUERY_MODEL_NOT_SUPPORTED(36, "The query failed due to a problem with the PACS settings. Please check the settings in the configuration dialog."),
    QUERY_NO_HOST(37, "The PACS host name has not been set."),
    QUERY_NO_LISTENER_CALLING_AE(38, "The listener calling AE title has not been set."),
    QUERY_NO_PORT(39, "The PACS port has not been set."),
    QUERY_NO_CALLED_AE_TITLE(40, "The PACS port has not been set."),
    EMPTY_LISTENER_PORT(51, "The Dicom node listener port has not been set."),
    EMPTY_LISTENER_AE_TITLE(52, "The Dicom node listener AE title has not been set."),
    SERVER_CERTIFICATE_FAILURE(61, "Cannot connect to the GIFT-Cloud server because the server certificate has not been added to your local keystore"),
    ANONYMISATION_UNACCEPTABLE(71, "Uploading is not permitted because the anonymisation scripts for this project have not been configured to remove identifiable patient information. Please configure the project anonymation scripts on the GIFT-Cloud server and re-start the GIFT-Cloud Uploader.");

    private final int errorCode;
    private final String userVisibleMessage;

    private GiftCloudUploaderError(final int errorCode, final String userVisibleMessage) {
        this.errorCode = errorCode;
        this.userVisibleMessage = userVisibleMessage;
    }

    public String getUserVisibleMessage() {
        return userVisibleMessage;
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
}