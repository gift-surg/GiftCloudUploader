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
import uk.ac.ucl.cs.cmic.giftcloud.uploader.CallableWithParameter;

import java.util.Collections;
import java.util.Set;


public abstract class CallableUploader implements CallableWithParameter<Set<String>, FileCollection> {
    protected final String projectLabel;
    protected final GiftCloudLabel.SubjectLabel subjectLabel;
    protected final SessionParameters sessionParameters;
    protected final XnatModalityParams xnatModalityParams;
    protected final FileCollection fileCollection;
    protected final GiftCloudServer server;
    protected final Iterable<ScriptApplicator> applicators;

    public static int MAX_TAG = Collections.max(ImmutableList.of(Tag.SOPInstanceUID,
            Tag.TransferSyntaxUID, Tag.FileMetaInformationVersion, Tag.SOPClassUID));

    public CallableUploader(
            final String projectLabel,
            final GiftCloudLabel.SubjectLabel subjectLabel,
            final SessionParameters sessionParameters,
            final XnatModalityParams xnatModalityParams,
            final FileCollection fileCollection,
            final Iterable<ScriptApplicator> applicators,
            final GiftCloudServer server) {
        this.projectLabel = projectLabel;
        this.subjectLabel = subjectLabel;
        this.sessionParameters = sessionParameters;
        this.xnatModalityParams = xnatModalityParams;
        this.fileCollection = fileCollection;
        this.server = server;
        this.applicators = applicators;
    }


    public final FileCollection getFileCollection() {
        return fileCollection;
    }

    public final FileCollection getParameter() {
        return fileCollection;
    }


    public interface CallableUploaderFactory {
        CallableUploader create(
                final String projectLabel,
                final GiftCloudLabel.SubjectLabel subjectLabel,
                final SessionParameters sessionParameters,
                final XnatModalityParams xnatModalityParams,
                final FileCollection fileCollection,
                final Iterable<ScriptApplicator> applicators,
                final GiftCloudServer server);
    }
}
