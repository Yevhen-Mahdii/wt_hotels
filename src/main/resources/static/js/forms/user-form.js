/** Builds account fields from the available roles and hotels. */
import {fragment} from "../ui/templates.js";
import {fillHotelSelect} from "../ui/components.js";

export function createUserForm({hotels, roles}) {
  const fields = fragment("user-form-template");
  const emailInput = fields.querySelector('[name="email"]');
  const passwordInput = fields.querySelector('[name="password"]');
  const roleSelect = fields.querySelector('[name="role"]');
  const hotelSelect = fields.querySelector('[name="hotelId"]');

  for (const role of roles) {
    roleSelect.add(new Option(role.label, role.name));
  }
  fillHotelSelect({select: hotelSelect, hotels});

  function readValues() {
    return {
      email: emailInput.value,
      password: passwordInput.value,
      role: roleSelect.value,
      hotelId: hotelSelect.value === "" ? null : Number(hotelSelect.value),
    };
  }

  return {fields, readValues};
}
