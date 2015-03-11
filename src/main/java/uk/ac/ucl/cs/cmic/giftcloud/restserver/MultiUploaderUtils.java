/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    Released under the Simplified BSD.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.nrg.IOUtils;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import org.nrg.util.Base64;
import uk.ac.ucl.cs.cmic.giftcloud.util.Utils;

import java.io.*;
import java.net.PasswordAuthentication;
import java.net.URLConnection;
import java.text.*;
import java.util.*;

public class MultiUploaderUtils {
    static final String AUTHORIZATION_HEADER = "Authorization";

    private static final DateFormat PARSER_SQL = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat PARSER_CAL = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");


    static String makeBasicAuthorization(final PasswordAuthentication auth) {
        return "Basic " + Base64.encode(auth.getUserName() + ":" + Arrays.toString(auth.getPassword()));
    }

    static void addBasicAuthorizationToHeaderMap(final Map<String, String> m,
                                                 final PasswordAuthentication auth) {
        m.put(AUTHORIZATION_HEADER, makeBasicAuthorization(auth));
    }

    public static JSONObject extractJSONEntity(final InputStream in)
            throws IOException, JSONException {
        return new JSONObject(new JSONTokener(new InputStreamReader(in)));
    }

    public static JSONArray extractResultFromEntity(final JSONObject entity)
            throws JSONException {
        return entity.getJSONObject("ResultSet").getJSONArray("Result");
    }

    static void addBasicAuthorization(final URLConnection conn, final PasswordAuthentication auth) {
        conn.addRequestProperty(AUTHORIZATION_HEADER, makeBasicAuthorization(auth));
    }

    public static String getErrorEntity(final InputStream errorStream) throws IOException {
        try {
            if (null != errorStream) {
                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                IOUtils.copy(stream, errorStream);
                if (stream.size() > 0) {
                    return stream.toString();
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } catch (IOException ignored) {
            return "";
        } finally {
            if (errorStream != null) {
                try {
                    errorStream.close();
                } catch (IOException ignored) {
                    // Ignore any errors here
                }
            }
        }
    }

    public static Date safeParse(final String scanDate) {
        if (StringUtils.isBlank(scanDate)) {
            return null;
        }
        try {
            return PARSER_CAL.parse(scanDate);
        } catch (ParseException e) {
            try {
                return PARSER_SQL.parse(scanDate);
            } catch (ParseException e1) {
                return null;
            }
        }
    }

    /**
     * Reads a list of newline-separated strings from the provided InputStream.
     * @param in InputStream from which strings will be read
     * @return A list of strings found in the input stream. Each line becomes a string.
     * @throws java.io.IOException
     */
    public static List<String> readStrings(final InputStream in) throws IOException {
        final List<String> items = Lists.newArrayList();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            if (StringUtils.isNotBlank(line)) {
                items.add(line.trim());
            }
        }
        return items;
    }

    public static <T> StringBuilder buildEcatFailureMessage(final StringBuilder sb, final Map<File,T> failures) {
        final Multimap<T,File> inverse = LinkedHashMultimap.create();
        Multimaps.invertFrom(Multimaps.forMap(failures), inverse);
        final Multimap<Object,File> causes = Utils.consolidateKeys(inverse, 4);
        for (final Object key : causes.keySet()) {
            final Collection<File> files = causes.get(key);
            sb.append(files.size()).append(" files not uploaded: ").append(key);
            sb.append(LINE_SEPARATOR);
        }
        return sb;
    }

    public static String buildFailureMessage(final Map<FileCollection, Throwable> failures) {
        final StringBuilder sb = new StringBuilder("<html>");
        buildHTMLFailureMessage(sb, failures);
        return sb.append("</html>").toString();
    }

    private static StringBuilder buildHTMLFailureMessage(final StringBuilder sb, final Map<FileCollection, Throwable> failures) {
        final Multimap<Throwable, FileCollection> inverse = LinkedHashMultimap.create();
        Multimaps.invertFrom(Multimaps.forMap(failures), inverse);
        final Multimap<Object, ?> causes = Utils.consolidateKeys(inverse, 4);
        final MessageFormat format = new MessageFormat("{0} not uploaded: {1}");
        format.setFormatByArgumentIndex(0, new ChoiceFormat(new double[]{0, 1, 2},
                new String[]{"No items", "One item", "{0,number} items"}));
        for (final Object key : causes.keySet()) {
            final Collection<?> items = causes.get(key);
            final Object message;
            if (key instanceof HttpUploadException) {
                final HttpUploadException e = (HttpUploadException) key;
                final StringBuilder m = new StringBuilder("HTTP error ");
                m.append(e.getStatusCode()).append(" - ");
                m.append(e.getMessage()).append("<br>");
                m.append(e.getEntity());
                message = m;
            } else {
                message = key;
            }
            sb.append("<p>").append(format.format(new Object[]{items.size(), message}));
            sb.append("</p><br>");
        }
        return sb;
    }

}
