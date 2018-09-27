import {ObservablePathImpl as ObservablePathImpl_gen} from "@kiegroup-ts-generated/uberfire-api";
import {ObservablePath} from "./ObservablePath";
import {Path} from "./Path";

export class ObservablePathImpl extends ObservablePathImpl_gen implements ObservablePath {

    getFileName(): string {
        return (this.path as Path).getFileName();
    }

    toURI(): string {
        return "";
    }

}