package app.DTOs;

import app.entities.Hotel;
import app.entities.Room;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelDTO {
    private Integer id;
    private String name;
    private String address;
    private List<RoomDTO> rooms;
}
