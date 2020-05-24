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
import java.lang.Math;
import javax.swing.Timer;//Specifying which Timer since there would be a conflict with util otherwise.
public class KGame extends JFrame{//Main, public class.
  public String kind="Menu";
  public boolean change=false;
  Timer myTimer;//Timer to keep the graphics at a good pace.  
  GamePanel game;
  Menu menu;
  Level level;
  public String[] gameLevel={""};
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
    startMidi("title.mid",-1);
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
        startMidi("game.mid",-1);
      }
      if(change==true && kind.equals("Menu")){
        change=false;
        remove(game);
        add(menu);
        midiPlayer.stop();
        startMidi("title.mid",-1);
      }
      if(change==true && kind.equals("Level")){
        change=false;
        remove(menu);
        remove(game);
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
  private int money=1000;
  private int destx, desty, selx, sely;//Variables for keeping track of the mouse's position.
  private KGame mainFrame;
  public boolean ready=false;
  private boolean [] keys;
  public Image[] kantMoves=new Image[13];
  public Image[] turretI=new Image[8];
  public Image[] coinsI=new Image[6];
  public Image tile1;
  public Image tile2;
  public Image shovel;
  public int[] costs={25,50,100,175,70,100,10};
  public ArrayList<Integer> chosenT=new ArrayList<Integer>();
  public ArrayList<Tower> turrets=new ArrayList<Tower>();
  public ArrayList<Monster> monsters=new ArrayList<Monster>();
  public ArrayList<Bullet> bullets=new ArrayList<Bullet>();
  public ArrayList<Bullet> trashB=new ArrayList<Bullet>();
  public ArrayList<Coin> coins=new ArrayList<Coin>();
  public ArrayList<Coin> trashC=new ArrayList<Coin>();
  public boolean beginPlay=false;
  public String turretType = "Basic";
  public int tBox=100;
  private Image background;
  private Image castle;
  private Image fence;
  private Image siding;
  private Kant kant=new Kant(120,120);
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
  public boolean isDown=false;
  public String mouseSelect="None";
  public int mouseIndex=-1;
  public boolean placeTurr=false;
  public int backx=-200;
  public String[] typesT={"Basic","Normal","Good","Great","Wall","Cannon","Sun"};
  public ArrayList<Samurai>sams= new ArrayList<Samurai>();
  public Image[]samwalk=new Image[6];
  public Image[]samwalkb=new Image[6];
  public Image[]samatk=new Image[9];
  public GamePanel(KGame m){     //Constructor.
    mainFrame=m; 
    keys = new boolean[KeyEvent.KEY_LAST+1];
    addMouseListener(new clickListener());
    addMouseMotionListener(new Mouse());
    destx=-20; desty=-20; selx=-20; sely=-20;    //Default values for the mouse's position is off screen.
    setSize(1000,700);
    addKeyListener(this);
    tmpMon=null;
    tmpTurret=null;
    for(int k=0;k<13;k++){
      kantMoves[k]=new ImageIcon("Kant "+(k+1)+".png").getImage();
    }
    for(int k=0;k<4;k++){
      turretI[k]=new ImageIcon("turret "+(k+1)+".png").getImage();
      canS[k]=new Sound("cannon"+(k+1)+".wav");
      monS[k]=new Sound("monsterS"+(k+1)+".wav");
    }
    for(int k=0;k<6;k++){
      coinsI[k]=new ImageIcon("coin"+(k+1)+".png").getImage();
    }
    for(int k=0; k<6; k++){
      samwalk[k]=new ImageIcon("samwalk"+(k+1)+".png").getImage();
      samwalkb[k]=new ImageIcon("samwalk"+(k+1)+"b.png").getImage();
    }
    for(int k=0; k<9; k++){
      samatk[k]=new ImageIcon("samatk"+(k+1)+".png").getImage();
    }
    turretI[4]=new ImageIcon("barricade.png").getImage();
    turretI[5]=new ImageIcon("cannon.png").getImage();
    turretI[6]=new ImageIcon("sun.png").getImage();
    turretI[7]=samatk[0];
    background=new ImageIcon("plains.png").getImage();
    castle=new ImageIcon("castle.png").getImage();
    tile1=new ImageIcon("tile1.png").getImage();
    tile2=new ImageIcon("tile2.png").getImage();
    fence=new ImageIcon("fence.png").getImage();
    siding=new ImageIcon("siding.png").getImage();
    shovel=new ImageIcon("shovel.jpg").getImage();
    lost=new ImageIcon("loser.png").getImage();
    won=new ImageIcon("winner.png").getImage();
    String[]gameMons=mainFrame.gameLevel;
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
      picInd=kant.move(kant.x,kant.y-walkSpeed,picInd);
    }
    else if (keys[KeyEvent.VK_A]){
      picInd=kant.move(kant.x-walkSpeed,kant.y,picInd);
    }
    else if (keys[KeyEvent.VK_S]){
      picInd=kant.move(kant.x,kant.y+walkSpeed,picInd);
    }
    else if (keys[KeyEvent.VK_D]){
      picInd=kant.move(kant.x+walkSpeed,kant.y,picInd);
    }
    else{
      picInd=kant.move(kant.x,kant.y,picInd);
    }
    if(placeTurr && destx>100 && destx<900 && desty>100 && desty<600){
      boolean overlay=false;
      for(Tower t:turrets){
        if(t.x-30<destx && destx<t.x+70 && t.y-30<desty && desty<t.y+70){
          overlay=true;
          break;
        }
      }
      if(!overlay){
        int temppx=destx;
        int temppy=desty;
        temppy=temppy/100;
        temppy=temppy*100;
        temppx=temppx/100;
        temppx=temppx*100;
        int indy=0;
        if(turretType.equals("Samurai")){
          if(money-65>0){
            sams.add(new Samurai(temppx,temppy));
            money-=65;
          }
        }
        else{
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
          else if(turretType.equals("Wall")){
            indy=4;
          }
          else if(turretType.equals("Cannon")){
            indy=5;
          }
          else if(turretType.equals("Sun")){
            indy=6;
          }         
          if (money-costs[indy]>=0){
            money-=costs[indy];
            turrets.add(new Tower(temppx+30,temppy+30,turretType,indy,mainFrame));          
          }
        }
      }
      placeTurr=false;
    }
    else{
      placeTurr=false;
    }
    if (keys[KeyEvent.VK_SPACE]){
      /*
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
        else if(turretType.equals("Wall")){
          indy=4;
        }
        else if(turretType.equals("Cannon")){
          indy=5;
        }
        else if(turretType.equals("Sun")){
          indy=6;
        }
        if (money-costs[indy]>=0){
          money-=costs[indy];
          turrets.add(new Tower(kant.x,kant.y+30,turretType,indy,mainFrame));
        }
      }
      */
    }
  }
  public void paintComponent(Graphics g){      //Method for actually drawing all the needed graphics onto the screen.
    if(beginPlay){
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
      g.drawImage(castle,backx,100,100,500,null);
      g.drawImage(shovel,900,0,40,40,null);
      int countT=0;
      for(int xxx=backx+100;xxx<backx+900;xxx+=100){
        for(int yyy=100;yyy<600;yyy+=100){
          if(countT%2==0){
            g.drawImage(tile1,xxx,yyy,100,100,null);
          }
          else{
            g.drawImage(tile2,xxx,yyy,100,100,null);
          }
          countT++;
        }
      }
      g.drawImage(fence,backx+100,0,800,100,null);
      g.drawImage(siding,backx+0,0,100,100,null);
      g.drawImage(kantMoves[picInd-1],kant.x,kant.y,40,40,null);
      if(kant.attack){
        g.drawImage(kantMoves[12],kant.x-10,kant.y-10,65,65,null);       
      }
      if(backx<0){
        backx+=5;
        return;
      }
      Font font = new Font("Verdana", Font.BOLD, 17);
      if(game.spot==-1 && monsters.size()==0){
        mainFrame.healthG=5;
        ends=true;
        didWon=true;
        return;
      }
      g.setFont(font);//Creating the font and setting its colour.
      g.setColor(Color.black);
      g.drawString("Coins: "+money,50,25);
      g.drawString("Wave "+game.wave,50,50);
      g.drawRect(tBox,620,40,40);
      if(isDown && mouseIndex>-1){
        if(mouseSelect.equals("Remove")){
          g.drawRect(destx-20,desty-20,40,40);
        }
        else{
          g.drawImage(turretI[mouseIndex],destx-20,desty-20,40,40,null);
        }
      }
      for(int i=0; i<chosenT.size(); i++){
        g.drawImage(turretI[chosenT.get(i).intValue()],100+(i*80),620,40,40,null);
      }
      int shotC=1;
      for(Tower turr:turrets){                        //Turret Drawing
        if(turr.shoot(bullets,monsters,coins) && shotC>0){
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
        if(turr.x<=selx && selx<=turr.x+40){
          if(turr.y<=sely && sely<=turr.y+40){
            tmpTurret=turr;
            g.drawRect(turr.x,turr.y,40,40);
            g.drawImage(turretI[turr.indy],200,10,40,40,null);
            if(turr.level<4){
              g.drawString("Level Up | "+turr.level+" -> "+(turr.level+1),200,60);
              g.drawString("Cost: "+turr.ucost*turr.level,200,80);
            }
            else{
              g.drawString("Max Level",200,60);
            }
            if(turr.type.equals("Wall")){
              g.drawRect(250,25,turr.maxhealth/24,10);
              g.fillRect(250,25,turr.health/24,10);
            }
            else if(turr.type.equals("Cannon")){
              g.drawRect(250,25,turr.maxhealth/4,10);
              g.fillRect(250,25,turr.health/4,10);
            }
            else{
              g.drawRect(250,25,turr.maxhealth/2,10);
              g.fillRect(250,25,turr.health/2,10);
            }        
          }
        }
        g.drawImage(turretI[turr.indy],turr.x,turr.y,40,40,null);
      }
      for(Bullet bull:bullets){
        money=bull.move(monsters,g,trashB,money);
      }
      for(Samurai sam:sams){
        if(sam.select){
          if(200<selx && selx<350){
            if(50<sely && sely<75){
              if(money-(30+(sam.level*70))>0){
                money-=30+(sam.level*70);
                sam.levelUp();
              }
            }
          }
        }
        sam.drawSam(g,monsters,samwalk,samwalkb,samatk, selx, sely);
      }
      for(Coin cc:coins){
        cc.draw(g,coinsI);
      }
      ArrayList<Monster>endMons= new ArrayList<Monster>();
      Monster wid=null;
      if(monsters!=null){
        for (Monster ms: monsters){
          if(ms.x>1000){
            if(ms.type.equals("widow")){
              wid=ms;
            }
            endMons.add(ms);
          }
          ms.monsterDraw(g,turrets,sams);
          if(mainFrame.healthG<=0){
            mainFrame.healthG=5;
            ends=true;
            return;
          }
          if(ms.x<=selx && selx<=ms.x+50){
            if(ms.y<=sely && sely<=ms.y+50){
              ms.select=true;
              if(tmpMon!=null){
                tmpMon.select=false;
              }
              tmpMon=ms;
              selx=-1;
            }
          }
          if(kant.attack && !ms.type.equals("ghost")){
            if(ms.x<kant.x+70 && kant.x-70<ms.x){
              if(ms.y<kant.y+40 && kant.y-40<ms.y){
                if(ms.damage(1,"Kant")){
                  endMons.add(ms);
                }
              }
            }
          }
        }
        if(wid!=null){
          monsters.add(new Monster(wid.x-1030,wid.y,"spider",wid.level,mainFrame));
          monsters.add(new Monster(wid.x-1050,wid.y,"spider",wid.level,mainFrame));
        }
      }
      if(selx!=-1 && tmpMon!=null){
        tmpMon.select=false;
        tmpMon=null;
      }
      if(tmpTurret!=null){
        if(200<selx && selx<350){
          if(50<sely && sely<75){
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
      coins.removeAll(trashC);
      trashB.clear();
      trashC.clear();
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
    else{
      if(chosenT.size()==3){
        beginPlay=true;
      }
      for(Integer num:chosenT){
        int tempx=num.intValue()*50+110;
        int tempy=490;
        g.setColor(Color.white);
        g.drawRect(tempx,tempy,40,40);
      }
      for(int k=0;k<8;k++){
        g.drawImage(turretI[k],k*50+110,490,40,40,null);
        if(isDown && destx<k*50+150 && destx>k*50+110 && desty<530 && desty>490){
          if(!chosenT.contains(k)){
            chosenT.add(k);
          }
          isDown=false;
        }
      }
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
  class Mouse implements MouseMotionListener {
    public void mouseDragged(MouseEvent e) 
    { 
      destx=e.getX();
      desty=e.getY();
    }
    public void mouseMoved(MouseEvent e) 
    { 
      destx=e.getX();
      desty=e.getY();
    }
  }
  class clickListener implements MouseListener{//Class for checking for the user's mouse inputs.
    public void mouseEntered(MouseEvent e) {
    }//The following methods all check for some aspect of the user's mouse input. Names and purposes are self-explanatory.
    public void mouseExited(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
      if(mouseIndex>-1 && !mouseSelect.equals("Remove")){
        placeTurr=true;
      }
      else{
        if(mouseSelect.equals("Remove")){
          for(Tower t:turrets){
            if(t.x-50<destx && t.x+50>destx && Math.abs(t.y-desty)<30){
              turrets.remove(t);
              money+=(costs[t.indy]+(t.ucost*t.level))/3;
              break;
            }
          }
        }
      }
      isDown=false;
      mouseSelect="None";
      mouseIndex=-1;
    }    
    public void mouseClicked(MouseEvent e){
    }  
    
    public void mousePressed(MouseEvent e){//Method for getting the coordinates of the mouse.
      isDown=true;
      selx=e.getX();
      sely=e.getY();
      for(Coin cc:coins){
        if(selx>cc.x && selx<cc.x+20 && sely>cc.y && sely<cc.y+20){
          money+=cc.val;
          trashC.add(cc);
        }
      }
      if(sely<40 && selx>900 && selx<940){
        mouseSelect="Remove";
        mouseIndex=0;
      }
      else if(620<sely && sely<660){
        for(int k=0;k<chosenT.size();k++){
          int tempx=180+k*80;
          if(selx<tempx && selx+80>tempx){
            if(chosenT.get(k)==7){
              turretType="Samurai";
              mouseSelect="Samurai";
            }
            else{
              turretType=typesT[chosenT.get(k).intValue()];
              mouseSelect=turretType;
            }
            tBox=tempx-80;
            mouseIndex=chosenT.get(k).intValue();
          }
        }
      }
    }
  }    
}


class Kant{
  public int x;
  public int y;
  public int picCount;
  public boolean attack=false;
  public Kant(int placex,int placey){
    x=placex;
    y=placey;
    picCount=0;
  }
  public int move(int mx,int my,int ind){
    attack=false;
    if(mx>x){
      if(mx<850){
        x=mx;
      }
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
      if(mx>100){
        x=mx;
      }
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
      if(my<550){
        y=my;
      }
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
      if(my>100){
        y=my;
      }
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
      ind=13;
      attack=true;
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
  public Tower(int xx,int yy, String kind, int img, KGame m){
    x=xx;
    y=yy;
    type=kind;
    indy=img;
    health=100;
    if(type.equals("Basic")){
      damage=15;
      bspeed=8;
      ucost=25;
    }
    else if(type.equals("Normal")){
      damage=25;
      bspeed=9;
      ucost=40;
    }
    else if(type.equals("Good")){
      damage=2;
      bspeed=10;
      ucost=70;
    }
    else if(type.equals("Great")){
      damage=75;
      bspeed=11;
      ucost=100;
    }
    else if(type.equals("Wall")){
      health=1200;
      damage=0;
      bspeed=0;
      ucost=50;
    }
    else if(type.equals("Cannon")){
      health=200;
      damage=100;
      bspeed=4;
      ucost=100;
    }
    else if(type.equals("Sun")){
      health=120;
      bspeed=0;
    }
    cooldown=(30-(bspeed*2))*10;
    max=cooldown;
    maxhealth=health;
    mainFrame=m;
  }
  public boolean shoot(ArrayList<Bullet> bs, ArrayList<Monster> ms,ArrayList<Coin> cs){
    if(cooldown<=0){
      if(type.equals("Sun")){
        cs.add(new Coin(x,y,10));
        cooldown=max;
        return false;
      }
      for(Monster mons: ms){
        if((Math.abs(y-mons.y)<30) && mons.x<=x+300 && mons.x>x){
          cooldown=max;
          if(!type.equals("Wall") && !type.equals("Sun")){
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
  private Image[] types=new Image[5];
  public Bullet(int xx, int yy, String ttype, int sspeed, int ddamage){
    x=xx;
    y=yy;
    type=ttype;
    speed=sspeed;
    damage=ddamage;
    for(int k=0;k<5;k++){
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
    else if(type.equals("Great")){
      ind=3;
    }
    else{
      ind=4;
    }
    g.drawImage(types[ind],x,y+10,40,10,null);
    for(Monster mons:ms){
      if(mons.x<x+40 && Math.abs(mons.y-y)<30){
        if(contact<0){
          contact=mons.x;
        }
        if(mons.damage(damage,type)){
          mons.x+=1000;
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
  private Image rulePic;
  private KGame mainFrame;
  private boolean rules=false;
  public boolean ready=false;
  public Menu(KGame m){
    mainFrame=m;
    addMouseListener(new clickListener());
    destx=-20;//Default values for the mouse's position is off screen.
    desty=-20;
    setSize(800,600);
    screen=new ImageIcon("KantScreen.jpg").getImage();
    rulePic=new ImageIcon("rules.png").getImage();
  }
  public void addNotify() {       //Method for notifying, seeing if the graphics are ready.
    super.addNotify();
    requestFocus();
    ready = true;
  }
  
  public void paintComponent(Graphics g){      //Method for actually drawing all the needed graphics onto the screen.
    g.drawImage(screen,0,0,1000,700,null);
    if(rules){
      g.drawImage(rulePic,0,0,995,665,null);
    }
  }
  
  class clickListener implements MouseListener{//Class for checking for the user's mouse inputs.
    public void mouseEntered(MouseEvent e) {
    }//The following methods all check for some aspect of the user's mouse input. Names and purposes are self-explanatory.
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}    
    public void mouseClicked(MouseEvent e){}  
    
    public void mousePressed(MouseEvent e){//Method for getting the coordinates of the mouse.
      destx = e.getX();
      desty = e.getY();
      if(410<destx && destx<610 && !rules){
        if(210<desty && desty<350){
          mainFrame.kind="Level";
          mainFrame.change=true;
        }
      }
      if(410<destx && destx<610){
        if(370<desty && desty<510){
          rules=true;
        }
      }
      if(rules){
        if(0<destx && destx<100){
          if(0<desty && desty<80){
            rules=false;
          }
        }
      }
    }
  }
}


class Level extends JPanel{
  public String[] level1={"g","z","z","z","z","z","z","z","z","n","-500","z","z","z","z","z","z","n","k","k","z","z","n","k","k","z","z","s","w","v","s2","w","v","-500","z","z","z","d","k","k","n","w","s","v","s","z","z","d","k","k","w","s","v"};
  public String[] level2={"z","z","z","z","z","s","s","z","z","w","w","z","z","s","s","-500","z","z","z","n","k","k","w","s","s","2","z","z","z","n","v","v","w","z","z","z","z","w","s","s2","v","-500","z","z","z","z","n","w","3","d","k","k","w","w"};
  public String[] level3={"n","v","v","v","z","z","z","z","s","s","z","z","k","w","k","w","z","z","z","z","v","v","-500","n","z","z","d","k","k","k","w","w","w","s","s2","v","v","v","v","v","z","z","d","k","k","k","w","w","w","s","s","v","v","v","v","v","-500","2","z","z","d","k","k","k","w","w","w","s","s2","v","v","v","v","s","s","d","z","z","d","k","k","k","w","w","w","s","s","v","v","v","v","v"};
  public String[] level4={"z","z","z","z","z","z","w","w","v","v","w","w","w","v","v","z","z","z","v","v","w","w","-500","2","n","z","z","z","w","w","w","w","s","s","w","w","v","v","s","w","w","v","v","s2","-500","3","n","n","d","d","k","k","k","w","w","w","s","s","w","v","v","v","s2"};
  private int destx,desty;//Variables for keeping track of the mouse's position.
  public KGame mainFrame;
  public boolean ready=false;
  private Image levelSelect;
  public Level(KGame m){
    mainFrame=m;
    addMouseListener(new clickListener());
    destx=-20;//Default values for the mouse's position is off screen.
    desty=-20;
    setSize(1000,700);
    levelSelect=new ImageIcon("LevelSelect.png").getImage();
  }
  public void addNotify() {       //Method for notifying, seeing if the graphics are ready.
    super.addNotify();
    requestFocus();
    ready = true;
  }
  
  public void paintComponent(Graphics g){      //Method for actually drawing all the needed graphics onto the screen.
    g.drawImage(levelSelect,0,0,1000,700,null);
  }
  
  class clickListener implements MouseListener{//Class for checking for the user's mouse inputs.
    public void mouseEntered(MouseEvent e) {}//The following methods all check for some aspect of the user's mouse input. Names and purposes are self-explanatory.
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}    
    public void mouseClicked(MouseEvent e){}  
    
    public void mousePressed(MouseEvent e){//Method for getting the coordinates of the mouse.
      destx = e.getX();
      desty = e.getY();
      if(95<desty && desty<315){
        if(110<destx && destx<195){
          mainFrame.gameLevel=level1;
        }
        else if(195<destx && destx<280){
          mainFrame.gameLevel=level2;
        }
        else if(280<destx && destx<365){
          mainFrame.gameLevel=level3;
        }
        else if(365<destx && destx<450){
          mainFrame.gameLevel=level4;
        }
        if(110<destx && destx<450){
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
  private Image[] mPicL;
  private Image attack;
  private Image arrow;
  public int ax=-1;
  private int listLen;
  public int pushx=0;
  public int pushy=0;
  public Monster(int placex, int placey, String mtype,int monlevel, KGame m){
    mainFrame=m;
    x=placex;
    y=placey;
    level=monlevel;
    picCount=0;
    type=mtype;    
    if(type.equals("werewolf")){
      listLen=10;
      mPicL=new Image[listLen];
    }
    else if(type.equals("demon") | type.equals("zombie")){
      listLen=15;
      mPicL=new Image[listLen];
    }
    else if(type.equals("nymph")){
      listLen=14;
      mPicL=new Image[listLen];
    }
    else if(type.equals("spider") | type.equals("widow")){
      listLen=5;
      mPicL=new Image[listLen];
    }
    else if(type.equals("skeleton")){
      listLen=13;
      mPicL=new Image[listLen];
    }
    else if(type.equals("vampire") | type.equals("ghost")){
      listLen=4;
      mPicL=new Image[listLen];
    }
    else{
      listLen=3;
      mPicL=new Image[listLen];
    }
    
    
    if(type.equals("nymph")){
      hp=150;
    }
    else if(type.equals("demon")){
      hp=300;
    }
    else if(type.equals("werewolf")){
      hp=200;
    }
    else if(type.equals("widow")){
      hp=50;
    }
    else{
      hp=100;
    }
    direction="L";
    if(type.equals("widow")){
      speed=4;
    }
    else if(type.equals("spider")){
      speed=3;
    }
    else if(type.equals("werewolf") | type.equals("vampire")){
      speed=2;
    }
    else{
      speed=1;
    }
    for(int i=1; i<listLen+1; i++){
      mPicL[i-1]=new ImageIcon(type+i+".png").getImage();
//      mPicR[i-1]=new ImageIcon(type+i+"R.png").getImage();
    }
    if(type.equals("spider") | type.equals("demon")){
      power=7;
    }
    else if(type=="werewolf"){
      power=14;
    }
    else{
      power=5;
    }
    hp=hp*level;
    maxhp=hp;
    power+=level*3;
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
    /*if(55<x && x<65){
      if(y>300){
        y-=100;
        x=690;
        picCount=0;
        direction="L";
      }
    }
    */
    if(x<10){
      speed=0;
      x=10000;
      mainFrame.healthG=0;
    }
  }
  public Image[] mPic(){
    return mPicL;
  }
  public void monsterDraw(Graphics g,ArrayList<Tower> turrets, ArrayList<Samurai>sams){
    floorUp();
    boolean overlay=false;
    boolean shooting=false;
    if(type.equals("skeleton")){
      for(Tower t:turrets){
        if(t.x-40<x && x<t.x+100 && Math.abs(y-t.y)<30){
          overlay=true;
          shooting=true;
          g.drawImage(attack,x,y,40,60,null);
          if(ax==-1){
            ax=x;
          }
          g.drawImage(arrow,ax,y+20,10,15,null);
          ax-=5;
          if(ax<t.x && Math.abs(t.y-y)<30){
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
        if(t.x-40<x && x<t.x+40 && Math.abs(t.y-y)<30 && !type.equals("ghost")){
          overlay=true;
          if(t.damage(power)){
            turrets.remove(t);
          }
          x+=5;          
          break;
        }
      }
    }
    ArrayList<Samurai>tmpsams=new ArrayList<Samurai>();
    for(Samurai s:sams){
      if(s.y==y && s.x+50>x && x>s.x && !type.equals("ghost")){
        if(s.damage(power)){
          tmpsams.add(s);
        }
        overlay=true;
      }
    }
    sams.removeAll(tmpsams);
    if(type.equals("spider") | type.equals("widow")){
      g.drawImage(mPic()[picCount/5],x,y+10,55,35,null);
      if(select){
        g.drawRect(x,y+10,50,40);
      }
    }
    else if(type.equals("vampire") | type.equals("werewolf")){
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
      g.drawImage(mPic()[picCount/5],200,20,40,40,null);
      int barNum=(maxhp/2)/300+1;
      for(int i=0; i<barNum; i++){
        if(i+1==barNum){
          g.drawRect(250,20+(20*i),(maxhp/2)%300,10);
        }
        else{
          g.drawRect(250,20+(20*i),300,10);
        }
      }
      barNum=(hp/2)/300+1;
      for(int i=0; i<barNum; i++){
        if(i+1==barNum){
          g.fillRect(250,20+(20*i),(hp/2)%300,10);
        }
        else{
          g.fillRect(250,20+(20*i),300,10);
        }
      }
      g.drawString("Level "+level,180,80);
    }
    picCount+=1;
    if(picCount/5==listLen){
      picCount=0;
    }
    if(!overlay){
      if(pushx<=0){
        x-=speed;
      }
      else{
        x+=3;
        pushx-=3;
      }
    }
  }
  public boolean damage(int hurt,String typeT){
    if(type.equals("vampire")){
      if(!typeT.equals("Basic") && !typeT.equals("Normal")){
        hp-=hurt;
      }
    }
    else{
      hp-=hurt;
    }    
    if (hp<=0){
      return true;
    }
    else{
      if(typeT.equals("Great") && (x+10)<=700){
        pushx=10;
        pushy=10;
      }
      return false;
    }
  }
}
class Sound{
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
  public int monsRow=100;
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
        monsters.add(new Monster(800,monsRow+20,"zombie",monsterLvl,mainFrame));
      }
      else if(spawn[spot].equals("s")){
        monsters.add(new Monster(800,monsRow+20,"spider",monsterLvl,mainFrame));
      }
      else if(spawn[spot].equals("s2")){
        monsters.add(new Monster(800,monsRow+20,"widow",monsterLvl,mainFrame));
      }
      else if(spawn[spot].equals("n")){
        monsters.add(new Monster(800,monsRow+20,"nymph",monsterLvl,mainFrame));
      }
      else if(spawn[spot].equals("k")){
        monsters.add(new Monster(800,monsRow+20,"skeleton",monsterLvl,mainFrame));
      }
      else if(spawn[spot].equals("w")){
        monsters.add(new Monster(800,monsRow+20,"werewolf",monsterLvl,mainFrame));
      }
      else if(spawn[spot].equals("v")){
        monsters.add(new Monster(800,monsRow+20,"vampire",monsterLvl,mainFrame));
      }
      else if(spawn[spot].equals("d")){
        monsters.add(new Monster(800,monsRow+20,"demon",monsterLvl,mainFrame));
      }
      else if(spawn[spot].equals("g")){
        monsters.add(new Monster(800,monsRow+20,"ghost",monsterLvl,mainFrame));
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
      monsRow+=randint(0,4)*100;
      if(monsRow>=600){
        monsRow=100+randint(0,4)*100;
      }
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
  public static int randint(int low, int high){   // Gets a random integer between a set range
    return (int)(Math.random()*(high-low+1)+low);
  }
}
class Coin{
  int x;
  int y;
  int val;
  int indy=0;
  public Coin(int sx,int sy, int vall){
    x=(int)(Math.random()*(sx+20-sx+20+1)+sx-20);
    y=(int)(Math.random()*(sy+20-sy+20+1)+sy-20);
    val=vall;
  }
  public void draw(Graphics g,Image[] i){
    g.drawImage(i[indy],x,y,20,20,null);
    indy+=1;
    if(indy==6){
      indy=0;
    }
  }
}
class Samurai{
  public int x;
  public int y;
  public int health;
  public int maxhealth;
  public int damage;
  public int level=1;;
  public String action="r";
  public int spot=0;
  public int attack=0;
  public boolean select=false;
  public int tmppt=0;
  public Samurai(int sx, int sy){
    x=sx; y=sy+20;
    health=1000;
    maxhealth=health;
    damage=3;
  }
  public void drawSam(Graphics g, ArrayList<Monster> monsters, Image[]samwalk, Image[]samwalkb, Image[]samatk, int selx, int sely){
    String tmpA=action;
    ArrayList<Monster>tmpmons=new ArrayList<Monster>();
    for(Monster m:monsters){
      if(m.y==y && m.x<x+100 && m.x>x){
        action="a";
        g.drawImage(samatk[attack/4],x,y,50,50,null);
        if(!m.type.equals("ghost")){
          if(m.damage(2,"Samurai")){
            tmpmons.add(m);
          }
        }
        attack+=1;
        if(attack==36){
          attack=0;
        }
      }
    }
    monsters.removeAll(tmpmons);
    if(action.equals("r")){
      g.drawImage(samwalk[spot/4],x,y,50,50,null);
      x+=1;
    }
    else if(action.equals("l")){
      g.drawImage(samwalkb[spot/4],x,y,50,50,null);
      x-=1;
    }
    spot+=1;
    if(spot==24){
      spot=0;
    }
    if(x>800){
      action="l";
    }
    if(x<100){
      action="r";
    }
    if(action.equals("a")){
      action=tmpA;
    }
    if(select){
      if(tmppt==selx*sely){
        g.drawRect(x,y,50,50);
        g.drawImage(samatk[0],200,10,40,40,null);
        if(level<4){
          g.drawString("Level Up | "+level+" -> "+(level+1),200,60);
          g.drawString("Cost: "+(level*70),200,80);
        }
        else{
          g.drawString("Max Level",200,60);
        }
        g.drawRect(250,25,maxhealth/16,10);
        g.fillRect(250,25,health/16,10);
      }
      else{
        select=false;
      }
    }
    if(x<selx && selx<x+50 && y<sely && sely<y+50){
      select=true;
      tmppt=selx*sely;
    }
  }
  public boolean damage(int hurt){
    health-=hurt;
    if(health<=0){
      return true;
    }
    return false;
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
