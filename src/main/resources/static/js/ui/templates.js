/** Loads trusted local HTML templates; API values are always assigned as text or DOM properties. */
export async function loadTemplates() {
  const documents = await Promise.all(
    ["/fragments/shared.html", "/fragments/forms.html"].map(async (url) => {
      const response = await fetch(url);
      if (!response.ok)
        throw new Error("Не вдалося завантажити шаблони сторінки.");
      return new DOMParser().parseFromString(
        await response.text(),
        "text/html",
      );
    }),
  );
  for (const source of documents) {
    for (const template of source.querySelectorAll("template")) {
      document.body.append(document.importNode(template, true));
    }
  }
}
export function fragment(id) {
  const template = document.getElementById(id);
  if (!(template instanceof HTMLTemplateElement))
    throw new Error(`Missing HTML template: ${id}`);
  return template.content.cloneNode(true);
}
export function element(id) {
  return fragment(id).firstElementChild;
}
export function text(root, selector, value) {
  root.querySelector(selector).textContent = value ?? "";
}
export function setValues(root, values) {
  for (const [name, value] of Object.entries(values))
    root.querySelector(`[name="${name}"]`).value = value ?? "";
}
