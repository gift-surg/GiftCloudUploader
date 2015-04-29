package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
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

    QueryFilterPanel(final ResourceBundle resourceBundle) {
        Border panelBorder = BorderFactory.createEtchedBorder();

        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(panelBorder);

        JLabel queryIntroduction = new JLabel(resourceBundle.getString("queryIntroductionLabelText"));
        add(queryIntroduction);

        JLabel queryFilterPatientNameLabel = new JLabel(resourceBundle.getString("queryPatientNameLabelText"));
        queryFilterPatientNameLabel.setToolTipText(resourceBundle.getString("queryPatientNameToolTipText"));
        add(queryFilterPatientNameLabel);
        queryFilterPatientNameTextField = new JTextField("",textFieldLengthForQueryPatientName);
        add(queryFilterPatientNameTextField);

        JLabel queryFilterPatientIDLabel = new JLabel(resourceBundle.getString("queryPatientIDLabelText"));
        queryFilterPatientIDLabel.setToolTipText(resourceBundle.getString("queryPatientIDToolTipText"));
        add(queryFilterPatientIDLabel);
        queryFilterPatientIDTextField = new JTextField("",textFieldLengthForQueryPatientID);
        add(queryFilterPatientIDTextField);

        JLabel queryFilterStudyDateLabel = new JLabel(resourceBundle.getString("queryStudyDateLabelText"));
        queryFilterStudyDateLabel.setToolTipText(resourceBundle.getString("queryStudyDateToolTipText"));
        add(queryFilterStudyDateLabel);
        queryFilterStudyDateTextField = new JTextField("",textFieldLengthForQueryStudyDate);
        add(queryFilterStudyDateTextField);

        JLabel queryFilterAccessionNumberLabel = new JLabel(resourceBundle.getString("queryAccessionNumberLabelText"));
        queryFilterAccessionNumberLabel.setToolTipText(resourceBundle.getString("queryAccessionNumberToolTipText"));
        add(queryFilterAccessionNumberLabel);
        queryFilterAccessionNumberTextField = new JTextField("",textFieldLengthForQueryAccessionNumber);
        add(queryFilterAccessionNumberTextField);


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
}
