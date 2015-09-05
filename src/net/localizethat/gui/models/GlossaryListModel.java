/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.localizethat.gui.models;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;
import net.localizethat.model.Glossary;

/**
 *
 * @author rpalomares
 */
public class GlossaryListModel extends AbstractListModel<Glossary> implements MutableComboBoxModel<Glossary> {
    List<Glossary> glossaryList;
    int selectComboItem = -1;

    public GlossaryListModel() {
        glossaryList = new ArrayList<>(5);
    }

    public GlossaryListModel(List<Glossary> listSource) {
        glossaryList = listSource;
    }

    /**
     * Implements ListModel missing method
     * @return the list size
     */
    @Override
    public int getSize() {
        return glossaryList.size();
    }

    /**
     * Implements ListModel missing method
     * @param index index of the element to display
     * @return the element
     */
    @Override
    public Glossary getElementAt(int index) {
        return glossaryList.get(index);
    }

    @Override
    public void addElement(Glossary item) {
        glossaryList.add(item);
        this.fireIntervalAdded(item, glossaryList.size() -1, glossaryList.size() -1);
    }

    @Override
    public void removeElement(Object obj) {
        Glossary item = (Glossary) obj;
        int index = glossaryList.indexOf(item);
        if (index != -1) {
            glossaryList.remove(index);
            this.fireIntervalRemoved(item, index, index);
        }
    }

    @Override
    public void insertElementAt(Glossary item, int index) {
        if (index >= 0 && index < glossaryList.size()) {
            glossaryList.add(index, item);
            this.fireIntervalAdded(item, index, index);
        }
    }

    @Override
    public void removeElementAt(int index) {
        if (index >= 0 && index < glossaryList.size()) {
            glossaryList.remove(index);
            this.fireIntervalRemoved(index, index, index);
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        Glossary item = (Glossary) anItem;
        int index = glossaryList.indexOf(item);
        if (index != -1) {
            this.selectComboItem = index;
            this.fireContentsChanged(item, index, index);
        } else {
            this.selectComboItem = -1;
        }
    }

    @Override
    public Object getSelectedItem() {
        if (this.selectComboItem != -1) {
            return glossaryList.get(selectComboItem);
        } else {
            return null;
        }
    }

    public void clearAll() {
        int end = Math.max(0, glossaryList.size() - 1);
        glossaryList.clear();
        fireIntervalRemoved(this, 0, end);
    }

    public void addAll(List<Glossary> lg) {
        int start = glossaryList.size();
        glossaryList.addAll(lg);
        int end = Math.max(0, glossaryList.size() - 1);
        fireIntervalAdded(this, start, end);
    }

    public Glossary findById(int id) {
        for(Glossary g : glossaryList) {
            if (g.getId() == id) {
                return g;
            }
        }
        return null;
    }
}
