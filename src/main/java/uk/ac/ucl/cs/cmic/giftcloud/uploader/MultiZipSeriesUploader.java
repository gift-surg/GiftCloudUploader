package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.data.UploadAbortedException;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.ZipSeriesAppendUploader;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.ZipSeriesUploader;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableUploader;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SessionParameters;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UploadStatisticsReporter;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.XnatModalityParams;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.ProgressHandleWrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

public class MultiZipSeriesUploader {

    private final Map<FileCollection, Throwable> failures = Maps.newLinkedHashMap();
    private final Set<String> uris = Sets.newLinkedHashSet();

    private final ExecutorService executor;
    private final CompletionService<Set<String>> completionService;
    private final Map<Future<Set<String>>, CallableUploader> uploaders;
    private final GiftCloudReporter reporter;

    public MultiZipSeriesUploader(final boolean append, final List<FileCollection> uploads, final XnatModalityParams xnatModalityParams, final Iterable<ScriptApplicator> applicators, final String projectLabel, final String subjectLabel, final SessionParameters sessionParameters, final GiftCloudReporter reporter, final GiftCloudServer server) {
        this.reporter = reporter;

        final CallableUploader.CallableUploaderFactory callableUploaderFactory = getZipSeriesUploaderFactory(append);

        int fileCount = getFileCountFromFileCollection(uploads);
        reporter.startProgressBar(fileCount);
        reporter.updateStatusText("Building sessionLabel manifest");

        final boolean useFixedSize = sessionParameters.getUseFixedSize();
        final int nThreads = sessionParameters.getNumberOfThreads();

        reporter.updateStatusText("Preparing upload...");
        reporter.trace("creating thread pool and executors");
        executor = Executors.newFixedThreadPool(nThreads);
        completionService = new ExecutorCompletionService<Set<String>>(executor);
        uploaders = Maps.newHashMap();
        reporter.trace("submitting uploaders for {}", uploads);
        final UploadStatisticsReporter stats = new UploadStatisticsReporter(reporter);
        for (final FileCollection s : uploads) {
            stats.addToSend(s.getSize());
            final CallableUploader uploader = callableUploaderFactory.create(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSize, s, applicators, stats, server);
            uploaders.put(completionService.submit(uploader), uploader);
        }


    }

    private static CallableUploader.CallableUploaderFactory getZipSeriesUploaderFactory(final boolean append) {
        if (append) {
            return new ZipSeriesAppendUploader.ZipSeriesAppendUploaderFactory();
        } else {
            return new ZipSeriesUploader.ZipSeriesUploaderFactory();
        }
    }

    public Map<FileCollection, Throwable> getFailures() {
        return failures;
    }

    public Set<String> getUris() {
        return uris;
    }

    public Optional<String> run(final GiftCloudReporter logger) {

        final ProgressHandleWrapper progress = new ProgressHandleWrapper(reporter);
        while (progress.isRunning() && !uploaders.isEmpty()) {
            final Future<Set<String>> future;
            try {
                future = completionService.take();
                logger.trace("retrieved completed future {}", future);
            } catch (InterruptedException e) {
                logger.debug("upload completion poll interrupted", e);
                continue;
            }

            try {
                final Set<String> us = future.get();
                logger.debug("{} completed -> {}", uploaders.get(future), us);
                uris.addAll(us);
                uploaders.remove(future);
            } catch (InterruptedException e) {
                logger.info("upload interrupted or timed out, retrying");
                completionService.submit(uploaders.get(future));
            } catch (ExecutionException exception) {
                executor.shutdownNow();
                final Throwable cause = exception.getCause();
                //noinspection ThrowableResultOfMethodCallIgnored
                failures.put(uploaders.remove(future).getFileCollection(), cause);
                future.cancel(true);
                final UploadAbortedException aborted;
                if (cause instanceof UploadAbortedException) {
                    aborted = (UploadAbortedException) cause;
                } else {
                    aborted = new UploadAbortedException(cause);
                }
                logger.info("upload aborted: shutting down executor", cause);

                for (final Map.Entry<Future<Set<String>>, CallableUploader> me : uploaders.entrySet()) {
                    me.getKey().cancel(true);
                    //noinspection ThrowableResultOfMethodCallIgnored
                    failures.put(me.getValue().getFileCollection(), aborted);
                }
                String message = MultiUploaderUtils.buildFailureMessage(failures);
                progress.failed(message, false);
                return Optional.of(message);
            }
        }

        if (!uploaders.isEmpty()) {
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
