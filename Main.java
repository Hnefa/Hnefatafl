import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //HnefataflGUI std = new HnefataflGUI(11);
                HnefataflGame game = new HnefataflGame(11);
                HnefataflGUI gui = new HnefataflGUI();
                //HnefataflGUI gui = new HnefataflGUI(11, game.getBoard());
                HnefataflController c = new HnefataflController(game, gui);
                int i = 0;
            }
        });
    }

}