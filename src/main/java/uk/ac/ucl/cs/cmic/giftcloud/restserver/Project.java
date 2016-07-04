/*
 * Project
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.apache.commons.lang.StringUtils;
import org.nrg.dcm.edit.ScriptFunction;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.DicomMetaDataAnonymiser;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.IndexedSessionLabelFunction;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.DicomPixelDataAnonymiser;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PixelDataAnonymiserFilterCache;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Project {

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final String name;
	private final Future<Map<String,String>> subjects;
	private final Future<Map<String,String>> sessions;
	private final Future<Iterable<org.nrg.dcm.edit.ScriptApplicator>> dicomScriptApplicator;
	private Optional<SeriesImportFilterApplicatorRetriever> seriesImportFilter = Optional.empty();
	private final DicomMetaDataAnonymiser dicomMetaDataAnonymiser;
	private final DicomProjectAnonymisationScripts dicomProjectAnonymisationScripts;
	private final DicomPixelDataAnonymiser pixelDataAnonymiser;

	public Project(final String projectName, final RestServer restServer, PixelDataAnonymiserFilterCache pixelDataAnonymiserFilterCache, GiftCloudProperties properties, GiftCloudReporter reporter) {
		this.name = projectName;

		sessions = executor.submit(new ProjectSessionLister(restServer, projectName));
		subjects = executor.submit(new ProjectSubjectLister(restServer, projectName));
		dicomScriptApplicator = executor.submit(new DicomScriptApplicatorRetriever(restServer, projectName, getDicomFunctions(sessions)));
		dicomProjectAnonymisationScripts = new DicomProjectAnonymisationScripts(dicomScriptApplicator);
		dicomMetaDataAnonymiser = new DicomMetaDataAnonymiser(dicomProjectAnonymisationScripts, properties, reporter);
		pixelDataAnonymiser = new DicomPixelDataAnonymiser(pixelDataAnonymiserFilterCache, properties, reporter);
	}

	private static Map<String, ScriptFunction>
	getDicomFunctions(final Future<Map<String,String>> sessions) {
		return Collections.singletonMap("makeSessionLabel",
				(ScriptFunction) new IndexedSessionLabelFunction(sessions));
	}

	public void dispose() {
		sessions.cancel(true);
		subjects.cancel(true);
		dicomScriptApplicator.cancel(true);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() { return name; }

	public SeriesImportFilterApplicatorRetriever getSeriesImportFilter(GiftCloudServer server) throws IOException {
		if (!seriesImportFilter.isPresent()) {
			try {
				if (StringUtils.isEmpty(name)) {
					final Optional<String> emptyProject = Optional.empty();
					seriesImportFilter = Optional.of(new SeriesImportFilterApplicatorRetriever(server, emptyProject));
				} else {  //
					seriesImportFilter = Optional.of(new SeriesImportFilterApplicatorRetriever(server, Optional.of(name)));
				}
			} catch (Exception exception) {
				seriesImportFilter = Optional.empty();
				throw new IOException("Error encountered retrieving series import filters", exception);
			}
		}

		return seriesImportFilter.get();
	}

	public DicomMetaDataAnonymiser getDicomMetaDataAnonymiser() {
		return dicomMetaDataAnonymiser;
	}

	public DicomPixelDataAnonymiser getPixelDataAnonymiser() {
		return pixelDataAnonymiser;
	}
}
