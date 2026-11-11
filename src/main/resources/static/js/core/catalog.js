/** Loads enum metadata before rendering; labels stay owned by the Java backend. */
import {rest} from "./rest.js";

let catalog;

export async function loadCatalog() {
  catalog = await rest.catalog();
}

export const roleOptions = () => catalog.roles;

export const equipmentOptions = () => catalog.equipment;

export const stateOptions = () => [...catalog.roomStates, ...catalog.bookingStatuses];

export const roleLabel = (name) =>
  catalog.roles.find((option) => option.name === name)?.label ?? name;

export const stateLabel = (name) =>
  stateOptions().find((option) => option.name === name)?.label ?? name;
