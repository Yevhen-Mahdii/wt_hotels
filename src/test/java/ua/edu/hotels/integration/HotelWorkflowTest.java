package ua.edu.hotels.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.*;
import ua.edu.hotels.data.dto.*;
import ua.edu.hotels.data.entity.*;
import ua.edu.hotels.data.enums.*;
import ua.edu.hotels.security.HotelToken;
import ua.edu.hotels.service.*;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:workflow;DB_CLOSE_DELAY=-1",
      "app.jwt-key-file=./target/test-jwt.key"
    })
/** Exercises real JWT authentication, database constraints and concurrent booking workflows. */
@AutoConfigureMockMvc
class HotelWorkflowTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbcTemplate;
  @Autowired BookingService bookingService;
  @Autowired JwtService jwtService;
  @Autowired RoleService roleService;
  @Autowired MutableClock clock;
  long admin, operator, manager, otherManager, hotel, otherHotel, room;

  @TestConfiguration
  static class Config {
    @Bean
    @Primary
    MutableClock testClock() {
      return new MutableClock();
    }
  }

  static class MutableClock extends Clock {
    AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-11-02T10:00:00Z"));

    public ZoneId getZone() {
      return ZoneId.of("Europe/Kyiv");
    }

    public Clock withZone(ZoneId zone) {
      return Clock.fixed(instant(), zone);
    }

    public Instant instant() {
      return now.get();
    }
  }

  @BeforeEach
  void prepare() throws Exception {
    for (String table :
        List.of("revenue", "stay", "booking", "room_equipment", "room", "app_user", "hotel"))
      jdbcTemplate.update("DELETE FROM " + table);
    clock.now.set(Instant.parse("2026-11-02T10:00:00Z"));
    var setup =
        anonymous(
            "/auth/setup",
            Map.of("email", "admin@example.test", "password", "correct-password"),
            200);
    admin = setup.path("user").path("id").asLong();
    hotel =
        postAs(admin, "/hotels", Map.of("name", "Лісовий", "address", "Київ"), 200)
            .path("id")
            .asLong();
    otherHotel =
        postAs(admin, "/hotels", Map.of("name", "Берег", "address", "Львів"), 200)
            .path("id")
            .asLong();
    operator = user("operator", Role.ADMINISTRATOR, null);
    manager = user("manager", Role.MANAGER, hotel);
    otherManager = user("other", Role.MANAGER, otherHotel);
    room = postAs(manager, "/rooms", roomBody(hotel, "101", 2, "1000"), 200).path("id").asLong();
  }

  long user(String name, Role role, Long hotelId) throws Exception {
    Map<String, Object> body =
        new HashMap<>(
            Map.of(
                "email",
                name + "@example.test",
                "password",
                "correct-password",
                "role",
                role.name()));
    body.put("hotelId", hotelId);
    return postAs(admin, "/users", body, 200).path("id").asLong();
  }

  Map<String, Object> roomBody(long h, String n, int beds, String price) {
    return Map.of(
        "hotelId",
        h,
        "number",
        n,
        "beds",
        beds,
        "price",
        price,
        "equipment",
        List.of("KETTLE", "TELEVISION"));
  }

  Map<String, Object> bookingBody(long r, String from, String to) {
    return Map.of("roomId", r, "startDate", from, "endDate", to);
  }

  JsonNode anonymous(String path, Object body, int status) throws Exception {
    return json.readTree(
        mvc.perform(
                post("/api" + path)
                    .contentType("application/json")
                    .content(json.writeValueAsBytes(body)))
            .andExpect(status().is(status))
            .andReturn()
            .getResponse()
            .getContentAsString());
  }

  JsonNode postAs(long id, String path, Object body, int code) throws Exception {
    return json.readTree(
        mvc.perform(
                post("/api" + path)
                    .header("Authorization", "Bearer " + jwtFor(id))
                    .contentType("application/json")
                    .content(json.writeValueAsBytes(body)))
            .andExpect(status().is(code))
            .andReturn()
            .getResponse()
            .getContentAsString());
  }

  JsonNode getAs(long id, String path) throws Exception {
    return json.readTree(
        mvc.perform(get("/api" + path).header("Authorization", "Bearer " + jwtFor(id)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());
  }

  @Test
  void firstSetupIsOneTimeAndJwtIsRequired() throws Exception {
    anonymous(
        "/auth/setup", Map.of("email", "second@example.test", "password", "correct-password"), 409);
    var login =
        anonymous(
            "/auth/login",
            Map.of("email", "ADMIN@EXAMPLE.TEST", "password", "correct-password"),
            200);
    String token = login.path("token").asText();
    assertThat(token.split("\\.")).hasSize(3);
    mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("passwordHash").doesNotExist());
    mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/me").header("Authorization", "Bearer " + token + "x"))
        .andExpect(status().isUnauthorized());
    anonymous(
        "/auth/login", Map.of("email", "admin@example.test", "password", "wrong-password"), 401);
  }

  @Test
  void permissionsAndHotelIsolation() throws Exception {
    postAs(manager, "/bookings", bookingBody(room, "2026-11-03", "2026-11-05"), 403);
    postAs(operator, "/rooms", roomBody(hotel, "202", 1, "1"), 403);
    postAs(operator, "/hotels", Map.of("name", "X", "address", "Y"), 403);
    postAs(otherManager, "/rooms/" + room + "/check-in", Map.of(), 403);
    mvc.perform(
            get("/api/revenue?start=2026-11-01&end=2026-12-01&hotelId=" + hotel)
                .header("Authorization", "Bearer " + jwtFor(otherManager)))
        .andExpect(status().isForbidden());
    assertThat(getAs(otherManager, "/rooms")).isEmpty();
  }

  @Test
  void overlapEditCancelAndAdjacentDates() throws Exception {
    long b =
        postAs(operator, "/bookings", bookingBody(room, "2026-11-03", "2026-11-05"), 200)
            .path("id")
            .asLong();
    postAs(operator, "/bookings", bookingBody(room, "2026-11-04", "2026-11-06"), 409);
    postAs(operator, "/bookings", bookingBody(room, "2026-11-05", "2026-11-06"), 200);
    mvc.perform(
            put("/api/bookings/" + b)
                .header("Authorization", "Bearer " + jwtFor(operator))
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookingBody(room, "2026-11-04", "2026-11-07"))))
        .andExpect(status().isConflict());
    postAs(operator, "/bookings/" + b + "/cancel", Map.of(), 200);
    postAs(operator, "/bookings", bookingBody(room, "2026-11-03", "2026-11-05"), 200);
  }

  @Test
  void actualStayFreezesPriceAndRecordsRevenueOnce() throws Exception {
    long b =
        postAs(operator, "/bookings", bookingBody(room, "2026-11-02", "2026-11-08"), 200)
            .path("id")
            .asLong();
    mvc.perform(
            put("/api/rooms/" + room)
                .header("Authorization", "Bearer " + jwtFor(manager))
                .contentType("application/json")
                .content(json.writeValueAsBytes(roomBody(hotel, "101", 2, "1200"))))
        .andExpect(status().isOk());
    postAs(manager, "/rooms/" + room + "/check-in", Map.of("bookingId", b), 200);
    postAs(operator, "/bookings/" + b + "/cancel", Map.of(), 409);
    mvc.perform(
            put("/api/rooms/" + room)
                .header("Authorization", "Bearer " + jwtFor(manager))
                .contentType("application/json")
                .content(json.writeValueAsBytes(roomBody(hotel, "101", 2, "1900"))))
        .andExpect(status().isOk());
    assertThat(
            getAs(manager, "/revenue?start=2026-11-01&end=2026-12-01").path("total").decimalValue())
        .isEqualByComparingTo("0");
    clock.now.set(Instant.parse("2026-11-05T10:00:00Z"));
    var income = postAs(manager, "/rooms/" + room + "/check-out", Map.of(), 200);
    assertThat(income.path("nights").asLong()).isEqualTo(3);
    assertThat(income.path("amount").decimalValue()).isEqualByComparingTo("3600");
    postAs(manager, "/rooms/" + room + "/check-out", Map.of(), 409);
    postAs(manager, "/rooms/" + room + "/check-in", Map.of(), 409);
    postAs(manager, "/rooms/" + room + "/clean", Map.of(), 200);
    assertThat(getAs(operator, "/revenue?start=2026-11-01&end=2026-12-01").path("items"))
        .hasSize(1);
  }

  @Test
  void searchSeparatesExactAndLargerAndCalendarShowsConflict() throws Exception {
    postAs(manager, "/rooms", roomBody(hotel, "102", 3, "2000"), 200);
    var result = getAs(operator, "/rooms/search?start=2026-11-02&end=2026-11-04&beds=2");
    assertThat(result.path("exact")).hasSize(1);
    assertThat(result.path("larger")).hasSize(1);
    postAs(manager, "/rooms/" + room + "/check-in", Map.of(), 200);
    postAs(manager, "/rooms/" + room + "/check-out", Map.of(), 200);
    postAs(operator, "/bookings", bookingBody(room, "2026-11-02", "2026-11-04"), 200);
    var calendar = getAs(operator, "/calendar?start=2026-11-02&end=2026-11-03");
    assertThat(calendar.get(0).path("days").get(0).path("conflict").asBoolean()).isTrue();
  }

  @Test
  void concurrentBookingAllowsOnlyOneWinner() throws Exception {
    HotelToken actor = new HotelToken(operator, "operator@example.test", Role.ADMINISTRATOR, null);
    var pool = Executors.newFixedThreadPool(2);
    var ready = new CountDownLatch(2);
    var go = new CountDownLatch(1);
    Callable<Boolean> task =
        () -> {
          ready.countDown();
          go.await();
          try {
            bookingService.save(
                actor,
                null,
                new BookingInput(room, LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 5)));
            return true;
          } catch (org.springframework.web.server.ResponseStatusException e) {
            assertThat(e.getStatusCode().value()).isEqualTo(409);
            return false;
          }
        };
    try {
      var a = pool.submit(task);
      var b = pool.submit(task);
      assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
      go.countDown();
      assertThat(List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS)))
          .containsExactlyInAnyOrder(true, false);
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void noonBoundaryAndDaylightSaving() {
    ZoneId zone = ZoneId.of("Europe/Kyiv");
    assertThat(new StayCalculator(clock).accountingDay(Instant.parse("2026-11-03T09:59:59Z")))
        .isEqualTo(LocalDate.of(2026, 11, 2));
    assertThat(new StayCalculator(clock).accountingDay(Instant.parse("2026-11-03T10:00:00Z")))
        .isEqualTo(LocalDate.of(2026, 11, 3));
    assertThat(
            new StayCalculator(clock)
                .nights(
                    Instant.parse("2026-10-24T09:00:00Z"), Instant.parse("2026-10-25T10:00:00Z")))
        .isEqualTo(1);
  }

  @Test
  void catalogAndPersistenceUseExplicitEnumCodes() throws Exception {
    mvc.perform(get("/api/catalog"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("roles[0].name").value("ADMIN"))
        .andExpect(jsonPath("roles[0].code").value(0))
        .andExpect(jsonPath("roles[0].label").value("Адмін"))
        .andExpect(jsonPath("equipment[0].name").value("KETTLE"));
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT role FROM app_user WHERE id=?", Integer.class, manager))
        .isEqualTo(2);
    assertThat(
            jdbcTemplate.queryForObject("SELECT state FROM room WHERE id=?", Integer.class, room))
        .isEqualTo(0);
    assertThat(
            jdbcTemplate.queryForList(
                "SELECT equipment_code FROM room_equipment WHERE room_id=? ORDER BY equipment_code",
                Integer.class,
                room))
        .containsExactly(0, 2);
    var loaded = getAs(manager, "/rooms");
    assertThat(loaded.get(0).path("equipment").toString()).contains("KETTLE", "TELEVISION");
  }

  private String jwtFor(long id) {
    User user = new User();
    user.setId(id);
    return jwtService.issue(user);
  }

  @Test
  void rolesAreSeededIdempotently() {
    roleService.initialize();
    roleService.initialize();
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app_role", Integer.class))
        .isEqualTo(Role.values().length);
    for (Role role : Role.values()) {
      assertThat(
              jdbcTemplate.queryForObject(
                  "SELECT name FROM app_role WHERE code=?", String.class, role.getCode()))
          .isEqualTo(role.name());
      assertThat(
              jdbcTemplate.queryForObject(
                  "SELECT label FROM app_role WHERE code=?", String.class, role.getCode()))
          .isEqualTo(role.getLabel());
    }
  }

  @Test
  void existingJwtUsesCurrentDatabaseRole() throws Exception {
    String token = jwtFor(operator);
    mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
    jdbcTemplate.update("UPDATE app_user SET role=? WHERE id=?", Role.ADMIN.getCode(), operator);
    mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    jdbcTemplate.update(
        "UPDATE app_user SET role=? WHERE id=?", Role.ADMINISTRATOR.getCode(), operator);
    mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void deletedUserCannotUseExistingJwt() throws Exception {
    String token = jwtFor(operator);
    jdbcTemplate.update("DELETE FROM app_user WHERE id=?", operator);
    mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }
}
