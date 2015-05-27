package org.livespark.formmodeler.rendering.server.rest;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public abstract class BaseEntityService {

    @PersistenceContext
    protected EntityManager em;

    @Inject
    protected CriteriaBuilder builder;

    protected <E> void create( E entity ) {
        em.persist( entity );
    }

    // TODO this should use an identifier
    public <E> void delete( E entity ) {
        em.remove( entity );
    }

    public <E> void update( E entity ) {
        em.merge( entity );
    }

    public <E> List<E> listAll( Class<E> type ) {
        CriteriaQuery<E> selectAllQuery = createSelectAllQuery( type );

        return em.createQuery( selectAllQuery ).getResultList();
    }

    private <E> CriteriaQuery<E> createSelectAllQuery( Class<E> entityType ) {
        CriteriaQuery<E> criteriaQuery = builder.createQuery( entityType );
        Root<E> rootEntity = criteriaQuery.from( entityType );

        return criteriaQuery.select( rootEntity );
    }

}
