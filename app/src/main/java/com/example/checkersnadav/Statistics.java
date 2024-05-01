package com.example.checkersnadav;

public class Statistics
{
    enum Outcomes { WIN, LOSS, DRAW, NONE }
    private int wins;
    private int losses;
    private int draws;
    private int averageMovesPerGame;
    private int topMoves;

    public Statistics()
    {
        wins = 0;
        losses = 0;
        draws = 0;
        averageMovesPerGame = 0;
        topMoves = 0;
    }

    public Statistics(int wins, int losses, int draws, int averageMovesPerGame, int topMoves) {
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
        this.averageMovesPerGame = averageMovesPerGame;
        this.topMoves = topMoves;
    }

    // Updates the statistics according to the arguments. Returns true if there was a new topMoves record, otherwise returns false.
    public boolean updateStatistics(Outcomes outcome, int moves)
    {
        int totalGames = wins + losses + draws;
        averageMovesPerGame = (averageMovesPerGame * (totalGames) + moves) / (totalGames + 1);

        switch (outcome)
        {
            case WIN:
            {
                wins++;
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
            return true;
        }

        return false;
    }


    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }

    public int getAverageMovesPerGame() {
        return averageMovesPerGame;
    }

    public int getTopMoves() {
        return topMoves;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public void setAverageMovesPerGame(int averageMovesPerGame) {
        this.averageMovesPerGame = averageMovesPerGame;
    }

    public void setTopMoves(int topMoves) {
        this.topMoves = topMoves;
    }
}
