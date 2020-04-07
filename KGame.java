import java.util.*;//Importing for graphics and other helpful additions.
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;  
import javax.imageio.*; 
import javax.swing.Timer;//Specifying which Timer since there would be a conflict with util otherwise.
public class KGame extends JFrame{//Main, public class.
  Timer myTimer;//Timer to keep the graphics at a good pace.  
  GamePanel game;
  public KGame() {//Constructor.
    super("Kant's Kastle");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800,625);
    
    myTimer = new Timer(10, new TickListener());
    myTimer.start();
    
    game = new GamePanel(this);
    add(game);
    
    setResizable(false);
    setVisible(true);
    }
    
 class TickListener implements ActionListener{//Class and its one method to update the graphics on screen every time the Timer tells them to.
  public void actionPerformed(ActionEvent evt){
   if(game!= null && game.ready){
    game.move();
    game.repaint();
   }   
  }
 }
  public static void main(String[] args){//Main method.
    KGame frame = new KGame();//Launching the graphics.
  }
}


class GamePanel extends JPanel implements KeyListener{//Class for drawing and managing the graphics.
 private int destx,desty;//Variables for keeping track of the mouse's position.
 private KGame mainFrame;
 public boolean ready=false;
 private boolean [] keys;
 public Image[] kantMoves=new Image[12];
 private Image background;
 private Kant kant=new Kant(0,0);
 private int picInd=1;
 
 public GamePanel(KGame m){//Constructor.
  mainFrame=m;
  keys = new boolean[KeyEvent.KEY_LAST+1];
  addMouseListener(new clickListener());
  destx=-20;//Default values for the mouse's position is off screen.
  desty=-20;
  setSize(800,600);
  addKeyListener(this);
  for(int k=0;k<12;k++){
    kantMoves[k]=new ImageIcon("Kant "+(k+1)+".png").getImage();
  }
 }
 
    public void addNotify() {//Method for notifying, seeing if the graphics are ready.
        super.addNotify();
        requestFocus();
        ready = true;
    }
    
 public void move(){
   if (keys[KeyEvent.VK_W]){
     picInd=kant.move(kant.x,kant.y-1,picInd);
   }
   else if (keys[KeyEvent.VK_A]){
     picInd=kant.move(kant.x-1,kant.y,picInd);
   }
   else if (keys[KeyEvent.VK_S]){
     picInd=kant.move(kant.x,kant.y+1,picInd);
   }
   else if (keys[KeyEvent.VK_D]){
     picInd=kant.move(kant.x+1,kant.y,picInd);
   }
 }
    public void paintComponent(Graphics g){//Method for actually drawing all the needed graphics onto the screen.
      g.drawImage(kantMoves[picInd-1],kant.x,kant.y,40,40,null);
    }
        public void keyTyped(KeyEvent e) {
 }

 public void keyPressed(KeyEvent e) {
   keys[e.getKeyCode()] = true;
 }
    
 public void keyReleased(KeyEvent e) {
   keys[e.getKeyCode()] = false;
 }
    class clickListener implements MouseListener{//Class for checking for the user's mouse inputs.
     public void mouseEntered(MouseEvent e) {}//The following methods all check for some aspect of the user's mouse input. Names and purposes are self-explanatory.
     public void mouseExited(MouseEvent e) {}
     public void mouseReleased(MouseEvent e) {}    
     public void mouseClicked(MouseEvent e){}  
       
     public void mousePressed(MouseEvent e){//Method for getting the coordinates of the mouse.
   destx = e.getX();
   desty = e.getY(); 
  }
    }    
}


class Kant{
  public int x;
  public int y;
  public Kant(int placex,int placey){
    x=placex;
    y=placey;
  }
  public int move(int mx,int my,int ind){
    if(mx>x){
      x=mx;
      if (ind>6 && ind<9){
        ind++;
      }
      else{
        ind=7;
      }
      return ind;
    }
    else if(mx<x){
      x=mx;
      if (ind>3 && ind<6){
        ind++;
      }
      else{
        ind=4;
      }
      return ind;
    }
    else if(my>y){
      y=my;
      if (ind>0 && ind<3){
        ind++;
      }
      else{
        ind=1;
      }
      return ind;
    }
    else if(my<y){
      y=my;
      if (ind>9 && ind<12){
        ind++;
      }
      else{
        ind=10;
      }
      return ind;
    }
    else{
      ind=1;
      return ind;
    }
  }
}