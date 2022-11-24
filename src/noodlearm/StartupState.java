package noodlearm;

import jig.ResourceManager;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EmptyTransition;
import org.newdawn.slick.state.transition.HorizontalSplitTransition;
import jig.Vector;

public class StartupState extends BasicGameState {

    @Override
    public int getID() {
        return Noodlearm.STARTUPSTATE;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {

    }

    @Override
	public void enter(GameContainer container, StateBasedGame game) {
    
    }
    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {

        Noodlearm na = (Noodlearm)game;
        //TODO
        g.drawString( "Press S to start server or C to connect as client.", 75,350 );
        g.drawString( "Press space (or A on controller) to start.", 100,400 );
        g.drawString( na.network_identity, 250, 450 );

        //g.drawImage(ResourceManager.getImage(Noodlearm.STARTUP_SCREEN_RES), 0, 0);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Noodlearm na = (Noodlearm)game;
		//Await user input to start the game (A press on game controller0)
		if (input.isKeyDown(Input.KEY_SPACE) || input.isButton1Pressed(Input.ANY_CONTROLLER)) {
            if ( na.network_identity.equals( "Server" ) ) {
                na.enterState(Noodlearm.PLAYINGSTATE, new EmptyTransition(), new HorizontalSplitTransition());
            } else {
                na.enterState(Noodlearm.CLIENTPLAYINGSTATE, new EmptyTransition(), new HorizontalSplitTransition());
            }
        }

        if ( input.isKeyDown(Input.KEY_C) ){
            na.network_identity = "Client";
        }

        if ( input.isKeyDown(Input.KEY_S) ){
            na.network_identity = "Server";
        }
    }
}
