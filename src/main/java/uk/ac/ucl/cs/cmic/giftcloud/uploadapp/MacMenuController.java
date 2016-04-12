package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;
public class MacMenuController
        implements MRJAboutHandler, MRJQuitHandler, MRJPrefsHandler
{

    private final GiftCloudUploaderController controller;

    public MacMenuController(final GiftCloudUploaderController controller) {

        this.controller = controller;
    }

    public void handleAbout()
    {
        controller.showAboutDialog();
    }

    public void handlePrefs() throws IllegalStateException
    {
        controller.showConfigureDialog(false);
    }

    public void handleQuit() throws IllegalStateException
    {
        System.exit(0);
    }
}