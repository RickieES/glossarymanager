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
import net.localizethat.model.GlsTranslation;
import net.localizethat.model.jpa.exceptions.IllegalOrphanException;
import net.localizethat.model.jpa.exceptions.NonexistentEntityException;
import net.localizethat.model.jpa.exceptions.PreexistingEntityException;

/**
 *
 * @author rpalomares
 */
public class GlsEntryJpaController implements Serializable {
    private EntityManagerFactory emf = null;

    public GlsEntryJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(GlsEntry glsEntry) throws PreexistingEntityException, Exception {
        if (glsEntry.getGlsTranslationCollection() == null) {
            glsEntry.setGlsTranslationCollection(new ArrayList<GlsTranslation>(GlsEntry.GLSTRNS_INITIAL_SIZE));
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Glossary glosId = glsEntry.getGlosId();
            if (glosId != null) {
                glosId = em.getReference(glosId.getClass(), glosId.getId());
                glsEntry.setGlosId(glosId);
            }
            Collection<GlsTranslation> attachedGlsTranslationCollection = new ArrayList<>(GlsEntry.GLSTRNS_INITIAL_SIZE);
            for (GlsTranslation glsTranslationCollectionGlsTranslationToAttach : glsEntry.getGlsTranslationCollection()) {
                glsTranslationCollectionGlsTranslationToAttach = em.getReference(glsTranslationCollectionGlsTranslationToAttach.getClass(), glsTranslationCollectionGlsTranslationToAttach.getId());
                attachedGlsTranslationCollection.add(glsTranslationCollectionGlsTranslationToAttach);
            }
            glsEntry.setGlsTranslationCollection(attachedGlsTranslationCollection);
            em.persist(glsEntry);
            if (glosId != null) {
                glosId.getGlsEntryCollection().add(glsEntry);
                glosId = em.merge(glosId);
            }
            for (GlsTranslation glsTranslationCollectionGlsTranslation : glsEntry.getGlsTranslationCollection()) {
                GlsEntry oldGlseIdOfGlsTranslationCollectionGlsTranslation = glsTranslationCollectionGlsTranslation.getGlseId();
                glsTranslationCollectionGlsTranslation.setGlseId(glsEntry);
                glsTranslationCollectionGlsTranslation = em.merge(glsTranslationCollectionGlsTranslation);
                if (oldGlseIdOfGlsTranslationCollectionGlsTranslation != null) {
                    oldGlseIdOfGlsTranslationCollectionGlsTranslation.getGlsTranslationCollection().remove(glsTranslationCollectionGlsTranslation);
                    oldGlseIdOfGlsTranslationCollectionGlsTranslation = em.merge(oldGlseIdOfGlsTranslationCollectionGlsTranslation);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findGlsEntry(glsEntry.getId()) != null) {
                throw new PreexistingEntityException("GlsEntry " + glsEntry + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(GlsEntry glsEntry) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            GlsEntry persistentGlsEntry = em.find(GlsEntry.class, glsEntry.getId());
            Glossary glosIdOld = persistentGlsEntry.getGlosId();
            Glossary glosIdNew = glsEntry.getGlosId();
            Collection<GlsTranslation> glsTranslationColOld = persistentGlsEntry.getGlsTranslationCollection();
            Collection<GlsTranslation> glsTranslationColNew = glsEntry.getGlsTranslationCollection();
            List<String> illegalOrphanMessages = null;
            for (GlsTranslation glsTrnsColOld : glsTranslationColOld) {
                if (!glsTranslationColNew.contains(glsTrnsColOld)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<>(1);
                    }
                    illegalOrphanMessages.add("You must retain GlsTranslation " + glsTrnsColOld
                            + " since its glseId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (glosIdNew != null) {
                glosIdNew = em.getReference(glosIdNew.getClass(), glosIdNew.getId());
                glsEntry.setGlosId(glosIdNew);
            }
            Collection<GlsTranslation> attachedGlsTrnsColNew = new ArrayList<>(GlsEntry.GLSTRNS_INITIAL_SIZE);
            for (GlsTranslation glsTranslationToAttach : glsTranslationColNew) {
                glsTranslationToAttach = em.getReference(glsTranslationToAttach.getClass(), glsTranslationToAttach.getId());
                attachedGlsTrnsColNew.add(glsTranslationToAttach);
            }
            glsTranslationColNew = attachedGlsTrnsColNew;
            glsEntry.setGlsTranslationCollection(glsTranslationColNew);
            glsEntry = em.merge(glsEntry);
            if (glosIdOld != null && !glosIdOld.equals(glosIdNew)) {
                glosIdOld.getGlsEntryCollection().remove(glsEntry);
                glosIdOld = em.merge(glosIdOld);
            }
            if (glosIdNew != null && !glosIdNew.equals(glosIdOld)) {
                glosIdNew.getGlsEntryCollection().add(glsEntry);
                glosIdNew = em.merge(glosIdNew);
            }
            for (GlsTranslation glsTranslationCollectionNewGlsTranslation : glsTranslationColNew) {
                if (!glsTranslationColOld.contains(glsTranslationCollectionNewGlsTranslation)) {
                    GlsEntry oldGlseIdOfGlsTranslationCollectionNewGlsTranslation = glsTranslationCollectionNewGlsTranslation.getGlseId();
                    glsTranslationCollectionNewGlsTranslation.setGlseId(glsEntry);
                    glsTranslationCollectionNewGlsTranslation = em.merge(glsTranslationCollectionNewGlsTranslation);
                    if (oldGlseIdOfGlsTranslationCollectionNewGlsTranslation != null && !oldGlseIdOfGlsTranslationCollectionNewGlsTranslation.equals(glsEntry)) {
                        oldGlseIdOfGlsTranslationCollectionNewGlsTranslation.getGlsTranslationCollection().remove(glsTranslationCollectionNewGlsTranslation);
                        oldGlseIdOfGlsTranslationCollectionNewGlsTranslation = em.merge(oldGlseIdOfGlsTranslationCollectionNewGlsTranslation);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = glsEntry.getId();
                if (findGlsEntry(id) == null) {
                    throw new NonexistentEntityException("The glsEntry with id " + id + " no longer exists.");
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
            GlsEntry glsEntry;
            try {
                glsEntry = em.getReference(GlsEntry.class, id);
                glsEntry.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The glsEntry with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<GlsTranslation> glsTranslationCollectionOrphanCheck = glsEntry.getGlsTranslationCollection();
            for (GlsTranslation glsTranslationCollectionOrphanCheckGlsTranslation : glsTranslationCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<>(1);
                }
                illegalOrphanMessages.add("This GlsEntry (" + glsEntry + ") cannot be destroyed since the GlsTranslation " + glsTranslationCollectionOrphanCheckGlsTranslation + " in its glsTranslationCollection field has a non-nullable glseId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Glossary glosId = glsEntry.getGlosId();
            if (glosId != null) {
                glosId.getGlsEntryCollection().remove(glsEntry);
                glosId = em.merge(glosId);
            }
            em.remove(glsEntry);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<GlsEntry> findGlsEntryEntities() {
        return findGlsEntryEntities(true, -1, -1);
    }

    public List<GlsEntry> findGlsEntryEntities(int maxResults, int firstResult) {
        return findGlsEntryEntities(false, maxResults, firstResult);
    }

    private List<GlsEntry> findGlsEntryEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(GlsEntry.class));
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

    public GlsEntry findGlsEntry(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(GlsEntry.class, id);
        } finally {
            em.close();
        }
    }

    public int getGlsEntryCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<GlsEntry> rt = cq.from(GlsEntry.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
