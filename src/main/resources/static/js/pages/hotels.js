/** Owns hotel loading, creation and editing. */
import {startPage} from "../core/page.js";
import {rest} from "../core/rest.js";
import {element, text} from "../ui/templates.js";
import {createHotelForm} from "../forms/hotel-form.js";
import {openForm} from "../ui/modal.js";
import {notify} from "../ui/notifications.js";

startPage(async ({main}) => {
  const items = main.querySelector("#items");
  let hotels = [];

  async function refreshHotels() {
    const [loadedHotels, rooms] = await Promise.all([rest.hotels.list(), rest.rooms.list()]);
    hotels = loadedHotels;
    const cards = hotels.map((hotel) => {
      const card = element("hotel-card-template");
      const roomCount = rooms.filter((room) => room.hotelId === hotel.id).length;
      text(card, "[data-name]", hotel.name);
      text(card, "[data-address]", hotel.address);
      text(card, "[data-count]", roomCount + " номерів");
      card.querySelector("[data-edit]").dataset.hotelId = String(hotel.id);
      return card;
    });
    items.replaceChildren(...cards);
    main.querySelector("#empty").hidden = hotels.length > 0;
  }

  function openHotelEditor(hotel = null) {
    const form = createHotelForm({hotel});

    async function saveHotel() {
      const input = form.readValues();
      if (hotel) {
        await rest.hotels.update(hotel.id, input);
      } else {
        await rest.hotels.create(input);
      }
    }

    openForm({
      title: hotel ? "Редагувати готель" : "Новий готель",
      fields: form.fields,
      onSubmit: saveHotel,
      onSaved: refreshHotels,
    });
  }

  main.querySelector("#add-hotel").addEventListener("click", () => openHotelEditor());
  items.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-hotel-id]");
    if (!button || !items.contains(button)) {
      return;
    }
    const hotelId = Number(button.dataset.hotelId);
    const hotel = hotels.find((hotel) => hotel.id === hotelId);
    if (!hotel) {
      notify("Готель не знайдено. Оновіть сторінку.", {error: true});
      return;
    }
    openHotelEditor(hotel);
  });

  await refreshHotels();
});
