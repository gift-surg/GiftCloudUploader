package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.display.ApplicationBase;

import java.io.IOException;
import java.util.Properties;

public class GiftCloudUploaderApplicationBase extends ApplicationBase{

    /**
     * <p>Construct a window with the default size, and specified title and property sources.</p>
     * <p/>
     * <p>Does not show the window.</p>
     * <p/>
     * <p>Will exit the application when the window closes.</p>
     *
     * @param    applicationPropertyFileName    the name of the properties file
     */
    public GiftCloudUploaderApplicationBase(String applicationPropertyFileName) {
        super(applicationPropertyFileName);
    }

    protected Properties getPropertiesFromApplicationBase() {
        return getProperties();
    }

    protected void storePropertiesToApplicationBase(String comment) throws IOException {
        storeProperties(comment);
    }

}
