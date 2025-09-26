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
    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("hotel");
    private final HotelService hotelService = new HotelService(emf);

    public void getAllHotels(Context ctx){
        List<HotelDTO> hotels = hotelService.getAllHotels();
        ctx.json(hotels);
    }
    public void createHotel(Context ctx){
        HotelDTO newHotel = ctx.bodyValidator(HotelDTO.class).get();
        HotelDTO created = hotelService.createHotel(newHotel);
        if (created != null) {
            ctx.json(created);
        }else  {
            throw new IllegalStateException("Incorrect JSON representation");
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

    public void deleteHotel(Context ctx){
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        boolean deleted = hotelService.deleteHotel(id);
        ctx.json(deleted);
    }
    public void updateHotel(Context ctx){
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        HotelDTO updatedHotel = ctx.bodyAsClass(HotelDTO.class);
        updatedHotel.setId(id);
        HotelDTO updated = hotelService.updateHotel(updatedHotel);
        ctx.json(updated);
    }
    public void addRoom(Context ctx) {
        int hotelId = ctx.pathParamAsClass("id", Integer.class).get();
        RoomDTO roomDTO = ctx.bodyAsClass(RoomDTO.class);

        HotelDTO updatedHotel = hotelService.addRoom(hotelId, roomDTO);
        ctx.json(updatedHotel);
    }

    public void removeRoom(Context ctx) {
        int hotelId = ctx.pathParamAsClass("id", Integer.class).get();
        int roomId = ctx.pathParamAsClass("roomId", Integer.class).get();

        HotelDTO updatedHotel = hotelService.removeRoom(hotelId, roomId);
        ctx.json(updatedHotel);
    }

    public void getRoomsForHotel(Context ctx) {
        int hotelId = ctx.pathParamAsClass("id", Integer.class).get();
        List<RoomDTO> rooms = hotelService.getRoomsForHotel(hotelId);
        ctx.json(rooms);
    }


}
