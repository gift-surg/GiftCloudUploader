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
import uk.ac.ucl.cs.cmic.giftcloud.dicom.SeriesZipper;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.UploadParameters;

import java.io.File;
import java.util.Set;

public class ZipSeriesUploader extends CallableUploader {

    private ZipSeriesUploader(
            final UploadParameters uploadParameters,
            final GiftCloudServer server) {
        super(uploadParameters, server);
    }

    @Override
    public Set<String> call() throws Exception {
        final SeriesZipper seriesZipper = new SeriesZipper(uploadParameters.getProjectApplicators());
        final File temporaryZipFile = seriesZipper.buildSeriesZipFile(fileCollection);
        return server.uploadZipFile(uploadParameters.getProjectName(), uploadParameters.getSubjectLabel(), uploadParameters.getExperimentLabel(), uploadParameters.getScanLabel(), temporaryZipFile);
    }

    public static class ZipSeriesUploaderFactory implements CallableUploaderFactory {
        public CallableUploader create(
                final UploadParameters uploadParameters,
                final GiftCloudServer server) {
            return new ZipSeriesUploader(uploadParameters, server);
        }
    }
}
