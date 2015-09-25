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
import uk.ac.ucl.cs.cmic.giftcloud.dicom.SeriesZipper;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.UploadParameters;

import java.io.File;
import java.util.Set;

public class ZipSeriesUploader extends CallableUploader {

    private ZipSeriesUploader(
            final String projectLabel,
            final GiftCloudLabel.SubjectLabel subjectLabel,
            final UploadParameters uploadParameters,
            final XnatModalityParams xnatModalityParams,
            final FileCollection fileCollection,
            final Iterable<ScriptApplicator> applicators,
            final GiftCloudServer server) {
        super(projectLabel, subjectLabel, uploadParameters, xnatModalityParams, fileCollection, applicators, server);
    }

    @Override
    public Set<String> call() throws Exception {
        final SeriesZipper seriesZipper = new SeriesZipper(applicators);
        final File temporaryZipFile = seriesZipper.buildSeriesZipFile(fileCollection);
        return server.uploadZipFile(uploadParameters.getProjectName(), uploadParameters.getSubjectLabel(), uploadParameters.getExperimentLabel(), uploadParameters.getScanLabel(), temporaryZipFile);
    }

    public static class ZipSeriesUploaderFactory implements CallableUploaderFactory {
        public CallableUploader create(
                final String projectLabel,
                final GiftCloudLabel.SubjectLabel subjectLabel,
                final UploadParameters uploadParameters,
                final XnatModalityParams xnatModalityParams,
                final FileCollection fileCollection,
                final Iterable<ScriptApplicator> applicators,
                final GiftCloudServer server) {
            return new ZipSeriesUploader(projectLabel, subjectLabel, uploadParameters, xnatModalityParams, fileCollection, applicators, server);
        }
    }
}
