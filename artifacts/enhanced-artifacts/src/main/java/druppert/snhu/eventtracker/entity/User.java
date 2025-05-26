package druppert.snhu.eventtracker.entity;

import java.io.Serializable;

/**
 * Represents an EventSync user.
 *
 * Stores identifying and preference data, including username, phone number,
 * and whether the user prefers to view upcoming events or a selected date view.
 *
 * Implements Serializable for Intent data transfer.
 *
 * CS-499
 *
 * Support for phone number persistence
 * Toggle for preferred event display mode
 * Constructors for various instantiation scenarios
 */

public class User implements Serializable {

    private int userId;
    private String username;
    private String phoneNumber;
    private boolean viewingUpcomingEvents = true;

    // Private constructor to prevent instantiation without a username
    private User() {}


    /**
     * Constructor with username only.
     * Used during initial registration before ID assignment.
     */
    public User(String username) {
        this.username = username;
    }

    /**
     * CS-499 Software Engineering: Support phone number persistence
     * Constructor with user ID and username.
     * Used for fully-registered users without phone number data.
     */
    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    /**
     * Full constructor with user ID, username, and phone number.
     * Enables persistence of contact information for SMS.
     * CS-499
     */
    public User(int userId, String username, String phoneNumber) {
        this.userId = userId;
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Sets whether the user prefers to view upcoming events or selected-date events.
     */
    public void setViewingUpcomingEvents(boolean viewingUpcomingEvents) {
        this.viewingUpcomingEvents = viewingUpcomingEvents;
    }
    /**
     * Gets the user's current event view preference.
     * @return true if showing upcoming events, false if showing a selected date.
     */
    public boolean getViewingUpcomingEvents() {
        return this.viewingUpcomingEvents;
    }

    /**
     * Prevent external modification of the user ID.
     * ID should remain immutable.
     */
    private void setUserId(int userId) {
        //Intentionally do nothing, this is an immutable field
        return;
    }

    /**
     * Gets the user's ID.
     * @return The unique user identifier.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the user's username.
     */
    public String getUsername() { return username; }


    /**
     * Gets the user's saved phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user's username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the user's phone number.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
