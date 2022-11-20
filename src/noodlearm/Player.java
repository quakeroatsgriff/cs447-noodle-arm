package noodlearm;

import java.util.ArrayList;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
public class Player extends Entity {
    public int lives_left;
    public int grid_ID;
    private int movement_timer;
    private int sprite_update_timer;
    private String direction;
    private boolean walking;
    private Vector velocity;
    private Weapon weapon;
    private int held_weapon_ID;
    private int weapon_count;
    public ArrayList<Weapon> weapon_inv;

    public Player(Grid grid_point){
        super(grid_point.getX(),grid_point.getY());
        this.lives_left=3;
        this.movement_timer=0;
        this.walking=false;
        this.grid_ID=grid_point.getID();
        this.held_weapon_ID=0;
        this.weapon_count = -1;
        this.setScale((float) 0.125);
        //Can hold 3 weapons
        this.weapon_inv=new ArrayList<Weapon>(3);

        //this.direction=Pushover.PLAYER_F_RES;
        this.velocity=new Vector(0,0);
        grid_point.setEntity("Player");
        // addImageWithBoundingBox(ResourceManager.getImage(Noodlearm.PLAYER_F_RES));
        addImageWithBoundingBox(ResourceManager.getImage(Noodlearm.KNIGHT_FORWARD_RES));

    }
    
    public void update(Noodlearm na, int delta){
        movement_timer-=delta;
        sprite_update_timer-=delta;
        if(movement_timer > 0)  {
            // updateSpriteWalking();
            translate(this.velocity.scale(delta));
        }
        //Else, movement timer has ended. be stationary
        else {
            this.walking=true;
            // updateSpriteWalking();
            this.setX(na.grid.get(this.grid_ID).getX());
            this.setY(na.grid.get(this.grid_ID).getY());
            this.velocity = new Vector(0.0f,0.0f);
        }
    }
    

    public boolean move(Grid grid_point_new, Grid grid_point_old, int direction){
        float dir_x=1.0f,dir_y=1.0f;
        //Update player sprite direction
        // changePlayerDirection(direction);
        switch(direction){
            //Left
            case 0:
                dir_x=-1.0f; dir_y=0.f;
                break;
            //Right
            case 1:
                dir_x=1.0f;  dir_y=0.0f;
                break;
            //Down (Forward)
            case 2:
                dir_x=0.0f;  dir_y=1.0f;
                break;
            //Up
            case 3:
                dir_x=0.0f;  dir_y=-1.0f;
                break;
        }
        //If the grid point cannot be moved onto
        if(!grid_point_new.walkable)    return false;
        if(this.movement_timer > 0)     return false;
        grid_point_old.setEntity("");
        grid_point_new.setEntity("Player");
        this.grid_ID = grid_point_new.getID();
        //Gets the direction from the old to the new grid 'point'
        this.velocity = new Vector(dir_x * (float)(32.0f / 200.0f), dir_y * (float)(32.0f / 200.0f));
        //Set movement timer to 200 ms
        this.movement_timer=200;
        return true;
    }

    public void pickupWeapon(WeaponSprite ws){
        this.weapon_inv.add(ws.weapon);
        this.weapon_count+=1;
    }
    /**
     * Cycle to next weapon ID in inventory, or roll back to 1st ID (0) if 
     * at last weapon ID
     */
    public void changeWeapon(){
        this.held_weapon_ID = held_weapon_ID > this.weapon_count ? 0 : held_weapon_ID+1;
        this.weapon=this.weapon_inv.get(held_weapon_ID);
        this.changeSprite();
    }
    /**
     * Changes sprite based on player's held weapon
     */
    private void changeSprite(){

    }
    /**
     * @return movement timer
     */
    public int getRemainingTime(){
        return movement_timer;
    }
}
