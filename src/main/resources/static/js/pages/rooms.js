/** Owns inventory loading, editing and the check-in / checkout / cleaning workflow. */
import {startPage} from "../core/page.js";
import {rest} from "../core/rest.js";
import {hotelName} from "../core/lookups.js";
import {equipmentOptions} from "../core/catalog.js";
import {roomCard} from "../ui/room-card.js";
import {createRoomForm} from "../forms/room-form.js";
import {createCheckinForm} from "../forms/checkin-form.js";
import {openForm} from "../ui/modal.js";
import {notify} from "../ui/notifications.js";

startPage(async ({main, user}) => {
  const items = main.querySelector("#items");
  let rooms = [];
  let bookings = [];

  async function refreshRooms() {
    const [hotels, loadedRooms, loadedBookings] = await Promise.all([
      rest.hotels.list(),
      rest.rooms.list(),
      rest.bookings.list(),
    ]);
    rooms = loadedRooms;
    bookings = loadedBookings;
    main.querySelector("#page-description").textContent =
      hotelName(hotels, user.hotelId) + " · щоденна робота готелю";
    const cards = rooms.map((room) =>
      roomCard({
        room,
        hotelName: hotelName(hotels, room.hotelId),
        bookings,
        mode: "inventory",
      }),
    );
    items.replaceChildren(...cards);
    main.querySelector("#empty").hidden = rooms.length > 0;
  }

  function openRoomEditor(room = null) {
    const form = createRoomForm({room, equipment: equipmentOptions()});

    async function saveRoom() {
      const values = form.readValues();
      const input = {
        hotelId: user.hotelId,
        number: values.number,
        beds: values.beds,
        price: values.price,
        equipment: values.equipment,
      };
      if (room) {
        await rest.rooms.update(room.id, input);
      } else {
        await rest.rooms.create(input);
      }
    }

    openForm({
      title: room ? "Налаштування номера" : "Новий номер",
      fields: form.fields,
      onSubmit: saveRoom,
      onSaved: refreshRooms,
    });
  }

  function openCheckin(room) {
    const form = createCheckinForm({room, bookings});

    async function checkIn() {
      const values = form.readValues();
      await rest.rooms.checkIn(room.id, values.bookingId);
    }

    openForm({
      title: "Заселення · № " + room.number,
      fields: form.fields,
      onSubmit: checkIn,
      onSaved: refreshRooms,
    });
  }

  async function changeRoomState(button, room) {
    const action = button.dataset.action;
    let confirmation;
    if (action === "check-out") {
      confirmation = "Завершити проживання та зафіксувати дохід?";
    } else if (action === "clean") {
      confirmation = "Підтвердити, що номер прибраний і готовий?";
    } else {
      return;
    }
    if (!confirm(confirmation)) {
      return;
    }

    button.disabled = true;
    try {
      if (action === "check-out") {
        await rest.rooms.checkOut(room.id);
      } else {
        await rest.rooms.completeCleaning(room.id);
      }
    } catch (error) {
      button.disabled = false;
      notify(error.message, {error: true});
      return;
    }

    notify("Стан номера оновлено");
    // Keep the old action disabled if refreshing fails after a successful mutation.
    try {
      await refreshRooms();
    } catch (error) {
      notify(error.message, {error: true});
    }
  }

  main.querySelector("#add-room").addEventListener("click", () => openRoomEditor());
  items.addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-room-id]");
    if (!button || !items.contains(button)) {
      return;
    }
    const roomId = Number(button.dataset.roomId);
    const room = rooms.find((room) => room.id === roomId);
    if (!room) {
      notify("Номер не знайдено. Оновіть сторінку.", {error: true});
      return;
    }

    if (button.hasAttribute("data-edit-room")) {
      openRoomEditor(room);
    } else if (button.hasAttribute("data-checkin")) {
      openCheckin(room);
    } else {
      await changeRoomState(button, room);
    }
  });

  await refreshRooms();
});
