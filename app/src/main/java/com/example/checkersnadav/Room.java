package com.example.checkersnadav;

public class Room {
    private String roomId;
    private String roomOwnerEmail;
    private String player2Name;
    private boolean isGameOngoing;
    private String roomName;

    public Room()
    {
        // Default constructor for Firebase
    }

    public Room(String roomId, String roomOwnerEmail, String roomName)
    {
        this.roomId = roomId;
        this.roomOwnerEmail = roomOwnerEmail;
        this.roomName = roomName;
        this.isGameOngoing = false;
    }

    public String getRoomId()
    {
        return roomId;
    }

    public String getRoomOwnerEmail() {
        return roomOwnerEmail;
    }

    public String getPlayer2() {
        return player2Name;
    }

    public void setPlayer2(String player2) {
        this.player2Name = player2;
    }

    public boolean isGameOngoing() {
        return isGameOngoing;
    }

    public void setGameOngoing(boolean gameOngoing) {
        isGameOngoing = gameOngoing;
    }

    public boolean canJoin() {
        return player2Name == null && !isGameOngoing;
    }

    public String getRoomName()
    {
        return roomName;
    }
}

