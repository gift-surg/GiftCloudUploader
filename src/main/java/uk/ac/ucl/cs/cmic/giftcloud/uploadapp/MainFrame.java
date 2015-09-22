package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.StatusObservable;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class MainFrame extends StatusObservable<MainFrame.MainWindowVisibility> {

    private final Container container;
    private final JFrame parent;
    private final ResourceBundle resourceBundle;
    private static String resourceBundleName  = "uk.ac.ucl.cs.cmic.giftcloud.GiftCloudUploader";

    /**
     * Enumeration for the visible states of the main window. Less error-prone than passing round booleans for specifying visibility
     */
    public enum MainWindowVisibility {
        VISIBLE(true),
        HIDDEN(false);

        private final boolean isVisible;

        MainWindowVisibility(final boolean isVisible) {
            this.isVisible = isVisible;
        }

        boolean isVisible() {
            return isVisible;
        }
    }

    public MainFrame(final Container container, final JFrame parent) {
        this.container = container;
        this.parent = parent;
        resourceBundle = ResourceBundle.getBundle(resourceBundleName);
    }

    public void show() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                container.setVisible(true);
                container.requestFocus();
                notifyStatusChanged(MainWindowVisibility.VISIBLE);
            }
        });
    }

    public void hide() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                container.setVisible(false);
                notifyStatusChanged(MainWindowVisibility.HIDDEN);
            }
        });
    }

    public Container getContainer() {
        return container;
    }

    public void addMainPanel(final Container panel) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                container.add(panel);
            }
        });
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public JFrame getParent() {
        return parent;
    }
}