package druppert.snhu.eventtracker.entity;

import java.io.Serializable;

public class User implements Serializable {

    private int userId;
    private String username;
    private String phoneNumber;
    private boolean viewingUpcomingEvents = true;

    private User() {
        //Intentionally left private, this class should not be instantiatable without a username
    }

    //CS-499: Support phone number persistence
    public User(String username) {
        this.username = username;
    }

    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    //CS-499: Support phone number persistence
    public User(int userId, String username, String phoneNumber) {
        this.userId = userId;
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    public void setViewingUpcomingEvents(boolean viewingUpcomingEvents) {
        this.viewingUpcomingEvents = viewingUpcomingEvents;
    }
    public boolean getViewingUpcomingEvents() {
        return this.viewingUpcomingEvents;
    }
    private void setUserId(int userId) {
        //Intentionally do nothing, this is an immutable field
        return;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() { return username; }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
