public class RandomAI implements Player{

    public Move move(Board b, int roundNo, String player) {
        Move m = b.getRandomMove(player);
        return m;
    }

    public void initialize (int i) {

    }
}
