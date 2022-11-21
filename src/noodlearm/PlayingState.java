package noodlearm;

import jig.ResourceManager;
import org.lwjgl.Sys;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EmptyTransition;
import org.newdawn.slick.state.transition.HorizontalSplitTransition;
import java.io.File;
import java.io.PrintWriter;
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

        // simple echo server demonstration
        Scanner input = new Scanner( System.in );
        System.out.println( "\nEnter your message, Ctrl+D to stop correspondence.");
        //TODO
        // while ( input.hasNextLine() ) {
        //     na.client.send( input.nextLine() );
        // }
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
        for(WeaponSprite ws : na.weapons_on_ground) ws.render(g);
        na.player.render(g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Noodlearm na = (Noodlearm)game;
        //If player is on the same tile as a weapon on the ground, equip and remove the weapon from the world
        for(WeaponSprite ws : na.weapons_on_ground)    {
            if((ws.grid_ID == na.player.grid_ID && !ws.attacking)){
                na.player.pickupWeapon(ws);
                //Remove weapon sprite from world
                na.weapons_on_ground.remove(ws);
                break;
            }
            if(ws.attacking){
                ws.update(na, delta);
                //If an attacking weapon's timer has reached 0, remove the weapon from the world
                if(ws.attacking_timer <=0){
                    na.weapons_on_ground.remove(ws);
                    break;
                }
            }
        }
        na.player.update(na, delta);
        checkInput(input, na);
    }

    private void checkInput(Input input, Noodlearm na){
        //Get game controller object
        // Controller controller = Controller.getController(0);

        //Player moves left
        if(input.isKeyDown(Input.KEY_A) || input.isControllerLeft(Input.ANY_CONTROLLER)){
            //If the player is still frozen from moving the boulder
            if(na.player.getRemainingTime() <= 0){
                na.player.move((na.grid.get(na.player.grid_ID-12)),na.grid.get(na.player.grid_ID),0);
                return;
            }
        }
        //Player moves Right
        if(input.isKeyDown(Input.KEY_D) || input.isControllerRight(Input.ANY_CONTROLLER)){
            //Move boulder to right if it's there
            if(na.player.getRemainingTime() <= 0){
                na.player.move((na.grid.get(na.player.grid_ID+12)),na.grid.get(na.player.grid_ID),1);
                return;
            }
        }
        //Player moves Down
        if(input.isKeyDown(Input.KEY_S) || input.isControllerDown(Input.ANY_CONTROLLER)){
            if(na.player.getRemainingTime() <= 0){
                na.player.move((na.grid.get(na.player.grid_ID+1)),na.grid.get(na.player.grid_ID),2);
                return;
            }
        }
        //Player moves Up
        if(input.isKeyDown(Input.KEY_W) || input.isControllerUp(Input.ANY_CONTROLLER)){
            //Move boulder to right if it's there
            if(na.player.getRemainingTime() <= 0){
                na.player.move((na.grid.get(na.player.grid_ID-1)),na.grid.get(na.player.grid_ID),3);
                return;
            }
        }
        //TODO
        //Player uses light attack (X on controller)
        if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON) || input.isButton3Pressed(Input.ANY_CONTROLLER)){
            if(na.player.getRemainingTime() <= 0){
                na.player.lightAttack(na);
                return; 
            }
        }
        //TODO
        //Player uses heavy attack (Y on controller)
        if(input.isMousePressed(Input.MOUSE_RIGHT_BUTTON) || input.isButtonPressed(3,Input.ANY_CONTROLLER)){
            if(na.player.getRemainingTime() <= 0){
                na.player.lightAttack(na);
                return; 
            };
        }
        //TODOs
        //Player switches weapons (B on controller)
        if(input.isKeyDown(Input.KEY_C) || input.isButton2Pressed(Input.ANY_CONTROLLER)){
            na.player.changeWeapon();
            return;
        }
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
        //Init player location
        na.player = new Player(na.grid.get(32));
        na.weapons_on_ground.add(new WeaponSprite(na.grid.get(33), "SWORD"));
        na.weapons_on_ground.add(new WeaponSprite(na.grid.get(45), "CLUB"));
        na.weapons_on_ground.add(new WeaponSprite(na.grid.get(57), "SPEAR"));
    }
    
}
