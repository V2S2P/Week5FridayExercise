package app;

import app.Security.SecurityRoutes.SecurityRoute;
import app.config.ApplicationConfig;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;

public class Main {
    public static void main(String[] args) {
        ApplicationConfig
                .getInstance()
                .initiateServer()
                .checkSecurityRoles() // check for role when route is called
//                .setRoute(SecurityRoutes.getSecurityRoutes())
                .setRoute(SecurityRoute.getSecuredRoutes())
//                .setRoute(new RestRoutes().getOpenRoutes())
//                .setRoute(new RestRoutes().personEntityRoutes) // A different way to get the EndpointGroup.
                .setRoute(new SecurityRoute().getSecurityRoutes())
//                .setRoute(()->{
//                    path("/index",()->{
//                        get("/",ctx->ctx.render("index.html"));
//                    });
//                })
                .startServer(7007)
                .setCORS()
                .setGeneralExceptionHandling();
//            .setErrorHandling()
//                .setApiExceptionHandling();
    }
}