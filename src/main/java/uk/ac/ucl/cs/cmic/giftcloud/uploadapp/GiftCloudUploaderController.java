package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import java.io.IOException;

public interface GiftCloudUploaderController {
    void showConfigureDialog() throws IOException, DicomNode.DicomNodeStartException;
    void showAboutDialog();

    void hide();

    void show();
}
