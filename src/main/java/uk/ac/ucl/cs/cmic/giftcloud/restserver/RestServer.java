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
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RestServer {

    private final GiftCloudSession giftCloudSession;
    private MultiUploadReporter reporter;

    public RestServer(final GiftCloudProperties giftCloudProperties, final String baseUrl, final MultiUploadReporter reporter) throws MalformedURLException {
        this.reporter = reporter;

        giftCloudSession = new GiftCloudSession(giftCloudProperties, new HttpConnectionFactory(baseUrl), reporter);
    }

    public void tryAuthentication() throws IOException {
        giftCloudSession.tryAuthentication();
    }

    public Collection<Object> getValues(final String path, final String key) throws IOException, JSONException {
        return giftCloudSession.request(new HttpRequestWithoutOutput<Collection<Object>>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpJsonResponseProcessor(new JSONValuesExtractor(key)), reporter));
    }

    public Map<String, String> getAliases(final String path, final String aliasKey, final String idKey) throws IOException, JSONException {
        return giftCloudSession.request(new HttpRequestWithoutOutput<Map<String, String>>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpJsonResponseProcessor(new JSONAliasesExtractor(aliasKey, idKey)), reporter));
    }

    public Optional<String> getPpidAlias(final String path, final String aliasKey, final String idKey) throws IOException, JSONException {
        return giftCloudSession.requestOptional(new HttpRequestWithoutOutput<String>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpJsonPpidResponseProcessor(new JSONAliasesExtractor(aliasKey, idKey)), reporter));
    }

    public <T> Optional<T> getUsingJsonExtractor(final String query) throws IOException {
        return giftCloudSession.requestOptional(new HttpRequestWithoutOutput<Optional<T>>(HttpConnectionWrapper.ConnectionType.GET, query, new HttpJsonResponseProcessor(new JSONConfigurationExtractor()), reporter));
    }

    public String getString(final String path) throws IOException {
        return giftCloudSession.request(new HttpRequestWithoutOutput<String>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpStringResponseProcessor(), reporter));
    }

    public Set<String> getStringList(final String path) throws IOException {
        return giftCloudSession.request(new HttpRequestWithoutOutput<Set<String>>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpStringListResponseProcessor(), reporter));
    }

    public <ApplicatorT> ApplicatorT getApplicator(final String path, final ScriptApplicatorFactory<ApplicatorT> factory) throws IOException {
        return giftCloudSession.request(new HttpRequestWithoutOutput<ApplicatorT>(HttpConnectionWrapper.ConnectionType.GET, path, new HttpApplicatorResponseProcessor(factory), reporter));
    }

    public void uploadEcat(final String path, final String projectName, final String sessionId, final String subjectLabel, final File file) throws IOException {
        giftCloudSession.request(new EcatUploadPostRequest(path, file, projectName, subjectLabel, sessionId, reporter));
    }

    public String getStringFromStream(final String path, final InputStream xmlStream) throws IOException {
        return giftCloudSession.request(new XmlStreamPostRequestWithStringResponse(path, xmlStream, reporter));
    }

    public String sendSessionVariables(final String path, final SessionParameters sessionParameters) throws IOException {
        return giftCloudSession.request(new JSONRequestConnectionProcessor(sessionParameters, path, reporter));
    }

    public Set<String> appendFileUsingZipUpload(final String relativeUrl, final ZipSeriesRequestFactory.ZipStreaming zipStreaming, final FileCollection fileCollection, Iterable<ScriptApplicator> applicators, UploadStatisticsReporter progress) throws IOException {
        return giftCloudSession.request(ZipSeriesRequestFactory.build(HttpConnectionWrapper.ConnectionType.PUT, zipStreaming, relativeUrl, fileCollection, applicators, progress, new HttpEmptyResponseProcessor(), reporter));
    }

    public Set<String> uploadSeriesUsingZipUpload(final String relativeUrl, final ZipSeriesRequestFactory.ZipStreaming zipStreaming, final FileCollection fileCollection, final Iterable<ScriptApplicator> applicators, final UploadStatisticsReporter progress) throws IOException {
        return giftCloudSession.request(ZipSeriesRequestFactory.build(HttpConnectionWrapper.ConnectionType.POST, zipStreaming, relativeUrl, fileCollection, applicators, progress, new HttpSetResponseProcessor(), reporter));
    }

    public void resetCancellation() {
        giftCloudSession.resetCancellation();
    }

    public void createResource(final String relativeUrl) throws IOException {
        giftCloudSession.request(new HttpRequestWithoutOutput<String>(HttpConnectionWrapper.ConnectionType.PUT, relativeUrl, new HttpStringResponseProcessor(), reporter));
    }

    public void createPostResource(final String relativeUrl) throws IOException {
        giftCloudSession.request(new HttpRequestWithoutOutput<String>(HttpConnectionWrapper.ConnectionType.POST, relativeUrl, new HttpStringResponseProcessor(), reporter));
    }
}
