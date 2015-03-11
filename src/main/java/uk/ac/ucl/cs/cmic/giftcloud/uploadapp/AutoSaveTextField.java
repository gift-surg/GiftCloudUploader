package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Optional;

public abstract class AutoSaveTextField extends JTextField implements ActionListener, FocusListener {
    AutoSaveTextField(final Optional<String> text, final int columns) {
        super(text.isPresent() ? text.get() : "", columns);
        addActionListener(this);
        addFocusListener(this);
    }

    abstract void autoSave();

    @Override
    public void actionPerformed(ActionEvent e) {
        autoSave();
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        autoSave();
    }
}
