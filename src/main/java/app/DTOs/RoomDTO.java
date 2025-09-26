package app.DTOs;

import app.entities.Hotel;
import app.entities.Room;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDTO {
    private Integer id;
    private Integer hotelId; // allow null during creation
    private int roomNumber;
    private int price;

    public RoomDTO(Room room){
        this.id = room.getId();
        this.hotelId = room.getHotel() != null ? room.getHotel().getId() : null;
        this.roomNumber = room.getRoomNumber();
        this.price = room.getPrice();
    }

    public static List<RoomDTO> toDTOList(List<Room> rooms) {
        return rooms.stream().map(RoomDTO::new).toList();
    }
}

