package org.gdg_codelab.tchat.model;

/**
 * Created by setico on 22/11/15.
 */
public class Message {
    private String user;
    private String message;

    public Message() {
    }

    public Message(String user, String message) {
        this.user = user;
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
