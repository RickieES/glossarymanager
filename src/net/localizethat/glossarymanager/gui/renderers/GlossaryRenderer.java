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
import net.localizethat.glossarymanager.model.Glossary;

/**
 *
 * @author rpalomares
 */
public class GlossaryRenderer extends JLabel implements ListCellRenderer<Glossary> {

    @Override
    public Component getListCellRendererComponent(JList list, Glossary value, int index, boolean isSelected,
            boolean cellHasFocus) {

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        this.setText(value.getName().substring(0, 32));
        return this;
    }
}
