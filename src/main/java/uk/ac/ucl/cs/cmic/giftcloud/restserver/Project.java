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

import org.nrg.dcm.edit.ScriptFunction;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.IndexedSessionLabelFunction;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Project {

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final String name;
	private final Future<Map<String,String>> subjects;
	private final Future<Map<String,String>> sessions;
	private final Future<Iterable<org.nrg.dcm.edit.ScriptApplicator>> dicomScriptApplicator;

	public Project(final String projectName, final RestServer restServer) {
		this.name = projectName;

		sessions = executor.submit(new ProjectSessionLister(restServer, projectName));
		subjects = executor.submit(new ProjectSubjectLister(restServer, projectName));
        dicomScriptApplicator = executor.submit(new DicomScriptApplicatorRetriever(restServer, projectName, getDicomFunctions(sessions)));
		executor.submit(new ProjectPreArcCodeRetriever(restServer, projectName));
	}

	private static Map<String, ScriptFunction>
	getDicomFunctions(final Future<Map<String,String>> sessions) {
		return Collections.singletonMap("makeSessionLabel",
				(ScriptFunction) new IndexedSessionLabelFunction(sessions));
	}

	public Iterable<org.nrg.dcm.edit.ScriptApplicator> getDicomScriptApplicators() throws IOException {
        try {
            return dicomScriptApplicator.get();
        } catch (InterruptedException e) {
            throw new IOException("Unable to retrieve Dicom scripts", e.getCause());
        } catch (ExecutionException e) {
            throw new IOException("Unable to retrieve Dicom scripts", e.getCause());
        }
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
}
