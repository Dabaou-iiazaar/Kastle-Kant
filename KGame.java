 //Adam Gaisinsky and Yang Li. FSE Progress. Kant's Kastle, a tower-defense style game with a unique twist.
import java.util.*;//Importing for graphics and other helpful additions.
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;  
import javax.imageio.*; 
import javax.swing.Timer;//Specifying which Timer since there would be a conflict with util otherwise.
public class KGame extends JFrame{//Main, public class.
  public String kind="Menu";
  public boolean change=false;
  Timer myTimer;//Timer to keep the graphics at a good pace.  
  GamePanel game;
  Menu menu;
  Level level;
  public KGame() {//Constructor.
    super("Kant's Kastle");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1000,700);
    
    myTimer = new Timer(30, new TickListener());
    myTimer.start();
    menu=new Menu(this);
    game = new GamePanel(this);
    level=new Level(this);
    add(menu);
    
    setResizable(false);
    setVisible(true);
  }
  
  class TickListener implements ActionListener{//Class and its one method to update the graphics on screen every time the Timer tells them to.
    public void actionPerformed(ActionEvent evt){
      if(change==true && kind.equals("Game")){
        change=false;
        remove(level);
        add(game);
      }
      if(change==true && kind.equals("Menu")){
        change=false;
        remove(game);
        add(menu);
      }
      if(change==true && kind.equals("Level")){
        change=false;
        remove(menu);
        add(level);
      }
      if(kind.equals("Game") && game!= null && game.ready){
        game.move();
        game.repaint();
      }
      else if(kind.equals("Menu") && menu!=null && menu.ready){
        menu.repaint();
      }
      else if(kind.equals("Level") && level!=null && level.ready){
        level.repaint();
      }
    }
  }
  public static void main(String[] args){//Main method.
    KGame frame = new KGame();//Launching the graphics.
  }
}


class GamePanel extends JPanel implements KeyListener{ //Class for drawing and managing the graphics.
  private int money=100;
  private int destx,desty;//Variables for keeping track of the mouse's position.
  private KGame mainFrame;
  public boolean ready=false;
  private boolean [] keys;
  public Image[] kantMoves=new Image[12];
  public Image[] turretI=new Image[4];
  public int[] costs={25,50,100,200};
  public ArrayList<Tower> turrets=new ArrayList<Tower>();
  public ArrayList<Monster> monsters=new ArrayList<Monster>();
  public String turretType = "Basic";
  private Image background;
  private Image castle;
  private Kant kant=new Kant(70,130);
  private int picInd=2;
  private int walkSpeed=4;
  public int timer=50;
  public int oldTimer=timer;
  
  public GamePanel(KGame m){     //Constructor.
    mainFrame=m; 
    keys = new boolean[KeyEvent.KEY_LAST+1];
    addMouseListener(new clickListener());
    destx=-20;                    //Default values for the mouse's position is off screen.
    desty=-20;
    setSize(1000,700);
    addKeyListener(this);
    for(int k=0;k<12;k++){
      kantMoves[k]=new ImageIcon("Kant "+(k+1)+".png").getImage();
    }
    for(int k=0;k<4;k++){
      turretI[k]=new ImageIcon("turret "+(k+1)+".png").getImage();
    }
    background=new ImageIcon("background.png").getImage();
    castle=new ImageIcon("castle.png").getImage();
  }
  
  public void addNotify() {       //Method for notifying, seeing if the graphics are ready.
    super.addNotify();
    requestFocus();
    ready = true;
  }
  
  public void move(){
    if (keys[KeyEvent.VK_BACK_SPACE]){
      mainFrame.kind="Menu";
      mainFrame.change=true;
      keys[KeyEvent.VK_BACK_SPACE]=false;
    }
    if (keys[KeyEvent.VK_W]){
      //  picInd=kant.move(kant.x,kant.y-walkSpeed,picInd);
    }
    else if (keys[KeyEvent.VK_A]){
      picInd=kant.move(kant.x-walkSpeed,kant.y,picInd);
    }
    else if (keys[KeyEvent.VK_S]){
      //  picInd=kant.move(kant.x,kant.y+walkSpeed,picInd);
    }
    else if (keys[KeyEvent.VK_D]){
      picInd=kant.move(kant.x+walkSpeed,kant.y,picInd);
    }
    else{
      picInd=kant.move(kant.x,kant.y,picInd);
    }
    if (keys[KeyEvent.VK_SPACE]){
      boolean overlay=false;
      for(Tower t:turrets){
        if(t.x-40<kant.x && kant.x<t.x+40 && kant.y+30==t.y){
          overlay=true;
          break;
        }
      }
      if(!overlay){
        int indy=0;
        if(turretType.equals("Basic")){
          indy=0;
        }
        else if(turretType.equals("Normal")){
          indy=1;
        }
        else if(turretType.equals("Good")){
          indy=2;
        }
        else{
          indy=3;
        }
        if (money-costs[indy]>=0){
          money-=costs[indy];
          turrets.add(new Tower(kant.x,kant.y+30,turretType,125));
        }
      }
    }
  }
  public void paintComponent(Graphics g){      //Method for actually drawing all the needed graphics onto the screen.
    g.drawImage(background,0,0,1000,700,null);
    g.drawImage(castle,40,0,720,600,null);
    g.drawImage(kantMoves[picInd-1],kant.x,kant.y,40,40,null);
    
    Font font = new Font("Verdana", Font.BOLD, 14);
    g.setFont(font);//Creating the font and setting its colour.
    g.setColor(Color.white);
    g.drawString("Coins: "+money, 50, 25);
    
    for(int i=0; i<4; i++){
      g.drawImage(turretI[i],100+(i*80),620,40,40,null);
    }
    for(int t=0;t<turrets.size();t++){
      money+=turrets.get(t).shoot(monsters);
      int indy=0;
      if(turrets.get(t).type.equals("Basic")){
        indy=0;
      }
      else if(turrets.get(t).type.equals("Normal")){
        indy=1;
      }
      else if(turrets.get(t).type.equals("Good")){
        indy=2;
      }
      else{
        indy=3;
      }
      g.drawImage(turretI[indy],turrets.get(t).x,turrets.get(t).y,40,40,null);
    }
    for (Monster ms: monsters){
      ms.monsterDraw(g,turrets);
    }
    if (timer==oldTimer){
      timer=0;
      monsters.add(new Monster(800,535,"zombie"));
    }
    else{
      timer+=1;
    }
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
      System.out.println(destx);
      if(620<desty && desty<660){
        if(100<destx && destx<140){turretType="Basic";}
        else if(180<destx && destx<220){turretType="Normal";}
        else if(260<destx && destx<300){turretType="Good";}
        else if(340<destx && destx<380){turretType="Super";}
      }
    }
  }    
}


class Kant{
  public int x;
  public int y;
  public int picCount;
  public Kant(int placex,int placey){
    x=placex;
    y=placey;
    picCount=0;
  }
  public int move(int mx,int my,int ind){
    if(695<mx && mx<705){
      if(my==130 | my==260 | my==390){
        y+=130;
        x=70;
      }
      ind=2;
      return ind;
    }
    else if(55<mx && mx<65){
      if(my==260 | my==390 | my==520){
        y-=130;
        x=690;
      }
      ind=2;
      return ind;
    }
    else if(mx>x){
      x=mx;
      if (ind>6 && ind<9){
        picCount++;
        if(picCount==3){
          ind++;
          picCount=0;
        }
      }
      else{
        ind=7;
      }
      return ind;
    }
    else if(mx<x){
      x=mx;
      if (ind>3 && ind<6){
        picCount++;
        if(picCount==3){
          ind++;
          picCount=0;
        }
      }
      else{
        ind=4;
      }
      return ind;
    }
    else if(my>y){
      y=my;
      if (ind>0 && ind<3){
        picCount++;
        if(picCount==3){
          ind++;
          picCount=0;
        }
      }
      else{
        ind=1;
      }
      return ind;
    }
    else if(my<y){
      y=my;
      if (ind>9 && ind<12){
        picCount++;
        if(picCount==3){
          ind++;
          picCount=0;
        }
      }
      else{
        ind=10;
      }
      return ind;
    }
    else{
      ind=2;
      return ind;
    }
  }
}
class Tower{
  public int x;
  public int y;
  public String type;
  public int cooldown;
  public int max;
  public int health;
  public Tower(int xx,int yy, String kind, int time){
    x=xx;
    y=yy;
    type=kind;
    cooldown=time;
    max=time;
    health=100;
  }
  public int shoot(ArrayList<Monster> ms){
    Monster target=null;
    int close=180;
    for(Monster monster: ms){
      if(monster.y+15==y){
        if((monster.x-x)>0 && (monster.x-x)<close){
          close=monster.x-x;
          target=monster;
        }
      }
    }
    if(cooldown<=0){
      cooldown=max;
      if(type.equals("Basic") && target!=null){
        if(target.damage(25)){
          ms.remove(target);
          return 5;
        }
      }
      else if(type.equals("Normal") && target!=null){
        if(target.damage(50)){
          ms.remove(target);
          return 5;
        }
      }
      else if(type.equals("Good") && target!=null){
        if(target.damage(100)){
          ms.remove(target);
          return 5;
        }
      }
      else{
        if(target!=null){
          if(target.damage(200)){
            ms.remove(target);
            return 5;
          }
        }
      }
    }
    else{
      cooldown-=10;
      return 0;
    }
    return 0;
  }
  public boolean damage(int hurt){
    health-=hurt;
    if (health<=0){
      return true;
    }
    else{
      return false;
    }
  }
}



class Menu extends JPanel{
  private int destx,desty;//Variables for keeping track of the mouse's position.
  private Image screen;
  private KGame mainFrame;
  public boolean ready=false;
  public Menu(KGame m){
    mainFrame=m;
    addMouseListener(new clickListener());
    destx=-20;//Default values for the mouse's position is off screen.
    desty=-20;
    setSize(800,600);
    screen=new ImageIcon("KantScreen.jpg").getImage();
  }
  public void addNotify() {       //Method for notifying, seeing if the graphics are ready.
    super.addNotify();
    requestFocus();
    ready = true;
  }
  
  public void paintComponent(Graphics g){      //Method for actually drawing all the needed graphics onto the screen.
    g.drawImage(screen,0,0,1000,700,null);
    g.setColor(Color.red);
    g.fillRect(400,300,200,200);
  }
  
  class clickListener implements MouseListener{//Class for checking for the user's mouse inputs.
    public void mouseEntered(MouseEvent e) {}//The following methods all check for some aspect of the user's mouse input. Names and purposes are self-explanatory.
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}    
    public void mouseClicked(MouseEvent e){}  
    
    public void mousePressed(MouseEvent e){//Method for getting the coordinates of the mouse.
      destx = e.getX();
      desty = e.getY();
      if(400<destx && destx<600){
        if(300<desty && desty<500){
          mainFrame.kind="Level";
          mainFrame.change=true;
          System.out.println("yo");
        }
      }
    }
  }
}


class Level extends JPanel{
  private int destx,desty;//Variables for keeping track of the mouse's position.
  //private Image screen;
  private KGame mainFrame;
  public boolean ready=false;
  public Level(KGame m){
    mainFrame=m;
    addMouseListener(new clickListener());
    destx=-20;//Default values for the mouse's position is off screen.
    desty=-20;
    setSize(1000,700);
    //screen=new ImageIcon("KantScreen.jpg").getImage();
  }
  public void addNotify() {       //Method for notifying, seeing if the graphics are ready.
    super.addNotify();
    requestFocus();
    ready = true;
  }
  
  public void paintComponent(Graphics g){      //Method for actually drawing all the needed graphics onto the screen.
    //g.drawImage(screen,0,0,1000,700,null);
    g.setColor(Color.white);
    g.fillRect(0,0,1000,700);
    g.setColor(Color.red);
    g.fillRect(10,10,100,100);
  }
  
  class clickListener implements MouseListener{//Class for checking for the user's mouse inputs.
    public void mouseEntered(MouseEvent e) {}//The following methods all check for some aspect of the user's mouse input. Names and purposes are self-explanatory.
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}    
    public void mouseClicked(MouseEvent e){}  
    
    public void mousePressed(MouseEvent e){//Method for getting the coordinates of the mouse.
      destx = e.getX();
      desty = e.getY();
      if(10<destx && destx<110){
        if(10<desty && desty<110){
          mainFrame.kind="Game";
          mainFrame.change=true;
        }
      }
    }
  }
}
class Monster{
  public int x;
  public int y;
  public int picCount;
  public int hp;
  public int power;
  public String type;
  public String direction;
  public double speed;
  private Image[] mPicL=new Image[3];
  private Image[] mPicR=new Image[3];
  public Monster(int placex, int placey, String mtype){
    x=placex;
    y=placey;
    picCount=0;
    type=mtype;
    hp=100;
    direction="L";
    speed=1.5;
    for(int i=1; i<4; i++){
      mPicL[i-1]=new ImageIcon(type+i+"L.png").getImage();
      mPicR[i-1]=new ImageIcon(type+i+"R.png").getImage();
    }
    power=1;
  }
  public void floorUp(){
    if(695<x && x<705){
      if(y==145 | y==275 | y==405){
        y+=130;
        x=70;       
        picCount=0;
        direction="R";
      }
    }
    else if(55<x && x<65){
      if(y==275 | y==405 | y==535){
        y-=130;
        x=690;
        picCount=0;
        direction="L";
      }
    }
  }
  public Image[] mPic(){
    if(direction.equals("R")){
      return mPicR;
    }
    else{
      return mPicL;
    }
  }
  public void monsterDraw(Graphics g,ArrayList<Tower> turrets){
    floorUp();
    g.drawImage(mPic()[picCount/5],x,y,40,50,null);
    picCount+=1;
    if(picCount/5==3){
      picCount=0;
    }
    boolean overlay=false;
    for(Tower t:turrets){
      if(t.x-40<x && x<t.x+40 && y+15==t.y){
        overlay=true;
        if(t.damage(power)){
          turrets.remove(t);
        }
        break;
      }
    }
    if(!overlay){
      if(direction.equals("L")){
        x-=speed;
      }
      else{
        x+=speed;
      }
    }
  }
  public boolean damage(int hurt){
    hp-=hurt;
    if (hp<=0){
      return true;
    }
    else{
      return false;
    }
  } 
}
