package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudUploaderRestServerFactory;

import javax.swing.*;

public class GiftCloudUploaderApp {

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
            new GiftCloudUploaderMain(mainFrame, new GiftCloudUploaderRestServerFactory(), new PropertyStoreFromApplication(GiftCloudMainFrame.propertiesFileName, reporter), dialogs, reporter);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
