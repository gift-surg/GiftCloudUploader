package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudUploaderRestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GiftCloudUploaderApp {

	public GiftCloudUploaderApp(final RestServerFactory restServerFactory, List<File> fileList) {
        try {
            final GiftCloudUploaderAppConfiguration application = new GiftCloudUploaderAppConfiguration();

            final GiftCloudMainFrame mainFrame = new GiftCloudMainFrame(application);
            application.registerMainFrame(mainFrame);
            final GiftCloudDialogs dialogs = new GiftCloudDialogs(application, mainFrame);
            final GiftCloudReporterFromApplication reporter = new GiftCloudReporterFromApplication(application.getLogger(), mainFrame.getContainer(), dialogs);

            UploaderGuiController uploaderMain = new UploaderGuiController(application, mainFrame, restServerFactory, dialogs, reporter);
            uploaderMain.start(false, fileList);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

	/**
	 * <p>The method to invoke the application.</p>
	 *
	 * @param	arg	none
	 */
	public static void main(String arg[]) {
        final List<File> fileList = new ArrayList<File>();
        if (arg.length==2) {
            fileList.add(new File(arg[1]));
        }

        new GiftCloudUploaderApp(new GiftCloudUploaderRestServerFactory(), fileList);
	}
}
