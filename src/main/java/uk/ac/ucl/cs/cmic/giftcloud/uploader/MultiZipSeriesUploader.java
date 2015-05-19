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

    private final BackgroundCompletionServiceTaskList<Set<String>, FileCollection> uploaderTaskList;
    private final boolean useFixedSize = true;
    private boolean append;
    private final GiftCloudReporter reporter;

    public MultiZipSeriesUploader(final boolean append, final List<FileCollection> uploads, final XnatModalityParams xnatModalityParams, final Iterable<ScriptApplicator> applicators, final String projectLabel, final String subjectLabel, final SessionParameters sessionParameters, final GiftCloudReporter reporter, final GiftCloudServer server) {
        this.append = append;
        this.reporter = reporter;


        int fileCount = getFileCountFromFileCollection(uploads);
        reporter.startProgressBar(fileCount);
        reporter.updateStatusText("Building sessionLabel manifest");

        final int nThreads = sessionParameters.getNumberOfThreads();
        uploaderTaskList = new BackgroundCompletionServiceTaskList<Set<String>, FileCollection>(nThreads);

        reporter.updateStatusText("Preparing upload...");
        reporter.trace("creating thread pool and executors");
        reporter.trace("submitting uploaders for {}", uploads);
    }

    public void addFile(final GiftCloudServer server, XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, CallableUploader.CallableUploaderFactory callableUploaderFactory, UploadStatisticsReporter stats, FileCollection fileCollection) {
        final CallableUploader uploader = callableUploaderFactory.create(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSize, fileCollection, applicators, stats, server);
        uploaderTaskList.addNewTask(uploader);
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
        while (progress.isRunning() && !uploaderTaskList.isEmpty()) {
            final Future<Set<String>> future;
            final BackgroundServiceTaskWrapper<CallableWithParameter<Set<String>, FileCollection>, Future<Set<String>>> taskWrapper;
            try {
                taskWrapper = uploaderTaskList.take();
                future = taskWrapper.getResult();
            } catch (InterruptedException e) {
                continue;
            }

            try {
                future.get();
                Set<String> us = future.get();
                if (us != null) {
                    uris.addAll(us);
                }
            } catch (InterruptedException e) {
                uploaderTaskList.add(taskWrapper.getTask(), taskWrapper.getErrorRecord());
            } catch (ExecutionException exception) {
                final Throwable cause = exception.getCause();
                failures.put(taskWrapper.getTask().getParameter(), cause);
                future.cancel(true);
                uploaderTaskList.cancelAllAndShutdown();

                String message = MultiUploaderUtils.buildFailureMessage(failures);
                progress.failed(message, false);
                return Optional.of(message);
            }
        }

        if (!uploaderTaskList.isEmpty()) {
            logger.error("progress failed before uploaders complete: {}");
            return Optional.of("progress failed before uploaders complete: {}");
        }

        return Optional.empty();
    }

    private static int getFileCountFromFileCollection(final List<FileCollection> fileCollections) {
        int count = 0;
        for (final FileCollection fileCollection : fileCollections) {
            count += fileCollection.getFileCount();
        }
        return count;
    }
}
