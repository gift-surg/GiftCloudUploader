package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.nrg.dcm.edit.ScriptEvaluationException;

import java.io.IOException;
import java.io.InputStream;

public interface ScriptApplicatorFactory<A> {
    A createScriptApplicator(final InputStream in) throws IOException, ScriptEvaluationException, org.nrg.ecat.edit.ScriptEvaluationException;
}
