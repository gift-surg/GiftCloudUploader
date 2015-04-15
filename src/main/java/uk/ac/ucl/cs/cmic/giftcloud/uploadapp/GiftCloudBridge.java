package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServer;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudServerFactory;

import javax.security.sasl.AuthenticationException;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.CancellationException;

public class GiftCloudBridge {

    private GiftCloudReporter reporter;
    private GiftCloudPropertiesFromBridge giftCloudProperties;
    private Container container;

    private final ProjectListModel projectListModel;
    private final GiftCloudServerFactory serverFactory;

    public GiftCloudBridge(final GiftCloudReporter reporter, final Container container, final GiftCloudPropertiesFromBridge giftCloudProperties) throws IOException {
        this.container = container;
        this.giftCloudProperties = giftCloudProperties;
        this.reporter = reporter;
        projectListModel = new ProjectListModel(giftCloudProperties);
        serverFactory = new GiftCloudServerFactory(giftCloudProperties, projectListModel, container, reporter);

        if (!giftCloudProperties.getGiftCloudUrl().isPresent()) {
            JOptionPane.showMessageDialog(container, "Please set an URL for the GIFT-Cloud server.", "Error", JOptionPane.DEFAULT_OPTION);
            return;
        }


    }

    public void tryAuthentication() {
        // We attempt to connect to the GIFT-Cloud server, in order to authenticate and to set the project list, but we allow the connection to fail gracefully
        try {
            final GiftCloudServer giftCloudServer = serverFactory.getGiftCloudServer();

            // Allow user to log in again if they have previously cancelled a login dialog
            giftCloudServer.resetCancellation();

            serverFactory.getGiftCloudServer().tryAuthentication();

        } catch (CancellationException e) {
            // Do nothing here

        } catch (AuthenticationException e) {
            JOptionPane.showMessageDialog(container, "The GIFT-Cloud username or password was not recognised.", "Error", JOptionPane.DEFAULT_OPTION);
            // ToDo: log error here

        } catch (IOException e) {

            JOptionPane.showMessageDialog(container, "Could not connect to the GIFT-Cloud server due to the following error: " + e.getMessage(), "Error", JOptionPane.DEFAULT_OPTION);
            // ToDo: log error here
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

            String selectedProjectName = (String) projectListModel.getSelectedItem();
            if (StringUtils.isEmpty(selectedProjectName)) {
                try {
                    selectedProjectName = showInputDialogToSelectProject(giftCloudServer.getListOfProjects(), container);
                } catch (IOException e) {
                    throw new IOException("Unable to retrieve project list due to following error: " + e.getMessage(), e);
                }
            }

            final String projectName = selectedProjectName;

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

            String selectedProjectName = (String) projectListModel.getSelectedItem();
            if (StringUtils.isEmpty(selectedProjectName)) {
                try {
                    selectedProjectName = showInputDialogToSelectProject(giftCloudServer.getListOfProjects(), container);
                } catch (IOException e) {
                    throw new IOException("Unable to retrieve project list due to following error: " + e.getMessage(), e);
                }
            }

            final String projectName = selectedProjectName;

            return giftCloudServer.appendToGiftCloud(paths, projectName);

        } catch (Throwable throwable) {

            return false;
        }
    }



    private String showInputDialogToSelectProject(final Vector<Object> projectMap, final Component component) throws IOException {
        final String lastProjectName = giftCloudProperties.getLastProject().isPresent() ? giftCloudProperties.getLastProject().get() : "";

        if (projectMap.size() < 1) {
            return null;
        }

        String[] projectStringArray = projectMap.toArray(new String[0]);

        final String defaultSelection = projectMap.contains(lastProjectName) ? lastProjectName : null;

        return (String)JOptionPane.showInputDialog(component, "Please select a project to which data will be uploaded.", "GIFT-Cloud", JOptionPane.QUESTION_MESSAGE, null, projectStringArray, defaultSelection);
    }

}
