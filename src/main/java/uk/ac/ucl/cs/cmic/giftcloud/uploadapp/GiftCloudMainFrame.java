package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class GiftCloudMainFrame  extends MainFrame {
    private JFrame frame;

    public static String propertiesFileName  = "GiftCloudUploader.properties";


    GiftCloudMainFrame(final JFrame frame) {
        super(frame, frame);
        this.frame = frame;
        final Image image = new ImageIcon(this.getClass().getClassLoader().getResource("uk/ac/ucl/cs/cmic/giftcloud/GiftSurgMiniIcon.png")).getImage();
        frame.setIconImage(image);
        frame.setTitle(getResourceBundle().getString("applicationTitle"));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Invoke the hide method on the controller, to ensure the system tray menu gets updated correctly
        frame.addWindowListener(new WindowAdapter() {
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
                frame.setVisible(true);
                frame.setAlwaysOnTop(true);
                frame.toFront();
                frame.requestFocus();
                frame.setAlwaysOnTop(false);
                notifyStatusChanged(MainWindowVisibility.VISIBLE);
            }
        });
    }

    @Override
    public void addMainPanel(final Container panel) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.add(panel);
                frame.pack();
            }
        });
    }
}