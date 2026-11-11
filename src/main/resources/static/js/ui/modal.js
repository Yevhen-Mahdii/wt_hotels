/** Owns dialog lifecycle and submissions; form markup lives in HTML templates. */
import {notify} from "./notifications.js";
import {fragment} from "./templates.js";

export function mountModal() {
  document.body.append(fragment("modal-template"));
  document.querySelector("#close-modal").addEventListener("click", closeModal);
}

export function closeModal() {
  document.querySelector("#modal")?.close();
}

export function openForm({title, fields, onSubmit, onSaved}) {
  const modal = document.querySelector("#modal");
  modal.querySelector("#modal-title").textContent = title;
  modal.querySelector("#modal-content").replaceChildren(fields);

  const form = modal.querySelector("form");
  const errorLabel = form.querySelector(".form-error");
  const button = form.querySelector("[type=submit]");
  const closeButton = modal.querySelector("#close-modal");
  let submitting = false;
  errorLabel.textContent = "";
  button.disabled = false;

  // The dialog is reused: discard the previous form's submit handler when it closes.
  const controller = new AbortController();
  modal.addEventListener("close", () => controller.abort(), {once: true});
  modal.addEventListener(
    "cancel",
    (event) => {
      if (submitting) {
        event.preventDefault();
      }
    },
    {signal: controller.signal},
  );
  form.addEventListener(
    "submit",
    async (event) => {
      event.preventDefault();
      if (submitting) {
        return;
      }
      submitting = true;
      button.disabled = true;
      closeButton.disabled = true;
      errorLabel.textContent = "";

      try {
        await onSubmit();
      } catch (error) {
        errorLabel.textContent = error.message;
        return;
      } finally {
        submitting = false;
        button.disabled = false;
        closeButton.disabled = false;
      }

      // Saving succeeded. A refresh error must not leave the form open for another save.
      controller.abort();
      closeModal();
      notify("Зміни збережено");
      try {
        await onSaved();
      } catch (error) {
        notify(error.message, {error: true});
      }
    },
    {signal: controller.signal},
  );

  modal.showModal();
}
