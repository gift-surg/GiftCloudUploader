package uk.ac.ucl.cs.cmic.giftcloud.uploader;

public enum GiftCloudUploaderError {
    OK(0, "Completed successfully."),
    QUERY_RETRIEVE_STILL_IN_PROGRESS(31, "A previous query/retrieve operation is still in progress. Please wait until this completes."),
    NO_QUERY_OR_QUERY_FAILED(32, "A query operation must be performed before a retrieve."),
    EMPTY_AE(33, "The PACS AE title has not been set. Please check the settings in the configuration dialog."),
    NETWORK_PROPERTIES_INVALID(34, "The query failed due to a problem with the PACS settings. Please check the settings in the configuration dialog."),
    QUERY_CANNOT_DETERMINE_PRESENTATION_ADDRESS(35, "The query failed due to a problem with the PACS settings. Please check the settings in the configuration dialog."),
    QUERY_MODEL_NOT_SUPPORTED(36, "The query failed due to a problem with the PACS settings. Please check the settings in the configuration dialog.");

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