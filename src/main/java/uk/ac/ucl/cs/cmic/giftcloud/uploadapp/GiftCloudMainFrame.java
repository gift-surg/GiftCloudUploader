package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

class GiftCloudMainFrame implements MainFrame {
    protected final JDialog container;
    private GiftCloudUploaderController controller;

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

    GiftCloudMainFrame(final String applicationTitle, final GiftCloudUploaderController controller) {
        this.controller = controller;
        container = new JDialog();
        container.setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/GiftSurgMiniIcon.png")).getImage()); // ToDo: This icon is loaded multiple times in the code
        container.setTitle(applicationTitle);
        container.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Invoke the hide method on the controller, to ensure the system tray menu gets updated correctly
        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                controller.hide();
            }
        });
    }

    private final java.util.List<MainFrameListener> listeners = new ArrayList<MainFrameListener>();

    void addListener(final MainFrameListener listener) {
        listeners.add(listener);
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
                notifyVisbilityChanged(MainWindowVisibility.HIDDEN);
            }
        });
    }

    private void notifyVisbilityChanged(final MainWindowVisibility visibility) {
        for (MainFrameListener listener : listeners) {
            listener.windowVisibilityChanged(visibility);
        }
    }

    @Override
    public void hide() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                container.setVisible(false);
                notifyVisbilityChanged(MainWindowVisibility.HIDDEN);
            }
        });
    }

    @Override
    public Container getContainer() {
        return container;
    }

    public void addMainPanel(final Container panel) {
        container.add(panel);
        container.pack();
    }

    interface MainFrameListener {
        void windowVisibilityChanged(final MainWindowVisibility visibility);
    }
}