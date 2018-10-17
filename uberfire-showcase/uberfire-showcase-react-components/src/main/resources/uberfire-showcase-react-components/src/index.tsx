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

    af_onClose(): void {
        console.info("ReactComponent is closed.")
    }
}

export class FirstReactPerspective extends AppFormer.Perspective {

    constructor() {
        super();
        this.isReact = true;
        this.af_isDefault = false;
        this.af_componentId = "FirstReactPerspective";
        this.af_perspectiveScreens = [];

        this.af_defaultPanelType = AppFormer.PanelType.MULTI_LIST;
        this.af_parts = FirstReactPerspective.parts();
        this.af_panels = FirstReactPerspective.panels();
    }

    private static parts() {
        const reactComponentPart = new AppFormer.Part();
        reactComponentPart.placeName = "ReactComponent";

        const welcomePart = new AppFormer.Part();
        welcomePart.placeName = "welcome";

        return [reactComponentPart, welcomePart];
    }

    private static panels() {

        // ----- West section
        const youtubeVideosPart = new AppFormer.Part();
        youtubeVideosPart.placeName = "YouTubeVideos";
        youtubeVideosPart.parameters = {
            "key1": "value1",
            "key2": "value2"
        };

        const panelWest = new AppFormer.Panel();
        panelWest.width = 250;
        panelWest.minWidth = 200;
        panelWest.position = AppFormer.CompassPosition.WEST;
        panelWest.panelType = AppFormer.PanelType.STATIC;
        panelWest.parts = [youtubeVideosPart];

        // ----- East section
        const readmePart = new AppFormer.Part();
        readmePart.placeName = "ReadmeScreen";

        const todoListPart = new AppFormer.Part();
        todoListPart.placeName = "TodoListScreen";

        const panelEast = new AppFormer.Panel();
        panelEast.width = 450;
        panelEast.position = AppFormer.CompassPosition.EAST;
        panelEast.panelType = AppFormer.PanelType.MULTI_LIST;
        panelEast.parts = [readmePart, todoListPart];

        // ----- South section
        const sampleEditorPart = new AppFormer.Part();
        sampleEditorPart.placeName = "SampleWorkbenchEditor";

        const youtubeScreenPart = new AppFormer.Part();
        youtubeScreenPart.placeName = "YouTubeScreen";

        const panelSouth = new AppFormer.Panel();
        panelSouth.height = 400;
        panelSouth.position = AppFormer.CompassPosition.SOUTH;
        panelSouth.panelType = AppFormer.PanelType.MULTI_LIST;
        panelSouth.parts = [sampleEditorPart, youtubeScreenPart];

        return [panelWest, panelEast, panelSouth];
    }

    af_perspectiveRoot(root?: { ss: AppFormer.Screen[]; ps: AppFormer.Perspective[] }): AppFormer.Element {
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