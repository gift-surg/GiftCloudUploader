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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class HttpUtils {
    private HttpUtils() {}

    public static String readEntity(final HttpConnectionWrapper connection) throws IOException {
        InputStream in;
        try {
            in = connection.getInputStream();
            if (null == in) {
                in = connection.getErrorStream();
            }
        } catch (IOException e) {
            in = connection.getErrorStream();
        }
        return null == in ? null : readEntity(in);
    }

    public static String readEntity(final InputStream in)
    throws IOException{
        return Joiner.on("<p>").join(readEntityLines(in));
    }


    public static List<String> readEntityLines(final InputStream in) throws IOException {
        IOException ioexception = null;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            final List<String> lines = Lists.newArrayList();
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
