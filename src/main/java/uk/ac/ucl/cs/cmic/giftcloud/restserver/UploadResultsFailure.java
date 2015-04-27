package uk.ac.ucl.cs.cmic.giftcloud.restserver;

public class UploadResultsFailure extends UploadResult {
    private final String htmlFailureText;

    public UploadResultsFailure(final String htmlFailureText) {
        super(false);
        this.htmlFailureText = htmlFailureText;
    }

    public String getHtmlFailureText() {
        return htmlFailureText;
    }
}
