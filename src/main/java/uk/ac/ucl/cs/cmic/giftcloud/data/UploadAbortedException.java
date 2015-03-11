/*
 * UploadAbortedException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

public class UploadAbortedException extends Exception {
    private static final long serialVersionUID = 1L;

    public UploadAbortedException() {}

    public UploadAbortedException(String message) {
        super(message);
    }

    public UploadAbortedException(Throwable cause) {
        super(cause);
    }

    public UploadAbortedException(String message, Throwable cause) {
        super(message, cause);
     }
}
