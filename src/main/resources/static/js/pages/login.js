/** Binds the static login page; first-run setup uses the same form with different labels. */
import {rest} from "../core/rest.js";
import {session} from "../core/session.js";
import {afterLoginUrl} from "../core/routes.js";

const form = document.querySelector("#login");
const button = form.querySelector("button");
const errorLabel = form.querySelector(".form-error");

let setup = false;

async function boot() {
  try {
    const status = await rest.auth.status();
    setup = status.setupRequired;

    if (session.token && !setup) {
      try {
        const user = await rest.auth.me();
        location.replace(afterLoginUrl(user.role));
        return;
      } catch (error) {
        if (error.status !== 401) {
          throw error;
        }
        session.clear();
      }
    }

    if (setup) {
      session.clear();
      document.querySelector("#login-caption").textContent = "Перший запуск";
      document.querySelector("#login-title").textContent = "Почнімо з Адміна";
      document.querySelector("#login-description").textContent =
        "Створіть обліковий запис Адміна. Після цього він зможе додати готелі та користувачів.";
      form.elements.password.autocomplete = "new-password";
      button.textContent = "Створити Адміна";
    }

    button.disabled = false;
  } catch (error) {
    errorLabel.textContent = error.message;
  }
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  button.disabled = true;
  errorLabel.textContent = "";

  try {
    const credentials = {
      email: form.elements.email.value,
      password: form.elements.password.value,
    };
    let response;
    if (setup) {
      response = await rest.auth.setup(credentials);
    } else {
      response = await rest.auth.login(credentials);
    }
    session.save(response.token);
    location.assign(afterLoginUrl(response.user.role));
  } catch (error) {
    errorLabel.textContent = error.message;
    button.disabled = false;
  }
});

window.addEventListener("pageshow", (event) => {
  if (event.persisted) {
    location.reload();
  }
});

boot();
