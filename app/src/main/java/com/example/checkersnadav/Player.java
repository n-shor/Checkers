package com.example.checkersnadav;

public class Player {
    private String username;
    private String email;
    private String hashedPassword;
    private Statistics stats;
    private String lastWinDate;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public Statistics getStats() {
        return stats;
    }

    public void setStats(Statistics stats) {
        this.stats = stats;
    }

    public String getLastWinDate() {
        return lastWinDate;
    }

    public void setLastWinDate(String lastWinDate) {
        this.lastWinDate = lastWinDate;
    }

    public Player()
    {
        // Default constructor required for Firebase DataSnapshot.getValue(Player.class)
    }

    public Player(String username, String email, String hashedPassword) {
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.stats = new Statistics();
        this.lastWinDate = ""; // This way the first comparison for the date of today would always yield false
    }



}