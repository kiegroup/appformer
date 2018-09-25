import {PathImpl as PathImpl_gen} from "@kiegroup-ts-generated/uberfire-api";
import {Path} from "./Path";

export class PathImpl extends PathImpl_gen implements Path {

    getFileName(): string {
        return this.fileName!;
    }

    toURI(): string {
        return this.uri!;
    }

}