/*
 * uk.ac.ucl.cs.cmic.giftcloud.restserver.HttpUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpUtils {
    private HttpUtils() {}


    public static List<String> readEntityLines(final InputStream in) throws IOException {
        IOException ioexception = null;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            final List<String> lines = new ArrayList();
            String line;
            while (null != (line = reader.readLine())) {
                lines.add(line);
            }
            return lines;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw null == ioexception ? e : ioexception;
            }
        }
    }
    
}
