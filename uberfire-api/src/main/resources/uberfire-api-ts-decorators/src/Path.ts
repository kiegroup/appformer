import {Path as Path_gen} from "@kiegroup-ts-generated/uberfire-api";

export interface Path extends Path_gen {

    getFileName(): string;

    toURI(): string;

}