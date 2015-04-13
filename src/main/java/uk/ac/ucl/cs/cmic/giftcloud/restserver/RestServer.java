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

import org.json.JSONException;
import org.netbeans.spi.wizard.ResultProgressHandle;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import org.nrg.dcm.edit.ScriptApplicator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RestServer {

    private final GiftCloudSession giftCloudSession;

    public RestServer(final GiftCloudProperties giftCloudProperties, final String baseUrl, final MultiUploadReporter reporter) throws IOException {

        giftCloudSession = new GiftCloudSession(giftCloudProperties, new HttpConnectionFactory(baseUrl), reporter);
    }

    public void tryAuthentication() throws IOException {
        giftCloudSession.tryAuthentication();
    }

    public Collection<Object> getValues(final String path, final String key) throws IOException, JSONException {
        return giftCloudSession.request(new HttpRequestWithoutOutput<Collection<Object>>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpJsonResponseProcessor(new JSONValuesExtractor(key))));
    }

    public Map<String, String> getAliases(final String path, final String aliasKey, final String idKey) throws IOException, JSONException {
        return giftCloudSession.request(new HttpRequestWithoutOutput<Map<String, String>>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpJsonResponseProcessor(new JSONAliasesExtractor(aliasKey, idKey))));
    }

    public Optional<String> getPpidAlias(final String path, final String aliasKey, final String idKey) throws IOException, JSONException {
        try {
            final String result = giftCloudSession.request(new HttpRequestWithoutOutput<String>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpJsonPpidResponseProcessor(new JSONAliasesExtractor(aliasKey, idKey))));
            return Optional.of(result);
        } catch (GiftCloudHttpException exception) {

            // 404 indicates the PPID doesn't exist
            if (exception.getResponseCode() == 404) {
                return Optional.empty();
            } else {
                throw exception;
            }
        }
    }

    public <T> Optional<T> getUsingJsonExtractor(final String query) throws IOException {
        try {
            return giftCloudSession.request(new HttpRequestWithoutOutput<Optional<T>>(HttpConnectionWrapper.ConnectionType.GET, query, new HttpJsonResponseProcessor(new JSONConfigurationExtractor())));

        } catch (GiftCloudHttpException exception) {

            // If it's a 404, that's OK, it can not exist.
            if (exception.getResponseCode() == 404) {
                return Optional.empty();
            } else {
                throw exception;
            }
        }
    }

    public String getString(final String path) throws IOException {
        return giftCloudSession.request(new HttpRequestWithoutOutput<String>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpStringResponseProcessor()));
    }

    public Set<String> getStringList(final String path) throws IOException {
        return giftCloudSession.request(new HttpRequestWithoutOutput<Set<String>>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpStringListResponseProcessor()));
    }

    public <ApplicatorT> ApplicatorT getApplicator(final String path, final ScriptApplicatorFactory<ApplicatorT> factory) throws IOException {
        return giftCloudSession.request(new HttpRequestWithoutOutput<ApplicatorT>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpApplicatorResponseProcessor(factory)));
    }

    public void uploadEcat(final String path, final String projectName, final String sessionId, final String subjectLabel, final ResultProgressHandle progress, final File file) throws IOException {
        giftCloudSession.request(new EcatUploadPostRequest(path, file, progress, projectName, subjectLabel, sessionId));
    }

    public String getStringFromStream(final String path, final InputStream xmlStream) throws IOException {
        return giftCloudSession.request(new XmlStreamPostRequestWithStringResponse(path, xmlStream));
    }

    public String sendSessionVariables(final String path, final SessionParameters sessionParameters) throws IOException {
        return giftCloudSession.request(new JSONRequestConnectionProcessor(sessionParameters, path));
    }

    public Set<String> uploadSingleFileAsZip(String relativeUrl, boolean useFixedSizeStreaming, final FileCollection fileCollection, Iterable<ScriptApplicator> applicators, UploadStatisticsReporter progress) throws Exception {
        if (useFixedSizeStreaming) {
            return giftCloudSession.request(new ZipSeriesRequestFixedSize(HttpConnectionWrapper.ConnectionType.PUT, relativeUrl, fileCollection, applicators, progress, new HttpEmptyResponseProcessor()));
        } else {
            return giftCloudSession.request(new ZipSeriesPostRequestChunked(HttpConnectionWrapper.ConnectionType.PUT, relativeUrl, fileCollection, applicators, progress, new HttpEmptyResponseProcessor()));
        }
    }

    public Set<String> uploadZipFile(final String relativeUrl, final boolean useFixedSizeStreaming, final FileCollection fileCollection,
                                     final Iterable<ScriptApplicator> applicators,
                                     final UploadStatisticsReporter progress) throws IOException {
        if (useFixedSizeStreaming) {
            return giftCloudSession.request(new ZipSeriesRequestFixedSize(HttpConnectionWrapper.ConnectionType.POST, relativeUrl, fileCollection, applicators, progress, new HttpSetResponseProcessor()));
        } else {
            return giftCloudSession.request(new ZipSeriesPostRequestChunked(HttpConnectionWrapper.ConnectionType.POST, relativeUrl, fileCollection, applicators, progress, new HttpSetResponseProcessor()));
        }
    }

    // In the event that the user cancels authentication
    public void resetCancellation() {
        giftCloudSession.resetCancellation();
    }

    public void createResource(final String relativeUrl) throws IOException {
        giftCloudSession.request(new HttpRequestWithoutOutput<String>(HttpConnectionWrapper.ConnectionType.PUT, relativeUrl, new HttpStringResponseProcessor()));
    }

    public void createPostResource(final String relativeUrl) throws IOException {
        giftCloudSession.request(new HttpRequestWithoutOutput<String>(HttpConnectionWrapper.ConnectionType.POST, relativeUrl, new HttpStringResponseProcessor()));
    }
}
