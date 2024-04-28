package com.example.checkersnadav;

public class Room {
    private String roomId;
    private String player1Name;
    private String player2Name;
    private boolean isGameOngoing;

    public Room()
    {
        // Default constructor for Firebase
    }

    public Room(String roomId, String player1)
    {
        this.roomId = roomId;
        this.player1Name = player1;
        this.isGameOngoing = false;
    }

    public String getRoomId()
    {
        return roomId;
    }

    public String getPlayer1() {
        return player1Name;
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
}

