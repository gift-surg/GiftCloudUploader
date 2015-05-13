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
package uk.ac.ucl.cs.cmic.giftcloud.data;

import com.google.common.collect.Sets;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.IndexedSessionLabelFunction;
import org.nrg.dcm.edit.ScriptFunction;
import uk.ac.ucl.cs.cmic.giftcloud.util.PrearchiveCode;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Project {
    // IMPORTANT: This must stay in sync with the AUTO_ARCHIVE constant defined in org.nrg.xnat.archive.GradualDicomImporter.
    public static final String AUTO_ARCHIVE = "autoArchive";

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final String name;
	private final RestServer restServer;
	private final Future<Map<String,String>> subjects;
	private final Set<Subject> newSubjects = Sets.newLinkedHashSet();	// locally added subjects
	private final Future<Map<String,String>> sessions;
	private final Future<Iterable<org.nrg.dcm.edit.ScriptApplicator>> dicomScriptApplicator;
    private final Future<Set<String>> petTracers;
    private final Future<PrearchiveCode> prearchiveCode;

	public Project(final String projectName, final RestServer restServer) {
		this.name = projectName;
		this.restServer = restServer;

		sessions = executor.submit(new ProjectSessionLister(restServer, projectName));
		subjects = executor.submit(new ProjectSubjectLister(restServer, projectName));
        dicomScriptApplicator = executor.submit(new DicomScriptApplicatorRetriever(restServer, projectName, getDicomFunctions(sessions)));
        petTracers = executor.submit(new PETTracerRetriever(restServer, projectName));
        prearchiveCode = executor.submit(new ProjectPreArcCodeRetriever(restServer, projectName));
	}

	/**
	 * Adds a (newly created) subject to the project.
	 * @param subject The subject to add to the project.
	 */
	public void addSubject(final Subject subject) { newSubjects.add(subject); }

	private static Map<String, ScriptFunction>
	getDicomFunctions(final Future<Map<String,String>> sessions) {
		return Collections.singletonMap("makeSessionLabel",
				(ScriptFunction) new IndexedSessionLabelFunction(sessions));
	}

	public RestServer getRestServerHelper() { return restServer; }

	public Collection<Subject> getSubjects()
	throws ExecutionException,InterruptedException {
		final Collection<Subject> s = Sets.newLinkedHashSet();
		for (final Map.Entry<String,String> me : subjects.get().entrySet()) {
			s.add(new Subject(me.getKey(), me.getValue()));
		}
		s.addAll(newSubjects);
		return s;
	}

	public Optional<Subject> findSubject(final String subjectName) throws ExecutionException, InterruptedException {
		for (final Subject subject : getSubjects()) {
			if (subjectName.equals(subject.getLabel()) || subjectName.equals(subject.getId())) {
				return Optional.of(subject);
			}
		}
		return Optional.empty();

	}

	public boolean hasSubject(final String name) throws ExecutionException, InterruptedException {
		for (final Subject subject : getSubjects()) {
			if (name.equals(subject.getLabel())) {
				return true;
			}
		}
		return false;
	}

	public Map<String,String> getSessionLabels()
	throws InterruptedException,ExecutionException {
		return sessions.get();
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

	public Set<String> getPETTracers() throws InterruptedException,ExecutionException {
        return petTracers.get();
    }

	public PrearchiveCode getPrearchiveCode() throws InterruptedException,ExecutionException {
	    return prearchiveCode.get();
	}

	public void dispose() {
		sessions.cancel(true);
		subjects.cancel(true);
		dicomScriptApplicator.cancel(true);
//		ecatScriptApplicator.cancel(true);
	}

	/**
	 * Submits a job to this project's executor service.
	 * @param callable The callable to passed to the executor.
	 * @return Returns the reference to the asynchronous results.
	 */
	public <T> Future<T> submit(Callable<T> callable) {
		return executor.submit(callable);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() { return name; }
}
