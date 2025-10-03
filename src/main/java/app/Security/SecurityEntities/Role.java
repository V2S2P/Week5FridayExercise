package app.Security.SecurityEntities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Role {
    @Id
    @Column(name = "role_name",nullable = false)
    private String roleName;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    Set<User> users;

    public Role(String roleName) {
        this.roleName = roleName;
    }
}
