package app.Security.SecurityEntities;

import jakarta.persistence.*;
import lombok.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "users")
public class User implements ISecurityUser{
    @Id
    @Column(name = "username",nullable = false)
    private String username;

    private String password;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    private Set<Role> roles;

    public User(String username, String password){
        this.username = username;
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        this.password = hashed;
    }

    @Override
    public boolean verifyPassword(String pw) {
        return BCrypt.checkpw(pw, password);
    }

    @Override
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    @Override
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }
}
