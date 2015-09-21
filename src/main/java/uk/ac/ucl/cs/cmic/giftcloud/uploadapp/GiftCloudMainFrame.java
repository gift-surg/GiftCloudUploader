package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.StatusObservable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class GiftCloudMainFrame extends StatusObservable<GiftCloudMainFrame.MainWindowVisibility> implements MainFrame {
    protected final JFrame container;

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

    GiftCloudMainFrame(final String applicationTitle, final Image image) {
        container = new JFrame();
        container.setIconImage(image);
        container.setTitle(applicationTitle);
        container.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Invoke the hide method on the controller, to ensure the system tray menu gets updated correctly
        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
    }

    @Override
    public void show() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                container.setVisible(true);

                // If we were invoking a JFrame we should also set the extended state
//                int state = container.getExtendedState();
//                state &= ~JFrame.ICONIFIED;
//                container.setExtendedState(state);

                container.setAlwaysOnTop(true);
                container.toFront();
                container.requestFocus();
                container.setAlwaysOnTop(false);
                notifyStatusChanged(MainWindowVisibility.VISIBLE);
            }
        });
    }

    @Override
    public void hide() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                container.setVisible(false);
                notifyStatusChanged(MainWindowVisibility.HIDDEN);
            }
        });
    }

    @Override
    public Container getContainer() {
        return container;
    }

    public JFrame getDialog() {
        return container;
    }

    public void addMainPanel(final Container panel) {
        container.add(panel);
        container.pack();
    }
}