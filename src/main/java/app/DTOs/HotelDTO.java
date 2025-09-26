package app.DTOs;

import app.entities.Hotel;
import app.entities.Room;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@ToString
public class HotelDTO {
    private Integer id;
    private String name;
    private String address;
    private List<RoomDTO> rooms;

    public HotelDTO(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();

        if (hotel.getRooms() != null) {
            this.rooms = RoomDTO.toDTOList(hotel.getRooms());
        }
    }
    public HotelDTO(Integer id, String name, String address, List<RoomDTO> rooms) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rooms = rooms;
    }

    public static List<HotelDTO> toDTOList(List<Hotel> hotels) {
        return hotels.stream().map(HotelDTO::new).toList();
    }
}
