package app.DAOs;

import app.DTOs.HotelDTO;
import app.entities.Hotel;
import app.entities.Room;

import java.util.List;

public interface CrudDAO{
    List<Hotel> getAllHotels();
    Hotel getHotelById(int id);
    Hotel createHotel(Hotel hotel);
    Hotel updateHotel(Hotel hotel);
    void deleteHotel(int Id);

    Hotel addRoom(Hotel hotel, Room room);
    Hotel removeRoom(Hotel hotel, Room room);
    List<Room> getRoomsForHotel(Hotel hotel);
}
