package app.mappers;

import app.DTOs.RoomDTO;
import app.entities.Hotel;
import app.entities.Room;

public class RoomMapper {
    public static RoomDTO toDTO(Room room) {
        if (room == null) return null;
        return new RoomDTO(
                room.getId(),
                room.getHotel() != null ? room.getHotel().getId() : null,
                room.getRoomNumber(),
                room.getPrice()
        );
    }

    public static Room toEntity(RoomDTO dto, Hotel hotel) {
        if (dto == null) return null;
        return Room.builder()
                .id(dto.getId())
                .hotel(hotel)
                .roomNumber(dto.getRoomNumber())
                .price(dto.getPrice())
                .build();
    }
}
