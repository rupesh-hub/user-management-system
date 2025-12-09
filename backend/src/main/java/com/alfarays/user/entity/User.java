package com.alfarays.user.entity;

import com.alfarays.image.entity.Image;
import com.alfarays.role.entity.Role;
import com.alfarays.shared.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "_users")
@NamedQueries(
        {
                @NamedQuery(name = "User.findByUsername", query = "SELECT U FROM User U WHERE U.username=:username"),
                @NamedQuery(name = "User.findByEmail", query = "SELECT U FROM User U WHERE lower(U.email) = lower(:email)"),
                @NamedQuery(name = "User.emailExists", query = "SELECT CASE WHEN COUNT(U) > 0 THEN TRUE ELSE FALSE END FROM User U WHERE lower(U.email) = lower(:email)"),
                @NamedQuery(name = "User.usernameExists", query = "SELECT CASE WHEN COUNT(U) > 0 THEN TRUE ELSE FALSE END FROM User U WHERE U.username = :username")
        }
)
public class User extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "_user_id_seq_generator")
    @SequenceGenerator(name = "_user_id_seq_generator", sequenceName = "_user_id_seq", allocationSize = 1, initialValue = 1)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    private String firstname;
    private String lastname;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    private String password;

    private LocalDateTime lastLogin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "_users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Image profile;

}
