package uk.ac.ucl.cs.cmic.giftcloud.restserver;

class ErrorDetails {
    private final String title;
    private final String htmlText;
    private Status status;

    enum Status {
        OK,
        ERROR,
        WARNING
    }


    private ErrorDetails(final String title, final String htmlText, final Status status) {
        this.title = title;
        this.htmlText = htmlText;
        this.status = status;
    }

    String getTitle() {
        return title;
    }

    String getHtmlText() {
        return htmlText;
    }

    static ErrorDetails error(final String title, final String htmlText) {
        return new ErrorDetails(title, htmlText, Status.ERROR);
    }

    static ErrorDetails ok(final String title, final String htmlText) {
        return new ErrorDetails(title, htmlText, Status.OK);
    }

    static ErrorDetails warning(final String title, final String htmlText) {
        return new ErrorDetails(title, htmlText, Status.WARNING);
    }
}
