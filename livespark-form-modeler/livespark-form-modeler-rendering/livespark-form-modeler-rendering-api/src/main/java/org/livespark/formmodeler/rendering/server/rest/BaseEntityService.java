package org.livespark.formmodeler.rendering.server.rest;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.livespark.formmodeler.rendering.client.shared.FormModel;

public abstract class BaseEntityService {

    @PersistenceContext
    protected EntityManager em;

    @Inject
    protected CriteriaBuilder builder;

    public <F extends FormModel> void createFromFormModel( F model ) {
        for ( Object dataModel : model.getDataModels() ) {
            create( dataModel );
        }
    }

    public <E> void create( E entity ) {
        em.persist( entity );
    }

    public <F extends FormModel> void deleteFromFormModel( F model ) {
        for ( Object dataModel : model.getDataModels() ) {
            delete( dataModel );
        }
    }

    // TODO this should use an identifier
    public <E> void delete( E entity ) {
        em.remove( entity );
    }

    public <F extends FormModel> void updateFromFormModel( F model ) {
        for ( Object dataModel : model.getDataModels() ) {
            update( dataModel );
        }
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
