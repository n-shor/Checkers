package com.example.checkersnadav;

/**
 * Represents a player in the application, holding all relevant information
 * such as username, email, password (hashed), and game statistics.
 */
public class Player
{
    private String username;           // Username of the player.
    private String email;              // Email address of the player, used for login.
    private String hashedPassword;     // Hashed password for secure storage and authentication.
    private Statistics stats;          // Player's game statistics.
    private String lastWinDate;        // Date of the last win to manage rewards or achievements.

    /**
     * Default constructor required for Firebase DataSnapshot.getValue(Player.class).
     */
    public Player()
    {
        // Firebase requires a no-argument constructor to deserialize custom objects.
    }

    /**
     * Constructs a new Player with specified details.
     * @param username The username of the player.
     * @param email The email address of the player.
     * @param hashedPassword The hashed password for secure authentication.
     */
    public Player(String username, String email, String hashedPassword)
    {
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.stats = new Statistics();  // Initialize default statistics.
        this.lastWinDate = "";          // Initialize with an empty string to handle first win comparisons.
    }

    // Getters and setters
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getHashedPassword()
    {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword)
    {
        this.hashedPassword = hashedPassword;
    }

    public Statistics getStats()
    {
        return stats;
    }

    public void setStats(Statistics stats)
    {
        this.stats = stats;
    }

    public String getLastWinDate()
    {
        return lastWinDate;
    }

    public void setLastWinDate(String lastWinDate)
    {
        this.lastWinDate = lastWinDate;
    }
}
