package com.example.socialmemedia;

public class Chat {

    private String sender;
    private String message;

    public Chat(String givenSender, String givenMessage){
        this.sender=givenSender;
        this.message=givenMessage;

    }

    public Chat(){

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
