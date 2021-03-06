package com.example.gurpreetsingh.encircleme;

import java.util.Date;

/**
 * Created by GurpreetSingh on 4/18/17.
 */
public class ChatMessageEvent {

    private String messageText;
    private String messageUser;
    private long messageTime;

    public ChatMessageEvent(String messageText, String messageUser) {
        this.messageText = messageText;
        this.messageUser = messageUser;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public ChatMessageEvent(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}