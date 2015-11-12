/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.dicom.SeriesZipper;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.UploadParameters;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ZipSeriesAppendUploader extends CallableUploader {

    public ZipSeriesAppendUploader(final UploadParameters uploadParameters,
                                   final GiftCloudServer server) {
        super(uploadParameters, server);
    }

    public Set<String> call() throws Exception {
        final SeriesZipper seriesZipper = new SeriesZipper(uploadParameters.getProjectApplicators(), server.getPixelDataAnonymiser());
        final File temporaryZipFile = seriesZipper.buildSeriesZipFile(fileCollection);
        try {
            server.appendZipFileToExistingScan(uploadParameters.getProjectName(), uploadParameters.getSubjectLabel(), uploadParameters.getExperimentLabel(), uploadParameters.getScanLabel(), uploadParameters.getXnatModalityParams(), temporaryZipFile);
        } finally {
            temporaryZipFile.delete();
        }
        return new HashSet<String>();
    }


    public static class ZipSeriesAppendUploaderFactory implements CallableUploaderFactory {
        public CallableUploader create(
                final UploadParameters uploadParameters,
                final GiftCloudServer server) {
            return new ZipSeriesAppendUploader(uploadParameters, server);
        }
    }
}
