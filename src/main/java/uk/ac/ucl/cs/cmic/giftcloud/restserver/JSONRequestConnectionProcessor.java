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

import com.google.common.base.Strings;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

class JSONRequestConnectionProcessor extends HttpRequestWithOutput<String> {

    private final Logger logger = LoggerFactory.getLogger(JSONRequestConnectionProcessor.class);
    private final JSONObject jsonObject;

    JSONRequestConnectionProcessor(final SessionParameters sessionParameters, final String urlString, final MultiUploadReporter reporter) {
        super(HttpConnectionWrapper.ConnectionType.POST, urlString, new HttpStringResponseProcessor(), reporter);
        this.jsonObject = buildCommitEntity(sessionParameters);
    }

    @Override
    protected void prepareConnection(final HttpConnectionBuilder connectionBuilder) throws IOException {
        super.prepareConnection(connectionBuilder);
        final String MEDIA_TYPE = "application/json";
        connectionBuilder.setContentType(MEDIA_TYPE);
    }

    @Override
    protected void streamToConnection(final OutputStream outputStream) throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        try {
            jsonObject.write(writer);
        } catch (Throwable t) {
            System.out.println("failure");
            t.printStackTrace();
            logger.debug("stream copy failed", t);
        } finally {
            writer.close();
        }
    }

    private JSONObject buildCommitEntity(final SessionParameters sessionParameters) {
        final JSONObject entity = new JSONObject();

        Collection<?> sessionParametersCollection = sessionParameters.getSessionVariables();

        if (null == sessionParametersCollection) {
            logger.error("session variables not assigned in JSONRequestConnectionProcessor.buildCommitEntry()", sessionParametersCollection);
        } else {
            for (final Object o : sessionParametersCollection) {
                if (o instanceof SessionVariable) {
                    final SessionVariable v = (SessionVariable) o;
                    final String path = v.getExportField();
                    if (!Strings.isNullOrEmpty(path)) {
                        try {
                            entity.put(path, v.getValue());
                        } catch (JSONException exception) {
                            String message = "unable to assign session variable " + path;
                            logger.error(message, exception);
                            // Analytics.enter(UploadAssistantApplet.class, Level.ERROR, message, exception);
                        }
                    }
                }
            }
        }
        logger.trace("Built commit entity: {}", entity);
        // Analytics.enter(UploadAssistantApplet.class, String.format("Built commit entity: %s", entity));
        return entity;
    }

}
