/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.localizethat.glossarymanager.tasks;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import net.localizethat.glossarymanager.GlossaryManager;
import net.localizethat.glossarymanager.model.Glossary;
import net.localizethat.glossarymanager.model.GlsEntry;
import net.localizethat.glossarymanager.model.GlsTranslation;
import net.localizethat.glossarymanager.model.L10n;
import net.localizethat.util.NoCaseStringComparator;

/**
 * SwingWorker task to check two strings (one in the original language and the other in a
 * localized one) against terms of any number of glossaries
 * @author rpalomares
 */
public class CheckGlossaryWorker
        extends SwingWorker<List<CheckGlossaryWorker.FailedEntry>, Void> {
    String original;
    String translated;
    L10n locale;
    EntityManager em;
    JPanel resultsPanel;
    List<Glossary> glsToCheckList;
    List<CheckGlossaryWorker.FailedEntry> failedEntriesList;

    /**
     * Make default constructor private so the class can't be instantiated without
     * supplying needed parameters
     */
    private CheckGlossaryWorker() {
    }

    public CheckGlossaryWorker(String original, String translated, L10n locale,
            EntityManager em, JPanel resultsPanel, Glossary... glsList) {
        int glsLength = glsList.length;

        this.original = original;
        this.translated = translated;
        this.locale = locale;

        if (em == null) {
            this.em = GlossaryManager.emf.createEntityManager();
        } else {
            this.em = em;
        }

        this.resultsPanel = resultsPanel;

        if (glsLength > 0) {
            this.glsToCheckList = Arrays.asList(glsList);
        }
        failedEntriesList = new ArrayList<>(5);
    }

    @Override
    protected List<CheckGlossaryWorker.FailedEntry> doInBackground() throws Exception {
        long start = System.currentTimeMillis();
        long finish;
        List<String> originalWords;
        List<String> translatedWords;
        List<GlsEntry> entries;
        NoCaseStringComparator ncsComp = new NoCaseStringComparator();

        TypedQuery<GlsEntry> glseQuery = em.createNamedQuery("GlsEntry.findByGlsAndTerm",
                GlsEntry.class);
        TypedQuery<GlsEntry> glse2Query = em.createNamedQuery("GlsEntry.findByGlsAndTermLoCase",
                GlsEntry.class);

        originalWords = slicePhrase(original);
        translatedWords = slicePhrase(translated);
        Collections.sort(translatedWords, ncsComp);

        // Build a list of words from the original text present in glossaries
        Iterator<String> origWordsIt = originalWords.iterator();
        while (origWordsIt.hasNext()) {
            String word = origWordsIt.next();
            FailedEntry fe = new FailedEntry(word, true, null);

            glseQuery.setParameter("glseterm", word);
            glse2Query.setParameter("glseterm", word);

            for(Glossary g : glsToCheckList) {
                glseQuery.setParameter("glosid", g);
                entries = glseQuery.getResultList();

                if (entries.isEmpty()) {
                    fe.setMatchCase(false);
                    glse2Query.setParameter("glosid", g);
                    entries = glse2Query.getResultList();
                }

                for(GlsEntry ge : entries) {
                    fe.addGe(ge);
                }
            }

            if (fe.getGlsEntriesList().size() > 0) {
                // Just a "potential" failed entry at the moment
                failedEntriesList.add(fe);
            }
        }

        // For each original word, we must search if any of the possible translations is in the
        // list of translated words. If it is, we remove the "potentially failed entry" and the
        // translated word; otherwise, the "potentially" failed entry turns into real failed
        // entry, being kept in the list
        Iterator<FailedEntry> feIterator = failedEntriesList.iterator();
        while (feIterator.hasNext()) {
            FailedEntry fe = feIterator.next();
            boolean translationFound = false;

            for(GlsEntry ge : fe.getGlsEntriesList()) {
                for(GlsTranslation gt : ge.getGlsTranslationCollection()) {
                    int index;
                    if (fe.isMatchCase()) {
                        index = Collections.binarySearch(translatedWords, gt.getValue());
                    } else {
                        index = Collections.binarySearch(translatedWords, gt.getValue(), ncsComp);
                    }
                    if (index >= 0) {
                        translatedWords.remove(index);
                        translationFound = true;
                        break;
                    }
                }
                if (translationFound) {
                    break;
                }
            }
            if (translationFound) {
                feIterator.remove();
            }
        }

        finish = System.currentTimeMillis();
        Logger.getLogger(CSVImporterWorker.class.getName()).log(Level.INFO,
                "Check glossary exec time: {0} ms", (finish - start));
        return failedEntriesList;
    }

    @Override
    public void done() {
        if (failedEntriesList.isEmpty()) {
            JLabel allOkLabel = new JLabel("Everything seems OK");
            resultsPanel.add(allOkLabel);
        } else {
            for (FailedEntry fe : failedEntriesList) {
                StringBuilder sb = new StringBuilder(20);
                sb.append("<html>");
                for (GlsEntry ge : fe.getGlsEntriesList()) {
                    for (GlsTranslation gt : ge.getGlsTranslationCollection()) {
                        sb.append("<b>");
                        sb.append(gt.getValue());
                        sb.append("</b>");
                        sb.append(" (from ");
                        sb.append(ge.getGlosId().getName());
                        sb.append(")");
                        sb.append("<br>");
                    }
                }
                sb.append("</html>");
                JButton b = new JButton(fe.getWord());
                b.setToolTipText(sb.toString());
                b.setMargin(new Insets(5, 5, 5, 5));
                resultsPanel.add(b);
            }
        }
        resultsPanel.repaint();
        resultsPanel.updateUI();
    }

    private List<String> slicePhrase(String phrase) {
        List<String> words = new ArrayList<>(5);

        StringTokenizer st = new StringTokenizer(phrase);
        while (st.hasMoreTokens()) {
            words.add(st.nextToken());
        }

        return words;
    }

    public static class FailedEntry {
        private String word;
        private boolean matchCase;
        private List<GlsEntry> GlsEntriesList;

        public FailedEntry(String word, boolean matchCase, GlsEntry ge) {
            this.word = word;
            this.GlsEntriesList = new ArrayList<>(0);
            if (ge != null) {
                GlsEntriesList.add(ge);
            }
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public boolean isMatchCase() {
            return matchCase;
        }

        public void setMatchCase(boolean matchCase) {
            this.matchCase = matchCase;
        }

        public List<GlsEntry> getGlsEntriesList() {
            return GlsEntriesList;
        }

        public void addGe(GlsEntry ge) {
            if (ge != null) {
                GlsEntriesList.add(ge);
            }
        }

        public void clearLge() {
            GlsEntriesList = new ArrayList<>(0);
        }
    }

}
