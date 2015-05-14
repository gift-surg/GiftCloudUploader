package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class DelayedProgress {

    private JDialog dialog;
    private int numClients = 0;
    private Thread timer = null;
    private String title;
    private String message;
    private int waitTimeMs;

    public DelayedProgress(final String title, final String message, final int waitTimeMs) {
        this.title = title;
        this.message = message;
        this.waitTimeMs = waitTimeMs;
    }

    public synchronized void show() {
        numClients++;

        if (timer == null) {
            timer = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(waitTimeMs);
                        createDialog();
                    } catch (InterruptedException e) {
                    }
                }
            });
            timer.start();
        }
    }

    public synchronized void hide() {
        if (numClients > 0) {
            numClients--;
        }
        if (numClients == 0 && dialog != null) {
            destroyDialog();
        }
    }

    private void destroyDialog() {
        if (timer != null) {
            timer.interrupt();
        }
        dialog.dispose();
        dialog = null;
    }

    private void createDialog() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    dialog = new JDialog((Frame)null, "GIFT-Cloud Uploader");
                    dialog.setLayout(new FlowLayout(FlowLayout.CENTER));
                    final JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
                    messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                    dialog.add(messageLabel);

                    dialog.pack();
                    dialog.setVisible(true);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
