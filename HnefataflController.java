import javax.swing.*;
import java.util.ArrayList;

public class HnefataflController {
    private HnefataflGUI gui;
    private HnefataflGUI gameGui;
    private HnefataflGame game;
    private Position p1 = null;
    private Position p2 = null;


    public HnefataflController (HnefataflGame game, HnefataflGUI gui) {
        this.gui = gui;

        setStartButtonBehaviour();
    }

    public HnefataflController (HnefataflGame game, HnefataflGUI gui, boolean p1_human, boolean p2_human) {
        this.gui = gui;

        // if no humans only give next move access.
        if (!p1_human && !p2_human) {

        } else {
            if (p1_human && p2_human) {
                // Keep button controls on at all times.
                setStartButtonBehaviour();
            } else if (p1_human) {
                // Enable it for only p1
            } else {
                // enable it for only p2
            }
        }
        //setStartButtonBehaviour();


    }

    private void setStartButtonBehaviour() {
        JButton start = this.gui.getStartButton();
        start.addActionListener(e -> {
            int size = this.gui.getGridSizeFromUser();
            String p1 = this.gui.getPlayer1();
            String p2 = this.gui.getPlayer2();

            if (size >= 9 && size % 2 == 1) {
                // Start game
                createGameWindow(size, p1, p2);

            } else {
                // Do nothing
                System.out.println(size + " and this is " + p1 + " followed by " + p2);
            }
        });
    }

    public void createGameWindow (int size, String player1, String player2) {

        Player plyr1 = getPlayerFromString(player1);
        Player plyr2 = getPlayerFromString(player2);

        this.game = new HnefataflGame(size, plyr1, plyr2); // <-- GIVE PARAMETERS FOR GAME HERE
        this.gameGui = new HnefataflGUI(size, this.game.getBoard());



        if (player1 != "Human") {
            plyr1.initialize(size);
            AIMove();
        }

        if (player2 != "Human") {
            plyr2.initialize(size);
        }

        if (player1 == "Human" || player2 == "Human") {
            JButton[][] board = gameGui.getBoard();

            for(int row = 0; row < board.length; row++) {
                for (int col = 0; col < board.length; col++) {
                    int finalRow = row;
                    int finalCol = col;

                    board[finalRow][finalCol].addActionListener(e -> {
                        if (p1 == null) {
                            p1 = new Position(finalRow, finalCol);
                        } else {
                            p2 = new Position(finalRow, finalCol);
                            ArrayList<Position> cap = game.move(p1, p2);
                            if (cap != null) {
                                gameGui.movePiece(p1, p2);
                                System.out.println("Moving piece from " + p1.getX() + ", " + p1.getY() + " to " + p2.getX() + ", " +p2.getY() + ".");
                                gameGui.clearCaptures(cap);
                            } else {
                                gameGui.displayMessage("Could not move piece. Invalid move.\n The active player is " + game.getActivePlayer() + ".");
                            }
                            p1 = null;
                            p2 = null;
                            if ((player1 == "Human" || player2 == "Human") && !(player1 == "Human" && player2 == "Human")) {
                                AIMove();
                            }
                        }
                    });
                }
            }
        } else {
            gameGui.addNextTurnButton();
            JButton nextButton = gameGui.getNextButton();
            nextButton.addActionListener(e -> {
               AIMove();
            });
        }
    }

    private void AIMove() {
        ArrayList<Position> cap = game.Move();
        Move m = game.getLastMove();
        Position p1 = m.getFrom();
        Position p2 = m.getTo();
        gameGui.movePiece(p1, p2);
        gameGui.clearCaptures(cap);
        int state = game.getGameState();
        String msg;

        switch (state) {
            case 0:
                msg = "Congraturlations! The Defending Player Won The Game!";
                gameGui.displayMessage(msg);
                break;
            case 1:
                msg = "Congraturlations! The Attacking Player Won The Game!";
                gameGui.displayMessage(msg);
                break;
            default:
                // do nothing
                break;
        }
    }

    private Player getPlayerFromString (String player) {
        switch (player) {
            case "Human":
                break;
            case "Barricading Aggressive AI":
                return new CornerBlockingAI();
                
            case "Barricading Cordon AI":
                return new BarricadingCordonAI();
                
            case "King's Guard AI":
                return new KingsGuardAI();
                
            case "Mobile King AI":
                return new MobileKingAI();
                
            case "Foot-In-The-Door AI":
                return new FootInTheDoorAI();
                
            case "Defensive AI":
                return new DefensiveAI();

            case "Aggressive AI":
                return new AttackingHunnFocusedAI();

            case "Fortification AI":
                return new FortificationAI();

            case "Hnefi Focused AI":
                return new HnefiFocusedAI();

            case "Random AI":
                return new RandomAI();
        }

        return null;
    }
}

