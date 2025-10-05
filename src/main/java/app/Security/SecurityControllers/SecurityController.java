package app.Security.SecurityControllers;

import app.Security.SecurityDAOs.ISecurityDAO;
import app.Security.SecurityDAOs.SecurityDAO;
import app.Security.SecurityEntities.User;
import app.config.HibernateConfig;
import app.exceptions.ApiException;
import app.exceptions.ValidationException;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.TokenVerificationException;
import io.javalin.http.*;
import dk.bugelhartmann.UserDTO;
import dk.bugelhartmann.TokenSecurity;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;


public class SecurityController implements ISecurityController {
    ISecurityDAO securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
    ObjectMapper objectMapper = new Utils().getObjectMapper();
    TokenSecurity tokenSecurity = new TokenSecurity();

    @Override
    public Handler login() {
        return (Context ctx) -> {
            User user = ctx.bodyAsClass(User.class);
            try{
                User verified = securityDAO.getVerifiedUser(user.getUsername(),  user.getPassword());
                Set<String> stringRoles = verified.getRoles().stream()
                        .map(roles -> roles.getRoleName())
                        .collect(Collectors.toSet());
                //Returning the hashed password inside the token is perhaps not great for security reasons, avoid sensitive information like that.
                UserDTO userDTO = new UserDTO(verified.getUsername(), verified.getPassword(), stringRoles);
                String token = createToken(userDTO);

                ObjectNode objectNode = objectMapper
                        .createObjectNode()
                        .put("token", token)
                        .put("username", user.getUsername());
                ctx.json(objectNode).status(200);
            }catch (ValidationException ex){
                ObjectNode objectNode = objectMapper.createObjectNode().put("msg","login failed. Wrong username or password.");
                ctx.json(objectNode).status(401);
            }
        };
    }

    @Override
    public Handler register() {
        return (Context ctx) -> {
            User user = ctx.bodyAsClass(User.class);

            try{
                if (securityDAO.getUserByUsername(user.getUsername()) != null){
                    throw new ValidationException("Username is already in use");
                }
                //Using two separate EntityManagers, so when we addUserRole, the createUser EntityManager wouldn't know about that
                //So that's why we "reload" the data by using the getUserByUsername method below, that gives us a "fresh" instance with all the data
                securityDAO.createUser(user.getUsername(), user.getPassword());
                securityDAO.addUserRole(user.getUsername(), "User");

                //Reload user to get fresh data (including roles)
                User newUser = securityDAO.getUserByUsername(user.getUsername());

                Set<String> roles = newUser.getRoles().stream()
                        .map(role -> role.getRoleName())
                        .collect(Collectors.toSet());
                UserDTO userDTO = new UserDTO(newUser.getUsername(), newUser.getPassword(), roles);
                String token = createToken(userDTO);

                ObjectNode jsonResponse = objectMapper.createObjectNode()
                        .put("token", token)
                        .put("username", user.getUsername());
                ctx.json(jsonResponse).status(201);
            }catch (ValidationException ex){
                ObjectNode errorResponse = objectMapper.createObjectNode()
                        .put("msg",ex.getMessage());
                ctx.json(errorResponse).status(400);
            }catch (Exception ex){
                ObjectNode errorResponse = objectMapper.createObjectNode()
                        .put("msg",ex.getMessage());
                ctx.json(errorResponse).status(500);
            }
        };
    }

    @Override
    public Handler authenticate() {
        return (Context ctx) -> {
            //If the request is OPTIONS, send 200 code and continue. No token required.
            if(ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }
            Set<String> allowedRoles = ctx.routeRoles().stream()
                    .map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());
            //If the endpoint route is not protected (no roles needed) or is ANYONE, skip authentication.
            if (isOpenEndpoint(allowedRoles)){
                return;
            }
            //Obtain and verify token, and store UserDTO in attribute as "user" so handlers(authorize, controllers) can access authenticated user info.
            UserDTO verifiedTokenUser = validateAndGetUserFromToken(ctx);
            ctx.attribute("user", verifiedTokenUser);
        };
    }
    private boolean isOpenEndpoint(Set<String> allowedRoles) {
        // If the endpoint is not protected with any roles:
        if (allowedRoles.isEmpty())
            return true;

        // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
        if (allowedRoles.contains("ANYONE")) {
            return true;
        }
        return false;
    }
    @Override
    public Handler authorize() {
        return (Context ctx) -> {
            //Get allowed roles for the route.
            Set<String> allowedRoles = ctx.routeRoles().stream()
                    .map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());
            //If route is open, do nothing.
            if (isOpenEndpoint(allowedRoles)){
                return;
            }
            //Retrieve user from attribute we sat in authenticate method above.
            UserDTO user = ctx.attribute("user");
            //If not present, throw exception.
            if (user == null){
                throw new ForbiddenResponse("No user was added from the token");
            }
            //If user doesn't have allowed role, throw exception with message.
            if (!userHasAllowedRole(user, allowedRoles))
                throw new ForbiddenResponse("User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
        };
    }
    //Checks if UserDTO contains any role that matches the allowed roles(case-insensitive)
    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }
    //Creates a token for the given UserDTO.
    private String createToken(UserDTO user) {
        try {
          //The variables needed for TokenSecurity
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            //If the environment variable DEPLOYED is set, we use the environment variables.
            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
            //If DEPLOYED is not set, we read from the config.properties file we have created.
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }
            System.out.println("Creating token for user: " + ISSUER);
            //Call createToken method with our variables and return the newly made token string.
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e) {
//            logger.error("Could not create token", e);
            throw new ApiException(500, "Could not create token");
        }
    }
    //Making sure the extraction of the token is done correctly, we check for some errors that could happen when trying token extraction.
    private static String getToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null) {
            throw new UnauthorizedResponse("Authorization header is missing"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }

        // If the Authorization Header was malformed, then no entry
        String token = header.split(" ")[1];
        if (token == null) {
            throw new UnauthorizedResponse("Authorization header is malformed"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }
        return token;
    }
    //Verification of token. Making sure the token is valid and not expired.
    private UserDTO verifyToken(String token) {
        //Deciding where to look for the secret_key, depending on if DEPLOYED is set or not.
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
            //If valid, extract UserDTO from token and returns it.
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            } else {
                throw new UnauthorizedResponse("Token not valid");
            }
        } catch (ParseException | TokenVerificationException e) {
//            logger.error("Could not create token", e);
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        }
    }
    //Extracts token from request and verifies it, returning UserDTO or error if something is wrong.
    private UserDTO validateAndGetUserFromToken(Context ctx) {
        String token = getToken(ctx);
        UserDTO verifiedTokenUser = verifyToken(token);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }
        return verifiedTokenUser;
    }
}
