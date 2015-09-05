/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.localizethat.model.jpa;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import net.localizethat.model.GlsEntry;
import net.localizethat.model.GlsTranslation;
import net.localizethat.model.L10n;
import net.localizethat.model.jpa.exceptions.NonexistentEntityException;
import net.localizethat.model.jpa.exceptions.PreexistingEntityException;

/**
 *
 * @author rpalomares
 */
public class GlsTranslationJpaController implements Serializable {
    private EntityManagerFactory emf = null;

    public GlsTranslationJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(GlsTranslation glsTranslation) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            L10n l10nId = glsTranslation.getL10nId();
            if (l10nId != null) {
                l10nId = em.getReference(l10nId.getClass(), l10nId.getId());
                glsTranslation.setL10nId(l10nId);
            }
            GlsEntry glseId = glsTranslation.getGlseId();
            if (glseId != null) {
                glseId = em.getReference(glseId.getClass(), glseId.getId());
                glsTranslation.setGlseId(glseId);
            }
            em.persist(glsTranslation);
            if (glseId != null) {
                glseId.getGlsTranslationCollection().add(glsTranslation);
                glseId = em.merge(glseId);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findGlsTranslation(glsTranslation.getId()) != null) {
                throw new PreexistingEntityException("GlsTranslation " + glsTranslation + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(GlsTranslation glsTranslation) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            GlsTranslation persistentGlsTranslation = em.find(GlsTranslation.class, glsTranslation.getId());
            L10n l10nIdNew = glsTranslation.getL10nId();
            GlsEntry glseIdOld = persistentGlsTranslation.getGlseId();
            GlsEntry glseIdNew = glsTranslation.getGlseId();
            if (l10nIdNew != null) {
                l10nIdNew = em.getReference(l10nIdNew.getClass(), l10nIdNew.getId());
                glsTranslation.setL10nId(l10nIdNew);
            }
            if (glseIdNew != null) {
                glseIdNew = em.getReference(glseIdNew.getClass(), glseIdNew.getId());
                glsTranslation.setGlseId(glseIdNew);
            }
            glsTranslation = em.merge(glsTranslation);
            if (glseIdOld != null && !glseIdOld.equals(glseIdNew)) {
                glseIdOld.getGlsTranslationCollection().remove(glsTranslation);
                glseIdOld = em.merge(glseIdOld);
            }
            if (glseIdNew != null && !glseIdNew.equals(glseIdOld)) {
                glseIdNew.getGlsTranslationCollection().add(glsTranslation);
                glseIdNew = em.merge(glseIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = glsTranslation.getId();
                if (findGlsTranslation(id) == null) {
                    throw new NonexistentEntityException("The glsTranslation with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            GlsTranslation glsTranslation;
            try {
                glsTranslation = em.getReference(GlsTranslation.class, id);
                glsTranslation.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The glsTranslation with id " + id + " no longer exists.", enfe);
            }
            GlsEntry glseId = glsTranslation.getGlseId();
            if (glseId != null) {
                glseId.getGlsTranslationCollection().remove(glsTranslation);
                glseId = em.merge(glseId);
            }
            em.remove(glsTranslation);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<GlsTranslation> findGlsTranslationEntities() {
        return findGlsTranslationEntities(true, -1, -1);
    }

    public List<GlsTranslation> findGlsTranslationEntities(int maxResults, int firstResult) {
        return findGlsTranslationEntities(false, maxResults, firstResult);
    }

    private List<GlsTranslation> findGlsTranslationEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(GlsTranslation.class));
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

    public GlsTranslation findGlsTranslation(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(GlsTranslation.class, id);
        } finally {
            em.close();
        }
    }

    public int getGlsTranslationCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<GlsTranslation> rt = cq.from(GlsTranslation.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
