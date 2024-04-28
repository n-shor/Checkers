package com.example.checkersnadav;

public class Room {
    private String roomId;
    private String roomOwnerEmail;
    private String player2Email;
    private boolean isGameOngoing;
    private String roomName;

    // Default constructor for Firebase
    public Room() {
    }

    // Constructor for initially creating a room
    public Room(String roomId, String roomOwnerEmail, String roomName) {
        this.roomId = roomId;
        this.roomOwnerEmail = roomOwnerEmail;
        this.roomName = roomName;
        this.isGameOngoing = false; // Game is not ongoing when room is first created
        this.player2Email = null; // Initially, there is no second player
    }

    // Getters and setters
    public String getRoomId() {
        return roomId;
    }

    public String getRoomOwnerEmail() {
        return roomOwnerEmail;
    }

    public String getPlayer2Email() {
        return player2Email;
    }

    public void setPlayer2Email(String player2Email) {
        this.player2Email = player2Email;
    }

    public boolean isGameOngoing() {
        return isGameOngoing;
    }

    public void setGameOngoing(boolean gameOngoing) {
        isGameOngoing = gameOngoing;
    }

    // Returns true if the room is joinable
    public boolean canJoin() {
        return player2Email == null && !isGameOngoing;
    }

    public String getRoomName() {
        return roomName;
    }
}
