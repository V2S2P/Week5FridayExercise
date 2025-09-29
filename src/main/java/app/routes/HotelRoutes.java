package app.routes;

import app.config.HibernateConfig;
import app.controllers.HotelController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;

public class HotelRoutes {
    private final HotelController hotelController;

    public HotelRoutes() {
        this.hotelController = new HotelController(
                HibernateConfig.getEntityManagerFactory("hotel")
        );
    }

    public EndpointGroup getRoutes() {
        return () -> {
            get("/", hotelController::getAllHotels);
            get("/{id}", hotelController::getHotelById);
            post("/", hotelController::createHotel);
            put("/{id}", hotelController::updateHotel);
            delete("/{id}", hotelController::deleteHotel);
            get("/{id}/rooms", hotelController::getRoomsForHotel);
            post("/{id}/rooms", hotelController::addRoom);
            delete("/{id}/rooms/{roomId}", hotelController::removeRoom);
        };
    }
}
