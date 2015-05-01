/*
 * uk.ac.ucl.cs.cmic.giftcloud.restserver.DicomScriptApplicatorRetriever
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.collect.Lists;
import org.nrg.dcm.edit.ScriptApplicator;
import org.nrg.dcm.edit.ScriptEvaluationException;
import org.nrg.dcm.edit.ScriptFunction;
import org.nrg.dcm.edit.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public final class DicomScriptApplicatorRetriever
implements Callable<Iterable<ScriptApplicator>> {

    private final Logger logger = LoggerFactory.getLogger(DicomScriptApplicatorRetriever.class);
    private final RestServer restServer;
    private final String project, projBasePath;
    private final ScriptApplicatorFactory<ScriptApplicator> factory;
    private final Map<String, ScriptFunction> scriptFunctions;

    public DicomScriptApplicatorRetriever(final RestServer restServer, final String project, final Map<String, ScriptFunction> scriptFunctions) {
        this.restServer = restServer;
        this.project = project;
        this.projBasePath = "/data/config/edit/projects/" + project + "/image/dicom/";
        this.factory = buildFactory(scriptFunctions);
        this.scriptFunctions = scriptFunctions;

    }

    // site and project anon scripts are not isomorphic
    // site: /data/config/anon/script?contents=true
    // project: enabled/disabled: /data/config/edit/projects/PROJECT/image/dicom/status (key: edit)
    //          script contents:  /data/config/edit/projects/PROJECT/image/dicom/script (key: script)

    private static ScriptApplicatorFactory<ScriptApplicator>
    buildFactory(final Map<String, ScriptFunction> scriptFunctions) {
        return new ScriptApplicatorFactory<ScriptApplicator>() {
            public ScriptApplicator createScriptApplicator(final InputStream in)
                    throws IOException,ScriptEvaluationException {
                return new ScriptApplicator(in, scriptFunctions);
            }
        };
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    public final Iterable<ScriptApplicator> call() throws Exception {
        final List<ScriptApplicator> applicators = Lists.newArrayList();

        Optional<String> script = restServer.getSiteWideAnonScript();
        if (script.isPresent()) {
            // The site script is straightforward.
            final ScriptApplicator siteScript = new ScriptApplicator(new ByteArrayInputStream(script.get().getBytes()), scriptFunctions);
            if (null != siteScript && !siteScript.getStatements().isEmpty()) {
                applicators.add(siteScript);
            }
        }

        // The project scripts are sort of complicated.
        Collection<?> statusc = restServer.getScriptStatus(project);
        logger.trace("project {} script status: {}", project, statusc);
        if (statusc.contains("true")) {
            Collection<?> scriptsc = restServer.getScripts(project);
            logger.trace("project {} script text: {}", project, scriptsc);
            for (final Object scripto : scriptsc) {
                if (null == scripto) continue;
                final ByteArrayInputStream bais = new ByteArrayInputStream(scripto.toString().getBytes("UTF-8"));
                try {
                    final ScriptApplicator projScript = factory.createScriptApplicator(bais);
                    if (null != projScript && !projScript.getStatements().isEmpty()) {
                        applicators.add(projScript);
                    }
                } finally {
                    bais.close();
                }
            }
        }
        
        // Unify similarly-named variables across scripts.
        // Variables in later scripts override earlier definitions.
        for (int i = 1; i < applicators.size(); i++) {
            final Iterable<Variable> vs = applicators.get(i).getSortedVariables();
            for (int j = 0; j < i; j++) {
                for (final Variable v : vs) {
                    applicators.get(j).unify(v);
                }
            }
        }
        return applicators;
    }
}
