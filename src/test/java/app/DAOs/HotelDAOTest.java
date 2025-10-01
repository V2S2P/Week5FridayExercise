package app.DAOs;

import app.config.HibernateConfig;
import app.entities.Hotel;
import app.entities.Room;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HotelDAOTest {
    private final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private HotelDAO hotelDAO;
    private Hotel hotel;
    private List<Room> rooms;
    private Room room;

    @BeforeAll
    static void initOnce() {
        HotelDAO.getInstance(emf);
    }

    @BeforeEach
    void setUp() {
        hotelDAO = HotelDAO.getInstance(emf);

        // Insert fresh test data
        var em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Room").executeUpdate();
        em.createQuery("DELETE FROM Hotel").executeUpdate();

        hotel =  Hotel.builder()
                .name("Test Hotel")
                .address("Copenhagen")
                .build();

        Room r1 = Room.builder()
                    .roomNumber(1)
                    .price(1000)
                    .build();

        Room r2 = Room.builder()
                    .roomNumber(2)
                    .price(1200)
                    .build();

        hotel.addRoom(r1);
        hotel.addRoom(r2);

        em.persist(hotel);
        em.getTransaction().commit();
        em.close();
    }

    @AfterEach
    void tearDown() {
        var em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Room").executeUpdate();
        em.createQuery("DELETE FROM Hotel").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @Test
    void getAllHotels() {
        List<Hotel> hotels = hotelDAO.getAllHotels();
        assertEquals(1, hotels.size());
        assertEquals("Test Hotel", hotels.get(0).getName());
    }

    @Test
    void getHotelById() {
        Hotel hotelFound = hotelDAO.getHotelById(hotel.getId());
        assertNotNull(hotelFound);
        assertEquals("Test Hotel", hotelFound.getName());
    }

    @Test
    void createHotel() {
        Hotel createHotel = Hotel.builder()
                .name("Another Hotel")
                .address("testergade12")
                .build();
        Hotel saved = hotelDAO.createHotel(createHotel);
        assertNotNull(saved.getId());
        assertEquals("Another Hotel", saved.getName());
    }

    @Test
    void updateHotel() {
        Hotel updatedHotel = hotelDAO.getHotelById(hotel.getId());
        updatedHotel.setName("Updated Hotel");
        Hotel saved = hotelDAO.updateHotel(updatedHotel);
        assertNotNull(saved);
        assertEquals("Updated Hotel", saved.getName());
    }

    @Test
    void deleteHotel() {
        hotelDAO.deleteHotel(hotel.getId());
        Hotel saved = hotelDAO.getHotelById(hotel.getId());
        assertNull(saved);
        assertTrue(hotelDAO.getAllHotels().isEmpty(), "Hotels should be empty");
    }

    @Test
    void addRoom() {
        // Build a new room
        Room newRoom = Room.builder()
                .roomNumber(3)
                .price(1500)
                .build();

        // Add room via DAO
        hotelDAO.addRoom(hotel, newRoom);

        // Assert it was persisted
        assertNotNull(newRoom.getId(), "Room ID should not be null");
        assertEquals(hotel.getId(), newRoom.getHotel().getId(), "Room should be linked to hotel");

        // Check the DB directly
        List<Room> roomsInDb = hotelDAO.getRoomsForHotel(hotel);
        assertEquals(3, roomsInDb.size(), "There should be 3 rooms total after adding one");

        boolean found = roomsInDb.stream()
                .anyMatch(r -> r.getRoomNumber() == 3 && r.getPrice() == 1500);
        assertTrue(found, "The new room should exist in the DB");
    }

    @Test
    void removeRoom() {
        // Remove first room via DAO
        hotelDAO.removeRoom(hotel, hotel.getRooms().get(0));

        // Fetch current rooms from DB
        List<Room> roomsInDb = hotelDAO.getRoomsForHotel(hotel);

        // Assert only one room remains
        assertEquals(1, roomsInDb.size(), "There should be only one room remaining");

        // Optionally, check the remaining room has the expected room number
        Room remaining = roomsInDb.get(0);
        assertEquals(2, remaining.getRoomNumber(), "Remaining room should have roomNumber 2");
    }

    @Test
    void getRoomsForHotel() {
        List<Room> rooms = hotelDAO.getRoomsForHotel(hotel);
        assertNotNull(rooms);
        assertEquals(2, rooms.size(), "There should be 2 rooms");
    }
}