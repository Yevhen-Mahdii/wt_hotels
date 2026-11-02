/** Uses the same form for first-run setup and login, saving a session only after API success. */
import { rest } from "../core/rest.js";
import { session } from "../core/session.js";
import { state } from "../core/state.js";
import { field } from "../ui/components.js";
export function renderLogin(container, setup, onAuthenticated) {
  container.innerHTML =
    '<div class="auth-shell"><section class="auth-intro"><div class="eyebrow">Єдина мережа. Спільний простір.</div><div class="auth-ornament" aria-hidden="true">⌂</div><h2>Кожен номер.<br>Усе під контролем.</h2><p>Бронювання, щоденна робота готелю та підсумки — в одному спокійному робочому просторі.</p></section><section class="auth-card"><div class="brand"><span class="brand-mark">т</span>Тихий двір</div><div class="eyebrow">' +
    (setup ? "Перший запуск" : "Раді бачити знову") +
    "</div><h1>" +
    (setup ? "Почнімо з Адміна" : "Вхід до системи") +
    "</h1><p>" +
    (setup
      ? "Створіть обліковий запис Адміна. Після цього він зможе додати готелі та користувачів."
      : "Увійдіть зі своїм робочим email і паролем.") +
    '</p><form id="login" class="form-grid">' +
    field(
      "Email",
      "email",
      "email",
      "",
      'maxlength="180" autocomplete="username"',
    ) +
    field(
      "Пароль",
      "password",
      "password",
      "",
      'minlength="10" maxlength="72" autocomplete="' +
        (setup ? "new-password" : "current-password") +
        '"',
    ) +
    '<p class="form-note">Щонайменше 10 символів у паролі.</p><p class="form-error" role="alert"></p><button>' +
    (setup ? "Створити Адміна" : "Увійти") +
    '</button></form><div class="auth-footer">Система керування мережею готелів</div></section></div>';
  const form = document.querySelector("#login");
  form.onsubmit = async (e) => {
    e.preventDefault();
    const button = form.querySelector("button");
    button.disabled = true;
    try {
      const data = await (setup ? rest.auth.setup : rest.auth.login)(
        Object.fromEntries(new FormData(form)),
      );
      session.save(data.token);
      state.user = data.user;

      await onAuthenticated();
    } catch (error) {
      form.querySelector(".form-error").textContent = error.message;
    } finally {
      button.disabled = false;
    }
  };
}
