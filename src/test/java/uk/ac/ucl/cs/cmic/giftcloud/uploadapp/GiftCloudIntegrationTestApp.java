package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.MockRestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * GiftCloudIntegrationTestApp is a Java application that mimics GiftCloudUploaderApp but does not connect to the server. A fake server is used instead, allowing this application to be used for testing purposes
 */
public class GiftCloudIntegrationTestApp {

	/**
	 * <p>The method to invoke the application.</p>
	 *
	 * @param	arg	none
	 */
	public static void main(String arg[]) {
		try {
			/// Get up root folder for logging
			final File appRoot = GiftCloudUtils.createOrGetGiftCloudFolder(Optional.<GiftCloudReporter>empty());
			System.setProperty("app.root", appRoot.getAbsolutePath());

			final List<File> fileList = new ArrayList<File>();
			if(arg.length==2) {
				fileList.add(new File(arg[1]));
			}

			final GiftCloudMainFrame mainFrame = new GiftCloudMainFrame(new JFrame());
			final GiftCloudDialogs dialogs = new GiftCloudDialogs(mainFrame);
			final GiftCloudReporterFromApplication reporter = new GiftCloudReporterFromApplication(mainFrame.getContainer(), dialogs);
            UploaderGuiController uploaderMain = new UploaderGuiController(mainFrame, new MockRestServerFactory(), new PropertyStoreFromApplication(GiftCloudMainFrame.propertiesFileName, reporter), dialogs, reporter);
            uploaderMain.start(false, fileList);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
