/** Replaces the previous notification timer so an older message cannot hide a newer one. */
let timer;

export function notify(message, error = false) {
  const element = document.querySelector("#notice");
  element.textContent = message;
  element.className = error ? "error" : "";
  element.hidden = false;
  clearTimeout(timer);
  timer = setTimeout(() => (element.hidden = true), 6500);
}
