package app.mappers;

import app.DTOs.HotelDTO;
import app.DTOs.RoomDTO;
import app.entities.Hotel;
import app.entities.Room;

import java.util.List;

public class HotelMapper {
    public static HotelDTO toDTO(Hotel hotel) {
        if (hotel == null) return null;
        List<RoomDTO> roomDTOs = hotel.getRooms() != null
                ? hotel.getRooms().stream().map(RoomMapper::toDTO).toList()
                : List.of();
        return new HotelDTO(hotel.getId(), hotel.getName(), hotel.getAddress(), roomDTOs);
    }

    public static Hotel toEntity(HotelDTO dto) {
        if (dto == null) return null;
        Hotel hotel = Hotel.builder()
                .id(dto.getId())
                .name(dto.getName())
                .address(dto.getAddress())
                .build();

        if (dto.getRooms() != null) {
            List<Room> rooms = dto.getRooms().stream()
                    .map(r -> RoomMapper.toEntity(r, hotel))
                    .toList();
            hotel.setRooms(rooms);
        }
        return hotel;
    }
}
