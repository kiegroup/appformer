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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public abstract class BaseEntityService {

    @PersistenceContext
    protected EntityManager em;

    protected CriteriaBuilder builder;

    @PostConstruct
    private void init() {
        builder = em.getCriteriaBuilder();
    }

    public <E> void create( E entity ) {
        em.merge( entity );
    }

    // TODO this should use an identifier
    public <E> void delete( E entity ) {
        final E attachedEntity = em.merge( entity );
        em.remove( attachedEntity );
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
