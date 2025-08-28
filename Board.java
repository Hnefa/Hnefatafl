import java.lang.reflect.Array;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.util.*;

import static java.lang.Math.abs;

public class Board {
    private int grid_size;
    private int center;
    private Position[] castles;
    private Position king;
    private String[][] board; // 0 = empty, 1 = attacking pawn, 2 =defending pawn, 3 = king, 4 = castle?

    public Board (int grid_size) throws InvalidParameterException {
        // If board is too small or even in dimensions
        if (grid_size < 9 || grid_size % 2 == 0) {
            throw new InvalidParameterException();
        }

        this.board = new String[grid_size][grid_size];
        int center = (grid_size / 2);

        // Populate attacking pieces
        for (int i = 0; i < grid_size + 1; i += grid_size - 1) {
            for (int j = center - 2; j < center + 3; j++) {
                this.board[i][j] = "♟";
                this.board[j][0] = "♟";
                this.board[j][grid_size - 1] = "♟";
            }
        }
        for (int k = 1; k < grid_size + 1; k += grid_size - 3) {
            this.board[k][center] = "♟";
            this.board[center][k] = "♟";
        }

        // Populate defending pieces
        this.board[center][center] = "♔"; // Place King
        for (int l = 1; l < 3; l++) {
            this.board[center][center + l] = "♙";
            this.board[center][center - l] = "♙";
            this.board[center + l][center] = "♙";
            this.board[center - l][center] = "♙";
            if (l == 1) {
                this.board[center + l][center + l] = "♙";
                this.board[center + l][center - l] = "♙";
                this.board[center - l][center - l] = "♙";
                this.board[center - l][center + l] = "♙";
            }
        }

        // populate edge castles
        /*this.board[grid_size - 1][grid_size - 1] = "4";
        this.board[grid_size - 1][0] = "4";
        this.board[0][grid_size - 1] = "4";
        this.board[0][0] = "4";*/

        // Populate Castle list
        this.castles = new Position[5];
        this.castles[0] = new Position(center, center);// center castle first
        this.castles[1] = new Position(0, 0);
        this.castles[2] = new Position(0, grid_size - 1);
        this.castles[3] = new Position(grid_size - 1, 0);
        this.castles[4] = new Position(grid_size - 1, grid_size - 1);

        // Set additional parameters
        this.grid_size = grid_size;
        this.center = center;
        this.king = new Position(center, center);
    }

    public Position getKing () {
        return this.king;
    }

    public Move getKingWinningMove(String player) {
        for (Position p : castles) {
            if (p.getX() != center && p.getY() != center) {
                if (isValidMove(this.king, p, player)) {
                    return new Move(this.king, p);
                }
            }
        }
        return null;
    }

    /**
     * This function returns true if the king has been captured. Returns false otherwise.
     * @return true / false
     */
    public boolean isKingCaptured () {
        int n = getKingCorneredNumber();
        if (n == 4) {
            return true;
        }

        return false;
    }

    /**
     * This function returns true if the white king has won the game.
     * @return true / false
     */
    public boolean hasKingEscaped () {
        for (Position p : castles) {
            if (p.getX() != center && p.getY() != center) {
                if (this.king.compare(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    public double getDistanceBetweenPoints(Position p1, Position p2) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * This function returns true if a given hunn can be captured. Note that the hunn does not need to occupy
     * the given position. If the hunn can be captured by the opposing player, return true. False otherwise.
     * @param p The (theoretical) position of a hunn.
     * @param player The (theoretical) player at the given location.
     * @return True / false
     */
    public boolean canHunnBeCaptured (Position p, String player) {
        int px = p.getX();
        int py = p.getY();


        // Check x
        if (px > 0) {
            if (getLocation(px - 1, py).equals(getOpposingPlayer(player))) {
                Position p2 = new Position(px + 1, py);
                if (px < board.length - 1) {
                    if (getPosition(p2).equals("")) {
                        ArrayList e = possibleMovesToPosition(getOpposingPlayer(player), p2, false);
                        if (e.size() > 0) {
                            return true;
                        }
                    }
                }

            }
        }
        if (px < board.length - 1) {
            if (getLocation(px + 1, py).equals(getOpposingPlayer(player))) {
                Position p2 = new Position(px - 1, py);
                if (px > 0) {
                    if (getPosition(p2).equals("")) {
                        ArrayList e = possibleMovesToPosition(getOpposingPlayer(player), p2, false);
                        if (e.size() > 0) {
                            return true;
                        }
                    }
                }

            }
        }

        // Check Y
        if (py > 0) {
            if (getLocation(px, py - 1).equals(getOpposingPlayer(player))) {
                Position p2 = new Position(px, py + 1);
                if (py < board.length - 1) {
                    if (getPosition(p2).equals("")) {
                        ArrayList e = possibleMovesToPosition(getOpposingPlayer(player), p2, false);
                        if (e.size() > 0) {
                            return true;
                        }
                    }
                }

            }
        }
        if (py < board.length - 1) {
            if (getLocation(px, py + 1).equals(getOpposingPlayer(player))) {
                Position p2 = new Position(px, py - 1);
                if (py > 0) {
                    if (getPosition(p2).equals("")) {
                        ArrayList e = possibleMovesToPosition(getOpposingPlayer(player), p2, false);
                        if (e.size() > 0) {
                            return true;
                        }
                    }
                }

            }
        }

        return false;
    }

    /**
     * This function gets all king moves
     */
    public ArrayList<Move> getAllKingMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        int kx = this.king.getX();
        int ky = this.king.getY();
        int i = 1;
        // x+
        while(true) {
            Position p = new Position(kx + i, ky);
            if (isValidMove(this.king, p, "♙")) {
                Move m = new Move(this.king, p);
                moves.add(m);
            } else {
                break;
            }
            i++;
        }

        i = 1;

        // x-
        while(true) {
            Position p = new Position(kx - i, ky);
            if (isValidMove(this.king, p, "♙")) {
                Move m = new Move(this.king, p);
                moves.add(m);
            } else {
                break;
            }
            i++;
        }

        i = 1;

        // y+
        while(true) {
            Position p = new Position(kx, ky + i);
            if (isValidMove(this.king, p, "♙")) {
                Move m = new Move(this.king, p);
                moves.add(m);
            } else {
                break;
            }
            i++;
        }
        i = 1;

        // y-
        while(true) {
            Position p = new Position(kx, ky - i);
            if (isValidMove(this.king, p, "♙")) {
                Move m = new Move(this.king, p);
                moves.add(m);
            } else {
                break;
            }
            i++;
        }

        return moves;
    }

    /**
     * This function gets the move that is closest to a position
     * @param goal The position you want to move to.
     * @param excluded An position you dont want to move.
     * @param player The player
     * @return The move that is closest.
     */
    public Move getClosestMove(Position goal, Position excluded, String player) {
        double closest = Double.MAX_VALUE;
        Position from = null;
        Position to = null;

        ArrayList<Position> lst = getAllBoardPositions(player);
        for (Position p : lst) {
            if (!p.compare(excluded)) {
                // check x+
                int val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX() + val, p.getY());
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, goal);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }
                }

                // check x-
                val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX() - val, p.getY());
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, goal);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }

                }

                // check y+
                val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX(), p.getY() + val);
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, goal);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }
                }

                // check y-
                val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX(), p.getY() - val);
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, goal);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }
                }
            }
        }

        Move m = new Move(from, to);
        return m;
    }

    /**
     * This function gets the move that is closest to a position
     * @param goal The position you want to move to.
     * @param excluded A position list you dont want to move.
     * @param player The player
     * @return The move that is closest.
     */
    public Move getClosestMoveListExcluded(Position goal, ArrayList<Position> excluded, String player) {
        double closest = Double.MAX_VALUE;
        Position from = null;
        Position to = null;

        ArrayList<Position> lst = getAllBoardPositions(player);
        for (Position p : lst) {
            boolean isExcluded = false;
            for (Position e : excluded) {
                if (p.compare(e)) {
                    isExcluded = true;
                }
            }
            if (!isExcluded) {
                // check x+
                int val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX() + val, p.getY());
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, goal);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }
                }

                // check x-
                val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX() - val, p.getY());
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, goal);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }

                }

                // check y+
                val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX(), p.getY() + val);
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, goal);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }
                }

                // check y-
                val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX(), p.getY() - val);
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, goal);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }
                }
            }
        }


        Move m = new Move(from, to);
        return m;
    }


    



    /**
     * Checks all possible moves. returns the move that is closest to the king.
     * @param player the player
     * @return the move closest to the king.
     */
    public Move getMoveClosestToKing (String player) {
        double closest = Double.MAX_VALUE;
        Position from = null;
        Position to = null;

        ArrayList<Position> lst = getAllBoardPositions(player);
        if (player.equals("♙") && lst.size() < 2) {
            return null;
        }

        for (Position p : lst) {
            if (!p.compare(this.king)) {
                // check x+
                int val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX() + val, p.getY());
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, this.king);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }
                }

                // check x-
                val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX() - val, p.getY());
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, this.king);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }

                }

                // check y+
                val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX(), p.getY() + val);
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, this.king);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }
                }

                // check y-
                val = 1;
                while (true) {
                    Position nextPos = new Position(p.getX(), p.getY() - val);
                    if (isValidMove(p, nextPos, player)) {
                        double dist = getDistanceBetweenPoints(nextPos, this.king);
                        if (dist < closest) {
                            closest = dist;
                            from = p;
                            to = nextPos;
                        }
                        val++;
                    } else {
                        break;
                    }
                }
            }
        }

        Move m = new Move(from, to);
        return m;
    }

    /**
     * This function gets a ove that will move a small pawn close to the king. If no moves are available, null is returned.
     * @param player The player.
     * @return A move.
     */
    public Move getKingsGuardMove (String player) {
        int kx = this.king.getX();
        int ky = this.king.getY();

        for (int x = kx - 2; x < kx + 3; x++) {
            for (int y = ky - 2; y < ky + 3; y++) {
                if ((x >= 0 && x <= board.length - 1) && (y >= 0 && y <= board.length - 1)) {
                    if (getLocation(x, y).equals("")) {
                        ArrayList<Position> lst = getAllBoardPositions(player);
                        for (Position p : lst) {
                            int px = p.getX();
                            int py = p.getY();

                            // if position is NOT near king
                            if (!((px >= kx - 2 && px < kx + 3) && (py >= ky - 2 && py < ky + 3))) {
                                Position to = new Position(x, y);
                                if (isValidMove(p, to, player)) {
                                    Move m = new Move(p, to);
                                    return m;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * This function gets the number of pieces in a "square" around the king (king +2 and -2)
     * @return The number of friendly pieces near the king.
     */
    public int getKingsGuardNumber () {
        int kx = this.king.getX();
        int ky = this.king.getY();

        int guards = 0;

        for (int x = kx - 2; x < kx + 3; x++) {
            for (int y = ky - 2; y < ky + 3; y++) {
                if ((0 <= x && x <= board.length - 1) && (0 <= y && y <= board.length - 1)) {
                    if (getLocation(x, y).equals("♙")) {
                        guards++;
                    }
                }
            }
        }

        return guards;
    }

    /**
     * This function gets the closest move to a iven position.
     * @param from Start
     * @param to where you want to go.
     * @param player the player
     * @return The closest available location.
     */
    public Move getClosestMoveToPosition (Position from, Position to, String player) {
        int x = to.getX();
        int y = to.getY();
        int shortest = Integer.MAX_VALUE;
        Move best = null;

        ArrayList<Move> moves = getAllMovesFromPosition(from, player);
        if (moves.size() > 0) {
            for (Move m : moves) {
                int mx = m.getTo().getX();
                int my = m.getTo().getY();
                int dist = abs(x - mx) + abs(y - my);
                if (dist < shortest) {
                    dist = shortest;
                    best = m;
                }
            }
            return best;
        } else {
            return null;
        }
    }

    /**
     * This function returns all possible moves from a given position.
     * @param p The given position
     * @param player The player
     * @return List of possible moves.
     */
    public ArrayList<Move> getAllMovesFromPosition(Position p, String player) {
        ArrayList<Move> lst = new ArrayList<>();

        // check x+
        int val = 1;
        while (true) {
            Position nextPos = new Position(p.getX() + val, p.getY());
            if (isValidMove(p, nextPos, player)) {
                lst.add(new Move(p, nextPos));
                val++;
            } else {
                break;
            }
        }

        // check x-
        val = 1;
        while (true) {
            Position nextPos = new Position(p.getX() - val, p.getY());
            if (isValidMove(p, nextPos, player)) {
                lst.add(new Move(p, nextPos));
                val++;
            } else {
                break;
            }

        }

        // check y+
        val = 1;
        while (true) {
            Position nextPos = new Position(p.getX(), p.getY() + val);
            if (isValidMove(p, nextPos, player)) {
                lst.add(new Move(p, nextPos));
                val++;
            } else {
                break;
            }
        }

        // check y-
        val = 1;
        while (true) {
            Position nextPos = new Position(p.getX(), p.getY() - val);
            if (isValidMove(p, nextPos, player)) {
                lst.add(new Move(p, nextPos));
                val++;
            } else {
                break;
            }
        }

        return lst;
    }

    /**
     * This function gets the longest possible move any direction.
     * @param p The position of the piece you want to move.
     * @param player the player
     * @return The longest possible move for the player.
     */
    public Move getLongestMoveAnyDirection (Position p, String player) {
        int furthest = 0;
        Position to = null;

        // check x+
        int val = 1;
        while (true) {
            Position nextPos = new Position(p.getX() + val, p.getY());
            if (isValidMove(p, nextPos, player)) {
                if (val > furthest) {
                    furthest = val;
                    to = nextPos;

                }
                val++;
            } else {
                break;
            }
        }

        // check x-
        val = 1;
        while (true) {
            Position nextPos = new Position(p.getX() - val, p.getY());
            if (isValidMove(p, nextPos, player)) {
                if (val > furthest) {
                    furthest = val;
                    to = nextPos;

                }
                val++;
            } else {
                break;
            }

        }

        // check y+
        val = 1;
        while (true) {
            Position nextPos = new Position(p.getX(), p.getY() + val);
            if (isValidMove(p, nextPos, player)) {
                if (val > furthest) {
                    furthest = val;
                    to = nextPos;

                }
                val++;
            } else {
                break;
            }
        }

        // check y-
        val = 1;
        while (true) {
            Position nextPos = new Position(p.getX(), p.getY() - val);
            if (isValidMove(p, nextPos, player)) {
                if (val > furthest) {
                    furthest = val;
                    to = nextPos;

                }
                val++;
            } else {
                break;
            }
        }

        Move m = new Move(p, to);
        return m;
    }

    /**
     * This function gets all possible captures a player can get. Do note that the captures are NOT taken.
     * @param player The active player
     * @return Priority Queue based on the number of captures per move.
     */
    public PriorityQueue<Move> getPossibleCaptures(String player) {
        PriorityQueue<Move> captures = new PriorityQueue<>(Comparator.comparingInt(m -> m.getCaptures()));

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                // If player owns position, get that pawns possible captures and add to captures.
                if (getLocation(i, j).equals(player) || (player.equals("♙") && getLocation(i, j).equals("♔"))) {
                    Position p = new Position(i, j);
                    ArrayList<Move> cap = getPossibleCapturesFromPosition(player, p);
                    for (Move m : cap) {
                        captures.add(m);
                    }
                }
            }
        }

        return captures;
    }

    private boolean isOpposingPlayer (String p1, String p2) {
        if (p2.equals("") || p1.equals(p2)) {
            return false;
        }

        if (p1.equals("♙") && p2.equals("♔")) {
            return false;
        }

        return true;
    }

    private Boolean isPositionEmpty (Position p) {
        if (getPosition(p).equals("")) {
            return true;
        }
        return false;
    }

    /**
     * This function returns an array of moves that will capture pieces.
     * @param player The active player
     * @param p The position owned by the player.
     * @return All moves that will capture pieces from given starting position.
     */
    public ArrayList<Move> getPossibleCapturesFromPosition (String player, Position p) {
        int x = p.getX();
        int y = p.getY();

        ArrayList<Move> captures = new ArrayList<>();

        // Check X-
        for (int i = 1; x - i >= 0; i++) {
            Position pos = new Position(x - i, y);
            Move m = new Move(p, pos);

            if (isValidMove(m.getFrom(), m.getTo(), player)) {
                if (isPositionEmpty(pos)) {
                    break;
                }

                if (y != board.length - 1) {
                    if (captureUp(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }
                if (y != 0) {
                    if (captureDown(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }

                if (x - i > 1) {
                    if (captureLeft(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }

                if (m.getCaptures() > 0) {
                    captures.add(m);
                }
            }
        }

        // Check y-
        for (int i = 1; y - i >= 0; i++) {
            Position pos = new Position(x, y - i);
            Move m = new Move(p, pos);

            if (isValidMove(m.getFrom(), m.getTo(), player)) {
                if (!isPositionEmpty(pos)) {
                    break;
                }

                if (x != 0) {
                    if (captureLeft(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }
                if (x != board.length - 1) {
                    if (captureRight(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }
                if (y - i > 1) {
                    if (captureDown(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }

                if (m.getCaptures() > 0) {
                    captures.add(m);
                }
            }
        }

        // Check X+
        for (int i = 1; x + i < board.length; i++) {
            Position pos = new Position(x + i, y);
            Move m = new Move(p, pos);

            if (isValidMove(m.getFrom(), m.getTo(), player)) {
                if (!isPositionEmpty(pos)) {
                    break;
                }

                if (y != board.length - 1) {
                    if (captureUp(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }
                if (y != 0) {
                    if (captureDown(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }
                if (x + i < board.length - 2) {
                    if (captureRight(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }

                if (m.getCaptures() > 0) {
                    captures.add(m);
                }
            }
        }

        // Check Y+
        for (int i = 1; y + i < board.length; i++) {
            Position pos = new Position(x, y + i);
            Move m = new Move(p, pos);

            if (isValidMove(m.getFrom(), m.getTo(), player)) {
                if (!isPositionEmpty(pos)) {
                    break;
                }

                if (x != 0) {
                    if (captureLeft(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }
                if (x != board.length - 1) {
                    if (captureRight(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }
                if (y + i < board.length - 2) {
                    if (captureUp(player, pos, false)) {
                        m.addCaptures(1);
                    }
                }

                if (m.getCaptures() > 0) {
                    captures.add(m);
                }
            }


        }

        return captures;
    }

    /**
     * This function gets and returns all edges occupied by enemies. Note that edges mean the two edge pieces
     * closest to the corners. If enemy occupy no edges, null is returned.
     * @param player The player
     * @return Enemy Occupied edge Positions.
     */
    public ArrayList<Position> getEnemyOccupiedEdges(String player) {
        ArrayList<Position> occupied = new ArrayList<>();
        Position edges[] = new Position[16];
        edges[0] = new Position(0, 1);
        edges[1] = new Position(0, 2);
        edges[2] = new Position(0, board.length - 2);
        edges[3] = new Position(0, board.length - 3);
        edges[4] = new Position(1, 0);
        edges[5] = new Position(2, 0);
        edges[6] = new Position(board.length - 2, 0);
        edges[7] = new Position(board.length - 3, 0);
        edges[8] = new Position(board.length - 1, 1);
        edges[9] = new Position(board.length - 1, 2);
        edges[10] = new Position(board.length - 1, board.length - 2);
        edges[11] = new Position(board.length - 1, board.length - 3);
        edges[12] = new Position(1, board.length - 1);
        edges[13] = new Position(2, board.length - 1);
        edges[14] = new Position(board.length - 2, board.length - 1);
        edges[15] = new Position(board.length - 3, board.length - 1);

        for (Position p : edges) {
            if (getPosition(p).equals(getOpposingPlayer(player))) {
                occupied.add(p);
            }
        }

        if (occupied.isEmpty()) {
            return null;
        }

        return occupied;
    }

    /**
     * This function checks if an enemy occupies an edge. Note that this checks the 2 positions closest to a corner.
     * @param player The player.
     * @return true / false
     */
    public boolean doesEnemyOccupyEdge(String player) {
        Position edges[] = new Position[16];
        edges[0] = new Position(0, 1);
        edges[1] = new Position(0, 2);
        edges[2] = new Position(0, board.length - 2);
        edges[3] = new Position(0, board.length - 3);
        edges[4] = new Position(1, 0);
        edges[5] = new Position(2, 0);
        edges[6] = new Position(board.length - 2, 0);
        edges[7] = new Position(board.length - 3, 0);
        edges[8] = new Position(board.length - 1, 1);
        edges[9] = new Position(board.length - 1, 2);
        edges[10] = new Position(board.length - 1, board.length - 2);
        edges[11] = new Position(board.length - 1, board.length - 3);
        edges[12] = new Position(1, board.length - 1);
        edges[13] = new Position(2, board.length - 1);
        edges[14] = new Position(board.length - 2, board.length - 1);
        edges[15] = new Position(board.length - 3, board.length - 1);

        for (Position p : edges) {
            if (getPosition(p).equals(getOpposingPlayer(player))) {
                return true;
            }
        }

        return false;
    }

    /**
     * This function gets all moves that will occupy a key edge middle location for the given player. If no
     * moves are possible, null will be returned.
     * @param player The pplayer
     * @return All possible Moves
     */
    public ArrayList<Move> getMiddleKeyLocationMoves(String player) {
        ArrayList<Position> team = getAllBoardPositions(player);
        ArrayList<Move> moves = new ArrayList<>();
        Position[] mid = new Position[4];
        mid[0] = new Position(1, 1);
        mid[1] = new Position(1, this.board.length - 1);
        mid[2] = new Position(this.board.length - 1, 1);
        mid[3] = new Position(this.board.length - 1, this.board.length - 1);

        for (Position p : team) {
            for (int i = 0; i < 4; i++) {
                if (isValidMove(p, mid[i], player)) {
                    moves.add(new Move(p, mid[i]));
                }
            }
        }

        if (moves.isEmpty()) {
            return null;
        }

        return moves;
    }

    /**
     * This function checks if the given player is at the top row.
     * @param player The player
     * @return true / false
     */
    public boolean isPlayerOccupyingEdgeN (String player) {
        for (int i = 2; i < grid_size - 2; i++) {
            String p = getLocation(0, i);
            if (p.equals(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This function gets the first free position at the northern edge.
     * @return the position
     */
    public Position getFirstFreeEgdeNPosition () {
        for (int i = 2; i < grid_size - 2; i++) {
            String p = getLocation(0, i);
            if (p.equals("")) {
                return new Position(0, i);
            }
        }
        return null;
    }

    /**
     * This function gets the first free position at the northern edge.
     * @return the positions
     */
    public ArrayList<Position> getOccupiedEdgeNPosition (String player) {
        ArrayList<Position> lst = getBarricadeList();
        for (int i = 0; i < grid_size - 1; i++) {
            String p = getLocation(0, i);
            if (p.equals(player)) {
                lst.add(new Position(0, i));
            }
        }
        return lst;
    }

    /**
     * This function checks if the given player is at the bottom row.
     * @param player The player
     * @return true / false
     */
    public boolean isPlayerOccupyingEdgeS (String player) {
        for (int i = 2; i < grid_size - 2; i++) {
            String p = getLocation(grid_size - 1, i);
            if (p.equals(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This function gets the first free position at the southern edge.
     * @return the position
     */
    public Position getFirstFreeEgdeSPosition () {
        for (int i = 2; i < grid_size - 2; i++) {
            String p = getLocation(grid_size - 1, i);
            if (p.equals("")) {
                return new Position(grid_size - 1, i);
            }
        }
        return null;
    }

    /**
     * This function gets the first free position at the western edge.
     * @return the position
     */
    public ArrayList<Position> getOccupiedEdgeSPosition (String player) {
        ArrayList<Position> lst = getBarricadeList();
        for (int i = 0; i < grid_size - 1; i++) {
            String p = getLocation(grid_size - 1, i);
            if (p.equals(player)) {
                lst.add(new Position(grid_size - 1, i));
            }
        }
        return lst;
    }

    /**
     * This function checks if the given player is at the east row.
     * @param player The player
     * @return true / false
     */
    public boolean isPlayerOccupyingEdgeE (String player) {
        for (int i = 2; i < grid_size - 2; i++) {
            String p = getLocation(i, 0);
            if (p.equals(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This function gets the first free position at teh eastern edge.
     * @return the position
     */
    public Position getFirstFreeEgdeEPosition () {
        for (int i = 2; i < grid_size - 2; i++) {
            String p = getLocation(i, 0);
            if (p.equals("")) {
                return new Position(i, 0);
            }
        }
        return null;
    }

    /**
     * This function gets the first free position at the eastern edge.
     * @return the position
     */
    public ArrayList<Position> getOccupiedEdgeEPosition (String player) {
        ArrayList<Position> lst = getBarricadeList();
        for (int i = 0; i < grid_size - 1; i++) {
            String p = getLocation(i, 0);
            if (p.equals(player)) {
                lst.add(new Position(i, 0));
            }
        }
        return lst;
    }

    /**
     * This function checks if the given player is at the west row.
     * @param player The player
     * @return true / false
     */
    public boolean isPlayerOccupyingEdgeW (String player) {
        for (int i = 2; i < grid_size - 2; i++) {
            String p = getLocation(i, grid_size - 1);
            if (p.equals(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This function gets the first free position at the western edge.
     * @return the position
     */
    public Position getFirstFreeEgdeWPosition () {
        for (int i = 2; i < grid_size - 2; i++) {
            String p = getLocation(i, grid_size - 1);
            if (p.equals("")) {
                return new Position(i, 0);
            }
        }
        return null;
    }

    /**
     * This function gets the first free position at the western edge.
     * @return the position
     */
    public ArrayList<Position> getOccupiedEdgeWPosition (String player) {
        ArrayList<Position> lst = getBarricadeList();
        for (int i = 0; i < grid_size - 1; i++) {
            String p = getLocation(i, grid_size - 1);
            if (p.equals(player)) {
                lst.add(new Position(i, grid_size - 1));
            }
        }
        return lst;
    }

    /**
     * This function gets the first free position at the desired column (W, E).
     * @param column the column
     * @return the position
     */
    public ArrayList<Position> getOccupiedColumn (String player, int column) {
        ArrayList<Position> lst = getBarricadeList();
        for (int i = 2; i < grid_size - 2; i++) {
            String p = getLocation(i, column);
            if (p.equals(player)) {
                lst.add(new Position(i, column));
            }
        }
        return lst;
    }

    /**
     * This function gets the first free position at the desired row (N, S).
     * @param row the column
     * @return the position
     */
    public ArrayList<Position> getOccupiedRow (String player, int row) {
        ArrayList<Position> lst = getBarricadeList();
        for (int i = 2; i < grid_size - 2; i++) {
            String p = getLocation(row, i);
            if (p.equals(player)) {
                lst.add(new Position(row, i));
            }
        }
        return lst;
    }

    /**
     * This function gets the first occupied position in a desired column (E / W).
     * @param column the row
     * @return the position
     */
    public Position getFirstOccupiedPositionInColumn (String player, int column) {
        for (int i = 0; i < grid_size; i++) {
            String p = getLocation(i, column);
            if (p.equals(player)) {
                return new Position(i, column);
            }
        }
        return null;
    }

    /**
     * This function gets the first occupied position in a desired row (N / S).
     * @param row the row
     * @return the position
     */
    public Position getFirstOccupiedPositionInRow (String player, int row) {
        for (int i = 0; i < grid_size ; i++) {
            String p = getLocation(row, i);
            if (p.equals(player)) {
                return new Position(row, i);
            }
        }
        return null;
    }

    /**
     * This function gets and return a list of all barricade positions.
     * @return The barricade position.
     */
    public ArrayList<Position> getBarricadeList() {
        ArrayList<Position> lst = new ArrayList();

        // Corner 1
        lst.add(new Position(2, 0));
        lst.add(new Position(0, 2));
        lst.add(new Position(1, 1));

        // Corner 2
        lst.add(new Position(grid_size - 3, 0));
        lst.add(new Position(grid_size - 1, 2));
        lst.add(new Position(grid_size - 2, 1));

        // Corner 3
        lst.add(new Position(0, grid_size - 3));
        lst.add(new Position(2, grid_size - 1));
        lst.add(new Position(1, grid_size - 2));

        // Corner 4
        lst.add(new Position(grid_size - 1 , grid_size - 3));
        lst.add(new Position(grid_size - 3, grid_size - 1));
        lst.add(new Position(grid_size - 2, grid_size - 2));

        return lst;
    }

    /**
     * This function checks if the given player can take a middle key location.
     * @param player The player
     * @return true / false
     */
    public boolean canTakeMiddleKeyLocation(String player) {
        ArrayList<Position> team = getAllBoardPositions(player);
        Position[] mid = new Position[4];
        mid[0] = new Position(1, 1);
        mid[1] = new Position(1, this.board.length - 2);
        mid[2] = new Position(this.board.length - 2, 1);
        mid[3] = new Position(this.board.length - 2, this.board.length - 2);

        for (Position p : team) {
            for (int i = 0; i < 4; i++) {
                if (isValidMove(p, mid[i], player)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This function gets all board positions occupied by a given player.
     * @param player The player
     * @return All occupied positions.
     */
    public ArrayList<Position> getAllBoardPositions(String player) {
        ArrayList<Position> pos = new ArrayList<>();

        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board.length; y++) {
                String s = getLocation(x, y);
                if (s.equals(player)  || s.equals("♔") && player.equals("♙")) {
                    pos.add(new Position(x, y));
                }
            }
        }

        return pos;
    }

    /**
     * This function gets all possible moves that will get an edge key location. If no moves gives an edge key location
     * null is returned.
     * @param player The player.
     * @return Possible moves / null
     */
    public ArrayList<Move> getObtainableEdgeKeyLocation(String player) {
        ArrayList<Position> team = getAllBoardPositions(player);
        ArrayList<Move> moves = new ArrayList<>();
        Position edges[] = new Position[8];
        edges[0] = new Position(0, 2);
        edges[1] = new Position(0, board.length - 3);
        edges[2] = new Position(2, 0);
        edges[3] = new Position(board.length - 3, 0);
        edges[4] = new Position(board.length - 1, 2);
        edges[5] = new Position(board.length - 1, board.length - 3);
        edges[6] = new Position(2, board.length - 1);
        edges[7] = new Position(board.length - 3, board.length - 1);

        for (Position p : team) {
            for (int i = 0; i < 8; i++) {
                if (isValidMove(p, edges[i], player)) {
                    moves.add(new Move(p, edges[i]));
                }
            }
        }

        if (moves.isEmpty()) {
            return null;
        }

        return moves;
    }

    /**
     * This function checks if a player can take an edge key location.
     * @param player The player
     * @return true / false
     */
    public boolean canTakeEdgeKeyLocation(String player) {
        ArrayList<Position> team = getAllBoardPositions(player);
        Position edges[] = new Position[8];
        edges[0] = new Position(0, 2);
        edges[1] = new Position(0, board.length - 3);
        edges[2] = new Position(2, 0);
        edges[3] = new Position(board.length - 3, 0);
        edges[4] = new Position(board.length - 1, 2);
        edges[5] = new Position(board.length - 1, board.length - 3);
        edges[6] = new Position(2, board.length - 1);
        edges[7] = new Position(board.length - 3, board.length - 1);

        for (Position p : team) {
            for (int i = 0; i < 8; i++) {
                if (isValidMove(p, edges[i], player)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This function gets the edge move corners for the king. If none, null is returned.
     * @return the edge king moves
     */
    public ArrayList<Move> getEdgeCornerMovesForKing() {
        Position edges[] = new Position[24];
        edges[0] = new Position(0, 1);
        edges[1] = new Position(0, 2);
        edges[2] = new Position(0, 3);
        edges[3] = new Position(0, board.length - 2);
        edges[4] = new Position(0, board.length - 3);
        edges[5] = new Position(0, board.length - 4);

        edges[6] = new Position(1, 0);
        edges[7] = new Position(2, 0);
        edges[8] = new Position(3, 0);
        edges[9] = new Position(board.length - 2, 0);
        edges[10] = new Position(board.length - 3, 0);
        edges[11] = new Position(board.length - 4, 0);

        edges[12] = new Position(board.length - 1, 1);
        edges[13] = new Position(board.length - 1, 2);
        edges[14] = new Position(board.length - 1, 3);
        edges[15] = new Position(board.length - 1, board.length - 2);
        edges[16] = new Position(board.length - 1, board.length - 3);
        edges[17] = new Position(board.length - 1, board.length - 4);

        edges[18] = new Position(1, board.length - 1);
        edges[19] = new Position(2, board.length - 1);
        edges[20] = new Position(3, board.length - 1);
        edges[21] = new Position(board.length - 2, board.length - 1);
        edges[22] = new Position(board.length - 3, board.length - 1);
        edges[23] = new Position(board.length - 4, board.length - 1);

        ArrayList<Move> edgeMoves = new ArrayList<>();

        for (Position p : edges) {
            if (isValidMove(this.king, p, "♙")) {
                edgeMoves.add(new Move(this.king, p));
            }
        }

        if (edgeMoves.isEmpty()) {
            return null;
        }
        return edgeMoves;
    }

    /**
     * This function checks if the king can reach an edge corner place.
     * @return true / false
     */
    public boolean canKingReachCorner() {
        Position edges[] = new Position[24];
        edges[0] = new Position(0, 1);
        edges[1] = new Position(0, 2);
        edges[2] = new Position(0, 3);
        edges[3] = new Position(0, board.length - 2);
        edges[4] = new Position(0, board.length - 3);
        edges[5] = new Position(0, board.length - 4);

        edges[6] = new Position(1, 0);
        edges[7] = new Position(2, 0);
        edges[8] = new Position(3, 0);
        edges[9] = new Position(board.length - 2, 0);
        edges[10] = new Position(board.length - 3, 0);
        edges[11] = new Position(board.length - 4, 0);

        edges[12] = new Position(board.length - 1, 1);
        edges[13] = new Position(board.length - 1, 2);
        edges[14] = new Position(board.length - 1, 3);
        edges[15] = new Position(board.length - 1, board.length - 2);
        edges[16] = new Position(board.length - 1, board.length - 3);
        edges[17] = new Position(board.length - 1, board.length - 4);

        edges[18] = new Position(1, board.length - 1);
        edges[19] = new Position(2, board.length - 1);
        edges[20] = new Position(3, board.length - 1);
        edges[21] = new Position(board.length - 2, board.length - 1);
        edges[22] = new Position(board.length - 3, board.length - 1);
        edges[23] = new Position(board.length - 4, board.length - 1);

        for (Position p : edges) {
            if (isValidMove(this.king, p, "♙")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get possible move to capture king. If king cannot be captured, returns null.
     * @return the move to capture king
     */
    public Move getCaptureKingMove () {
        if (getKingCorneredNumber() == 3) {
            int n = getKingCapturePosition();
            Position k;
            switch (n) {
                case 3:
                    k = new Position(this.king.getX(), this.king.getY() - 1);
                    break;
                case 4:
                    k = new Position(this.king.getX(), this.king.getY() + 1);
                    break;
                case 1:
                    k = new Position(this.king.getX() - 1, this.king.getY());
                    break;
                case 2:
                    k = new Position(this.king.getX() + 1, this.king.getY());
                    break;
                default:
                    return null;
            }

            ArrayList<Move> moves = possibleMovesToPosition("♟", k, false);
            if (moves.isEmpty()) {
                return null;
            }
            return moves.get(0);
        }
        return null;
    }

    /**
     * This function checks if the king can be captured
     * @return true / false
     */
    public Boolean canCaptureKing () {
        if (getKingCorneredNumber() == 3) {
            int n = getKingCapturePosition();
            Position k;
            switch (n) {
                case 3:
                    k = new Position(this.king.getX(), this.king.getY() - 1);
                    break;
                case 4:
                    k = new Position(this.king.getX(), this.king.getY() + 1);
                    break;
                case 1:
                    k = new Position(this.king.getX() - 1, this.king.getY());
                    break;
                case 2:
                    k = new Position(this.king.getX() + 1, this.king.getY());
                    break;
                default:
                    return false;
            }

            ArrayList<Move> moves = possibleMovesToPosition("♟", k, false);
            if (moves.isEmpty()) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * This function returns a random move for the given player.
     * @param player The player.
     * @return A random move.
     */
    public Move getRandomMove(String player) {
        while (true) {
            Random r = new Random();
            int x = r.nextInt(board.length);
            int y = r.nextInt(board.length);

            if (getLocation(x, y).equals("")) {
                ArrayList<Move> moves = possibleMovesToPosition(player, new Position(x, y), false);
                if (moves.size() > 0) {
                    return moves.get(0);
                }
            }
        }

    }
    public ArrayList<Move> possibleMovesToPosition (String player, Position p, boolean isPositionOccupied) {
        int x = p.getX();
        int y = p.getY();
        ArrayList<Move> moves = new ArrayList<>();

        String tmp = getPosition(p);
        if (isPositionOccupied) {
            setPosition(p, "");
        }

        if (player.equals("♔")) {
            player = "♙";
        }

        for (int i = 0; i < board.length; i++) {
            String s = getLocation(x, i);

            if (s.equals("♔")) {
                s = "♙";
            }

            if (s.equals(player)) {
                Position taken = new Position(x, i);
                if (isValidMove(taken, p, player)) {
                    moves.add(new Move(taken, p));
                }
            }
        }

        for (int i = 0; i < board.length; i++) {
            String s = getLocation(i, y);

            if (s.equals("♔")) {
                s = "♙";
            }

            if (s.equals(player)) {
                Position taken = new Position(i, y);
                if (isValidMove(taken, p, player)) {
                    moves.add(new Move(taken, p));
                }
            }
        }

        setPosition(p, tmp);

        return moves;
    }

    /**
     * This function returns an integer that tells where the king can be captured from.
     * 1 -> king x - 1
     * 2 -> king x + 1
     * 3 -> king y - 1
     * 4 -> king y + 1
     * -1 -> invalid
     * @return Where the king could be captured from
     */
    private int getKingCapturePosition () {
        Position u, d, l, r;
        int x = this.king.getX();
        int y = this.king.getY();

        u = new Position(x, y + 1);
        d = new Position(x, y - 1);
        l = new Position(x - 1, y);
        r = new Position(x + 1, y);

        if (u.getY() < board.length) {
            if(getPosition(u).equals("")) {
                return 4;
            }
        }

        if (d.getY() >= 0) {
            if(getPosition(d).equals("")) {
                return 3;
            }
        }

        if (l.getX() >= 0) {
            if(getPosition(l).equals("")) {
                return 1;
            }
        }

        if (r.getX() < board.length) {
            if(getPosition(r).equals("")) {
                return 2;
            }
        }

        return -1;
    }



    /**
     * This function returns true if the given row (x) contains the given player. False otherwise.
     * @param x The row
     * @param player The player
     * @return True / False
     */
    public boolean doesXcontainPlayer(int x, String player) {
        if (player.equals("♔")) {
            player = "♙";
        }

        for (int i = 0; i < board.length; i++) {
            String s = getLocation(x, i);

            if (s.equals("♔")) {
                s = "♙";
            }

            if (s.equals(player)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This function returns true if the given row (y) contains the given player. False otherwise.
     * @param y The row
     * @param player The player
     * @return True / False
     */
    public boolean doesYcontainPlayer(int y, String player) {
        if (player.equals("♔")) {
            player = "♙";
        }

        for (int i = 0; i < board.length; i++) {
            String s = getLocation(i, y);

            if (s.equals("♔")) {
                s = "♙";
            }

            if (s.equals(player)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This function gets the number of corners the king is surrounded by. A wall is counted as a corner.
     * A captured king will return 4, 3 part captured return 3 etc.
     * @return The number of "walls" around king.
     */
    private int getKingCorneredNumber () {
        Position u, d, l, r;
        int x = this.king.getX();
        int y = this.king.getY();
        int count = 0;
        int defending_pieces = getAllBoardPositions("♙").size();
        Position cen = new Position(this.center, this.center);

        u = new Position(x, y + 1);
        d = new Position(x, y - 1);
        l = new Position(x - 1, y);
        r = new Position(x + 1, y);

        if (u.getY() > board.length - 1) {
            if (defending_pieces <= 1) {
                count++;
            }
        } else if (u.compare(cen)) {
            count++;
        } else {
            if (getPosition(u).equals("♟")) {
                count++;
            }
        }

        if (d.getY() < 0) {
            if (defending_pieces <= 1) {
                count++;
            }
        } else if (d.compare(cen)) {
            count++;
        } else {
            if (getPosition(d).equals("♟")) {
                count++;
            }
        }

        if (l.getX() < 0) {
            if (defending_pieces <= 1) {
                count++;
            }
        } else if (l.compare(cen)) {
            count++;
        } else {
            if (getPosition(l).equals("♟")) {
                count++;
            }
        }

        if (r.getX() > board.length - 1) {
            if (defending_pieces <= 1) {
                count++;
            }
        } else if (r.compare(cen)) {
            count++;
        } else {
            if (getPosition(r).equals("♟")) {
                count++;
            }
        }

        return count;
    }

    /*private boolean canMoveFromAToB (Position p1, Position p2) {
        int x1 = p1.getX();
        int y1 = p1.getY();
        int x2 = p2.getX();
        int y2 = p2.getY();

        if (x1 == x2) {

        } else {
            y
        }
    }*/

    public int getCenter () {
        return this.center;
    }

    public void setPosition (Position p, String str) {
        this.board[p.getX()][p.getY()] = str;
    }
    public String getPosition (Position p) {
        if (this.board[p.getX()][p.getY()] == null) {
            return "";
        }
        return this.board[p.getX()][p.getY()];
    }

    public String getLocation (int x, int y) {
        if (this.board[x][y] == null) {
            return "";
        }
        return this.board[x][y];
    }

    /**
     * This function returns true if the given position is blocking the king from exiting. Returns false otherwise.
     * @param p THe position.
     * @return True / False
     */
    public boolean isBlockingKing (Position p) {
        int px = p.getX();
        int py = p.getY();
        int kx = this.king.getX();
        int ky = this.king.getY();

        // Ignore center positions
        if (((py < 2 ||  board.length -3 > py) || (px < 2 || board.length - 3 > px) ) ) {
            return false;
        }

        if (px == kx) {
            if (py > ky) {
                for (int y = ky + 1; y < board.length - 1; y++) {
                    if (y != py) {
                        if (!getLocation(px, y).equals("")) {
                            return false;
                        }
                    }
                }
                return true;
            } else {
                for (int y = ky - 1; y > 0; y--) {
                    if (y != py) {
                        if (!getLocation(px, y).equals("")) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } else if (py == ky) {
            if (px > kx) {
                for (int x = kx + 1; x < board.length - 1; x++) {
                    if (x != px) {
                        if (!getLocation(x, py).equals("")) {
                            return false;
                        }
                    }
                }
                return true;
            } else {
                for (int x = kx - 1; x > 0; x--) {
                    if (x != px) {
                        if (!getLocation(x, py).equals("")) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    public void move (Position p1, Position p2) {
        if (p1.compare(this.king)) {
            this.king = p2;
        }
        String temp = getPosition(p1);
        setPosition(p1, getPosition(p2));
        setPosition(p2, temp);
    }

    private boolean isValidPiece (Position p) {
        String str = getPosition(p);
        if (str.equals("♟") || str.equals("♙") || str.equals("♔")) {
            return true;
        }
        return false;
    }

    public String getOpposingPlayer (String plyr) {
        if (plyr.equals("♟")) {
            return "♙";
        }
        return "♟";
    }

    private boolean captureUp (String player, Position p, Boolean shouldCapture) {
        int x = p.getX();
        int y = p.getY();

        if (y > board.length - 3) {
            return false;
        }

        // Check y+
        if (getLocation(x, y + 1).equals(getOpposingPlayer(player))) {
            if (getLocation(x, y + 2).equals(player) || ((x == center && y + 2 == center) && (!getLocation(center, center).equals("♔") || player.equals("♙"))) || ((x == 0 || x == board.length - 1) && y + 2 == board.length - 1) || player.equals("♙") && getLocation(x, y + 2).equals("♔")) {
                if (shouldCapture) {
                    setPosition(new Position(x, y + 1), "");
                }

                return true;
            }
        }
        return false;
    }

    private boolean captureDown (String player, Position p, boolean shouldCapture) {
        int x = p.getX();
        int y = p.getY();

        if (y < 2) {
            return false;
        }

        if (getLocation(x, y - 1).equals(getOpposingPlayer(player))) {
            if (getLocation(x, y - 2).equals(player) || ((x == center && y - 2 == center) && (!getLocation(center, center).equals("♔") || player.equals("♙"))) || ((x == 0 || x == board.length - 1) && y - 2 == 0) || player.equals("♙") && getLocation(x, y - 2).equals("♔")) {
                if (shouldCapture) {
                    setPosition(new Position(x, y - 1), "");
                }

                return true;
            }
        }
        return false;
    }

    /**
     * This funuction counts the number of the given players pieces in the Northern quadrant.
     * @param player The player
     * @return the number of pieces.
     */
    public int countPiecesN (String player) {
        int count = 0;

        if (player.equals("♔")) {
            player = "♙";
        }

        for (int x = 0; x < ((board.length - 1) / 2); x++) {
            for (int y = 0; y < ((board.length - 1) / 2); y++) {
                String s = getLocation(x, y);
                if (s.equals("♔")) {
                    s = "♙";
                }

                if (s.equals(player)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * This funuction counts the number of the given players pieces in the Northern quadrant.
     * @param player The player
     * @return the number of pieces.
     */
    public int countPiecesE (String player) {
        int count = 0;

        if (player.equals("♔")) {
            player = "♙";
        }

        for (int x = 0; x < ((board.length - 1) / 2); x++) {
            for (int y = ((board.length - 1) / 2); y < board.length - 1; y++) {
                String s = getLocation(x, y);
                if (s.equals("♔")) {
                    s = "♙";
                }

                if (s.equals(player)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * This funuction counts the number of the given players pieces in the Northern quadrant.
     * @param player The player
     * @return the number of pieces.
     */
    public int countPiecesW (String player) {
        int count = 0;

        if (player.equals("♔")) {
            player = "♙";
        }

        for (int x = ((board.length - 1) / 2); x < board.length - 1; x++) {
            for (int y = 0; y < ((board.length - 1) / 2); y++) {
                String s = getLocation(x, y);
                if (s.equals("♔")) {
                    s = "♙";
                }

                if (s.equals(player)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * This funuction counts the number of the given players pieces in the Northern quadrant.
     * @param player The player
     * @return the number of pieces.
     */
    public int countPiecesS (String player) {
        int count = 0;

        if (player.equals("♔")) {
            player = "♙";
        }

        for (int x = ((board.length - 1) / 2); x < board.length - 1; x++) {
            for (int y = ((board.length - 1) / 2); y < board.length - 1; y++) {
                String s = getLocation(x, y);
                if (s.equals("♔")) {
                    s = "♙";
                }

                if (s.equals(player)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * This function returns the quadrant for the given position.
     * 1 -> North
     * 2 -> East
     * 3 -> West
     * 4 -> South
     * @param p The Position
     * @return A number corresponding to the quadrant.
     */
    public int getQuadrant(Position p) {
        int x = p.getX();
        int y = p.getY();

        if (x < ((board.length - 1) / 2) && y < ((board.length - 1) / 2)) {
            return 1;
        }
        if (x < ((board.length - 1) / 2) && ((board.length - 1) / 2) < y) {
            return 2;
        }
        if (((board.length - 1) / 2) < x && y < ((board.length - 1) / 2)) {
            return 3;
        }
        return 4;
    }


    /**
     * This function returns the threat score of a given quadrant.
     *      * 1 -> North
     *      * 2 -> East
     *      * 3 -> West
     *      * 4 -> South
     * @param q the quadrant
     * @param player the enemy player
     * @return the threat score
     */
    public int getQuadrantScore(int q, String player) {
        int score = 0;
        switch (score) {
            case 1:
                score += 2 * countPiecesN(player);
                score += countPiecesE(player);
                score += countPiecesW(player);
                break;
            case 2:
                score += 2 * countPiecesE(player);
                score += countPiecesN(player);
                score += countPiecesS(player);
                break;
            case 3:
                score += 2 * countPiecesW(player);
                score += countPiecesN(player);
                score += countPiecesS(player);
                break;
            case 4:
                score += 2 * countPiecesS(player);
                score += countPiecesE(player);
                score += countPiecesW(player);
                break;
        }

        return score;
    }

    /**
     * This function gets all the positions blockable in order for the king to be prevented from moving to win.
     * if no positions are found, null is returned.
     * @return blockable positions / null
     */
    public ArrayList<Position> getKingBlockPositions() {
        ArrayList<Position> block = new ArrayList<>();
        int kx = king.getX();
        int ky = king.getY();

        if (kx == 0) {
            if (isValidMove(this.king, new Position(0, 0), "♙")) {
                for (int y = 1; y < ky; y++) {
                    block.add(new Position(kx, y));
                }
            }
            if (isValidMove(this.king, new Position(0, board.length - 1), "♙")) {
                for (int y = ky + 1; y < board.length - 1; y++) {
                    block.add(new Position(kx, y));
                }
            }
        } else if (ky == 0) {
            if (isValidMove(this.king, new Position(0, 0), "♙")) {
                for (int x = 1; x < kx; x++) {
                    block.add(new Position(x, ky));
                }
            }
            if (isValidMove(this.king, new Position( board.length - 1, 0), "♙")) {
                for (int x = kx + 1; x < board.length - 1; x++) {
                    block.add(new Position(x, ky));
                }
            }
        } else if (kx == board.length - 1) {
            if (isValidMove(this.king, new Position(board.length - 1, 0), "♙")) {
                for (int y = 1; y < ky; y++) {
                    block.add(new Position(kx, y));
                }
            }
            if (isValidMove(this.king, new Position(board.length - 1, board.length - 1), "♙")) {
                for (int y = ky + 1; y < board.length - 1; y++) {
                    block.add(new Position(kx, y));
                }
            }
        } else if (ky == board.length - 1) {
            if (isValidMove(this.king, new Position(0, board.length - 1), "♙")) {
                for (int x = 1; x < kx; x++) {
                    block.add(new Position(x, ky));
                }
            }
            if (isValidMove(this.king, new Position(board.length - 1, board.length - 1), "♙")) {
                for (int x = kx + 1; x < board.length - 1; x++) {
                    block.add(new Position(x, ky));
                }
            }
        }

        if (block.isEmpty()) {
            return null;
        }

        return block;
    }

    /**
     * This function checks if the given quadrant is the most threatened quadrant based on threat score.
     * @param p A position in a quadrant.
     * @param player the enemy player
     * @return true / false
     */
    public boolean isBestQuadrant (Position p, String player) {
        int q = getQuadrant(p);
        int qscore = getQuadrantScore(q, player);

        // for every quadrant, calculate score
        for (int i = 0; i < 4; i++) {
            int score = getQuadrantScore(i + 1, player);
            if (qscore < score) {
                return false;
            }
        }
        return true;
    }

    private boolean captureLeft (String player, Position p, boolean shouldCapture) {
        int x = p.getX();
        int y = p.getY();

        if (x < 2) {
            return false;
        }

        // check x-
        if (getLocation(x - 1, y).equals(getOpposingPlayer(player))) {
            if (getLocation(x - 2, y).equals(player) || ((x - 2 == center && y == center) && (!getLocation(center, center).equals("♔") || player.equals("♙"))) || ((y == 0 || y == board.length - 1) && x - 2 == 0) || player.equals("♙") && getLocation(x - 2, y).equals("♔")) {
                if (shouldCapture) {
                    setPosition(new Position(x - 1, y), "");
                }

                return true;
            }
        }
        return false;
    }

    private boolean captureRight (String player, Position p, Boolean shouldCapture) {
        int x = p.getX();
        int y = p.getY();

        if (x > board.length - 3) {
            return false;
        }

        // check x+
        if (getLocation(x + 1, y).equals(getOpposingPlayer(player))) {
            if (getLocation(x + 2, y).equals(player) || ((x + 2 == center && y == center) && (!getLocation(center, center).equals("♔") || player.equals("♙"))) || ((y == 0 || y == board.length - 1) && x + 2 == board.length - 1) || player.equals("♙") && getLocation(x + 2, y).equals("♔")) {
                Position np = new Position(x + 1 , y);
                if (shouldCapture) {
                    setPosition(np, "");
                }

                return true;
            }
        }
        return false;
    }

    public ArrayList<Position> capturePieces(Position p, String player) {
        int x = p.getX();
        int y = p.getY();

        ArrayList<Position> captures = new ArrayList<>();

        if (1 < x && x < board.length - 2 && 1 < y && y < board.length - 2) {
            // Check all 4, validate center
            if (captureRight(player, p, true)) {
                captures.add(new Position(x + 1, y));
            }
            if (captureLeft(player, p, true)){
                captures.add(new Position( x - 1, y));
            }
            if (captureUp(player, p, true)) {
                captures.add(new Position(x, y + 1));
            }
            if (captureDown(player, p, true)) {
                captures.add(new Position(x, y - 1));
            }
        } else if (1 < x && x < board.length - 2) {
            // one y value is whack, handle as adjusted.
            if (captureLeft(player, p, true)){
                captures.add(new Position( x - 1, y));
            }
            if (captureRight(player, p, true)) {
                captures.add(new Position(x + 1, y));
            }
            if (y <= 1) {
                if (captureUp(player, p, true)) {
                    captures.add(new Position(x, y + 1));
                }
            } else {
                if (captureDown(player, p, true)) {
                    captures.add(new Position(x, y - 1));
                }
            }

        } else if (1 < y && y < board.length - 2) {
            // one x value is whack. Handle as adjusted.
            if (captureUp(player, p, true)) {
                captures.add(new Position(x, y + 1));
            }
            if (captureDown(player, p, true)) {
                captures.add(new Position(x, y - 1));
            }
            if (x <= 1) {
                if (captureRight(player, p, true)) {
                    captures.add(new Position(x + 1, y));
                }
            } else {
                if (captureLeft(player, p, true)){
                    captures.add(new Position( x - 1, y));
                }
            }

        } else {
            // Handle X
            if (x == 0 || x == 1) {
                if (captureRight(player, p, true)) {
                    captures.add(new Position(x + 1, y));
                }
            } else {
                if (captureLeft(player, p, true)){
                    captures.add(new Position( x - 1, y));
                }
            }

            // Handle Y
            if (y == 0 || y == 1) {
                if (captureUp(player, p, true)) {
                    captures.add(new Position(x, y + 1));
                }
            } else {
                if (captureDown(player, p, true)) {
                    captures.add(new Position(x, y - 1));
                }
            }
        }

        return captures;
    }

    /**
     * This function gets and returns the center position.
     * @return The center position.
     */
    public Position getCenterPosition () {
        Position cen = new Position(this.center, this.center);
        return cen;
    }


    /**
     * This function checks if a given move is valid.
     * @param p1 starting position
     * @param p2 end position
     * @param player active player
     * @return true / false
     */
    public boolean isValidMove(Position p1, Position p2, String player) {
        if (p1.compare(p2) || !p1.isValid(this.grid_size) || !p2.isValid(this.grid_size)) {
            return false;
        }

        // Check it is the correct player's turn
        if (!getPosition(p1).equals(player)) {
            if (!(getPosition(p1).equals("♔") && player.equals("♙"))) {
                return false;
            }
        }

        // Handle movement of empty spaces
        if (!isValidPiece(p1)) {
            return false;
        }

        // Prevent movement into the center
        /*Position cent = new Position(this.center, this.center);
        if (p2.compare(cent)) {
            return false;
        }*/

        // Prevent movement to castle positions
        for (Position cas : this.castles) {
            if (p2.compare(cas)) {
                if (!getPosition(p1).equals("♔")) {
                    return false;
                }
            }
        }


        int x1 = p1.getX();
        int x2 = p2.getX();
        int y1 = p1.getY();
        int y2 = p2.getY();
        int difference;

        if (x1 == x2) {
            // Handle if they have same x
            if (y1 < y2) {
                difference = y2 - y1;
                for (int i = 1; i <= difference; i++) {
                    String str = getLocation(x1, y1 + i);
                    if (!str.equals("")) {
                        return false;
                    }
                }
                return true;
            } else {
                difference = y1 - y2;
                for (int i = -1; i >= -difference; i--) {
                    String str = getLocation(x1, y1 + i);
                    if (!str.equals("")) {
                        return false;
                    }
                }
                return true;
            }

        } else if (y1 == y2) {
            // Handle if same y;
            if (x1 < x2) {
                difference = x2 - x1;
                for (int i = 1; i <= difference; i++) {
                    String str = getLocation(x1 + i,y1);
                    if (!str.equals("")) {
                        int j = 0;
                        return false;
                    }
                }
                return true;
            } else {
                difference = x1 - x2;
                for (int i = -1; i >= -difference; i--) {
                    String str = getLocation(x1+i, y1);
                    if (!str.equals("")) {
                        int j = 0;
                        return false;
                    }
                }
                return true;
            }

        }

        return false;
    }

    public String[][] getBoard () {
        return this.board;
    }

    /*public ArrayList<Position> getTakePiecePositions (Position p) {
        int player = getPosition(p);
        ArrayList<Position> captures = new ArrayList<Position>();

        if (player == 1 || player == 2 || player == 3) {
            if (player == 1) {
                for (int i = 1; i < grid_size - p.getX(); i++) {
                    if (getLocation(p.getX(), p.getY() + i) != 0) {
                        if (getLocation(p.getX(), p.getY() + i) == 2 && getLocation(p.getX(), p.getY() + i + 1) == 1) {
                            captures.add(new Position(p.getX(), p.getY() + i - 1));
                        }
                        break;
                    } else {
                        if (getLocation())
                    }
                }
                    this.board[p.getX()][p.getY() + i]
                }
            }
        }
        if (player == 1) {
    }*/
}
