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

        na.client = null;

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

        Player player = null, other_player = null;

        if ( na.network_identity.matches( "Server" ) ) {
            player = na.server_player;
            other_player = na.client_player;
        } else if ( na.network_identity.matches( "Client" ) ) {
            player = na.client_player;
            other_player = na.server_player;
        } else {
            System.err.println( "Fatal Error: Network identity unknown." );
            System.exit( -1 );
        }

        // amount we need to translate objects to keep player on center of screen
        Vector relative_coordinate_translation = new Vector(
                -player.getX() + na.ScreenWidth / 2.0f,
                -player.getY() + na.ScreenHeight / 2.0f
        );

        // negation of relative_server_player_coordinates, saves function calls
        Vector world_coordinate_translation = relative_coordinate_translation.negate();

        for(Grid grid_cell : na.grid)  {
            //Debug draw grid tile numbers
            // g.drawString(""+grid_cell.getID(), grid_cell.getX()-16, grid_cell.getY()-16);
            //Grid textures

            // adjust position relative to player, render and move back
            grid_cell.translate( relative_coordinate_translation );
            grid_cell.render(g);
            grid_cell.translate( world_coordinate_translation );
        }

        // adjust position relative to player, render and move back
        other_player.translate( relative_coordinate_translation );
        other_player.render(g);
        other_player.translate( world_coordinate_translation );

        // we lock the weapon array when editing on this thread to avoid concurrency issues
        if ( na.client != null)
            na.client.lock_weapon_array = true;
        // render weapons
        for(WeaponSprite ws : na.weapons_on_ground) {
            // adjust position relative to player, render and move back
            ws.translate( relative_coordinate_translation );
            ws.render(g);
            ws.translate( world_coordinate_translation );
        }
        if ( na.client != null)
            na.client.lock_weapon_array = false;

        // store position of player so we can move them back after render
        Vector old_postition = player.getPosition();
        // move player to center screen
        player.setPosition( na.ScreenWidth / 2.0f, na.ScreenHeight / 2.0f );
        // render player
        player.render(g);
        // move them back
        player.setPosition( old_postition );
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Noodlearm na = (Noodlearm)game;

        for ( WeaponSprite ws : na.weapons_on_ground ) {
            if ( Weapon.attackTimer( ws, na, delta ) )
                break;
            if ( Weapon.pickupWeapon(ws, na, na.server_player ) ) {
                na.server.send_server_weapon_pickup_notice( Integer.toString( ws.grid_ID ) );
                break;
            }
            if ( Weapon.pickupWeapon( ws, na, na.client_player ) ) {
                na.server.send_client_weapon_pickup_notice( Integer.toString( ws.grid_ID ) );
                break;
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
            if ( !na.server_player.weapon_inv.isEmpty() )
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
