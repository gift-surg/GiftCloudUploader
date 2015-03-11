/**
 * JDateChooserTextFieldDataEditor
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 12/29/11 by rherri01
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class JDateChooserTextFieldDataEditor extends JTextFieldDateEditor {
    public static JDateChooserTextFieldDataEditor getInstance() {
        return _instance;
    }

    private JDateChooserTextFieldDataEditor() {
        super();
    }

    @Override
    public Dimension getPreferredSize() {
        //if we have a date and a formatter, use that to get the text field size
        if(this.dateFormatter != null) {
            //if no date, use today
            Date d = this.date != null ? this.date : new Date();
            String s = this.dateFormatter.format(d);
            return new JTextField(s).getPreferredSize();
        }
        //else, use the date pattern string itself
        if (datePattern != null) {
            return new JTextField(datePattern).getPreferredSize();
        }
        //else, return default
        return super.getPreferredSize();
    }

    private static final JDateChooserTextFieldDataEditor _instance = new JDateChooserTextFieldDataEditor();
}
