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
    enum Outcomes { WIN, LOSS, DRAW }

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
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
        this.averageMovesPerGame = averageMovesPerGame;
        this.topMoves = topMoves;
    }

    /**
     * Updates the player's statistics based on the outcome of a game and the number of moves made.
     *
     * @param outcome      The outcome of the game (win, loss, or draw).
     * @param moves        The number of moves made in the game.
     * @param hasDailyBonus Flag indicating whether a daily bonus is applied to wins.
     */
    public void updateStatistics(Outcomes outcome, int moves, boolean hasDailyBonus)
    {
        int totalGames = wins + losses + draws;
        averageMovesPerGame = (averageMovesPerGame * totalGames + moves) / (totalGames + 1);

        switch (outcome)
        {
            case WIN:
            {
                wins += hasDailyBonus ? 3 : 1;
                break;
            }
            case LOSS:
            {
                losses++;
                break;
            }
            case DRAW:
            {
                draws++;
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Unknown game outcome");
            }
        }

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
}
