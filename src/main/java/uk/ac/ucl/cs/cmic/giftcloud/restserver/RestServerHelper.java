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
import com.google.common.collect.Maps;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.nrg.dcm.edit.ScriptApplicator;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.data.Project;
import uk.ac.ucl.cs.cmic.giftcloud.data.UploadFailureHandler;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.Study;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.ZipSeriesAppendUploader;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.ZipSeriesUploader;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.UploadResultPanel;
import uk.ac.ucl.cs.cmic.giftcloud.util.AutoArchive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class RestServerHelper {

    private final RestServer restServer;
    private final MultiUploadReporter reporter;

    // Access to these members is through a synchronized method to ensure thread safety
    private Optional<String> siteWideAnonScript = Optional.empty();
    private boolean siteWideAnonScriptHasBeenRetrieved = false;

    public static final String PREVENT_ANON = "prevent_anon";
    public static final String PREVENT_AUTO_COMMIT = "prevent_auto_commit";
    public static final String SOURCE = "SOURCE";



    public RestServerHelper(final RestServer restServer, final MultiUploadReporter reporter) {
        this.restServer = restServer;
        this.reporter = reporter;
    }

    public void tryAuthentication() throws IOException {
        restServer.tryAuthentication();
    }

    public Vector<Object> getListOfProjects() throws IOException {
        final String uri = "/REST/projects?format=json&owner=true&member=true";
        return new Vector<Object>(restServer.getValues(uri, "id"));
    }

    public Map<String, String> getListOfSubjects(final String projectName) throws IOException, JSONException {
        final String uri = "/REST/projects/" + projectName + "/subjects?format=json&columns=DEFAULT"; // Note: &columns=DEFAULT is for 1.4rc3 compatibility
        return restServer.getAliases(uri, "label", "ID");
    }

    public Map<String, String> getListOfSessions(final String projectName) throws IOException, JSONException {
        final String uri = "/REST/projects/" + projectName + "/experiments?format=json";
        return restServer.getAliases(uri, "label", "ID");
    }

    public Map<String, String> getListOfScans(final String projectName, final String subjectName, final String sessionName) throws IOException, JSONException {
        final String uri = "/REST/projects/" + projectName + "/subjects/" + subjectName + "/experiments/" + sessionName + "/scans?format=json";
        return restServer.getAliases(uri, "label", "ID");
    }

    public Map<String, String> getListOfPseudonyms(final String projectName) throws IOException, JSONException {
        final String uri = "/REST/projects/" + projectName + "/pseudonyms?format=json";
        return restServer.getAliases(uri, "label", "ID");
    }

    public Map<String, String> getListOfResources(final String projectName, final String subjectName, final String sessionName, final String scanName) throws IOException, JSONException {
        final String uri = "/REST/projects/" + projectName + "/subjects/" + subjectName + "/experiments/" + sessionName + "/scans/" + scanName + "/resources?format=json";
        return restServer.getAliases(uri, "label", "ID");
    }

    public Optional<String> getSubjectPseudonym(final String projectName, final String ppid) throws IOException {
        final String uri = "/REST/projects/" + projectName + "/pseudonyms/" + ppid + "?format=json&columns=DEFAULT";
        return restServer.getPpidAlias(uri, "label", "ID");
    }

    public Collection<?> getScriptStatus(final String projectName) throws IOException {
        String uri = "/data/config/edit/projects/" + projectName + "/image/dicom/status/?format=json"; // TD: added JSON field
        return restServer.getValues(uri, "edit");
    }

    public Collection<?> getScripts(final String projectName) throws IOException {
        final String uri = "/data/config/edit/projects/" + projectName + "/image/dicom/script";
        return restServer.getValues(uri, "script");
    }

    public synchronized Optional<String> getSiteWideAnonScript() throws IOException {
        if (!siteWideAnonScriptHasBeenRetrieved) {
            final Optional<String> result = restServer.getUsingJsonExtractor("/data/config/anon/script?format=json");
            if (result.isPresent() && StringUtils.isNotBlank(result.get())) {
                siteWideAnonScript = Optional.of(result.get());
            }

            siteWideAnonScriptHasBeenRetrieved = true;
        }

        return siteWideAnonScript;
    }


    public Optional<Map<String, String>> getSitewideSeriesImportFilter() throws IOException, JSONException {
        final String uri = "/data/config/seriesImportFilter/config?format=json";
        return restServer.getUsingJsonExtractor(uri);
    }

    public Optional<Map<String, String>> getProjectSeriesImportFilter(String projectName) throws IOException, JSONException {
        final String uri = "/data/projects/" + projectName + "/config/seriesImportFilter/config?format=json";
        return restServer.getUsingJsonExtractor(uri);
    }

    public String getPreArcCode(final String projectName) throws Exception {
        final String uri = String.format("/data/archive/projects/%s/prearchive_code", projectName);
        return restServer.getString(uri);
    }

    public Set<String> getProjectTracers(final String projectName) throws Exception {
        final String uri = "/REST/projects/" + projectName + "/config/tracers/tracers?contents=true";
        return restServer.getStringList(uri);
    }

    public Set<String> getSiteTracers() throws Exception {
        final String uri = "/REST/config/tracers/tracers?contents=true";
        return restServer.getStringList(uri);
    }

    public <ApplicatorT> ApplicatorT getApplicator(final String projectName, final ScriptApplicatorFactory<ApplicatorT> factory) throws Exception {
        final String uri = "/REST/projects/" + projectName + "/resources/UPLOAD_CONFIG/files/ecat.eas";
        return restServer.getApplicator(uri, factory);
    }

    public String uploadSubject(final String projectName, final InputStream xmlStream) throws Exception {
        final String uri = "/REST/projects/" + projectName + "/subjects";
        return restServer.getStringFromStream(uri, xmlStream);
    }

    public boolean uploadToStudy(final List<FileCollection> uploads, final XnatModalityParams xnatModalityParams, final Iterable<ScriptApplicator> applicators, final String projectLabel, final String subjectLabel, final SessionParameters sessionParameters, final ResultProgressHandle progress, final Optional<String> windowName, final Optional<JSObject> jsContext, final MultiUploadReporter logger) {

        MultiZipSeriesUploader uploader = new MultiZipSeriesUploader(uploads, xnatModalityParams, applicators, projectLabel, subjectLabel, sessionParameters, progress, windowName, jsContext, logger, this, new ZipSeriesUploader.ZipSeriesUploaderFactory());

        if (!uploader.run(progress, logger)) {
            return false;
        }

        Set<String> uris = uploader.getUris();

        if (1 != uris.size()) {
            logger.error("Server reports unexpected sessionLabel count:" + uris.size() + " : " + uris);
            progress.failed("<p>The XNAT server reported receiving an unexpected number of sessions: (" + uris.size() + ")</p>" + "<p>Please contact the system manager for help.</p>", false);
            return false;
        }

        final String uri = uris.iterator().next();
            final Optional<TimeZone> timeZone = Optional.empty();
            return closeSession(uri, sessionParameters, progress, uploader.getFailures(), windowName, jsContext, timeZone);
    }

    public boolean appendToStudy(final List<FileCollection> uploads, final XnatModalityParams xnatModalityParams, final Iterable<ScriptApplicator> applicators, final String projectLabel, final String subjectLabel, final SessionParameters sessionParameters, final ResultProgressHandle progress, final Optional<String> windowName, final Optional<JSObject> jsContext, final MultiUploadReporter logger) {

        MultiZipSeriesUploader uploader = new MultiZipSeriesUploader(uploads, xnatModalityParams, applicators, projectLabel, subjectLabel, sessionParameters, progress, windowName, jsContext, logger, this, new ZipSeriesAppendUploader.ZipSeriesAppendUploaderFactory());

        if (!uploader.run(progress, logger)) {
            return false;
        }

        Set<String> uris = uploader.getUris();

        if (1 != uris.size()) {
            logger.error("Server reports unexpected sessionLabel count:" + uris.size() + " : " + uris);
            progress.failed("<p>The XNAT server reported receiving an unexpected number of sessions: (" + uris.size() + ")</p>" + "<p>Please contact the system manager for help.</p>", false);
            return false;
        }

        final String uri = uris.iterator().next();
        final Optional<TimeZone> timeZone = Optional.empty();
        return closeSession(uri, sessionParameters, progress, uploader.getFailures(), windowName, jsContext, timeZone);
    }

    public boolean uploadToEcat(final FileCollection fileCollection, final String projectLabel, final String subjectLabel, final SessionParameters sessionParameters, final ResultProgressHandle progress, final Optional<String> windowName, final Optional<JSObject> jsContext, final UploadFailureHandler failureHandler, final TimeZone timeZone, final MultiUploadReporter logger) {


        if (fileCollection.getFileCount() == 0) {
            progress.failed("No ECAT files available to upload", true);
            return false;
        }

        final EcatUploader uploader = new EcatUploader(this, fileCollection, projectLabel, subjectLabel, sessionParameters, progress, failureHandler, timeZone, logger);

        if (!uploader.run()) {
            return false;
        }

        final String sessionLabel = sessionParameters.getSessionLabel();
        progress.setBusy("Creating session " + sessionLabel);

        // No failures, since the upload terminates if a failure occurs.
        // Alternatively, we could obtain the failures from the uploader, but these are currently of a different type - irrelevant since there are none
        final Map<FileCollection, Throwable> failures = Maps.newHashMap();

        closeSession(uploader.getUri(), sessionParameters, progress, failures, windowName, jsContext, Optional.ofNullable(timeZone));

        return true;
    }




    private boolean closeSession(final String uri, final SessionParameters sessionParameters, final ResultProgressHandle progress, final Map<FileCollection, Throwable> failures, final Optional<String> windowName, final Optional<JSObject> jsContext, final Optional<TimeZone> timeZone) {
        final String adminEmail = sessionParameters.getAdminEmail();
        final String session = sessionParameters.getSessionLabel();
        final String fullUrlString = sessionParameters.getBaseURL() + uri;

        // Close session and return result
        try {
            if (failures.isEmpty()) {
                progress.setBusy("Committing session");

                final URL sessionViewUrl = commitSessionAndGetSessionViewUrl(uri, sessionParameters, timeZone);

                // TODO: build summary, notify user
                final UploadResultPanel resultPanel = new UploadResultPanel(session, sessionViewUrl, windowName, jsContext);

                progress.finished(Summary.create(resultPanel, new URL(fullUrlString)));
                return true;
            } else {
                progress.failed(MultiUploaderUtils.buildFailureMessage(failures), false);
                return false;
            }
        } catch (JSONException e) {
            reporter.error("unable to write commit request entity", e);
            // ToDo: TD: should there be a progress.failed call here?
            return false;
        } catch (GiftCloudHttpException e) {
            reporter.error("session commit failed", e);
            progress.failed(e.getHtmlText(), true);
            return false;
        } catch (IOException e) {
            reporter.error("Session commit failed", e);
            final StringBuilder sb = new StringBuilder("<h3>Communications error</h3>");
            sb.append("<p>The server at ");
            sb.append(sessionParameters.getBaseURL() + uri);
            sb.append(" is inaccessible (");
            sb.append(e.getMessage());
            sb.append("). Please contact your administrator ");
            sb.append("<").append(adminEmail).append(">");
            sb.append(" for help.</p>");
            progress.failed(sb.toString(), false);
            return false;
        } catch (Throwable t) {
            reporter.error("Session commit failed", t);
            final StringBuilder sb = new StringBuilder("<h3>Error in applet</h3>");
            sb.append("<p>An error in the uploader (").append(t);
            sb.append(" prevented the session from being committed.");
            sb.append(" Please contact your administrator ");
            sb.append("<").append(adminEmail).append(">");
            sb.append(" for help.</p>");
            progress.failed(sb.toString(), false);
            return false;
        }
    }


    private final URL commitSessionAndGetSessionViewUrl(final String uri, final SessionParameters sessionParameters, final Optional<TimeZone> timeZone) throws Exception {
        final String response = commitSession(uri, sessionParameters, timeZone);
        String resultPath = RestServerHelper.getWebAppRelativePath(sessionParameters.getBaseURL(), response);
        final URL result = new URL(sessionParameters.getBaseURL() + "/" + resultPath);
        return buildSessionViewURL(result, resultPath);
    }

    private final String commitSession(final String relativeUrl, final SessionParameters sessionParameters, final Optional<TimeZone> timeZone) throws Exception
    {
        String queryParams = "?action=commit&SOURCE=applet";

        //add visit
        if (null != sessionParameters.getVisit() && !Strings.isNullOrEmpty(sessionParameters.getVisit())) {
            queryParams += "&VISIT=" + sessionParameters.getVisit();
        }

        //add protocol
        if (null != sessionParameters.getProtocol() && !Strings.isNullOrEmpty(sessionParameters.getProtocol())) {
            queryParams += "&PROTOCOL=" + sessionParameters.getProtocol();
        }

        //add timeZone
        if (timeZone.isPresent()) {
            queryParams += "&TIMEZONE=" + timeZone.get().getID();
        }

        return restServer.sendSessionVariables(relativeUrl + queryParams, sessionParameters);
    }

    /**
     * The RestServer URL includes the web application part of the path.
     * If the given path starts with the web application path, returns the path
     * minus the web application context; otherwise return the full path. Either
     * way, any leading /'s are removed.
     *
     * @param url  The URL to be inspected.
     * @param path The relative path to validate.
     * @return The relative path to the REST server URL, stripped of leading slashes.
     */
    private static String getWebAppRelativePath(final URL url, final String path) {
        final StringBuilder buffer = new StringBuilder(path);
        while ('/' == buffer.charAt(0)) {
            buffer.deleteCharAt(0);
        }
        final String context = url.getPath();
        boolean pathHasContext = true;
        for (int i = 0; i < context.length(); i++) {
            if (context.charAt(i) != path.charAt(i)) {
                pathHasContext = false;
                break;
            }
        }
        if (pathHasContext) {
            buffer.delete(0, context.length());
        }
        while ('/' == buffer.charAt(0)) {
            buffer.deleteCharAt(0);
        }
        return buffer.toString();
    }

    private static URL buildSessionViewURL(final URL url, final String relativePath) {
        final String[] components = relativePath.split("/");
        if (!"data".equals(components[0]) && !"REST".equals(components[0])) {
            LoggerFactory.getLogger(Study.class).warn("Strange session path {}: first component is neither \"data\" nor \"REST\"", relativePath);
        }
        if ("prearchive".equals(components[1])) {
            // prearchive sessions need some extra help for nice display
            try {
                return new URL(url.toString() + "?screen=XDATScreen_uploaded_xnat_imageSessionData.vm");
            } catch (MalformedURLException e) {
                LoggerFactory.getLogger(Study.class).error("can't build prearchive session view url for " + url, e);
                return url;
            }
        } else {
            // archived sessions are viewable using REST url
            return url;
        }
    }


    public Set<String> uploadZipFile(final String projectLabel, final String subjectLabel, final SessionParameters sessionParameters, boolean useFixedSizeStreaming, final FileCollection fileCollection, Iterable<ScriptApplicator> applicators, UploadStatisticsReporter progress) throws Exception {

        final String visit = sessionParameters.getVisit();
        final String protocol = sessionParameters.getProtocol();
        final String sessionLabel = sessionParameters.getSessionLabel();
        final String scanLabel = sessionParameters.getScanLabel();

        final String dataPostURL;
        final StringBuilder buffer = new StringBuilder();
        buffer.append("/REST/services/import?import-handler=DICOM-zip");
        buffer.append("&PROJECT_ID=").append(projectLabel);
        buffer.append("&SUBJECT_ID=").append(subjectLabel);
        buffer.append("&EXPT_LABEL=").append(sessionLabel);

        if (!Strings.isNullOrEmpty(scanLabel)) {
            buffer.append("&SCAN=").append(scanLabel);
        }
        if (!Strings.isNullOrEmpty(visit)) {
            buffer.append("&VISIT=").append(visit);
        }
        if (!Strings.isNullOrEmpty(protocol)) {
            buffer.append("&PROTOCOL=").append(protocol);
        }
        buffer.append("&rename=true&prevent_anon=true&prevent_auto_commit=true&SOURCE=applet");

        final AutoArchive autoArchive = sessionParameters.getAutoArchive();
        if (autoArchive != null) {
            buffer.append("&").append(Project.AUTO_ARCHIVE).append("=").append(autoArchive);
        }
        dataPostURL = buffer.toString();

        return restServer.uploadZipFile(dataPostURL, useFixedSizeStreaming, fileCollection, applicators, progress);
    }

    private void createSubjectIfNotExisting(final String projectLabel, final String subjectLabel) throws IOException {
        restServer.createResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel);
    }

    private void createSessionIfNotExisting(final String projectLabel, final String subjectLabel, final String sessionLabel, final String params) throws IOException {
        Map<String, String> sessions = getListOfSessions(projectLabel);

        if (!sessions.containsKey(sessions)) {
            restServer.createResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel + "/experiments/" + sessionLabel + params);
        }
    }

    private void createScanIfNotExisting(final String projectLabel, final String subjectLabel, final String sessionLabel, final String scanLabel, final String params) throws IOException {
        Map<String,String> scans = getListOfScans(projectLabel, subjectLabel, sessionLabel);

        if (!scans.containsKey(scanLabel)) {
            restServer.createResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel + "/experiments/" + sessionLabel + "/scans/" + scanLabel + params);
        }
    }

    private void createScanCollectionIfNotExisting(final String projectLabel, final String subjectLabel, final String sessionLabel, final String scanLabel, final String resourceName, final String params) throws IOException {
        Map<String,String> resources = getListOfResources(projectLabel, subjectLabel, sessionLabel, scanLabel);

        if (!resources.containsKey(resourceName)) {
            restServer.createResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel + "/experiments/" + sessionLabel + "/scans/" + scanLabel + "/resources/" + resourceName + params);
        }
    }

    public void createPseudonymIfNotExisting(final String projectLabel, final String subjectLabel, final String pseudonym) throws IOException {
        final Optional<String> pseuodynym = getSubjectPseudonym(projectLabel, subjectLabel);
        if (!pseuodynym.isPresent()) {
            createSubjectIfNotExisting(projectLabel, subjectLabel);
            restServer.createPostResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel + "/pseudonyms/" + pseudonym);
        }
    }

    public Set<String> appendZipFileToExistingScan(final String projectLabel, final String subjectLabel, final SessionParameters sessionParameters, final XnatModalityParams xnatModalityParams, boolean useFixedSizeStreaming, final FileCollection fileCollection, Iterable<ScriptApplicator> applicators, UploadStatisticsReporter progress) throws Exception {

        createSubjectIfNotExisting(projectLabel, subjectLabel);

        {
            final String sessionCreateParams = "?xsiType=" + xnatModalityParams.getXnatSessionTag();
            createSessionIfNotExisting(projectLabel, subjectLabel, sessionParameters.getSessionLabel(), sessionCreateParams);
        }

        {
            final String scanCreateParams = "?xsiType=" + xnatModalityParams.getXnatScanTag();
            createScanIfNotExisting(projectLabel, subjectLabel, sessionParameters.getSessionLabel(), sessionParameters.getScanLabel(), scanCreateParams);
        }

        final String collectionLabel = xnatModalityParams.getCollectionString();

        {
            final String scanCollectionCreateParams = "?format=" + xnatModalityParams.getFormatString() + "&xsi:type=" + xnatModalityParams.getXnatScanTag();
            createScanCollectionIfNotExisting(projectLabel, subjectLabel, sessionParameters.getSessionLabel(), sessionParameters.getScanLabel(), collectionLabel, scanCollectionCreateParams);
        }

        final Collection<File> files = fileCollection.getFiles();
        final File firstFile = files.iterator().next();
        final String uriParams = "?extract=true";
        final String uri = "/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel + "/experiments/" + sessionParameters.getSessionLabel() + "/scans/" + sessionParameters.getScanLabel() + "/resources/" + collectionLabel + "/files/" + firstFile.getName() + ".zip" + uriParams;

        return restServer.uploadSingleFileAsZip(uri, useFixedSizeStreaming, fileCollection, applicators, progress);
    }

    public void uploadEcat(final String projectLabel, final String subjectLabel, final SessionParameters sessionParameters, final String timestamp, final String timeZoneId, final ResultProgressHandle progress, final File file, final int fileNumber) throws Exception {

        final String visit = sessionParameters.getVisit();
        final String protocol = sessionParameters.getProtocol();
        final String sessionLabel = sessionParameters.getSessionLabel();

        final String dataPostURL;
        final StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("/data/services/import?dest=/prearchive/projects/%s/%s/%s", projectLabel, timestamp, sessionLabel));

        if (!Strings.isNullOrEmpty(visit)){
            buffer.append("&VISIT=").append(visit);
        }
        if (!Strings.isNullOrEmpty(protocol)){
            buffer.append("&PROTOCOL=").append(protocol);
        }
        if (!Strings.isNullOrEmpty(timeZoneId)){
            buffer.append("&TIMEZONE=").append(timeZoneId);
        }
        buffer.append("&overwrite=append&rename=true&prevent_anon=true&prevent_auto_commit=true&SOURCE=applet");
        dataPostURL = buffer.toString();

        restServer.uploadEcat(dataPostURL, projectLabel, sessionLabel, subjectLabel, progress, file);
    }


    public void resetCancellation() {
        restServer.resetCancellation();
    }


}
