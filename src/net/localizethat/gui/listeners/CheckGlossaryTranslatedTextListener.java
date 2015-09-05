/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.localizethat.gui.listeners;

import javax.persistence.EntityManager;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import net.localizethat.model.Glossary;
import net.localizethat.model.L10n;
import net.localizethat.tasks.CheckGlossaryWorker;


public class CheckGlossaryTranslatedTextListener extends AbstractSimpleDocumentListener {
    private CheckGlossaryWorker cgw;
    private String original;
    private final JTextArea translatedTextArea;
    private L10n locale;
    private final EntityManager em;
    private final JTextPane origStrPane;
    private final JPanel resultsPanel;
    Glossary[] glsList;

    public CheckGlossaryTranslatedTextListener(String original, JTextArea translatedTextArea,
            L10n locale, EntityManager em, JTextPane origStrPane, JPanel resultsPanel, Glossary... glsList) {
        this.original = original;
        this.translatedTextArea = translatedTextArea;
        this.locale = locale;
        this.em = em;
        this.origStrPane = origStrPane;
        this.resultsPanel = resultsPanel;
        this.glsList = glsList;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public void setLocale(L10n locale) {
        this.locale = locale;
    }

    @Override
    protected void doTask(DocumentEvent e) {
        if ((cgw == null) || (cgw.isDone())) {
            cleanResultsPanel();
            cgw = new CheckGlossaryWorker(original, translatedTextArea.getText(),
                    locale, em, origStrPane, resultsPanel, glsList);
            cgw.execute();
        }
    }

    private void cleanResultsPanel() {
        while (resultsPanel.getComponents().length > 0) {
            resultsPanel.remove(0);
        }
        resultsPanel.validate();
        resultsPanel.repaint();
    }
}
