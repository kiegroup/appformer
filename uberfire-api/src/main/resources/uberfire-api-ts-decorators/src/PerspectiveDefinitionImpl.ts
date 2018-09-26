import {PerspectiveDefinitionImpl as PerspectiveDefinitionImpl_gen} from "@kiegroup-ts-generated/uberfire-api";
import {PerspectiveDefinition} from "./PerspectiveDefinition";

export class PerspectiveDefinitionImpl extends PerspectiveDefinitionImpl_gen implements PerspectiveDefinition {

    getName(): string | undefined {
        return this.name;
    }
}