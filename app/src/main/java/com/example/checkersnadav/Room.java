package com.example.checkersnadav;

public class Room {
    private String roomId;
    private String player1;
    private String player2;
    private boolean isGameOngoing;

    public Room() {
        // Default constructor for Firebase
    }

    public Room(String roomId, String player1) {
        this.roomId = roomId;
        this.player1 = player1;
        this.isGameOngoing = false;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public boolean isGameOngoing() {
        return isGameOngoing;
    }

    public void setGameOngoing(boolean gameOngoing) {
        isGameOngoing = gameOngoing;
    }

    public boolean canJoin() {
        return player2 == null && !isGameOngoing;
    }
}

