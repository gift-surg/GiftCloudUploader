package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.collect.Maps;
import org.netbeans.spi.wizard.ResultProgressHandle;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.data.UploadFailureHandler;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CancellationException;

public class EcatUploader {

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    private final Queue<File> fileStack = new LinkedList<File>();
    private RestServerHelper restServerHelper;
    private final String projectLabel;
    private final String subjectLabel;
    private final SessionParameters sessionParameters;
    private final ResultProgressHandle progress;
    private final UploadFailureHandler failureHandler;
    private final TimeZone timeZone;
    private final MultiUploadReporter logger;
    final String timestamp;

    final Map<File, Object> failures = Maps.newLinkedHashMap();


    public EcatUploader(final RestServerHelper restServerHelper, final FileCollection fileCollection, final String projectLabel, final String subjectLabel, final SessionParameters sessionParameters, final ResultProgressHandle progress, final UploadFailureHandler failureHandler, final TimeZone timeZone, final MultiUploadReporter logger) {
        this.restServerHelper = restServerHelper;
        this.projectLabel = projectLabel;
        this.subjectLabel = subjectLabel;
        this.sessionParameters = sessionParameters;
        this.progress = progress;
        this.failureHandler = failureHandler;
        this.timeZone = timeZone;
        this.logger = logger;

        for (final File f : fileCollection.getFiles()) {
            fileStack.add(f);
        }

        timestamp = makeTimestamp();

    }

    public final Map<File, Object> getFailures() {
        return failures;
    }

    public boolean run() {
        // add scans to session, and the data file to each scan
        int i = 0;
        final int size = fileStack.size();

        while (!fileStack.isEmpty()) {
            File nextFile = fileStack.remove();

            try {
                progress.setProgress(String.format("Uploading scan %d/%d", i, size), i - 1, size);
                restServerHelper.uploadEcat(projectLabel, subjectLabel, sessionParameters, timestamp, timeZone.getID(), progress, nextFile, i);
                break;

            } catch (CancellationException exception) {
                // Cancellation should terminate the whole process
                throw exception;

            } catch (Throwable t) {
                if (failureHandler.shouldRetry(nextFile, t)) {
                    logger.info("upload failed, retrying", t);
                } else {
                    failures.put(nextFile, t);
                    final StringBuilder message = new StringBuilder("user canceled operation after errors:");
                    message.append(MultiUploaderUtils.LINE_SEPARATOR);
                    MultiUploaderUtils.buildEcatFailureMessage(message, failures);
                    progress.failed(message.toString(), true);

                    // Add all other files to the failure list
                    while (!fileStack.isEmpty()) {
                        failures.put(fileStack.remove(), getUserCanceledFailure());
                    }
                    return false;
                }
            }
        }

        return true;
    }

    public String getUri() {
        final String sessionLabel = sessionParameters.getSessionLabel();
        return String.format("/data/prearchive/projects/%s/%s/%s", projectLabel, timestamp, sessionLabel);
    }

    private static String makeTimestamp() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    }

    private static Object getUserCanceledFailure() {
        return "User canceled upload";
    }

}
