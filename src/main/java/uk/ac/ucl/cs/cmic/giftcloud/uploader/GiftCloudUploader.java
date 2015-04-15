package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudProperties;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.MultiUploadReporter;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudDialogs;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.ProjectListModel;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.MultiUploadParameters;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.MultiUploadWizard;

import javax.security.sasl.AuthenticationException;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.CancellationException;

public class GiftCloudUploader {
    private final GiftCloudProperties giftCloudProperties;
    private final Container container;
    private final MultiUploadReporter reporter;
    private final ProjectListModel projectListModel;
    private final GiftCloudServerFactory serverFactory;

    public GiftCloudUploader(final GiftCloudProperties giftCloudProperties, final Container container, final MultiUploadReporter reporter) {
        this.giftCloudProperties = giftCloudProperties;
        this.container = container;
        this.reporter = reporter;
        projectListModel = new ProjectListModel(giftCloudProperties);
        serverFactory = new GiftCloudServerFactory(giftCloudProperties, projectListModel, reporter.getContainer(), reporter);
    }

    /**
     * Launches the interactive wizard for file uploading
     * This method does not throw any exceptions
     *
     * @param multiUploadParameters
     * @return true if the wizard was launched successfully
     */
    public boolean runWizard(final MultiUploadParameters multiUploadParameters) {
        String giftCloudServerUrl = "";

        try {
            final GiftCloudServer giftCloudServer = serverFactory.getGiftCloudServer();
            giftCloudServerUrl = giftCloudServer.getGiftCloudServerUrl();

            final Dimension windowSize = new Dimension(300, 300);
            new MultiUploadWizard(giftCloudServer.getRestServerHelper(), windowSize, multiUploadParameters, giftCloudServer.getGiftCloudServerUrl(), reporter);
            return true;

        } catch (CancellationException e) {
            reporter.silentLogException(e, "The upload wizard was cancelled. Server:" + giftCloudServerUrl + ", error:" + e.getMessage());
            // Do not report anything to user, since the user initiated the cancellation
            return false;

        } catch (AuthenticationException e) {
            reporter.silentLogException(e, "The GIFT-Cloud username or password was not recognised. Server:" + giftCloudServerUrl + ", error:" + e.getMessage());
            JOptionPane.showMessageDialog(container, "The GIFT-Cloud username or password was not recognised.", "Error", JOptionPane.DEFAULT_OPTION);
            return false;

        } catch (Exception e) {
            reporter.silentLogException(e, "An error occurred while executing the upload wizard using the GIFT-Cloud server at " + giftCloudServerUrl + ": " + e.getMessage());
            JOptionPane.showMessageDialog(container, "Could not launch the wizard due to the following error: " + e.getMessage(), "Error", JOptionPane.DEFAULT_OPTION);
            return false;
        }
    }

    /**
     * Attempt to connect to the GIFT-Cloud server and initiate a session
     * This method does not throw any exceptions
     *
     * @return true if the authentication was successful
     */
    public boolean tryAuthentication() {
        String giftCloudServerUrl = "";

        // We attempt to connect to the GIFT-Cloud server, in order to authenticate and to set the project list, but we allow the connection to fail gracefully
        try {
            final GiftCloudServer giftCloudServer = serverFactory.getGiftCloudServer();
            giftCloudServerUrl = giftCloudServer.getGiftCloudServerUrl();

            // Allow user to log in again if they have previously cancelled a login dialog
            giftCloudServer.resetCancellation();

            serverFactory.getGiftCloudServer().tryAuthentication();
            return true;

        } catch (CancellationException e) {
            reporter.silentLogException(e, "Authentication was cancelled. Server:" + giftCloudServerUrl + ", error:" + e.getMessage());
            // Do not report anything to user, since the user initiated the cancellation
            return false;

        } catch (AuthenticationException e) {
            reporter.silentLogException(e, "The GIFT-Cloud username or password was not recognised. Server:" + giftCloudServerUrl + ", error:" + e.getMessage());
            JOptionPane.showMessageDialog(container, "The GIFT-Cloud username or password was not recognised.", "Error", JOptionPane.DEFAULT_OPTION);
            return false;

        } catch (Exception e) {
            reporter.silentLogException(e, "An error occurred when attempting to connect to the GIFT-Cloud server at " + giftCloudServerUrl + ": " + e.getMessage());
            JOptionPane.showMessageDialog(container, "Could not connect to the GIFT-Cloud server due to the following error: " + e.getMessage(), "Error", JOptionPane.DEFAULT_OPTION);
            return false;
        }
    }

    public ComboBoxModel<String> getProjectListModel() {
        return projectListModel;
    }

    public boolean uploadToGiftCloud(Vector<String> paths) throws IOException {

        try {
            final GiftCloudServer giftCloudServer = serverFactory.getGiftCloudServer();

            // Allow user to log in again if they have previously cancelled a login dialog
            giftCloudServer.resetCancellation();

            final String projectName = getProjectName(giftCloudServer);

            return giftCloudServer.uploadToGiftCloud(paths, projectName);

        } catch (Throwable throwable) {

            return false;
        }
    }

    public boolean appendToGiftCloud(Vector<String> paths) throws IOException {

        try {
            final GiftCloudServer giftCloudServer = serverFactory.getGiftCloudServer();

            // Allow user to log in again if they have previously cancelled a login dialog
            giftCloudServer.resetCancellation();

            final String projectName = getProjectName(giftCloudServer);

            return giftCloudServer.appendToGiftCloud(paths, projectName);

        } catch (Throwable throwable) {

            return false;
        }
    }

    private String getProjectName(final GiftCloudServer giftCloudServer) throws IOException {
        String selectedProjectName = (String) projectListModel.getSelectedItem();
        if (StringUtils.isEmpty(selectedProjectName)) {
            try {
                selectedProjectName = GiftCloudDialogs.showInputDialogToSelectProject(giftCloudServer.getListOfProjects(), container, giftCloudProperties.getLastProject());
            } catch (IOException e) {
                throw new IOException("Unable to retrieve project list due to following error: " + e.getMessage(), e);
            }
        }
        return selectedProjectName;
    }

}
