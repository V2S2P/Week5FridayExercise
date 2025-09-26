package app.DAOs;

import app.DTOs.HotelDTO;
import app.entities.Hotel;
import app.entities.Room;
import jakarta.persistence.*;

import java.util.List;

public class HotelDAO implements CrudDAO{
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
            //LEFT JOIN FETCH h.rooms tells Hibernate to fetch the rooms eagerly in the same query.
            //DISTINCT ensures that hotels don’t get duplicated in the result list if a hotel has multiple rooms.
            TypedQuery<Hotel> query = em.createQuery("SELECT DISTINCT h FROM Hotel h LEFT JOIN FETCH h.rooms", Hotel.class);
            return query.getResultList();
        }
    }

    @Override
    public Hotel getHotelById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            // Fetch hotel and rooms eagerly in one query
            TypedQuery<Hotel> query = em.createQuery(
                    "SELECT h FROM Hotel h LEFT JOIN FETCH h.rooms WHERE h.id = :id",
                    Hotel.class
            );
            query.setParameter("id", id);

            // Returns the hotel with rooms initialized
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // hotel not found
        }
    }

    @Override
    public Hotel createHotel(Hotel hotel) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(hotel);
            em.getTransaction().commit();
            return hotel;
        }catch (IllegalStateException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Hotel updateHotel(Hotel hotel) {
        try(EntityManager em = emf.createEntityManager()) {
            Hotel updatedHotel = em.find(Hotel.class, hotel.getId());
            if (updatedHotel != null) {
                em.getTransaction().begin();
                em.merge(hotel);
                em.getTransaction().commit();
                return hotel;
            }else {
                return null;
            }
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

    @Override
    public Hotel addRoom(Hotel hotel, Room room) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // fetch hotel with rooms eagerly
            hotel = em.createQuery("SELECT h FROM Hotel h LEFT JOIN FETCH h.rooms WHERE h.id = :id", Hotel.class)
                    .setParameter("id", hotel.getId())
                    .getSingleResult();

            room.setHotel(hotel);
            hotel.getRooms().add(room);
            em.persist(room);

            hotel.getRooms().size();

            em.getTransaction().commit();
            return hotel;
        }
    }

    @Override
    public Hotel removeRoom(Hotel hotel, Room room) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Fetch hotel with rooms eagerly
            hotel = em.createQuery(
                            "SELECT h FROM Hotel h LEFT JOIN FETCH h.rooms WHERE h.id = :id",
                            Hotel.class
                    )
                    .setParameter("id", hotel.getId())
                    .getSingleResult();

            // Find the room to remove
            room = em.find(Room.class, room.getId());

            if (room != null) {
                hotel.getRooms().remove(room); // remove from hotel's collection
                em.remove(room);               // remove from DB
            }

            em.getTransaction().commit();

            return hotel;
        }
    }

    @Override
    public List<Room> getRoomsForHotel(Hotel hotel) {
        try(EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT r FROM Room r WHERE r.hotel = :hotel",Room.class)
                    .setParameter("hotel", hotel)
                    .getResultList();
        }
    }
}
