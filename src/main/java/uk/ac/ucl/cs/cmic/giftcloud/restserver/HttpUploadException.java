
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

public final class HttpUploadException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int statusCode;
    private final String entity;

    public HttpUploadException(final int statusCode, final String message, final String entity) {
        super(message);
        this.statusCode = statusCode;
        this.entity = entity;
    }

    public int getStatusCode() { return statusCode; }

    public String getEntity() { return entity; }
    
    public String toString() {
        return super.toString() + "\nMessage entity:\n" + entity;       
    }
}
