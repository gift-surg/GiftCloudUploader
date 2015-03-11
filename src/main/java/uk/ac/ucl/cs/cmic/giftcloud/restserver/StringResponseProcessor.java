/*
 * uk.ac.ucl.cs.cmic.giftcloud.restserver.StringResponseProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.io.IOException;
import java.io.InputStream;

class StringResponseProcessor {

	static String getString(final InputStream inputStream) throws IOException {
        final int BUF_SIZE = 512;
        final StringBuilder sb = new StringBuilder();
        final byte[] buf = new byte[BUF_SIZE];
        for (int n = inputStream.read(buf); n > 0; n = inputStream.read(buf)) {
            sb.append(new String(buf, 0, n));
        }
        return sb.toString();
	}
}
