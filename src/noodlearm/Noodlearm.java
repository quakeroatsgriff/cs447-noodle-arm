package noodlearm;
import java.util.ArrayList;
import java.util.ResourceBundle;

import jig.Entity;
import jig.ResourceManager;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class Noodlearm extends StateBasedGame{
    public static final int STARTUPSTATE = 0;
    public static final int PLAYINGSTATE  = 1;
    public static final int GAMEOVERSTATE  = 2;

    public final int ScreenWidth;
    public final int ScreenHeight;
    //Resource strings
    public static final String STARTUP_SCREEN_RES = "noodlearm/res/";
    public static final String GAMEOVER_SCREEN_RES = "noodlearm/res/";

    /*
    * Creates the Noodle Arm game frame.
    */
    public Noodlearm(String title, int width, int height){
        super(title);
        ScreenHeight = height;
        ScreenWidth = width;
        Entity.setCoarseGrainedCollisionBoundary(Entity.AABB);
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
        addState(new StartupState());
        addState(new PlayingState());
        addState(new GameoverState());

        //Preload resources here
        //TODO
        // ResourceManager.loadImage(STARTUP_SCREEN_RES);
        // ResourceManager.loadImage(GAMEOVER_SCREEN_RES);

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