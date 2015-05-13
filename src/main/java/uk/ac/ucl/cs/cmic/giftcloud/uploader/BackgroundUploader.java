package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class BackgroundUploader extends BackgroundService<CallableUploader, Future<Set<String>>> {

    /**
     * When re-starting the service and the previous thread has been signalled to stop but has not yet completed, this
     * is how long we wait before just going ahead and creating a new thread anyway
     */
    private static final long MAXIMUM_THREAD_COMPLETION_WAIT_MS = 10000;

    private final ResultProgressHandle progress;
    private final boolean useFixedSize = true;
    final BackgroundCompletionServiceTaskList backgroundCompletionServiceTaskList;
    private BackgroundUploadOutcomeCallback outcomeCallback;


    public BackgroundUploader(final BackgroundCompletionServiceTaskList backgroundCompletionServiceTaskList, final ResultProgressHandle progress, final BackgroundUploadOutcomeCallback outcomeCallback, final GiftCloudReporter reporter) {
        super(BackgroundService.BackgroundThreadTermination.CONTINUE_UNTIL_TERMINATED, backgroundCompletionServiceTaskList, MAXIMUM_THREAD_COMPLETION_WAIT_MS, reporter);

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
        backgroundCompletionServiceTaskList.addNewTask(uploader);
    }


    @Override
    protected void processItem(final Future<Set<String>> futureResult) throws Exception {
        final Set<String> result = futureResult.get();
    }

    @Override
    protected void notifySuccess(BackgroundServiceTaskWrapper<CallableUploader, Future<Set<String>>> taskWrapper) {
        final FileCollection fileCollection = taskWrapper.getTask().getFileCollection();
        outcomeCallback.fileUploadSuccess(fileCollection);
    }

    @Override
    protected void notifyFailure(BackgroundServiceTaskWrapper<CallableUploader, Future<Set<String>>> taskWrapper) {
        final FileCollection fileCollection = taskWrapper.getTask().getFileCollection();
        outcomeCallback.fileUploadFailure(fileCollection);
    }

    interface BackgroundUploadOutcomeCallback {
        void fileUploadSuccess(final FileCollection fileCollection);
        void fileUploadFailure(final FileCollection fileCollection);

    }
}
