package app;

import app.DAOs.HotelDAO;
import app.entities.Hotel;
import app.entities.Room;
import jakarta.persistence.EntityManagerFactory;

import java.util.ArrayList;
import java.util.List;

public class Populator {

    private final HotelDAO hotelDAO;
    private final EntityManagerFactory emf;

    public Populator(HotelDAO hotelDAO, EntityManagerFactory emf) {
        this.hotelDAO = hotelDAO;
        this.emf = emf;
    }

    // Populate 1 hotel with 2 rooms
    public List<Hotel> populate1Hotel() {
        Hotel hotel = new Hotel();
        hotel.setName("Hotel Kammer");
        hotel.setAddress("Kongens Vej 8080");

        Room r1 = new Room();
        r1.setRoomNumber(1);
        r1.setPrice(1200);
        r1.setHotel(hotel);

        Room r2 = new Room();
        r2.setRoomNumber(2);
        r2.setPrice(1500);
        r2.setHotel(hotel);

        hotel.setRooms(List.of(r1, r2));

        Hotel savedHotel = hotelDAO.createHotel(hotel); // returns entity
        return new ArrayList<>(List.of(savedHotel));
    }

    // Cleanup all hotels and rooms
    public void cleanUpHotels() {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Room").executeUpdate();
            em.createQuery("DELETE FROM Hotel").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE hotel_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE room_id_seq RESTART WITH 1").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
