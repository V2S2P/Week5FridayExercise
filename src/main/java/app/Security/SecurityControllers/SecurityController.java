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
        return null;
    }

    @Override
    public Handler authenticate() {
        return (Context ctx) -> {
            if(ctx.method().toString().equals("OPTIONS")) {
                ctx.status(200);
                return;
            }
            Set<String> allowedRoles = ctx.routeRoles().stream()
                    .map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());
            if (isOpenEndpoint(allowedRoles)){
                return;
            }
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
            Set<String> allowedRoles = ctx.routeRoles().stream()
                    .map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());
            if (isOpenEndpoint(allowedRoles)){
                return;
            }
            UserDTO user = ctx.attribute("user");
            if (user == null){
                throw new ForbiddenResponse("No user was added from the token");
            }
            if (!userHasAllowedRole(user, allowedRoles))
                throw new ForbiddenResponse("User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
        };
    }
    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }
    private String createToken(UserDTO user) {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }
            System.out.println("Creating token for user: " + ISSUER);
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e) {
//            logger.error("Could not create token", e);
            throw new ApiException(500, "Could not create token");
        }
    }
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
    private UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
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
    private UserDTO validateAndGetUserFromToken(Context ctx) {
        String token = getToken(ctx);
        UserDTO verifiedTokenUser = verifyToken(token);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }
        return verifiedTokenUser;
    }
}
