/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableUploader;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SessionParameters;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.UploadStatisticsReporter;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.XnatModalityParams;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServer;

import java.util.Set;

public class ZipSeriesUploader extends CallableUploader {

    private ZipSeriesUploader(
            final String projectLabel,
            final String subjectLabel,
            final SessionParameters sessionParameters,
            final XnatModalityParams xnatModalityParams,
            final boolean useFixedSizeStreaming,
            final FileCollection fileCollection,
            final Iterable<ScriptApplicator> applicators,
            final UploadStatisticsReporter progress,
            final GiftCloudServer server) {
        super(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSizeStreaming, fileCollection, applicators, progress, server);
    }

    @Override
    public Set<String> call() throws Exception {
        return server.getRestServerHelper().uploadZipFile(projectLabel, subjectLabel, sessionParameters, useFixedSizeStreaming, fileCollection, applicators, progress);
    }

    public static class ZipSeriesUploaderFactory implements CallableUploaderFactory {
        public CallableUploader create(
                final String projectLabel,
                final String subjectLabel,
                final SessionParameters sessionParameters,
                final XnatModalityParams xnatModalityParams,
                final boolean useFixedSizeStreaming,
                final FileCollection fileCollection,
                final Iterable<ScriptApplicator> applicators,
                final UploadStatisticsReporter progress,
                final GiftCloudServer server) {
            return new ZipSeriesUploader(projectLabel, subjectLabel, sessionParameters, xnatModalityParams, useFixedSizeStreaming, fileCollection, applicators, progress, server);
        }
    }
}
