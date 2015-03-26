/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.collect.ImmutableList;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;


public abstract class CallableUploader implements Callable<Set<String>> {
    protected String projectLabel;
    protected String subjectLabel;
    protected final SessionParameters sessionParameters;
    protected final XnatModalityParams xnatModalityParams;
    protected final FileCollection fileCollection;
    protected final UploadStatisticsReporter progress;
    protected final boolean useFixedSizeStreaming;
    protected final RestServerHelper restServerHelper;
    protected final Iterable<ScriptApplicator> applicators;

    public static int MAX_TAG = Collections.max(ImmutableList.of(Tag.SOPInstanceUID,
            Tag.TransferSyntaxUID, Tag.FileMetaInformationVersion, Tag.SOPClassUID));

    public CallableUploader(
            final String projectLabel,
            final String subjectLabel,
            final SessionParameters sessionParameters,
            final XnatModalityParams xnatModalityParams,
            final boolean useFixedSizeStreaming,
            final FileCollection fileCollection,
            final Iterable<ScriptApplicator> applicators,
            final UploadStatisticsReporter progress,
            final RestServerHelper restServerHelper) {
        this.projectLabel = projectLabel;
        this.subjectLabel = subjectLabel;
        this.sessionParameters = sessionParameters;
        this.xnatModalityParams = xnatModalityParams;
        this.useFixedSizeStreaming = useFixedSizeStreaming;
        this.fileCollection = fileCollection;
        this.progress = progress;
        this.restServerHelper = restServerHelper;
        this.applicators = applicators;
    }


    public final FileCollection getFileCollection() {
        return fileCollection;
    }

    public static interface CallableUploaderFactory {
        public CallableUploader create(
                final String projectLabel,
                final String subjectLabel,
                final SessionParameters sessionParameters,
                final XnatModalityParams xnatModalityParams,
                final boolean useFixedSizeStreaming,
                final FileCollection fileCollection,
                final Iterable<ScriptApplicator> applicators,
                final UploadStatisticsReporter progress,
                final RestServerHelper restServerHelper);
    }
}
