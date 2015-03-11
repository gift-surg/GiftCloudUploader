/*
 * UploadResultPanel
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import netscape.javascript.JSObject;
import uk.ac.ucl.cs.cmic.giftcloud.util.MouseClickTriggeredCallable;
import uk.ac.ucl.cs.cmic.giftcloud.util.JSEval;
import uk.ac.ucl.cs.cmic.giftcloud.util.Messages;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Optional;

public final class UploadResultPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int SPACING = 4;
    private static final Cursor linkCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    private JSObject context;
    private String windowName;

    public UploadResultPanel(final String label, final URL url, final Optional<String> windowName, final Optional<JSObject> jsContext) {
        if (jsContext.isPresent()) {
            this.context = jsContext.get();
        } else {
            this.context = null;
        }
        if (windowName.isPresent()) {
            this.windowName = windowName.get();
        } else {
            this.windowName = "_self";
        }

        final boolean isArchived = url.getPath().split("/")[3].equalsIgnoreCase("archive");
        final JLabel link = getLinkForResource(label, url);
        final Box box = new Box(BoxLayout.Y_AXIS);
        add(box);
        box.add(new JLabel(Messages.getMessage(Messages.UPLOADRESULTPANEL_SUCCESS, Messages.getMessage(isArchived ? Messages.VOCABULARY_ARCHIVE : Messages.VOCABULARY_PREARCHIVE))));
        box.add(Box.createVerticalStrut(SPACING));
        box.add(link);
    }

    private JLabel getLinkForResource(String label, URL url) {
        final JLabel link = new JLabel(Messages.getMessage(Messages.UPLOADRESULTPANEL_DEST_LINK, url, label));
        link.setCursor(linkCursor);
        if (null != context) {
            final JSEval eval = new JSEval(context, String.format("this.open(\"%s\",'%s');", url, windowName));
            link.addMouseListener(new MouseClickTriggeredCallable(eval));
        } else {
//            UIUtils.handleAppletInput(this, Messages.getMessage(Messages.ERROR_MSG_NOURLSUPPORT), Messages.getMessage(Messages.ERROR_TITLE_NOURLSUPPORT), JOptionPane.ERROR_MESSAGE, url.toString());
        }
        return link;
    }
}
