package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.collect.ImmutableList;
import org.nrg.dcm.edit.ScriptApplicator;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 */
public class DicomProjectAnonymisationScripts {
    private Future<Iterable<ScriptApplicator>> dicomScriptApplicatorFuture;

    public DicomProjectAnonymisationScripts(final Future<Iterable<ScriptApplicator>> dicomScriptApplicatorFuture) {

        this.dicomScriptApplicatorFuture = dicomScriptApplicatorFuture;
    }

    public Iterable<org.nrg.dcm.edit.ScriptApplicator> getDicomScriptApplicators() throws IOException {
        try {
            return ImmutableList.copyOf(dicomScriptApplicatorFuture.get());
        } catch (InterruptedException e) {
            throw new IOException("Unable to retrieve Dicom scripts", e.getCause());
        } catch (ExecutionException e) {
            throw new IOException("Unable to retrieve Dicom scripts", e.getCause());
        }
    }
}
