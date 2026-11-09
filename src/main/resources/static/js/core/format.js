/** Formats dates and currency; views assign data with textContent rather than HTML strings. */
export const today = () =>
  new Intl.DateTimeFormat("sv-SE", { timeZone: "Europe/Kyiv" }).format(new Date());

// Use UTC calendar arithmetic so browser time zones and DST cannot shift a date-only value.
export function addDays(date, count) {
  const value = new Date(date + "T12:00:00Z");
  value.setUTCDate(value.getUTCDate() + count);
  return value.toISOString().slice(0, 10);
}

export const money = (value) =>
  new Intl.NumberFormat("uk-UA", {
    style: "currency",
    currency: "UAH",
    maximumFractionDigits: 2,
  }).format(value);

export const dateLabel = (date) =>
  new Intl.DateTimeFormat("uk-UA", { day: "2-digit", month: "short" }).format(
    new Date(date + "T12:00:00Z"),
  );

export const timestampLabel = (value) =>
  new Date(value).toLocaleString("uk-UA", { timeZone: "Europe/Kyiv" });
