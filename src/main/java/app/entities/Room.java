package app.entities;

import app.DTOs.RoomDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_id") // FK column in DB
    private Hotel hotel;

    private int roomNumber;
    private int price;

    public Room(RoomDTO roomDTO, Hotel hotel){
        this.id = roomDTO.getId();
        this.hotel = hotel;
        this.roomNumber = roomDTO.getRoomNumber();
        this.price = roomDTO.getPrice();
    }
}

