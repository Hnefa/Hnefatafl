import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;

public class HnefataflGUI {
    private int size = 9;
    private JFrame frame;
    private JFrame titleFrame;
    private JButton[][] board;
    private JFormattedTextField gridSize;
    private String[] attackingPlayers = {"Human", "Aggressive AI", "Barricading AI", "Random AI"};
    private String[] defendingPlayers = {"Human", "Fortification AI", "King's Guard AI", "Random AI"};
    private JComboBox<String> p1;
    private JComboBox<String> p2;
    private JButton start;
    private JButton next;

    public HnefataflGUI() {
        titleFrame = new JFrame("Hnefatafl Game Simulator");
        titleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        titleFrame.setSize(500, 300);
        titleFrame.setLayout(new BorderLayout());

        JPanel upperPanel = new JPanel();
        JPanel centerPanel = new JPanel();
        JPanel lowerPanel = new JPanel();

        // Create a NumberFormatter that only accepts integers
        NumberFormat format = NumberFormat.getIntegerInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setAllowsInvalid(false); // Prevents non-integer input
        formatter.setMinimum(0);           // Optional: set min value
        formatter.setMaximum(111);         // Optional: set max value

        gridSize = new JFormattedTextField(formatter);
        gridSize.setColumns(10);

        JLabel sizeLabel = new JLabel("Board Size:");
        upperPanel.add(sizeLabel);
        upperPanel.add(gridSize);

        JLabel p1_lbl = new JLabel("Attacking Player:");
        p1 = new JComboBox<String>(attackingPlayers);
        JLabel p2_lbl = new JLabel("Defending Player:");
        p2 = new JComboBox<String>(defendingPlayers);
        centerPanel.add(p1_lbl);
        centerPanel.add(p1);
        centerPanel.add(p2_lbl);
        centerPanel.add(p2);

        start = new JButton("Start");
        lowerPanel.add(start);

        titleFrame.add(upperPanel, BorderLayout.NORTH);
        titleFrame.add(centerPanel, BorderLayout.CENTER);
        titleFrame.add(lowerPanel, BorderLayout.SOUTH);

        titleFrame.setVisible(true);


    }

    /*public void displayMessage(String s) {
        JOptionPane.showMessageDialog(null, s);
    }*/

    public HnefataflGUI(int size, Board b) {
        this.board = new JButton[size][size];
        this.size = size;
        frame = new JFrame("Hnefatafl");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);

        frame.setLayout(new GridLayout(size + 1, size));
        //frame.add(this.next);

        createBoard(b);
        this.next = new JButton("Next");
        this.next.setVisible(false);
        //
        //
        frame.add(this.next);
        frame.setVisible(true);
    }

    public JButton getNextButton() {
        return this.next;
    }

    public void addNextTurnButton() {
        this.next.setVisible(true);
    }

    public JButton[][] getBoard () {
        return this.board;
    }
    private void createBoard(Board b) {
        for (int row = 0; row < this.size; row++) {
            for (int col = 0; col < this.size; col++) {
                JButton square = new JButton(b.getBoard()[row][col]);
                square.setFont(new Font("Arial Unicode MS", Font.PLAIN, 32));
                square.setOpaque(true);
                square.setBorderPainted(true);

                // Color alternation
                if ((row == 0 && col == 0) || (row == size - 1 && col == 0) || (row == 0 && col == size - 1) || (row == size - 1 && col == size - 1) || (row == b.getCenter() && col == b.getCenter())) {
                    square.setBackground(new Color(181, 136, 99)); // dark
                } else {
                    square.setBackground(new Color(240, 217, 181)); // light
                }

                //

                board[row][col] = square;
                frame.add(square);
            }
        }
    }

    public void clearCaptures (ArrayList<Position> lst) {
        for (Position p: lst) {
            this.board[p.getX()][p.getY()].setText("");
        }
    }

    /**
     * Display information to user.
     * @param message The information
     */
    public void displayMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    public JButton getStartButton () {
        return this.start;
    }

    public int getGridSizeFromUser() {
        if (this.gridSize.getValue() == null) {
            return -1;
        }
        return (int) this.gridSize.getValue();
    }

    public String getPlayer1 () {
        String player = (String) p1.getSelectedItem();
        return player;
    }

    public String getPlayer2 () {
        String player = (String) p2.getSelectedItem();
        return player;
    }

    public void movePiece (Position p1, Position p2) {
        String s = this.board[p1.getX()][p1.getY()].getText();
        this.board[p1.getX()][p1.getY()].setText("");
        this.board[p2.getX()][p2.getY()].setText(s);
    }

}
