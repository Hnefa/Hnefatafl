public class Position {
    private int x;
    private int y;

    public Position (int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX () {
        return this.x;
    }

    public int getY () {
        return this.y;
    }

    /**
     * This function checks if a given position is a valid position on the board.
     * @param size The size of the board.
     * @return True if on the board, false if not.
     */
    public boolean isValid(int size) {
        if ((0 <= this.x && this.x < size) && (0 <= this.y && this.y < size)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
    public boolean compare (Position p) {
        if (p.getX() == this.getX() && p.getY() == this.getY()) {
            return true;
        }
        return false;
    }
}
