package uk.ac.ucl.cs.cmic.giftcloud;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudUploaderRestServerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudUploaderMain;

public class GiftCloudTestUploaderApp {

	/**
	 * <p>The method to invoke the application.</p>
	 *
	 * @param	arg	none
	 */
	public static void main(String arg[]) {
		try {
            new GiftCloudUploaderMain(new GiftCloudUploaderRestServerFactory());
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
