package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableUploader;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SessionParameters;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UploadStatisticsReporter;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.XnatModalityParams;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.ProgressHandleWrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MultiZipSeriesUploader {

    private final Map<FileCollection, Throwable> failures = Maps.newLinkedHashMap();
    private final Set<String> uris = Sets.newLinkedHashSet();

    private final BackgroundCompletionServiceTaskList<Set<String>, FileCollection> backgroundCompletionServiceTaskList;
    private final boolean useFixedSize = true;
    private boolean append;
    private final GiftCloudReporter reporter;

    public MultiZipSeriesUploader(final BackgroundCompletionServiceTaskList backgroundCompletionServiceTaskList, final boolean append, final List<FileCollection> uploads, final GiftCloudReporter reporter) {
        this.backgroundCompletionServiceTaskList = backgroundCompletionServiceTaskList;
        this.append = append;
        this.reporter = reporter;
    }

    public void addFile(final GiftCloudServer server, XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, CallableUploader.CallableUploaderFactory callableUploaderFactory, UploadStatisticsReporter stats, FileCollection fileCollection) {
        final CallableUploader uploader = callableUploaderFactory.create(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSize, fileCollection, applicators, stats, server);
        backgroundCompletionServiceTaskList.addNewTask(uploader);
    }

    public Map<FileCollection, Throwable> getFailures() {
        return failures;
    }

    public Set<String> getUris() {
        return uris;
    }

    public Optional<String> run(final GiftCloudReporter logger) {

        final ProgressHandleWrapper progress = new ProgressHandleWrapper(reporter);
        progress.setBusy("Uploading");
        while (progress.isRunning() && !backgroundCompletionServiceTaskList.isEmpty()) {
            final Future<Set<String>> future;
            final BackgroundServiceTaskWrapper<CallableWithParameter<Set<String>, FileCollection>, Future<Set<String>>> taskWrapper;
            try {
                taskWrapper = backgroundCompletionServiceTaskList.take();
                future = taskWrapper.getResult();
            } catch (InterruptedException e) {
                continue;
            }

            try {
                processItem(future);
            } catch (InterruptedException e) {
                backgroundCompletionServiceTaskList.add(taskWrapper.getTask(), taskWrapper.getErrorRecord());
            } catch (ExecutionException exception) {
                final Throwable cause = exception.getCause();
                failures.put(taskWrapper.getTask().getParameter(), cause);
                future.cancel(true);
                backgroundCompletionServiceTaskList.cancelAllAndShutdown();

                String message = MultiUploaderUtils.buildFailureMessage(failures);
                progress.failed(message, false);
                return Optional.of(message);
            } catch (Exception e) {
                // ToDo
            }
        }

        if (!backgroundCompletionServiceTaskList.isEmpty()) {
            logger.error("progress failed before uploaders complete: {}");
            return Optional.of("progress failed before uploaders complete: {}");
        }

        return Optional.empty();
    }

    protected void processItem(final Future<Set<String>> futureResult) throws Exception {
        Set<String> us = futureResult.get();
        if (us != null) {
            uris.addAll(us);
        }

    }
}
