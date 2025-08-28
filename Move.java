public class Move {
    private Position p1; // from
    private Position p2; // to

    private int captures;

    public Move (Position p1, Position p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.captures = 0;
    }

    public int getCaptures () {
        return this.captures;
    }

    public void addCaptures (int i) {
        this.captures += i;
    }

    public Position getFrom() {
        return this.p1;
    }

    public Position getTo() {
        return this.p2;
    }
}
