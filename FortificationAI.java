public class FortificationAI implements Player {
    private Boolean isFortBuilt;
    private Boolean isNorthernFortBuilt;
    private Boolean isSouthernFortBuilt;
    private Boolean isEasternFortBuilt;
    private Boolean isWesternFortBuilt;
    private Position[] north;
    private Position[] south;
    private Position[] east;
    private Position[] west;

    private int bl;
    private int c;

    public FortificationAI () {
        this.isFortBuilt = false;
        this.isNorthernFortBuilt = false;
        this.isSouthernFortBuilt = false;
        this.isEasternFortBuilt = false;
        this.isWesternFortBuilt = false;
    }

    public void initialize(int i) {
        this.bl = i;
        this.c = (bl + 1) / 2;
        this.c -= 1;

        this.north = new Position[6];
        this.east = new Position[6];
        this.south = new Position[6];
        this.west = new Position[6];

        // Define north Fort
        this.north[0] = new Position(c - 1, c - 1);
        this.north[1] = new Position(c - 1, c - 2);
        this.north[2] = new Position(c - 1, c);
        this.north[3] = new Position(c + 1, c - 1);
        this.north[4] = new Position(c + 1, c - 2);
        this.north[5] = new Position(c, c - 1);

        // Define South Fort
        this.south[0] = new Position(c + 1, c + 1);
        this.south[1] = new Position(c + 1, c + 2);
        this.south[2] = new Position(c + 1, c);
        this.south[3] = new Position(c - 1, c + 1);
        this.south[4] = new Position(c - 1, c + 2);
        this.south[5] = new Position(c, c + 1);

        // Define East Fort
        this.east[0] = new Position(c + 1, c + 1);
        this.east[1] = new Position(c + 2, c + 1);
        this.east[2] = new Position(c, c + 1);
        this.east[3] = new Position(c + 1, c - 1);
        this.east[4] = new Position(c + 2, c - 1);
        this.east[5] = new Position(c + 1, c);

        // Define West Fort
        this.west[0] = new Position(c - 1, c - 1);
        this.west[1] = new Position(c - 2, c - 1);
        this.west[2] = new Position(c, c - 1);
        this.west[3] = new Position(c - 1, c + 1);
        this.west[4] = new Position(c - 2, c + 1);
        this.west[5] = new Position(c - 1, c);
    }

    public Move move (Board b, int roundNo, String player) {

        if (this.isFortBuilt) {
            // Game is tied, move back and forth in fort
            if (!b.getKing().compare(b.getCenterPosition())) {
                // move to center position
                Move m = new Move (b.getKing(), b.getCenterPosition());
                return m;
            } else {
                Position from = b.getKing();
                Position to;
                if (isNorthernFortBuilt) {
                    to = north[5];
                } else if (isSouthernFortBuilt) {
                    to = south[5];
                } else if (isEasternFortBuilt) {
                    to = east[5];
                } else {
                    to = west[5];
                }
                Move m = new Move(from, to);
                return m;

            }
        } else {
            // Priority 1 : Build Northern Fort
            if (isFortCompletable(b, player, this.north)) {
                Move m = getMoveForFort(b, player, this.north);
                if (m != null) {
                    if (m.getTo().compare(this.north[3])) {
                        this.isNorthernFortBuilt = true;
                    }
                    return m;
                }
            }

            // Priority 2: Build Western Fort
            if (isFortCompletable(b, player, this.west)) {
                Move m = getMoveForFort(b, player, this.west);
                if (m != null) {
                    if (m.getTo().compare(this.west[3])) {
                        this.isWesternFortBuilt = true;
                    }
                    return m;
                }
            }

            // Priority 3: Build Southern Fort
            if (isFortCompletable(b, player, this.south)) {
                Move m = getMoveForFort(b, player, this.south);
                if (m != null) {
                    if (m.getTo().compare(this.south[3])) {
                        this.isSouthernFortBuilt = true;
                    }
                    return m;
                }
            }

            // Priority 4: Build Eastern Fort
            if (isFortCompletable(b, player, this.south)) {
                Move m = getMoveForFort(b, player, this.north);
                if (m != null) {
                    if (m.getTo().compare(this.east[3])) {
                        this.isEasternFortBuilt = true;
                    }
                    return m;
                }
            }

        }

        // Select random Move
        Move m = b.getRandomMove(player);
        return m;

    }

    private Move getMoveForFort (Board b, String player, Position[] fort) {
        Position from;
        Position to;
        Boolean movedFourthPiece = false;

        if (b.getPosition(fort[1]).equals(player)) {
            // First piece is moved
            if (b.getPosition(fort[0]).equals(player)) {
                // Second piece is moved
                if (b.getPosition(fort[4]).equals(player)) {
                    // Third Piece is moved
                    if (b.getPosition(fort[3]).equals(player)) {
                        // Fourth Piece is moved
                        // Should never reach here.
                        this.isFortBuilt = true;
                        return null;
                    } else {
                        movedFourthPiece = true;
                        from = fort[5];
                        to = fort[3];
                    }
                } else {
                    from = fort[3];
                    to = fort[4];
                }
            } else {
                from = fort[2];
                to = fort[0];
            }
        } else {
            from = fort[0];
            to = fort[1];
        }

        if (b.isValidMove(from, to, player)) {
            if (movedFourthPiece) {
                this.isFortBuilt = true;
            }
            Move m = new Move(from, to);
            return m;
        }
        return null;
    }

    /**
     * Helper function used to check if a fort is completable.
     * @param b the board
     * @param player the player
     * @param pos a fort
     * @return true / false;
     */
    private boolean isFortCompletable(Board b, String player, Position[] pos) {
        for (Position p : pos) {
            if (b.getPosition(p).equals(b.getOpposingPlayer(player))) {
                return false;
            }
        }
        return true;
    }
}
