import * as React from "react";
import * as AppFormer from 'appformer-js';
import * as AppFormerEditors from 'appformer-js-editors';
import {Clock} from "./Clock";
import {MarkdownFile} from "./MarkdownFile";
import {TemplatedPanel} from "./TemplatedPanel";
import {ObservablePath} from "uberfire-api-ts-decorators";
import {PlaceRequest} from "@kiegroup-ts-generated/uberfire-api";

export class SimpleReactComponent extends AppFormer.Screen {
    constructor() {
        super("ReactComponent");
        this.af_isReact = true;
        this.af_componentTitle = "React component";
    }

    private openREADME() {
        (window as any).appformerGwtBridge.goToPath("default://master@uf-playground/README.md");
    }

    private openTodo() {
        (window as any).appformerGwtBridge.goToPath("default://master@uf-playground/todo.md");
    }

    af_componentRoot(): AppFormer.Element {
        return <div style={{padding: "10px"}}>
            <h1>
                This is a very simple React component.
                <h6>It features a Clock and links to a Markdown Editor.</h6>
            </h1>

            <h3 style={{fontWeight: "bold"}}>Clock:</h3>
            <Clock/>

            <h3 style={{fontWeight: "bold"}}>Links:</h3>
            <button className={"btn btn-primary btn-sm"} onClick={() => this.openREADME()}>README.md</button>
            <button className={"btn btn-primary btn-sm"} onClick={() => this.openTodo()}>todo.md</button>
        </div>;
    }

    af_onOpen(): void {
        console.info("ReactComponent is open.")
    }

    af_onClose(): void {
        console.info("ReactComponent is closed.")
    }
}

export class CompassLayoutJsPerspective extends AppFormer.CompassLayoutPerspective {

    constructor() {
        super("CompassLayoutJsPerspective");
        this.af_isTransient = false;
        this.af_name = "JS Compass Layout Perspective";

        this.af_parts = this.parts();
        this.af_panels = this.panels();
    }

    private parts() {
        const reactComponentPart = new AppFormer.Part("ReactComponent");
        const welcomePart = new AppFormer.Part("welcome");
        return [reactComponentPart, welcomePart];
    }

    private panels() {

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

export class ReactTemplatedJsPerspective extends AppFormer.Perspective {

    constructor() {
        super("ReactTemplatedJsPerspective");
        this.af_isReact = true;
        this.af_isTransient = true;
        this.af_name = "JS React Templated Perspective";
    }

    af_componentRoot(children?: any): AppFormer.Element {
        return (
            <div className="fluid-container">
                <div style={{height: "5%", paddingTop: "10px"}} className="text-center">
                    <span className="lead">React templated Perspective</span>
                </div>
                <div style={{height: "95%"}}>
                    <div className="fluid-row" style={{height: "48%"}}>
                        <div className="col-md-4">
                            <TemplatedPanel title={"React Component"}>
                                <div af-js-component={"ReactComponent"} data-startup-foo1="bar1" data-startup-foo2="bar2"/>
                            </TemplatedPanel>
                        </div>
                        <div className="col-md-4">
                            <TemplatedPanel title={"Welcome"}>
                                <div af-js-component={"welcome"}/>
                            </TemplatedPanel>
                        </div>
                        <div className="col-md-4">
                            <TemplatedPanel title={"Readme"}>
                                <div af-js-component={"ReadmeScreen"}/>
                            </TemplatedPanel>
                        </div>
                    </div>
                    <div style={{height: "2%"}}/>
                    <div className="fluid-row" style={{height: "48%"}}>
                        <div className="col-md-4">
                            <TemplatedPanel title={"Plugins Explorer"}>
                                <div af-js-component={"Plugins Explorer"}/>
                            </TemplatedPanel>
                        </div>
                        <div className="col-md-4">
                            <TemplatedPanel title={"Todo List"}>
                                <div af-js-component={"TodoListScreen"}/>
                            </TemplatedPanel>
                        </div>
                        <div className="col-md-4">
                            <TemplatedPanel title={"GitHub Commit Stats"}>
                                <div af-js-component={"GitHubCommitDaysStats"}/>
                            </TemplatedPanel>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

}

export class StringTemplatedJsPerspective extends AppFormer.Perspective {

    constructor() {
        super("StringTemplatedJsPerspective");
        this.af_isReact = false;
        this.af_isTransient = true;
        this.af_name = "JS String Templated Perspective";
    }

    af_componentRoot(children?: any): string {
        return `
            <div class="fluid-container">
                <div style="height: 5%; padding-top: 10px;" class="text-center">
                    <span class="lead">HTML-templated Perspective</span>
                </div>
                <div style="height: 95%">
                    <div class="fluid-row" style="height: 48%">
                        <div class="col-md-4">
                            <div class="panel panel-warning">
                                <div class="panel-heading" style="height: 10%">
                                    <h3 class="panel-title">Plugins Explorer</h3></div>
                                <div class="panel-body" style="height: 80%">
                                    <div af-js-component="Plugins Explorer"></div>
                                </div>
                                <div class="panel-footer" style="height: 10%">
                                    <span class="badge">NOT powered by React :-)</span></div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="panel panel-warning">
                                <div class="panel-heading" style="height: 10%">
                                    <h3 class="panel-title">Welcome</h3></div>
                                <div class="panel-body" style="height: 80%">
                                    <div af-js-component="welcome" data-startup-special-message="Hi there!"></div>
                                </div>
                                <div class="panel-footer" style="height: 10%">
                                    <span class="badge">NOT powered by React :-)</span></div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="panel panel-warning">
                                <div class="panel-heading" style="height: 10%">
                                    <h3 class="panel-title">GitHubCommitDaysStats</h3></div>
                                <div class="panel-body" style="height: 80%">
                                    <div af-js-component="GitHubCommitDaysStats"></div>
                                </div>
                                <div class="panel-footer" style="height: 10%">
                                    <span class="badge">NOT powered by React :-)</span></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>`;
    }
}

export class ReactMarkdownEditor extends AppFormerEditors.Editor {

    constructor() {
        super("ReactEditor");
        this.af_resourceTypes = ["MdResourceType"];
        this.af_componentTitle = "ReactEditor";
        this.af_isReact = true;
        this.af_priority = 200000;
    }

    private uri: string;

    af_onEditorStartup(path: ObservablePath, place: PlaceRequest): void {
        //FIXME: path and place parameters are not arriving correctly.
        this.uri = path + ""; //This is a workaround to get the uri
    }

    public af_componentRoot(children?: any): AppFormer.Element {
        return <MarkdownFile uri={this.uri}/>
    }
}

(window as any).$registerResourceType({
    id: "MdResourceType",
    short_name: "Markdown Resource Type",
    description: "Markdown Description",
    prefix: "",
    suffix: "md",
    priority: "1000",
    simple_wildcard_pattern: "*.md",
    accept: (filename: string) => filename.split('.').pop() === "md"
});

AppFormer.register(new SimpleReactComponent());
AppFormer.register(new CompassLayoutJsPerspective());
AppFormer.register(new ReactTemplatedJsPerspective());
AppFormer.register(new StringTemplatedJsPerspective());
AppFormer.register(new ReactMarkdownEditor());