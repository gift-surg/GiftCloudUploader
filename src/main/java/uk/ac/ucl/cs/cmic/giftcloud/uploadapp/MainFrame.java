package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.StatusObservable;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

public class MainFrame extends StatusObservable<MainFrame.MainWindowVisibility> {

    private Container container;
    private JFrame parent;
    private final GiftCloudUploaderAppConfiguration application;

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

    public MainFrame(final GiftCloudUploaderAppConfiguration application) throws InvocationTargetException, InterruptedException {
        this.application = application;
        createFrame();

        // Invoke the hide method on the controller, to ensure the system tray menu gets updated correctly
        parent.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
    }

    private void createFrame() throws InvocationTargetException, InterruptedException {
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                setSystemLookAndFeel();
                parent = new JFrame();
                container = parent;
                Optional<Image> icon = application.getIconImage();
                if (icon.isPresent()) {
                    parent.setIconImage(icon.get());
                }
                parent.setTitle(application.getApplicationTitle());
                parent.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
        });

    }

    public void show() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                container.setVisible(true);
                parent.setAlwaysOnTop(true);
                parent.toFront();
                container.requestFocus();
                parent.setAlwaysOnTop(false);
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
                parent.pack();
            }
        });
    }

    public JFrame getParent() {
        return parent;
    }

    private void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Panel.background", Color.white);
            UIManager.put("CheckBox.background", Color.lightGray);
            UIManager.put("SplitPane.background", Color.white);
            UIManager.put("OptionPane.background", Color.white);
            UIManager.put("Panel.background", Color.white);

            Font font = new Font("Arial Unicode MS",Font.PLAIN,12);
            if (font != null) {
                UIManager.put("Tree.font", font);
                UIManager.put("Table.font", font);
            }
        } catch (Throwable t) {
            System.out.println("Error when setting the system look and feel: " + t.getLocalizedMessage());
        }
    }
}