/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import com.google.common.collect.ImmutableList;
import org.dcm4che2.data.Tag;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.SeriesZipper;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.UploadParameters;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class ZipSeriesUploader extends CallableUploader {
    protected final UploadParameters uploadParameters;
    private SeriesZipper seriesZipper;
    protected boolean append;
    protected final FileCollection fileCollection;
    protected final GiftCloudServer server;

    public static int MAX_TAG = Collections.max(ImmutableList.of(Tag.SOPInstanceUID,
            Tag.TransferSyntaxUID, Tag.FileMetaInformationVersion, Tag.SOPClassUID));

    public ZipSeriesUploader(final UploadParameters uploadParameters, final GiftCloudServer server, final SeriesZipper seriesZipper, final boolean append) {
        this.uploadParameters = uploadParameters;
        this.seriesZipper = seriesZipper;
        this.append = append;
        this.fileCollection = uploadParameters.getFileCollection();
        this.server = server;
    }

    public final FileCollection getFileCollection() {
        return fileCollection;
    }

    public final FileCollection getParameter() {
        return fileCollection;
    }

    @Override
    public Set<String> call() throws Exception {
        final File temporaryZipFile = seriesZipper.buildSeriesZipFile(fileCollection);
        try {
            return server.uploadZipFile(uploadParameters.getProjectName(), uploadParameters.getSubjectLabel(), uploadParameters.getExperimentLabel(), uploadParameters.getScanLabel(), uploadParameters.getXnatModalityParams(), temporaryZipFile, append);
        } finally {
            temporaryZipFile.delete();
        }
    }
}
