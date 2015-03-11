package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.display.SafeProgressBarUpdaterThread;
import com.pixelmed.utils.MessageLogger;
import org.apache.commons.lang.StringUtils;

import javax.security.sasl.AuthenticationException;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.CancellationException;

public class GiftCloudBridge {

    private GiftCloudReporter reporter;
    private GiftCloudPropertiesFromBridge giftCloudProperties;
    private Container container;

    private ProjectListModel projectListModel = new ProjectListModel();

    Optional<GiftCloudAutoUploader> giftCloudAutoUploader = Optional.empty();

    public GiftCloudBridge(final Container container, final GiftCloudPropertiesFromBridge giftCloudProperties) throws IOException {
        this.container = container;
        this.giftCloudProperties = giftCloudProperties;
        reporter = new GiftCloudReporter(container);

        if (!giftCloudProperties.getGiftCloudUrl().isPresent()) {
            JOptionPane.showMessageDialog(container, "Please set an URL for the GIFT-Cloud server.", "Error", JOptionPane.DEFAULT_OPTION);
            return;
        }

        // We attempt to connect to the GIFT-Cloud server, in order to authenticate and to set the project list, but we allow the connection to fail gracefully
        try {
            getGiftCloudAutoUploader().tryAuthentication();

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

    public boolean uploadToGiftCloud(Vector paths, MessageLogger logger, SafeProgressBarUpdaterThread progressBarUpdaterThread) throws IOException {

        try {
            final GiftCloudAutoUploader giftCloudAutoUploader = getGiftCloudAutoUploader();

            // Allow user to log in again if they have previously cancelled a login dialog
            giftCloudAutoUploader.resetCancellation();

            String selectedProjectName = (String) projectListModel.getSelectedItem();
            if (StringUtils.isEmpty(selectedProjectName)) {
                try {
                    selectedProjectName = showInputDialogToSelectProject(giftCloudAutoUploader.getListOfProjects(), container);
                } catch (IOException e) {
                    throw new IOException("Unable to retrieve project list due to following error: " + e.getMessage(), e);
                }
            }

            final String projectName = selectedProjectName;

            return getGiftCloudAutoUploader().uploadToGiftCloud(paths, logger, progressBarUpdaterThread, projectName);

        } catch (Throwable throwable) {

            return false;
        }
    }

    public boolean appendToGiftCloud(Vector paths, MessageLogger logger, SafeProgressBarUpdaterThread progressBarUpdaterThread) throws IOException {

        try {
            final GiftCloudAutoUploader giftCloudAutoUploader = getGiftCloudAutoUploader();

            // Allow user to log in again if they have previously cancelled a login dialog
            giftCloudAutoUploader.resetCancellation();

            String selectedProjectName = (String) projectListModel.getSelectedItem();
            if (StringUtils.isEmpty(selectedProjectName)) {
                try {
                    selectedProjectName = showInputDialogToSelectProject(giftCloudAutoUploader.getListOfProjects(), container);
                } catch (IOException e) {
                    throw new IOException("Unable to retrieve project list due to following error: " + e.getMessage(), e);
                }
            }

            final String projectName = selectedProjectName;

            return getGiftCloudAutoUploader().appendToGiftCloud(paths, logger, progressBarUpdaterThread, projectName);

        } catch (Throwable throwable) {

            return false;
        }
    }

    private GiftCloudAutoUploader getGiftCloudAutoUploader() throws IOException {

        final Optional<String> giftCloudUrl = giftCloudProperties.getGiftCloudUrl();

        // Check for an URL which is either not present or empty
        if (!giftCloudUrl.isPresent() || StringUtils.isBlank(giftCloudUrl.get())) {
            throw new MalformedURLException("Please set the URL for the GIFT-Cloud server.");
        }

        // We need to create new GiftCloudAutoUploader if one does not exist, or if the URL has changed
        if (!(giftCloudAutoUploader.isPresent() && giftCloudAutoUploader.get().getUrl().equals(giftCloudUrl.get()))) {

            // The project list is no longer valid. We will update it after creating a new GiftCloudAutoUploader, but if that throws an exception, we want to leave the project list model in an invalid state
            projectListModel.invalidate();

            giftCloudAutoUploader = Optional.of(new GiftCloudAutoUploader(container, giftCloudProperties, reporter));

            // Now update the project list
            projectListModel.setItems(giftCloudAutoUploader.get().getListOfProjects());
        }

        return giftCloudAutoUploader.get();
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

    class ProjectListModel extends DropDownListModel {

        @Override
        void setLastUsedValue(String newValue) {
            giftCloudProperties.setLastProject(newValue);
        }

        @Override
        Optional<String> getLastUsedValue() {
            return giftCloudProperties.getLastProject();
        }
    }

}
