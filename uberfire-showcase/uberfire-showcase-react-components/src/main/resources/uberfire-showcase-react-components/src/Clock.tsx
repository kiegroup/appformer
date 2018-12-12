import * as React from "react";

interface State {
    readonly clockJobId?: number;
    readonly time: string;
}

interface Props {

}

const actions = {
    tick(cur: State): State {
        return {
            time: new Date().toLocaleDateString("en-US", {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: "numeric",
                minute: "numeric",
                second: "numeric"
            })
        }
    }
};

export class Clock extends React.Component<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {time: "Clock is starting.."};
    }

    componentDidMount(): void {
        const id = window.setInterval(() => this.setState(actions.tick), 1000);
        this.setState({clockJobId: id});
    }

    componentWillUnmount(): void {
        window.clearInterval(this.state.clockJobId!);
    }

    render() {
        return <>
        <span>
            {this.state.time}
        </span>
        </>;
    }
}