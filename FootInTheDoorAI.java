import java.util.ArrayList;
import java.util.PriorityQueue;

public class FootInTheDoorAI implements Player {

    private boolean hasKingExitedThrone;
    private Position[] c1;
    private Position[] c2;
    private Position[] c3;
    private Position[] c4;
    private Position[] edges;
    private Position[] middleEdges;
    private Position[] riskyEdges;
    private Position[] throneExits;
    int bl;

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

        this.edges = new Position[8];
        this.edges[0] = new Position(2, 0);
        this.edges[1] = new Position(0, 2);
        this.edges[2] = new Position(bl - 3, 0);
        this.edges[3] = new Position(0, bl - 3);
        this.edges[4] = new Position(bl - 1, 2);
        this.edges[5] = new Position(2, bl - 1);
        this.edges[6] = new Position(bl - 1 , bl - 3);
        this.edges[7] = new Position(bl - 3, bl - 1);
        this.middleEdges = new Position[4];
        this.middleEdges[0] = new Position(1, 1);
        this.middleEdges[1] = new Position(1, bl - 2);
        this.middleEdges[2] = new Position(bl - 2, 1);
        this.middleEdges[3] = new Position(bl - 2, bl - 2);
        this.riskyEdges = new Position[8];
        this.riskyEdges[0] = new Position(0, 1);
        this.riskyEdges[1] = new Position(1, 0);
        this.riskyEdges[2] = new Position(0, bl - 2);
        this.riskyEdges[3] = new Position(bl - 2, 0);
        this.riskyEdges[4] = new Position(bl - 1, 1);
        this.riskyEdges[5] = new Position(1, bl - 1);
        this.riskyEdges[6] = new Position(bl - 1, bl - 2);
        this.riskyEdges[7] = new Position(bl - 2, bl - 1);

        //
        int c = i/2;
        this.throneExits = new Position[8];
        this.throneExits[0] = new Position(c, c + 2);
        this.throneExits[1] = new Position(c, c + 1);
        this.throneExits[2] = new Position(c + 2, c);
        this.throneExits[3] = new Position(c + 1, c);
        this.throneExits[4] = new Position(c, c - 2);
        this.throneExits[5] = new Position(c, c- 1);
        this.throneExits[6] = new Position(c - 2, c);
        this.throneExits[7] = new Position(c - 1, c);
    }

    public Move move(Board b, int roundNo, String player) {
        // Priority #0 : WIN
        Move winningMove = b.getKingWinningMove(player);
        if (winningMove != null) {
            return winningMove;
        }

        // Priority #1 : Take Middle Key Location
        if (b.canTakeMiddleKeyLocation(player)) {
            ArrayList<Move> mids = new ArrayList<>();
            for (Position pos : this.middleEdges) {
                ArrayList<Move> temp = b.possibleMovesToPosition(player, pos, false);
                mids.addAll(temp);
            }

            if (!mids.isEmpty()) {
                Move mid = evaluateMiddleMoves(mids, b, player);
                //mid = b.getClosestMove(mid.getTo(), mid.getFrom(), player);
                return mid;
            }
        }

        // Priority #2 : Take Edge Key Location
        if (b.canTakeEdgeKeyLocation(player)) {
            ArrayList<Move> corners = new ArrayList<>();
            for (Position pos : this.edges) {
                ArrayList<Move> temp = b.possibleMovesToPosition(player, pos, false);
                corners.addAll(temp);
            }

            Move corner = evaluateCornerMoves(corners, b, player);
        }

        // Priority #3 : Take Edge Key Location
        if (!hasKingExitedThrone) {
            Move m = getExitThroneMove(b, player);
            if (m != null) {
                return m;
            }
        }

        // Priority #4 : Protect King
        if (b.canCaptureKing()) {
            Move m = b.getCaptureKingMove();
            if (m != null) {
                Move block = evaluateBlockingMoves(m, b, player);
                if (block != null) {
                    return block;
                }
            }

        }

        // Priority #5 : Move King Towards Exit
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

        // Priority #6 : Fight
        PriorityQueue<Move> captureMoves = b.getPossibleCaptures(player);
        if (captureMoves == null) {
            int a = 0;
        }
        if (captureMoves.size() > 0) {
            Move m = evaluateCaptureMoves(captureMoves, b);
            if (m != null) {
                return m;
            }
        }

        // Perform random move
        return b.getRandomMove(player);
    }

    private Move getExitThroneMove (Board b, String player) {
        int counter = 0;

        for (int i = 0; i < throneExits.length; i++) {
            if (!b.getPosition(throneExits[i]).equals("")) {
                if (b.getPosition(throneExits[i]).equals(player)) {
                    return b.getLongestMoveAnyDirection(throneExits[i], player);
                } else {
                    // Skip next move if it is in same direction.
                    if (i % 2 == 0) {
                        i++;
                    }
                }
            } else {
                counter++;
            }

            if (i % 2 == 1) {
                if (counter == 2) {
                    // move king to current direction.
                    this.hasKingExitedThrone = true;
                    return b.getLongestMoveAnyDirection(b.getKing(), player);
                } else {
                    counter = 0;
                }
            }
        }
        return null;
    }

    /**
     * This function evaluates all moves that take the edge key position and returns the best one.
     * @param corners The edge moves available.
     * @param b the board.
     * @return The best available move.
     */
    private Move evaluateCornerMoves(ArrayList<Move> corners, Board b, String player) {
        Move best = null;
        int bestScore = 0;

        for (Move m : corners) {
            int score = 1;

            // reward/punish middle corner movements
            for (Position p : this.edges) {
                if (m.getTo().compare(p)) {
                    score += 4;
                }
                if (m.getFrom().compare(p)) {
                    if (b.possibleMovesToPosition(player, m.getFrom(), true).isEmpty()) {
                        score -= 16;
                    }else {
                        score -= 4;
                    }

                }
            }

            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }

        // Idk if this will happen but IF
        if (best == null) {
            return corners.get(0);
        }
        return best;
    }

    /**
     * This function evaluates all moves that take the middle key position and returns the best one.
     * @param mids The middle moves available.
     * @param b the board.
     * @return The best available move.
     */
    private Move evaluateMiddleMoves(ArrayList<Move> mids, Board b, String player) {
        Move best = null;
        int bestScore = 0;

        for (Move m : mids) {
            int score = 1;

            if (middleEdges[0].compare(m.getFrom()) || middleEdges[1].compare(m.getFrom()) || middleEdges[2].compare(m.getFrom()) || middleEdges[3].compare(m.getFrom())) {
                if (b.possibleMovesToPosition(b.getOpposingPlayer(player), m.getFrom(), true).isEmpty()) {
                    score -= 20;
                }else {
                    score -= 5;
                }
                //score -= 10;
            }

            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }

        // Idk if this will happen but IF
        if (best == null) {
            return mids.get(0);
        }
        return best;
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
    private Move evaluateCaptureMoves (PriorityQueue<Move> moves, Board b) {
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
