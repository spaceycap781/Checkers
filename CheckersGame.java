import java.util.ArrayList;
import java.util.List;

public class CheckersGame {

    private int[][] board;
    private int turn;
    private int selectedRow;
    private int selectedCol;
    private boolean jumpInProgress;

    /*
     * Pieces:
     * 0 = empty
     * 1 = red
     * 2 = black
     * 3 = red king
     * 4 = black king
     */

    public static class Move {
        public final int fromRow;
        public final int fromCol;
        public final int toRow;
        public final int toCol;
        public final boolean isJump;
        public final int jumpedRow;
        public final int jumpedCol;

        public Move(int fromRow, int fromCol, int toRow, int toCol) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.isJump = false;
            this.jumpedRow = -1;
            this.jumpedCol = -1;
        }

        public Move(int fromRow, int fromCol, int toRow, int toCol, int jumpedRow, int jumpedCol) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.isJump = true;
            this.jumpedRow = jumpedRow;
            this.jumpedCol = jumpedCol;
        }
    }

    public CheckersGame() {
        resetGame();
    }

    public void resetGame() {
        board = new int[8][8];
        turn = 2;
        selectedRow = -1;
        selectedCol = -1;
        jumpInProgress = false;
        setupBoard();
    }

    private void setupBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 != 0) {
                    if (row < 3) {
                        board[row][col] = 1;
                    } else if (row > 4) {
                        board[row][col] = 2;
                    }
                }
            }
        }
    }

    public int getPieceAt(int row, int col) {
        if (!isOnBoard(row, col)) {
            return 0;
        }

        return board[row][col];
    }

    public int getTurn() {
        return turn;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public boolean isJumpInProgress() {
        return jumpInProgress;
    }

    private boolean isOnBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private boolean isRedPiece(int piece) {
        return piece == 1 || piece == 3;
    }

    private boolean isBlackPiece(int piece) {
        return piece == 2 || piece == 4;
    }

    private boolean isKing(int piece) {
        return piece == 3 || piece == 4;
    }

    private int getPlayerForPiece(int piece) {
        if (isRedPiece(piece)) {
            return 1;
        } else if (isBlackPiece(piece)) {
            return 2;
        }

        return 0;
    }

    private boolean isOpponent(int player, int piece) {
        if (piece == 0) {
            return false;
        }

        if (player == 1) {
            return isBlackPiece(piece);
        } else {
            return isRedPiece(piece);
        }
    }

    private int[][] getDirections(int piece) {
        int player = getPlayerForPiece(piece);

        if (isKing(piece)) {
            return new int[][] {
                {-1, -1},
                {-1, 1},
                {1, -1},
                {1, 1}
            };
        }

        if (player == 1) {
            return new int[][] {
                {1, -1},
                {1, 1}
            };
        }

        return new int[][] {
            {-1, -1},
            {-1, 1}
        };
    }

    private List<Move> getJumpsForPiece(int row, int col) {
        List<Move> jumps = new ArrayList<>();

        int piece = getPieceAt(row, col);
        if (piece == 0) {
            return jumps;
        }

        int player = getPlayerForPiece(piece);
        int[][] directions = getDirections(piece);

        for (int i = 0; i < directions.length; i++) {
            int rowChange = directions[i][0];
            int colChange = directions[i][1];

            int middleRow = row + rowChange;
            int middleCol = col + colChange;

            int landingRow = row + 2 * rowChange;
            int landingCol = col + 2 * colChange;

            if (isOnBoard(landingRow, landingCol)) {
                int middlePiece = getPieceAt(middleRow, middleCol);

                if (board[landingRow][landingCol] == 0 && isOpponent(player, middlePiece)) {
                    jumps.add(new Move(row, col, landingRow, landingCol, middleRow, middleCol));
                }
            }
        }

        return jumps;
    }

    private List<Move> getNormalMovesForPiece(int row, int col) {
        List<Move> moves = new ArrayList<>();

        int piece = getPieceAt(row, col);
        if (piece == 0) {
            return moves;
        }

        int[][] directions = getDirections(piece);

        for (int i = 0; i < directions.length; i++) {
            int newRow = row + directions[i][0];
            int newCol = col + directions[i][1];

            if (isOnBoard(newRow, newCol) && board[newRow][newCol] == 0) {
                moves.add(new Move(row, col, newRow, newCol));
            }
        }

        return moves;
    }

    public List<Move> getValidMovesForPlayer(int player) {
        List<Move> moves = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int piece = board[row][col];

                if (getPlayerForPiece(piece) == player) {
                    moves.addAll(getJumpsForPiece(row, col));
                    moves.addAll(getNormalMovesForPiece(row, col));
                }
            }
        }

        return moves;
    }

    public List<Move> getValidMovesForPiece(int row, int col) {
        List<Move> validMoves = new ArrayList<>();

        int piece = getPieceAt(row, col);
        if (piece == 0) {
            return validMoves;
        }

        int piecePlayer = getPlayerForPiece(piece);
        if (piecePlayer != turn) {
            return validMoves;
        }

        // During a double jump, the same piece has to keep jumping
        if (jumpInProgress) {
            if (row == selectedRow && col == selectedCol) {
                return getJumpsForPiece(row, col);
            }

            return validMoves;
        }

        List<Move> allMoves = getValidMovesForPlayer(turn);

        for (Move move : allMoves) {
            if (move.fromRow == row && move.fromCol == col) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    public boolean selectPiece(int row, int col) {
        if (!isOnBoard(row, col)) {
            return false;
        }

        int piece = board[row][col];

        if (piece == 0 || getPlayerForPiece(piece) != turn) {
            return false;
        }

        if (jumpInProgress) {
            return row == selectedRow && col == selectedCol;
        }

        List<Move> moves = getValidMovesForPiece(row, col);

        if (!moves.isEmpty()) {
            selectedRow = row;
            selectedCol = col;
            return true;
        }

        return false;
    }

    public void clearSelection() {
        if (!jumpInProgress) {
            selectedRow = -1;
            selectedCol = -1;
        }
    }

    public boolean makeMove(int toRow, int toCol) {
        if (selectedRow == -1 || selectedCol == -1) {
            return false;
        }

        Move moveToMake = null;
        List<Move> moves = getValidMovesForPiece(selectedRow, selectedCol);

        for (Move move : moves) {
            if (move.toRow == toRow && move.toCol == toCol) {
                moveToMake = move;
                break;
            }
        }

        if (moveToMake == null) {
            return false;
        }

        int movingPiece = board[selectedRow][selectedCol];

        board[toRow][toCol] = movingPiece;
        board[selectedRow][selectedCol] = 0;

        if (moveToMake.isJump) {
            board[moveToMake.jumpedRow][moveToMake.jumpedCol] = 0;
        }

        boolean becameKing = promoteIfNeeded(toRow, toCol, movingPiece);

        if (moveToMake.isJump && !becameKing) {
            List<Move> nextJumps = getJumpsForPiece(toRow, toCol);

            if (!nextJumps.isEmpty()) {
                selectedRow = toRow;
                selectedCol = toCol;
                jumpInProgress = true;
                return true;
            }
        }

        selectedRow = -1;
        selectedCol = -1;
        jumpInProgress = false;

        if (turn == 1) {
            turn = 2;
        } else {
            turn = 1;
        }

        return true;
    }

    private boolean promoteIfNeeded(int row, int col, int movingPiece) {
        if (movingPiece == 1 && row == 7) {
            board[row][col] = 3;
            return true;
        }

        if (movingPiece == 2 && row == 0) {
            board[row][col] = 4;
            return true;
        }

        return false;
    }

    public int getRedCount() {
        int count = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (isRedPiece(board[row][col])) {
                    count++;
                }
            }
        }

        return count;
    }

    public int getBlackCount() {
        int count = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (isBlackPiece(board[row][col])) {
                    count++;
                }
            }
        }

        return count;
    }

    public int getWinner() {
        int redCount = getRedCount();
        int blackCount = getBlackCount();

        if (redCount == 0) {
            return 2;
        }

        if (blackCount == 0) {
            return 1;
        }

        if (getValidMovesForPlayer(turn).isEmpty()) {
            if (turn == 1) {
                return 2;
            } else {
                return 1;
            }
        }

        return 0;
    }
}