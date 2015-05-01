/**
 * ProjectPreArcCodeRetriever
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 12/12/11 by rherri01
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.util.PrearchiveCode;

import java.util.concurrent.Callable;

public class ProjectPreArcCodeRetriever implements Callable<PrearchiveCode> {
    private final RestServer restServer;
    private final String projectName;

    public ProjectPreArcCodeRetriever(final RestServer restServer, final String projectName) {
        this.restServer = restServer;
        this.projectName = projectName;
    }

    /*
      * (non-Javadoc)
      * @see java.util.concurrent.Callable#call()
      */
    @Override
    public PrearchiveCode call() throws Exception {
        return PrearchiveCode.code(restServer.getPreArcCode(projectName));
    }
}
