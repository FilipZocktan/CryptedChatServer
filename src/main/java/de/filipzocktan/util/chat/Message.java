package de.filipzocktan.util.chat;

import de.filipzocktan.cryptochat.server.util.User;

public class Message {

    private final User user;
    private final String message;

    public Message(User user, String message) {
        this.user = user;
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }
}
