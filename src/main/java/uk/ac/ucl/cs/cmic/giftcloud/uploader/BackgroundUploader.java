package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class BackgroundUploader extends BackgroundService<CallableUploader, Future<Set<String>>> {

    private final ResultProgressHandle progress;
    private final boolean useFixedSize = true;
    final BackgroundCompletionServiceTaskList backgroundCompletionServiceTaskList;
    private BackgroundUploadOutcomeCallback outcomeCallback;


    public BackgroundUploader(final BackgroundCompletionServiceTaskList backgroundCompletionServiceTaskList, final ResultProgressHandle progress, final BackgroundUploadOutcomeCallback outcomeCallback, final MultiUploadReporter reporter) {
        super(backgroundCompletionServiceTaskList, reporter);

        this.progress = progress;
        this.backgroundCompletionServiceTaskList = backgroundCompletionServiceTaskList;
        this.outcomeCallback = outcomeCallback;
    }


    private void addFiles(final GiftCloudServer server, List<FileCollection> uploads, XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, CallableUploader.CallableUploaderFactory callableUploaderFactory, UploadStatisticsReporter stats) {
        for (final FileCollection fileCollection : uploads) {
            addFile(server, xnatModalityParams, applicators, projectLabel, subjectLabel, sessionParameters, callableUploaderFactory, stats, fileCollection);
        }
    }

    private void addFile(final GiftCloudServer server, XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, CallableUploader.CallableUploaderFactory callableUploaderFactory, UploadStatisticsReporter stats, FileCollection fileCollection) {
        final CallableUploader uploader = callableUploaderFactory.create(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSize, fileCollection, applicators, stats, server);
        backgroundCompletionServiceTaskList.add(uploader);
    }


    @Override
    protected void processItem(final Future<Set<String>> futureResult) throws Exception {
        final Set<String> result = futureResult.get();
    }

    @Override
    protected void notifySuccess(BackgroundServiceTaskWrapper<CallableUploader, Future<Set<String>>> taskWrapper) {
        final FileCollection fileCollection = taskWrapper.getTask().getFileCollection();
        outcomeCallback.notifySuccess(fileCollection);
    }

    @Override
    protected void notifyFailure(BackgroundServiceTaskWrapper<CallableUploader, Future<Set<String>>> taskWrapper) {
        final FileCollection fileCollection = taskWrapper.getTask().getFileCollection();
        outcomeCallback.notifyFailure(fileCollection);
    }

    interface BackgroundUploadOutcomeCallback {
        void notifySuccess(final FileCollection fileCollection);
        void notifyFailure(final FileCollection fileCollection);

    }
}
