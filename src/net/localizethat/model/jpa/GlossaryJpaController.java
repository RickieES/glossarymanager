/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.localizethat.model.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import net.localizethat.model.Glossary;
import net.localizethat.model.GlsEntry;
import net.localizethat.model.L10n;
import net.localizethat.model.jpa.exceptions.IllegalOrphanException;
import net.localizethat.model.jpa.exceptions.NonexistentEntityException;
import net.localizethat.model.jpa.exceptions.PreexistingEntityException;

/**
 *
 * @author rpalomares
 */
public class GlossaryJpaController implements Serializable {
    private EntityManagerFactory emf = null;

    public GlossaryJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Glossary glossary) throws PreexistingEntityException, Exception {
        if (glossary.getGlsEntryCollection() == null) {
            glossary.setGlsEntryCollection(new ArrayList<GlsEntry>(Glossary.GLS_INITIAL_SIZE));
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            L10n l10nId = glossary.getL10nId();
            if (l10nId != null) {
                l10nId = em.getReference(l10nId.getClass(), l10nId.getId());
                glossary.setL10nId(l10nId);
            }
            Collection<GlsEntry> attachedGlsEntryCollection = new ArrayList<>(Glossary.GLS_INITIAL_SIZE);
            for (GlsEntry glsEntryCollectionGlsEntryToAttach : glossary.getGlsEntryCollection()) {
                glsEntryCollectionGlsEntryToAttach = em.getReference(glsEntryCollectionGlsEntryToAttach.getClass(),
                        glsEntryCollectionGlsEntryToAttach.getId());
                attachedGlsEntryCollection.add(glsEntryCollectionGlsEntryToAttach);
            }
            glossary.setGlsEntryCollection(attachedGlsEntryCollection);
            em.persist(glossary);
            for (GlsEntry glsEntryCollectionGlsEntry : glossary.getGlsEntryCollection()) {
                Glossary oldGlosIdOfGlsEntryCollectionGlsEntry = glsEntryCollectionGlsEntry.getGlosId();
                glsEntryCollectionGlsEntry.setGlosId(glossary);
                glsEntryCollectionGlsEntry = em.merge(glsEntryCollectionGlsEntry);
                if (oldGlosIdOfGlsEntryCollectionGlsEntry != null) {
                    oldGlosIdOfGlsEntryCollectionGlsEntry.getGlsEntryCollection().remove(glsEntryCollectionGlsEntry);
                    oldGlosIdOfGlsEntryCollectionGlsEntry = em.merge(oldGlosIdOfGlsEntryCollectionGlsEntry);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findGlossary(glossary.getId()) != null) {
                throw new PreexistingEntityException("Glossary " + glossary + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Glossary glossary) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Glossary persistentGlossary = em.find(Glossary.class, glossary.getId());
            L10n l10nIdOld = persistentGlossary.getL10nId();
            L10n l10nIdNew = glossary.getL10nId();
            Collection<GlsEntry> glsEntryCollectionOld = persistentGlossary.getGlsEntryCollection();
            Collection<GlsEntry> glsEntryCollectionNew = glossary.getGlsEntryCollection();
            List<String> illegalOrphanMessages = null;
            for (GlsEntry glsEntryCollectionOldGlsEntry : glsEntryCollectionOld) {
                if (!glsEntryCollectionNew.contains(glsEntryCollectionOldGlsEntry)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<>(5);
                    }
                    illegalOrphanMessages.add("You must retain GlsEntry " + glsEntryCollectionOldGlsEntry + " since its glosId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (l10nIdNew != null) {
                l10nIdNew = em.getReference(l10nIdNew.getClass(), l10nIdNew.getId());
                glossary.setL10nId(l10nIdNew);
            }
            Collection<GlsEntry> attachedGlsEntryCollectionNew = new ArrayList<>(Glossary.GLS_INITIAL_SIZE);
            for (GlsEntry glsEntryCollectionNewGlsEntryToAttach : glsEntryCollectionNew) {
                glsEntryCollectionNewGlsEntryToAttach = em.getReference(glsEntryCollectionNewGlsEntryToAttach.getClass(),
                        glsEntryCollectionNewGlsEntryToAttach.getId());
                attachedGlsEntryCollectionNew.add(glsEntryCollectionNewGlsEntryToAttach);
            }
            glsEntryCollectionNew = attachedGlsEntryCollectionNew;
            glossary.setGlsEntryCollection(glsEntryCollectionNew);
            glossary = em.merge(glossary);
            for (GlsEntry glsEntryCollectionNewGlsEntry : glsEntryCollectionNew) {
                if (!glsEntryCollectionOld.contains(glsEntryCollectionNewGlsEntry)) {
                    Glossary oldGlosIdOfGlsEntryCollectionNewGlsEntry = glsEntryCollectionNewGlsEntry.getGlosId();
                    glsEntryCollectionNewGlsEntry.setGlosId(glossary);
                    glsEntryCollectionNewGlsEntry = em.merge(glsEntryCollectionNewGlsEntry);
                    if (oldGlosIdOfGlsEntryCollectionNewGlsEntry != null
                            && !oldGlosIdOfGlsEntryCollectionNewGlsEntry.equals(glossary)) {
                        oldGlosIdOfGlsEntryCollectionNewGlsEntry.getGlsEntryCollection().remove(
                                glsEntryCollectionNewGlsEntry);
                        oldGlosIdOfGlsEntryCollectionNewGlsEntry = em.merge(oldGlosIdOfGlsEntryCollectionNewGlsEntry);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (IllegalOrphanException ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = glossary.getId();
                if (findGlossary(id) == null) {
                    throw new NonexistentEntityException("The glossary with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Glossary glossary;
            try {
                glossary = em.getReference(Glossary.class, id);
                glossary.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The glossary with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<GlsEntry> glsEntryCollectionOrphanCheck = glossary.getGlsEntryCollection();
            for (GlsEntry glsEntryCollectionOrphanCheckGlsEntry : glsEntryCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<>(Glossary.GLS_INITIAL_SIZE);
                }
                illegalOrphanMessages.add("This Glossary (" + glossary + ") cannot be destroyed since the GlsEntry "
                        + glsEntryCollectionOrphanCheckGlsEntry
                        + " in its glsEntryCollection field has a non-nullable glosId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(glossary);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Glossary> findGlossaryEntities() {
        return findGlossaryEntities(true, -1, -1);
    }

    public List<Glossary> findGlossaryEntities(int maxResults, int firstResult) {
        return findGlossaryEntities(false, maxResults, firstResult);
    }

    private List<Glossary> findGlossaryEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Glossary.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Glossary findGlossary(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Glossary.class, id);
        } finally {
            em.close();
        }
    }

    public int getGlossaryCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Glossary> rt = cq.from(Glossary.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
