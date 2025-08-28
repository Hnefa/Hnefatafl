import java.util.ArrayList;

public class HnefiFocusedAI implements Player {

    public void initialize (int i) {

    }
    public Move move(Board b, int roundNo, String player) {
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

        // Priority #3 Move as close to king as possible
        Position hnefi = b.getKing();
        return b.getClosestMove(hnefi, new Position(-1, -1), player);
    }

    private Move evaluateBlockingMove(ArrayList<Move> blockingMoves, Board b) {
        Move best = null;
        double bestScore = 999999;
        Position hnefi = b.getKing();

        for (Move m : blockingMoves) {
            double score = 1;

            Position p = m.getTo();
            double dist = b.getDistanceBetweenPoints(hnefi, p);
            score += dist;

            if (bestScore < score) {
                best = m;
                bestScore = score;
            }
        }

        // Idk if this will happen but IF
        if (best == null) {
            return blockingMoves.get(0);
        }
        return best;
    }

}
