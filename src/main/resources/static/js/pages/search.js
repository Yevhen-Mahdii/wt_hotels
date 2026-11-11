/** Loads matching rooms and creates bookings using the displayed search dates. */
import {startPage} from "../core/page.js";
import {rest} from "../core/rest.js";
import {hotelName} from "../core/lookups.js";
import {bindFilters} from "../ui/filters.js";
import {roomCard} from "../ui/room-card.js";
import {createBookingForm} from "../forms/booking-form.js";
import {openForm} from "../ui/modal.js";
import {notify} from "../ui/notifications.js";

startPage(async ({main, user}) => {
  const hotels = await rest.hotels.list();
  let rooms = [];
  let searchDates;
  const filters = bindFilters({container: main, hotels, user, onApply: refreshResults});

  function renderMatches(group, matches) {
    const section = main.querySelector(`[data-results="${group}"]`);
    const cards = matches.map((room) =>
      roomCard({
        room,
        hotelName: hotelName(hotels, room.hotelId),
        mode: "search",
      }),
    );
    section.querySelector("[data-count]").textContent = matches.length + " номерів";
    section.querySelector(".grid").replaceChildren(...cards);
    section.querySelector(".empty").hidden = matches.length > 0;
  }

  async function refreshResults() {
    const query = filters.getAppliedValues();
    const result = await rest.rooms.search(query);
    rooms = [...result.exact, ...result.larger];
    searchDates = {start: query.start, end: query.end};
    renderMatches("exact", result.exact);
    renderMatches("larger", result.larger);
  }

  function openBooking(room) {
    const form = createBookingForm({
      room,
      hotelName: hotelName(hotels, room.hotelId),
      startDate: searchDates.start,
      endDate: searchDates.end,
    });

    async function createBooking() {
      const values = form.readValues();
      await rest.bookings.create({
        roomId: room.id,
        startDate: values.startDate,
        endDate: values.endDate,
      });
    }

    openForm({
      title: "Бронювання · № " + room.number,
      fields: form.fields,
      onSubmit: createBooking,
      onSaved: refreshResults,
    });
  }

  main.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-book]");
    if (!button || !main.contains(button)) {
      return;
    }
    const roomId = Number(button.dataset.roomId);
    const room = rooms.find((room) => room.id === roomId);
    if (!room) {
      notify("Номер не знайдено. Повторіть пошук.", {error: true});
      return;
    }
    openBooking(room);
  });

  await refreshResults();
});
