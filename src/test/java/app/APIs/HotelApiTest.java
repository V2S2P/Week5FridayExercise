package app.APIs;

import app.Populator;
import app.DAOs.HotelDAO;
import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.entities.Hotel;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HotelApiTest {

    private Javalin app;
    private EntityManagerFactory emf;
    private HotelDAO hotelDAO;
    private Populator populator;
    private Hotel hotel; // hotel used in each test

    @BeforeAll
    void init() {
        HibernateConfig.setIsTest(true); // use test DB
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        hotelDAO = HotelDAO.getInstance(emf);
        populator = new Populator(hotelDAO, emf);

        app = ApplicationConfig.startServer(7007);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7007;
    }

    @BeforeEach
    void setUp() {
        List<Hotel> hotels = populator.populate1Hotel();
        hotel = hotels.get(0);
    }

    @AfterEach
    void tearDown() {
        populator.cleanUpHotels();
    }

    @AfterAll
    void close() {
        if (app != null) app.stop();
        if (emf != null) emf.close();
    }

    // ------------------ Tests ------------------

    @Test
    void testRootEndpoint() {
        given()
                .when().get("/api/v1")
                .then().statusCode(200)
                .body(containsString("Hello World!"));
    }

    @Test
    void testGetHotelById() {
        given()
                .when().get("/api/v1/hotel/" + hotel.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo(hotel.getName()))
                .body("rooms.size()", equalTo(2));
    }

    @Test
    void testGetAllHotels() {
        given()
                .when().get("/api/v1/hotel")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].name", equalTo(hotel.getName()));
    }

    @Test
    void testUpdateHotel() {
        String updatedJson = """
            {
              "name": "Updated Hotel",
              "address": "New Address",
              "rooms": [
                {"id": %d, "roomNumber": 1, "price": 1800},
                {"id": %d, "roomNumber": 2, "price": 2200}
              ]
            }
        """.formatted(hotel.getRooms().get(0).getId(), hotel.getRooms().get(1).getId());

        given()
                .contentType("application/json")
                .body(updatedJson)
                .when().put("/api/v1/hotel/" + hotel.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Hotel"))
                .body("rooms[0].price", equalTo(1800));
    }

    @Test
    void testGetRoomsForHotel() {
        given()
                .when().get("/api/v1/hotel/" + hotel.getId() + "/rooms")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2));
    }

    @Test
    void testAddRoom() {
        String newRoomJson = """
            {"roomNumber": 3, "price": 2500}
        """;

        given()
                .contentType("application/json")
                .body(newRoomJson)
                .when().post("/api/v1/hotel/" + hotel.getId() + "/rooms")
                .then()
                .statusCode(201)
                .body("roomNumber", equalTo(3))
                .body("price", equalTo(2500));
    }

    @Test
    void testRemoveRoom() {
        int roomId = hotel.getRooms().get(0).getId();

        when().delete("/api/v1/hotel/" + hotel.getId() + "/rooms/" + roomId)
                .then().statusCode(anyOf(is(200), is(204)));

        // Confirm only 1 room left
        when().get("/api/v1/hotel/" + hotel.getId() + "/rooms")
                .then().statusCode(200)
                .body("size()", equalTo(1));
    }

    @Test
    void testDeleteHotel() {
        when().delete("/api/v1/hotel/" + hotel.getId())
                .then().statusCode(anyOf(is(200), is(204)));

        when().get("/api/v1/hotel/" + hotel.getId())
                .then().statusCode(404);
    }
}
