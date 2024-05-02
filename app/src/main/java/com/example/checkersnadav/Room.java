package com.example.checkersnadav;

public class Room {
    private String roomId;
    private String roomOwnerId;
    private String player2Id;
    private boolean isGameOngoing;
    private String roomName;

    // Default constructor for Firebase
    public Room() {
    }

    // Constructor for initially creating a room
    public Room(String roomId, String roomOwnerId, String roomName) {
        this.roomId = roomId;
        this.roomOwnerId = roomOwnerId;
        this.roomName = roomName;
        this.isGameOngoing = false; // Game is not ongoing when room is first created
        this.player2Id = null; // Initially, there is no second player
    }

    // Getters and setters
    public String getRoomId() {
        return roomId;
    }

    public String getRoomOwnerId() {
        return roomOwnerId;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(String player2Email) {
        this.player2Id = player2Email;
    }

    public boolean isGameOngoing() {
        return isGameOngoing;
    }

    public void setGameOngoing(boolean gameOngoing) {
        isGameOngoing = gameOngoing;
    }

    // Returns true if the room is joinable
    public boolean canJoin() {
        return player2Id == null && !isGameOngoing;
    }

    public String getRoomName() {
        return roomName;
    }
}
