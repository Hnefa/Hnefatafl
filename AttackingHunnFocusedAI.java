import java.util.PriorityQueue;

public class AttackingHunnFocusedAI implements Player {
    private int bl;
    private int c;

    public AttackingHunnFocusedAI () {

    }

    public void initialize (int i) {
        this.bl = i;
        this.c = (i + 1) / 2;
    }

    public Move move (Board b, int roundNo, String player) {
        // Perform h1->h3 Opening
        if (roundNo == 0) {
            Position p1 = new Position(bl-1, c+1);
            Position p2 = new Position(bl -3, c+1);
            Move m = new Move(p1, p2);
            return m;
        }

        // Priority 1 : Capture King
        if (b.canCaptureKing()) {
            Move m = b.getCaptureKingMove();
            return m;
        }

        // Priority 2 : Capture Hunn
        PriorityQueue<Move> possibleCaptures = b.getPossibleCaptures(player);
        if (possibleCaptures.size() > 0) {
            // Capture the MOST pieces possible.
            Move m = possibleCaptures.poll();
            return m;
        }

        // Priority 3 : Move as close to the king as possible
        Move closest = b.getClosestMove(b.getKing(), new Position(-1, -1), player);
        Move second = b.getClosestMove(b.getKing(), closest.getFrom(), player);
        // Get the 2 closest moves to the king. If the player can move to any without risking capture, do so.
        if (b.canHunnBeCaptured(closest.getTo(), player)) {
            if (!b.canHunnBeCaptured(second.getTo(), player)) {
                return second;
            }
        } else {
            return closest;
        }

        // Priority 4 : Select random move that doesnt get you captured.
        Move m = b.getRandomMove(player);
        return m;
    }
}
