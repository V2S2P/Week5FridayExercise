package app.Service;

import app.DAOs.HotelDAO;
import app.DTOs.HotelDTO;
import app.DTOs.RoomDTO;
import app.entities.Hotel;
import app.entities.Room;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class HotelService {
    private final HotelDAO hotelDAO;

    public HotelService(EntityManagerFactory emf) {
        this.hotelDAO = HotelDAO.getInstance(emf);
    }

    public List<HotelDTO> getAllHotels() {
        List<Hotel> hotels = hotelDAO.getAllHotels();

        // Access rooms to ensure they are initialized
        hotels.forEach(h -> h.getRooms().size());

        // Convert entities to DTOs
        return hotels.stream()
                .map(HotelDTO::new)
                .toList();
    }

    public HotelDTO createHotel(HotelDTO newHotel){
        Hotel hotel = new Hotel(newHotel);
        Hotel createdHotel = hotelDAO.createHotel(hotel);
        return new HotelDTO(createdHotel);
    }
    public HotelDTO getHotelById(int hotelId) {
        Hotel hotel = hotelDAO.getHotelById(hotelId);
        if (hotel == null) {
            return null; // not found
        }
        return new HotelDTO(hotel); // rooms are already initialized
    }
    public HotelDTO updateHotel(HotelDTO updateHotel){
        Hotel hotel = new Hotel(updateHotel);
        Hotel updatedHotel = hotelDAO.updateHotel(hotel);
        return new HotelDTO(updatedHotel);
    }

    public boolean deleteHotel(int hotelId){
        Hotel hotel = hotelDAO.getHotelById(hotelId);
        if(hotel != null){
            hotelDAO.deleteHotel(hotelId);
            return true;
        }
        return false;
    }
    public HotelDTO addRoom(int hotelId, RoomDTO roomDTO){
        Hotel hotel = hotelDAO.getHotelById(hotelId);
        if(hotel == null){
            return null;
        }
        Room room = new Room(roomDTO,hotel);
        hotel = hotelDAO.addRoom(hotel, room);
        return new HotelDTO(hotel);
    }
    public HotelDTO removeRoom(int hotelId, int roomId){
        Hotel hotel = hotelDAO.getHotelById(hotelId);
        if(hotel == null){
            return null;
        }
        Room room = hotel.getRooms().stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .orElse(null);
        if(room == null){
            return new HotelDTO(hotel);
        }
        hotel = hotelDAO.removeRoom(hotel, room);
        return new HotelDTO(hotel);
    }
    public List<RoomDTO> getRoomsForHotel(int hotelId){
        Hotel hotel = hotelDAO.getHotelById(hotelId);
        if(hotel == null){
            return List.of();
        }
        List<Room> rooms = hotelDAO.getRoomsForHotel(hotel);
        return RoomDTO.toDTOList(rooms);
    }
}
