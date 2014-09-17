/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.localizethat.glossarymanager.model.jpa;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import net.localizethat.glossarymanager.model.L10n;
import net.localizethat.glossarymanager.model.jpa.exceptions.IllegalOrphanException;
import net.localizethat.glossarymanager.model.jpa.exceptions.NonexistentEntityException;
import net.localizethat.glossarymanager.model.jpa.exceptions.PreexistingEntityException;

/**
 *
 * @author rpalomares
 */
public class L10nJpaController implements Serializable {
    private EntityManagerFactory emf = null;

    public L10nJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(L10n l10n) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(l10n);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findL10n(l10n.getId()) != null) {
                throw new PreexistingEntityException("L10n " + l10n + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(L10n l10n) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = l10n.getId();
                if (findL10n(id) == null) {
                    throw new NonexistentEntityException("The l10n with id " + id + " no longer exists.");
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
            L10n l10n;
            try {
                l10n = em.getReference(L10n.class, id);
                l10n.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The l10n with id " + id + " no longer exists.", enfe);
            }
            em.remove(l10n);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<L10n> findL10nEntities() {
        return findL10nEntities(true, -1, -1);
    }

    public List<L10n> findL10nEntities(int maxResults, int firstResult) {
        return findL10nEntities(false, maxResults, firstResult);
    }

    private List<L10n> findL10nEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(L10n.class));
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

    public L10n findL10n(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(L10n.class, id);
        } finally {
            em.close();
        }
    }

    public int getL10nCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<L10n> rt = cq.from(L10n.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
