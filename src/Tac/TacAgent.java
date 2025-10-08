/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tac;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import java.awt.Color;

public class TacAgent extends Agent 
{
    private TacAgentGUI tacGui; 
    public int [][] board = new int[8][8];
    boolean turn = false; // turn
    int step = 0;
    int row, column;
    String lastMsg = "";

    protected void setup() {
        for (int i=0; i < 8 ; i++) {
            for (int j=0; j < 8 ; j++) {
                board[i][j] = -1;
            }
        }
        
        // Init based board
        board[3][3] = 1;
        board[4][4] = 1;
        board[3][4] = 0;
        board[4][3] = 0;
        
        // Printout a welcome message   
        System.out.println("Tac-agent "+getAID().getName()+" is ready.");   

        // Show the GUI to interact with the user   
        tacGui = new TacGUIImplementation();   
        tacGui.setAgent(this);   
        tacGui.show();   

        // menunggu tawaran bermain dari tic
        addBehaviour(new waitingBehaviour(this));
        // bermain
        addBehaviour(new playingBehaviour(this));
    }

    // perilaku tac pada saat menunggu tawaran bermain dari tic
    class waitingBehaviour extends CyclicBehaviour {
	ACLMessage msg= receive();
        
        public waitingBehaviour (Agent a) {
            super(a);
        }
        
        public void action() {
            if (step == 0) {
                msg = receive();
                if (msg != null) {
                    lastMsg = msg.getContent();
                    msg = new ACLMessage(ACLMessage.INFORM);
                    // menjawab tawaran
                    msg.setContent( "Okay!" );
                    msg.addReceiver( new AID( "tic", AID.ISLOCALNAME) );
                    System.out.println("Tac -> Tic: "+ msg.getContent());
                    send(msg); // masuk ke tahap bermain
                    step = 1; 
                }
            }
        }
    }    
    
    // perilaku tac pada saat bermain
    class playingBehaviour extends CyclicBehaviour {
	ACLMessage msg= receive();
        
        public playingBehaviour (Agent a) {
            super(a);
        }
        
        public void action() {
            if (step == 1 && !isTurn()) {
                msg = receive();
            
                if ((msg != null) && (!msg.getContent().equals((String) lastMsg))){
                    lastMsg = msg.getContent();
                    int r = Integer.parseInt(String.valueOf(msg.getContent().charAt(0)));
                    int c = Integer.parseInt(String.valueOf(msg.getContent().charAt(2)));
                    board[r][c] = 0;
                    javax.swing.JButton btn = tacGui.getButton(r*8+c);
                    btn.setBackground(Color.blue);
                    tacGui.activateButton();
                    setTurn(true);
                }
            }
        }    
    }
    
    boolean isTurn(){
        return (turn); 
    }
    
    void setTurn(boolean b){
        turn = b;
    }
    
    void updateBoard(String bt){
        setTurn(false);
//        row = Integer.parseInt(String.valueOf(bt.charAt(1)))-1;
//        column = Integer.parseInt(String.valueOf(bt.charAt(2)))-1;
        int LL = Integer.parseInt(bt);
        row = LL / 8;
        column = LL % 8;
        // kirim berita ke tic
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	msg.setContent(""+row+" "+column);
     	msg.addReceiver( new AID( "tic", AID.ISLOCALNAME) );
        System.out.println("Tac -> Tic: " + msg.getContent());
	send(msg);
    }
    
}//end class TacAgent