package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.DicomException;

import javax.swing.*;
import java.io.IOException;
import java.util.ResourceBundle;

public class GiftCloudUploaderMain {

	protected static String propertiesFileName  = ".com.pixelmed.display.GiftCloudUploader.properties";
	protected ResourceBundle resourceBundle;

    public GiftCloudUploaderMain(ResourceBundle resourceBundle) throws DicomException, IOException {
        this.resourceBundle = resourceBundle;

        final GiftCloudUploaderApplicationBase applicationBase = new GiftCloudUploaderApplicationBase(propertiesFileName);

        GiftCloudMainFrame giftCloudMainFrame = new GiftCloudMainFrame(resourceBundle.getString("applicationTitle"));
        final GiftCloudDialogs giftCloudDialogs = new GiftCloudDialogs(giftCloudMainFrame);

        final String buildDate = applicationBase.getBuildDateFromApplicationBase();
        JLabel statusBar = applicationBase.getStatusBarFromApplicationBase();

        final GiftCloudPropertiesFromBridge giftCloudProperties = new GiftCloudPropertiesFromBridge(applicationBase);

        final GiftCloudUploaderPanel giftCloudUploaderPanel = new GiftCloudUploaderPanel(giftCloudProperties, resourceBundle, giftCloudDialogs, giftCloudMainFrame, buildDate, statusBar);

        giftCloudMainFrame.addMainPanel(giftCloudUploaderPanel);
        giftCloudMainFrame.show();

        new GiftCloudSystemTray(giftCloudMainFrame, giftCloudDialogs, resourceBundle.getString("giftCloudProductName"));
    }

}
