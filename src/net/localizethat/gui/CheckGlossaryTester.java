/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.localizethat.gui;

import java.beans.Beans;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import net.localizethat.Main;
import net.localizethat.gui.listeners.CheckGlossaryTranslatedTextListener;
import net.localizethat.model.Glossary;
import net.localizethat.model.L10n;
import net.localizethat.tasks.CheckGlossaryWorker;
import net.localizethat.util.gui.JStatusBar;

/**
 *
 * @author rpalomares
 */
public class CheckGlossaryTester extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private final JStatusBar statusBar;
    private final EntityManagerFactory emf;
    private final Glossary g;
    private final CheckGlossaryTranslatedTextListener cgttl;

    /**
     * Creates new form CheckGlossaryTester
     */
    public CheckGlossaryTester() {
        statusBar = Main.mainWindow.getStatusBar();
        emf = Main.emf;
        // The following code is executed inside initComponents()
        // entityManager = emf.createEntityManager();

        initComponents();
        if (!Beans.isDesignTime()) {
            entityManager.getTransaction().begin();
        }

        refreshL10nList();
        g = entityManager.find(Glossary.class, 1);
        cgttl = new CheckGlossaryTranslatedTextListener(origStrTextPane.getText(),
                this.trnsStrTextArea, (L10n) this.localeCombo.getSelectedItem(),
                entityManager, origStrTextPane, resultsPanel, g);
        trnsStrTextArea.getDocument().addDocumentListener(cgttl);
    }

    private void refreshL10nList() {
        TypedQuery<L10n> l10nQuery = entityManager.createNamedQuery("L10n.findAll",
                L10n.class);
        l10nComboModel.clearAll();
        l10nComboModel.addAll(l10nQuery.getResultList());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entityManager = emf.createEntityManager();
        l10nComboModel = new net.localizethat.gui.models.ListComboBoxGenericModel<L10n>();
        origStrLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        origStrTextPane = new javax.swing.JTextPane();
        trnsStrLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        trnsStrTextArea = new javax.swing.JTextArea();
        localeLabel = new javax.swing.JLabel();
        localeCombo = new javax.swing.JComboBox<L10n>();
        resultsPanel = new javax.swing.JPanel();
        testButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        origStrLabel.setText("Original string:");

        origStrTextPane.setText("<ul>\n  <li>Check the address for typing errors such as\n    <strong>ww</strong>.example.com instead of\n    <strong>www</strong>.example.com</li>\n  <li>If you are unable to load any pages, check your computer's network\n    connection.</li>\n  <li>If your computer or network is protected by a firewall or proxy, make sure\n    that &brandShortName; is permitted to access the Web.</li>\n</ul>");
        jScrollPane1.setViewportView(origStrTextPane);

        trnsStrLabel.setText("Translated string:");

        trnsStrTextArea.setColumns(20);
        trnsStrTextArea.setRows(5);
        trnsStrTextArea.setText("<ul>\n <li>Compruebe que la dirección no tiene errores de escritura del tipo <strong>ww</strong>.ejemplo.com en lugar de <strong>www</strong>.ejemplo.com</li>\n <li>Si no puede cargar ninguna página, compruebe la conexión de red de su ordenador.</li>\n <li>Si su ordenador o red están protegidos por un cortafuegos o proxy, asegúrese de que &brandShortName; tiene permiso para acceder a la web.</li>\n</ul>");
        trnsStrTextArea.setToolTipText("Type or paste the translation");
        jScrollPane2.setViewportView(trnsStrTextArea);

        localeLabel.setText("Locale:");

        localeCombo.setModel(l10nComboModel);
        localeCombo.addFocusListener(formListener);

        resultsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        resultsPanel.setToolTipText("Failed checks appear here");
        resultsPanel.setMinimumSize(new java.awt.Dimension(14, 101));

        testButton.setText("Test");
        testButton.addActionListener(formListener);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(testButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(trnsStrLabel)
                            .addComponent(localeLabel)
                            .addComponent(origStrLabel))
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE)
                            .addComponent(localeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(origStrLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(trnsStrLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(localeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.FocusListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == testButton) {
                CheckGlossaryTester.this.testButtonActionPerformed(evt);
            }
        }

        public void focusGained(java.awt.event.FocusEvent evt) {
        }

        public void focusLost(java.awt.event.FocusEvent evt) {
            if (evt.getSource() == localeCombo) {
                CheckGlossaryTester.this.localeComboFocusLost(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButtonActionPerformed
        cleanResultsPanel();
        CheckGlossaryWorker cgw = new CheckGlossaryWorker(origStrTextPane.getText(),
                trnsStrTextArea.getText(), (L10n) localeCombo.getSelectedItem(),
                entityManager, origStrTextPane, resultsPanel, g);
        cgw.execute();
    }//GEN-LAST:event_testButtonActionPerformed

    private void localeComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_localeComboFocusLost
        cgttl.setLocale(localeCombo.getItemAt(localeCombo.getSelectedIndex()));
    }//GEN-LAST:event_localeComboFocusLost

    private void cleanResultsPanel() {
        while (resultsPanel.getComponents().length > 0) {
            resultsPanel.remove(0);
        }
        resultsPanel.validate();
        resultsPanel.repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.persistence.EntityManager entityManager;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private net.localizethat.gui.models.ListComboBoxGenericModel<L10n> l10nComboModel;
    private javax.swing.JComboBox<L10n> localeCombo;
    private javax.swing.JLabel localeLabel;
    private javax.swing.JLabel origStrLabel;
    private javax.swing.JTextPane origStrTextPane;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JButton testButton;
    private javax.swing.JLabel trnsStrLabel;
    private javax.swing.JTextArea trnsStrTextArea;
    // End of variables declaration//GEN-END:variables
}