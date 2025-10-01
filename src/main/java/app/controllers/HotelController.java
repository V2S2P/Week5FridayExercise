package app.controllers;

import app.DAOs.HotelDAO;
import app.DTOs.HotelDTO;
import app.DTOs.RoomDTO;
import app.Service.HotelService;
import app.config.HibernateConfig;
import app.entities.Hotel;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManagerFactory;
import org.eclipse.jetty.websocket.api.StatusCode;

import java.util.List;

public class HotelController {
    private final HotelService hotelService;

    public HotelController(EntityManagerFactory emf) {
        this.hotelService = new HotelService(emf);
    }

    public void getAllHotels(Context ctx) {
        ctx.json(hotelService.getAllHotels())
                .status(HttpStatus.OK);
    }

    public void createHotel(Context ctx) {
        HotelDTO newHotel = ctx.bodyValidator(HotelDTO.class).get();
        HotelDTO created = hotelService.createHotel(newHotel);
        if (created != null) {
            ctx.status(HttpStatus.CREATED);
            ctx.json(created);
        } else {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("Invalid hotel data");
        }
    }

    public void getHotelById(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        HotelDTO hotel = hotelService.getHotelById(id);
        if (hotel != null) {
            ctx.status(HttpStatus.OK);
            ctx.json(hotel);
        } else {
            ctx.status(HttpStatus.NOT_FOUND)
                    .result("Hotel not found");
        }
    }

    public void deleteHotel(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        boolean deleted = hotelService.deleteHotel(id);

        if (deleted) {
            ctx.status(HttpStatus.NO_CONTENT); // 204, no response body
        }else {
            ctx.status(HttpStatus.NOT_FOUND)
                    .result("Hotel not found");
        }
    }

    public void updateHotel(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        HotelDTO dto = ctx.bodyAsClass(HotelDTO.class);
        dto.setId(id);

        HotelDTO updated = hotelService.updateHotel(dto);

        if (updated != null) {
            ctx.status(HttpStatus.OK)
                    .json(updated);
        }else {
            ctx.status(HttpStatus.NOT_FOUND)
                    .result("Hotel not found");
        }
    }

    public void addRoom(Context ctx) {
        int hotelId = ctx.pathParamAsClass("id", Integer.class).get();
        RoomDTO roomDTO = ctx.bodyAsClass(RoomDTO.class);
        RoomDTO created = hotelService.addRoom(hotelId, roomDTO);

        if (created != null) {
            ctx.status(HttpStatus.CREATED)
                    .json(created);
        }else {
            ctx.status(HttpStatus.NOT_FOUND)
                    .result("Hotel not found");
        }

    }

    public void removeRoom(Context ctx) {
        int hotelId = ctx.pathParamAsClass("id", Integer.class).get();
        int roomId = ctx.pathParamAsClass("roomId", Integer.class).get();

        boolean removed = hotelService.removeRoom(hotelId, roomId);

        if (removed) {
            ctx.status(HttpStatus.NO_CONTENT);
        } else {
            ctx.status(HttpStatus.NOT_FOUND).result("Hotel or room not found");
        }
    }

    public void getRoomsForHotel(Context ctx) {
        int hotelId = ctx.pathParamAsClass("id", Integer.class).get();
        try {
            List<RoomDTO> rooms = hotelService.getRoomsForHotel(hotelId);
            ctx.json(rooms)
                    .status(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .result(e.getMessage());
        }
    }
}
