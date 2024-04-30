package com.example.checkersnadav;

public class Statistics
{
    enum Outcomes { WIN, LOSS, DRAW }
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


    public void updateStatistics(Outcomes outcome, int moves)
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

        topMoves = Math.max(topMoves, moves);
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
}
