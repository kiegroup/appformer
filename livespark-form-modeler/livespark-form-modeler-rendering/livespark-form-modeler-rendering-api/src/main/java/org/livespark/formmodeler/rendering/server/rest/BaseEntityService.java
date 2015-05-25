package org.livespark.formmodeler.rendering.server.rest;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public abstract class BaseEntityService<E> {

    @PersistenceContext
    protected EntityManager em;

    @Inject
    protected CriteriaBuilder builder;

    protected abstract Class<E> getEntityServiceType();

    protected void create( E entity ) {
        em.persist( entity );
    }

    // TODO this should use an identifier
    public void delete( E entity ) {
        em.remove( entity );
    }

    public void updated( E entity ) {
        em.merge( entity );
    }

    public List<E> listAll() {
        CriteriaQuery<E> selectAllQuery = createSelectAllQuery( getEntityServiceType() );

        return em.createQuery( selectAllQuery ).getResultList();
    }

    private <T> CriteriaQuery<T> createSelectAllQuery( Class<T> entityType ) {
        CriteriaQuery<T> criteriaQuery = builder.createQuery( entityType );
        Root<T> rootEntity = criteriaQuery.from( entityType );

        return criteriaQuery.select( rootEntity );
    }

}
