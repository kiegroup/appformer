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

package org.livespark.formmodeler.rendering.client.shared.converters;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DefaultConverter;

/**
 * Created by pefernan on 6/12/15.
 */

@DefaultConverter
public class CharacterConverter implements Converter<Character, String> {

    @Override
    public Class<Character> getModelType() {
        return Character.class;
    }

    @Override
    public Class<String> getWidgetType() {
        return String.class;
    }

    @Override
    public Character toModelValue( String s ) {
        if (s == null) return null;
        return new Character( s.charAt( 0 ) );
    }

    @Override
    public String toWidgetValue( Character character ) {
        if (character == null) return null;
        return character.toString();
    }
}
