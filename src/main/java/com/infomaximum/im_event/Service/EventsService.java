package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Repository.EventsRepository;
import com.infomaximum.im_event.Repository.UsersRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */

@Service
public class EventsService {

    private final UsersRepository usersRepository;
    private final EventsRepository eventsRepository;

    private final ApplicationContext applicationContext;


    public EventsService(UsersRepository usersRepository, EventsRepository eventsRepository, ApplicationContext applicationContext) {
        this.usersRepository = usersRepository;
        this.eventsRepository = eventsRepository;
        this.applicationContext = applicationContext;
    }


    public List<Event> getEventsByType(EVENT_TYPE type){
        return eventsRepository.getEventsByEventType(type);
    }

    public List<Event> getAllEvents(){
        final List<Event> eventList = eventsRepository.findAll();
        if (!eventList.isEmpty()){
            return eventList;
        }
        throw new NoSuchElementException();
    }

    public Event getEventById(Long id){
        final Optional<Event> eventFromDB = eventsRepository.getEventById(id);
        if (eventFromDB.isPresent()){
            return eventFromDB.get();
        }
        throw new NoSuchElementException(eventFromDB.toString());
    }

    public Event getEventByName(String name){
        final Optional<Event> eventFromDB = eventsRepository.getEventByName(name);
        if (eventFromDB.isPresent()){
            return eventFromDB.get();
        }
        throw new NoSuchElementException(eventFromDB.toString());
    }

    public Event addNewEvent(Event event){
        final Optional<Event> eventByName = eventsRepository.getEventByName(event.getName());
        return eventByName.orElseGet(() -> eventsRepository.saveAndFlush(event));
    }

    public Event addEvent(String name, User initiator, String start_date, EVENT_TYPE eventType, Boolean isRepeatable, Double coin, String description){
        final Optional<Event> eventByName = eventsRepository.getEventByName(name);
        if (eventByName.isPresent()){
            return eventByName.get();
        }else {
            final Event event = new Event(name, initiator, start_date, eventType, isRepeatable, description);
            if (coin > 0){
                event.addCoins(coin);
            }
            eventsRepository.saveAndFlush(event);
            return event;
        }
    }

    public boolean changeInitiator(Long eventID, User user) {
        final Optional<Event> event = eventsRepository.getEventById(eventID);
        if (event.isEmpty()) {
            return false;
        }
        event.get().setInitiator(user);
        eventsRepository.saveAndFlush(event.get());
        final Optional<Event> optionalEvent = eventsRepository.getEventById(eventID);
        return optionalEvent
                .map(value -> value.getInitiator().getTelegramId().equals(user.getTelegramId()))
                .orElse(false);
    }

    public List<User> getEventUsers(String event) {
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);
        if (eventByName.isPresent()){
            return eventByName.get().getParticipants();
        }
        return new ArrayList<>();
    }

    public boolean addUserToEvent(User user, Event event){
        event.addParticipant(user);
        eventsRepository.saveAndFlush(event);
        final Optional<Event> eventById = eventsRepository.getEventById(event.getId());
        if (eventById.isPresent()){
            final Optional<User> first = eventById.get().getParticipants().stream()
                    .filter(u -> Objects.equals(u.getTelegramId(), user.getTelegramId()))
                    .filter(u -> u.getEmail().equals(user.getEmail()))
                    .findFirst();
            return first.isPresent();
        }else {
            return false;
        }
    }
    public boolean removeUserToEvent(User user, Event event){
        event.removeParticipant(user);
        eventsRepository.saveAndFlush(event);
        final Optional<Event> eventById = eventsRepository.getEventById(event.getId());
        if (eventById.isPresent()){
            final Optional<User> first = eventById.get().getParticipants().stream()
                    .filter(u -> Objects.equals(u.getTelegramId(), user.getTelegramId()))
                    .filter(u -> u.getEmail().equals(user.getEmail()))
                    .findFirst();
            return first.isEmpty();
        }else {
            return false;
        }
    }

    public Boolean addUserToEvent(String user, String event, String userName) {
        final Optional<User> redactor = usersRepository.getUserByName(user);
        if (redactor.isEmpty()){
            return false;
        }
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);
        final Optional<User> userByName = usersRepository.getUserByName(userName);
        if (eventByName.isEmpty()){
            return false;
//            return String.format("Мероприятие с именем %s не существует", event);
        }
        if (userByName.isEmpty()){
            return false;
//            return String.format("Пользователь с именем %s не существует", userName);
        }
        for (User u: eventByName.get().getParticipants()){
            if (u.getName().equals(user)){
                return false;
//                return "Вы уже зарегистрированы на данное мероприятие";
            }
        }
        eventByName.get().addParticipant(userByName.get());
        eventsRepository.saveAndFlush(eventByName.get());
        return true;
//        return String.format("%s был успешно добавлен на мероприятие %s", userName, event);
    }

    public String deleteEvent(String user, String event) {
        final Optional<User> userByName = usersRepository.getUserByName(user);
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);

        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }
        if (userByName.get().getIsAdmin() || eventByName.get().getInitiator().getName().equals(userByName.get().getName())){
            eventsRepository.delete(eventByName.get());
            return String.format("Событье %s удалена", event);
        }else {
            return String.format("Уважаемый %s! Вы не имеете право удалить данное мероприятие", user);
        }
    }

    public String deleteEvent(Long id) {
        final Optional<Event> eventByName = eventsRepository.getEventById(id);
        if (eventByName.isEmpty()){
            return String.format("Событье с ID %s не существует", id);
        }else {
            eventsRepository.delete(eventByName.get());
            return String.format("Событье с ID %s удалена", id);
        }

    }

    public String addCoinsToEvent(String user, String event, Double coins) {
        final Optional<User> userByName = usersRepository.getUserByName(user);
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);

        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        if (!userByName.get().getIsAdmin()){
            return String.format("Уважаемый %s! Вы не имеете право на добавление дублонов", user);
        }
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }

        eventByName.get().setCoins(coins);
        eventsRepository.flush();
        return String.format("Для мероприятия %s было добавлено %f дублонов", event, coins);
    }

    public String finishEvent(String user, String event) {
        final Optional<User> userByName = usersRepository.getUserByName(user);
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);

        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }
        if (userByName.get().getName().equals(eventByName.get().getInitiator().getName()) || !userByName.get().getIsAdmin()){
            return String.format("Уважаемый %s! Вы не имеете право завершить мероприятие", user);
        }
        eventByName.get().setIsActive(false);
        final Double coins = eventByName.get().getCoins();
        if (coins > 0){
            try {
                final List<User> participants = eventByName.get().getParticipants();
                if (!participants.isEmpty()){
                    final double count = coins / participants.size();
                    participants.forEach(p -> p.setCoins(p.getCoins() + count));
                }
            }finally {
                eventByName.get().setCoins(0.0);
            }

        }
        return String.format("Событье %s завершена", event);
    }

    public String deleteUserFromEvent(String user, String event, String deletingUser) {
        final Optional<User> userByName = usersRepository.getUserByName(user);
        final Optional<User> deletingUserByName = usersRepository.getUserByName(deletingUser);
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);

        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        if (deletingUserByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", deletingUserByName);
        }
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }
        if (userByName.get().getIsAdmin() || userByName.get().getName().equals(deletingUserByName.get().getName())){

            eventByName.get().deleteParticipant(deletingUserByName.get());
            eventsRepository.saveAndFlush(eventByName.get());
            return String.format("Пользователь %s был удален с мероприятия", deletingUserByName);
        }else {
            return String.format("Уважаемый %s! Вы не имеете право удалять участников мероприятия", user);
        }

    }

    public String restartEvent(String user, String event) {
        final Optional<User> userByName = usersRepository.getUserByName(user);
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);

        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }
        if (userByName.get().getIsAdmin() || eventByName.get().getInitiator().getName().equals(userByName.get().getName())){
            eventByName.get().setIsActive(true);
            return String.format("Мероприятие %s была успешно перезапущена", event);
        }else {
            return String.format("Уважаемый %s!Вам не удалось перезапустить мероприятие, возможно у вас нет прав", user);
        }


    }
}
