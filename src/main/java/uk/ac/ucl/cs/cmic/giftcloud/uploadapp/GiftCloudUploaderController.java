package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.DicomNetworkException;

import java.io.IOException;

public interface GiftCloudUploaderController {
    void showConfigureDialog() throws IOException, DicomNetworkException, DicomException;
}
