package uk.ac.ucl.cs.cmic.giftcloud.restserver;

/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudUploaderAppConfiguration;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.net.PasswordAuthentication;

public class GiftCloudLoginDialog {
    private final static GridBagConstraints promptConstraint = new GridBagConstraints();
    private final static GridBagConstraints labelConstraint = new GridBagConstraints();
    private final static GridBagConstraints fieldConstraint = new GridBagConstraints();

    private final static String LOGIN_DIALOG_TITLE = "GIFT-Cloud";

    static {
        promptConstraint.gridx = 0;
        promptConstraint.gridwidth = 2;
        promptConstraint.anchor = GridBagConstraints.PAGE_START;
        promptConstraint.fill = GridBagConstraints.HORIZONTAL;
        promptConstraint.ipady = 40;
        labelConstraint.gridx = 0;
        labelConstraint.anchor = GridBagConstraints.LINE_START;
        fieldConstraint.gridx = 1;
        fieldConstraint.anchor = GridBagConstraints.LINE_END;
        fieldConstraint.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraint.weightx = 1;
    }

    private final ImageIcon icon;
    private final GiftCloudProperties giftCloudProperties;
    private final Component parent;

    public GiftCloudLoginDialog(final GiftCloudUploaderAppConfiguration appConfiguration, final GiftCloudProperties giftCloudProperties, final Component parent) {
        this.icon = appConfiguration.getLargeIcon();
        this.giftCloudProperties = giftCloudProperties;
        this.parent = parent;
    }

    public PasswordAuthentication getPasswordAuthentication(final String prompt) {
        // Set the default background colour to white
        UIManager UI =new UIManager();
        UI.put("OptionPane.background", Color.white);
        UI.put("Panel.background", Color.white);

        String defaultUserName = "";
        if (giftCloudProperties.getLastUserName().isPresent()) {
            defaultUserName = giftCloudProperties.getLastUserName().get();
        }

        // Create a panel for entering username and password
        final JPanel usernamePasswordPanel = new JPanel(new GridBagLayout());

        final JLabel promptField = new JLabel(prompt, SwingConstants.CENTER);
        final JTextField usernameField = new JTextField(defaultUserName, 16);
        final JPasswordField passwordField = new JPasswordField(16);

        // Add a special listener to get the focus onto the username field when the component is created, or password if the username has already been populated from the default value
        if (StringUtils.isBlank(defaultUserName)) {
            usernameField.addAncestorListener(new RequestFocusListener());
        } else {
            passwordField.addAncestorListener(new RequestFocusListener());
        }

        usernamePasswordPanel.add(promptField, promptConstraint);
        usernamePasswordPanel.add(new JLabel("Username:"), labelConstraint);
        usernamePasswordPanel.add(usernameField, fieldConstraint);
        usernamePasswordPanel.add(new JLabel("Password:"), labelConstraint);
        usernamePasswordPanel.add(passwordField, fieldConstraint);

        // Show the login dialog
        final int returnValue = JOptionPane.showConfirmDialog(new JDialog(getFrame(parent)), usernamePasswordPanel, LOGIN_DIALOG_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icon);

        if (JOptionPane.OK_OPTION == returnValue) {
            giftCloudProperties.setLastUserName(usernameField.getText());
            giftCloudProperties.setLastPassword(passwordField.getPassword());
            giftCloudProperties.save();


            return new PasswordAuthentication(usernameField.getText(), passwordField.getPassword());
        } else {
            return null;
        }
    }

    private static Frame getFrame(Component comp) {
        if (comp instanceof Frame) {
            return (Frame)comp;
        }
        return getFrame(SwingUtilities.windowForComponent(comp));
    }

    // This listener class is used to set the focus onto the username text input field when the login dialog is created
    private static class RequestFocusListener implements AncestorListener
    {
        @Override
        public void ancestorAdded(AncestorEvent ancestorEvent)
        {
            JComponent component = ancestorEvent.getComponent();

            // Set focus
            component.requestFocusInWindow();

            // Remove this listener
            component.removeAncestorListener(this);
        }

        @Override
        public void ancestorMoved(AncestorEvent e) {}

        @Override
        public void ancestorRemoved(AncestorEvent e) {}
    }
}
