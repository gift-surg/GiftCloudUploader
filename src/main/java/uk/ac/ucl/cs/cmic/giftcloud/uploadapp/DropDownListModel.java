/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.Optional;
import java.util.Vector;

abstract class DropDownListModel extends DefaultComboBoxModel implements ListDataListener {

    // We prevent changing of the default value until the model has been populated
    private boolean preventSettingLastUsedValue = true;

    public DropDownListModel() {
        addListDataListener(this);
    }

    abstract void setLastUsedValue(final String newValue);
    abstract Optional<String> getLastUsedValue();

    /* Indicates that the items in the model are no longer valid, but we don't yet have replacement values. So clear the items, and prevent any further actions until new items have been set */
    final public void invalidate() {
        preventSettingLastUsedValue = true;
        removeAllElements();
    }

    final public void setItems(final Vector<Object> objectList) {

        preventSettingLastUsedValue = true;

        removeAllElements();

        for (Object object : objectList) {
            addElement(object);
        }

        Optional<String> lastUsedValue = getLastUsedValue();
        if (lastUsedValue.isPresent() && objectList.contains(lastUsedValue.get())) {
            setSelectedItem(lastUsedValue.get());
        }
        preventSettingLastUsedValue = false;
    }

    @Override
    final public void intervalAdded(ListDataEvent e) {
    }

    @Override
    final public void intervalRemoved(ListDataEvent e) {
    }

    @Override
    final public void contentsChanged(ListDataEvent e) {
        if (!preventSettingLastUsedValue) {
            setLastUsedValue((String) getSelectedItem());
        }
    }
}
