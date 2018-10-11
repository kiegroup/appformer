import * as React from "react";
import * as AppFormer from 'appformer-js';
import {Clock} from "./Clock";
import {Files} from "./Files";

alert("I'm alive!");

export class StaticReactComponent extends AppFormer.Screen {
    constructor() {
        super();
        this.isReact = true;
        this.af_componentId = "ReactComponent";
        this.af_componentTitle = "React component";
        this.af_subscriptions = {};
        this.af_componentService = {};
    }

    af_componentRoot(root?: { ss: AppFormer.Screen[]; ps: AppFormer.Perspective[] }): AppFormer.Element {
        return <div style={{padding: "10px"}}>
            <Clock/>
            <Files/>
        </div>;
    }

    af_onOpen(): void {
        console.info("ReactComponent is open.")
    }
}

export class FirstReactPerspective extends AppFormer.Perspective {

    constructor() {
        super();
        this.isReact = true;
        this.af_isDefault = false;
        this.af_componentId = "First React Perspective";
        this.af_perspectiveScreens = [];
    }

    af_perspectiveRoot(root?: { ss: AppFormer.Screen[]; ps: AppFormer.Perspective[] }): AppFormer.Element {
        return <div>This is a test perspective!</div>; // TODO create perspective layout here
    }

    af_onStartup(): void {
        alert("PERSPECTIVE STARTED!");
    }

    af_onOpen(): void {
        alert("PERSPECTIVE OPENED!");
    }

    af_onClose(): void {
        alert("PERSPECTIVE CLOSED!");
    }

    af_onShutdown(): void {
        alert("PERSPECTIVE SHUTDOWN!");
    }

}

AppFormer.register({StaticReactComponent, FirstReactPerspective});