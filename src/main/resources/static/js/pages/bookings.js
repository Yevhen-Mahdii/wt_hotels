/** Owns reservation loading, date changes and cancellation. */
import {startPage} from "../core/page.js";
import {hotelName} from "../core/lookups.js";
import {rest} from "../core/rest.js";
import {element, text} from "../ui/templates.js";
import {badge} from "../ui/components.js";
import {notify} from "../ui/notifications.js";
import {createBookingForm} from "../forms/booking-form.js";
import {openForm} from "../ui/modal.js";

startPage(async ({main, user}) => {
  const items = main.querySelector("#items");
  let hotels = [];
  let rooms = [];
  let bookings = [];

  function createBookingCard(booking) {
    const card = element("booking-card-template");
    const room = rooms.find((room) => room.id === booking.roomId);
    text(card, "[data-number]", "№ " + (room?.number ?? booking.roomId));
    text(card, "[data-hotel]", hotelName(hotels, room?.hotelId));
    text(card, "[data-dates]", booking.startDate + " → " + booking.endDate);
    card.querySelector("[data-status]").replaceChildren(badge(booking.status));
    if (user.role !== "ADMINISTRATOR" || booking.status !== "RESERVED") {
      card.querySelector(".actions").remove();
    } else {
      card.querySelector("[data-edit]").dataset.bookingId = String(booking.id);
      card.querySelector("[data-cancel]").dataset.bookingId = String(booking.id);
    }
    return card;
  }

  async function refreshBookings() {
    const [loadedHotels, loadedRooms, loadedBookings] = await Promise.all([
      rest.hotels.list(),
      rest.rooms.list(),
      rest.bookings.list(),
    ]);
    hotels = loadedHotels;
    rooms = loadedRooms;
    bookings = loadedBookings;
    items.replaceChildren(...bookings.map(createBookingCard));
    main.querySelector("#empty").hidden = bookings.length > 0;
  }

  function openBookingEditor(booking) {
    const room = rooms.find((room) => room.id === booking.roomId);
    if (!room) {
      notify("Номер не знайдено. Оновіть сторінку.", {error: true});
      return;
    }
    const form = createBookingForm({
      room,
      hotelName: hotelName(hotels, room.hotelId),
      startDate: booking.startDate,
      endDate: booking.endDate,
    });

    async function saveBooking() {
      const values = form.readValues();
      await rest.bookings.update(booking.id, {
        roomId: room.id,
        startDate: values.startDate,
        endDate: values.endDate,
      });
    }

    openForm({
      title: "Змінити дати",
      fields: form.fields,
      onSubmit: saveBooking,
      onSaved: refreshBookings,
    });
  }

  async function cancelBooking(button, booking) {
    if (!confirm("Скасувати це бронювання?")) {
      return;
    }
    button.disabled = true;
    try {
      await rest.bookings.cancel(booking.id);
    } catch (error) {
      button.disabled = false;
      notify(error.message, {error: true});
      return;
    }

    notify("Бронювання скасовано");
    try {
      await refreshBookings();
    } catch (error) {
      notify(error.message, {error: true});
    }
  }

  items.addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-booking-id]");
    if (!button || !items.contains(button)) {
      return;
    }
    const bookingId = Number(button.dataset.bookingId);
    const booking = bookings.find((booking) => booking.id === bookingId);
    if (!booking) {
      notify("Бронювання не знайдено. Оновіть сторінку.", {error: true});
      return;
    }
    if (button.hasAttribute("data-edit")) {
      openBookingEditor(booking);
    } else if (button.hasAttribute("data-cancel")) {
      await cancelBooking(button, booking);
    }
  });

  await refreshBookings();
});
