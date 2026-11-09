/** Connects the shared HTML navigation to the authenticated account. */
import { state } from "../core/state.js";
import { roleLabel } from "../core/catalog.js";
import { allowedPages } from "../core/routes.js";
import { fragment, text } from "./templates.js";

export function mountShell(onLogout) {
  const shell = document.querySelector(".shell");
  shell.prepend(fragment("shell-template"));
  text(shell, "[data-account-email]", state.user.email);
  text(shell, "[data-account-role]", roleLabel(state.user.role));
  text(shell, "[data-role-caption]", "Тихий двір / " + roleLabel(state.user.role));
  for (const link of shell.querySelectorAll("nav [data-page]")) {
    if (!allowedPages(state.user.role).includes(link.dataset.page)) {
      link.remove();
    } else if (link.dataset.page === state.page) {
      link.classList.add("active");
      link.setAttribute("aria-current", "page");
    }
  }
  shell.querySelector("#logout").addEventListener("click", onLogout);
}
