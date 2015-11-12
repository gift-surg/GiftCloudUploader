package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudUploaderRestServerFactory;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

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
			GiftCloudUploaderMain uploaderMain = new GiftCloudUploaderMain(mainFrame, new GiftCloudUploaderRestServerFactory(), new PropertyStoreFromApplication(GiftCloudMainFrame.propertiesFileName, reporter), dialogs, reporter);
			uploaderMain.start(false, new ArrayList<File>());
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
