/** Connects the shared HTML navigation to the authenticated account. */
import {roleLabel} from "../core/catalog.js";
import {allowedPages} from "../core/routes.js";
import {fragment, text} from "./templates.js";

export function mountShell({user, page, onLogout}) {
  const shell = document.querySelector(".shell");
  shell.prepend(fragment("shell-template"));
  text(shell, "[data-account-email]", user.email);
  text(shell, "[data-account-role]", roleLabel(user.role));
  text(shell, "[data-role-caption]", "Тихий двір / " + roleLabel(user.role));
  for (const link of shell.querySelectorAll("nav [data-page]")) {
    if (!allowedPages(user.role).includes(link.dataset.page)) {
      link.remove();
    } else if (link.dataset.page === page) {
      link.classList.add("active");
      link.setAttribute("aria-current", "page");
    }
  }
  shell.querySelector("#logout").addEventListener("click", onLogout);
}
