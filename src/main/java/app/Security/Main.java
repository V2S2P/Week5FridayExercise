package app.Security;

import app.Security.SecurityDAOs.ISecurityDAO;
import app.Security.SecurityDAOs.SecurityDAO;
import app.Security.SecurityEntities.Role;
import app.Security.SecurityEntities.User;
import app.config.HibernateConfig;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;

public class Main {
    public static void main(String[] args) {
        ISecurityDAO dao = new SecurityDAO(HibernateConfig.getEntityManagerFactory());

        User user = dao.createUser("user1", "pass123");
        System.out.println(user.getUsername()+": "+user.getPassword());
        Role role = dao.createRole("User");

        User updatedUser = dao.addUserRole("user1", "User");
        System.out.println(updatedUser);

        try {
            User validatedUser = dao.getVerifiedUser("user1", "pass123");
            System.out.println("User was validated: "+validatedUser.getUsername());
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }
}
