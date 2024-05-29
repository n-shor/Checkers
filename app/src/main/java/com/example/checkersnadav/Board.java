package com.example.checkersnadav;

/**
 * Represents the game board for a checkers game.
 * Manages the game state including the positions of pieces, whose turn it is, and the rules for moving and capturing pieces.
 */
public class Board
{
    public static final boolean WHITE = false; // Represents the White player.
    public static final boolean BLACK = !WHITE; // Represents the Black player, opposite of White.
    public static final int BOARD_SIZE = 8; // The size of the game board (8x8).
    private final Piece[][] state; // Array to store the pieces on the board.
    private boolean turn; // Boolean flag to indicate whose turn it is; true for Black, false for White.
    private int movesSinceCaptureOrKing = 0; // Counts moves since the last capture or kinging to check for draws.
    private int lastMoveX = BOARD_SIZE - 1; // Tracks the x-coordinate of the last move.
    private int lastMoveY = BOARD_SIZE - 2; // Tracks the y-coordinate of the last move, ensuring it's initially set on a black square.

    /**
     * Initializes a new Board with pieces in their standard positions for a game of checkers.
     */
    public Board()
    {
        state = new Piece[BOARD_SIZE][BOARD_SIZE];
        turn = WHITE; // White starts the game.

        // Initialize board: placing pieces in the correct starting positions.
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                if ((i + j) % 2 != 0)
                { // Pieces are placed on squares where the sum of indices is odd.
                    if (i <= 2)
                    {
                        state[i][j] = new Piece(WHITE); // Initial positions for White pieces.
                    }
                    else if (i >= 5)
                    {
                        state[i][j] = new Piece(BLACK); // Initial positions for Black pieces.
                    }
                    else
                    {
                        state[i][j] = null; // Empty squares.
                    }
                }
                else
                {
                    state[i][j] = null; // Empty squares where the sum of indices is even.
                }
            }
        }
    }

    /**
     * Attempts to move a piece from the source to the destination coordinates.
     * Validates the move, performs the move, handles captures, and switches turns if applicable.
     *
     * @param xSrc Source x-coordinate of the piece being moved.
     * @param ySrc Source y-coordinate of the piece being moved.
     * @param xDst Destination x-coordinate for the move.
     * @param yDst Destination y-coordinate for the move.
     * @return true if the move is successfully made, false if the move is invalid.
     */
    public boolean move(int xSrc, int ySrc, int xDst, int yDst)
    {
        if (!isValidMove(xSrc, ySrc, xDst, yDst))
        {
            return false; // Move is invalid if it does not comply with the rules of Checkers.
        }

        Piece piece = state[xSrc][ySrc];
        state[xDst][yDst] = piece; // Move the piece to the new position.
        state[xSrc][ySrc] = null; // Clear the original position.

        movesSinceCaptureOrKing++; // Increment the move counter for draw conditions.

        // Handle captures for regular pieces and kings.
        if (!piece.isKing() && Math.abs(xDst - xSrc) == 2)
        {
            performPieceCapture(xSrc, ySrc, xDst, yDst);
            // Check for a mandatory capture for a piece, or a mandatory capture for a king in case the piece just got promoted.
            if (pieceHasMandatoryCapture(xDst, yDst) ||
                    (((xDst == 0 && piece.isBlack() == BLACK) || (xDst == BOARD_SIZE - 1 && piece.isBlack() == WHITE)) && kingHasMandatoryCapture(xDst, yDst)))
            {
                turn = !turn; // Maintain the turn if further captures are possible.
            }
            movesSinceCaptureOrKing = 0; // Reset the counter after a capture.
        }
        // King capture
        // making sure the path is not clear - meaning there is something to capture
        // (we already know the move is valid so we don't have to worry about isPathClear() not being specific enough)
        // also, it doesn't matter that the piece has moved already because we only check the squares that are in between the source and the destination squares
        else if (!isPathClear(xSrc, ySrc, xDst, yDst))
        {
            performKingCapture(xSrc, ySrc, xDst, yDst);
            if (kingHasMandatoryCapture(xDst, yDst))
            {
                turn = !turn; // Maintain the turn if further captures are possible.
            }
            movesSinceCaptureOrKing = 0; // Reset the counter after a capture.
        }

        // Handle promotion to King.
        if (!piece.isKing() && ((xDst == 0 && piece.isBlack() == BLACK) || (xDst == BOARD_SIZE - 1 && piece.isBlack() == WHITE)))
        {
            piece.setKing(true);
            movesSinceCaptureOrKing = 0; // Reset the counter on kinging.
        }

        turn = !turn; // Switch turns if the move completes without further capture options.

        lastMoveX = xDst; // Update the last move coordinates.
        lastMoveY = yDst;

        return true;
    }

    /**
     * Checks if the specified piece at the given board location can legally move or capture.
     * This method checks all potential simple moves and captures for the piece, including special moves if the piece is a king.
     *
     * @param x The x-coordinate of the piece on the board.
     * @param y The y-coordinate of the piece on the board.
     * @return true if there is at least one legal move or capture available, false otherwise.
     */
    private boolean canMove(int x, int y)
    {
        Piece piece = state[x][y];
        if (piece == null)
        {
            return false; // No piece at this position, so it cannot move.
        }

        // Define possible move increments for checkers (move one or jump two)
        int[] possibleMoves = {1, -1};
        for (int dx : possibleMoves)
        {
            for (int dy : possibleMoves)
            {
                int newX = x + dx;
                int newY = y + dy;

                // Check simple moves
                if (isValidMove(x, y, newX, newY))
                {
                    return true; // Return immediately if a valid move is found
                }

                // Check captures
                newX = x + 2 * dx;
                newY = y + 2 * dy;
                if (isValidMove(x, y, newX, newY))
                {
                    return true; // Return immediately if a valid capture is found
                }

                // Additional checks for king moves that can move multiple squares in one turn
                if (piece.isKing())
                {
                    for (int i = 2; i < BOARD_SIZE; i++)
                    {
                        newX = x + i * dx;
                        newY = y + i * dy;
                        if (isValidMove(x, y, newX, newY))
                        {
                            return true; // Return immediately if a valid king move is found
                        }
                    }
                }
            }
        }
        return false; // No valid moves found
    }

    /**
     * Determines the winner of the game by checking the presence and mobility of pieces for each player.
     * It evaluates the board to decide if either player has no pieces or cannot move, which would end the game.
     *
     * @return A string indicating the winner ('BLACK' or 'WHITE'), 'DRAW' for a stalemate, or 'NONE' if the game continues.
     */
    public String getWinner()
    {
        boolean whiteHasPieces = false;
        boolean blackHasPieces = false;
        boolean whiteHasMoves = false;
        boolean blackHasMoves = false;

        // Iterate through the board to find pieces and check for possible moves.
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                Piece piece = state[i][j];
                if (piece != null)
                {
                    if (piece.isBlack())
                    {
                        blackHasPieces = true;
                        if (!blackHasMoves)
                        {
                            blackHasMoves = canMove(i, j); // Check for moves if not already found
                        }
                    }
                    else
                    {
                        whiteHasPieces = true;
                        if (!whiteHasMoves)
                        {
                            whiteHasMoves = canMove(i, j); // Check for moves if not already found
                        }
                    }
                }
            }
        }

        // Determine the winner based on pieces left and possible moves.
        if (!whiteHasPieces || (!whiteHasMoves && turn == WHITE))
        {
            return Game.BLACK_STRING; // Black wins if White has no pieces or cannot move
        }
        if (!blackHasPieces || (!blackHasMoves && turn == BLACK))
        {
            return Game.WHITE_STRING; // White wins if Black has no pieces or cannot move
        }
        return Game.NONE_STRING; // No winner if the game is still ongoing
    }

    /**
     * Checks if the game is over by evaluating if a win or a draw condition has been met.
     * This method uses the getWinner method and checks for draw conditions based on consecutive non-capturing moves.
     *
     * @return A string indicating the game outcome ('BLACK', 'WHITE', 'DRAW'), or 'NONE' if the game is not over.
     */
    public String checkGameStatus()
    {
        String winner = getWinner();
        if (!winner.equals(Game.NONE_STRING))
        {
            return winner; // Return the winner if one has been determined
        }
        if (movesSinceCaptureOrKing >= 80)
        {
            return Game.DRAW_STRING; // Declare a draw if there are 80 moves by both players without a capture or kinging
        }
        return Game.NONE_STRING; // The game continues if no end condition is met
    }

    /**
     * Returns the current state of the game board as a 2D array of Pieces.
     * This can be used to visualize the board or analyze the game state.
     *
     * @return The 2D array representing the current arrangement of pieces on the board.
     */
    public Piece[][] getState()
    {
        return state;
    }

    /**
     * Checks if the specified move is valid according to the rules of checkers.
     * This includes ensuring the move stays within the bounds of the board, lands on a black square,
     * the destination square is empty, and the movement adheres to the rules of checkers for both kings and normal pieces.
     *
     * @param xSrc Source x-coordinate on the board.
     * @param ySrc Source y-coordinate on the board.
     * @param xDst Destination x-coordinate on the board.
     * @param yDst Destination y-coordinate on the board.
     * @return true if the move is valid according to checkers rules, false otherwise.
     */
    public boolean isValidMove(int xSrc, int ySrc, int xDst, int yDst)
    {
        // Check for out-of-bounds moves.
        if (xDst < 0 || xDst >= BOARD_SIZE || yDst < 0 || yDst >= BOARD_SIZE)
        {
            return false;
        }

        // Ensure the move is to a black square and the destination is empty.
        if ((xDst + yDst) % 2 == 0 || state[xDst][yDst] != null)
        {
            return false;
        }

        Piece piece = state[xSrc][ySrc];
        // Validate that there is a piece at the source and it belongs to the current player.
        if (piece == null || piece.isBlack() != turn)
        {
            return false;
        }

        // Special handling to ensure only the piece involved in a multi-capture continues moving.
        if (state[lastMoveX][lastMoveY].isBlack() == turn && (lastMoveX != xSrc || lastMoveY != ySrc))
        {
            return false;
        }

        // Additional rules for kings and non-kings.
        if (piece.isKing()) {
            // Check diagonal movement for kings.
            if (Math.abs(xDst - xSrc) != Math.abs(yDst - ySrc))
            {
                return false;
            }

            // For king moves, check if the path is clear and if capturing is mandatory.
            if (isPathClear(xSrc, ySrc, xDst, yDst))
            {
                return !playerHasMandatoryCapture();
            }
            else
            {
                // Verify capture validity for king moves.
                return hasOpponentPieceInBetween(xSrc, ySrc, xDst, yDst);
            }
        }
        else
        {
            // Rules for normal pieces.
            int dx = Math.abs(xDst - xSrc);
            int dy = Math.abs(yDst - ySrc);

            // Ensure normal moves are in the correct direction and capturing is not mandatory.
            if (dx == 1 && dy == 1)
            {
                if (playerHasMandatoryCapture())
                {
                    return false;
                }
                return (piece.isBlack() && xDst - xSrc == -1) || (!piece.isBlack() && xDst - xSrc == 1);
            }
            else if (dx == 2 && dy == 2)
            {
                // Ensure captures are legally executable.
                return hasOpponentPieceInBetween(xSrc, ySrc, xDst, yDst);
            }
        }

        return false;
    }

    /**
     * Performs the capture of an opponent's piece located between the source and destination squares during a move.
     * This method assumes that the capture is valid and updates the board to remove the captured piece.
     *
     * @param xSrc Source x-coordinate from where the piece is capturing.
     * @param ySrc Source y-coordinate from where the piece is capturing.
     * @param xDst Destination x-coordinate to where the piece is moving.
     * @param yDst Destination y-coordinate to where the piece is moving.
     */
    private void performPieceCapture(int xSrc, int ySrc, int xDst, int yDst)
    {
        int capturedX = (xSrc + xDst) / 2; // Calculate the x-coordinate of the captured piece.
        int capturedY = (ySrc + yDst) / 2; // Calculate the y-coordinate of the captured piece.
        state[capturedX][capturedY] = null; // Remove the captured piece from the board.
    }

    /**
     * Performs a capture by a king, removing the first opponent's piece encountered along the diagonal move.
     * This method assumes the path has been verified to be clear up to the first encountered piece.
     *
     * @param xSrc Source x-coordinate from where the king is capturing.
     * @param ySrc Source y-coordinate from where the king is capturing.
     * @param xDst Destination x-coordinate to where the king is moving.
     * @param yDst Destination y-coordinate to where the king is moving.
     */
    private void performKingCapture(int xSrc, int ySrc, int xDst, int yDst)
    {
        int stepX = Integer.signum(xDst - xSrc); // Calculate horizontal step direction.
        int stepY = Integer.signum(yDst - ySrc); // Calculate vertical step direction.
        int curX = xSrc + stepX;
        int curY = ySrc + stepY;
        boolean capturePerformed = false;

        // Traverse the path until a piece is captured or the destination is reached.
        while (curX != xDst && curY != yDst && !capturePerformed) {
            if (state[curX][curY] != null && state[curX][curY].isBlack() != turn)
            {
                state[curX][curY] = null; // Capture the opponent's piece.
                capturePerformed = true; // Mark that a capture has been made.
            }
            // Continue moving along the diagonal.
            curX += stepX;
            curY += stepY;
        }
    }

    /**
     * Checks if the path between the source and destination squares is clear of any pieces, which is necessary for a valid king move.
     *
     * @param xSrc Source x-coordinate.
     * @param ySrc Source y-coordinate.
     * @param xDst Destination x-coordinate.
     * @param yDst Destination y-coordinate.
     * @return true if the path is clear of pieces, false otherwise.
     */
    private boolean isPathClear(int xSrc, int ySrc, int xDst, int yDst)
    {
        int stepX = Integer.signum(xDst - xSrc); // Determine step direction horizontally.
        int stepY = Integer.signum(yDst - ySrc); // Determine step direction vertically.
        int curX = xSrc + stepX, curY = ySrc + stepY;

        // Check each square along the diagonal until reaching the destination.
        while (curX != xDst && curY != yDst)
        {
            if (state[curX][curY] != null)
            {
                return false; // Path is not clear if any square contains a piece.
            }
            curX += stepX;
            curY += stepY;
        }
        return true;
    }

    /**
     * Determines if there is exactly one opponent's piece in the path between the source and destination squares.
     *
     * @param xSrc Source x-coordinate.
     * @param ySrc Source y-coordinate.
     * @param xDst Destination x-coordinate.
     * @param yDst Destination y-coordinate.
     * @return true if there is exactly one opponent's piece in between and nothing else, false otherwise.
     */
    private boolean hasOpponentPieceInBetween(int xSrc, int ySrc, int xDst, int yDst)
    {
        int stepX = Integer.signum(xDst - xSrc); // Calculate horizontal movement step.
        int stepY = Integer.signum(yDst - ySrc); // Calculate vertical movement step.
        int opponentCount = 0; // Counter for opponent pieces encountered.

        int curX = xSrc + stepX;
        int curY = ySrc + stepY;

        // Traverse the path and count opponent pieces.
        while (curX != xDst && curY != yDst)
        {
            if (state[curX][curY] != null && state[curX][curY].isBlack() != turn)
            {
                if (opponentCount == 0)
                { // First opponent piece encountered.
                    opponentCount++;
                }
                else
                {
                    return false; // Invalid move if more than one opponent piece is encountered.
                }
            }
            curX += stepX;
            curY += stepY;
        }

        // Valid capture if exactly one opponent piece is encountered.
        return opponentCount == 1;
    }

    /**
     * Checks for mandatory captures available from the given board position for non-king pieces.
     * A mandatory capture exists if an adjacent opponent piece can be jumped over onto an empty square.
     *
     * @param x The x-coordinate of the piece on the board.
     * @param y The y-coordinate of the piece on the board.
     * @return true if a mandatory capture is available, false otherwise.
     */
    private boolean pieceHasMandatoryCapture(int x, int y)
    {
        int[][] directionVectors = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}; // Possible directions for a capture.

        for (int[] dir : directionVectors)
        {
            int jumpX = x + 2 * dir[0]; // Target square after the jump.
            int jumpY = y + 2 * dir[1];

            // Check for valid capture conditions.
            if (jumpX >= 0 && jumpX < BOARD_SIZE && jumpY >= 0 && jumpY < BOARD_SIZE &&
                    state[x + dir[0]][y + dir[1]] != null &&
                    state[x + dir[0]][y + dir[1]].isBlack() != turn &&
                    state[jumpX][jumpY] == null)
            {
                return true; // Capture is mandatory.
            }
        }
        return false; // No mandatory capture found.
    }

    /**
     * Checks if there is a mandatory capture available from the given position for a king.
     * Kings can capture in any diagonal direction over multiple squares.
     *
     * @param x The x-coordinate of the king on the board.
     * @param y The y-coordinate of the king on the board.
     * @return true if a capture is mandatory, false otherwise.
     */
    private boolean kingHasMandatoryCapture(int x, int y)
    {
        // Directions vectors for diagonal movement
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions)
        {
            int curX = x + dir[0];
            int curY = y + dir[1];

            boolean foundOpponent = false;

            while (curX >= 0 && curX < BOARD_SIZE && curY >= 0 && curY < BOARD_SIZE)
            {
                if (state[curX][curY] != null)
                {
                    if (foundOpponent)
                    {
                        break; // Found two pieces in a row - the capture would be illegal
                    }

                    if (state[curX][curY].isBlack() != turn)
                    {
                        foundOpponent = true; // Found an opponent's piece
                    }
                    else
                    {
                        break; // Blocked by a same-color piece
                    }
                }
                else if (foundOpponent)
                {
                    // Empty space after an opponent's piece - valid capture
                    return true;
                }
                curX += dir[0];
                curY += dir[1];
            }
        }
        return false;
    }

    /**
     * Checks if there is a mandatory capture available for any of the current player's pieces.
     * This method iterates over all pieces of the current player to determine if any captures are obligatory.
     *
     * @return true if at least one mandatory capture is available, false otherwise.
     */
    private boolean playerHasMandatoryCapture()
    {
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                Piece piece = state[i][j];
                if (piece != null && piece.isBlack() == turn)
                {
                    // Check both king and non-king pieces for mandatory captures.
                    if ((piece.isKing() && kingHasMandatoryCapture(i, j)) ||
                            (!piece.isKing() && pieceHasMandatoryCapture(i, j)))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sets the turn for the current game.
     * This method is used in online play to synchronize the turn state across devices.
     *
     * @param turn The current turn state to set.
     */
    public void setTurn(boolean turn)
    {
        this.turn = turn;
    }

    /**
     * Gets the current turn state of the game.
     * Useful in online play for synchronizing the game state across different clients.
     *
     * @return the current turn state.
     */
    public boolean getTurn()
    {
        return turn;
    }

    /**
     * Gets the x-coordinate of the last move made on the board.
     * This is used in online play to synchronize game state across devices.
     *
     * @return the x-coordinate of the last move.
     */
    public int getLastMoveX()
    {
        return lastMoveX;
    }

    /**
     * Sets the x-coordinate of the last move.
     * This is important in online play for maintaining consistent game state across sessions.
     *
     * @param lastMoveX The x-coordinate of the last move to be set.
     */
    public void setLastMoveX(int lastMoveX)
    {
        this.lastMoveX = lastMoveX;
    }

    /**
     * Gets the y-coordinate of the last move made on the board.
     * This method is crucial in online play for ensuring all players see the same game state.
     *
     * @return the y-coordinate of the last move.
     */
    public int getLastMoveY()
    {
        return lastMoveY;
    }

    /**
     * Sets the y-coordinate of the last move.
     * Used in online play to update and synchronize the position of the last move across devices.
     *
     * @param lastMoveY The y-coordinate of the last move to be set.
     */
    public void setLastMoveY(int lastMoveY)
    {
        this.lastMoveY = lastMoveY;
    }

    /**
     * Gets the number of moves since the last capture or kinging event.
     * This is used to determine draw conditions based on inactivity or non-progressive play.
     *
     * @return the number of moves since the last capture or kinging.
     */
    public int getMovesSinceCaptureOrKing()
    {
        return movesSinceCaptureOrKing;
    }

    /**
     * Sets the number of moves since the last capture or kinging event.
     * Important for tracking game progress and determining draws in prolonged games without captures or promotions.
     *
     * @param movesSinceCaptureOrKing The number of moves to set since the last significant game event.
     */
    public void setMovesSinceCaptureOrKing(int movesSinceCaptureOrKing)
    {
        this.movesSinceCaptureOrKing = movesSinceCaptureOrKing;
    }
}
