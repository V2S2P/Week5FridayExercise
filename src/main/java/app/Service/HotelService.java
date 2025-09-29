package app.Service;

import app.DAOs.HotelDAO;
import app.DTOs.HotelDTO;
import app.DTOs.RoomDTO;
import app.entities.Hotel;
import app.entities.Room;
import app.mappers.HotelMapper;
import app.mappers.RoomMapper;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class HotelService {
    private final HotelDAO hotelDAO;

    public HotelService(EntityManagerFactory emf) {
        this.hotelDAO = HotelDAO.getInstance(emf);
    }

    public List<HotelDTO> getAllHotels() {
        return hotelDAO.getAllHotels().stream()
                .map(HotelMapper::toDTO)
                .toList();
    }

    public HotelDTO getHotelById(int hotelId) {
        return HotelMapper.toDTO(hotelDAO.getHotelById(hotelId));
    }

    public HotelDTO createHotel(HotelDTO dto) {
        Hotel hotel = HotelMapper.toEntity(dto);
        return HotelMapper.toDTO(hotelDAO.createHotel(hotel));
    }

    public HotelDTO updateHotel(HotelDTO dto) {
        Hotel hotel = HotelMapper.toEntity(dto);
        return HotelMapper.toDTO(hotelDAO.updateHotel(hotel));
    }

    public boolean deleteHotel(int id) {
        Hotel hotel = hotelDAO.getHotelById(id);
        if (hotel != null) {
            hotelDAO.deleteHotel(id);
            return true;
        }
        return false;
    }

    public HotelDTO addRoom(int hotelId, RoomDTO roomDTO) {
        Hotel hotel = hotelDAO.getHotelById(hotelId);
        if (hotel == null) return null;
        Room room = RoomMapper.toEntity(roomDTO, hotel);
        return HotelMapper.toDTO(hotelDAO.addRoom(hotel, room));
    }

    public HotelDTO removeRoom(int hotelId, int roomId) {
        Hotel hotel = hotelDAO.getHotelById(hotelId);
        if (hotel == null) return null;
        Room room = hotel.getRooms().stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .orElse(null);
        if (room == null) return HotelMapper.toDTO(hotel);
        return HotelMapper.toDTO(hotelDAO.removeRoom(hotel, room));
    }

    public List<RoomDTO> getRoomsForHotel(int hotelId) {
        Hotel hotel = hotelDAO.getHotelById(hotelId);
        if (hotel == null){
            throw new IllegalArgumentException("Hotel id " + hotelId + " does not exist");
        }
        return hotelDAO.getRoomsForHotel(hotel).stream()
                .map(RoomMapper::toDTO)
                .toList();
    }
}

