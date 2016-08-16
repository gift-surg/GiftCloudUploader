package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.MockRestServerFactory;

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
		new GiftCloudUploaderApp(new MockRestServerFactory(), arg);
	}
}
