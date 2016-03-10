/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import uk.ac.ucl.cs.cmic.giftcloud.dicom.DicomMetaDataAnonymiser;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.SeriesZipper;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.UploadParameters;

import java.io.File;
import java.util.Set;

public class ZipSeriesUploader extends CallableUploader {

    public ZipSeriesUploader(final UploadParameters uploadParameters, final GiftCloudServer server, final DicomMetaDataAnonymiser dicomMetaDataAnonymiser, final boolean append) {
        super(uploadParameters, server, dicomMetaDataAnonymiser, append);
    }

    @Override
    public Set<String> call() throws Exception {
        final SeriesZipper seriesZipper = new SeriesZipper(dicomMetaDataAnonymiser, server.getPixelDataAnonymiser(), uploadParameters);
        final File temporaryZipFile = seriesZipper.buildSeriesZipFile(fileCollection);
        try {
            return server.uploadZipFile(uploadParameters.getProjectName(), uploadParameters.getSubjectLabel(), uploadParameters.getExperimentLabel(), uploadParameters.getScanLabel(), uploadParameters.getXnatModalityParams(), temporaryZipFile, false);
        } finally {
            temporaryZipFile.delete();
        }
    }
}
