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

package org.livespark.formmodeler.rendering.client.shared.query;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class MaskQueryCriteria implements QueryCriteria {

    private String mask;

    private String value;

    public MaskQueryCriteria() {
    }

    public MaskQueryCriteria( @MapsTo( "mask" ) String mask, @MapsTo( "value" ) String value ) {
        Assert.notNull( "Mask cannot be empty", mask );
        Assert.notNull( "Value cannot be null", value );
        this.mask = mask;
        this.value = value;
    }

    public String getMask() {
        return mask;
    }

    public void setMask( String mask ) {
        this.mask = mask;
    }

    public String getValue() {
        return value;
    }

    public void setValue( String value ) {
        this.value = value;
    }
}
