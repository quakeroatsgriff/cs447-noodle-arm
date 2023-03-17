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

public class WinLoseState extends BasicGameState {
    private int timer=5000;
    @Override
    public int getID() {
        return Noodlearm.WINLOSESTATE;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {

    }

    @Override
	public void enter(GameContainer container, StateBasedGame game) {
        Noodlearm na = (Noodlearm)game;
        timer=4000;
        if(na.network_identity.equals("Client")){
            timer=3000;
        }
    }
    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {

        Noodlearm na = (Noodlearm)game;
        //TODO
        g.drawString("Your score: "+ (na.player_score),200,600);
        if(na.win_or_lose)  g.drawImage(ResourceManager.getImage(Noodlearm.WIN_SCREEN_RES), 50, 0);
        else    g.drawImage(ResourceManager.getImage(Noodlearm.GAMEOVER_SCREEN_RES), 50, 0);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Noodlearm na = (Noodlearm)game;
        timer-=delta;
        if(timer<0){
            if(na.network_identity.equals("Server"))
                na.enterState(Noodlearm.PLAYINGSTATE, new EmptyTransition(), new HorizontalSplitTransition());
            else
                na.enterState(Noodlearm.CLIENTPLAYINGSTATE, new EmptyTransition(), new HorizontalSplitTransition());                
        }
    }
}
