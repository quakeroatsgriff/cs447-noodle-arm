package noodlearm;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import java.util.Iterator;
import java.util.Scanner;

public class ClientPlayingState extends PlayingState {
    @Override
    public int getID() {
        return Noodlearm.CLIENTPLAYINGSTATE;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {

    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        Noodlearm na = (Noodlearm)game;

        // create and start client thread
        if(na.client==null){
            na.client = new Client( na );
            na.client.start();
        }
        // here we check if the server sent us a map,
        // if they haven't we sleep for one second and check again
        while ( na.client.current_map.equals( "" ) ) {
            try {
                Thread.sleep( 200 );
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        
        initTestLevel( na );

    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Noodlearm na = (Noodlearm)game;
        if(na.client.current_client_player_location == null || na.client.current_server_player_location == null
        || na.client_player == null || na.server_player == null) return;
        if ( na.client.current_server_player_location != na.server_player.grid_ID & na.client.current_server_player_location != -1 ) {
            na.server_player.move(na.grid.get(na.client.current_server_player_location), na.grid.get(na.server_player.grid_ID));
        }
        if ( na.client.current_client_player_location != na.client_player.grid_ID & na.client.current_client_player_location != -1 ) {
            na.client_player.move(na.grid.get(na.client.current_client_player_location), na.grid.get(na.client_player.grid_ID));
        }
        // we lock the weapon array when editing on this thread to avoid concurrency issues
        while ( na.client.lock_weapon_array) {
            try {
                Thread.sleep( 5 );
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        for ( WeaponSprite ws : na.weapons_on_ground ) {
            if ( Weapon.attackTimer( ws, na, delta ) )
                break;
            //Iterate through all the enemies and players and determine if the weapon sprite shares
            //the same grid ID. Deal damage to the entity if that's the case
            for(Enemy enemy : na.enemies){
                if(enemy.grid_ID == ws.grid_ID) {
                    ws.dealDamage(na, enemy);
                    break;
                }
            }
            if(na.client_player.grid_ID == ws.grid_ID){
                ws.dealDamage(na, na.client_player);
            }
            else if(na.server_player.grid_ID == ws.grid_ID){
                ws.dealDamage(na, na.server_player);
            }
        }
        int enemies_alive=0;
        //Iterate through each enemy and determine if they need to be moved
        if(!na.client.enemies.isEmpty()){
            //Remove any enemies from the world if their health is 0 or less
            for(int i=0; i < na.client.enemies.size(); i++) {
                Enemy client_enemy = na.client.enemies.get(i);
                if(client_enemy.hit_points > 0){
                    enemies_alive++;
                    client_enemy.timeUpdate(na, delta);
                    if(client_enemy.grid_ID != client_enemy.grid_ID){
                        client_enemy.move(na.grid.get(client_enemy.grid_ID), na.grid.get(client_enemy.grid_ID), client_enemy.direction);
                    }
                }
            }
        }
        na.enemies_alive=enemies_alive;

        na.server_player.update(na, delta);
        na.client_player.update(na, delta);
        checkInput( input, na );
    }

    private void checkInput(Input input, Noodlearm na){
        //Get game controller object
        // Controller controller = Controller.getController(0);

        //Player moves left
        if(input.isKeyDown(Input.KEY_A) || input.isControllerLeft(Input.ANY_CONTROLLER)){
            //If the player is no longer doing an action
            if(na.client_player.getRemainingTime() <= 0){
                Grid new_location = na.grid.get(na.client_player.grid_ID - 1);
                Grid old_location = na.grid.get(na.client_player.grid_ID);
                na.client.send_move_request( Integer.toString( old_location.getID() ), Integer.toString( new_location.getID() ) );
                return;
            }
        }
        //Player moves Right
        if(input.isKeyDown(Input.KEY_D) || input.isControllerRight(Input.ANY_CONTROLLER)){
            //If the player is no longer doing an action
            if(na.client_player.getRemainingTime() <= 0){
                Grid new_location = na.grid.get(na.client_player.grid_ID + 1);
                Grid old_location = na.grid.get(na.client_player.grid_ID);
                na.client.send_move_request( Integer.toString( old_location.getID() ), Integer.toString( new_location.getID() ) );
                return;
            }
        }
        //Player moves Down
        if(input.isKeyDown(Input.KEY_S) || input.isControllerDown(Input.ANY_CONTROLLER)){
            //If the player is no longer doing an action
            if(na.client_player.getRemainingTime() <= 0){
                Grid new_location = na.grid.get(na.client_player.grid_ID + 48);
                Grid old_location = na.grid.get(na.client_player.grid_ID);
                na.client.send_move_request( Integer.toString( old_location.getID() ), Integer.toString( new_location.getID() ) );
                return;
            }
        }
        //Player moves Up
        if(input.isKeyDown(Input.KEY_W) || input.isControllerUp(Input.ANY_CONTROLLER)){
            //If the player is no longer doing an action
            if(na.client_player.getRemainingTime() <= 0){
                Grid new_location = na.grid.get(na.client_player.grid_ID - 48);
                Grid old_location = na.grid.get(na.client_player.grid_ID);
                na.client.send_move_request( Integer.toString( old_location.getID() ), Integer.toString( new_location.getID() ) );
                return;
            }
        }
        //Player uses light attack (X on controller)
        if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON) || input.isButton3Pressed(Input.ANY_CONTROLLER)){
            //If the player is no longer doing an action
            if(na.client_player.getRemainingTime() <= 0){
                na.client.send_light_attack();
                na.client_player.lightAttack(na);
                return;
            }
        }
        //TODO
        //Player uses heavy attack (Y on controller)
        if(input.isMousePressed(Input.MOUSE_RIGHT_BUTTON) || input.isButtonPressed(3,Input.ANY_CONTROLLER)){
            //If the player is no longer doing an action
            if(na.client_player.getRemainingTime() <= 0){
                na.client.send_light_attack();
                na.client_player.lightAttack(na);
                return;
            };
        }
        //Player switches weapons (B on controller)
        if(input.isKeyDown(Input.KEY_C) || input.isButton2Pressed(Input.ANY_CONTROLLER)){
            //Only switch if the player has at least 1 weapon
            if ( !na.client_player.weapon_inv.isEmpty() )
                na.client_player.changeWeapon();
            return;
        }
    }

    private void initTestLevel(Noodlearm na){
        // Reset level by removing old grid
        na.grid.clear();
        // initialize variables
        Scanner sc = null;
        int ID_counter = 0, x = 0, y = 0, enemy_ID_counter = 0;

        // open a new scanner
        sc = new Scanner( na.client.current_map );
        // split string by line
        sc.useDelimiter( "\n" );

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
                    // a four means a sword sprite, so we add the floor then the sword sprite
                    case 4:
                        na.grid.add( new Grid( 0, x++, y, ID_counter ) );
                        na.weapons_on_ground.add( new WeaponSprite( na.grid.get( ID_counter++ ), "SWORD") );
                        break;
                    // a five means a spear sprite, so we add the floor then the spear sprite
                    case 5:
                        na.grid.add( new Grid( 0, x++, y, ID_counter ) );
                        na.weapons_on_ground.add( new WeaponSprite( na.grid.get( ID_counter++ ), "SPEAR") );
                        break;
                    // a six means a club sprite, so we add the floor then the club sprite
                    case 6:
                        na.grid.add( new Grid( 0, x++, y, ID_counter ) );
                        na.weapons_on_ground.add( new WeaponSprite( na.grid.get( ID_counter++ ), "CLUB") );
                        break;
                    case 7:
                        na.grid.add( new Grid( 0, x++, y, ID_counter ) );
                        na.enemies.add( new Enemy(na.grid.get( ID_counter++ ), "HOUND", enemy_ID_counter++));
                        break;
                    // an eight means an enemy skeleton sprite, so we add the floor then the skeleton sprite
                    case 8:
                        na.grid.add( new Grid( 0, x++, y, ID_counter ) );
                        na.enemies.add( new Enemy(na.grid.get( ID_counter++ ), "SKELETON", enemy_ID_counter++));
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
        na.client.enemies=na.enemies;
    }

}
