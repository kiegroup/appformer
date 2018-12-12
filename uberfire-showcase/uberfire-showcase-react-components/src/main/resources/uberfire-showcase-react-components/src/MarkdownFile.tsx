import * as React from "react";
import {VFSService} from "@kiegroup-ts-generated/uberfire-backend-api-rpc";
import {Path} from "uberfire-api-ts-decorators";

interface Props {
    uri: string;
}

interface State {
    readonly openFile?: File;
}

interface File {
    readonly contents: string;
    readonly path: Path;
}

export class MarkdownFile extends React.Component<Props, State> {

    private readonly vfsService: VFSService;

    constructor(props: Props) {
        super(props);
        this.state = {};
        this.vfsService = new VFSService();
    }

    componentDidMount(): void {
        this.vfsService.get({uri: this.props.uri})
            .then(path => this.vfsService.readAllString({path: path}).then(contents => ({
                path: path,
                contents: contents
            })))
            .then(file => this.setState({openFile: file}));
    }

    render() {
        return <>

            {this.state.openFile &&
            <div style={{margin: "10px", padding: "5px"}}>
                <span> <b>Path:</b> {this.state.openFile.path.toURI()}</span>
                <div style={{border: "1px dotted lightgray"}}
                     dangerouslySetInnerHTML={{__html: (window as any).marked(this.state.openFile.contents)}}/>
            </div>
            }

        </>;
    }
}