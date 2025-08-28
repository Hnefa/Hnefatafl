import java.util.ArrayList;
import java.util.PriorityQueue;

public class BarricadingCordonAI implements Player {

    private Position[] edges;
    private Position[] middleEdges;
    private Position[] riskyEdges;
    private boolean fullyBarricaded;
    private boolean cordonComplete;
    private boolean northernCordon;
    private boolean westernCordon;
    private boolean southernCordon;
    private boolean easternCordon;

    private boolean centerNorth;
    private boolean centerSouth;
    private boolean centerWest;
    private boolean centerEast;
    int bl;

    public BarricadingCordonAI () {

    }

    /**
     * This function is used for calling the edge placements things and board size
     * @param i board length
     */
    public void initialize (int i) {
        this.bl = i;
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
        fullyBarricaded = false;
        this.cordonComplete = false;
        this.northernCordon = false;
        this.easternCordon = false;
        this.westernCordon = false;
        this.southernCordon = false;
        this.centerNorth = false;
        this.centerSouth = false;
        this.centerEast = false;
        this.centerWest = false;
    }

    public Move move (Board b, int roundNo, String player) {
        if (roundNo == 0) {
            return getStartingMove(b);
        }

        // Priority #1 : Capture King
        if (b.canCaptureKing()) {
            Move m = b.getCaptureKingMove();
            return m;
        }

        // Priority #2 : Block King
        if (b.canKingReachCorner()) {
            // Block it
            ArrayList<Position> block = b.getKingBlockPositions();
            ArrayList<Move> kingMoves = b.getEdgeCornerMovesForKing();
            ArrayList<Position> pawns = b.getAllBoardPositions(player);
            ArrayList<Move> blockingMoves = new ArrayList<>();
            if (block != null) {
                // Get all block-moves
                for (Position p1 : pawns) {
                    for (Position p2 : block) {
                        if (b.isValidMove(p1, p2, player)) {
                            blockingMoves.add(new Move(p1, p2));
                        }
                    }
                    for (Move m : kingMoves) {
                        if (b.isValidMove(p1, m.getTo(),player)) {
                            blockingMoves.add(new Move(p1, m.getTo()));
                        }
                    }
                }


                if (!blockingMoves.isEmpty()) {
                    Move blocking = evaluateBlockingMove(blockingMoves, b);
                    return blocking;
                }
            } else {
                for (Position p1 : pawns) {
                    for (Move m : kingMoves) {
                        if (b.isValidMove(p1, m.getTo(), player)) {
                            blockingMoves.add(new Move(p1, m.getTo()));
                        }
                    }
                }

                if (!blockingMoves.isEmpty()) {
                    Move blocking = evaluateBlockingMove(blockingMoves, b);
                    return blocking;
                }
            }

        }

        if (!cordonComplete) {

            // Priority #4 : Take Defensive Position
            if (b.doesEnemyOccupyEdge(player)) {
                ArrayList<Position> occupied = b.getEnemyOccupiedEdges(player);
                ArrayList<Move> defensiveMoves = new ArrayList<>();

                // get all possible defensive moves
                for (Position pos : occupied) {
                    int x = pos.getX();
                    int y = pos.getY();

                    if (x == 0) {
                        ArrayList<Move> def = b.possibleMovesToPosition(player, new Position(x + 2, y), false);
                        defensiveMoves.addAll(def);
                    } else if (y == 0) {
                        ArrayList<Move> def = b.possibleMovesToPosition(player, new Position(x, y + 2), false);
                        defensiveMoves.addAll(def);
                    } else if (x == this.bl - 1) {
                        ArrayList<Move> def = b.possibleMovesToPosition(player, new Position(x - 2, y), false);
                        defensiveMoves.addAll(def);
                    } else if (y == this.bl - 1) {
                        ArrayList<Move> def = b.possibleMovesToPosition(player, new Position(x, y - 2), false);
                        defensiveMoves.addAll(def);
                    }
                }

                if (!defensiveMoves.isEmpty()) {
                    Move def = evaluateDefensiveMoves(defensiveMoves, b);
                    if (def != null) {
                        if (!b.isBlockingKing(def.getFrom())) {
                            return def;
                        }
                    } else {
                        // Continue down list of priority.
                    }

                }
            }

            // Priority #5 : Take Middle Key Location
            if (b.canTakeMiddleKeyLocation(player) && !cordonComplete) {
                ArrayList<Move> mids = new ArrayList<>();
                for (Position pos : this.middleEdges) {
                    boolean isOccupied = false;
                    if (b.getPosition(pos).equals("")) {
                        ArrayList<Move> temp = b.possibleMovesToPosition("♟", pos, false);
                        mids.addAll(temp);
                    }
                }

                if (!mids.isEmpty()) {
                    Move mid = evaluateMiddleMoves(mids, b);
                    if (b.isBlockingKing(mid.getFrom())) {
                        mid = b.getClosestMove(mid.getTo(), mid.getFrom(), player);
                        return mid;
                    } else {
                        return mid;
                    }
                }
            }

            // Priority #3 : Capture Non-Center Pieces
            /* PriorityQueue<Move> potentialCaptures = b.getPossibleCaptures(player);
            if (potentialCaptures.size() > 0 && !cordonComplete) {
                // Remove center pieces
                for(Move m: potentialCaptures) {
                    int x = m.getTo().getX();
                    int y = m.getTo().getY();
                }

                if (potentialCaptures.size() > 0) {
                    Move capture = validateCaptureMove(potentialCaptures, b);
                    if (!b.isBlockingKing(capture.getFrom())) {
                        return capture;
                    }
                }
            }
    */
            // Priority #6 : Take Edge Key Location
            if (b.canTakeEdgeKeyLocation(player) && !cordonComplete) {
                ArrayList<Move> corners = new ArrayList<>();
                for (Position pos : this.edges) {
                    ArrayList<Move> temp = b.possibleMovesToPosition("♟", pos, false);
                    corners.addAll(temp);
                }

                Move corner = evaluateCornerMoves(corners, b);
                if (!b.isBlockingKing(corner.getFrom())) {
                    return corner;
                }
            }


            // Start cordon
            ArrayList<Position> lst;
            if (!cordonComplete) {
                if (!b.isPlayerOccupyingEdgeN(b.getOpposingPlayer(player))) {
                    // move there
                    Position fp = b.getFirstFreeEgdeNPosition();
                    if (fp == null) {
                        this.cordonComplete = true;
                        this.northernCordon = true;
                        return this.move(b, roundNo, player);
                    } else {
                        lst = b.getOccupiedEdgeNPosition(player);
                        Move m = b.getClosestMoveListExcluded(fp, lst, player);
                        if (m != null) {
                            return m;
                        }
                    }
                } else if (!b.isPlayerOccupyingEdgeE(b.getOpposingPlayer(player))) {
                    // move there
                    Position fp = b.getFirstFreeEgdeEPosition();
                    if (fp == null) {
                        this.cordonComplete = true;
                        this.easternCordon = true;
                        return this.move(b, roundNo, player);
                    } else {
                        lst = b.getOccupiedEdgeEPosition(player);
                        Move m = b.getClosestMoveListExcluded(fp, lst, player);
                        if (m != null) {
                            return m;
                        }
                    }
                } else if (!b.isPlayerOccupyingEdgeS(b.getOpposingPlayer(player))) {
                    // move there
                    Position fp = b.getFirstFreeEgdeSPosition();
                    if (fp == null) {
                        this.cordonComplete = true;
                        this.southernCordon = true;
                        return this.move(b, roundNo, player);
                    } else {
                        lst = b.getOccupiedEdgeSPosition(player);
                        Move m = b.getClosestMoveListExcluded(fp, lst, player);
                        if (m != null) {
                            return m;
                        }
                    }
                } else if (!b.isPlayerOccupyingEdgeW(b.getOpposingPlayer(player))) {
                    // move there
                    Position fp = b.getFirstFreeEgdeWPosition();
                    if (fp == null) {
                        this.cordonComplete = true;
                        this.westernCordon = true;
                        return this.move(b, roundNo, player);
                    } else {
                        lst = b.getOccupiedEdgeWPosition(player);
                        Move m = b.getClosestMoveListExcluded(fp, lst, player);
                        if (m != null) {
                            return m;
                        }
                    }
                } else {
                    // Move Randomly
                    Move m = b.getRandomMove(player);
                }
            }
        } else {
            // Move cordon
            if (northernCordon) {
                for (int i = 0; i < bl - 1; i++) {
                    if (b.getCenter() == i) {
                        if (this.centerNorth == false) {
                            ArrayList<Position> exclusion = b.getBarricadeList();
                            for (int j = 0; j <= i; j++) {
                                exclusion.addAll(b.getOccupiedRow(player, j));
                            }
                            Position goal = new Position(b.getCenter() + 2, b.getCenter());
                            Position goal2 = new Position(b.getCenter() + 1, b.getCenter());
                            if (b.getPosition(goal).equals("")) {
                                Move m = b.getClosestMoveListExcluded(goal, exclusion, player);
                                if (m != null) {
                                    return m;
                                }
                            }

                            if (b.getPosition(goal2).equals(player)) {
                                centerNorth = true;
                            } else if (b.getPosition(goal2).equals("")) {
                                Move m = b.getClosestMoveListExcluded(goal2, exclusion, player);
                                if (m != null) {
                                    return m;
                                }
                            }
                        }
                    }

                    Position pos = b.getFirstOccupiedPositionInRow(player, i);
                    if (pos != null) {
                        Position n = new Position(pos.getX() + 1, pos.getY());
                        if (b.isValidMove(pos, n, player)) {
                            Move m = new Move(pos, n);
                            return m;
                        } else {
                            int fst = pos.getY();
                            while (fst < bl - 1) {
                                fst++;
                                Position n2 = new Position(pos.getX(), fst);
                                if (b.getPosition(n2).equals(player)) {
                                    Position n3 = new Position(n2.getX() + 1, n2.getY());
                                    if(b.isValidMove(n2, n3, player)) {
                                        Move m = new Move(n2, n3);
                                        return m;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (southernCordon) {
                for (int i = bl - 1; i >= 0; i++) {
                    if (b.getCenter() == i) {
                        if (this.centerSouth == false) {
                            ArrayList<Position> exclusion = b.getBarricadeList();
                            for (int j = bl - 1; j >= i; j--) {
                                exclusion.addAll(b.getOccupiedRow(player, j));
                            }
                            Position goal = new Position(b.getCenter() - 2, b.getCenter());
                            Position goal2 = new Position(b.getCenter() - 1, b.getCenter());
                            if (b.getPosition(goal).equals("")) {
                                Move m = b.getClosestMoveListExcluded(goal, exclusion, player);
                                if (m != null) {
                                    return m;
                                }
                            }

                            if (b.getPosition(goal2).equals(player)) {
                                centerSouth = true;
                            } else if (b.getPosition(goal2).equals("")) {
                                Move m = b.getClosestMoveListExcluded(goal2, exclusion, player);
                                if (m != null) {
                                    return m;
                                }
                            }
                        }
                    }

                    Position pos = b.getFirstOccupiedPositionInRow(player, i);
                    if (pos != null) {
                        Position n = new Position(pos.getX() - 1, pos.getY());
                        if (b.isValidMove(pos, n, player)) {
                            Move m = new Move(pos, n);
                            return m;
                        }
                        else {
                            int fst = pos.getY();
                            while (fst < bl - 1) {
                                fst++;
                                Position n2 = new Position(pos.getX(), fst);
                                if (b.getPosition(n2).equals(player)) {
                                    Position n3 = new Position(n2.getX() - 1, n2.getY());
                                    if (b.isValidMove(n2, n3, player)) {
                                        Move m = new Move(n2, n3);
                                        return m;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (easternCordon) {
                for (int i = 0; i < bl - 1; i++) {
                    if (b.getCenter() == i) {
                        if (this.centerEast == false) {
                            ArrayList<Position> exclusion = b.getBarricadeList();
                            for (int j = 0; j <= i; j++) {
                                exclusion.addAll(b.getOccupiedColumn(player, j));
                            }
                            Position goal = new Position(b.getCenter(), b.getCenter() + 2);
                            Position goal2 = new Position(b.getCenter(), b.getCenter() + 1);
                            if (b.getPosition(goal).equals("")) {
                                Move m = b.getClosestMoveListExcluded(goal, exclusion, player);
                                if (m != null) {
                                    return m;
                                }
                            }

                            if (b.getPosition(goal2).equals(player)) {
                                centerEast = true;
                            } else if (b.getPosition(goal2).equals("")) {
                                Move m = b.getClosestMoveListExcluded(goal2, exclusion, player);
                                if (m != null) {
                                    return m;
                                }
                            }
                        }
                    }

                    Position pos = b.getFirstOccupiedPositionInColumn(player, i);
                    if (pos != null) {
                        Position n = new Position(pos.getX(), pos.getY() + 1);
                        if (b.isValidMove(pos, n, player)) {
                            Move m = new Move(pos, n);
                            return m;
                        }
                        else {
                            int fst = pos.getY();
                            while (fst < bl - 1) {
                                fst++;
                                Position n2 = new Position(pos.getX(), fst);
                                if (b.getPosition(n2).equals(player)) {
                                    Position n3 = new Position(n2.getX() , n2.getY() + 1);
                                    if (b.isValidMove(n2, n3, player)) {
                                        Move m = new Move(n2, n3);
                                        return m;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (westernCordon) {
                for (int i = bl - 1; i >= 0; i++) {
                    if (b.getCenter() == i) {
                        if (this.centerWest == false) {
                            ArrayList<Position> exclusion = b.getBarricadeList();
                            for (int j = bl - 1; j >= i; j--) {
                                exclusion.addAll(b.getOccupiedColumn(player, j));
                            }
                            Position goal = new Position(b.getCenter(), b.getCenter() - 2);
                            Position goal2 = new Position(b.getCenter(), b.getCenter() - 1);
                            if (b.getPosition(goal).equals("")) {
                                Move m = b.getClosestMoveListExcluded(goal, exclusion, player);
                                if (m != null) {
                                    return m;
                                }
                            }

                            if (b.getPosition(goal2).equals(player)) {
                                centerWest = true;
                            } else if (b.getPosition(goal2).equals("")) {
                                Move m = b.getClosestMoveListExcluded(goal2, exclusion, player);
                                if (m != null) {
                                    return m;
                                }
                            }
                        }
                    }

                    Position pos = b.getFirstOccupiedPositionInColumn(player, i);
                    if (pos != null) {
                        Position n = new Position(pos.getX(), pos.getY() - 1);
                        if (b.isValidMove(pos, n, player)) {
                            Move m = new Move(pos, n);
                            return m;
                        }
                        else {
                            int fst = pos.getY();
                            while (fst < bl - 1) {
                                fst++;
                                Position n2 = new Position(pos.getX(), fst);
                                if (b.getPosition(n2).equals(player)) {
                                    Position n3 = new Position(n2.getX() , n2.getY() - 1);
                                    if (b.isValidMove(n2, n3, player)) {
                                        Move m = new Move(n2, n3);
                                        return m;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }




        // Game should be won if reached here, but if not, handle it
        // Play defensive! make sure you can never be taken!

        Move m = b.getRandomMove(player);
        return m;
    }

    private Move evaluateBlockingMove(ArrayList<Move> blockingMoves, Board b) {
        Move best = null;
        int bestScore = 0;

        for (Move m : blockingMoves) {
            int score = 1;

            // reward/punish middle corner movements
            for (Position p : this.edges) {
                if (m.getTo().compare(p)) {
                    score += 10;
                }
                if (m.getFrom().compare(p)) {
                    if (b.possibleMovesToPosition("♙", m.getFrom(), true).isEmpty()) {
                        score -= 20;
                    }else {
                        score -= 12;
                    }

                }
            }

            // reward/punish middle corner movements
            for (Position p : this.middleEdges) {
                if (m.getTo().compare(p)) {
                    score += 15;
                }
                if (m.getFrom().compare(p)) {
                    if (b.possibleMovesToPosition("♙", m.getFrom(), true).isEmpty()) {
                        score -= 30;
                    }else {
                        score -= 16;
                    }

                }
            }

            // reward/punish risky corner movements
            for (Position p : this.riskyEdges) {
                if (m.getTo().compare(p)) {
                    score += 12;
                }
                if (m.getFrom().compare(p)) {
                    if (b.possibleMovesToPosition("♙", m.getFrom(), true).isEmpty()) {
                        score -= 3;
                    }else {
                        score -= 1;
                    }

                }
            }

            // reward best quadrant
            if (b.isBestQuadrant(m.getTo(), "♙")) {
                score += 1;
            }

            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }

        // Idk if this will happen but IF
        if (best == null) {
            return blockingMoves.get(0);
        }
        return best;
    }

    /**
     * This function evaluates possible defensive moves. If no defensive move is decent, null is returned.
     * @param defensiveMoves list of defensive moves.
     * @param b The board.
     * @return The best available defensive move / null
     */
    private Move evaluateDefensiveMoves(ArrayList<Move> defensiveMoves, Board b) {
        Move best = null;
        int bestScore = 0;

        for (Move m : defensiveMoves) {
            int score = 0;
            int x = m.getTo().getX();
            int y = m.getTo().getY();

            if (m.getCaptures() < 0) {
                score += m.getCaptures() * 3;
            }

            // reward/punish edge position movements
            for (Position e : this.edges) {
                if (m.getFrom().compare(e)) {
                    if (b.possibleMovesToPosition("♙", m.getFrom(), true).isEmpty()) {
                        score -= 2;
                    } else {
                        score -= 50;
                    }
                }
            }

            // reward/punish middle corner movements
            for (Position mid : this.edges) {

                if (m.getFrom().compare(mid)) {
                    if (b.possibleMovesToPosition("♙", m.getFrom(), true).isEmpty()) {
                        score -= 100;
                    }else {
                        score -= 5;
                    }

                }
            }

            // Punish moving if piece is blocking king.
            if (b.isBlockingKing(m.getFrom())) {
                score -= 1000;
            }

            // reward best quadrant
            if (b.isBestQuadrant(m.getTo(), "♙")) {
                score += 1;
            }

            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }

        if (best == null || bestScore < -51) {
            return null;
        }

        return best;
    }

    /**
     * This function evaluates all moves that take the edge key position and returns the best one.
     * @param corners The edge moves available.
     * @param b the board.
     * @return The best available move.
     */
    private Move evaluateCornerMoves(ArrayList<Move> corners, Board b) {
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
                    if (b.possibleMovesToPosition("♙", m.getFrom(), true).isEmpty()) {
                        score -= 16;
                    }else {
                        score -= 4;
                    }

                }
            }

            // Punish moving if piece is blocking king.
            if (b.isBlockingKing(m.getFrom())) {
                score -= 1000;
            }

            // reward best quadrant
            if (b.isBestQuadrant(m.getTo(), "♙")) {
                score += 1;
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
    private Move evaluateMiddleMoves(ArrayList<Move> mids, Board b) {
        Move best = null;
        int bestScore = 0;

        for (Move m : mids) {
            int score = 1;

            // reward/punish middle corner movements
            if (!b.possibleMovesToPosition("♙", m.getTo(), true).isEmpty()) {
                score += 20;
            } else {
                score += 5;
            }

            if (middleEdges[0].compare(m.getFrom()) || middleEdges[1].compare(m.getFrom()) || middleEdges[2].compare(m.getFrom()) || middleEdges[3].compare(m.getFrom())) {
                if (b.possibleMovesToPosition("♙", m.getFrom(), true).isEmpty()) {
                    score -= 20;
                }else {
                    score -= 5;
                }

            }

            // Punish moving if piece is blocking king.
            if (b.isBlockingKing(m.getFrom())) {
                score -= 1000;
            }

            // reward best quadrant
            if (b.isBestQuadrant(m.getTo(), "♙")) {
                score += 1;
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
     * This function selects the optimal move to make based of the given choices.
     * THIS FUNCTION DOES NOT CHECK IF YOU GET CAPTURED AFTER
     * @param pq capture moves
     * @param b the board
     * @return the best move.
     */
    private Move validateCaptureMove(PriorityQueue<Move> pq, Board b) {
        Move best = null;
        int bestScore = 0;

        for (Move m : pq) {
            int score = 0;
            int x = m.getTo().getX();
            int y = m.getTo().getY();

            if (m.getCaptures() < 0) {
                score += m.getCaptures() * 3;
            }

            if ((2 < x && x < b.getBoard().length - 3) && (2 < y && y < b.getBoard().length - 3)) {
                score -= 100;
            }

            // reward/punish edge position movements
            for (Position e : this.edges) {
                if (m.getTo().compare(e)) {
                    score += 2;
                }
                if (m.getFrom().compare(e)) {
                    if (b.possibleMovesToPosition("♙", m.getFrom(), true).isEmpty()) {
                        score -= 2;
                    } else {
                        score -= 7;
                    }

                }
            }

            // Punish moving if piece is blocking king.
            if (b.isBlockingKing(m.getFrom())) {
                score -= 1000;
            }

            // reward/punish middle corner movements
            for (Position mid : this.edges) {
                if (m.getTo().compare(mid)) {
                    score += 5;
                }
                if (m.getFrom().compare(mid)) {
                    if (b.possibleMovesToPosition("♙", m.getFrom(), true).isEmpty()) {
                        score -= 20;
                    }else {
                        score -= 5;
                    }

                }
            }

            // reward best quadrant
            if (b.isBestQuadrant(m.getTo(), "♙")) {
                score += 1;
            }

            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }

        if (best == null) {
            return pq.poll();
        }
        return best;
    }

    private Move getStartingMove(Board b) {
        int val = b.getCenter();
        Position p1 = new Position(0, val - 1);
        Position p2 = new Position(1, val - 1);
        Move m = new Move(p1, p2);
        return m;
    }
}
