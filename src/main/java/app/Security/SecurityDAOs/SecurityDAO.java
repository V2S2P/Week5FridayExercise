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
    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException {
        try(EntityManager em = emf.createEntityManager()){
            User foundUser = em.find(User.class, username);
            foundUser.getRoles();

            if(foundUser != null && foundUser.verifyPassword(password)){
                return foundUser;
            }else {
                throw new ValidationException("User or Password was incorrect");
            }
        }
    }

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
}
