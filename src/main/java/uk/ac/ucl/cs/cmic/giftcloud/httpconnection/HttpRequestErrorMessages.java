/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.httpconnection;

import java.net.URL;

import static java.net.HttpURLConnection.*;

class HttpRequestErrorMessages {

    static ErrorDetails getResponseMessage(final int responseCode, final String urlString, final URL url) {

        switch (responseCode) {
            case HTTP_ACCEPTED:
            case HTTP_NOT_AUTHORITATIVE:
            case HTTP_NO_CONTENT:
            case HTTP_RESET:
            case HTTP_PARTIAL:
            case HTTP_MOVED_PERM:
                return ErrorDetails.warning("Unexpected server response.", "The operation succeeded but returned an unexpected response code");

            case HTTP_OK:
            case HTTP_CREATED:
                return ErrorDetails.ok("OK", "<h3>OK</h3> The operation completed successfully.");

            // Handle 302, at least temporarily: Spring auth redirects to login page,
            // so assume that's what's happened when we see a redirect at this point.
            case HTTP_MOVED_TEMP:
            case HTTP_UNAUTHORIZED:
                return ErrorDetails.error("Authorisation failure", "<h3>Authorisation failure</h3> The username or password was not recognised by the server");

            case HTTP_BAD_REQUEST:
                StringBuilder sb = new StringBuilder("<h3>Session data conflict</h3>");
                sb.append("<p>The server ");
                appendServer(sb, url);
                sb.append(" does not recognise the request or does not support the requested features.</p>");
                return ErrorDetails.error("Unsupported", sb.toString());

            case HTTP_CONFLICT:
                sb = new StringBuilder("<h3>Session data conflict</h3>");
                sb.append("<p>The server ");
                appendServer(sb, url);
                sb.append(" reports a conflict between the uploaded data and a session in the archive.</p>");
                sb.append("<p>All or part of this session was previously uploaded. Go to the prearchive page ");
                sb.append("to archive the data just uploaded as a new session, or to merge it into an existing session.");
                return ErrorDetails.error("Conflict", sb.toString());

            case HTTP_INTERNAL_ERROR:
                sb = new StringBuilder("<h3>Internal Server Error (500)</h3>");
                sb.append("<p>The server ");
                appendServer(sb, url);
                sb.append(" is accessible but was unable to commit the requested session");
                sb.append(" due to an internal error.</p>");
                sb.append("<p>Please contact the administrator for help.");
                sb.append(" A detailed description of the problem should be available");
                sb.append(" in the DICOM receiver log or the XNAT logs.</p>");
                return ErrorDetails.error("Internal Error", sb.toString());

            case HTTP_NOT_FOUND:
                sb = new StringBuilder("<h3>Resource not found (404)</h3>");
                sb.append("<p>The server ");
                appendServer(sb, url);
                sb.append(" is accessible but reports that the session resource ");
                sb.append(urlString);
                sb.append(" does not exist.</p>");
                sb.append("<p>Contact the administrator for help.</p>");
                return ErrorDetails.error("Resource not found", sb.toString());

            default:
                sb = new StringBuilder("<h3>Unexpected error (");
                sb.append(responseCode).append(")</h3><p>The operation could not be completed.</p>");
                sb.append("<p>Please contact the administrator for help.</p>");

                return ErrorDetails.error("Unexpected error", sb.toString());
        }
    }

    private static StringBuilder appendServer(final StringBuilder sb, final URL url) {
        sb.append(url.getProtocol()).append("://");
        sb.append(url.getHost());
        final int httpPort = url.getPort();
        if (-1 != httpPort && httpPort != url.getDefaultPort()) {
            sb.append(":").append(httpPort);
        }
        return sb;
    }
}


