/*
 * uk.ac.ucl.cs.cmic.giftcloud.util.UIUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.util;

import java.io.IOException;
import java.net.URISyntaxException;

public class UIUtils {
    private UIUtils() {
    }

    /**
     * Initializes the cookie handler. This takes the root URL for the XNAT REST service as its parameter. All cookies
     * will be associated with that URL.
     * @param xnatUrl    The root URL for the XNAT REST service.
     * @throws URISyntaxException Thrown when the XNAT URL parameter is an invalid syntax for constructing a URI.
     * @throws IOException Thrown when there are format or validation errors with the submitted URL parameter.
     */

    public static boolean getConfirmSessionDatePage() {
        return Boolean.parseBoolean(System.getProperty("verification.date.display", "true"));
    }
}
