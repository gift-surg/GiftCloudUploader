package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.query.QueryInformationModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A panel showing query/retrieve buttons and text options and the results of the DICOM query operation
 */
public class QueryRetrievePanel extends JPanel {

    private final QueryFilterPanel queryFilterPanel;
    private GiftCloudUploaderController controller;
    private QueryRetrieveRemoteView queryRetrieveRemoteView;


    QueryRetrievePanel(final GiftCloudUploaderController controller, final ResourceBundle resourceBundle) {
        this.controller = controller;

        queryFilterPanel = new QueryFilterPanel(controller, resourceBundle);
        queryRetrieveRemoteView = new QueryRetrieveRemoteView();

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        Border panelBorder = BorderFactory.createEtchedBorder();

        JPanel retrieveButtonPanel = new JPanel();
        retrieveButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        retrieveButtonPanel.setBorder(panelBorder);

        JButton retrieveButton = new JButton(resourceBundle.getString("retrieveButtonLabelText"));
        retrieveButton.setToolTipText(resourceBundle.getString("retrieveButtonToolTipText"));
        retrieveButtonPanel.add(retrieveButton);
        retrieveButton.addActionListener(new RetrieveActionListener());

        {
            GridBagConstraints queryFilterTextEntryPanelConstraints = new GridBagConstraints();
            queryFilterTextEntryPanelConstraints.gridx = 0;
            queryFilterTextEntryPanelConstraints.gridy = 0;
            queryFilterTextEntryPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
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
            layout.setConstraints(queryRetrieveRemoteView, remoteBrowserPanesConstraints);
            add(queryRetrieveRemoteView);
        }
        {
            GridBagConstraints buttonPanelConstraints = new GridBagConstraints();
            buttonPanelConstraints.gridx = 0;
            buttonPanelConstraints.gridy = 3;
            buttonPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
            layout.setConstraints(retrieveButtonPanel, buttonPanelConstraints);
            add(retrieveButtonPanel);
        }

    }

    public void updateQueryPanel(final QueryInformationModel queryInformationModel, final AttributeList filter, final QueryInformationModel currentRemoteQueryInformationModel) throws DicomNetworkException, DicomException, IOException {
        queryRetrieveRemoteView.updateQueryPanel(queryInformationModel, filter, currentRemoteQueryInformationModel);
    }

    public List<QuerySelection> getCurrentRemoteQuerySelectionList() {
        return queryRetrieveRemoteView.getCurrentRemoteQuerySelectionList();
    }

    public QueryRetrieveRemoteView getQueryRetrievePanel() {
        return queryRetrieveRemoteView;
    }

    private class RetrieveActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.retrieve(getCurrentRemoteQuerySelectionList());
        }
    }

}
