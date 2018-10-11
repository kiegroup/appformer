import * as React from "react";
import {VFSService} from "@kiegroup-ts-generated/uberfire-backend-api-rpc";
import {Path} from "uberfire-api-ts-decorators";

interface State {
    readonly openFile?: File;
}

interface Props {

}

interface File {
    readonly contents: string;
    readonly path: Path;
}

const actions = {
    openFile(file: File): (cur: State) => State {
        return (cur: State) => ({openFile: file});
    }
};

export class Files extends React.Component<Props, State> {

    private readonly vfsService: VFSService;

    constructor(props: Props) {
        super(props);
        this.state = {};
        this.vfsService = new VFSService();
    }

    private open(uri: string) {
        return this.vfsService.get({uri: uri})
            .then(path => {
                return this.vfsService.readAllString({path: path}).then(contents => ({
                    path: path,
                    contents: contents
                }));
            })
            .then(file => this.setState(actions.openFile(file)));
    }

    render() {
        return <>

        <h3>Files:</h3>
        <button className={"btn btn-primary btn-sm"} onClick={() => this.open("default://uf-playground/todo.md")}>Open TODO.md</button>
        <button className={"btn btn-primary btn-sm"} onClick={() => this.open("default://uf-playground/README.md")}>Open README.md</button>

        {this.state.openFile &&
        <div style={{margin: "10px", padding: "5px"}}>
            <span> <b>Path:</b> {this.state.openFile.path.toURI()}</span>
            <div style={{border: "1px dotted lightgray"}}
                 dangerouslySetInnerHTML={{__html: (window as any).marked(this.state.openFile.contents)}}/>
        </div>
        }

        {!this.state.openFile &&
        <div>No open file..</div>
        }

        </>;
    }
}