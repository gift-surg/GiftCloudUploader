/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UserCallback;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PixelDataAnonymiserFilterCache;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.ProjectCache;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GiftCloudServer {

    private final String giftCloudServerUrlString;
    private GiftCloudProperties giftCloudProperties;
    private GiftCloudReporter reporter;
    private final RestClient restClient;
    private final URI giftCloudUri;
    private final ProjectCache projectCache;

    public GiftCloudServer(final PixelDataAnonymiserFilterCache filters, final RestServerFactory restServerFactory, final String giftCloudServerUrlString, final GiftCloudProperties giftCloudProperties, final UserCallback userCallback, final GiftCloudReporter reporter) throws MalformedURLException {
        this.giftCloudServerUrlString = giftCloudServerUrlString;
        this.giftCloudProperties = giftCloudProperties;
        this.reporter = reporter;

        if (StringUtils.isBlank(giftCloudServerUrlString)) {
            throw new MalformedURLException("Please set the URL for the GIFT-Cloud server.");
        }

        try {
            giftCloudUri = new URI(giftCloudServerUrlString);
        } catch (URISyntaxException e) {
            throw new MalformedURLException("The GIFT-Cloud server name " + giftCloudServerUrlString + " is not a valid URL.");
        }

        restClient = restServerFactory.create(giftCloudServerUrlString, giftCloudProperties, userCallback, reporter);
        projectCache = new ProjectCache(restClient, filters);
    }

    public void tryAuthentication() throws IOException {
        restClient.tryAuthentication();
    }

    public List<String> getListOfProjects() throws IOException {
        return restClient.getListOfProjects();
    }

    public Project getProject(final String projectName) {
        return projectCache.getProject(projectName, giftCloudProperties, reporter);
    }

    public void resetCancellation() {
        restClient.resetCancellation();
    }

    public boolean matchesServer(final String giftCloudUrl) throws MalformedURLException {
        try {
            final URI uri = new URI(giftCloudUrl);
            return (uri.equals(giftCloudUri));
        } catch (URISyntaxException e) {
            throw new MalformedURLException("The GIFT-Cloud server name " + giftCloudUrl + " is not a valid URL.");
        }
    }

    public String getGiftCloudServerUrl() {
        return giftCloudServerUrlString;
    }

    public Map<String,String> getListOfSubjects(final String projectName) throws IOException {
        return restClient.getListOfSubjects(projectName);
    }

    public Map<String, String> getListOfSessions(final String projectName) throws IOException {
        return restClient.getListOfSessions(projectName);
    }

    public Map<String, String> getListOfScans(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel) throws IOException {
        return restClient.getListOfScans(projectName, subjectLabel, experimentLabel);
    }

    public Optional<Map<String, String>> getSitewideSeriesImportFilter() throws IOException {
        return restClient.getSitewideSeriesImportFilter();
    }

    public Optional<Map<String, String>> getProjectSeriesImportFilter(final String projectName) throws IOException {
        return restClient.getProjectSeriesImportFilter(projectName);
    }

    public Set<String> uploadZipFile(final String projectLabel, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentLabel, final GiftCloudLabel.ScanLabel scanLabel, final XnatModalityParams xnatModalityParams, final File temporaryFile, final boolean append) throws Exception {
        return restClient.uploadZipFile(projectLabel, subjectLabel, experimentLabel, scanLabel, xnatModalityParams, temporaryFile, append);
    }

    public void createSubjectAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectName, final String hashedPatientId) throws IOException {
        restClient.createSubjectAliasIfNotExisting(projectName, subjectName, hashedPatientId);
    }

    public Optional<GiftCloudLabel.SubjectLabel> getSubjectLabel(final String projectName, final String hashedPatientId) throws IOException {
        return restClient.getSubjectLabel(projectName, hashedPatientId);
    }

    public void createExperimentAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentAlias, final String hashedStudyInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {
        restClient.createExperimentAliasIfNotExisting(projectName, subjectLabel, experimentAlias, hashedStudyInstanceUid, xnatModalityParams);
    }

    public Optional<GiftCloudLabel.ExperimentLabel> getExperimentLabel(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final String hashedStudyInstanceUid) throws IOException {
        return restClient.getExperimentLabel(projectName, subjectLabel, hashedStudyInstanceUid);
    }

    public void createScanAliasIfNotExisting(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentAlias, final GiftCloudLabel.ScanLabel scanLabel, final String hashedSeriesInstanceUid, final XnatModalityParams xnatModalityParams) throws IOException {
        restClient.createScanAliasIfNotExisting(projectName, subjectLabel, experimentAlias, scanLabel, hashedSeriesInstanceUid, xnatModalityParams);
    }

    public Optional<GiftCloudLabel.ScanLabel> getScanLabel(final String projectName, final GiftCloudLabel.SubjectLabel subjectLabel, final GiftCloudLabel.ExperimentLabel experimentAlias, final String hashedSeriesInstanceUid) throws IOException {
        return restClient.getScanLabel(projectName, subjectLabel, experimentAlias, hashedSeriesInstanceUid);
    }
}
