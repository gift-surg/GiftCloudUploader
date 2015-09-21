package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.MockRestServerFactory;

public class GiftCloudIntegrationTestApp {

	/**
	 * <p>The method to invoke the application.</p>
	 *
	 * @param	arg	none
	 */
	public static void main(String arg[]) {
		try {
            new GiftCloudUploaderMain(new MockRestServerFactory());
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
