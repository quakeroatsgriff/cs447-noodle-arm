package noodlearm;

import jig.ResourceManager;
import jig.Shape;
import jig.Vector;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.*;

import java.io.File;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Stack;

public class PlayingState extends BasicGameState {
    WeaponSprite equipped;
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
        if(na.server==null){
            na.server = new Server( na );
            na.server.start();

            na.client = null;
        }
        initTestLevel( na );
        equipped = null;

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
        if(player==null || other_player==null)    return;
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
        for(Enemy enemy : na.enemies){
//            System.out.println(enemy.hit_points);
            //Remove any enemies from the world if their health is 0 or less
            if(enemy.hit_points > 0){
                enemy.translate(relative_coordinate_translation);
                enemy.render(g);
                enemy.translate( world_coordinate_translation );
            }
        }
        // we lock the weapon array when editing on this thread to avoid concurrency issues
        if(na.client != null)
            na.client.lock_weapon_array=true;
        // render weapons
        for(WeaponSprite ws : na.weapons_on_ground) {
            // adjust position relative to player, render and move back
            ws.translate( relative_coordinate_translation );
            ws.render(g);
            ws.translate( world_coordinate_translation );
        }
        if(na.client != null)
            na.client.lock_weapon_array=false;
        
        other_player.translate( relative_coordinate_translation );
        other_player.render(g);
        other_player.translate( world_coordinate_translation );

        // store position of player so we can move them back after render
        Vector old_postition = player.getPosition();
        // move player to center screen
        player.setPosition( na.ScreenWidth / 2.0f, na.ScreenHeight / 2.0f );
        // render player
        player.render(g);
        // move them back
        player.setPosition( old_postition );

        g.setColor(Color.darkGray);
        g.fillRect(0, 0, 600, 50);
        g.fillRect(0, 700, 100, 100);
        g.setColor(Color.white);
        if (player.weapon != null) {
            if (player.weapon.type.matches("SWORD")) {
                equipped = new WeaponSprite(50, 750, "SWORD");
                equipped.render(g);
            } else if (player.weapon.type.matches("SPEAR")) {
                equipped = new WeaponSprite(50, 750, "SPEAR");
                equipped.render(g);
            } else if (player.weapon.type.matches("CLUB")) {
                equipped = new WeaponSprite(50, 750, "CLUB");
                equipped.render(g);
            }
        }
        g.drawString("Enemies Remaining: " + na.enemies_alive, 200, 10);
        g.drawString("Health: " + player.hit_points, 500, 10);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        Noodlearm na = (Noodlearm)game;
        if(na.client_player == null || na.server_player == null) return;
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
        //Remove any enemies from the world if their health is 0 or less
        int enemies_alive=0;
        for(Enemy enemy : na.enemies)   {
            //Remove any enemies from the world if their health is 0 or less
            if(enemy.hit_points > 0){
                enemies_alive++;
                enemy.timeUpdate(na, delta);
                //Unhighlight any path tiles in order to prevent the same tile being lit up multiple times in a row.
                Stack<Integer> move_order = (Stack<Integer>)enemy.move_order.clone();
                while(!move_order.empty()){
                    Grid grid_point = na.grid.get(move_order.pop());
                    // grid_point.unhighlight(na,highlight_flag);
                }
                //Enemy will move or attack if the movement timer is up
                if(enemy.getRemainingTime() <= 0){
                    //If an enemy is within attack range of the targeted player, charge up attack
                    if(enemy.withinAttackRange(na.server_player) || enemy.withinAttackRange(na.client_player))  {
                        int direction = enemy.getDirectionToPlayer(na.server_player);
                        if(direction == -1) enemy.direction=enemy.getDirectionToPlayer(na.client_player);
                        else enemy.direction=direction;
                        //Charge up attack, or actually do the attack if the charge timer is up
                        if(enemy.chargeUpAttack(delta)){
                            na.server.send_server_enemy_ID(Integer.toString(enemy.ID));
                            enemy.attack(na);
                            na.server.send_server_enemy_attack();
                        }
                    }
                    else{
                        enemy.resetChargeUp();
                        na.server.send_server_enemy_ID(Integer.toString(enemy.ID));
                        //Determine the path the enemy should move in and also send the outcome to the client
                        
                        enemy.updateMoveOrder(na);
                        int next_grid_ID=enemy.getNextMove(na);
                        if(next_grid_ID != -1){
                            int direction=enemy.getMoveDirection(next_grid_ID);
                            na.server.send_server_enemy_direction(Integer.toString(direction));
                            enemy.move(na.grid.get(next_grid_ID), na.grid.get(enemy.grid_ID), direction);    
                            na.server.send_server_enemy_location(Integer.toString(next_grid_ID));
                        }
                    }
                }
            } else {
                na.server.send_server_enemy_ID(Integer.toString(enemy.ID));
                na.server.send_server_enemy_dead();
            }
            na.enemies_alive=enemies_alive;
        }
        // enemies_alive=0;
        //All enemies are destroyed
        if(enemies_alive==0)    {
            na.server.send_server_win_level();
            na.win_or_lose=true;
            na.enterState(Noodlearm.WINLOSESTATE, new EmptyTransition(), new HorizontalSplitTransition());
        }
        na.server_player.update(na, delta);
        na.client_player.update(na, delta);
        //One of the players died, lose game
        if(na.server_player.hit_points <= 0 || na.client_player.hit_points <= 0){
            na.server.send_server_lose_level();
            na.win_or_lose=false;
            na.enterState(Noodlearm.WINLOSESTATE, new EmptyTransition(), new HorizontalSplitTransition());
        }
        checkInput(input, na);
    }

    private void checkInput(Input input, Noodlearm na){
        //Get game controller object
        // Controller controller = Controller.getController(0);

        //Player moves left
        if (input.isKeyDown(Input.KEY_A) || input.isControllerLeft(Input.ANY_CONTROLLER)){
            //If the player is no longer doing an action
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
            //If the player is no longer doing an action
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
            //If the player is no longer doing an action
            if(na.server_player.getRemainingTime() <= 0){
                Grid new_location = na.grid.get(na.server_player.grid_ID + 48);
                Grid old_location = na.grid.get(na.server_player.grid_ID);
                if (na.server_player.move(new_location, old_location)) {
                    na.server.send_server_player_location(Integer.toString(new_location.getID()));
                }
                return;
            }
        }
        //Player moves Up
        if(input.isKeyDown(Input.KEY_W) || input.isControllerUp(Input.ANY_CONTROLLER)){
            //If the player is no longer doing an action
            if(na.server_player.getRemainingTime() <= 0){
                Grid new_location = na.grid.get(na.server_player.grid_ID - 48);
                Grid old_location = na.grid.get(na.server_player.grid_ID);
                if (na.server_player.move(new_location, old_location)) {
                    na.server.send_server_player_location(Integer.toString(new_location.getID()));
                }
                return;
            }
        }
        //Player uses light attack (X on controller)
        if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON) || input.isButton3Pressed(Input.ANY_CONTROLLER)){
            if(na.server_player.getRemainingTime() <= 0){
                na.server.send_light_attack();
                na.server_player.lightAttack(na);
                return; 
            }
        }
        //TODO
        //Player uses heavy attack (Y on controller)
        if(input.isMousePressed(Input.MOUSE_RIGHT_BUTTON) || input.isButtonPressed(3,Input.ANY_CONTROLLER)){
            if(na.server_player.getRemainingTime() <= 0){
                na.server.send_light_attack();
                na.server_player.lightAttack(na);
                return; 
            };
        }
        //Player switches weapons (B on controller)
        if(input.isKeyDown(Input.KEY_C) || input.isButton2Pressed(Input.ANY_CONTROLLER)){
            //Only switch if the player has at least 1 weapon
            if ( !na.server_player.weapon_inv.isEmpty() )
                na.server_player.changeWeapon();
            return;
        }
    }

    private void initTestLevel(Noodlearm na){

        // create a map generator, and generate a random map
        MapGenerator map_generator = new MapGenerator( 48, 48 );
        String map = map_generator.generate_map();

        // send the map to the client
        na.server.send_map( map );

        // Reset level by removing old grid
        na.grid.clear();
        // initialize variables
        Scanner sc = null;
        int grid_ID_counter = 0, x = 0, y = 0, enemy_ID_counter=0;

        // open a new scanner
        try {
            sc = new Scanner( map );
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
                        na.grid.add( new Grid( 0, x++, y, grid_ID_counter ) );
                        na.server_player = new Player( na.grid.get( grid_ID_counter++ ) );
                        break;
                    // a three means the client's player, so we add floor tile then player
                    case 3:
                        na.grid.add( new Grid( 0, x++, y, grid_ID_counter ) );
                        na.client_player = new Player( na.grid.get( grid_ID_counter++ ) );
                        break;
                    // a four means a sword sprite, so we add the floor then the sword sprite
                    case 4:
                        na.grid.add( new Grid( 0, x++, y, grid_ID_counter ) );
                        na.weapons_on_ground.add( new WeaponSprite( na.grid.get( grid_ID_counter++ ), "SWORD") );
                        break;
                    // a five means a spear sprite, so we add the floor then the spear sprite
                    case 5:
                        na.grid.add( new Grid( 0, x++, y, grid_ID_counter ) );
                        na.weapons_on_ground.add( new WeaponSprite( na.grid.get( grid_ID_counter++ ), "SPEAR") );
                        break;
                    // a six means a club sprite, so we add the floor then the club sprite
                    case 6:
                        na.grid.add( new Grid( 0, x++, y, grid_ID_counter ) );
                        na.weapons_on_ground.add( new WeaponSprite( na.grid.get( grid_ID_counter++ ), "CLUB") );
                        break;
                    // a seven means an enemy hound sprite, so we add the floor then the hound sprite
                    case 7:
                        na.grid.add( new Grid( 0, x++, y, grid_ID_counter ) );
                        na.enemies.add( new Enemy(na.grid.get( grid_ID_counter++ ), "HOUND", enemy_ID_counter++));
                        break;
                    // an eight means an enemy skeleton sprite, so we add the floor then the skeleton sprite
                    case 8:
                        na.grid.add( new Grid( 0, x++, y, grid_ID_counter ) );
                        na.enemies.add( new Enemy(na.grid.get( grid_ID_counter++ ), "SKELETON", enemy_ID_counter++));
                        break;
                    // regular tile
                    default:
                        na.grid.add( new Grid( tile_type, x++, y, grid_ID_counter++ ) );
                }
            }
            // reset column and increase row
            x = 0; y++;
        }
        sc.close();
    }
    
}
