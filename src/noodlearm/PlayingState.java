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
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;
import jig.Vector;

public class PlayingState extends BasicGameState {
    @Override
    public int getID() {
        return Noodlearm.PLAYINGSTATE;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {

    }

    @Override
	public void enter(GameContainer container, StateBasedGame game) {
        Noodlearm na = (Noodlearm)game;
        initTestLevel(na);
    }
    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Noodlearm na = (Noodlearm)game;
        for(Grid grid_cell : na.grid)  {
            //Debug draw grid tile numbers
            // g.drawString(""+grid_cell.getID(), grid_cell.getX()-16, grid_cell.getY()-16);
            //Grid textures
            grid_cell.render(g);
        };
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Noodlearm na = (Noodlearm)game;
    }

    //TODO Use nate's way of making maps from file
    private void initTestLevel(Noodlearm na){
        //Reset level by removing old grid
        na.grid.clear();
        Scanner sc=null;
        int ID_counter=0;
        try{
            sc = new Scanner(new File("src/noodlearm/res/grids/test-grid.txt"));
        } catch(Exception CannotOpenFile) {
            CannotOpenFile.printStackTrace();
        }
        for(int x=0; x<12; x++){
            for(int y=0; y<12; y++){
                try{
                    if(sc.hasNext()){
                        //Insert grid object into array list
                        na.grid.add(new Grid(Integer.parseInt(sc.next()),x,y,ID_counter++));
                    }
                }catch(NullPointerException e){ e.printStackTrace();}
            }
        }
        sc.close();
    }
}
