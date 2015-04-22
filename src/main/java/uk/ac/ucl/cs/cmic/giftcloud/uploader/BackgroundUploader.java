package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class BackgroundUploader extends BackgroundService<Callable<Set<String>>, BackgroundCompletionServiceTaskList<Set<String>>, Future<Set<String>>> {

    private final RestServerHelper restServerHelper;
    private final ResultProgressHandle progress;
    private final boolean useFixedSize = true;
    final BackgroundCompletionServiceTaskList backgroundCompletionServiceTaskList;


    public BackgroundUploader(final BackgroundCompletionServiceTaskList backgroundCompletionServiceTaskList, final RestServerHelper restServerHelper, final ResultProgressHandle progress, final MultiUploadReporter reporter) {
        super(backgroundCompletionServiceTaskList, reporter);
        this.restServerHelper = restServerHelper;
        this.progress = progress;
        this.backgroundCompletionServiceTaskList = backgroundCompletionServiceTaskList;
    }


    private void addFiles(List<FileCollection> uploads, XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, CallableUploader.CallableUploaderFactory callableUploaderFactory, UploadStatisticsReporter stats) {
        for (final FileCollection fileCollection : uploads) {
            addFile(xnatModalityParams, applicators, projectLabel, subjectLabel, sessionParameters, callableUploaderFactory, stats, fileCollection);
        }
    }

    private void addFile(XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, CallableUploader.CallableUploaderFactory callableUploaderFactory, UploadStatisticsReporter stats, FileCollection fileCollection) {
        final CallableUploader uploader = callableUploaderFactory.create(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSize, fileCollection, applicators, stats, restServerHelper);
        backgroundCompletionServiceTaskList.add(uploader);
    }


    @Override
    protected void processItem(Future<Set<String>> futureResult) throws Exception {
        final Set<String> result = futureResult.get();
    }

}
