package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.dicom.ZipSeriesAppendUploader;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.ZipSeriesUploader;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.CallableUploader;

public class ZipSeriesUploaderFactorySelector {
    public static CallableUploader.CallableUploaderFactory getZipSeriesUploaderFactory(final boolean append) {
        if (append) {
            return new ZipSeriesAppendUploader.ZipSeriesAppendUploaderFactory();
        } else {
            return new ZipSeriesUploader.ZipSeriesUploaderFactory();
        }
    }
}
