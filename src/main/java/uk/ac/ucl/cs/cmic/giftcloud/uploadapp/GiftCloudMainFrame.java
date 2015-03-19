package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class GiftCloudMainFrame implements MainFrame {
    protected final JDialog container;
    private GiftCloudUploaderController controller;

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
            }
        });
    }

    @Override
    public void hide() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                container.setVisible(false);
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
}