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
    private Integer hotelId;
    private int roomNumber;
    private int price;
}

