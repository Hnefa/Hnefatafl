import java.security.InvalidParameterException;
import java.util.*;

public class HnefataflGame {
    private int playerTurn = 0;
    private Board gameboard;
    private Queue<String> gameHistory;
    private int winner;
    private String[] activePlayer;

    private Player player1;
    private Player player2;
    private Move lastMove;

    public HnefataflGame (int boardSize) throws InvalidParameterException {
        this.playerTurn = 0;
        this.winner = -1;
        this.activePlayer = new String[2];
        this.activePlayer[0] = "♟";
        this.activePlayer[1] = "♙";
        this.gameHistory = new LinkedList<String>();
        this.gameboard = new Board(boardSize);
        this.lastMove = null;
    }

    public HnefataflGame (int boardSize, Player p1, Player p2) throws InvalidParameterException {
        this.playerTurn = 0;
        this.winner = -1;
        this.activePlayer = new String[2];
        this.activePlayer[0] = "♟";
        this.activePlayer[1] = "♙";
        this.gameHistory = new LinkedList<String>();
        this.gameboard = new Board(boardSize);
        this.player1 = p1;
        this.player2 = p2;
        this.lastMove = null;
    }

    /**
     * This function validates if a move can be done. If move can be done, move is done in gameBoard, move added to gameHistory, turn order increased
     * and otherwise updated. then true is returned. If move cannot be done, return false.
     * @param p1 From position
     * @param p2 To position
     * @return returns all captured positions
     */
    public ArrayList<Position> move (Position p1, Position p2) {
        if (gameboard.isValidMove(p1, p2, activePlayer[playerTurn % 2])) {
            gameboard.move(p1, p2);
            ArrayList<Position> cap = gameboard.capturePieces(p2, activePlayer[playerTurn % 2]);
            String str = p1.toString() + " -> " +p2.toString();
            gameHistory.add(str);
            playerTurn++;
            this.lastMove = new Move(p1, p2);
            return cap;
        }
        return null;
    }

    /**
     * This gets the current game state. Returns 0 if white won, 1 if black won and -1 if there is no winner.
     * @return 0, 1 or -1
     */
    public int getGameState () {
        // if white has won, return 0
        if (gameboard.hasKingEscaped()) {
            return 0;
        }

        // return 1 if black won
        if (gameboard.isKingCaptured()) {
            return 1;
        }

        // return -1 otherwise
        return -1;
    }

    /**
     * Selects a move automatically for AI agents
     * @return Captured positions
     */
    public ArrayList<Position> Move () {
        Move m;
        if (playerTurn % 2 == 0) {
            m = player1.move(gameboard, playerTurn, activePlayer[0]);
        } else {
            m = player2.move(gameboard, playerTurn, activePlayer[1]);
        }
        Position p1 = m.getFrom();
        Position p2 = m.getTo();
        gameboard.move(p1, p2);
        ArrayList<Position> cap = gameboard.capturePieces(p2, activePlayer[playerTurn % 2]);
        String str = p1.toString() + " -> " +p2.toString();
        gameHistory.add(str);
        playerTurn++;
        this.lastMove = m;
        return cap;
    }

    public Board getBoard() {
        return this.gameboard;
    }

    public Move getLastMove() {
        return this.lastMove;
    }

    public String getActivePlayer () {
        return activePlayer[playerTurn % 2];
    }
}
