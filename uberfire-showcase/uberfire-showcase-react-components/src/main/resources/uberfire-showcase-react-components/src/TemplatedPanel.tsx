import * as React from "react";

interface State {
}

interface Props {
    readonly title: string;
}

export class TemplatedPanel extends React.Component<Props, State> {

    render() {

        return (
            <div className="panel panel-warning">
                <div className="panel-heading" style={{height: "10%"}}>
                    <h3 className="panel-title">{this.props.title}</h3></div>
                <div className="panel-body" style={{height: "80%"}}>
                    {this.props.children}
                </div>
                <div className="panel-footer" style={{height: "10%"}}><span className="badge">powered by React</span>
                </div>
            </div>
        );
    }

}