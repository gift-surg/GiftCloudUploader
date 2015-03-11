/*
 * VariableAssignmentManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;

import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariableConsumer;
import uk.ac.ucl.cs.cmic.giftcloud.data.ValueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public final class VariableAssignmentManager {
    private static final String MESSAGE_SPACE = "                ";
    private static final String MESSAGE_INVALID = "INVALID";
    
    public final static GridBagConstraints labelConstraint = new GridBagConstraints();
    public final static GridBagConstraints valueConstraint = new GridBagConstraints();
    public final static GridBagConstraints messageConstraint = new GridBagConstraints();
    static {
        labelConstraint.gridx = 0;
        labelConstraint.weightx = 0.1;

        valueConstraint.gridx = 1;
        valueConstraint.fill = GridBagConstraints.HORIZONTAL;
        valueConstraint.weightx = 0.6;

        messageConstraint.gridx = 2;
        messageConstraint.fill = GridBagConstraints.HORIZONTAL;
        messageConstraint.weightx = 0.3;
    }

    private static final Logger logger = LoggerFactory.getLogger(VariableAssignmentManager.class);
    private final List<VariableRow> vms;

    public VariableAssignmentManager(final Container container,
            final Collection<SessionVariable> vars, final SessionVariableConsumer consumer) {
        this.vms = Lists.newArrayListWithExpectedSize(vars.size());
        for (final SessionVariable v : vars) {
            vms.add(new VariableRow(v, container, consumer));
        }
        logger.trace("managing variables {}", vars);
    }

    private final class VariableRow implements ValueListener {
        private final Logger logger = LoggerFactory.getLogger(VariableRow.class);
        private final SessionVariable v;
        private final SessionVariableConsumer consumer;
        private final JLabel message;

        VariableRow(final SessionVariable v,
                final Container gbc, final SessionVariableConsumer consumer) {
            this.v = v;
            this.consumer = consumer;

            message = new JLabel(MESSAGE_SPACE);
            if (!v.isHidden()) {
                gbc.add(new JLabel(v.getDescription()), labelConstraint);
                gbc.add(v.isMutable() ? v.getEditor() : new JLabel(v.getValue()), valueConstraint);
                gbc.add(message, messageConstraint);
            }

            v.addListener(this);
            v.refresh();

            try {
                v.validate(v.getValue());
                consumer.update(v, true);
            } catch (SessionVariable.InvalidValueException e) {
                consumer.update(v, false);
            }
        }

        /*
         * (non-Javadoc)
         * @see ValueListener#hasChanged(SessionVariable)
         */
        public void hasChanged(final SessionVariable v) {
            logger.trace("{} has changed", v);
            assert this.v.getName().equals(v.getName());
            this.message.setText(null);
            this.message.setToolTipText(null);
            final Container parent = this.message.getParent();
            if (null != parent) {
                parent.validate();
            }
            consumer.update(v, true);

            final Iterator<VariableRow> vi = vms.iterator();
            while (vi.hasNext()) {
                if (this.equals(vi.next())) {
                    break;
                }
            }

            while (vi.hasNext()) {
                vi.next().v.refresh();
            }
        }

        /*
         * (non-Javadoc)
         * @see ValueListener#isInvalid(SessionVariable, java.lang.Object, java.lang.String)
         */
        public void isInvalid(final SessionVariable variable, final Object value, final String message) {
            consumer.update(v, false);
            this.message.setText(MESSAGE_INVALID);
            this.message.setForeground(Color.RED);
            this.message.setToolTipText(message);
            this.message.getParent().validate();
        }
    }
}
