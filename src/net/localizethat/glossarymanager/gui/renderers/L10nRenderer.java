/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.localizethat.glossarymanager.gui.renderers;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import net.localizethat.glossarymanager.model.L10n;

/**
 *
 * @author rpalomares
 */
public class L10nRenderer extends JLabel implements ListCellRenderer<L10n> {

    @Override
    public Component getListCellRendererComponent(JList list, L10n value, int index, boolean isSelected,
            boolean cellHasFocus) {

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        this.setText(value.getCode() + " - " + value.getName());
        return this;
    }
}
