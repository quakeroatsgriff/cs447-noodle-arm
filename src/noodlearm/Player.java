package noodlearm;

import java.util.ArrayList;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import org.newdawn.slick.Animation;

public class Player extends Entity {
    public int lives_left;
    public int grid_ID;
    private int action_timer;   //for movement and attacking
    private int sprite_update_timer;
    private int direction; //0 1 2 3 = left, right, forward (down), back (up)
    private boolean walking;
    private boolean attacking;
    private Vector velocity;
    private Weapon weapon;
    private int held_weapon_ID;
    private int weapon_count;
    private int weapon_switch_timer;
    public ArrayList<Weapon> weapon_inv;
    Animation left_walk_animation, right_walk_animation, down_walk_animation;

    public Player(Grid grid_point){
        super(grid_point.getX(),grid_point.getY());
        this.lives_left=3;
        this.action_timer=0;
        this.weapon_switch_timer=0;
        this.walking=false;
        this.attacking=false;
        this.grid_ID=grid_point.getID();
        this.held_weapon_ID=-1;
        this.weapon_count = -1;
        this.setScale((float) 0.125);
        //Can hold 3 weapons
        this.weapon_inv=new ArrayList<Weapon>(3);

        this.direction=2;
        this.velocity=new Vector(0,0);
        grid_point.setEntity("Player");
        addImageWithBoundingBox(ResourceManager.getImage(Noodlearm.KNIGHT_DOWN_FACE));
        this.left_walk_animation = new Animation(
                ResourceManager.getSpriteSheet(
                        Noodlearm.KNIGHT_LEFT_WALK_ANIMATION, 500, 500
                ),
                0, 0, 3,0, true,
                150, true
        );
        this.right_walk_animation = new Animation(
                ResourceManager.getSpriteSheet(
                        Noodlearm.KNIGHT_RIGHT_WALK_ANIMATION, 500, 500
                ),
                0, 0, 3,0, true,
                150, true
        );
        this.down_walk_animation = new Animation(
                ResourceManager.getSpriteSheet(
                        Noodlearm.KNIGHT_DOWN_WALK_ANIMATION, 500, 500
                ),
                0, 0, 3,0, true,
                150, true
        );
    }
    
    public void update(Noodlearm na, int delta){
        action_timer-=delta;
        sprite_update_timer-=delta;
        weapon_switch_timer-=delta;
        if(action_timer > 0)  {
            // updateSpriteWalking();
            translate(this.velocity.scale(delta));
            this.walking = true;
        }
        //Else, movement timer has ended. be stationary
        else {
            this.walking=false;
            // updateSpriteWalking();
            this.setX(na.grid.get(this.grid_ID).getX());
            this.setY(na.grid.get(this.grid_ID).getY());
            this.velocity = new Vector(0.0f,0.0f);
            //If the player was attacking for their action
            if(this.attacking){
                clearAllSprites();
                addImageWithBoundingBox(ResourceManager.getImage(this.weapon.texture));
                //Load weapon before loading player sprite
                addImageWithBoundingBox(ResourceManager.getImage(Noodlearm.KNIGHT_DOWN_FACE));

                this.attacking=false;
            }
        }

        if ( this.direction == 0 ) {
            if (this.walking) {
                change_player_sprite("left_walk");
            } else {
                change_player_sprite("left_still");
            }
        } else if ( this.direction == 1 ) {
            if (this.walking) {
                change_player_sprite("right_walk");
            } else {
                change_player_sprite("right_still");
            }
        } else if ( this.direction == 2 ) {
            if (this.walking) {
                change_player_sprite("down_walk");
            } else {
                change_player_sprite("down_still");
            }
        } else {
            change_player_sprite( "down_still" );
        }
    }
    

    public boolean move(Grid grid_point_new, Grid grid_point_old){
        float dir_x=1.0f,dir_y=1.0f;

        if ( grid_point_new.getID() == grid_point_old.getID() - 1 ) {
            this.direction = 0;
        } else if ( grid_point_new.getID() == grid_point_old.getID() + 1 ) {
            this.direction = 1;
        } else if ( grid_point_new.getID() == grid_point_old.getID() + 48 ) {
            this.direction = 2;
        } else if ( grid_point_new.getID() == grid_point_old.getID() - 48 ) {
            this.direction = 3;
        }
        //Update player sprite direction
        // changePlayerDirection(direction);
        switch(direction){
            //Left
            case Noodlearm.LEFT:
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
        if(this.action_timer > 0)     return false;
        grid_point_old.setEntity("");
        grid_point_new.setEntity("Player");
        this.grid_ID = grid_point_new.getID();
        //Gets the direction from the old to the new grid 'point'
        this.velocity = new Vector(dir_x * (float)(64.0f / 200.0f), dir_y * (float)(64.0f / 200.0f));
        //Set movement timer to 200 ms
        this.action_timer=200;
        return true;
    }

    public void pickupWeapon(WeaponSprite ws){
        this.weapon_inv.add(ws.weapon);
        this.weapon_count+=1;
        //If the weapon is the first one the player picked up
        if(this.held_weapon_ID == -1)   changeWeapon();
    }
    /**
     * Cycle to next weapon ID in inventory, or roll back to 1st ID (0) if 
     * at last weapon ID
     */
    public void changeWeapon(){
        //Don't change weapon if attacking or has switched recently
        if(this.weapon_switch_timer <= 0 && !this.attacking){
            this.held_weapon_ID = held_weapon_ID >= this.weapon_count ? 0 : held_weapon_ID+1;
            this.weapon=this.weapon_inv.get(held_weapon_ID);
            this.changeSprite();
            this.weapon_switch_timer=250;
        }
        return;
    }

    /**
     * Do light attack with current held weapon
     * @returns True if succesful attack, false if not successful
     */
    public boolean lightAttack(Noodlearm na){
        //Base case, player has no weapon
        if(this.weapon_count==-1)   return false;
        this.attacking=true;
        this.action_timer=this.weapon.speed;
        removeImage(ResourceManager.getImage(this.weapon.texture));
        //Add attacking weapon into world
        na.weapons_on_ground.add(new WeaponSprite(this, this.direction, weapon));
        return true;
    }

    /**
     * Changes sprite based on player's held weapon
     */
    private void changeSprite(){
        this.clearAllSprites();
        addImageWithBoundingBox(ResourceManager.getImage(this.weapon.texture));
        //Load weapon before loading player sprite
        addImageWithBoundingBox(ResourceManager.getImage(Noodlearm.KNIGHT_DOWN_FACE));

    }

    /**
     * I know this is janky, but it was the only way I could think of to ensure the proper
     * sprite is being removed no matter the scenerio the player is in.
     */
    private void clearAllSprites(){
        removeImage(ResourceManager.getImage(Noodlearm.SWORD_RES));
        removeImage(ResourceManager.getImage(Noodlearm.SPEAR_RES));
        removeImage(ResourceManager.getImage(Noodlearm.CLUB_RES));
        removeAnimation( this.down_walk_animation );
        removeImage(ResourceManager.getImage(Noodlearm.KNIGHT_DOWN_FACE));
        removeAnimation( this.left_walk_animation );
        removeImage( ResourceManager.getImage( Noodlearm.KNIGHT_LEFT_FACE ) );
        removeAnimation( this.right_walk_animation );
        removeImage( ResourceManager.getImage( Noodlearm.KNIGHT_RIGHT_FACE ) );
    }

    /**
     * @return movement timer
     */
    public int getRemainingTime(){
        return action_timer;
    }

    public void change_player_sprite( String direction ) {

        clearAllSprites();
        switch ( direction ) {
            case "left_walk":
                addAnimation(
                        this.left_walk_animation
                );
                break;
            case "left_still":
                addImageWithBoundingBox( ResourceManager.getImage( Noodlearm.KNIGHT_LEFT_FACE ) );
                break;
            case "right_walk":
                addAnimation(
                        this.right_walk_animation
                );
                break;
            case "right_still":
                addImageWithBoundingBox( ResourceManager.getImage( Noodlearm.KNIGHT_RIGHT_FACE ) );
                break;
            case "down_walk":
                addAnimation(
                        this.down_walk_animation
                );
                break;
            case "down_still":
                addImageWithBoundingBox( ResourceManager.getImage( Noodlearm.KNIGHT_DOWN_FACE ) );
                break;
            default:
                addImageWithBoundingBox( ResourceManager.getImage( Noodlearm.KNIGHT_DOWN_FACE ) );
        }
    }
}
