//Adam Gaisinsky and Yang Li. FSE. Kant's Kastle, a tower-defence style game with a unique twist.
// Plays similarly to PvZ and has 10 levels and an endless mode.
// There are 11 unique weapons and 9 different types of enemies.
// Drag, drop, control, and upgrade your weapons to keep the monsters away!

import java.util.*;       //Importing for graphics and other helpful additions.
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;  
import javax.imageio.*; 
import javax.sound.midi.*;
import java.applet.*;
import java.lang.Math;
import javax.swing.Timer;          //Specifying which Timer since there would be a conflict with util otherwise.
public class KGame extends JFrame{ //Main, public class.
  public String kind="Menu";       //String that determines which GamePanel should be on the JFrqme.
  public boolean change=false;     //Variable that becomes true when there should be a GamePanel change.
  Timer myTimer;                   //Timer to keep the graphics at a good pace.  
  GamePanel game;                  //This and the two below are the 3 different GamePanels that are added.
  Menu menu;
  Level level;
  public boolean[] awards=new boolean[3];//These two arrays are used to keep track of whether or not the player has earned any of the three possible achievements.
  public Image[] trophies=new Image[3];
  public String[] lines=new String[3];//The three possible achievement images will be stored here.
  public String[] descriptions={"You're on your way!\nCompleted the first level.","Master of defence!\nFinished the last level.","Just the beginning.\nAttempted the endless mode."};//Achievement descriptions.
  public String[] gameLevel={""};  //Will hold Strings that represent which enemy types will be spawned in, or what the level and delay between enemies should be.
  public int stage;
  public boolean endless=false;
  private static Sequencer midiPlayer;
  public int healthG=5;            //Once reaches zero, the level of the game that the player is on will end, they will lose.
  public KGame() {           //Constructor.
    super("Kant's Kastle");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1000,700);
    myTimer = new Timer(40, new TickListener());
    myTimer.start();
    menu=new Menu(this);
    game = new GamePanel(this);
    level=new Level(this);
    add(menu);
    startMidi("title.mid",-1); //Playing title music.
    setResizable(false);
    setVisible(true);
    for(int k=0;k<3;k++){
      trophies[k]=new ImageIcon("trophy"+(k+1)+".png").getImage();
    }
    try{//Try-catch for errors.
      BufferedReader inF = new BufferedReader(new FileReader("record.txt"));
      for(int k=0; k<3;k++){//Reading from this file to see if the player had previously unlocked any achievements.
        String line=inF.readLine();
        if(line.equals("yes")){
          awards[k]=true;
        }
        else{
          awards[k]=false;
        }
        lines[k]=line;
      }
      inF.close();
    }
    catch(Exception e){
      System.out.println(e);
    }
  }
  
  class TickListener implements ActionListener{    //Class and its one method to update the graphics on screen every time the Timer tells them to. Creates the different screens and allows you to switch between them.
    public void actionPerformed(ActionEvent evt){
      if(change==true && kind.equals("Game")){     //Below, depending on which current GamePanel the JFrame has, a new GamePanel will be added if change is called for.
        change=false;
        game=new GamePanel(level.mainFrame);
        remove(level);
        add(game);
        midiPlayer.stop();
        startMidi("game.mid",-1);  //Music changes when you change screens
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
      if(kind.equals("Game") && game!= null && game.ready){   //Below, moving and painting the appropriate, active GamePanel.
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
  public static void startMidi(String midFilename,int len) {  //Method for playing the music and loading it up.
    try {    //Midi music player function taken from Mr. Mckenzie.
      File midiFile = new File(midFilename);    //Getting the music to be loaded in the following lines.
      Sequence song = MidiSystem.getSequence(midiFile);
      midiPlayer = MidiSystem.getSequencer();
      midiPlayer.open();
      midiPlayer.setSequence(song);
      midiPlayer.setLoopCount(len);        //Choosing how many times the music will be looped.
      midiPlayer.start();
    } catch (MidiUnavailableException e) { //Below is all for catching potential errors when loading the music.
      e.printStackTrace();
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public static void main(String[] args){ //Main method.
    KGame frame = new KGame();            //Launching the graphics.
  }
}


class GamePanel extends JPanel implements KeyListener{ //Class for drawing and managing the graphics and the game.
  private int money=100;                  //money is used to buy weapons
  private int destx, desty, selx, sely;   //Variables for keeping track of the mouse's position. The last two are for the mouse's position specifically when it is clicked down.
  private KGame mainFrame;
  public boolean ready=false;
  private boolean [] keys;
  public Image[] kantMoves=new Image[13];  //Arrays for holding some of the images for the game that have similar names. Most of these are sprites.
  public Image[] turretI=new Image[10];
  public Image[] coinsI=new Image[6];
  public Image tile1;                      //Tile1/2 and stone are for drawing the game board
  public Image tile2;
  public Image shovel;
  public Image stone;
  public Image slot;
  public int[] costs={20,40,80,160,60,90,50,55,70,25};        //Costs for the turrets.
  public ArrayList<Integer> chosenT=new ArrayList<Integer>(); //ArrayList holding the player's chosen turrets.
  public ArrayList<Tower> turrets=new ArrayList<Tower>();     //ArrayList holding all the turrets active in the game.
  public ArrayList<Monster> monsters=new ArrayList<Monster>();//ArrayList holding all the monsters active in the game.
  public ArrayList<Bullet> bullets=new ArrayList<Bullet>();   //All the bullets active in the game.
  public ArrayList<Bullet> trashB=new ArrayList<Bullet>();    //Below, ArrayLists for holding objects that are to be removed from the game. Done this way to avoid concurrent modification errors.
  public ArrayList<Tower> trashT=new ArrayList<Tower>();
  public ArrayList<Coin> coins=new ArrayList<Coin>();
  public ArrayList<Coin> trashC=new ArrayList<Coin>();
  public boolean beginPlay=false;                             //Determines if the game sequence is ready or not
  public String turretType = "Basic";  //Default selected turret.
  public int tBox=100;                 //X-coordinate of the box that is to be draw around the selected turret.
  private Image background;
  private Image castle;
  private Image fence;
  private Image siding;
  private Kant kant=new Kant(120,120); //Spawning in thd player-controlled character that is used as a weapon.
  private int picInd=2;                //Index for which sprite for Kant should be drawn.
  private int walkSpeed=4;             //Kant's walkspeed.
  public int timer=0;                  //Timing for spawning monsters.
  public double volume=0;
  private Monster tmpMon;              //Will hold a monster that is to be drawn and shown with information.
  private Tower tmpTurret;             //Same as above, but for turrets.
  public Sound[] canS=new Sound[4];    //Sound effects.
  public Sound[] monS=new Sound[4];
  public Sound explosion;
  public GameMaker game;               //Helps to read in which enemies are to be spawned in and when.
  public int soundCount=40;            //Timer that, when lowered enough, will play the monster horde's sound effect.
  private Image lost;
  private Image won;
  public boolean ends=false;           //Variables for checking if certain conditions are met
  public boolean didWon=false;
  public boolean isDown=false;
  public String mouseSelect="None";
  public int mouseIndex=-1;            //Index for which turret's image should be drawn on the mouse's postion, for placing.
  public boolean placeTurr=false;
  public int backx=-1000;              //X-coordinate for scrolling the game's screen at the beginning.
  public String[] typesT={"Basic","Normal","Good","Great","Wall","Cannon","Gold","Samurai","Spike","Mine"};
  public ArrayList<Samurai>sams= new ArrayList<Samurai>(); //Special ArrayList just for holding the Samurai defenders.
  public Image[]samwalk=new Image[6];  //Sprites for the samurai.
  public Image[]samwalkb=new Image[6];
  public Image[]samatk=new Image[9];
  public GamePanel(KGame m){     //Constructor.
    mainFrame=m; //Making sure the JFrame can still be accessed.
    keys = new boolean[KeyEvent.KEY_LAST+1];     //Getting keyboard input and mouse information.
    addMouseListener(new clickListener());
    addMouseMotionListener(new Mouse());
    destx=-20; desty=-20; selx=-20; sely=-20;    //Default values for the mouse's position is off screen.
    setSize(1000,700);
    addKeyListener(this);
    tmpMon=null;
    tmpTurret=null;
    for(int k=0;k<13;k++){  //Below, loading in all of the images and sound effects for this game.
      kantMoves[k]=new ImageIcon("Kant "+(k+1)+".png").getImage();
    }
    for(int k=0;k<4;k++){
      turretI[k]=new ImageIcon("turret "+(k+1)+".png").getImage();
      canS[k]=new Sound("cannon"+(k+1)+".wav");
      monS[k]=new Sound("monsterS"+(k+1)+".wav");
    }
    explosion=new Sound("explosion.wav");
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
    if(mainFrame.endless){
      turretI[6]=new ImageIcon("sunx.png").getImage();
    }
    else{
      turretI[6]=new ImageIcon("sun.png").getImage();
    }
    turretI[7]=samatk[0];
    turretI[8]=new ImageIcon("spike.png").getImage();
    turretI[9]=new ImageIcon("Mine.png").getImage();
    background=new ImageIcon("lake.png").getImage();
    castle=new ImageIcon("gate.png").getImage();
    tile1=new ImageIcon("tile1.png").getImage();
    tile2=new ImageIcon("tile2.png").getImage();
    stone=new ImageIcon("stone.png").getImage();
    fence=new ImageIcon("fence.png").getImage();
    siding=new ImageIcon("siding.png").getImage();
    shovel=new ImageIcon("shovel.jpg").getImage();
    lost=new ImageIcon("loser.png").getImage();
    won=new ImageIcon("winner.png").getImage();
    slot=new ImageIcon("slot.png").getImage();
    String[]gameMons=mainFrame.gameLevel;
    game=new GameMaker(gameMons,mainFrame,mainFrame.endless);//New GameMaker for the processing enemies.
    if(mainFrame.endless){
      money=350;            // You start with 350 coins in endless mode
    }
  }
  
  public void addNotify(){ //Method for notifying, seeing if the graphics are ready.
    super.addNotify();
    requestFocus();
    ready = true;
  }
  
  public void move(){                  //Method for moving Kant using WASD and updating the index for his sprites, and for placing down the turrets of the player,a and for some other key controls.
    if (keys[KeyEvent.VK_BACK_SPACE]){ //At any time during an actual game level, the player can quit back to the game menu.
      mainFrame.kind="Menu";//Resetting variables for possible new game levels.
      mainFrame.change=true;
      mainFrame.endless=false;
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
    else{   //When the player doesn't actually move Kant.
      picInd=kant.move(kant.x,kant.y,picInd);
    }
    if(placeTurr && destx>100 && destx<900 && desty>100 && desty<600){
      boolean overlay=false;   //Making sure that the turret is about to be placed in a valid, available position.
      for(Tower t:turrets){    //Checking every turret active for possible collisions.
        if(t.x-30<destx && destx<t.x+70 && t.y-30<desty && desty<t.y+70){
          overlay=true;        //When the position is already taken.
          break;
        }
      }
      if(!overlay){        //When the position is available.
        int temppx=destx;
        int temppy=desty;
        temppy=temppy/100; //Making sure that the turret is placed in the center of a grid, in the right position. Basically rounding off to nearest 100.
        temppy=temppy*100;
        temppx=temppx/100;
        temppx=temppx*100;
        int indy=0;        //Default index for the picture being shown. Below, getting the apropriate image and money taken for the chosen and placed turret.
        if(turretType.equals("Samurai")){          //Buys a samurai.
          if(money-55>0){
            sams.add(new Samurai(temppx,temppy));
            money-=55;
          }
        }
        else{
          if(turretType.equals("Basic")){         //Purchasing weapons. indy indicates what type of weapon is desired.
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
          else if(turretType.equals("Gold")){
            indy=6;
          }
          else if(turretType.equals("Spike")){
            indy=8;
          }
          else if(turretType.equals("Mine")){
            indy=9;
          }
          if (money-costs[indy]>=0){
            money-=costs[indy];
            turrets.add(new Tower(temppx+30,temppy+30,turretType,indy,mainFrame)); //Adding the purchased weapon to the list.         
          }
        }
      }
      placeTurr=false;//The placing is done.
    }
    else{
      placeTurr=false;
    }
  }
  public void paintComponent(Graphics g){   //Method for actually drawing all the needed graphics onto the screen.
    if(beginPlay){                  //When the turrets are chosen by the player.
      if(ends){                     //When the actual game level has ended.
        if(didWon){
          g.drawImage(won,0,0,1000,700,null);
        }
        else{
          g.drawImage(lost,0,0,1000,700,null);
        }
        if(keys[KeyEvent.VK_ENTER]){  //When the player enters to end the finished game level and return to level select.
          mainFrame.kind="Level";//Resetting variables so new game levels will be properly new.
          mainFrame.change=true;
          ends=false;
          didWon=false;
          mainFrame.endless=false;
        }
        try{//Try-catch in case of error where the file cannot be found.
          int changed;//This is the index of the boolean and status that has changed.
          if(!mainFrame.awards[0] && mainFrame.stage==1 && didWon){//When the first level has been completed.
            changed=0;
          }
          else if(!mainFrame.awards[1] && mainFrame.stage==10 && didWon){//When the second level is completed.           
            changed=1;
          }
          else if(!mainFrame.awards[2] && mainFrame.stage==-1){//When the endless mode has been attempted.
            changed=2;
          }
          else{
            changed=-1;
          }
          PrintWriter outFile = new PrintWriter(new BufferedWriter (new FileWriter ("record.txt")));//Writing out to this file to record the achievements.
          for(int k=0;k<3;k++){
            if(k!=changed){
              outFile.println(mainFrame.lines[k]);
            }
            else{//Only changing the file and updating if something has actually changed.
              outFile.println("yes");
              mainFrame.lines[k]="yes";
              mainFrame.awards[k]=true;
            }
          }
          outFile.close();
        }
        catch(Exception e){
          System.out.println(e);
        }
        return;
      }
      g.drawImage(background,backx+1000,0,1000,700,null); //Drawing the images that almost always appear on the screen during a game.
      int countT=0;      //Keeping track of which type of grass tile is supposed to be drawn. ALternates form the darker variety to the lighter, or vice-versa.
      for(int xx=backx;xx<backx+1000;xx+=100){            //Going through some positions on the screen and drawing the bordering stone tiles.
        g.drawImage(stone,xx,0,100,100,null);
        g.drawImage(stone,xx,600,100,100,null);
        g.drawImage(stone,backx+900,xx-backx,100,100,null);
      }
      for(int xxx=backx;xxx<backx+900;xxx+=100){  //Drawing all the alternating grasss tiles.
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
      g.drawImage(shovel,900,backx,40,40,null);
      g.drawImage(castle,backx-225,-300,400,1100,null);
      g.drawImage(kantMoves[picInd-1],kant.x,kant.y,40,40,null); //Drawing the Kant sprites.
      if(kant.attack){
        g.drawImage(kantMoves[12],kant.x-10,kant.y-10,65,65,null);       
      }
      if(backx<0){  //For scrolling the screen after turrets are chosen.
        backx+=15;
        return;
      }
      Font font = new Font("Verdana", Font.BOLD, 17);
      if(game.spot==-1 && monsters.size()==0) {//When the player has won the game, meaning all monsters are dead and the list for spawning monsters is traversed through.
        mainFrame.healthG=5; //Resetting.
        ends=true;
        didWon=true;
        return;
      }
      g.setFont(font); //Creating the font and setting its colour.
      g.setColor(Color.black);
      g.drawString("Coins: "+money,50,25);
      g.drawString("Wave "+game.wave,50,50);
      g.drawRect(tBox,620,40,40);
      if(isDown && mouseIndex>-1){//For removing plants using the shovel feature or just drawing the selected turret on the mouse's position.
        if(mouseSelect.equals("Remove")){
          g.drawRect(destx-20,desty-20,40,40); //Drawing in the removeing square
        }
        else{
          g.drawImage(turretI[mouseIndex],destx-20,desty-20,40,40,null);
        }
      }
      for(int i=0; i<chosenT.size(); i++){
        g.drawImage(turretI[chosenT.get(i).intValue()],100+(i*80),620,40,40,null);//Drawing the chosen turrets for the player to choose from.
      }
      int shotC=1;//Making sure the sound effects are not being played to often or at the same time.
      for(Tower turr:turrets){         //Turret drawing and playing the turret shooting sound effects.
        if(turr.shoot(bullets,monsters,coins)){
          if(shotC>0){
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
          if(turr.type.equals("Mine")){
            trashT.add(turr);  //Single-use mine is to be removed.
            explosion.play();
          }
        }
        if(turr.x<=selx && selx<=turr.x+40){ //When a turret that is in the field is clicked on to see its properties and stats.
          if(turr.y<=sely && sely<=turr.y+40){
            tmpTurret=turr;
            g.drawRect(turr.x,turr.y,40,40); //Drawing the turret's likeness above the field of play and putting a border around it.
            g.drawImage(turretI[turr.indy],200,10,40,40,null);
            if(turr.level<4)                 {//Showing the turret's stats and options for leveling it up, if available.
              g.drawString("Level Up | "+turr.level+" -> "+(turr.level+1),200,60);
              g.drawString("Cost: "+turr.ucost*turr.level,200,80);
            }
            else{
              g.drawString("Max Level",200,60);
            }  //Drawing the turret's healthbar. Based off a tower's hp, their health is divided by a certain amount for the bar so the bar is always 100 pixels wide
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
        g.drawImage(turretI[turr.indy],turr.x,turr.y,40,40,null);//Actually drawing the turret.
      }
      for(Bullet bull:bullets){ //Moving the bullets.
        money=bull.move(monsters,g,trashB,money);
      }
      for(Samurai sam:sams){   //Seeing if a samurai is selected and if it is to be leveled up.
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
        sam.drawSam(g,monsters,samwalk,samwalkb,samatk, selx, sely); //Drawing the samurai.
      }
      for(Coin cc:coins){ //Drawing all the collectible coins.
        cc.draw(g,coinsI);
      }
      ArrayList<Monster>endMons= new ArrayList<Monster>(); //For holding all the monsters that are to be removed from the game-field.
      Monster wid=null;    //Becomes not-null if a widow spider is killed. Later, it is checked and two normal spiders will be spawned in its place if it is not null.
      if(monsters!=null){  //Goes through the monsters ArrayList if possible and removes dead ones, checks for widow-deaths, monster-information selection, and monster and turret interaction.
        for (Monster ms: monsters){
          if(ms.x>1000){
            if(ms.type.equals("widow")){
              wid=ms;
            }
            endMons.add(ms);
          }
          ms.monsterDraw(g,turrets,sams);
          if(mainFrame.healthG<=0){ //Checking to see if a monster's getting past defences has ended the game.
            mainFrame.healthG=5;
            ends=true;
            return;
          }
          if(ms.x<=selx && selx<=ms.x+50){ //For monster information selection.
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
                  endMons.add(ms); //Damaging monsters with Kant's powers if eligible.
                }
              }
            }
          }
        }
        if(wid!=null){ //Adding the two monsters after widow-burst.
          monsters.add(new Monster(wid.x-1030,wid.y,"spider",wid.level,mainFrame));
          monsters.add(new Monster(wid.x-1050,wid.y,"spider",wid.level,mainFrame));
        }
      }
      if(selx!=-1 && tmpMon!=null){
        tmpMon.select=false;
        tmpMon=null; //When no monster is selected for information-viewing.
      }
      if(tmpTurret!=null){
        if(200<selx && selx<350){
          if(50<sely && sely<75){
            for(Tower t:turrets){
              if(t==tmpTurret){  //Checking to see if the selected turret is to be upgraded.
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
      timer+=game.time();  //Timer updated to see if monsters should be spawned or not.
      if(timer>game.monspawn){
        monsters=game.loadLevel(monsters);
        timer-=game.monspawn;
      }
      bullets.removeAll(trashB);
      monsters.removeAll(endMons);
      coins.removeAll(trashC);
      turrets.removeAll(trashT); //Removing all the useless, finished objects from the ArrayLists above.
      trashT.clear();
      trashB.clear();
      trashC.clear();   //Clearing the ArrayLists holding the ones to be removed to avoid redundancy.
      volume=(double)((double)monsters.size()/(double)15);
      if(volume>1){
        volume=1;
      }  
      if(soundCount<=1){  //Playing the appropriate sound effect for the monsters, depending on how many there are and how loud they are to be.
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
        soundCount=40;  //Making sure the sound isn't always being played.
      }
      soundCount-=1;
    }
    else{
      if(chosenT.size()==5){    //Turret selection
        beginPlay=true;  //Once the turrets have been chosen.
      }
      g.drawImage(background,backx+1000,0,1000,700,null);
      for(int k=0;k<10;k++){   //Drawing the turrets that can be chosen from.
        g.drawImage(slot,(k%4)*190+125-20,(k/4)*155+150-10,100+40,100+30,null);
        g.drawImage(turretI[k],(k%4)*190+125,(k/4)*155+150,100,100,null);
        Font font = new Font("Verdana", Font.BOLD, 17);//Drawing their names, as well.
        g.setFont(font);
        g.setColor(Color.black);
        String adder="";
        if(k<4){
          adder+=" Turret";
        }
        g.drawString(typesT[k]+adder,(k%4)*190+125,(k/4)*155+150-10);
        if(isDown && destx<(k%4)*190+125+100 && destx>(k%4)*190+125 && desty<(k/4)*155+150+100 && desty>(k/4)*155+150){
          if(!chosenT.contains(k)){//For selecting a turrret from all of them.
            if(mainFrame.endless){ //There are no gold mines in endless mode.
              if(k!=6){
                chosenT.add(k);
              }
            }
            else{
              chosenT.add(k);
            }
          }
          isDown=false;
        }
      }
      g.setFont(new Font("Verdana",Font.BOLD,35));
      g.drawString("Pick 5 Defences!",350,50);
      for(Integer num:chosenT){    //Drawing a white box around all those that have been chosen.
        int tempx=(num.intValue()%4)*190+125;
        int tempy=(num.intValue()/4)*155+150;
        g.setColor(Color.white);
        g.drawRect(tempx,tempy,100,100);
        
      }
    }
  }
  public void keyTyped(KeyEvent e) { //Below, listeners for the mouse, some of which are used to update important variables.
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
  class clickListener implements MouseListener{ //Class for checking for some of the user's mouse inputs.
    public void mouseEntered(MouseEvent e) {
    } //The following methods all check for some aspect of the user's mouse input. Names and purposes are self-explanatory.
    public void mouseExited(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
      if(mouseIndex>-1 && !mouseSelect.equals("Remove")){
        placeTurr=true; //When the mouse is released and a turret is to be placed.
      }
      else{
        if(mouseSelect.equals("Remove")){
          for(Tower t:turrets){ //Going through all of the turrets in the field and seeing if one of them is to be removed.
            if(t.x-50<destx && t.x+50>destx && Math.abs(t.y-desty)<30){
              turrets.remove(t); //Making sure some money is regained.
              money+=(costs[t.indy]+(t.ucost*t.level))/3;
              break;
            }
          }
          for(Samurai s:sams){
            if(s.x-50<destx && s.x+50>destx && Math.abs(s.y-desty)<30){
              sams.remove(s); //Removing, but for samurai.
              money+=30*s.level;
              break;
            }
          }
        }
      }
      isDown=false; //Resetting now that the removing or placing is done.
      mouseSelect="None";
      mouseIndex=-1;
    }    
    public void mouseClicked(MouseEvent e){
    }  
    
    public void mousePressed(MouseEvent e){ //Method for getting the coordinates of the mouse, when it is clicked down.
      isDown=true;
      selx=e.getX();
      sely=e.getY();
      for(Coin cc:coins){
        if(selx>cc.x && selx<cc.x+20 && sely>cc.y && sely<cc.y+20){
          money+=cc.val; //For collecting coins.
          trashC.add(cc);
        }
      }
      if(sely<40 && selx>900 && selx<940){
        mouseSelect="Remove";
        mouseIndex=0; //For when a shovel is to be used.
      }
      else if(620<sely && sely<660){ //For selecting a turret or samurai.
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
            tBox=tempx-80; //Updating the border-box's x-coordinates.
            mouseIndex=chosenT.get(k).intValue();
          }
        }
      }
    }
  }    
}


class Kant{ //Class for the player-controlled character.
  public int x;
  public int y;
  public int picCount;
  public boolean attack=false;
  public Kant(int placex,int placey){ //Constructor.
    x=placex;
    y=placey;
    picCount=0;
  }
  public int move(int mx,int my,int ind){ //Moving the character.
    attack=false;
    if(mx>x){
      if(mx<850){
        x=mx; //Making sure that the movement doesn't go outside of the play-area and the screen.
      }
      if (ind>6 && ind<9){
        picCount++; //Below, continuing Kant's animation and aslo making sure that all this stays within Kant's images' range. Four if-statements for Kant's four directions of movement.
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
    else{         //When Kant is not moving he does damage
      ind=13;
      attack=true;
      return ind;
    }
  }
}
class Tower{ //Class for the turrets. This class is also used for the canons, spikes, mines, and coin-producing defences, too.
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
  public int level=1;
  public KGame mainFrame;
  public Tower(int xx,int yy, String kind, int img, KGame m){//Constructor.
    x=xx;
    y=yy;
    type=kind;
    indy=img;
    health=100;
    if(type.equals("Basic")){ //Below, setting some stats to the appropriate amount for what the defence is and will do, based on type.
      damage=15;
      bspeed=8;
      ucost=25;
    }
    else if(type.equals("Normal")){
      damage=25;
      bspeed=9;
      ucost=45;
    }
    else if(type.equals("Good")){
      damage=2;
      bspeed=10;
      ucost=70;
    }
    else if(type.equals("Great")){
      damage=75;
      bspeed=10;
      ucost=150;
    }
    else if(type.equals("Wall")){
      health=1200;
      damage=0;
      bspeed=0;
      ucost=50;
    }
    else if(type.equals("Cannon")){
      health=250;
      damage=100;
      bspeed=4;
      ucost=100;
    }
    else if(type.equals("Gold")){
      health=120;
      bspeed=-35;
    }
    else if(type.equals("Spike")){
      health=100;
      damage=1;
      bspeed=15;
    }
    else if(type.equals("Mine")){
      health=1;
      damage=300;
      bspeed=15;
    }
    cooldown=(30-(bspeed*2))*10; //Getting the cooldown time it takes for a defence to do its thing.
    max=cooldown;
    maxhealth=health;
    mainFrame=m;
  }
  public boolean shoot(ArrayList<Bullet> bs, ArrayList<Monster> ms,ArrayList<Coin> cs){//Method for shooting out a bullet or a coin.
    if(cooldown<=0){ //When the cooldown has finished, a coin may be added if the type is Gold.
      if(type.equals("Gold")){
        cs.add(new Coin(x,y,10));
        cooldown=max;
        return false;
      }
      else if(type.equals("Spike") | type.equals("Mine")){ //For types of Spike or Mine, an invisible bullet is added that will damage enemies.
        for(Monster mons: ms){
          if((Math.abs(y-mons.y)<30) && mons.x<=x+42 && mons.x>(x-66) && !mons.type.equals("ghost")){
            bs.add(new Bullet(x,y,type,0,damage)); //When an enemy is in range, the bullet is added.
            cooldown=max;
            return true;
          }
        }
        return false;
      }
      else{ //For when an actuall shooting-defence is shooting.
        for(Monster mons: ms){
          if((Math.abs(y-mons.y)<30) && mons.x<=x+300 && mons.x>x){
            cooldown=max; //If an enemy is in range, a bullet is added. Walls don't shoot anything.
            if(!type.equals("Wall")){
              bs.add(new Bullet(x,y,type,bspeed,damage));
            }
            return true;
          }
        }
        return false;
      }
    }
    else{
      cooldown-=5;
      return false;
    }
  }
  public boolean damage(int hurt){ //Method for damaging the defence.
    health-=hurt;
    if (health<=0){
      return true;   //Returns true if the defence is destroyed.
    }
    else{
      return false;
    }
  }
  public void levelUp(){  //Method for leveling up the defence.
    if(level<4){
      maxhealth+=maxhealth/2; //Stats are improved by 150% by doing so.
      health=maxhealth;
      damage+=damage/2;
      level+=1;
    }
  }
}
class Bullet{ //Class for the bullets and projectiles in the game fired by the defences.
  int x;
  int y;
  String type;
  int speed;
  int damage;
  int contact=-1;  //Will hold where the bullet first made contact with a monster. Used for the turret that can go through multiple enemies.
  private Image[] types=new Image[6]; //Images for the different bullet types.
  public Bullet(int xx, int yy, String ttype, int sspeed, int ddamage){ // Getting the bullet values based on the turret that shot
    x=xx;
    y=yy;
    type=ttype;
    speed=sspeed;
    damage=ddamage;
    for(int k=0;k<5;k++){
      types[k]=new ImageIcon("Bullet "+(k+1)+".png").getImage();
    }
    types[5]=new ImageIcon("explosion.png").getImage(); //One image is for the mine's explosion effect.
  }
  public int move(ArrayList<Monster> ms,Graphics g,ArrayList<Bullet> tb,int money){ //Method for making the bullet move.
    int ind=0;  //Below, drawing the right image for the bullet.
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
    else if(type.equals("Spike")){
      ind=-1;
    }
    else if(type.equals("Mine")){
      ind=5;
    }
    else{
      ind=4;
    }
    if(ind>=0 && ind<5){
      g.drawImage(types[ind],x,y+10,40,10,null);
    }
    if(ind==5){
      g.drawImage(types[ind],x-20,y-20,100,100,null);
    }
    for(Monster mons:ms){  //Going through all the monsters.
      if(mons.x<x+40 && Math.abs(mons.y-y)<30 && mons.x>(x-66)){ //When a monster collides with the bullet.
        if(contact<0){  //Getting the contact position of the first monster, if it is needed.
          contact=mons.x;
        }
        if(mons.damage(damage,type)){
          mons.x+=1000;   //Damaging monster. If it is killed, it is moved off-screen until it is removed.
          mons.speed=0;   //Making sure it doesn't move, either.
          if(!type.equals("Mine")){
            money+=10*mons.level;  //Getting some money for killing some enemies.
          }
        }
        if(type.equals("Good") && contact+100<x){ //For removing the bullet and putting it off-screen, too, after it has either made contact or gone through enough distance.
          x=10000;
          speed=0;
          tb.add(this);
          break;
        }
        else{
          if(!type.equals("Good")){ //Allows the good bullet to do splash damage
            x=10000;
            speed=0;
            tb.add(this);
            break;
          }
        }
      }
    }
    x+=speed;   //Increasing the postion of the bullet.
    if(x>700){  //Removing when it is about to go offscreen.
      x=10000;
      speed=0;
      tb.add(this);
    }
    return money; //Getting the money made back.
  }
}


class Menu extends JPanel{ //Class for the menu. Same as the GamePanel clas except that it is not for a game's level and has reduced features.
  private int destx,desty; //Variables for keeping track of the mouse's position.
  private Image screen;    //Images of the rules and credits that will be shown on the screen when selected.
  private Image rulePic;
  private Image credits;
  private KGame mainFrame;
  private boolean rules=false; //Booleans for if these should be shown.
  private boolean credit=false;
  public boolean ready=false;
  public Menu(KGame m){  //Constructor.
    mainFrame=m;
    addMouseListener(new clickListener());
    destx=-20;  //Default values for the mouse's position is off screen.
    desty=-20;
    setSize(800,600);
    screen=new ImageIcon("KantScreen.jpg").getImage(); //Loading in the images.
    rulePic=new ImageIcon("rules.png").getImage();
    credits=new ImageIcon("credits.png").getImage();
  }
  public void addNotify() {    //Method for notifying, seeing if the graphics are ready.
    super.addNotify();
    requestFocus();
    ready = true;
  }
  
  public void paintComponent(Graphics g){   //Method for actually drawing all the needed graphics onto the screen.
    g.drawImage(screen,0,0,1000,700,null);  //Drawing the base image and the rules or the credits if selected.
    if(rules){
      g.drawImage(rulePic,0,0,995,665,null);
    }
    if(credit){
      g.drawImage(credits,0,0,1000,700,null);
    }
  }
  
  class clickListener implements MouseListener{  //Class for checking for the user's mouse inputs.
    public void mouseEntered(MouseEvent e) {
    }  //The following methods all check for some aspect of the user's mouse input. Names and purposes are self-explanatory.
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}    
    public void mouseClicked(MouseEvent e){}  
    
    public void mousePressed(MouseEvent e){ //Method for getting the coordinates of the mouse.
      destx = e.getX();
      desty = e.getY();   //Below, if the x and y positions of the mouse are within a certain range, the corresponding image's boolean will be made true or false.
      if(410<destx && destx<610 && !rules){ //Allows you to navigate around the title screen using clicks
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
      if(410<destx && destx<610){
        if(530<desty && desty<670){
          credit=true;
        }
      }
      if(rules){
        if(0<destx && destx<100){
          if(0<desty && desty<80){
            rules=false;
          }
        }
      }
      if(credit){
        if(0<destx && destx<100){
          if(0<desty && desty<80){
            credit=false;
          }
        }
      }
    }
  }
}


class Level extends JPanel{  //Class for the level-select screen. Very similar to the menu.
  public String[] level1={"100","-100","z","z","z","z","z","z","z","z","n","-500","z","z","z","z","z","z","n","k","k","z","z","n","k","k","z","z","s","w","v","s2","w","v","-500","2","z","z","z","d","k","k","n","w","s","v","s","z","z","d","k","k","w","s","v"};
  public String[] level2={"100","-100","z","z","z","z","z","k","k","z","z","w","z","z","-500","s","s","z","z","z","n","k","k","g","w","s","s","2","z","z","z","n","g","v","v","w","z","z","z","z","w","s","s2","v","-500","z","z","z","z","n","w","3","d","k","g","k","w","w"};
  public String[] level3={"150","-100","z","z","z","z","n","n","n","d","w","w","z","z","z","-500","z","d","z","n","n","z","d","n","w","n","z","z","z","d","d","d","d","-500","2","z","z","z","n","n","w","3","d","d","d","d","d"};
  public String[] level4={"100","-100","z","z","n","s","z","k","k","k","z","z","z","z","s","s","z","z","-500","n","n","s","s","z","z","z","2","z","z","z","n","k","k","w","z","z","z","n","w","z","s","s","z","w","s","w","s","w","-500","3","d","z","z","z","z","d","w","d","k","k","k","k","s","w","w"};
  public String[] level5={"70","-100","z","z","z","z","z","z","z","z","n","-500","z","z","z","z","z","z","n","g","g","z","z","n","g","g","z","z","g","g","g","g","w","-500","2","z","z","z","d","g","g","n","w","w","g","g","z","z","d","g","g","w","w"};
  public String[] level6={"100","-100","g","k","g","k","z","z","z","v","v","z","z","k","k","k","g","z","z","z","z","z","s2","-500","s2","z","z","g","g","k","k","v","v","v","s","s2","v","v","v","k","k","k","g","s","z","2","k","k","s","v","s","s","v","v","g","s2","-500","3","z","z","d","z","z","k","k","4","z","z","s2","z","z","v","v","s","s","s2","z","s2"};
  public String[] level7={"100","-100","n","v","v","v","z","z","z","z","s","s","z","z","k","w","k","w","g","z","z","z","z","v","v","-500","n","z","z","d","k","k","k","w","w","w","s","s2","v","v","v","v","v","z","z","d","k","k","k","w","w","w","s","s","v","v","v","v","v","-500","2","z","z","d","k","k","k","w","w","w","s","s2","v","v","v","v","s","s","d","z","z","d","k","k","k","w","w","w","s","s","v","v","v","v","v"};
  public String[] level8={"50","-100","k","k","k","k","z","z","z","k","k","k","k","z","z","k","k","k","k","z","z","z","w","k","k","k","k","w","n","n","w","-500","k","k","k","s","z","z","n","s","k","k","z","n","v","s","z","z","k","k","w","d","w","-500","z","z","z","d","d","d","k","k","n","w","s","v","v","z","z","2","d","d","3","k","k","k","k","k","k","k","k","k","k","k","k"};
  public String[] level9={"100","-100","z","z","z","z","z","z","n","n","n","v","w","g","v","v","z","z","z","v","v","w","w","-500","2","n","z","z","z","w","w","w","s","s","w","w","g","v","v","s","w","w","v","v","z","z","z","-500","3","n","n","d","k","k","k","w","w","w","s","s","w","v","v","v","s2"};
  public String[] level10={"100","-100","z","z","z","z","z","z","n","g","n","g","w","v","v","v","g","z","z","s","v","s","v","w","n","w","-500","2","d","z","z","z","n","w","w","s","s","v","g","w","w","g","v","w","v","s","w","g","w","v","v","s","s2","-500","3","n","n","d","d","k","k","k","w","w","4","n","d","w","k","g","g","5","n","d","w","k","g","g","s"};
  private int destx,desty; //Variables for keeping track of the mouse's position. Above, the arrays hold the enemy spawn-order and spawn-times for each level.
  public KGame mainFrame;
  public boolean ready=false;
  private Image levelSelect;
  public Level(KGame m){ //Constructor.
    mainFrame=m;
    addMouseListener(new clickListener());
    destx=-20;    //Default values for the mouse's position is off screen.
    desty=-20;
    setSize(1000,700);
    levelSelect=new ImageIcon("LevelSelect.png").getImage();
  }
  public void addNotify() {  //Method for notifying, seeing if the graphics are ready.
    super.addNotify();
    requestFocus();
    ready = true;
  }
  
  public void paintComponent(Graphics g){ //Method for actually drawing all the needed graphics onto the screen.
    g.drawImage(levelSelect,0,0,1000,700,null);
    for(int k=0;k<3;k++){
      if(mainFrame.awards[k]){//Drawing the unlocked achievements, if any.
        g.drawImage(mainFrame.trophies[k],300+k*200,460,150,150,null);
      }
    }
    if(desty>460 && desty<610){
      for(int k=0;k<3;k++){//If the player clicks on a trophy, the description will be shown.
        if(destx>300+k*200 && destx<300+k*200+150 && mainFrame.awards[k]){
          Font font = new Font("Verdana", Font.BOLD, 17);
          g.setFont(font); //Creating the font and setting its colour.
          g.setColor(Color.black);
          g.drawString(mainFrame.descriptions[k],300,380);
        }
      }
    }
  }
  
  class clickListener implements MouseListener{ //Class for checking for the user's mouse inputs.
    public void mouseEntered(MouseEvent e) {}   //The following methods all check for some aspect of the user's mouse input. Names and purposes are self-explanatory.
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}    
    public void mouseClicked(MouseEvent e){}  
    
    public void mousePressed(MouseEvent e){   //Method for getting the coordinates of the mouse.
      destx = e.getX();
      desty = e.getY();    //Below, if the x and y coordinates are within a certain area, the corresponding level will be made to be loaded up and turned to.
      if(420<desty && desty<595){
        if(110<destx && destx<190){
          mainFrame.endless=true;  //For the endless level.
          mainFrame.kind="Game";
          mainFrame.change=true;
          mainFrame.stage=-1;
        }
      }
      if(95<desty && desty<315){        //Gets the selected level from the user
        if(110<destx && destx<190){
          mainFrame.gameLevel=level1;
          mainFrame.stage=1;
        }
        else if(190<destx && destx<275){
          mainFrame.gameLevel=level2;
          mainFrame.stage=2;
        }
        else if(275<destx && destx<355){
          mainFrame.gameLevel=level3;
          mainFrame.stage=3;
        }
        else if(355<destx && destx<440){
          mainFrame.gameLevel=level4;
          mainFrame.stage=4;
        }
        else if(440<destx && destx<520){
          mainFrame.gameLevel=level5;
          mainFrame.stage=5;
        }
        else if(520<destx && destx<605){
          mainFrame.gameLevel=level6;
          mainFrame.stage=6;
        }
        else if(605<destx && destx<680){
          mainFrame.gameLevel=level7;
          mainFrame.stage=7;
        }
        else if(680<destx && destx<765){
          mainFrame.gameLevel=level8;
          mainFrame.stage=8;
        }
        else if(765<destx && destx<835){
          mainFrame.gameLevel=level9;
          mainFrame.stage=9;
        }
        else if(835<destx && destx<900){
          mainFrame.gameLevel=level10;
          mainFrame.stage=10;
        }
        if(110<destx && destx<900){
          mainFrame.kind="Game";
          mainFrame.change=true;
        }
      }
    }
  }
}
class Monster{  //Class for the monster enemies.
  public KGame mainFrame; //Basic variables to control health, damage, and movement
  public int x;
  public int y;
  public int picCount;
  public int hp;
  public int maxhp;
  public int power;
  public String type;
  public double speed;
  public boolean select=false;
  public int level;
  private Image[] mPicL;
  private Image attack;
  private Image arrow;
  public int ax=-1;
  private int listLen;
  public int pushx=0;
  public Monster(int placex, int placey, String mtype,int monlevel, KGame m){ //Constructor.
    mainFrame=m;
    x=placex;
    y=placey;
    level=monlevel;
    picCount=0;
    type=mtype;    
    if(type.equals("werewolf")){ //Loading the proper images and list lengths depending on the monster's type.
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
    
    
    if(type.equals("nymph")){ //Getting the right health for the monster-type.
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
    if(type.equals("widow")){  //Getting the right speed.
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
    for(int i=1; i<listLen+1; i++){  //Loading in the actual images.
      mPicL[i-1]=new ImageIcon(type+i+".png").getImage();
    }
    if(type.equals("spider") | type.equals("demon")){ //Getting the right damage values.
      power=7;
    }
    else if(type=="werewolf"){
      power=10;
    }
    else{
      power=5;
    }
    hp=hp*level; //The level of the monster modifies the monster's stats by 100%.
    maxhp=hp;
    power+=level*3;
    if(type.equals("skeleton")){ //Special images just for the skeleton monster-type.
      attack=new ImageIcon("skeleshoot.png").getImage();
      arrow=new ImageIcon("arrow.png").getImage();
    }
  }
  public void floorEnd(){//Method for checking if a monster has passed the defences and defeated the player.
    if(x<10){
      speed=0;//Getting the monster off-screen, to be removed.
      x=10000;
      mainFrame.healthG=0;//Will end the game because of this.
    }
  }
  public void monsterDraw(Graphics g,ArrayList<Tower> turrets, ArrayList<Samurai>sams){//Method for drawing the monster's actions.
    floorEnd();
    boolean overlay=false;
    boolean shooting=false;
    if(type.equals("skeleton")){ //For letting the skeletons shoot at towers.
      for(Tower t:turrets){      //Going through all the towers and shooting at one in range if it is not a spike.
        if(t.x-40<x && x<t.x+100 && Math.abs(y-t.y)<30 && !t.type.equals("Spike")){
          overlay=true;
          shooting=true;
          g.drawImage(attack,x,y,40,60,null);
          if(ax==-1){   //For the skeleton's projectile's position.
            ax=x;
          }
          g.drawImage(arrow,ax,y+20,10,15,null);
          ax-=5;   //Getting the projectile to move.
          if(ax<t.x && Math.abs(t.y-y)<30){
            ax=-1;
            if(t.damage(power)){  //Damaging towers with the projectile.
              turrets.remove(t);
            }
          }
          break;
        }
      }
    }
    else{//For just attacking towers when the monster isn't a skeleton or a ghost.
      for(Tower t:turrets){//Going through all the towers.
        if(t.x-40<x && x<t.x+40 && Math.abs(t.y-y)<30 && !type.equals("ghost") && !t.type.equals("Spike")){
          overlay=true;//If in range, the monster will attack.
          if(t.damage(power)){
            turrets.remove(t);
          }
          x+=5;  //Stopping the monster from moving forwards, past the tower.        
          break;
        }
      }
    }
    ArrayList<Samurai>tmpsams=new ArrayList<Samurai>(); //ArrayList for holding defeated samurai.
    for(Samurai s:sams){     //Going through all the samurai.
      if(s.y==y && s.x+50>x && x>s.x && !type.equals("ghost")){
        if(s.damage(power)){ //When in range and not a ghost, the samurai is attacked.
          tmpsams.add(s);
        }
        overlay=true;
      }
    }
    sams.removeAll(tmpsams);
    if(type.equals("spider") | type.equals("widow")){ //Below, drawing all the monsters with the right image for their movement.
      g.drawImage(mPicL[picCount/5],x,y+10,55,35,null);
      if(select){
        g.drawRect(x,y+10,50,40);
      }
    }
    else if(type.equals("vampire") | type.equals("werewolf")){
      g.drawImage(mPicL[picCount/5],x,y,50,40,null);
      if(select){
        g.drawRect(x,y,50,40);
      }
    }
    else{
      if(!shooting){
        g.drawImage(mPicL[picCount/5],x,y,40,50,null);
      }
      if(select){   //Drawing the border around the selected monster.
        g.drawRect(x,y,40,50);
      }
    }
    if(select){     //For drawing the monster when they are selected to be seen with information near the top of the screen.
      g.drawImage(mPicL[picCount/5],200,20,40,40,null);
      int barNum=(maxhp/2)/300+1;   //Determining the number of hp bars. Each one is 300 pixels long
      for(int i=0; i<barNum; i++){  //Drawing the health-bars, both the border and the actual amount.
        if(i+1==barNum){
          g.drawRect(250,20+(20*i),(maxhp/2)%300,10);  //Drawing the bars stacked on each other
        }
        else{
          g.drawRect(250,20+(20*i),300,10);
        }
      }
      barNum=(hp/2)/300+1;         //Repeating the process except with filled rects
      for(int i=0; i<barNum; i++){
        if(i+1==barNum){
          g.fillRect(250,20+(20*i),(hp/2)%300,10); //The bar is drawn to the remainder of 300
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
        x-=speed;  //Moving towards their objective.
      }
      else{
        x+=3;      //Additional push for moving a monster back.
        pushx-=3;  //Happens when being hit by turret of type great.
      }
    }
  }
  public boolean damage(int hurt,String typeT){ //Method for hurting the monsters. Will return true if the monster is killed.
    if(type.equals("vampire")){    //Hurt depends on type. Vampire cant be hurt by basic
      if(!typeT.equals("Basic")){
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
        pushx=7; //Getting the push-pack for this type of turret.
      }
      return false;
    }
  }
}
class Sound{    //Sound class. Used for sound-effects.
  File wavFile;
  AudioClip sound;
  public Sound(String name)
  {
    wavFile = new File(name);
    try{sound = Applet.newAudioClip(wavFile.toURL());}
    catch(Exception e){e.printStackTrace();}
  }
  public void play(){ //Method for playing the sound.
    sound.play();
  }
}
class GameMaker{ //Class for getting the monsters to be spawned in at the right time, according to the level.
  public KGame mainFrame;
  public String[] spawn=new String[25];
  public int len;
  public int spot=1;
  public int monspawn=0;
  public int waitTime=1;
  public int monsterLvl=1;
  public int wave=0;
  public int monsRow=100;
  public boolean infinite;
  public boolean money;
  public int wait=-500;
  public int max=-500;
  public String[] types={"z","z","z","z","z","z","z","n","n","k","k","g","g","g","v","v","v","w","w","w","s","s","s","s2","d"};//Strings that stand for all the types of monsters. Used while getting random enemies with endless mode.
  public GameMaker(String[] level, KGame game,boolean endless){ //Constructor.
    if(!endless){
      spawn=level;
    }
    mainFrame=game;
    infinite=endless;
    if(infinite){
      spawn=new String[25]; //Making an empty array for infinite mode.
    }
    len=spawn.length;
    spot=1;
    if(infinite){
      spawn[0]="100";        //Setting up the basically universal first two values. The 100 is the amount of time between monster spawns.
      spawn[1]="-500";       //Negative values are for the time lapses between monster waves.
      for(int k=2;k<25;k++){ //Getting the random monster spawns.
        spawn[k]=types[randint(0,24)];
      }
    }
  }
  public ArrayList<Monster> loadLevel(ArrayList<Monster> monsters){ //Method for spawning in the monsters.
    if(monspawn==0){      //Getting time between monster spawns.
      monspawn=Integer.parseInt((spawn[0]));
    }
    if(spot>=len){
      if(infinite){
        spot=1;    //Settting up the next batch of monsters with infinite mode.
        spawn[1]="-500";
        monsterLvl+=1;
        for(int k=2;k<25;k++){
          spawn[k]=types[randint(0,24)];
        }
      }
      else{
        spot=-1;  //Setting spawn to an invalid number so that the game knows that there are no more monsters to spawn.
        return monsters;
      }
    }
    if(spot>=0){
      if(spawn[spot].equals("z")){  //Spawning the corresponding monster from the letters.
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
        int num=Integer.parseInt(spawn[spot]); //Getting the time delays between waves if the number is negative. Setting the new monster levels if positive.
        if(num>0){
          monsterLvl=num;
        }
        else{
          waitTime=num;
        }
      }
      spot+=1;
      monsRow+=randint(0,4)*100; //Spawning in a random row.
      if(monsRow>=600){
        monsRow=100+randint(0,4)*100;
      }
    }
    return monsters;
  }
  public int time(){ //Method for timing the spawns.
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
class Coin{ //Class for the coins that the player can click on to collect and gain money.
  int x;
  int y;
  int val;
  int indy=0;
  public Coin(int sx,int sy, int vall){
    x=(int)(Math.random()*(sx+20-sx+20+1)+sx-20); //Getting a random coin position within a certain area. Area is determined by the coin-spawning tower's position.
    y=(int)(Math.random()*(sy+20-sy+20+1)+sy-20);
    val=vall;
  }
  public void draw(Graphics g,Image[] i){
    g.drawImage(i[indy],x,y,20,20,null); //Drawing the coin in its movements.
    indy+=1;
    if(indy==6){
      indy=0;
    }
  }
}
class Samurai{ //Class for the samurai heros that the player does not control.
  public int x;
  public int y;
  public int health;
  public int maxhealth;
  public int damage;
  public int level=1;;
  public String action="r"; //String that keeps track of which direction the samurai is heading, or if it is attacking.
  public int spot=0;   //Index of the only-moving animation pictures.
  public int attack=0; //Keeps track of which image in samurai's attack images should be drawn.
  public boolean select=false;
  public int tmppt=0;
  public Samurai(int sx, int sy){ //Constructor.
    x=sx; y=sy+20;
    health=1000;
    maxhealth=health;
    damage=3;
  }
  public void drawSam(Graphics g, ArrayList<Monster> monsters, Image[]samwalk, Image[]samwalkb, Image[]samatk, int selx, int sely){//Method for drawing the samurai and its actions.
    String tmpA=action;
    ArrayList<Monster>tmpmons=new ArrayList<Monster>(); //Will hold all of the vanquished monsters.
    for(Monster m:monsters){            //Going through all the monsters.
      if(m.y==y && m.x<x+100 && m.x>x){ //If in range.
        action="a";
        g.drawImage(samatk[attack/4],x,y,50,50,null);
        if(!m.type.equals("ghost")){    //Samurai attacks if the enemy is not a ghost.
          if(m.damage(2,"Samurai")){
            tmpmons.add(m);
          }
        }
        attack+=1;
        if(attack==36){  //Making sure the index does not go out of range.
          attack=0;
        }
      }
    }
    monsters.removeAll(tmpmons);//Clearing the monsters of the dead.
    if(action.equals("r")){
      g.drawImage(samwalk[spot/4],x,y,50,50,null);//Drawing the samurai as it moves right.
      x+=1;
    }
    else if(action.equals("l")){//Samurai moving left.
      g.drawImage(samwalkb[spot/4],x,y,50,50,null);
      x-=1;
    }
    spot+=1;
    if(spot==24){
      spot=0;//Keeping index in range.
    }
    if(x>800){
      action="l"; //When the samurai has to turn around after walking in either direction.
    }
    if(x<100){
      action="r";
    }
    if(action.equals("a")){
      action=tmpA;
    }
    if(select){   //For when the player selects to view the samurai's information.
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
        g.drawRect(250,25,maxhealth/16,10); //Drawing the health-bar.
        g.fillRect(250,25,health/16,10);
      }
      else{
        select=false;
      }
    }
    if(x<selx && selx<x+50 && y<sely && sely<y+50){
      select=true;     //When the samurai is to be viewed.
      tmppt=selx*sely;
    }
  }
  public boolean damage(int hurt){ //Method for damaging the samurai.
    health-=hurt;
    if(health<=0){
      return true;
    }
    return false;
  }
  public void levelUp(){   //Method for the samurai's leveling up.
    if(level<4){           //Improving the stats as well by 150%.
      maxhealth+=maxhealth/2;
      health=maxhealth;
      damage+=damage/2;
      level+=1;
    }
  }
}
