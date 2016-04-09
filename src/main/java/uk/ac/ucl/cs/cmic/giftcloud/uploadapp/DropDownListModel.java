/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;

abstract class DropDownListModel extends DefaultComboBoxModel<String> implements ListDataListener {

    // We prevent changing of the default value until the model has been populated
    private boolean preventSettingLastUsedValue = true;
    private boolean enabled = false;

    public DropDownListModel() {
        addListDataListener(this);
    }

    abstract void setLastUsedValue(final String newValue);
    abstract Optional<String> getLastUsedValue();

    /* Indicates that the items in the model are no longer valid, but we don't yet have replacement values. So clear the items, and prevent any further actions until new items have been set */
    final public void invalidate() {
        preventSettingLastUsedValue = true;
        setEnabledStatus(false);
        removeAllElements();
    }

    final public void setItems(final List<String> objectList) {

        preventSettingLastUsedValue = true;

        removeAllElements();

        for (final String object : objectList) {
            addElement(object);
        }

        Optional<String> lastUsedValue = getLastUsedValue();
        if (lastUsedValue.isPresent() && objectList.contains(lastUsedValue.get())) {
            setSelectedItem(lastUsedValue.get());
        }
        preventSettingLastUsedValue = false;
        setEnabledStatus(true);
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

    public boolean isEnabled() {
        return enabled;
    }

    private void setEnabledStatus(final boolean newEnabled) {
        if (newEnabled != enabled) {
            enabled = newEnabled;
            notifyStatusChanged(newEnabled);
        }
    }


    private final java.util.List<EnabledListener<Boolean>> listeners = new ArrayList<EnabledListener<Boolean>>();

    public synchronized void addListener(final EnabledListener<Boolean> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(final EnabledListener<Boolean> listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public interface EnabledListener<Boolean> {
        void statusChanged(final Boolean visibility);
    }

    protected synchronized void notifyStatusChanged(final Boolean status) {
        for (final EnabledListener<Boolean> listener : listeners) {
            listener.statusChanged(status);
        }
    }
}
