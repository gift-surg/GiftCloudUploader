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
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudException;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploaderError;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class GiftCloudUploaderRestServer implements RestServer {

    private final RestServerSessionHelper restServerSessionHelper;

    // Access to these members is through a synchronized method to ensure thread safety
    private Optional<String> siteWideAnonScript = Optional.empty();
    private boolean siteWideAnonScriptHasBeenRetrieved = false;


    public GiftCloudUploaderRestServer(final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final ConnectionFactory connectionFactory, final GiftCloudReporter reporter) throws MalformedURLException {
        this.restServerSessionHelper = new RestServerSessionHelper(giftCloudServerUrlString, giftCloudProperties, connectionFactory, reporter);
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
    public Set<String> uploadZipFile(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final File temporaryFile) throws Exception {
        final String dataPostURL;
        final StringBuilder buffer = new StringBuilder();
        buffer.append("/REST/services/import?import-handler=DICOM-zip");
        buffer.append("&PROJECT_ID=").append(projectLabel);
        buffer.append("&SUBJECT_ID=").append(subjectLabel.getStringLabel());
        buffer.append("&EXPT_LABEL=").append(experimentLabel.getStringLabel());

        if (!Strings.isNullOrEmpty(scanLabel.getStringLabel())) {
            buffer.append("&SCAN=").append(scanLabel.getStringLabel());
        }
        buffer.append("&rename=true&prevent_anon=true&prevent_auto_commit=true&SOURCE=applet");

        dataPostURL = buffer.toString();

        return restServerSessionHelper.uploadSeriesUsingZipUpload(dataPostURL, temporaryFile);
    }


    private synchronized void createSubjectIfNotExisting(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel) throws IOException {
        Map<String, String> subjects = getListOfSubjects(projectLabel);

        if (!subjects.containsKey(subjectLabel.getStringLabel())) {
            restServerSessionHelper.createResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel.getStringLabel());
        }
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
            try {
                restServerSessionHelper.createPostResource("/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel.getStringLabel() + "/pseudonyms/" + hashedPatientId);
            } catch (AuthorisationFailureException exception) {
                // This is a special case: the subject was created successfully but the pseudonym creation failed. This probably indicates that project feature "Upload Additional Scans" is not enabled for the Member group of this XNAT project
                throw new GiftCloudException(GiftCloudUploaderError.NO_UPLOAD_PERMISSIONS);
            }
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
    public void appendZipFileToExistingScan(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final XnatModalityParams xnatModalityParams, final File temporaryFile) throws Exception {
        createSubjectIfNotExisting(projectLabel, subjectLabel);

        {
            final String sessionCreateParams = "?xsiType=" + xnatModalityParams.getXnatSessionTag();
            createExperimentIfNotExisting(projectLabel, subjectLabel, experimentLabel, sessionCreateParams);
        }

        {
            final String scanCreateParams = "?xsiType=" + xnatModalityParams.getXnatScanTag();
            createScanIfNotExisting(projectLabel, subjectLabel, experimentLabel, scanLabel, scanCreateParams);
        }

        final String collectionLabel = xnatModalityParams.getCollectionString();

        {
            final String scanCollectionCreateParams = "?xsiType=xnat:resourceCatalog" + "&format=" + xnatModalityParams.getFormatString();
            createScanCollectionIfNotExisting(projectLabel, subjectLabel, experimentLabel, scanLabel, collectionLabel, scanCollectionCreateParams);
        }

        final String uriParams = "?extract=true";
        final String uri = "/data/archive/projects/" + projectLabel + "/subjects/" + subjectLabel + "/experiments/" + experimentLabel + "/scans/" + scanLabel + "/resources/" +  collectionLabel + "/files/" + temporaryFile.getName() + uriParams;

        restServerSessionHelper.appendFileUsingZipUpload(uri, temporaryFile);
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
