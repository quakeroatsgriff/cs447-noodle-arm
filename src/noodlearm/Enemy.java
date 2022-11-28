package noodlearm;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

public class Enemy extends Entity {
    public int hit_points;
    public int ID;  //Enemy ID in the arraylist
    public int grid_ID;
    private int action_timer;   //for movement and attacking
    private int sprite_update_timer;
    private int charge_up_timer;
    private boolean walking;
    private boolean attacking;
    public String type;
    private Weapon weapon;
    public int direction; //0 1 2 3 = left, right, forward (down), back (up)
    private Vector velocity;
    public Stack<Integer> move_order;
    public int player_last_known_loc;

    public Enemy(Grid grid_point, String type, int ID){
        super(grid_point.getX(),grid_point.getY());
        this.setScale((float) 0.125);
        this.hit_points=1;
        this.action_timer=0;
        this.charge_up_timer=0;
        this.ID = ID;
        this.walking=false;
        this.attacking=false;
        this.grid_ID=grid_point.getID();
        this.direction=Noodlearm.DOWN;
        this.velocity=new Vector(0,0);
        this.move_order = new Stack<Integer>();
        this.player_last_known_loc=-1;
        this.type=type;
        grid_point.setEntity(type);
        grid_point.walkable = false;
        this.weapon = new Weapon(type);
        switch(type){
            case "HOUND":
                addImageWithBoundingBox(ResourceManager.getImage(Noodlearm.HOUND_RES));
                break;
            case "SKELETON":
                addImageWithBoundingBox(ResourceManager.getImage(Noodlearm.SKELETON_RES));
                break;
        }

    }

    /**
     * Moves the enemy in the given direction to a new grid tile
     * @param grid_point_new
     * @param grid_point_old
     * @returns true if successful move, false if no move made
     */
    public boolean move(Grid grid_point_new, Grid grid_point_old, int direction){
        //Invalid movement direction
        if(direction ==-1)  return false;
        float dir_x=1.0f,dir_y=1.0f;
        //Update enemy sprite direction
        // changeEnemyDirection(direction);
        switch(direction){
            case Noodlearm.LEFT:
                dir_x=-1.0f; dir_y=0.f;
                break;
            case Noodlearm.RIGHT:
                dir_x=1.0f;  dir_y=0.0f;
                break;
            case Noodlearm.DOWN:
                dir_x=0.0f;  dir_y=1.0f;
                break;
            case Noodlearm.UP:
                dir_x=0.0f;  dir_y=-1.0f;
                break;
        }
        //If the grid point cannot be moved onto
        if(!grid_point_new.walkable)    return false;
        if(this.action_timer > 0)     return false;
        grid_point_old.setEntity("");
        grid_point_new.setEntity(this.type);
        grid_point_old.walkable=true;
        grid_point_new.walkable=false;
        this.grid_ID = grid_point_new.getID();
        this.direction=direction;
        //Gets the direction from the old to the new grid 'point'
        this.velocity = new Vector(dir_x * (float)(64.0f / 250.0f), dir_y * (float)(64.0f / 250.0f));
         //Set movement timer to 250 ms
        this.action_timer=250;
        return true;
    }
    /**
     * Mainly updates timers wtih delta and adjust things that rely on time
     * @param na
     * @param delta
     */
    public void timeUpdate(Noodlearm na, int delta){
        action_timer-=delta;
        sprite_update_timer-=delta;
        if(action_timer > 0)  {
            // updateSpriteWalking();
            translate(this.velocity.scale(delta));
        }
        //Else, movement timer has ended. be stationary
        else {
            this.walking=false;
            this.setX(na.grid.get(this.grid_ID).getX());
            this.setY(na.grid.get(this.grid_ID).getY());
            this.velocity = new Vector(0.0f,0.0f);
        }
    }
    
    /**
     *  Updates the movement stack, or gets a new one if needed
     * @param na
     * @return true if movement stack was updated, false if not
     */
    public boolean updateMoveOrder(Noodlearm na){
        int closest_player_grid_ID=targetPlayer(na);
        //If the stack of grid tiles to move to is empty or the player has moved 
        if(this.move_order.empty() || this.player_last_known_loc != closest_player_grid_ID){
            //Get new path order stack
            this.move_order=this.pathToPlayer(na, na.grid.get(this.grid_ID),new ArrayList<Integer>(1),closest_player_grid_ID);
            this.player_last_known_loc = closest_player_grid_ID;
            return true;
        }
        return false;
    }
    /**
     * Determines the closest player to the enemy. Used to determine end point in A* pathfinding
     * @param na
     * @return the closest player's grid ID
     */
    private int targetPlayer(Noodlearm na){
        //Get euclidean distance from enemy to players
        float heuristic_server=this.getHeuristic(
            na.grid.get(this.grid_ID), na.grid.get(na.server_player.grid_ID));
        float heuristic_client=this.getHeuristic(
            na.grid.get(this.grid_ID), na.grid.get(na.client_player.grid_ID));
        //Determine which of the two distances is shorter
        if(heuristic_client < heuristic_server) return na.client_player.grid_ID;
        else                                    return na.server_player.grid_ID;
    }
    /**
     * Gets the next tile direction to move to
     * @param na
     * @return grid ID to move to
     */
    public int getNextMove(Noodlearm na){
        //Base case, no movement in stack
        if(this.move_order == null || this.move_order.isEmpty()){
            return -1;
        }
        return (this.move_order.pop());
    }
    /**
     * Determines the direction to move in given a tile
     * @param next_grid_ID to move to
     * @return direction to move in 
     */
    public int getMoveDirection(int next_grid_ID){
    // if(this.grid_ID!=na.player.grid_ID){
        if(next_grid_ID+1 == this.grid_ID) return Noodlearm.LEFT;
        if(next_grid_ID-1 == this.grid_ID) return Noodlearm.RIGHT;
        if(next_grid_ID-48 == this.grid_ID) return Noodlearm.DOWN;
        if(next_grid_ID+48 == this.grid_ID) return Noodlearm.UP;
        //Base case, no valid directional movement. If everything goes according to plan, this shouldn't 
        //have to be returned ever
        return -1;
    }

    /**
     * Part of the update loop for enemies. Calculates the path to take to the player's position using A*
     * @param na
     * @param start_grid_node
     * @param closed_list Nodes that have already been traveled to
     * @param player_grid_ID The grid tile the player is on
     */
    public Stack<Integer> pathToPlayer(Noodlearm na, Grid start_grid_node, ArrayList<Integer> closed_list, int player_grid_ID){
        Stack<Integer> new_order = new Stack<Integer>();
        Grid cheapest_node=new Grid(-1);
        ArrayList<Integer> grid_paths = new ArrayList<Integer>(4);
        //We can just skip the traversal if we are already at the player tile
        if(start_grid_node.getID()==player_grid_ID){
            new_order.push(start_grid_node.getID());
            return new_order;
        }
        else{
            // //Left,right,up,down paths
            grid_paths.add(start_grid_node.getID()-1);
            grid_paths.add(start_grid_node.getID()+1);
            grid_paths.add(start_grid_node.getID()-48);
            grid_paths.add(start_grid_node.getID()+48);
            for (int path : grid_paths){
                if(na.grid.get(path).walkable ){
                    float heuristic=getHeuristic(na.grid.get(path), na.grid.get(player_grid_ID));
                    if((na.grid.get(path).travel_cost + heuristic < cheapest_node.travel_cost)
                    && !closed_list.contains(path)){
                        cheapest_node.travel_cost=na.grid.get(path).travel_cost + heuristic;
                        cheapest_node.setID(path);
                    }
                }
            }
            //If somehow no path was chosen, just return the current stack as it is now
            if(cheapest_node.getID() == -1){
                return null;
            }else{
                ArrayList<Integer> closed=(ArrayList<Integer>)closed_list.clone();
                closed.add(cheapest_node.getID());
                new_order = pathToPlayer(na, na.grid.get(cheapest_node.getID()), closed, player_grid_ID);
                if(new_order == null)   return new Stack<Integer>();
                new_order.push(start_grid_node.getID());
            }
        }
        return new_order;
    }
    /**
     * Calculates heuristic distance from enemy to player
     * @param enemy_grid
     * @param player_grid
     * @return heuristic distance in float
     */
    private float getHeuristic(Grid enemy_grid, Grid player_grid){
        float x = player_grid.getX() - enemy_grid.getX();
        float y = player_grid.getY() - enemy_grid.getY();
        return (float) Math.sqrt((x*x) + (y*y));
    }
    
    /**
     * Determines if the enemy is in attack range to the targeted player
     * @param player
     * @return true if within attack range, false if not
     */
    public boolean withinAttackRange(Player player){
        //Enemy attack range
        int ar = this.weapon.range;
        return (this.grid_ID-ar == player.grid_ID || this.grid_ID+ar == player.grid_ID ||
        this.grid_ID-(48*ar) == player.grid_ID || this.grid_ID+(48*ar) == player.grid_ID);
    }
    /**
     * Get direction to face in when within attack range of aplyaer
     * @param player
     * @return int direction
     */
    public int getDirectionToPlayer(Player player){
        //Enemy attack range
        int ar = this.weapon.range;
        
        if(this.grid_ID-ar == player.grid_ID)               return Noodlearm.LEFT;
        else if(this.grid_ID+ar == player.grid_ID)          return Noodlearm.RIGHT;
        else if(this.grid_ID-(48*ar) == player.grid_ID)     return Noodlearm.UP;
        else if(this.grid_ID+(48*ar) == player.grid_ID)     return Noodlearm.DOWN;
        return -1;
    }
    /**
    * Updates the timer to charge up an attack and determine if the attack is ready
    * @param delta
    * @return true if ready to attack, false if not ready
    */
    public boolean chargeUpAttack(int delta){
        this.charge_up_timer+=delta;
        return (this.charge_up_timer >= this.weapon.speed);
    }
    
    /**
     * Do attack with weapon stats
     * @returns True if succesful attack, false if not successful
     */
    public boolean attack(Noodlearm na){
        //Base case, player has no weapon
        this.attacking=true;
        // this.action_timer=this.weapon.speed;
        //Add attacking weapon into world
        na.weapons_on_ground.add(new WeaponSprite(this, this.direction, weapon));
        this.resetChargeUp();
        return true;
    }

    /*
     * Reset timer on charge up timer. Indicates that the enemy is no longer in range of attacking
     */
    public void resetChargeUp(){
        this.charge_up_timer=0;
    }
    /**
     * @return movement timer
     */
    public int getRemainingTime(){
        return action_timer;
    }
}
