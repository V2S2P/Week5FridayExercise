package app.Security.SecurityDAOs;

import app.Security.SecurityEntities.Role;
import app.Security.SecurityEntities.User;
import app.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

public class SecurityDAO implements ISecurityDAO{
    EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }
    //Checking if username and password (and hashing) is matching with the login info the user inserts.
    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException {
        try(EntityManager em = emf.createEntityManager()){
            //Fetching user by its primary key (username in this case)
            User foundUser = em.find(User.class, username);
            //Force initialization of roles(technically shouldn't be needed as roles are eagerly fetched, but it doesn't seem to work correctly).
            foundUser.getRoles();

            //If user exists and password hashing is matching, we return the user.
            if(foundUser != null && foundUser.verifyPassword(password)){
                return foundUser;
            }else {
                throw new ValidationException("User or Password was incorrect");
            }
        }
    }
    //Creating a User.
    @Override
    public User createUser(String username, String password) {
        try(EntityManager em = emf.createEntityManager()){
            User user = new User(username, password);
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
    }
    //Creating a Role.
    @Override
    public Role createRole(String roleName) {
        try(EntityManager em = emf.createEntityManager()){
            Role role = new Role(roleName);
            em.getTransaction().begin();
            em.persist(role);
            em.getTransaction().commit();
            return role;
        }
    }
    //Add Role to a User.
    @Override
    public User addUserRole(String username, String roleName) throws EntityNotFoundException {
        try(EntityManager em = emf.createEntityManager()){
            User foundUser = em.find(User.class, username);
            Role foundRole = em.find(Role.class, roleName);
            if(foundRole == null || foundUser == null){
                throw new EntityNotFoundException("Either User or Role does not exist");
            }
            em.getTransaction().begin();
            foundUser.addRole(foundRole);
            em.getTransaction().commit();
            return foundUser;
        }
    }
    //A helper method to fetch freshly updated data when using two different EntityManager sessions.
    @Override
    public User getUserByUsername(String username) throws EntityNotFoundException {
        try(EntityManager em = emf.createEntityManager()){
            User foundUser = em.find(User.class, username);
            if(foundUser == null){
                throw new EntityNotFoundException("Either User or Username does not exist");
            }else {
                return foundUser;
            }
        }catch(EntityNotFoundException e){
            throw new EntityNotFoundException("User not found");
        }
    }
}
