package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

/**
 * Controls the creation of the Query-retrieve dialog
 */
class QueryRetrieveDialogController {

    private final GiftCloudUploaderAppConfiguration appConfiguration;
    private final MainFrame mainFrame;
    private final UploaderGuiController controller;
    private QueryRetrieveDialog remoteQueryRetrieveDialog = null;

    /**
     * Create new controller for query-retrieve dialog
     * @param appConfiguration
     * @param mainFrame
     * @param controller
     * @param reporter
     */
    QueryRetrieveDialogController(final GiftCloudUploaderAppConfiguration appConfiguration, final MainFrame mainFrame, final UploaderGuiController controller, final GiftCloudReporterFromApplication reporter) {
        this.appConfiguration = appConfiguration;
        this.mainFrame = mainFrame;
        this.controller = controller;
    }

    /**
     * Shows dialog if it is not already visible
     */
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

    Optional<QueryRetrieveRemoteView> getQueryRetrieveRemoteView() {
        if (remoteQueryRetrieveDialog == null) {
            return Optional.empty();
        } else {
            return Optional.of(remoteQueryRetrieveDialog.getQueryRetrieveRemoteView());
        }
    }
}
