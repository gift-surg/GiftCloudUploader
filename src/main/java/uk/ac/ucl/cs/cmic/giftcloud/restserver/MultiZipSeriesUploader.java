package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import netscape.javascript.JSObject;
import org.netbeans.spi.wizard.ResultProgressHandle;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.data.UploadAbortedException;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

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
    private final MultiUploadReporter logger;

    public MultiZipSeriesUploader(final List<FileCollection> uploads, final XnatModalityParams xnatModalityParams, final Iterable<ScriptApplicator> applicators, final String projectLabel, final String subjectLabel, final SessionParameters sessionParameters, final ResultProgressHandle progress, final Optional<String> windowName, final Optional<JSObject> jsContext, final MultiUploadReporter logger, final RestServerHelper restServerHelper, final CallableUploader.CallableUploaderFactory callableUploaderFactory) {
        this.logger = logger;
        int fileCount = getFileCountFromFileCollection(uploads);
        progress.setProgress(0, fileCount);
        progress.setBusy("Building sessionLabel manifest");

        final boolean useFixedSize = sessionParameters.getUseFixedSize();
        final int nThreads = sessionParameters.getNumberOfThreads();

        progress.setBusy("Preparing upload...");
        logger.trace("creating thread pool and executors");
        executor = Executors.newFixedThreadPool(nThreads);
        completionService = new ExecutorCompletionService<Set<String>>(executor);
        uploaders = Maps.newHashMap();
        logger.trace("submitting uploaders for {}", uploads);
        final UploadStatisticsReporter stats = new UploadStatisticsReporter(progress);
        for (final FileCollection s : uploads) {
            stats.addToSend(s.getSize());
            final CallableUploader uploader = callableUploaderFactory.create(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSize, s, applicators, stats, restServerHelper);
            uploaders.put(completionService.submit(uploader), uploader);
        }


    }

    public Map<FileCollection, Throwable> getFailures() {
        return failures;
    }

    public Set<String> getUris() {
        return uris;
    }

    public boolean run(final ResultProgressHandle progress, final MultiUploadReporter logger) {

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
                return false;
            }
        }

        if (!uploaders.isEmpty()) {
            logger.error("progress failed before uploaders complete: {}");
            return false;
        }

        return true;
    }

    private static int getFileCountFromFileCollection(final List<FileCollection> fileCollections) {
        int count = 0;
        for (final FileCollection fileCollection : fileCollections) {
            count += fileCollection.getFileCount();
        }
        return count;
    }
}
