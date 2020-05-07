//Adam Gaisinsky and Yang Li. FSE Progress. Kant's Kastle, a tower-defense style game with a unique twist.
import java.util.*;//Importing for graphics and other helpful additions.
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;  
import javax.imageio.*; 
import javax.sound.midi.*;
import java.applet.*;
import javax.swing.Timer;//Specifying which Timer since there would be a conflict with util otherwise.
public class KGame extends JFrame{//Main, public class.
  public String kind="Menu";
  public boolean change=false;
  Timer myTimer;//Timer to keep the graphics at a good pace.  
  GamePanel game;
  Menu menu;
  Level level;
  private static Sequencer midiPlayer;
  public int healthG=5;
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
        game=new GamePanel(level.mainFrame);
        remove(level);
        add(game);
        midiPlayer.stop();
      }
      if(change==true && kind.equals("Menu")){
        change=false;
        remove(game);
        add(menu);
      }
      if(change==true && kind.equals("Level")){
        change=false;
        remove(menu);
        remove(game);
        add(level);
        startMidi("Moz2.mid",-1);
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
  public static void startMidi(String midFilename,int len) {//Method for playing the music and loading it up.
      try {//Midi music player function taken from Mr. Mckenzie.
         File midiFile = new File(midFilename);//Getting the music to be loaded in the following lines.
         Sequence song = MidiSystem.getSequence(midiFile);
         midiPlayer = MidiSystem.getSequencer();
         midiPlayer.open();
         midiPlayer.setSequence(song);
         midiPlayer.setLoopCount(len);//In effect the music lasts forever.
         midiPlayer.start();
      } catch (MidiUnavailableException e) {//Below is all for catching potential errors when loading the music.
         e.printStackTrace();
      } catch (InvalidMidiDataException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
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
  public Image[] turretI=new Image[5];
  public int[] costs={25,50,100,175,75};
  public ArrayList<Tower> turrets=new ArrayList<Tower>();
  public ArrayList<Monster> monsters=new ArrayList<Monster>();
  public ArrayList<Bullet> bullets=new ArrayList<Bullet>();
  public ArrayList<Bullet> trashB=new ArrayList<Bullet>();
  public String turretType = "Basic";
  public int tBox=100;
  private Image background;
  private Image castle;
  private Kant kant=new Kant(70,130);
  private int picInd=2;
  private int walkSpeed=4;
  public int timer=0;
  public int monSpawn=80;
  public boolean breakIn=false;
  public double volume=0;
  private Monster tmpMon;
  private Tower tmpTurret;
  public Sound[] canS=new Sound[4];
  public Sound[] monS=new Sound[4];
  private double soundC=-1;
  public GameMaker game;
  public int soundCount=40;
  private Image lost;
  private Image won;
  public boolean ends=false;
  public boolean didWon=false;
  
  public GamePanel(KGame m){     //Constructor.
    mainFrame=m; 
    keys = new boolean[KeyEvent.KEY_LAST+1];
    addMouseListener(new clickListener());
    destx=-20;                    //Default values for the mouse's position is off screen.
    desty=-20;
    setSize(1000,700);
    addKeyListener(this);
    tmpMon=null;
    tmpTurret=null;
    for(int k=0;k<12;k++){
      kantMoves[k]=new ImageIcon("Kant "+(k+1)+".png").getImage();
    }
    for(int k=0;k<4;k++){
      turretI[k]=new ImageIcon("turret "+(k+1)+".png").getImage();
      canS[k]=new Sound("cannon"+(k+1)+".wav");
      monS[k]=new Sound("monsterS"+(k+1)+".wav");
    }
    turretI[4]=new ImageIcon("barricade.png").getImage();
    background=new ImageIcon("background.png").getImage();
    castle=new ImageIcon("castle.png").getImage();
    lost=new ImageIcon("loser.png").getImage();
    won=new ImageIcon("winner.png").getImage();
    String[]gameMons={"z","z","z","z","z","z","z","z","w","w","w","w","w","w","z","z","z","z","z","-500","2","w","w","w","w","w","s","s","w","w","v","v","s","w","w","v","v","s","z","z","z","z","z","-500","3","d","d","k","k","k","w","w","w","s","s","w","v","v","v"};
    game=new GameMaker(gameMons,mainFrame);
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
        else if(turretType.equals("Great")){
          indy=3;
        }
        else{
          indy=4;
        }
        if (money-costs[indy]>=0){
          money-=costs[indy];
          turrets.add(new Tower(kant.x,kant.y+30,turretType,indy,100,mainFrame));
        }
      }
    }
  }
  public void paintComponent(Graphics g){      //Method for actually drawing all the needed graphics onto the screen.
    if(ends){
      if(didWon){
        g.drawImage(won,0,0,1000,700,null);
      }
      else{
        g.drawImage(lost,0,0,1000,700,null);
      }
      if(keys[KeyEvent.VK_ENTER]){
        mainFrame.kind="Level";
        mainFrame.change=true;
        ends=false;
        didWon=false;
      }
      return;
    }
    g.drawImage(background,0,0,1000,700,null);
    g.drawImage(castle,40,0,720,600,null);
    g.drawImage(kantMoves[picInd-1],kant.x,kant.y,40,40,null);
    
    Font font = new Font("Verdana", Font.BOLD, 14);
    if(game.spot==-1 && monsters.size()==0){
      mainFrame.healthG=5;
      ends=true;
      didWon=true;
      return;
    }
    g.setFont(font);//Creating the font and setting its colour.
    g.setColor(Color.white);
    g.drawString("Coins: "+money,50,25);
    g.drawString("Wave "+game.wave,50,50);
    g.setColor(new Color(230,230,230));
    g.fillRect(790,90,180,350);
    g.setColor(Color.red);
    g.drawRect(tBox,620,40,40);
    g.drawString("Power Levels",830,110);
    
    for(int i=0; i<5; i++){
      g.drawImage(turretI[i],100+(i*80),620,40,40,null);
    }
    int shotC=1;
    for(Tower turr:turrets){                        //Turret Drawing
      if(turr.shoot(bullets,monsters) && shotC>0){
        if(turr.type.equals("Basic")){
          canS[0].play();
        }
        else if(turr.type.equals("Normal")){
          canS[1].play();
        }
        else if(turr.type.equals("Good")){
          canS[2].play();
        }
        else if(turr.type.equals("Great")){
          canS[3].play();
        }
        shotC-=1;
      }
      if(turr.x<=destx && destx<=turr.x+40){
        if(turr.y<=desty && desty<=turr.y+40){
          tmpTurret=turr;
          g.drawRect(turr.x,turr.y,40,40);
          g.drawImage(turretI[turr.indy],800,120,40,40,null);
          if(turr.level<4){
            g.drawString("Level Up | "+turr.level+" -> "+(turr.level+1),800,200);
            g.drawString("Cost: "+turr.ucost*turr.level,800,225);
          }
          else{
            g.drawString("Max Level",800,200);
          }
          if(turr.type.equals("Wall")){
            g.drawRect(795,165,turr.maxhealth/20,10);
            g.fillRect(795,165,turr.health/20,10);
          }
          else{
            g.drawRect(795,165,turr.maxhealth/2,10);
            g.fillRect(795,165,turr.health/2,10);
          }        
        }
      }
      g.drawImage(turretI[turr.indy],turr.x,turr.y,40,40,null);
    }
    for(Bullet bull:bullets){
      money=bull.move(monsters,g,trashB,money);
    }
    ArrayList<Monster>endMons= new ArrayList<Monster>();
    if(monsters!=null){
    for (Monster ms: monsters){
      if(ms.x>1000){
        endMons.add(ms);
      }
      ms.monsterDraw(g,turrets);
      if(mainFrame.healthG<=0){
        mainFrame.healthG=5;
        ends=true;
        return;
      }
      if(ms.x<=destx && destx<=ms.x+50){
        if(ms.y<=desty && desty<=ms.y+50){
          ms.select=true;
          if(tmpMon!=null){
            tmpMon.select=false;
          }
          tmpMon=ms;
          destx=-1;
        }
      }
    }
    }
    if(destx!=-1 && tmpMon!=null){
      tmpMon.select=false;
      tmpMon=null;
    }

    if(tmpTurret!=null){
      if(800<destx && destx<950){
        if(180<desty && desty<205){
          for(Tower t:turrets){
            if(t==tmpTurret){
              if(money>tmpTurret.ucost*tmpTurret.level){
                money-=tmpTurret.ucost*tmpTurret.level;
                t.levelUp();
              }
            }
          }
          tmpTurret=null;
        }
      }
    }
    timer+=game.time();
    if(timer>monSpawn){
      monsters=game.loadLevel(monsters);
      timer-=monSpawn;
    }
    bullets.removeAll(trashB);
    monsters.removeAll(endMons);
    trashB.clear();
    volume=(double)((double)monsters.size()/(double)15);
    if(volume>1){
      volume=1;
    }  
    if(soundCount<=1){
    if(volume<1.5 && volume>0.75){
      monS[3].play();
    }
    else if(volume<0.75 && volume>0.5){
      monS[2].play();
    }
    else if(volume<0.5 && volume>0.25){
      monS[1].play();
    }
    else if(volume<0.25 && volume>0){
      monS[0].play();
    }
    soundCount=40;
    }
    soundCount-=1;
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
      if(620<desty && desty<660){
        if(100<destx && destx<140){turretType="Basic"; tBox=100;}
        else if(180<destx && destx<220){turretType="Normal"; tBox=180;}
        else if(260<destx && destx<300){turretType="Good"; tBox=260;}
        else if(340<destx && destx<380){turretType="Great"; tBox=340;}
        else if(420<destx && destx<460){turretType="Wall"; tBox=420;}
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
  public int indy;
  public int cooldown;
  public int max;
  public int health;
  public int maxhealth;
  public int damage;
  private int bspeed;
  public int ucost;
  public int level=1;;
  public KGame mainFrame;
  public Tower(int xx,int yy, String kind, int img, int time,KGame m){
    x=xx;
    y=yy;
    type=kind;
    indy=img;
    cooldown=time;
    max=time;
    health=100;
    if(type.equals("Basic")){
      damage=15;
      bspeed=8;
      ucost=25;
    }
    else if(type.equals("Normal")){
      damage=25;
      bspeed=10;
      ucost=40;
    }
    else if(type.equals("Good")){
      damage=2;
      bspeed=12;
      ucost=70;
    }
    else if(type.equals("Great")){
      damage=75;
      bspeed=12;
      ucost=100;
    }
    else{
      health=1000;
      damage=0;
      bspeed=0;
      ucost=50;
    }
    maxhealth=health;
    mainFrame=m;
  }
  public boolean shoot(ArrayList<Bullet> bs, ArrayList<Monster> ms){
    if(cooldown<=0){
      for(Monster mons: ms){
        if(mons.y+15==y && mons.x<=x+300 && mons.x>x){
          cooldown=max;
          if(!type.equals("Wall")){
            bs.add(new Bullet(x,y,type,bspeed,damage));
          }
          return true;
        }
      }
      return false;
    }
    else{
      cooldown-=5;
      return false;
    }
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
  public void levelUp(){
    if(level<4){
      maxhealth+=maxhealth/2;
      health=maxhealth;
      damage+=damage/2;
      level+=1;
    }
  }
}
class Bullet{
  int x;
  int y;
  String type;
  int speed;
  int damage;
  int contact=-1;
  private Image[] types=new Image[4];
  public Bullet(int xx, int yy, String ttype, int sspeed, int ddamage){
    x=xx;
    y=yy;
    type=ttype;
    speed=sspeed;
    damage=ddamage;
    for(int k=0;k<4;k++){
      types[k]=new ImageIcon("Bullet "+(k+1)+".png").getImage();
    }
  }
  public int move(ArrayList<Monster> ms,Graphics g,ArrayList<Bullet> tb,int money){
    int ind=0;
    if(type.equals("Basic")){
       ind=0;
    }
    else if(type.equals("Normal")){
       ind=1;
    }
    else if(type.equals("Good")){
       ind=2;
    }
    else{
       ind=3;
    }
    g.drawImage(types[ind],x,y+10,40,10,null);
    for(Monster mons:ms){
      if(mons.x<x+40 && y==mons.y+15){
        if(contact<0){
          contact=mons.x;
        }
          if(mons.damage(damage)){
            mons.x=10000;
            mons.speed=0;
            money+=10;
          }
          if(type.equals("Good") && contact+100<x){
            x=10000;
            speed=0;
            tb.add(this);
            break;
          }
          else{
            if(!type.equals("Good")){
            x=10000;
            speed=0;
            tb.add(this);
            break;
            }
          }
      }
    }
    x+=speed;
    if(x>700){
      x=10000;
      speed=0;
      tb.add(this);
    }
    return money;
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
        }
      }
    }
  }
}


class Level extends JPanel{
  private int destx,desty;//Variables for keeping track of the mouse's position.
  //private Image screen;
  public KGame mainFrame;
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
  public KGame mainFrame;
  public int x;
  public int y;
  public int picCount;
  public int hp;
  private int maxhp;
  public int power;
  public String type;
  public String direction;
  public double speed;
  public boolean select=false;
  public int level;
  private Image[] mPicL=new Image[3];
  private Image attack;
  private Image arrow;
  public int ax=-1;
  public Monster(int placex, int placey, String mtype,int monlevel, KGame m){
    mainFrame=m;
    x=placex;
    y=placey;
    level=monlevel;
    picCount=0;
    type=mtype;
    if(type.equals("werewolf") | type.equals("vampire")){
      hp=150;
    }
    else if(type.equals("devil")){
      hp=300;
    }
    else{
      hp=100;
    }
    direction="L";
    if(type.equals("spider")){
      speed=3;
    }
    else if(type.equals("werewolf") | type.equals("vampire")){
      speed=2;
    }
    else{
      speed=1;
    }
    for(int i=1; i<4; i++){
      mPicL[i-1]=new ImageIcon(type+i+".png").getImage();
//      mPicR[i-1]=new ImageIcon(type+i+"R.png").getImage();
    }
    if(type.equals("spider") | type.equals("devil")){
       power=10;
    }
    else if(type=="werewolf"){
      power=25;
    }
    else{
      power=5;
    }
    hp=hp*level;
    maxhp=hp;
    power=power*level;
    if(type.equals("skeleton")){
      attack=new ImageIcon("skeleshoot.png").getImage();
      arrow=new ImageIcon("arrow.png").getImage();
    }
  }
  public void floorUp(){
/*    if(695<x && x<705){
      if(y==145 | y==275 | y==405){
        y+=130;
        x=70;       
        picCount=0;
        direction="R";
      }
    } */
    if(55<x && x<65){
      if(y==275 | y==405 | y==535){
        y-=130;
        x=690;
        picCount=0;
        direction="L";
      }
    }
    if(x<65 && y==145){
      speed=0;
      x=10000;
      mainFrame.healthG-=1;
    }
  }
  public Image[] mPic(){
    return mPicL;
  }
  public void monsterDraw(Graphics g,ArrayList<Tower> turrets){
    floorUp();
    boolean overlay=false;
    boolean shooting=false;
    if(type.equals("skeleton")){
      for(Tower t:turrets){
        if(t.x-40<x && x<t.x+100 && y+15==t.y){
          overlay=true;
          shooting=true;
          g.drawImage(attack,x,y,40,60,null);
          if(ax==-1){
            ax=x;
          }
          g.drawImage(arrow,ax,y+20,10,15,null);
          ax-=5;
          if(ax<t.x && y+15==t.y){
            ax=-1;
            if(t.damage(power)){
              turrets.remove(t);
            }
          }
          break;
        }
      }
    }
    else{
      for(Tower t:turrets){
        if(t.x-40<x && x<t.x+40 && y+15==t.y){
          overlay=true;
          if(t.damage(power)){
            turrets.remove(t);
          }
          x+=5;          
          break;
        }
      }
    }
    if(type.equals("spider")){
      g.drawImage(mPic()[picCount/5],x,y+10,50,40,null);
      if(select){
        g.drawRect(x,y+10,50,40);
      }
    }
    else if(type.equals("vampire")){
      g.drawImage(mPic()[picCount/5],x,y,50,40,null);
      if(select){
        g.drawRect(x,y,50,40);
      }
    }
    else{
      if(!shooting){
        g.drawImage(mPic()[picCount/5],x,y,40,50,null);
      }
      if(select){
        g.drawRect(x,y,40,50);
      }
    }
    if(select){
      g.drawImage(mPic()[picCount/5],800,120,40,40,null);
      int barNum=(maxhp/2)/150+1;
      for(int i=0; i<barNum; i++){
        if(i+1==barNum){
          g.drawRect(795,185+(20*i),(maxhp/2)%150,10);
        }
        else{
          g.drawRect(795,185+(20*i),150,10);
        }
      }
      barNum=(hp/2)/150+1;
      for(int i=0; i<barNum; i++){
        if(i+1==barNum){
          g.fillRect(795,185+(20*i),(hp/2)%150,10);
        }
        else{
          g.fillRect(795,185+(20*i),150,10);
        }
      }
      g.drawString("Level "+level,795,175);
    }
    picCount+=1;
    if(picCount/5==3){
      picCount=0;
    }
    if(!overlay){
      x-=speed;
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
class Sound
{
  File wavFile;
  AudioClip sound;
  public Sound(String name)
  {
    wavFile = new File(name);
    try{sound = Applet.newAudioClip(wavFile.toURL());}
    catch(Exception e){e.printStackTrace();}
  }
  public void play(){
    sound.play();
  }
}
class GameMaker{
  public KGame mainFrame;
  public String[] spawn;
  public int len;
  public int spot=0;
  public int waitTime=1;
  public int monsterLvl=1;
  public int wave=1;
  public GameMaker(String[] level, KGame game){
    spawn=level;
    mainFrame=game;
    len=spawn.length;
  }
  public ArrayList<Monster> loadLevel(ArrayList<Monster> monsters){
    if(spot>=len){
      spot=-1;
      return monsters;
    }
    if(spot>=0){
    if(spawn[spot].equals("z")){
      monsters.add(new Monster(800,535,"zombie",monsterLvl,mainFrame));
    }
    else if(spawn[spot].equals("s")){
      monsters.add(new Monster(800,535,"spider",monsterLvl,mainFrame));
    }
    else if(spawn[spot].equals("k")){
      monsters.add(new Monster(800,535,"skeleton",monsterLvl,mainFrame));
    }
    else if(spawn[spot].equals("w")){
      monsters.add(new Monster(800,535,"werewolf",monsterLvl,mainFrame));
    }
    else if(spawn[spot].equals("v")){
      monsters.add(new Monster(800,405,"vampire",monsterLvl,mainFrame));
    }
    else if(spawn[spot].equals("d")){
      monsters.add(new Monster(800,535,"devil",monsterLvl,mainFrame));
    }
    else{
      int num=Integer.parseInt(spawn[spot]);
      if(num>0){
        monsterLvl=num;
      }
      else{
        waitTime=num;
      }
    }
    spot+=1;
    }
    return monsters;
  }
  public int time(){
    if(waitTime<1){
      int tmp=waitTime;
      waitTime=1;
      wave+=1;
      return tmp;
    }
    return waitTime;
  } 
}
