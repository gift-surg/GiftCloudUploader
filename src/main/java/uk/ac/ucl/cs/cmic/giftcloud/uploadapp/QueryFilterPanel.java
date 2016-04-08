package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * The panel containing filter controls for performing a query operation
 */
public class QueryFilterPanel extends JPanel {

    private static int textFieldLengthForQueryPatientName = 16;
    private static int textFieldLengthForQueryPatientID = 10;
    private static int textFieldLengthForQueryStudyDate = 8;
    private static int textFieldLengthForQueryAccessionNumber = 10;

    private final JTextField queryFilterPatientNameTextField;
    private final JTextField queryFilterPatientIDTextField;
    private final JTextField queryFilterStudyDateTextField;
    private final JTextField queryFilterAccessionNumberTextField;
    private GiftCloudUploaderController controller;

    QueryFilterPanel(final GiftCloudUploaderController controller, final ResourceBundle resourceBundle) {
        this.controller = controller;

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        final GridBagConstraints introConstraints = new GridBagConstraints();
        introConstraints.gridx = 0;
        introConstraints.gridy = 0;
        introConstraints.gridwidth = 2;
        introConstraints.weightx = 1;
        introConstraints.weighty = 1;
        introConstraints.fill = GridBagConstraints.HORIZONTAL;

        final GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        labelConstraints.gridwidth = 1;
        labelConstraints.weightx = 1;
        labelConstraints.weighty = 1;
        labelConstraints.anchor = GridBagConstraints.LINE_START;
        labelConstraints.fill = GridBagConstraints.NONE;

        final GridBagConstraints inputConstraints = new GridBagConstraints();
        inputConstraints.gridx = 1;
        inputConstraints.gridy = 0;
        inputConstraints.gridwidth = 1;
        inputConstraints.weightx = 1;
        inputConstraints.weighty = 1;
        inputConstraints.anchor = GridBagConstraints.LINE_END;
        inputConstraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel queryIntroduction = new JLabel(resourceBundle.getString("queryIntroductionLabelText"));
        layout.setConstraints(queryIntroduction, introConstraints);
        add(queryIntroduction);

        JLabel queryFilterPatientNameLabel = new JLabel(resourceBundle.getString("queryPatientNameLabelText"));
        queryFilterPatientNameLabel.setToolTipText(resourceBundle.getString("queryPatientNameToolTipText"));
        labelConstraints.gridy = 1;
        layout.setConstraints(queryFilterPatientNameLabel, labelConstraints);
        add(queryFilterPatientNameLabel);

        queryFilterPatientNameTextField = new JTextField("",textFieldLengthForQueryPatientName);
        inputConstraints.gridy = 1;
        layout.setConstraints(queryFilterPatientNameTextField, inputConstraints);
        add(queryFilterPatientNameTextField);


        JLabel queryFilterPatientIDLabel = new JLabel(resourceBundle.getString("queryPatientIDLabelText"));
        queryFilterPatientIDLabel.setToolTipText(resourceBundle.getString("queryPatientIDToolTipText"));
        labelConstraints.gridy = 2;
        layout.setConstraints(queryFilterPatientIDLabel, labelConstraints);
        add(queryFilterPatientIDLabel);

        queryFilterPatientIDTextField = new JTextField("",textFieldLengthForQueryPatientID);
        inputConstraints.gridy = 2;
        layout.setConstraints(queryFilterPatientIDTextField, inputConstraints);
        add(queryFilterPatientIDTextField);

        JLabel queryFilterStudyDateLabel = new JLabel(resourceBundle.getString("queryStudyDateLabelText"));
        queryFilterStudyDateLabel.setToolTipText(resourceBundle.getString("queryStudyDateToolTipText"));
        labelConstraints.gridy = 3;
        layout.setConstraints(queryFilterStudyDateLabel, labelConstraints);
        add(queryFilterStudyDateLabel);

        queryFilterStudyDateTextField = new JTextField("",textFieldLengthForQueryStudyDate);
        inputConstraints.gridy = 3;
        layout.setConstraints(queryFilterStudyDateTextField, inputConstraints);
        add(queryFilterStudyDateTextField);

        JLabel queryFilterAccessionNumberLabel = new JLabel(resourceBundle.getString("queryAccessionNumberLabelText"));
        queryFilterAccessionNumberLabel.setToolTipText(resourceBundle.getString("queryAccessionNumberToolTipText"));
        labelConstraints.gridy = 4;
        layout.setConstraints(queryFilterAccessionNumberLabel, labelConstraints);
        add(queryFilterAccessionNumberLabel);

        queryFilterAccessionNumberTextField = new JTextField("",textFieldLengthForQueryAccessionNumber);
        inputConstraints.gridy = 4;
        layout.setConstraints(queryFilterAccessionNumberTextField, inputConstraints);
        add(queryFilterAccessionNumberTextField);


        // Add the query/search button
        JPanel queryButtonPanel = new JPanel();
        queryButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton queryButton = new JButton(resourceBundle.getString("queryButtonLabelText"));
        queryButton.setToolTipText(resourceBundle.getString("queryButtonToolTipText"));
        queryButtonPanel.add(queryButton);
        queryButton.addActionListener(new QueryActionListener());


        {
            GridBagConstraints buttonPanelConstraints = new GridBagConstraints();
            buttonPanelConstraints.gridx = 0;
            buttonPanelConstraints.gridy = 5;
            buttonPanelConstraints.gridwidth = 2;
            buttonPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
            layout.setConstraints(queryButtonPanel, buttonPanelConstraints);
            add(queryButtonPanel);
        }


    }

    public QueryParams getQueryParams() {
        final QueryParams queryParams = new QueryParams();

        String patientName = queryFilterPatientNameTextField.getText().trim();
        if (patientName != null && patientName.length() > 0) {
            queryParams.setPatientName(patientName);
        }

        String patientID = queryFilterPatientIDTextField.getText().trim();
        if (patientID != null && patientID.length() > 0) {
            queryParams.setPatientId(patientID);
        }

        String accessionNumber = queryFilterAccessionNumberTextField.getText().trim();
        if (accessionNumber != null && accessionNumber.length() > 0) {
            queryParams.setAccessionNumber(accessionNumber);
        }

        String studyDate = queryFilterStudyDateTextField.getText().trim();
        if (studyDate != null && studyDate.length() > 0) {
            queryParams.setStudyDate(studyDate);
        }
        return queryParams;
    }

    private class QueryActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            controller.query(getQueryParams());
        }
    }

}
