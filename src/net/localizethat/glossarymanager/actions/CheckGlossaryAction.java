/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.localizethat.glossarymanager.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import net.localizethat.glossarymanager.GlossaryManager;
import net.localizethat.glossarymanager.gui.CheckGlossaryTester;

/**
 * Opens a tab in the main window that allows to test strings against the glossary
 * @author rpalomares
 */
public class CheckGlossaryAction extends AbstractAction {
    private static final String TITLE = "Glossary Checker";
    private static final String DESCRIPTION = "Opens " + TITLE;
    private static final int MNEMONIC = java.awt.event.KeyEvent.VK_H;
    private CheckGlossaryTester checkGlsTester;

    /**
     * Action representing the launching of the Glossary tester panel as a tab in main window
     */
    public CheckGlossaryAction() {
        super(TITLE);
        putValue(SHORT_DESCRIPTION, DESCRIPTION);
        putValue(MNEMONIC_KEY, MNEMONIC);
        putValue(ACCELERATOR_KEY, javax.swing.KeyStroke.getKeyStroke(MNEMONIC, java.awt.event.InputEvent.CTRL_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // GlossaryManager.mainWindow.getStatusBar().setInfoText("Creating window, please wait...");
        if (checkGlsTester == null) {
            checkGlsTester = new CheckGlossaryTester();
        }
        GlossaryManager.mainWindow.addTab(checkGlsTester, TITLE);
    }

}
