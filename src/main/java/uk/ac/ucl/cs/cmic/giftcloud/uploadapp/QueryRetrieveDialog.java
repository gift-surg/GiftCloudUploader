package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * A dialog for performing Dicom query and retrieve operations
 */
public class QueryRetrieveDialog extends JDialog {

    private final QueryFilterPanel queryFilterPanel;
    private final UploaderGuiController controller;
    private final QueryRetrieveRemoteView queryRetrieveRemoteView;
    private final JButton retrieveButton;


    /**
     * Creates the query-retrieve dialog
     *
     * @param owner the owning dialog
     * @param controller used to perform the query-retrieve operations
     * @param resourceBundle for accessing the text labels
     */
    QueryRetrieveDialog(final JFrame owner, final UploaderGuiController controller, final ResourceBundle resourceBundle) {
        super(owner);
        this.controller = controller;

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setTitle(resourceBundle.getString("queryRetrieveWindowTitle"));

        queryFilterPanel = new QueryFilterPanel(controller, resourceBundle);

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        JPanel buttonPanel = new JPanel();
        final GridBagLayout buttonPanellayout = new GridBagLayout();
        buttonPanel.setLayout(buttonPanellayout);

        JSeparator separator = new JSeparator();
        GridBagConstraints separatorConstraint = new GridBagConstraints();
        separatorConstraint.weightx = 1.0;
        separatorConstraint.fill = GridBagConstraints.HORIZONTAL;
        separatorConstraint.gridwidth = GridBagConstraints.REMAINDER;
        buttonPanel.add(separator, separatorConstraint);

        JPanel retrieveButtonPanel = new JPanel();
        retrieveButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton cancelButton = new JButton(resourceBundle.getString("closeRetrieveButtonLabelText"));
        cancelButton.setToolTipText(resourceBundle.getString("closeRetrieveButtonToolTipText"));
        retrieveButtonPanel.add(cancelButton);
        cancelButton.addActionListener(new CloseRetrieveActionListener());

        retrieveButton = new JButton(resourceBundle.getString("retrieveButtonLabelText"));
        retrieveButton.setEnabled(false);
        retrieveButton.setToolTipText(resourceBundle.getString("retrieveButtonToolTipText"));
        retrieveButtonPanel.add(retrieveButton);
        retrieveButton.addActionListener(new RetrieveActionListener());

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        buttonPanellayout.setConstraints(buttonPanel, constraints);
        buttonPanel.add(retrieveButtonPanel);

        // The remote view will call back into this class to enable or disable the retrieve button
        queryRetrieveRemoteView = new QueryRetrieveRemoteView(this);

        {
            GridBagConstraints queryFilterTextEntryPanelConstraints = new GridBagConstraints();
            queryFilterTextEntryPanelConstraints.gridx = 0;
            queryFilterTextEntryPanelConstraints.gridy = 0;
            queryFilterTextEntryPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
            queryFilterTextEntryPanelConstraints.insets = new Insets(5, 5, 5, 5);
            layout.setConstraints(queryFilterPanel, queryFilterTextEntryPanelConstraints);
            add(queryFilterPanel);
        }
        {
            GridBagConstraints remoteBrowserPanesConstraints = new GridBagConstraints();
            remoteBrowserPanesConstraints.gridx = 0;
            remoteBrowserPanesConstraints.gridy = 2;
            remoteBrowserPanesConstraints.weightx = 1;
            remoteBrowserPanesConstraints.weighty = 1;
            remoteBrowserPanesConstraints.fill = GridBagConstraints.BOTH;
            remoteBrowserPanesConstraints.insets = new Insets(5, 5, 5, 5);
            layout.setConstraints(queryRetrieveRemoteView, remoteBrowserPanesConstraints);
            add(queryRetrieveRemoteView);
        }
        {
            GridBagConstraints buttonPanelConstraints = new GridBagConstraints();
            buttonPanelConstraints.gridx = 0;
            buttonPanelConstraints.gridy = 3;
            buttonPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
            buttonPanelConstraints.insets = new Insets(5, 5, 5, 5);
            layout.setConstraints(buttonPanel, buttonPanelConstraints);
            add(buttonPanel);
        }

        pack();
    }

    public QueryRetrieveRemoteView getQueryRetrieveRemoteView() {
        return queryRetrieveRemoteView;
    }

    public void updateListStatus(boolean nonEmpty) {
        retrieveButton.setEnabled(nonEmpty);
    }

    private class RetrieveActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.retrieve(queryRetrieveRemoteView.getCurrentRemoteQuerySelectionList());
        }
    }

    private class CloseRetrieveActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            setVisible(false);
        }
    }
}
