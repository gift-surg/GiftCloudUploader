/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;

import java.io.File;
import java.util.Set;

public class ZipSeriesUploader extends CallableUploader {

    private ZipSeriesUploader(
            final String projectLabel,
            final GiftCloudLabel.SubjectLabel subjectLabel,
            final SessionParameters sessionParameters,
            final XnatModalityParams xnatModalityParams,
            final boolean useFixedSizeStreaming,
            final FileCollection fileCollection,
            final Iterable<ScriptApplicator> applicators,
            final GiftCloudServer server) {
        super(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSizeStreaming, fileCollection, applicators, server);
    }

    @Override
    public Set<String> call() throws Exception {
        final SeriesZipper seriesZipper = new SeriesZipper(applicators);
        final File temporaryZipFile = seriesZipper.buildSeriesZipFile(fileCollection);
        return server.uploadZipFile(projectLabel, subjectLabel, sessionParameters, temporaryZipFile);
    }

    public static class ZipSeriesUploaderFactory implements CallableUploaderFactory {
        public CallableUploader create(
                final String projectLabel,
                final GiftCloudLabel.SubjectLabel subjectLabel,
                final SessionParameters sessionParameters,
                final XnatModalityParams xnatModalityParams,
                final boolean useFixedSizeStreaming,
                final FileCollection fileCollection,
                final Iterable<ScriptApplicator> applicators,
                final GiftCloudServer server) {
            return new ZipSeriesUploader(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSizeStreaming, fileCollection, applicators, server);
        }
    }
}
