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

package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;


import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServer;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;
import uk.ac.ucl.cs.cmic.giftcloud.data.Project;
import uk.ac.ucl.cs.cmic.giftcloud.data.Session;
import uk.ac.ucl.cs.cmic.giftcloud.data.Subject;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class UploadSelector {

    public static String UPLOAD_SELECTOR_WIZARD_ID = "upload_selector";

    private Optional<Project> project = Optional.empty();
    private Optional<Session> session = Optional.empty();
    private Optional<Subject> subject = Optional.empty();
    private Optional<Date> sessionDate = Optional.empty();
    private Optional<String> windowName = Optional.empty();
    private boolean fetchDateFromSession = false;

    private RestServer restServer;
    private GiftCloudReporter reporter;

    public UploadSelector(RestServer restServer, MultiUploadParameters multiUploadParameters, GiftCloudReporter reporter) throws ExecutionException, InterruptedException {
        this.restServer = restServer;
        this.reporter = reporter;

        final String paramWindowName = multiUploadParameters.getParameter(MultiUploadParameters.WINDOW_NAME);
        if (StringUtils.isNotBlank(paramWindowName)) {
            windowName = Optional.of(paramWindowName);
        }


        final Optional<String> projectName = multiUploadParameters.getProjectName();
        if (projectName.isPresent()) {
            createProject(projectName.get());
        }

        Optional<String> subjectName = multiUploadParameters.getSubjectName();
        if (subjectName.isPresent()) {
            if (!isProjectSet()) {
                // TODO: use a placeholder?
                throw new UnsupportedOperationException("can't assign subject without project");
            } else {
                Optional<Subject> subject = getProject().findSubject(subjectName.get());
                setSubject(subject.get());
            }
        }

        if (multiUploadParameters.getDateFromSession()) {
            setFetchDateFromSession(true);
        } else {
            final Optional<String> scanDate = multiUploadParameters.getScanDate();
            if (scanDate.isPresent()) {
                Date date = MultiUploaderUtils.safeParse(scanDate.get());
                if (date != null) {
                    setDate(date);
                }
            }
        }

    }

    public boolean getDateFromSession() {
        return fetchDateFromSession;
    }

    public void setFetchDateFromSession(boolean fromSession) {
        fetchDateFromSession = fromSession;
    }

    /**
     * Returns the Project which has been selected for use
     *
     * @return    the Project selected for use
     */
    public Project getProject() {
        if (!project.isPresent()) {
            throw new RuntimeException("Project requested but none has been selected");
        }
        return project.get();
    }

    public Optional<Project> getOptionalProject() {
        return project;
    }

    public void replaceProject(final String name) {
        if (project.isPresent()) {
            project.get().dispose();
        }
        createProject(name);
    }

    public boolean isProjectSet() {
        return project.isPresent();
    }

    /**
     *
     *
     */
    public void createProject(final String name) {
        if (project.isPresent()) {
            throw new RuntimeException("A Project has already been created");
        }
        project = Optional.of(new Project(name, restServer));
    }

    /**
     *
     *
     */
    public Vector<String> getListOfProjects() throws IOException {
        return restServer.getListOfProjects();
    }



    /**
     * Returns the Project which has been selected for use
     *
     * @return    the Project selected for use
     */
    public Date getDate() {
        if (!sessionDate.isPresent()) {
            throw new RuntimeException("Date requested but none has been selected");
        }
        return sessionDate.get();
    }

    public void setDate(final Date date) {
        if (sessionDate.isPresent()) {
            throw new RuntimeException("Project requested but none has been selected");
        }
        sessionDate = Optional.of(date);
    }

    public boolean isDateSet() {
        return sessionDate.isPresent();
    }

    public Session getSession() {
        if (!session.isPresent()) {
            throw new RuntimeException("Session requested but none has been selected");
        }
        return session.get();
    }

    public void setSession(Session session) {
        this.session = Optional.of(session);
    }

    public boolean isSessionSet() {
        return session.isPresent();
    }

    public Subject getSubject() {
        if (!subject.isPresent()) {
            throw new RuntimeException("Subject requested but none has been selected");
        }
        return subject.get();
    }

    public void setSubject(Subject subject) {
        this.subject = Optional.of(subject);
    }

    public boolean isSubjectSet() {
        return subject.isPresent();
    }

    public void clearSubject() {
        if (subject.isPresent()) {
//            subject.get().dispose();
        }
        subject = Optional.empty();
    }

    public final Optional<JSObject> getJSContext() {
        JSObject object = reporter.getJSContext();
        if (object == null) {
            return Optional.empty();
        } else {
            return Optional.of(object);
        }
    }

    public Optional<String> getWindowName() {
        return windowName;
    }
}
