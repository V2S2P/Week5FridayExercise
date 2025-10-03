package app.Security.SecurityDAOs;

import app.Security.SecurityEntities.Role;
import app.Security.SecurityEntities.User;
import io.javalin.validation.ValidationException;
import jakarta.persistence.EntityNotFoundException;

public interface ISecurityDAO {
    User getVerifiedUser(String username, String password) throws ValidationException, app.exceptions.ValidationException; // used for login
    User createUser(String username, String password); // used for register
    Role createRole(String roleName);
    User addUserRole(String username, String role) throws EntityNotFoundException;
}
