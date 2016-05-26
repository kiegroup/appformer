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

package org.livespark.formmodeler.renderer.client.rendering.renderers.bs3.validator;

import org.gwtbootstrap3.client.ui.base.mixin.BlankValidatorMixin;
import org.gwtbootstrap3.client.ui.form.error.ErrorHandler;
import org.livespark.formmodeler.renderer.client.rendering.renderers.bs3.base.RadioGroupBase;

public class RadioGroupBlankValidatorMixin<W extends RadioGroupBase<V>, V> extends BlankValidatorMixin<W, V> {

    public RadioGroupBlankValidatorMixin( final W inputWidget, final ErrorHandler errorHandler ) {
        super(inputWidget, errorHandler);
    }

}
