package com.infomaximum.im_event.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
@EqualsAndHashCode
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    @Column(name = "isAdmin")
    private Boolean isAdmin;
    @Column(name = "user_name")
    private String name;
    @Column(name = "user_surname")
    private String surname;
    @Column(name = "user_password")
    private String password;
    @Column(name = "user_email")
    private String email;
    @Column(name = "coins")
    private Double coins;
    @Column(name = "telegramId")
    private Long telegramId;
    @Column
    private String telegramUrl;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_events")
    private List<Event> events;

}
