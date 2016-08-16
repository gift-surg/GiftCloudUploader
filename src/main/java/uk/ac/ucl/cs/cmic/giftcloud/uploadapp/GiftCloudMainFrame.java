package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import java.lang.reflect.InvocationTargetException;

class GiftCloudMainFrame  extends MainFrame {

    public static String propertiesFileName  = "GiftCloudUploader.properties";


    GiftCloudMainFrame(final GiftCloudUploaderAppConfiguration application) throws InvocationTargetException, InterruptedException {
        super(application);

    }

}