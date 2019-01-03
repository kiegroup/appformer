import { Screen } from "appformer-js";
import { Path } from "@kiegroup-ts-generated/uberfire-api";
import { ObservablePath } from "@kiegroup-ts-generated/uberfire-api";
import { PlaceRequest } from "@kiegroup-ts-generated/uberfire-api";

export abstract class ResourceType {
  public shortName: string;
  public description: string;
  public prefix: string;
  public suffix: string;
  public priority: number;
  public simpleWildcardPattern: string;
  public category: string;
  public abstract accept(path: Path): boolean;
}

export enum LockingStrategy {
  FRAMEWORK_PESSIMISTIC,
  EDITOR_PROVIDED
}

export abstract class Editor extends Screen {
  public af_priority: number = 0;
  public af_resourceTypes: string[] = [];

  public af_lockingStrategy: LockingStrategy = LockingStrategy.FRAMEWORK_PESSIMISTIC;
  public af_isDynamic: boolean = false;
  public af_owningPerspective?: string = undefined;

  public abstract af_onEditorStartup(path: ObservablePath, place: PlaceRequest): void;

  public af_onSave(): void {
    //
  }
}
