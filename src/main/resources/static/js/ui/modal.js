/** Owns dialog lifecycle and submissions; form markup lives in HTML templates. */
import { notify } from "./notifications.js";
import { fragment } from "./templates.js";
export function mountModal() {
  document.body.append(fragment("modal-template"));
  document.querySelector("#close-modal").addEventListener("click", closeModal);
}
export function closeModal() {
  document.querySelector("#modal")?.close();
}
export function openForm(title, content, submit, onSaved) {
  const modal = document.querySelector("#modal");
  modal.querySelector("#modal-title").textContent = title;
  modal.querySelector("#modal-content").replaceChildren(content);
  const form = modal.querySelector("form");
  const errorLabel = form.querySelector(".form-error");
  const button = form.querySelector("[type=submit]");
  errorLabel.textContent = "";
  button.disabled = false;
  // The dialog is reused: discard the previous form's submit handler when it closes.
  const controller = new AbortController();
  modal.addEventListener("close", () => controller.abort(), { once: true });
  form.addEventListener(
    "submit",
    async (event) => {
      event.preventDefault();
      button.disabled = true;
      errorLabel.textContent = "";
      try {
        const data = new FormData(form);
        // Preserve repeated fields (equipment checkboxes) alongside scalar form values.
        await submit(Object.fromEntries(data), data);
        closeModal();
        notify("Зміни збережено");
        try {
          await onSaved();
        } catch (error) {
          notify(error.message, true);
        }
      } catch (error) {
        errorLabel.textContent = error.message;
      } finally {
        button.disabled = false;
      }
    },
    { signal: controller.signal },
  );
  modal.showModal();
}
