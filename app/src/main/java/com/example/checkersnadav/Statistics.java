package com.example.checkersnadav;

/**
 * This class models the statistics for a player in a game, tracking wins, losses, draws,
 * and move-related metrics.
 */
public class Statistics
{
    /**
     * Enumeration of possible game outcomes.
     */
    public enum Outcomes { WIN, LOSS, DRAW }

    private int elo;
    private int wins;
    private int losses;
    private int draws;
    private int averageMovesPerGame;
    private int topMoves;

    /**
     * Default constructor initializing statistics to zero.
     */
    public Statistics()
    {
        elo = 1000; // This is the default elo value
        wins = 0;
        losses = 0;
        draws = 0;
        averageMovesPerGame = 0;
        topMoves = 0;
    }

    /**
     * Constructor that allows setting initial values for statistics.
     *
     * @param wins                The initial number of wins.
     * @param losses              The initial number of losses.
     * @param draws               The initial number of draws.
     * @param averageMovesPerGame The initial average number of moves per game.
     * @param topMoves            The maximum number of moves made in a single game.
     */
    public Statistics(int wins, int losses, int draws, int averageMovesPerGame, int topMoves)
    {
        elo = 1000; // This is the default elo value
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
        this.averageMovesPerGame = averageMovesPerGame;
        this.topMoves = topMoves;
    }

    /**
     * Updates the player's statistics based on the outcome of a game, the opponent's elo and the number of moves made.
     *
     * @param opponentElo   The opposing player's elo.
     * @param outcome       The outcome of the game (win, loss, or draw).
     * @param moves         The number of moves made in the game.
     * @param hasDailyBonus Flag indicating whether a daily bonus is applied to wins.
     */
    public void updateStatistics(Outcomes outcome, int moves, boolean hasDailyBonus, int opponentElo)
    {
        int totalGames = wins + losses + draws;
        averageMovesPerGame = (averageMovesPerGame * totalGames + moves) / (totalGames + 1);

        // To calculate the Winning probability of this player
        float p = winProbability(elo, opponentElo);

        // Elo change calculation
        int k = 30;
        float eloChange;
        switch (outcome)
        {
            case WIN:
            {
                eloChange = (hasDailyBonus ? 3 : 1) * k * (1 - p);
                wins += hasDailyBonus ? 3 : 1; // 3 wins for daily bonus and only 1 win for winning normally.
                break;
            }
            case LOSS:
            {
                eloChange = k * (0 - p);
                losses++;
                break;
            }
            case DRAW:
            {
                eloChange = k * (0.5f - p);
                draws++;
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Unknown game outcome");
            }
        }

        elo = elo + (int)eloChange;

        if (moves > topMoves)
        {
            topMoves = moves;
        }
    }


    // Getter and setter methods for each field.

    public int getWins()
    {
        return wins;
    }

    public void setWins(int wins)
    {
        this.wins = wins;
    }

    public int getLosses()
    {
        return losses;
    }

    public void setLosses(int losses)
    {
        this.losses = losses;
    }

    public int getDraws()
    {
        return draws;
    }

    public void setDraws(int draws)
    {
        this.draws = draws;
    }

    public int getAverageMovesPerGame()
    {
        return averageMovesPerGame;
    }

    public void setAverageMovesPerGame(int averageMovesPerGame)
    {
        this.averageMovesPerGame = averageMovesPerGame;
    }

    public int getTopMoves()
    {
        return topMoves;
    }

    public void setTopMoves(int topMoves)
    {
        this.topMoves = topMoves;
    }

    public int getElo()
    {
        return elo;
    }

    public void setElo(int elo)
    {
        this.elo = elo;
    }

    /**
     * Helper function to calculate the probability of a victory for a player to win a game against another player
     * @param rating1 the first player's rating.
     * @param rating2 the second player's rating.
     * @return the probability of the first player to win a game against the second player.
     */
    private static float winProbability(float rating1, float rating2)
    {
        return 1.0f / (1.0f + (float)(Math.pow(10, (rating2 - rating1) / 400)));
    }
}
