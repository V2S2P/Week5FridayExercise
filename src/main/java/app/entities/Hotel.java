package app.entities;

import app.DTOs.HotelDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String address;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms;

    public Hotel(HotelDTO hotelDTO) {
        this.id = hotelDTO.getId();
        this.name = hotelDTO.getName();
        this.address = hotelDTO.getAddress();

        // Convert RoomDTOs -> Rooms, and set the back-reference
        if (hotelDTO.getRooms() != null) {
            this.rooms = hotelDTO.getRooms().stream()
                    .map(roomDTO -> new Room(roomDTO, this)) // this hotel
                    .toList();
        }
    }
}

