/** Owns form submission, duplicate-click prevention and inline failure feedback. */
import { notify } from "./notifications.js";
export function closeModal() {
  document.querySelector("#modal").close();
}
export function openForm(title, content, submit, onSaved) {
  const modal = document.querySelector("#modal");
  document.querySelector("#modal-title").textContent = title;
  document.querySelector("#modal-content").innerHTML =
    '<form class="form-grid">' +
    content +
    '<p class="form-error" role="alert"></p><button type="submit">Зберегти</button></form>';
  document.querySelector("#close-modal").onclick = closeModal;
  const form = modal.querySelector("form");
  form.onsubmit = async (event) => {
    event.preventDefault();
    const button = form.querySelector("[type=submit]");
    button.disabled = true;
    try {
      const data = new FormData(form);
      // Pass raw FormData too: Object.fromEntries alone loses repeated equipment checkboxes.
      await submit(Object.fromEntries(data), data);
      closeModal();
      notify("Зміни збережено");
      await onSaved();
    } catch (error) {
      form.querySelector(".form-error").textContent = error.message;
    } finally {
      button.disabled = false;
    }
  };
  modal.showModal();
}
