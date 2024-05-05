package com.example.checkersnadav;

/**
 * Represents a game room where two players can join to play checkers.
 */
public class Room
{
    private String roomId;         // Unique identifier for the room
    private String roomOwnerId;    // Identifier of the user who owns the room
    private String player2Id;      // Identifier of the second player who joins the room
    private boolean isGameOngoing; // Flag indicating if a game is currently ongoing in this room
    private String roomName;       // Name of the room

    /**
     * Default constructor required for Firebase Database operations.
     */
    public Room()
    {
    }

    /**
     * Constructs a new Room with the specified owner, room identifier, and room name.
     *
     * @param roomId Unique identifier for the room.
     * @param roomOwnerId Identifier of the user who owns the room.
     * @param roomName Name of the room.
     */
    public Room(String roomId, String roomOwnerId, String roomName)
    {
        this.roomId = roomId;
        this.roomOwnerId = roomOwnerId;
        this.roomName = roomName;
        this.isGameOngoing = false; // Game is not ongoing when the room is first created
        this.player2Id = null; // Initially, there is no second player
    }

    // Getters and Setters

    /**
     * Gets the unique room identifier.
     *
     * @return the room ID.
     */
    public String getRoomId()
    {
        return roomId;
    }

    /**
     * Gets the room owner's identifier.
     *
     * @return the room owner's ID.
     */
    public String getRoomOwnerId()
    {
        return roomOwnerId;
    }

    /**
     * Gets the second player's identifier.
     *
     * @return the second player's ID.
     */
    public String getPlayer2Id()
    {
        return player2Id;
    }

    /**
     * Sets the second player's identifier.
     *
     * @param player2Id the second player's ID.
     */
    public void setPlayer2Id(String player2Id)
    {
        this.player2Id = player2Id;
    }

    /**
     * Checks if the game is ongoing in the room.
     *
     * @return true if a game is ongoing, false otherwise.
     */
    public boolean isGameOngoing()
    {
        return isGameOngoing;
    }

    /**
     * Sets the game ongoing status for the room.
     *
     * @param gameOngoing true to set the game as ongoing, false otherwise.
     */
    public void setGameOngoing(boolean gameOngoing)
    {
        isGameOngoing = gameOngoing;
    }

    /**
     * Checks if the room is joinable, meaning it has no second player and no ongoing game.
     *
     * @return true if the room is joinable, false otherwise.
     */
    public boolean canJoin()
    {
        return player2Id == null && !isGameOngoing;
    }

    /**
     * Gets the name of the room.
     *
     * @return the room name.
     */
    public String getRoomName()
    {
        return roomName;
    }
}
