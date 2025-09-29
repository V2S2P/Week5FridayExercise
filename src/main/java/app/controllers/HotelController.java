package app.controllers;

import app.DAOs.HotelDAO;
import app.DTOs.HotelDTO;
import app.DTOs.RoomDTO;
import app.Service.HotelService;
import app.config.HibernateConfig;
import app.entities.Hotel;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class HotelController {
    private final HotelService hotelService;

    public HotelController(EntityManagerFactory emf) {
        this.hotelService = new HotelService(emf);
    }

    public void getAllHotels(Context ctx) {
        ctx.json(hotelService.getAllHotels());
    }

    public void createHotel(Context ctx) {
        HotelDTO newHotel = ctx.bodyValidator(HotelDTO.class).get();
        HotelDTO created = hotelService.createHotel(newHotel);
        if (created != null) {
            ctx.json(created);
            ctx.json("hotel created");
        } else {
            ctx.status(400).result("Invalid hotel data");
        }
    }

    public void getHotelById(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        HotelDTO hotel = hotelService.getHotelById(id);
        if (hotel != null) {
            ctx.json(hotel);
        } else {
            ctx.status(404).result("Hotel not found");
        }
    }

    public void deleteHotel(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        ctx.json(hotelService.deleteHotel(id));
    }

    public void updateHotel(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        HotelDTO dto = ctx.bodyAsClass(HotelDTO.class);
        dto.setId(id);
        ctx.json(hotelService.updateHotel(dto));
    }

    public void addRoom(Context ctx) {
        int hotelId = ctx.pathParamAsClass("id", Integer.class).get();
        RoomDTO roomDTO = ctx.bodyAsClass(RoomDTO.class);
        ctx.json(hotelService.addRoom(hotelId, roomDTO));
    }

    public void removeRoom(Context ctx) {
        int hotelId = ctx.pathParamAsClass("id", Integer.class).get();
        int roomId = ctx.pathParamAsClass("roomId", Integer.class).get();
        ctx.json(hotelService.removeRoom(hotelId, roomId));
    }

    public void getRoomsForHotel(Context ctx) {
        int hotelId = ctx.pathParamAsClass("id", Integer.class).get();
        try {
            List<RoomDTO> rooms = hotelService.getRoomsForHotel(hotelId);
            ctx.json(rooms);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        }
    }
}
