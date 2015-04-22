package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.*;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class BackgroundUploader extends BackgroundService<CallableUploader, BackgroundUploadersInProgressList, BackgroundUploadersInProgressList.BackgroundUploaderResult> {

    private final RestServerHelper restServerHelper;
    private final ResultProgressHandle progress;
    private final boolean useFixedSize = true;
    final BackgroundUploadersInProgressList backgroundUploadersInProgressList;


    public BackgroundUploader(final BackgroundUploadersInProgressList backgroundUploadersInProgressList, final RestServerHelper restServerHelper, final ResultProgressHandle progress, final MultiUploadReporter reporter) {
        super(backgroundUploadersInProgressList, reporter);
        this.restServerHelper = restServerHelper;
        this.progress = progress;
        this.backgroundUploadersInProgressList = backgroundUploadersInProgressList;
    }


    private void addFiles(List<FileCollection> uploads, XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, CallableUploader.CallableUploaderFactory callableUploaderFactory, UploadStatisticsReporter stats) {
        for (final FileCollection fileCollection : uploads) {
            addFile(xnatModalityParams, applicators, projectLabel, subjectLabel, sessionParameters, callableUploaderFactory, stats, fileCollection);
        }
    }

    private void addFile(XnatModalityParams xnatModalityParams, Iterable<ScriptApplicator> applicators, String projectLabel, String subjectLabel, SessionParameters sessionParameters, CallableUploader.CallableUploaderFactory callableUploaderFactory, UploadStatisticsReporter stats, FileCollection fileCollection) {
        final CallableUploader uploader = callableUploaderFactory.create(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSize, fileCollection, applicators, stats, restServerHelper);
        backgroundUploadersInProgressList.add(uploader);
    }


    @Override
    protected void processItem(BackgroundUploadersInProgressList.BackgroundUploaderResult pendingUploadItem) throws Exception {
        final Future<Set<String>> future = pendingUploadItem.getFutureResult();
        final Set<String> result = future.get();
    }
}
