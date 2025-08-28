import java.util.ArrayList;
import java.util.PriorityQueue;

public class DefensiveAI implements Player {
    private boolean hasKingExitedThrone;
    private Position[] c1;
    private Position[] c2;
    private Position[] c3;
    private Position[] c4;
    private Position[] throneExits;
    int bl;

    public DefensiveAI () {

    }

    public void initialize (int i) {
        this.bl = i;
        this.c1 = new Position[3];
        this.c2 = new Position[3];
        this.c3 = new Position[3];
        this.c4 = new Position[3];
        // Corner 1
        this.c1[0] = new Position(2, 0);
        this.c1[1] = new Position(0, 2);
        this.c1[2] = new Position(1, 1);

        // Corner 2
        this.c2[0] = new Position(bl - 3, 0);
        this.c2[1] = new Position(bl - 1, 2);
        this.c2[2] = new Position(bl - 2, 1);

        // Corner 3
        this.c3[0] = new Position(0, bl - 3);
        this.c3[1] = new Position(2, bl - 1);
        this.c3[2] = new Position(1, bl - 2);

        // Corner 4
        this.c4[0] = new Position(bl - 1 , bl - 3);
        this.c4[1] = new Position(bl - 3, bl - 1);
        this.c4[2] = new Position(bl - 2, bl - 2);
    }

    public Move move(Board b, int roundNo, String player) {
        // Priority #0 : WIN
        Move winningMove = b.getKingWinningMove(player);
        if (winningMove != null) {
            return winningMove;
        }

        // Priority 1: Protect King
        if (b.canCaptureKing()) {
            Move m = b.getCaptureKingMove();
            if (m != null) {
                Move block = evaluateBlockingMoves(m, b, player);
                if (block != null) {
                    return block;
                }
            }

        }

        // Priority 2: Protect Hunn
        ArrayList<Position> positions = b.getAllBoardPositions(player);
        for (Position p : positions) {
            if (!p.compare(b.getKing())) {
                if (b.canHunnBeCaptured(p, player)) {
                    // Move hunn
                    ArrayList<Move> escapes = b.getAllMovesFromPosition(p, player);
                    Move m = evaluateEscapeMoves(b, player, escapes);
                    if (m != null) {
                        return m;
                    }
                }
            }
        }

        // Priority 3: Fight
        PriorityQueue<Move> captureMoves = b.getPossibleCaptures(player);
        if (captureMoves == null) {
            int a = 0;
        }
        if (captureMoves.size() > 0) {
            Move m = evaluateCaptureMoves(captureMoves, b, player);
            if (m != null) {
                return m;
            }
        }

        // Priority 4: Move king towards exit
        ArrayList<Move> cornerMoves = b.getEdgeCornerMovesForKing();
        if (cornerMoves != null) {
            Move m = cornerMoves.get(0);
            if (m != null) {
                return m;
            }
        } else {
            ArrayList<Position> exits = getFreeExits(b);
            if (exits.size() > 0) {
                Move ex = getBestExitMove(exits, b, player);
                if (ex != null) {
                    return ex;
                }
            }
        }

        // Perform random non capturable move
        for (int i = 0; i < 10; i++) {
            Move m = b.getRandomMove(player);
            if (!b.canHunnBeCaptured(m.getTo(), player)) {
                return m;
            }
        }
        return b.getRandomMove(player);
    }

    private Move evaluateEscapeMoves (Board b, String player, ArrayList<Move> esc) {
        int bestScore = 0;
        Move bestMove = null;

        for (Move m : esc) {
            int score = 1;

            if (b.canHunnBeCaptured(m.getTo(), player)) {
                score -= 10;
            } else {
                score += 5;
            }

            if (score > bestScore) {
                bestScore = score;
                bestMove = m;
            }
        }

        return bestMove;
    }

    /**
     * This function gets a move that blocks capture of the king.
     * @param threat The threatening move
     * @param b The board
     * @param player the player
     * @return a move / null
     */
    private Move evaluateBlockingMoves (Move threat, Board b, String player) {
        int kx = b.getKing().getX();
        int ky = b.getKing().getY();
        int tx = threat.getTo().getX();
        int ty = threat.getTo().getX();

        if (kx == tx) {
            if (ky < ty) {
                for (int y = ty; y < threat.getFrom().getY(); y++) {
                    Position p = new Position(kx, y);
                    ArrayList<Move> defense = b.possibleMovesToPosition(player, p, false);
                    if (defense.size() > 0) {
                        return defense.get(0);
                    }
                }
            } else {
                for (int y = ty; y > threat.getFrom().getY(); y--) {
                    Position p = new Position(kx, y);
                    ArrayList<Move> defense = b.possibleMovesToPosition(player, p, false);
                    if (defense.size() > 0) {
                        return defense.get(0);
                    }
                }
            }
        } else {
            if (kx < tx) {
                for (int x = tx; x < threat.getFrom().getX(); x++) {
                    Position p = new Position(x, ky);
                    ArrayList<Move> defense = b.possibleMovesToPosition(player, p, false);
                    if (defense.size() > 0) {
                        return defense.get(0);
                    }
                }
            } else {
                for (int x = tx; x > threat.getFrom().getX(); x--) {
                    Position p = new Position(x, ky);
                    ArrayList<Move> defense = b.possibleMovesToPosition(player, p, false);
                    if (defense.size() > 0) {
                        return defense.get(0);
                    }
                }
            }
        }

        return null;
    }

    /**
     * This function moves the king as close to open exit as possible
     * @param goals the open exits.
     * @param b the board.
     * @param player The player
     * @return The move that gets the king closest to an exit.
     */
    private Move getBestExitMove(ArrayList<Position> goals, Board b, String player) {
        double shortest = Double.MAX_VALUE;
        Move best = null;
        ArrayList<Move> k_moves = b.getAllKingMoves();

        for (Position p : goals) {
            for (Move m : k_moves) {
                double distance = b.getDistanceBetweenPoints(p, m.getTo());
                if (distance < shortest) {
                    shortest = distance;
                    best = m;
                }
            }
        }

        return best;
    }

    /**
     * This function gets the best capture move by getting the capture closest to the king
     * @param moves List of possible moves
     * @param b The board
     * @return Best capture.
     */
    private Move evaluateCaptureMoves (PriorityQueue<Move> moves, Board b, String player) {
        Position k = b.getKing();
        int kx = k.getX();
        int ky = k.getY();

        int bestScore = 0;
        Move bestMove = null;
        Position hnefi = b.getKing();

        for (Move m : moves) {
            int score = 1;
            if (m.getFrom().compare(hnefi)) {
                score += 100;
            }

            if (b.canHunnBeCaptured( m.getTo(), player)) {
                score -= 200;
            }

            if (score > bestScore) {
                bestScore = score;
                bestMove = m;
            }
        }

        return bestMove;
    }

    private ArrayList<Position> getFreeExits (Board b) {
        ArrayList<Position> lst = new ArrayList<>();
        for (Position p : c1) {
            if (b.getPosition(p).equals("")) {
                lst.add(p);
            }
        }
        for (Position p : c2) {
            if (b.getPosition(p).equals("")) {
                lst.add(p);
            }
        }
        for (Position p : c3) {
            if (b.getPosition(p).equals("")) {
                lst.add(p);
            }
        }
        for (Position p : c4) {
            if (b.getPosition(p).equals("")) {
                lst.add(p);
            }
        }
        return lst;
    }
}
