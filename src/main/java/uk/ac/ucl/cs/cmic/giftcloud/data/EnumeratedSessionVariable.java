/*
 * EnumeratedSessionVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.data;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public final class EnumeratedSessionVariable
extends AbstractSessionVariable implements SessionVariable,ItemListener {
    private static final String MAKE_SELECTION = "(Select)";
    private static final String OTHER_ITEM = "Other...";
    private static final Collection<?> UNSELECTABLES = Arrays.asList(MAKE_SELECTION, OTHER_ITEM);
    private final Logger logger = LoggerFactory.getLogger(EnumeratedSessionVariable.class);
    private final JComboBox comboBox = new JComboBox();
    private Object lastDeselected = null;
    private SessionVariable shadowed;
    private Collection<?> items;
    private boolean requireSelection;
    private boolean allowOther;

    private EnumeratedSessionVariable(final String name,
            final String exportField,
            final Collection<?> items,
            final SessionVariable shadowed,
            boolean requireSelection,
            boolean allowOther) {
        super(name, exportField);
        this.shadowed = shadowed;
        this.requireSelection = requireSelection;
        this.allowOther = allowOther;
        setItems(items);
                }

    public EnumeratedSessionVariable(final String name,
            final String exportField,
            final Collection<?> items,
            boolean requireSelection,
            boolean allowOther) {
        this(name, exportField, items, null, requireSelection, allowOther);
    }

    /* (non-Javadoc)
     * @see SessionVariable#getEditor()
     */
    @Override
    public JComboBox getEditor() { return comboBox; }

    /* (non-Javadoc)
     * @see SessionVariable#getValue()
     */
    @Override
    public String getValue() {
        final Object selectedItem = comboBox.getSelectedItem();
        return MAKE_SELECTION == selectedItem ? null : selectedItem.toString();
    }

    /* (non-Javadoc)
     * @see SessionVariable#getValueMessage()
     */
    @Override
    public String getValueMessage() {
        final Object selectedItem = comboBox.getSelectedItem();
        if (MAKE_SELECTION == selectedItem) {
            return "Select a value for " + getName();
        } else {
            final String v = comboBox.getSelectedItem().toString();
            if (null == v || "".equals(v)) {
                return "Select a value for " + getName();
            } else {
                return null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see SessionVariable#isHidden()
     */
    @Override
    public boolean isHidden() { return false; }

    /*
     * (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    @Override
    public void itemStateChanged(final ItemEvent e) {
        if (MAKE_SELECTION == comboBox.getItemAt(0)) {
            comboBox.removeItem(MAKE_SELECTION);
        }
        if (ItemEvent.DESELECTED == e.getStateChange()) {
            lastDeselected = e.getItem();
            if (UNSELECTABLES.contains(lastDeselected)) {
                lastDeselected = null;
            }
        } else if (ItemEvent.SELECTED == e.getStateChange()) {
            if (OTHER_ITEM == e.getItem()) {
                final String val = JOptionPane.showInputDialog("Enter a value for " + getName() + ":");
                if (null == val) {
                    if (null == lastDeselected) {
                        comboBox.setSelectedIndex(0);
                    } else {
                        comboBox.setSelectedItem(lastDeselected);
                    }
                } else {
                    if (null == findItem(comboBox, val)) {
                        comboBox.addItem(val);
                        comboBox.removeItem(OTHER_ITEM);
                        comboBox.addItem(OTHER_ITEM);
                    }
                    comboBox.setSelectedItem(val);
                }
            }
        }
        fireHasChanged();
    }

    /* (non-Javadoc)
     * @see SessionVariable#refresh()
     */
    @Override
    public void refresh() {}

    /* (non-Javadoc)
     * @see SessionVariable#setValue(java.lang.String)
     */
    @Override
    public String setValue(final String value) {
        final Object old = comboBox.getSelectedItem();
        synchronized(this) {
            if (null == findItem(comboBox, value)) {
                comboBox.addItem(value);
            }
            comboBox.setSelectedItem(value);
        }
        try {
            shadowed.setValue(value);
        } catch (InvalidValueException ignore) {
            logger.error("new value failed validation on shadowed variable " + shadowed, ignore);
        }
        fireHasChanged();
        return null == old ? null : old.toString();
    }

    /**
     * This sets the list of items for the control and re-populates the list box accordingly.
     * @param items The items to display in the list box.
     */
    public void setItems(Collection<?> items) {
        // Only re-run the populateListBox() method if the new items list differs from the cached items list.
        // This allows control values to be more easily cached.
        if (this.items == null || !(this.items.containsAll(items) && items.containsAll(this.items))) {
            this.items = items;

            final String initial = null == shadowed ? null : shadowed.getValue();
            final List<Object> startItems = Lists.newArrayList();
            if (requireSelection) {
                startItems.add(MAKE_SELECTION);
                addValidator(new ValueValidator() {
                    public boolean isValid(final Object value) {
                        logger.trace("checking {} for validity", value);
                        return null != value;
                    }
                    public String getMessage(final Object value) {
                        return null == value ? "Select a value for " + getName() : null;
                    }
                });
            }
            startItems.addAll(items);
            if (allowOther) {
                startItems.add(OTHER_ITEM);
            }

            comboBox.removeItemListener(this);
            comboBox.removeAllItems();
            comboBox.setModel(new DefaultComboBoxModel(startItems.toArray()));
            comboBox.setEditable(false);
            comboBox.addItemListener(this);

            if (!Strings.isNullOrEmpty(initial)) {
                setValue(initial);
            }
        }
    }

    private Object findItem(final JComboBox cb, final Object o) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).equals(o)) {
                return cb.getItemAt(i);
            }
        }
        return null;
    }
}
