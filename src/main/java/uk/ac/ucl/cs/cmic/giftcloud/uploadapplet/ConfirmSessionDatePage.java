/*
 * ConfirmSessionDatePage
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import com.toedter.calendar.JDateChooser;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Map;

public final class ConfirmSessionDatePage extends WizardPage implements PropertyChangeListener, ChangeListener {
    public static final String PRODUCT_NAME = "*session-date*";

    private static final String STEP_DESCRIPTION = "Confirm session date";
    private static final String LONG_DESCRIPTION = "Enter the date of the session you plan to upload";
    public static final String SESSION_DATE_MESSAGE = "<html><p>Please enter the date for the session you wish to upload. This date</p>" +
            "<p>must match the session's date or you will <i>not</i> be able to upload the</p>" +
            "<p>session to the XNAT server. The date will be verified once you have</p>" +
            "<p>selected the session you want to upload.</p></html>";
    public static final String SESSION_DATE_LABEL = "<html><b>Verify session date:</b></html>";
    public static final String SESSION_DATE_PROBLEM = "Enter a date for the session you intend to upload.";
    public static final String NO_DATE_DESCRIPTION = "I don't know the date or my session doesn't have a date.";
    private final Logger logger = LoggerFactory.getLogger(ConfirmSessionDatePage.class);
    private final JDateChooser _dateChooser = new JDateChooser(null, UIUtils.DEFAULT_DATE_FORMAT, JDateChooserTextFieldDataEditor.getInstance());
    private final JCheckBox _noDate = new JCheckBox(NO_DATE_DESCRIPTION, false);
    private UploadSelector uploadSelector;

    public static String getDescription() {
        return STEP_DESCRIPTION;
    }

    public ConfirmSessionDatePage(final UploadSelector uploadSelector) {
        this.uploadSelector = uploadSelector;
        setLayout(new GridBagLayout());
        setLongDescription(LONG_DESCRIPTION);
        _dateChooser.addPropertyChangeListener("date", this);
        _noDate.addChangeListener(this);
        
    }

    /**
     * Indicates whether the user can click the Next button.
     * @param stepName The name of the current step.
     * @param settings Any settings for the current step.
     * @param wizard The current wizard.
     * @return Returns <b>WizardPanelNavResult.REMAIN_ON_PAGE</b> if the user hasn't entered a confirmation date,
     *         <b>WizardPanelNavResult.PROCEED</b> otherwise.
     */
    @Override
    public WizardPanelNavResult allowNext(String stepName, Map settings, Wizard wizard) {
    	if(_noDate.isSelected()){
    		return WizardPanelNavResult.PROCEED;
    	}
        Date date = _dateChooser.getDate();
        if (date == null) {
            setProblem("You must enter a valid value for the session date.");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        return WizardPanelNavResult.PROCEED;
    }

    /**
     * Indicates whether the user can click the Finish button.
     * @param stepName The name of the current step.
     * @param settings Any settings for the current step.
     * @param wizard The current wizard.
     * @return Returns <b>WizardPanelNavResult.REMAIN_ON_PAGE</b>: the user should never be able to finish from this
     *         step.
     */
    @Override
    public WizardPanelNavResult allowFinish(String stepName, Map settings, Wizard wizard) {
        return WizardPanelNavResult.REMAIN_ON_PAGE;
    }

    /**
     * Handles state-change events for the <b>JDateChooser</b> control. 
     * This catches changes by the user to the verification date 
     * control and forces a re-validation of the wizard page contents.
     * @param event The event raised by the property-change event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        logger.debug("Got event: " + event);
        if(!_noDate.isSelected()) {
            uploadSelector.setDate(_dateChooser.getDate());
        	userInputReceived(this, event);
        }
    }

    /**
     * Handles property-change events for the <b>JCheckBox</b> control. 
     * This catches changes by the user to the and forces a re-validation
     * of the wizard page contents.
     * @param event The event raised by the property-change event.
     */
	@Override
	public void stateChanged(ChangeEvent event) {
        logger.debug("Got event: " + event);
        if(_noDate == null || _dateChooser == null){
    		return;
    	}
        
        if(_noDate.isSelected()){
            uploadSelector.setFetchDateFromSession(true);
        	_dateChooser.setDate(null);
        	_dateChooser.setEnabled(false);
          	 
        } else {
            uploadSelector.setFetchDateFromSession(false);
        	_dateChooser.setEnabled(true);
        }

		userInputReceived(this,event);
	}

    /*
     * (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage#recycle()
     */
    @Override
    protected void recycle() {
        removeAll();
    }

    /*
     * (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage#renderingPage()
     */
    @Override
    protected void renderingPage() {
        logger.trace("rendering");
        final Insets standardInsets = new Insets(5, 5, 5, 5);
        add(new JLabel(SESSION_DATE_LABEL), new GridBagConstraints() {{
            gridx = 0; gridy = 0; 
            ipadx = ipady = 5; 
            anchor = GridBagConstraints.FIRST_LINE_START; 
            insets = standardInsets; }});
        add(_dateChooser, new GridBagConstraints() {{
            gridx = 1; gridy = 0;
            ipadx = ipady = 5;
            anchor = GridBagConstraints.FIRST_LINE_START;
            insets = standardInsets; }});
        add(new JLabel(SESSION_DATE_MESSAGE), new GridBagConstraints() {{
            gridx = 1; gridy = 1;
            ipadx = ipady = 5;
            anchor = GridBagConstraints.FIRST_LINE_START;
            fill = GridBagConstraints.HORIZONTAL;
            insets = standardInsets; }});
        add(_noDate, new GridBagConstraints() {{
            gridx = 1; gridy = 2;
            ipadx = ipady = 5;
            anchor = GridBagConstraints.FIRST_LINE_START;
            fill = GridBagConstraints.HORIZONTAL;
            insets = standardInsets; }});
        setProblem(SESSION_DATE_PROBLEM);
    }

    /*
     * (non-Javadoc)
     * @see org.netbeans.spi.wizard.WizardPage#validateContents(java.awt.Component, java.lang.Object)
     */
    @Override
    protected String validateContents(final Component component, final Object event) {
        if (_dateChooser.getDate() != null || _noDate.isSelected()) {
            return null;
        }
        return "";
    }


}
