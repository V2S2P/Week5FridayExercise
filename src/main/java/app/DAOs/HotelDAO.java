package app.DAOs;

import app.DTOs.HotelDTO;
import app.entities.Hotel;
import app.entities.Room;
import jakarta.persistence.*;

import java.util.List;

public class HotelDAO implements CrudDAO {
    private static HotelDAO instance;
    private static EntityManagerFactory emf;

    private HotelDAO() {}

    public static HotelDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new HotelDAO();
            HotelDAO.emf = emf;
        }
        return instance;
    }

    @Override
    public List<Hotel> getAllHotels() {
        try(EntityManager em = emf.createEntityManager()) {
            TypedQuery<Hotel> query = em.createQuery(
                    "SELECT DISTINCT h FROM Hotel h LEFT JOIN FETCH h.rooms", Hotel.class);
            return query.getResultList();
        }
    }

    @Override
    public Hotel getHotelById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Hotel> query = em.createQuery(
                    "SELECT h FROM Hotel h LEFT JOIN FETCH h.rooms WHERE h.id = :id",
                    Hotel.class
            );
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Hotel createHotel(Hotel hotel) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(hotel);
            em.getTransaction().commit();
            return hotel;
        }
    }

    @Override
    public Hotel updateHotel(Hotel hotel) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Hotel updated = em.merge(hotel);
            em.getTransaction().commit();
            return updated;
        }
    }

    @Override
    public void deleteHotel(int hotelId) {
        try(EntityManager em = emf.createEntityManager()) {
            Hotel hotel = em.find(Hotel.class, hotelId);
            if (hotel != null) {
                em.getTransaction().begin();
                em.remove(hotel);
                em.getTransaction().commit();
            }
        }
    }
    //Used this method to avoid LazyInitializationException
    public Hotel addRoom(Hotel hotel, Room room) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            hotel = em.merge(hotel); // ensure managed
            room.setHotel(hotel);    // link the room
            em.persist(room);

            em.getTransaction().commit();

            // reload with join fetch
            return em.createQuery(
                            "SELECT h FROM Hotel h LEFT JOIN FETCH h.rooms WHERE h.id = :id",
                            Hotel.class
                    )
                    .setParameter("id", hotel.getId())
                    .getSingleResult();
        }
    }
    /*@Override
    public Hotel addRoom(Hotel hotel, Room room) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            hotel = em.find(Hotel.class, hotel.getId());
            room.setHotel(hotel);
            hotel.getRooms().add(room);
            em.persist(room);
            em.getTransaction().commit();
            return hotel;
        }
    }
     */

    @Override
    public Hotel removeRoom(Hotel hotel, Room room) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            hotel = em.find(Hotel.class, hotel.getId());
            room = em.find(Room.class, room.getId());
            if (room != null) {
                hotel.getRooms().remove(room);
                em.remove(room);
            }
            em.getTransaction().commit();
            return hotel;
        }
    }

    @Override
    public List<Room> getRoomsForHotel(Hotel hotel) {
        try(EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT r FROM Room r WHERE r.hotel = :hotel", Room.class)
                    .setParameter("hotel", hotel)
                    .getResultList();
        }
    }
}
