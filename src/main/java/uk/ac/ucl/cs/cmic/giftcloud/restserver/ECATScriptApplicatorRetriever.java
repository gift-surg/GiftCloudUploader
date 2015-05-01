/*
 * uk.ac.ucl.cs.cmic.giftcloud.restserver.ECATScriptApplicatorRetriever
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.nrg.ecat.edit.ScriptApplicator;
import org.nrg.ecat.edit.ScriptEvaluationException;
import org.nrg.ecat.edit.ScriptFunction;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Callable;

public final class ECATScriptApplicatorRetriever
extends BaseScriptApplicatorRetreiver<ScriptApplicator>
implements Callable<ScriptApplicator> {
	public ECATScriptApplicatorRetriever(final RestServer restServer, final String projectName,
			final Map<String,? extends ScriptFunction> scriptFunctions) {
		super(restServer, buildFactory(scriptFunctions), projectName
				);
	}
	
	private static ScriptApplicatorFactory<ScriptApplicator>
	buildFactory(final Map<String,? extends ScriptFunction> scriptFunctions) {
		return new ScriptApplicatorFactory<ScriptApplicator>() {
			public ScriptApplicator createScriptApplicator(final InputStream in)
                    throws IOException, ScriptEvaluationException {
				return new ScriptApplicator(in, scriptFunctions);
			}
		};
	}

	public ScriptApplicator call() throws Exception {
        return restServer.getApplicator(projectName, factory);
    }
}
