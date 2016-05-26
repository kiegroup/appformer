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

package org.livespark.formmodeler.model.impl.basic;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.livespark.formmodeler.metaModel.FieldDef;
import org.livespark.formmodeler.model.FieldDefinition;

/**
 * @author Pere Fernandez <pefernan@redhat.com>
 */
@Portable
@Bindable
public class SliderFieldDefinition extends FieldDefinition {

    public static final String CODE = "Slider";

    private String code = CODE;

    @FieldDef( label = "Min. Value" )
    private Double min = new Double( 0.0 );

    @FieldDef( label = "Max. Value" )
    private Double max = new Double( 50.0 );

    @FieldDef( label = "Precision" )
    private Double precision = new Double( 1.0 );

    @FieldDef( label = "Step")
    private Double step = new Double( 1.0 );

    @Override
    public String getCode() {
        return code;
    }

    public Double getMin() {
        return min;
    }

    public void setMin( Double min ) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax( Double max ) {
        this.max = max;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision( Double precision ) {
        this.precision = precision;
    }

    public double getStep() {
        return step;
    }

    public void setStep( double step ) {
        this.step = step;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[]{ Double.class.getName() };
    }

    @Override
    protected void doCopyFrom( FieldDefinition other ) {
        if ( other instanceof SliderFieldDefinition ) {
            SliderFieldDefinition otherSlider = (SliderFieldDefinition) other;
            min = otherSlider.getMin();
            max = otherSlider.getMax();
            precision = otherSlider.getPrecision();
            step = otherSlider.getStep();
        }
    }
}
