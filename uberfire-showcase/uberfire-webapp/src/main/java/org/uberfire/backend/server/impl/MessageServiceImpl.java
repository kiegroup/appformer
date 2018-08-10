/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.backend.server.impl;

import javax.enterprise.context.Dependent;

import org.jboss.errai.bus.server.annotations.Service;
import org.uberfire.shared.MessageService;

@Service
@Dependent
public class MessageServiceImpl implements MessageService {

    @Override
    public void muteHello() {
        System.out.println("Shh! Hello..");
    }

    @Override
    public String hello() {
        return "Hello AppFormer.js! How are you?";
    }

    @Override
    public String hello(final String who) {
        return "Hello " + who + "! How are you?";
    }
}
