package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;

/**
 * Controls the creation of the Query-retrieve dialog
 */
class QueryRetrieveDialogController {

    private QueryRetrieveDialog remoteQueryRetrieveDialog;
    private GiftCloudUploaderAppConfiguration appConfiguration;
    private MainFrame mainFrame;
    private UploaderGuiController controller;

    QueryRetrieveDialogController(final GiftCloudUploaderAppConfiguration appConfiguration, final MainFrame mainFrame, final UploaderGuiController controller, final GiftCloudReporterFromApplication reporter) {
        this.appConfiguration = appConfiguration;
        this.mainFrame = mainFrame;
        this.controller = controller;
        remoteQueryRetrieveDialog = new QueryRetrieveDialog(mainFrame.getParent(), controller, appConfiguration.getResourceBundle());
    }

    void showQueryRetrieveDialog() {
        if (remoteQueryRetrieveDialog == null || !remoteQueryRetrieveDialog.isVisible()) {
            GiftCloudUtils.runLaterOnEdt(new Runnable() {
                @Override
                public void run() {
                    remoteQueryRetrieveDialog = new QueryRetrieveDialog(mainFrame.getParent(), controller, appConfiguration.getResourceBundle());
                    remoteQueryRetrieveDialog.setVisible(true);
                }
            });
        }
    }

    QueryRetrieveRemoteView getQueryRetrieveRemoteView() {
        return remoteQueryRetrieveDialog.getQueryRetrieveRemoteView();
    }
}
