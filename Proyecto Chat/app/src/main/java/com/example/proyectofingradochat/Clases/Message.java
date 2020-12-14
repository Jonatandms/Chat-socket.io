package com.example.proyectofingradochat.Clases;

public class Message {

    private int mType;
    private String mMessage;
    private String mUsername;

    public Message(String username, String message) {
        this.mMessage=message;
        this.mUsername=username;

    }


    public int getType() {
        return mType;
    };

    public String getMessage() {
        return mMessage;
    };

    public String getUsername() {
        return mUsername;
    };



}
