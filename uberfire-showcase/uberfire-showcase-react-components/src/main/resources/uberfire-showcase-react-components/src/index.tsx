import * as React from "react";
import * as AppFormer from 'appformer-js';
import {Clock} from "./Clock";
import {Files} from "./Files";

alert("I'm alive!");

export class StaticReactComponent extends AppFormer.Screen {
    constructor() {
        super("ReactComponent");
        this.af_isReact = true;
        this.af_componentTitle = "React component";
    }

    af_componentRoot(root?: { ss: AppFormer.Screen[]; ps: AppFormer.Perspective[] }): AppFormer.RootElement {
        return <div style={{padding: "10px"}}>
            <Clock/>
            <Files/>
        </div>;
    }

    af_onOpen(): void {
        console.info("ReactComponent is open.")
    }

    af_onClose(): void {
        console.info("ReactComponent is closed.")
    }
}

export class FirstReactPerspective extends AppFormer.Perspective {

    constructor() {
        super("FirstReactPerspective");
        this.af_isReact = true;
        this.af_parts = FirstReactPerspective.parts();
        this.af_panels = FirstReactPerspective.panels();
    }

    private static parts() {
        const reactComponentPart = new AppFormer.Part("ReactComponent");
        const welcomePart = new AppFormer.Part("welcome");
        return [reactComponentPart, welcomePart];
    }

    private static panels() {

        // ----- West section
        const youtubeVideosPart = new AppFormer.Part("YouTubeVideos");
        youtubeVideosPart.parameters = {
            "key1": "value1",
            "key2": "value2"
        };

        const panelWest = new AppFormer.Panel(AppFormer.CompassPosition.WEST);
        panelWest.width = 250;
        panelWest.minWidth = 200;
        panelWest.panelType = AppFormer.PanelType.STATIC;
        panelWest.parts = [youtubeVideosPart];

        // ----- East section
        const readmePart = new AppFormer.Part("ReadmeScreen");
        const todoListPart = new AppFormer.Part("TodoListScreen");

        const panelEast = new AppFormer.Panel(AppFormer.CompassPosition.EAST);
        panelEast.width = 450;
        panelEast.panelType = AppFormer.PanelType.MULTI_LIST;
        panelEast.parts = [readmePart, todoListPart];

        // ----- South section
        const sampleEditorPart = new AppFormer.Part("SampleWorkbenchEditor");
        const youtubeScreenPart = new AppFormer.Part("YouTubeScreen");

        const panelSouth = new AppFormer.Panel(AppFormer.CompassPosition.SOUTH);
        panelSouth.height = 400;
        panelSouth.panelType = AppFormer.PanelType.MULTI_LIST;
        panelSouth.parts = [sampleEditorPart, youtubeScreenPart];

        return [panelWest, panelEast, panelSouth];
    }

    af_perspectiveRoot(root?: { ss: AppFormer.Screen[]; ps: AppFormer.Perspective[] }): AppFormer.RootElement {
        return <div>This is a test perspective!</div>; // TODO create perspective layout here
    }

    af_onStartup(): void {
        console.info("React Perspective Started!");
    }

    af_onOpen(): void {
        console.info("React Perspective Opened!");
    }

    af_onClose(): void {
        console.info("React Perspective Closed!");
    }

    af_onShutdown(): void {
        console.info("React Perspective Shutdown!");
    }

}

AppFormer.register({StaticReactComponent, FirstReactPerspective});