/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tic;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;  
import java.awt.Color;
public class TicAgent extends Agent {
    private TicAgentGUI ticGui; 
    public int [][] board = new int[8][8]; 
    boolean turn = true; // turn
    int step = 0;
    int row, column;
   
    protected void setup(){
        for (int i=0; i < 8 ; i++) {
            for (int j=0; j < 8 ; j++) {
                board[i][j] = -1;
            }
        }
        
            // Init based board
//        board[3][3] = 0;
//        board[4][4] = 0;
//        board[3][4] = 1;
//        board[4][3] = 1;
        
        
        
        System.out.println("Tic-agent "+getAID().getName()+" is ready.");   

        // Show the GUI to interact with the user   
        ticGui = new TicGUIImplementation();   
        ticGui.setAgent(this);   
        ticGui.show();  
        // mengundang tac untuk bermain
        addBehaviour(new invitingBehaviour(this));
        // selanjutnya masuk ke permainan
        addBehaviour(new playingBehaviour(this));
        
    }

    protected String getButtonName(int r, int c) {
        return ticGui.getButton(r*8+c).getName();
    }
    
    // perilaku tic pada saat mengundang tac untuk bermain
    class invitingBehaviour extends CyclicBehaviour {
        String lastMsg = "";
	ACLMessage msg= receive();

        public invitingBehaviour (Agent a) {
            super(a);
        }
        
        public void action(){
            if (step == 0) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent( "Let's play board!" );
                msg.addReceiver( new AID( "tac", AID.ISLOCALNAME) );
                System.out.println("tic -> tac: "+ msg.getContent());
                send(msg);
                block(500);
                // tunggu beberapa saat
                msg= receive();
                if (msg!=null && msg.getContent().contains("Okay")) {
                    step = 1;
                    ticGui.activateButton();
                }
            }           
        }
    }
    
    // perilaku tic pada saat bermain
    class playingBehaviour extends CyclicBehaviour {
        String lastMsg = "";
	ACLMessage msg= receive();

        public playingBehaviour (Agent a) {
            super(a);
        }
        
        public void action() {
            if (step == 1 && !isTurn()) {
                msg = receive();
                if ((msg != null) && (!msg.getContent().equals((String) lastMsg))){
                    int r = Integer.parseInt(String.valueOf(msg.getContent().charAt(0)));
                    int c = Integer.parseInt(String.valueOf(msg.getContent().charAt(2)));
                    board[r][c] = 0;
                    javax.swing.JButton btn = ticGui.getButton(r*8+c);
                    btn.setBackground(Color.green);
                    ticGui.activateButton();
                    setTurn(true);
                }
            }
        }
    }

    // cek sedang dapat giliran atau tidak
    boolean isTurn(){
        return (turn); 
    }
    
    // set dapat giliran atau tidak 
    void setTurn(boolean b){
        turn = b;
    }
    
    // mencatat perubahan papan permainan
    // method ini dipanggil setiap kali ada tombol yang ditekan oleh pemain
    void updateBoard(String bt){
        setTurn(false);
//        row = Integer.parseInt(String.valueOf(bt.charAt(3)))-1;
//        column = Integer.parseInt(String.valueOf(bt.charAt(4)))-1;
        int LL = Integer.parseInt(bt);
        row = LL / 8;
        column = LL % 8;
        board[row][column] = 1;
        // kirim berita ke tac
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	msg.setContent(""+row+" "+column);
     	msg.addReceiver( new AID( "tac", AID.ISLOCALNAME) );
        System.out.println("Tic -> Tac: " + msg.getContent());
//        System.out.println("pesan "+ msg.toString());
	send(msg);
    }
    
}//end class TicAgent