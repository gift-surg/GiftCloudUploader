/*
 * uk.ac.ucl.cs.cmic.giftcloud.restserver.BaseScriptApplicatorRetreiver
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.util.concurrent.Callable;

abstract class BaseScriptApplicatorRetreiver<ApplicatorT> implements Callable<ApplicatorT> {
    protected final RestServer restServer;
    protected final String projectName;
    protected final ScriptApplicatorFactory<ApplicatorT> factory;

    BaseScriptApplicatorRetreiver(final RestServer restServer,
            final ScriptApplicatorFactory<ApplicatorT> factory,
            final String projectName) {
        this.restServer = restServer;
        this.factory = factory;
        this.projectName = projectName;
    }

    public abstract ApplicatorT call() throws Exception;
}
