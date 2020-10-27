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
 */

import * as React from "react";
import { useEffect, useState } from "react";
import { ComponentMessage } from "./DashbuilderTypes";
import { Logo, LogoProps } from "./Logo";

const DEFAULT_SRC = "./images/dashbuilder-logo.png";
const SRC_PROP = "src";
const WIDTH_PROP = "width";
const HEIGHT_PROP = "height";

export function App() {
  const [logoProps, setLogoProps] = useState<LogoProps>({
    src: DEFAULT_SRC
  });

  const handleMessage = (e: MessageEvent) => {
    const message = e.data as ComponentMessage;
    if (message.type === "INIT") {
      const componentProps = message.properties || new Map<string, any>();
      setLogoProps({
        src: (componentProps.get(SRC_PROP) as string) || DEFAULT_SRC,
        width: componentProps.get(WIDTH_PROP) as string,
        height: componentProps.get(HEIGHT_PROP) as string
      });
    }
  };

  useEffect(() => {
    window.addEventListener("message", handleMessage, false);
    return () => {
      window.removeEventListener("message", handleMessage, false);
    };
  });
  return <Logo {...logoProps} />;
}
