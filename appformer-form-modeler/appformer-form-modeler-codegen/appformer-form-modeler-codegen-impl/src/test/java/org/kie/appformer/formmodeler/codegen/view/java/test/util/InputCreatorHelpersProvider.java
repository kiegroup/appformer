/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.appformer.formmodeler.codegen.view.java.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.CheckBoxHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.DatePickerHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.ListBoxHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.ObjectSelectorBoxHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.PictureHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.RadioGroupHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.SliderHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.TextAreaHelper;
import org.kie.appformer.formmodeler.codegen.view.impl.java.inputs.impl.TextBoxHelper;

public class InputCreatorHelpersProvider {

    public static List<InputCreatorHelper> getBasicInputHelpers() {
        List<InputCreatorHelper> helpers = new ArrayList<>();

        helpers.add(new TextBoxHelper());
        helpers.add(new CheckBoxHelper());
        helpers.add(new DatePickerHelper());
        helpers.add(new SliderHelper());
        helpers.add(new TextAreaHelper());

        return helpers;
    }

    public static List<InputCreatorHelper> getObjectSelectorHelpers() {
        return Arrays.asList(new ObjectSelectorBoxHelper());
    }

    public static List<InputCreatorHelper> getPictureHelpers() {
        return Arrays.asList(new PictureHelper());
    }

    public static List<InputCreatorHelper> getSelectorHelpers() {
        return Arrays.asList(new ListBoxHelper(),
                             new RadioGroupHelper());
    }

    public static List<InputCreatorHelper> getAllInputCreatorHelpers() {
        List result = new ArrayList();

        result.addAll(getBasicInputHelpers());
        result.addAll(getObjectSelectorHelpers());
        result.addAll(getPictureHelpers());
        result.addAll(getSelectorHelpers());

        return result;
    }
}
