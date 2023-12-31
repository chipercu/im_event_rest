package com.infomaximum.im_event.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "events")
public class Event {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_name")
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_initiator")
    private User initiator;

    @Column(name = "isActive")
    private Boolean isActive;

    @Column(name = "isRepeatable")
    private Boolean isRepeatable;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_participants")
    private List<User> participants;

    @Column(name = "description")
    private String description;

    @Column(name = "create_date")
    private String create_date;
    @Column(name = "start_date")
    private String start_date;
    @Column(name = "coins")
    private Double coins;

    @Column(name = "event_type")
    private EVENT_TYPE eventType;

    public Event(String name, User initiator, String start_date, EVENT_TYPE eventType, Boolean isRepeatable, String description) {
        this.name = name;
        this.initiator = initiator;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        this.create_date = dateFormat.format(new Date());
        this.start_date = start_date;
        this.coins = 0.0;
        this.eventType = eventType;
        this.isRepeatable = isRepeatable;
        this.isActive = true;
        this.description = description;
    }

    public void addCoins(Double coin) {
        this.coins = coin;
    }

    public boolean addParticipant(User user) {
        return participants.add(user);
    }

    public boolean removeParticipant(User user) {
        final Optional<User> first = participants.stream()
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .filter(u -> u.getTelegramId().equals(user.getTelegramId()))
                .findFirst();
        return first.map(value -> participants.remove(value)).orElse(false);
    }

    public boolean deleteParticipant(User user){
        final Optional<User> first = participants.stream()
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .filter(u -> u.getTelegramId().equals(user.getTelegramId()))
                .findFirst();
        return first.map(value -> participants.remove(value)).orElse(false);
    }
}
