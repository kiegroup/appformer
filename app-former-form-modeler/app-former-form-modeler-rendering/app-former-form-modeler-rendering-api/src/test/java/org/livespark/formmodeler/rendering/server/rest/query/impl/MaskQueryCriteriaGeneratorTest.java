/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.rendering.server.rest.query.impl;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.livespark.formmodeler.rendering.client.shared.query.MaskQueryCriteria;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class MaskQueryCriteriaGeneratorTest extends TestCase {
    public static final String SHORT_MASK = "{name}: {price}";
    public static final String LONG_MASK = "ID:{id}\nNAME:{name}\nDESCRIPTION:{description}\nPRICE:{price}";
    public static final String WRONG_MASK = "{{id}{otherwrongstuffhere}{{{}}";

    protected CriteriaBuilder builder;
    protected Root rootEntity;
    protected Path path;
    protected Expression resultExpression;

    protected MaskQueryCriteriaGenerator generator;

    @Before
    public void setup() {

        builder = mock( CriteriaBuilder.class );

        when( builder.concat( any( Expression.class ), any( Expression.class ) ) ).thenReturn( mock( Expression.class ) );
        when( builder.concat( anyString(), any( Expression.class ) ) ).thenReturn( mock( Expression.class ) );
        when( builder.like( any( Expression.class ), anyString() ) ).thenReturn( mock( Predicate.class ) );

        rootEntity = mock( Root.class );

        path = mock( Path.class );

        resultExpression = mock( Expression.class );

        when( path.as( String.class ) ).thenReturn( resultExpression );

        when( rootEntity.get( anyString() ) ).thenReturn( path );

        generator = new MaskQueryCriteriaGenerator();
    }

    @Test( expected = IllegalArgumentException.class )
    public void testWrongMask() {
        generator.buildCriteriaExpression( new MaskQueryCriteria( WRONG_MASK, "" ), builder, rootEntity );
    }

    @Test( expected = NullPointerException.class )
    public void testNullMask() {
        generator.buildCriteriaExpression( new MaskQueryCriteria( null, "" ), builder, rootEntity );
    }

    @Test
    public void testMaskWithEmptyValue() {
        Expression expression = generator.buildCriteriaExpression( new MaskQueryCriteria( SHORT_MASK, "" ), builder, rootEntity );
        assertNull( "Expression should be null", expression );
    }

    @Test
    public void testShortMask() {
        Expression expression = generator.buildCriteriaExpression( new MaskQueryCriteria( SHORT_MASK, "test" ), builder, rootEntity );
        assertNotNull( "Expression cannot be null", expression );

        verify( rootEntity, times( 2 ) ).get( anyString() );
        verify( path, times( 2 ) ).as( String.class );

        verify( builder ).concat( anyString(), any( Expression.class ) );
        verify( builder ).concat( any( Expression.class ), any( Expression.class ) );

        verify( builder ).like( any( Expression.class ), anyString() );
    }

    @Test
    public void testLongMask() {
        Expression expression = generator.buildCriteriaExpression( new MaskQueryCriteria( LONG_MASK, "test" ), builder, rootEntity );
        assertNotNull( "Expression cannot be null", expression );

        verify( rootEntity, times( 4 ) ).get( anyString() );
        verify( path, times( 4 ) ).as( String.class );

        verify( builder, times( 4 ) ).concat( anyString(), any( Expression.class ) );
        verify( builder, times( 3 ) ).concat( any( Expression.class ), any( Expression.class ) );

        verify( builder ).like( any( Expression.class ), anyString() );
    }
}
