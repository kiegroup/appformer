/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.formmodeler.rendering.server.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.livespark.formmodeler.rendering.client.shared.query.QueryCriteria;
import org.livespark.formmodeler.rendering.server.rest.query.QueryCriteriaGenerator;

public abstract class BaseEntityService {

    @PersistenceContext
    protected EntityManager em;

    @Inject
    private Instance<QueryCriteriaGenerator<? extends QueryCriteria>> installedQueryCriteriaGenerators;

    protected Map<Class<? extends QueryCriteria>, QueryCriteriaGenerator> queryCriteriaGenerators = new HashMap<>();

    protected CriteriaBuilder builder;

    @PostConstruct
    private void init() {
        builder = em.getCriteriaBuilder();
        for( QueryCriteriaGenerator generator : installedQueryCriteriaGenerators ) {
            queryCriteriaGenerators.put( generator.getSupportedType(), generator );
        }
    }

    public <E> E create( final E entity ) {
        return em.merge( entity );
    }

    // TODO this should use an identifier
    public <E> void delete( final E entity ) {
        final E attachedEntity = em.merge( entity );
        em.remove( attachedEntity );
    }

    public <E> void update( final E entity ) {
        em.merge( entity );
    }

    public <E> List<E> listAll( final Class<E> type ) {
        final CriteriaQuery<E> selectAllQuery = createQuery( type, null );

        return em.createQuery( selectAllQuery ).getResultList();
    }

    public <E> List<E> list( final Class<E> type, QueryCriteria criteria ) {
        final CriteriaQuery<E> selectAllQuery = createQuery( type, criteria );

        return em.createQuery( selectAllQuery ).getResultList();
    }

    private <E> CriteriaQuery<E> createQuery( Class<E> entityType, QueryCriteria criteria ) {
        final CriteriaQuery<E> criteriaQuery = builder.createQuery( entityType );
        final Root<E> rootEntity = criteriaQuery.from( entityType );

        if ( criteria != null ) {
            QueryCriteriaGenerator generator = queryCriteriaGenerators.get( criteria.getClass() );

            if ( generator != null ) {
                Expression filter = generator.buildCriteriaExpression( criteria, builder, rootEntity );

                if ( filter != null ) {
                    criteriaQuery.where( filter );
                }
            }
        }

        return criteriaQuery.select( rootEntity );
    }
}
