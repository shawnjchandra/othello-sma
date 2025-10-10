/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PlayerOne;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import java.awt.Color;
import java.util.ArrayList;

public class PlayerOneAgent extends Agent {

    private PlayerOneAgentGUI playerOneGui;
    public int[][] board = new int[8][8];
    boolean turn = true; // turn
    int step = 0;
    int row, column;
    String lastMsg = "";
    int pieceCount = 0;
    
    
    protected void setup() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = -1;
            }
        }

        // Init based board
        board[3][3] = 1;   // Player One Hijau
        board[4][4] = 1;
        board[3][4] = 0;    // Player Two Biru
        board[4][3] = 0;

        // Printout a welcome message   
        System.out.println("PlayerOne-agent " + getAID().getName() + " is ready.");

        // Show the GUI to interact with the user   
        playerOneGui = new PlayerOneGUIImplementation();
        playerOneGui.setAgent(this);
        playerOneGui.show();

        // mengundang player Two
        addBehaviour(new invitingBehaviour(this));
        // bermain
        addBehaviour(new playingBehaviour(this));
    }

    // perilaku tac pada saat menunggu tawaran bermain dari PlayerTwo
    private class invitingBehaviour extends CyclicBehaviour {

        ACLMessage msg = receive();

        public invitingBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            if (step == 0) {
                
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent( "Let's play board!" );
                msg.addReceiver( new AID( "pTwo", AID.ISLOCALNAME) );
                System.out.println("Player One -> Player Two : "+ msg.getContent());
                send(msg);
                block(500);
                // tunggu beberapa saat
                msg= receive();
                if (msg!=null && msg.getContent().contains("Okay")) {
                    step = 1;
                    playerOneGui.activateButton();
                }
            }
        }
    }

    // perilaku tac pada saat bermain
    private class playingBehaviour extends CyclicBehaviour {
        ACLMessage msg = receive();

        public playingBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            if (step == 1 && !isTurn()) {
                msg = receive();
                if ((msg != null) && (!msg.getContent().equals((String) lastMsg))) {
                    lastMsg = msg.getContent();

                    int r = Integer.parseInt(String.valueOf(msg.getContent().charAt(0)));
                    int c = Integer.parseInt(String.valueOf(msg.getContent().charAt(2)));
                    
                    board[r][c] = 1;
                    
                    javax.swing.JButton btn = playerOneGui.getButton(r * 8 + c);
                    
                    btn.setBackground(Color.blue);
                    
                    flipDisc(r, c, 1);
                    updateGUI();
                    
                    checkGameEnd();

                    playerOneGui.activateButton();
                    setTurn(true);
                }
            }
        }
    }

    boolean isTurn() {
        return (turn);
    }

    void setTurn(boolean b) {
        turn = b;
    }

    void setPieceCount(int pieceCount) {
        this.pieceCount = pieceCount;
    }

    void updateBoard(String bt) {
        int LL = Integer.parseInt(bt);
        row = LL / 8;
        column = LL % 8;

        if (isValidMove(row, column, 0)) { // 1 untuk playerOneAgent
            board[row][column] = 0;
            flipDisc(row, column, 0);
            setTurn(false); // Ganti giliran

            // Kirim pesan ke pTwo
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("" + row + " " + column);
            msg.addReceiver(new AID("pTwo", AID.ISLOCALNAME));
            send(msg);

            // Update GUI setelah flip
            updateGUI();
        } 
    }

    void updateGUI() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == 1) {
                    playerOneGui.getButton(i * 8 + j).setBackground(Color.green);
                } else if (board[i][j] == 0) {
                    playerOneGui.getButton(i * 8 + j).setBackground(Color.blue);
                }
            }
        }
    }

    boolean isValidMove(int row, int col, int player) {
        if (board[row][col] != -1) {
            return false;
        }

        int opponent = (player == 1) ? 0 : 1;
        boolean valid = false;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int r = row + dr;
                int c = col + dc;
                boolean foundOpponent = false;

                while (r >= 0 && r < 8 && c >= 0 && c < 8 && board[r][c] == opponent) {
                    // PERBAIKAN: Lanjutkan ke arah yang sama
                    r += dr;
                    c += dc;
                    foundOpponent = true;
                }

                if (foundOpponent && r >= 0 && r < 8 && c >= 0 && c < 8 && board[r][c] == player) {
                    valid = true;
                    break; // Cukup temukan satu arah yang valid
                }
            }
            if (valid) {
                break;
            }
        }
        return valid;
    }

    void flipDisc(int row, int col, int player) {
        int opponent = (player == 1) ? 0 : 1;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int r = row + dr;
                int c = col + dc;
                ArrayList<int[]> toFlip = new ArrayList<>();

                while (r >= 0 && r < 8 && c >= 0 && c < 8 && board[r][c] == opponent) {
                    toFlip.add(new int[]{r, c});
                    r += dr;
                    c += dc;
                }

                if (r >= 0 && r < 8 && c >= 0 && c < 8 && board[r][c] == player) {
                    for (int[] coordinate : toFlip) {
                        board[coordinate[0]][coordinate[1]] = player;
                    }
                }
            }
        }
    }

    int countPOnePieces() {
        int currPieceCount = 0;
        for (int i = 0;i < 8;++i) {
            for (int j = 0;j < 8;++j) {
                if (board[i][j] == 0) {
                    ++currPieceCount;
                }
            }
        }
        return currPieceCount;
    }

    int countPTwoPieces() {
        int currPieceCount = 0;
        for (int i = 0;i < 8;++i) {
            for (int j = 0;j < 8;++j) {
                if (board[i][j] == 1) {
                    ++currPieceCount;
                }
            }
        }
        return currPieceCount;
    }

    boolean hasValidMove(int player) {
        for (int i = 0;i < 8;++i) {
            for (int j = 0;j < 8;++j) {
                if (isValidMove(i, j, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isBoardFull() {
        for (int i = 0;i < 8;++i) {
            for (int j = 0;j < 8;++j) {
                if (board[i][j] == -1) {
                    return false;
                }
            }
        }
        return true;
    }

    void checkGameEnd() {
        boolean pOneCanMove = hasValidMove(0);
        boolean pTwoCanMove = hasValidMove(1);
        boolean boardFull = isBoardFull();
        boolean end = false;
        String announcement = "";

        if (!pOneCanMove || !pTwoCanMove) {
            end = true;
            announcement = getAnnouncement(false); 
        }
        if (boardFull) {
            end = true;
            announcement = getAnnouncement(true); 
        }
        
        if(end){
            System.out.println(announcement);
            updateGUI();
            step = 2;
        }
    }
    
    String getAnnouncement (boolean full) {
        String announcement = "";
        int pOnePieces = countPOnePieces();
        int pTwoPieces = countPTwoPieces();
        
        if (full) {
            announcement += "=== GAME OVER ===\n";
        } else {
            announcement += "=== NO MORE MOVES!! ===\n";
        }
        
        announcement +=("PlayerOne pieces = " + pOnePieces+"\n");
        announcement +=("PlayerTwo pieces = " + pTwoPieces+"\n");
        if (pOnePieces > pTwoPieces) {
            announcement +=("PlayerOne Wins!\n");
        } else if (pTwoPieces > pOnePieces) {
            announcement +=("PlayerTwo Wins!\n");
        } else {
            announcement +=("Draw!\n");
        }
        
        return announcement;
    }
}//end class PlayerOne
