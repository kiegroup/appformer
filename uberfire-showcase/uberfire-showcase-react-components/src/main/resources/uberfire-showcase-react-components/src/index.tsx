import {VFSService} from "@kiegroup-ts-generated/uberfire-backend-api-rpc";

alert("I'm alive!");

import * as AppFormer from 'appformer-js';
import * as React from "react";

export class StaticReactComponent extends AppFormer.Screen {
    constructor() {
        super();
        this.isReact = true;
        this.af_componentId = "FirstComponent";
        this.af_componentTitle = "First component";
        this.af_subscriptions = {};
        this.af_componentService = {};
    }

    af_componentRoot(root?: { ss: AppFormer.Screen[]; ps: AppFormer.Perspective[] }): AppFormer.Element {
        return <div>This is a test!</div>;
    }

    af_onOpen(): void {
        alert("OPEN!!");
        new VFSService().get({uri: "default://uf-playground/todo.md"}).then(p => {
            console.info(p.getFileName());
        });
    }
}

AppFormer.register({StaticReactComponent});