package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import javax.swing.event.ListDataListener;

/**
 * Implements a ComboBoxModel which uses an underlying ComboBoxModel for the elements,
 * but does not change its selected item.
 *
 * An example use would be in a dialog with cancel and apply buttons, so that the selection change is only applied if
 * the apply button is pushed, but not if the cancel button is pushed.
 */
public class TemporaryProjectListModel implements ComboBoxModel<String> {

    private final ComboBoxModel<String> comboBoxModel;
    private Object selectedItem = null;

    public TemporaryProjectListModel(final ComboBoxModel<String> comboBoxModel) {
        this.comboBoxModel = comboBoxModel;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedItem = anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return comboBoxModel.getSize();
    }

    @Override
    public String getElementAt(int index) {
        return comboBoxModel.getElementAt(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        comboBoxModel.addListDataListener(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        comboBoxModel.removeListDataListener(l);
    }
}
