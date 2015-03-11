package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import java.awt.*;

class GiftCloudMainFrame implements MainFrame {
    protected final JDialog container;

    GiftCloudMainFrame(final String applicationTitle) {
        container = new JDialog();
        container.setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("GiftSurgIconOnly.png")).getImage()); // ToDo: This icon is loaded multiple times in the code
        container.setTitle(applicationTitle);
        container.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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