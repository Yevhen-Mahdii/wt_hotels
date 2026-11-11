/** Builds hotel fields without loading data or saving changes. */
import {fragment} from "../ui/templates.js";

export function createHotelForm({hotel = null} = {}) {
  const fields = fragment("hotel-form-template");
  const nameInput = fields.querySelector('[name="name"]');
  const addressInput = fields.querySelector('[name="address"]');
  nameInput.value = hotel?.name ?? "";
  addressInput.value = hotel?.address ?? "";

  function readValues() {
    return {name: nameInput.value, address: addressInput.value};
  }

  return {fields, readValues};
}
