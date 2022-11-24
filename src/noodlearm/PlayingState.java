package noodlearm;

import jig.Vector;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.File;
import java.util.Scanner;

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
        // create and start server thread
        na.server = new Server( na );
        na.server.start();

        initTestLevel( na );
        na.server.send_map( "1 1 1 1 1 1 1 1 1 1 1 1\n" +
                "1 0 0 0 0 0 0 0 0 0 0 1\n" +
                "1 0 0 0 0 0 0 0 0 0 0 1\n" +
                "1 0 0 0 0 0 0 0 0 0 0 1\n" +
                "1 0 0 0 0 0 0 0 0 0 0 1\n" +
                "1 0 0 0 0 0 0 0 0 0 0 1\n" +
                "1 0 0 0 0 0 0 0 0 0 0 1\n" +
                "1 0 0 0 0 0 0 0 0 0 0 1\n" +
                "1 0 2 0 0 0 0 0 0 3 0 1\n" +
                "1 0 0 0 0 0 0 0 0 0 0 1\n" +
                "1 0 0 0 0 0 0 0 0 0 0 1\n" +
                "1 1 1 1 1 1 1 1 1 1 1 1" );

        // simple echo server demonstration

        // Scanner input = new Scanner( System.in );
        // System.out.println( "\nEnter your message, Ctrl+D to stop correspondence.");
        // while ( input.hasNextLine() ) {
        //     na.client.send( input.nextLine() );
        // }
    }
    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        Noodlearm na = (Noodlearm)game;

        Vector old_postition = na.server_player.getPosition();

        for(Grid grid_cell : na.grid)  {
            //Debug draw grid tile numbers
            // g.drawString(""+grid_cell.getID(), grid_cell.getX()-16, grid_cell.getY()-16);
            //Grid textures

            grid_cell.translate( -na.server_player.getX() + na.ScreenWidth / 2.0f, -na.server_player.getY() + na.ScreenHeight / 2.0f );
            grid_cell.render(g);
            grid_cell.translate( na.server_player.getX() - na.ScreenWidth / 2.0f, na.server_player.getY() - na.ScreenHeight / 2.0f );
        }
        na.server_player.setPosition( na.ScreenWidth / 2.0f, na.ScreenHeight / 2.0f );
        na.server_player.render(g);
        na.server_player.setPosition( old_postition );
        for(WeaponSprite ws : na.weapons_on_ground) ws.render(g);

        na.client_player.render(g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Noodlearm na = (Noodlearm)game;
        //If player is on the same tile as a weapon on the ground, equip and remove the weapon from the world
        for(WeaponSprite ws : na.weapons_on_ground)    {
            if((ws.grid_ID == na.server_player.grid_ID && !ws.attacking)){
                na.server_player.pickupWeapon(ws);
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
        na.server_player.update(na, delta);
        na.client_player.update(na, delta);
        checkInput(input, na);
    }

    private void checkInput(Input input, Noodlearm na){
        //Get game controller object
        // Controller controller = Controller.getController(0);

        //Player moves left
        if (input.isKeyDown(Input.KEY_A) || input.isControllerLeft(Input.ANY_CONTROLLER)){
            //If the player is still frozen from moving the boulder
            if (na.server_player.getRemainingTime() <= 0){
                Grid new_location = na.grid.get(na.server_player.grid_ID - 1);
                Grid old_location = na.grid.get(na.server_player.grid_ID);
                if (na.server_player.move(new_location, old_location)) {
                    na.server.send_server_player_location(Integer.toString(new_location.getID()));
                }
                return;
            }
        }
        //Player moves Right
        if(input.isKeyDown(Input.KEY_D) || input.isControllerRight(Input.ANY_CONTROLLER)){
            //Move boulder to right if it's there
            if(na.server_player.getRemainingTime() <= 0){
                Grid new_location = na.grid.get(na.server_player.grid_ID + 1);
                Grid old_location = na.grid.get(na.server_player.grid_ID);
                if (na.server_player.move(new_location, old_location)) {
                    na.server.send_server_player_location(Integer.toString(new_location.getID()));
                }
                return;
            }
        }
        //Player moves Down
        if(input.isKeyDown(Input.KEY_S) || input.isControllerDown(Input.ANY_CONTROLLER)){
            if(na.server_player.getRemainingTime() <= 0){
                Grid new_location = na.grid.get(na.server_player.grid_ID + 12);
                Grid old_location = na.grid.get(na.server_player.grid_ID);
                if (na.server_player.move(new_location, old_location)) {
                    na.server.send_server_player_location(Integer.toString(new_location.getID()));
                }
                return;
            }
        }
        //Player moves Up
        if(input.isKeyDown(Input.KEY_W) || input.isControllerUp(Input.ANY_CONTROLLER)){
            //Move boulder to right if it's there
            if(na.server_player.getRemainingTime() <= 0){
                Grid new_location = na.grid.get(na.server_player.grid_ID - 12);
                Grid old_location = na.grid.get(na.server_player.grid_ID);
                if (na.server_player.move(new_location, old_location)) {
                    na.server.send_server_player_location(Integer.toString(new_location.getID()));
                }
                return;
            }
        }
        //TODO
        //Player uses light attack (X on controller)
        if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON) || input.isButton3Pressed(Input.ANY_CONTROLLER)){
            if(na.server_player.getRemainingTime() <= 0){
                na.server_player.lightAttack(na);
                return; 
            }
        }
        //TODO
        //Player uses heavy attack (Y on controller)
        if(input.isMousePressed(Input.MOUSE_RIGHT_BUTTON) || input.isButtonPressed(3,Input.ANY_CONTROLLER)){
            if(na.server_player.getRemainingTime() <= 0){
                na.server_player.lightAttack(na);
                return; 
            };
        }
        //TODOs
        //Player switches weapons (B on controller)
        if(input.isKeyDown(Input.KEY_C) || input.isButton2Pressed(Input.ANY_CONTROLLER)){
            na.server_player.changeWeapon();
            return;
        }
    }

    private void initTestLevel(Noodlearm na){
        // Reset level by removing old grid
        na.grid.clear();
        // initialize variables
        Scanner sc = null;
        int ID_counter = 0, x = 0, y = 0;

        // open a new scanner
        try {
            sc = new Scanner( new File( "src/noodlearm/res/grids/level_one.txt" ) );
            // split file by line
            sc.useDelimiter( "\n" );
        } catch( Exception CannotOpenFile ) {
            CannotOpenFile.printStackTrace();
        }

        // for each line in file
        while ( sc.hasNext() ) {
            // for each char in line
            for ( String b : sc.next().split( " " ) ) {
                // parse int from string
                int tile_type = Integer.parseInt( b );
                // we can encode specialty tiles here
                switch ( tile_type ) {
                    // a two means the server's player, so we add floor tile then player
                    case 2:
                        na.grid.add( new Grid( 0, x++, y, ID_counter ) );
                        na.server_player = new Player( na.grid.get( ID_counter++ ) );
                        break;
                    // a three means the client's player, so we add floor tile then player
                    case 3:
                        na.grid.add( new Grid( 0, x++, y, ID_counter ) );
                        na.client_player = new Player( na.grid.get( ID_counter++ ) );
                        break;
                    // regular tile
                    default:
                        na.grid.add( new Grid( tile_type, x++, y, ID_counter++ ) );
                }
            }
            // reset column and increase row
            x = 0; y++;
        }
        sc.close();

        na.weapons_on_ground.add(new WeaponSprite(na.grid.get(33), "SWORD"));
        na.weapons_on_ground.add(new WeaponSprite(na.grid.get(45), "CLUB"));
        na.weapons_on_ground.add(new WeaponSprite(na.grid.get(57), "SPEAR"));
    }
    
}
