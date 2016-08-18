package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.PixelDataAnonymiserFilterCache;

/**
 * Factory/controller class for the dialog that allows creation of pixel data redaction templates
 */
class PixelDataTemplateDialogController {
    private PixelDataTemplateDialog pixelDataDialog = null;
    private GiftCloudUploaderAppConfiguration appConfiguration;
    private PixelDataAnonymiserFilterCache pixelDataAnonymiserFilterCache;
    private final MainFrame mainFrame;
    private GiftCloudDialogs dialogs;
    private GiftCloudReporterFromApplication reporter;

    /**
     * Creates a new controller class
     *
     * @param appConfiguration
     * @param pixelDataAnonymiserFilterCache
     * @param mainFrame
     * @param dialogs
     * @param reporter
     */
    PixelDataTemplateDialogController(final GiftCloudUploaderAppConfiguration appConfiguration, PixelDataAnonymiserFilterCache pixelDataAnonymiserFilterCache, final MainFrame mainFrame, final GiftCloudDialogs dialogs, final GiftCloudReporterFromApplication reporter) {
        this.appConfiguration = appConfiguration;
        this.pixelDataAnonymiserFilterCache = pixelDataAnonymiserFilterCache;
        this.mainFrame = mainFrame;
        this.dialogs = dialogs;
        this.reporter = reporter;
    }


    /**
     * Lazy creation for showing the pixel data template dialog
     */
    void showPixelDataTemplateDialog() {
        if (pixelDataDialog == null || !pixelDataDialog.isVisible()) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    pixelDataDialog = new PixelDataTemplateDialog(mainFrame.getContainer(), appConfiguration.getResourceBundle().getString("pixelDataDialogTitle"), pixelDataAnonymiserFilterCache, appConfiguration.getProperties(), dialogs, reporter);
                }
            });
        }
    }
}
