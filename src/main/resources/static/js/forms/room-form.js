/** Builds room fields and reads their values; the page owns saving and refreshing. */
import {fragment, element} from "../ui/templates.js";

export function createRoomForm({room = null, equipment}) {
  const fields = fragment("room-form-template");
  const numberInput = fields.querySelector('[name="number"]');
  const bedsInput = fields.querySelector('[name="beds"]');
  const priceInput = fields.querySelector('[name="price"]');
  const equipmentContainer = fields.querySelector(".checks");

  numberInput.value = room?.number ?? "";
  bedsInput.value = room?.beds ?? 1;
  priceInput.value = room?.price ?? 0;

  for (const option of equipment) {
    const label = element("equipment-option-template");
    const checkbox = label.querySelector("input");
    checkbox.value = option.name;
    checkbox.checked = room?.equipment.includes(option.name) ?? false;
    label.querySelector("span").textContent = option.label;
    equipmentContainer.append(label);
  }

  function readValues() {
    const equipment = [];
    for (const checkbox of equipmentContainer.querySelectorAll("input:checked")) {
      equipment.push(checkbox.value);
    }

    // Native form validation checks required fields, ranges and steps before submission.
    return {
      number: numberInput.value,
      beds: bedsInput.valueAsNumber,
      price: priceInput.valueAsNumber,
      equipment,
    };
  }

  return {fields, readValues};
}
