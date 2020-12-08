/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * */

import { DataSet } from "./dataset";
import { BrowserComponentBus } from "./controller/BrowserComponentBus";
import { DashbuilderComponentController } from "./controller/DashbuilderComponentController";
import { DashbuilderComponentDispatcher } from "./controller/DashbuilderComponentDispatcher";
import { ComponentController } from "./controller";

const bus = new BrowserComponentBus();
const controller = new DashbuilderComponentController(bus);
const listener = new DashbuilderComponentDispatcher(bus, controller);

listener.init();

export function getComponentController(
  onInit?: (params: Map<string, any>) => void,
  onDataSet?: (dataSet: DataSet, params?: Map<string, any>) => void
): ComponentController {
  if (onInit) {
    controller.setOnInit(onInit);
  }
  if (onDataSet) {
    controller.setOnDataSet(onDataSet);
  }
  return controller;
}

export function restart() {
  destroy();
  listener.init();
}

export function destroy() {
  listener.stop();
}
