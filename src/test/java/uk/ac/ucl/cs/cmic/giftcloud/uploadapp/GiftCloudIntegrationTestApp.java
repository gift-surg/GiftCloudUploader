package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.MockRestServerFactory;

import javax.swing.*;

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
			final GiftCloudMainFrame mainFrame = new GiftCloudMainFrame(new JFrame());
			final GiftCloudDialogs dialogs = new GiftCloudDialogs(mainFrame);
			final GiftCloudReporterFromApplication reporter = new GiftCloudReporterFromApplication(mainFrame.getContainer(), dialogs);
            GiftCloudUploaderMain uploaderMain = new GiftCloudUploaderMain(mainFrame, new MockRestServerFactory(), new PropertyStoreFromApplication(GiftCloudMainFrame.propertiesFileName, reporter), dialogs, reporter);
            uploaderMain.start(false);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
