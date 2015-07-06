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
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.nrg.dcm.edit.ScriptApplicator;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.data.Project;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.Study;
import uk.ac.ucl.cs.cmic.giftcloud.util.AutoArchive;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class GiftCloudUploaderRestServer implements RestServer {

    private final RestServerSessionHelper restServerSessionHelper;
    private final GiftCloudReporter reporter;

    // Access to these members is through a synchronized method to ensure thread safety
    private Optional<String> siteWideAnonScript = Optional.empty();
    private boolean siteWideAnonScriptHasBeenRetrieved = false;

    public static final String PREVENT_ANON = "prevent_anon";
    public static final String PREVENT_AUTO_COMMIT = "prevent_auto_commit";
    public static final String SOURCE = "SOURCE";



    public GiftCloudUploaderRestServer(final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final ConnectionFactory connectionFactory, final GiftCloudReporter reporter) throws MalformedURLException {
        this.restServerSessionHelper = new RestServerSessionHelper(giftCloudServerUrlString, giftCloudProperties, connectionFactory, reporter);
        this.reporter = reporter;
    }

    @Override
    public void tryAuthentication() throws IOException {
        restServerSessionHelper.tryAuthentication();
    }

    @Override
    public Vector<String> getListOfProjects() throws IOException {
        final String uri = "/REST/projects?format=json&owner=true&member=true";
        return new Vector<String>(restServerSessionHelper.getValues(uri, "id"));
    }

    @Override
    public Map<String, String> getListOfSubjects(final String projectName) throws IOException, JSONException {
        final String uri = "/REST/projects/" + projectName + "/subjects?format=json&columns=DEFAULT"; // Note: &columns=DEFAULT is for 1.4rc3 compatibility
        return restServerSessionHelper.getAliases(uri, "label", "ID");
    }

    @Override
    public Map<String, String> getListOfSessions(final String projectName) throws IOException, JSONException {
        final String uri = "/REST/projects/" + projectName + "/experiments?format=json";
        return restServerSessionHelper.getAliases(uri, "label", "ID");
    }

    @Override
    public Map<String, String> getListOfScans(final String projectName, final GiftCloudLabel.SubjectLabel subjectName, final GiftCloudLabel.ExperimentLabel experimentLabel) throws IOException, JSONException {
        final String uri = "/REST/projects/" + projectName + "/subjects/" + subjectName.getStringLabel() + "/experiments/" + experimentLabel.getStringLabel() + "/scans?format=json";
        return restServerSessionHelper.getAliases(uri, "label", "ID");
    }

    @Override
    public Map<String, String> getListOfPseudonyms(final String projectName) throws IOException, JSONException {
        final String uri = "/REST/projects/" + projectName + "/pseudonyms?format=json";
        return restServerSessionHelper.getAliases(uri, "label", "ID");
    }

    @Override
    public Map<String, String> getListOfResources(final String projectName, final GiftCloudLabel.SubjectLabel subjectName, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel) throws IOException, JSONException {
        final String uri = "/REST/projects/" + projectName + "/subjects/" + subjectName.getStringLabel() + "/experiments/" + experimentLabel.getStringLabel() + "/scans/" + scanLabel.getStringLabel() + "/resources?format=json";
        return restServerSessionHelper.getAliases(uri, "label", "ID");
    }

    @Override
    public Optional<GiftCloudLabel.SubjectLabel> getSubjectLabel(final String projectName, final String ppid) throws IOException {
        final String uri = "/REST/projects/" + projectName + "/pseudonyms/" + ppid + "?format=json&columns=DEFAULT";
        final Optional<String> subjectabelString = restServerSessionHelper.getPpidAlias(uri, "label", "ID");
        return subjectabelString.isPresent() ? Optional.of(GiftCloudLabel.SubjectLabel.getFactory().create(subjectabelString.get())) : Optional.<GiftCloudLabel.SubjectLabel>empty();
    }

    @Override
    public Optional<GiftCloudLabel.ExperimentLabel> getExperimentLabel(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final String peid) throws IOException {
        final String uri = "/REST/projects/" + projectName + "/subjects/" + subjectLabel.getStringLabel() + "/experiments/uids/" + peid + "?format=json&columns=DEFAULT";
        final Optional<String> experimentLabelString = restServerSessionHelper.getId(uri, "label");
        return experimentLabelString.isPresent() ? Optional.of(GiftCloudLabel.ExperimentLabel.getFactory().create(experimentLabelString.get())) : Optional.<GiftCloudLabel.ExperimentLabel>empty();
    }

    @Override
    public Optional<GiftCloudLabel.ScanLabel> getScanLabel(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedSeriesInstanceUid) throws IOException {
        final String uri = "/REST/projects/" + projectName + "/subjects/" + subjectLabel.getStringLabel() + "/experiments/" + experimentLabel.getStringLabel() + "/scans/uids/" + hashedSeriesInstanceUid + "?format=json&columns=DEFAULT";
        final Optional<String> scanLabelString = restServerSessionHelper.getId(uri, "ID");
        return scanLabelString.isPresent() ? Optional.of(GiftCloudLabel.ScanLabel.getFactory().create(scanLabelString.get())) : Optional.<GiftCloudLabel.ScanLabel>empty();
    }

    @Override
    public Collection<String> getScriptStatus(final String projectName) throws IOException {
        String uri = "/data/config/edit/projects/" + projectName + "/image/dicom/status/?format=json"; // TD: added JSON field
        return restServerSessionHelper.getValues(uri, "edit");
    }

    @Override
    public Collection<String> getScripts(final String projectName) throws IOException {
        final String uri = "/data/config/edit/projects/" + projectName + "/image/dicom/script";
        return restServerSessionHelper.getValues(uri, "script");
    }

    @Override
    public synchronized Optional<String> getSiteWideAnonScript() throws IOException {
        if (!siteWideAnonScriptHasBeenRetrieved) {
            final Optional<String> result = restServerSessionHelper.getUsingJsonExtractor("/data/config/anon/script?format=json");
            if (result.isPresent() && StringUtils.isNotBlank(result.get())) {
                siteWideAnonScript = result;
            }

            siteWideAnonScriptHasBeenRetrieved = true;
        }

        return siteWideAnonScript;
    }


    @Override
    public Optional<Map<String, String>> getSitewideSeriesImportFilter() throws IOException, JSONException {
        final String uri = "/data/config/seriesImportFilter/config?format=json";
        return restServerSessionHelper.getUsingJsonExtractor(uri);
    }

    @Override
    public Optional<Map<String, String>> getProjectSeriesImportFilter(String projectName) throws IOException, JSONException {
        final String uri = "/data/projects/" + projectName + "/config/seriesImportFilter/config?format=json";
        return restServerSessionHelper.getUsingJsonExtractor(uri);
    }

    @Override
    public String getPreArcCode(final String projectName) throws Exception {
        final String uri = String.format("/data/archive/projects/%s/prearchive_code", projectName);
        return restServerSessionHelper.getString(uri);
    }

    @Override
    public Set<String> getProjectTracers(final String projectName) throws Exception {
        final String uri = "/REST/projects/" + projectName + "/config/tracers/tracers?contents=true";
        return restServerSessionHelper.getStringList(uri);
    }

    @Override
    public Set<String> getSiteTracers() throws Exception {
        final String uri = "/REST/config/tracers/tracers?contents=true";
        return restServerSessionHelper.getStringList(uri);
    }

    @Override
    public String uploadSubject(final String projectName, final InputStream xmlStream) throws Exception {
        final String uri = "/REST/projects/" + projectName + "/subjects";
        return restServerSessionHelper.getStringFromStream(uri, xmlStream);
    }


    @Override
    public UploadResult closeSession(final String uri, final SessionParameters sessionParameters, final Map<FileCollection, Throwable> failures, final Optional<TimeZone> timeZone) {
        final String adminEmail = sessionParameters.getAdminEmail();
        final GiftCloudLabel.ExperimentLabel experimentLabel = sessionParameters.getExperimentLabel();

        // Close session and return result
        try {
            if (failures.isEmpty()) {
                final URL sessionViewUrl = commitSessionAndGetSessionViewUrl(uri, sessionParameters, timeZone);
                return new UploadResultsSuccess(uri, experimentLabel, sessionViewUrl);
            } else {
                reporter.updateStatusText(MultiUploaderUtils.buildFailureMessage(failures));
                return new UploadResultsFailure(MultiUploaderUtils.buildFailureMessage(failures));
            }
        } catch (JSONException e) {
            reporter.error("unable to write commit request entity", e);
            return new UploadResultsFailure("unable to write commit request entity");
        } catch (GiftCloudHttpException e) {
            reporter.error("session commit failed", e);
            return new UploadResultsFailure(e.getHtmlText());
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
            return new UploadResultsFailure(sb.toString());

        } catch (Throwable t) {
            reporter.error("Session commit failed", t);
            final StringBuilder sb = new StringBuilder("<h3>Error in applet</h3>");
            sb.append("<p>An error in the uploader (").append(t);
            sb.append(" prevented the session from being committed.");
            sb.append(" Please contact your administrator ");
            sb.append("<").append(adminEmail).append(">");
            sb.append(" for help.</p>");
            return new UploadResultsFailure(sb.toString());
        }
    }


    private final URL commitSessionAndGetSessionViewUrl(final String uri, final SessionParameters sessionParameters, final Optional<TimeZone> timeZone) throws Exception {
        final String response = commitSession(uri, sessionParameters, timeZone);
        String resultPath = GiftCloudUploaderRestServer.getWebAppRelativePath(sessionParameters.getBaseURL(), response);
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

        return restServerSessionHelper.sendSessionVariables(relativeUrl + queryParams, sessionParameters);
    }

    /**
     * The RestServerSessionHelper URL includes the web application part of the path.
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


    @Override
    public Set<String> uploadZipFile(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final SessionParameters sessionParameters, boolean useFixedSizeStreaming, final FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception {

        final String visit = sessionParameters.getVisit();
        final String protocol = sessionParameters.getProtocol();
        final GiftCloudLabel.ExperimentLabel experimentLabel = sessionParameters.getExperimentLabel();
        final GiftCloudLabel.ScanLabel scanLabel = sessionParameters.getScanLabel();

        final String dataPostURL;
        final StringBuilder buffer = new StringBuilder();
        buffer.append("/REST/services/import?import-handler=DICOM-zip");
        buffer.append("&PROJECT_ID=").append(projectLabel);
        buffer.append("&SUBJECT_ID=").append(subjectLabel.getStringLabel());
        buffer.append("&EXPT_LABEL=").append(experimentLabel.getStringLabel());

        if (!Strings.isNullOrEmpty(scanLabel.getStringLabel())) {
            buffer.append("&SCAN=").append(scanLabel.getStringLabel());
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

        ZipSeriesRequestFactory.ZipStreaming zipStreaming = useFixedSizeStreaming ? ZipSeriesRequestFactory.ZipStreaming.FixedSize : ZipSeriesRequestFactory.ZipStreaming.Chunked;
        return restServerSessionHelper.uploadSeriesUsingZipUpload(dataPostURL, zipStreaming, fileCollection, applicators);
    }

    private synchronized void createSubjectIfNotExisting(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel) throws IOException {
        restServerSessionHelper.createResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel.getStringLabel());
    }

    private synchronized void createExperimentIfNotExisting(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String params) throws IOException {
        Map<String, String> sessions = getListOfSessions(projectLabel);

        if (!sessions.containsKey(experimentLabel.getStringLabel())) {
            restServerSessionHelper.createResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel.getStringLabel() + "/experiments/" + experimentLabel.getStringLabel() + params);
        }
    }

    private synchronized void createScanIfNotExisting(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final String params) throws IOException {
        Map<String,String> scans = getListOfScans(projectLabel, subjectLabel, experimentLabel);

        if (!scans.containsKey(scanLabel.getStringLabel())) {
            restServerSessionHelper.createResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel + "/experiments/" + experimentLabel + "/scans/" + scanLabel.getStringLabel() + params);
        }
    }

    private synchronized void createScanCollectionIfNotExisting(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final String resourceName, final String params) throws IOException {
        Map<String,String> resources = getListOfResources(projectLabel, subjectLabel, experimentLabel, scanLabel);

        if (!resources.containsKey(resourceName)) {
            restServerSessionHelper.createResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel.getStringLabel() + "/experiments/" + experimentLabel.getStringLabel() + "/scans/" + scanLabel.getStringLabel() + "/resources/" + resourceName + params);
        }
    }

    @Override
    public synchronized void createSubjectAliasIfNotExisting(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedPatientId) throws IOException {
        final Optional<GiftCloudLabel.SubjectLabel> subjectLabelFromServer = getSubjectLabel(projectLabel, hashedPatientId);
        if (!subjectLabelFromServer.isPresent()) {
            createSubjectIfNotExisting(projectLabel, subjectLabel);
            restServerSessionHelper.createPostResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel.getStringLabel() + "/pseudonyms/" + hashedPatientId);
        }
    }

    @Override
    public void createExperimentAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final String hashedStudyInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {
        final Optional<GiftCloudLabel.ExperimentLabel> experimentLabelFromServer = getExperimentLabel(projectName, subjectLabel, hashedStudyInstanceUid);
        if (!experimentLabelFromServer.isPresent()) {
            createSubjectIfNotExisting(projectName, subjectLabel);
            final String sessionCreateParams = "?xsiType=" + xnatModalityParams.getXnatSessionTag() + "&UID=" + hashedStudyInstanceUid;
            createExperimentIfNotExisting(projectName, subjectLabel, experimentLabel, sessionCreateParams);
        }
    }


    @Override
    public void appendZipFileToExistingScan(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final SessionParameters sessionParameters, final XnatModalityParams xnatModalityParams, boolean useFixedSizeStreaming, final FileCollection fileCollection, Iterable<ScriptApplicator> applicators) throws Exception {

        createSubjectIfNotExisting(projectLabel, subjectLabel);

        {
            final String sessionCreateParams = "?xsiType=" + xnatModalityParams.getXnatSessionTag();
            createExperimentIfNotExisting(projectLabel, subjectLabel, sessionParameters.getExperimentLabel(), sessionCreateParams);
        }

        {
            final String scanCreateParams = "?xsiType=" + xnatModalityParams.getXnatScanTag();
            createScanIfNotExisting(projectLabel, subjectLabel, sessionParameters.getExperimentLabel(), sessionParameters.getScanLabel(), scanCreateParams);
        }

        final String collectionLabel = xnatModalityParams.getCollectionString();

        {
            final String scanCollectionCreateParams = "?xsiType=xnat:resourceCatalog" + "&format=" + xnatModalityParams.getFormatString();
            createScanCollectionIfNotExisting(projectLabel, subjectLabel, sessionParameters.getExperimentLabel(), sessionParameters.getScanLabel(), collectionLabel, scanCollectionCreateParams);
        }

        final Collection<File> files = fileCollection.getFiles();
        final File firstFile = files.iterator().next();
        final String uriParams = "?extract=true";
        final String uri = "/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel + "/experiments/" + sessionParameters.getExperimentLabel() + "/scans/" + sessionParameters.getScanLabel() + "/resources/" +  collectionLabel + "/files/" + firstFile.getName() + ".zip" + uriParams;

        ZipSeriesRequestFactory.ZipStreaming zipStreaming = useFixedSizeStreaming ? ZipSeriesRequestFactory.ZipStreaming.FixedSize : ZipSeriesRequestFactory.ZipStreaming.Chunked;
        restServerSessionHelper.appendFileUsingZipUpload(uri, zipStreaming, fileCollection, applicators);
    }


    @Override
    public void resetCancellation() {
        restServerSessionHelper.resetCancellation();
    }

    @Override
    public void createScanAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectAlias, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final String hashedSeriesInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {
        final Optional<GiftCloudLabel.ScanLabel> scanLabelFromServer = getScanLabel(projectName, subjectAlias, experimentLabel, hashedSeriesInstanceUid);
        if (!scanLabelFromServer.isPresent()) {
            createSubjectIfNotExisting(projectName, subjectAlias);
            final String sessionCreateParams = "?xsiType=" + xnatModalityParams.getXnatSessionTag();
            createExperimentIfNotExisting(projectName, subjectAlias, experimentLabel, sessionCreateParams);
            final String scanCreateParams = "?xsiType=" + xnatModalityParams.getXnatScanTag() + "&UID=" + hashedSeriesInstanceUid;
            createScanIfNotExisting(projectName, subjectAlias, experimentLabel, scanLabel, scanCreateParams);
        }
    }
}
