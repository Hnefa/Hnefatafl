import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                HnefataflGame game = new HnefataflGame(11);
                HnefataflGUI gui = new HnefataflGUI();
                HnefataflController c = new HnefataflController(game, gui);
            }
        });
    }


}
