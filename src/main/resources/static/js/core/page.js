/** Checks access and mounts shared UI before handing control to the page. */
import {rest} from "./rest.js";
import {session} from "./session.js";
import {loadCatalog} from "./catalog.js";
import {allowedPages, homeUrl, loginUrl} from "./routes.js";
import {loadTemplates} from "../ui/templates.js";
import {mountShell} from "../ui/shell.js";
import {mountModal} from "../ui/modal.js";
import {notify} from "../ui/notifications.js";

export async function startPage(initialize) {
  const status = document.querySelector("#page-status");
  const main = document.querySelector("#main");

  const redirectToLogin = () => {
    session.clear();
    location.replace(loginUrl());
  };
  window.addEventListener("session-expired", redirectToLogin);

  // A page restored from the back-forward cache must revalidate its session and data.
  window.addEventListener("pageshow", (event) => {
    if (event.persisted) {
      location.reload();
    }
  });

  try {
    if (!session.token) {
      redirectToLogin();
      return;
    }

    const user = await rest.auth.me();
    const page = document.body.dataset.page;
    if (!allowedPages(user.role).includes(page)) {
      location.replace(homeUrl(user.role));
      return;
    }

    await Promise.all([loadCatalog(), loadTemplates()]);

    mountShell({
      user,
      page,
      onLogout() {
        session.clear();
        location.replace("/index.html");
      },
    });
    mountModal();

    await initialize({main, user});

    status.hidden = true;
    document.querySelector(".shell").hidden = false;
  } catch (error) {
    if (error.status === 401) {
      return;
    }

    status.textContent = "Не вдалося завантажити сторінку. Оновіть її, щоб повторити спробу.";
    notify(error.message, {error: true});
  }
}
