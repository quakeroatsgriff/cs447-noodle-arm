package noodlearm;

import jig.Entity;
import jig.ResourceManager;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import java.util.ArrayList;

public class Noodlearm extends StateBasedGame{
    public static final int STARTUPSTATE = 0;
    public static final int PLAYINGSTATE  = 1;
    public static final int CLIENTPLAYINGSTATE  = 2;
    public static final int GAMEOVERSTATE  = 3;

    public final int ScreenWidth;
    public final int ScreenHeight;

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int UP = 3;    

    Client client;
    Server server;
    public ArrayList<Grid> grid;
    public ArrayList<WeaponSprite> weapons_on_ground;
    public ArrayList<Enemy> enemies;
    public Player server_player;
    public Player client_player;
    public String network_identity;
    //Resource strings
    public static final String STARTUP_SCREEN_RES = "noodlearm/res/";
    public static final String GAMEOVER_SCREEN_RES = "noodlearm/res/";
    public static final String BLANK_RES = "noodlearm/res/img/world/blank.png";
    public static final String WALL_RES = "noodlearm/res/img/world/wall.png";
    public static final String HIGHLIGHT_RES = "noodlearm/res/img/world/path-highlight.png";
    public static final String SWORD_RES = "noodlearm/res/img/weapons/sword.png";
    public static final String CLUB_RES = "noodlearm/res/img/weapons/club.png";
    public static final String SPEAR_RES = "noodlearm/res/img/weapons/spear.png";
    public static final String KNIGHT_FORWARD_RES = "noodlearm/res/img/player/knight.png";
    public static final String HOUND_RES = "noodlearm/res/img/enemies/hound.png";
    public static final String SKELETON_RES = "noodlearm/res/img/enemies/skeleton.png";


    /*
    * Creates the Noodle Arm game frame.
    */
    public Noodlearm(String title, int width, int height){
        super(title);
        ScreenHeight = height;
        ScreenWidth = width;
        //Circle bounding boxes so entities can be rotated. Because AABB doesn't allow that...
        Entity.setCoarseGrainedCollisionBoundary(Entity.CIRCLE);
        grid = new ArrayList<Grid>(50);
        weapons_on_ground = new ArrayList<WeaponSprite>(1);
        enemies = new ArrayList<Enemy>(10);
        network_identity = "Server";
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
        addState(new StartupState());
        addState(new PlayingState());
        addState(new ClientPlayingState());
        addState(new GameoverState());

        //Preload resources here
        //TODO
        // ResourceManager.loadImage(STARTUP_SCREEN_RES);
        // ResourceManager.loadImage(GAMEOVER_SCREEN_RES);
        ResourceManager.loadImage(BLANK_RES);
        ResourceManager.loadImage(WALL_RES);
        ResourceManager.loadImage(SWORD_RES);
        ResourceManager.loadImage(SPEAR_RES);
        ResourceManager.loadImage(CLUB_RES);
        ResourceManager.loadImage(KNIGHT_FORWARD_RES);
        ResourceManager.loadImage(HOUND_RES);
        ResourceManager.loadImage(SKELETON_RES);
        ResourceManager.loadImage(HIGHLIGHT_RES);


    }

    public static void main(String[] args){
        AppGameContainer app;
        try{
            app = new AppGameContainer((new Noodlearm("Noodle Arm", 600, 800)));
            app.setDisplayMode(600,800,false);
            Entity.antiAliasing=false;
            app.setVSync(true);
            app.start();
        } catch(SlickException e){
            e.printStackTrace();
        }
    }
}