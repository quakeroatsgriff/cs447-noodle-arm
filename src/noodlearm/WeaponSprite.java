package noodlearm;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import jig.ResourceManager;
import java.util.Random;
import java.io.*;

//This is like the weapon class but extends the entity class. It is used to have 
//a sprite image in the overworld
public class WeaponSprite extends Entity{
    public Weapon weapon;
    public int grid_ID;
    public boolean attacking;
    public int attacking_timer;
    private int direction;
    private boolean halfway_flag;
    private boolean dealt_damage;
    //Constructor for a weapon laying on the ground
    public WeaponSprite(Grid grid_point, String type){
        super(grid_point.getX(), grid_point.getY());
        this.attacking = false;
        this.weapon = new Weapon(type);
        this.grid_ID=grid_point.getID();
        this.setScale((float) 0.125);
        addImageWithBoundingBox(ResourceManager.getImage(weapon.texture));
    }

    //Constructor for a weapon that is attacking from a player
    public WeaponSprite(Player player, int direction, Weapon weapon){
        super(player.getX(), player.getY());
        this.addImageWithBoundingBox(ResourceManager.getImage(weapon.texture));
        this.setScale((float) 0.125);
        this.weapon=weapon;
        this.grid_ID = player.grid_ID;
        this.attacking=true;
        this.attacking_timer=weapon.speed;
        this.direction=direction;
        this.halfway_flag=false;
        this.dealt_damage=false;
        //Determine which direction to swing in for swords or clubs
        if(weapon.type=="CLUB" || weapon.type=="SWORD"){
            switch(direction){
                case Noodlearm.LEFT:
                    //The angle in degres the weapon should end on when turning
                    this.setRotation(weapon.rotation_amount + 135);
                    this.setX(this.getX()-48);
                    this.setY(this.getY()+32);
                    break;
                case Noodlearm.RIGHT:
                    this.setRotation(weapon.rotation_amount - 45);
                    this.setX(this.getX()+48);
                    this.setY(this.getY()-32);
                    break;
                case Noodlearm.DOWN:
                    this.setRotation(weapon.rotation_amount + 45);
                    this.setX(this.getX()+32);
                    this.setY(this.getY()+64);
                    break;
                case Noodlearm.UP:
                    this.setRotation(weapon.rotation_amount + 225);
                    this.setX(this.getX()-32);
                    this.setY(this.getY()-64);
                    break;
            }
        }
        //Set weapon sprite grid tile relative to direction of attack
        switch(direction){
            case Noodlearm.LEFT:
                    this.grid_ID-=1;
                    break;
                case Noodlearm.RIGHT:
                    this.grid_ID+=1;
                    break;
                case Noodlearm.DOWN:
                    this.grid_ID+=48;
                    break;
                case Noodlearm.UP:
                    this.grid_ID-=48;
                    break;
        }
    }
    
    //Constructor for a weapon that is attacking from an enemy
    public WeaponSprite(Enemy enemy, int direction, Weapon weapon){
        super(enemy.getX(), enemy.getY());
        this.addImageWithBoundingBox(ResourceManager.getImage(weapon.texture));
        this.setScale((float) 0.125);
        this.weapon=weapon;
        this.grid_ID = enemy.grid_ID;
        this.attacking=true;
        this.attacking_timer=weapon.speed;
        this.direction=direction;
        this.halfway_flag=false;
        this.dealt_damage=false;
        //Determine which direction to attack in for swords or clubs
        if(weapon.type=="HOUND" || weapon.type=="SKELETON"){
            switch(direction){
                case Noodlearm.LEFT:
                    //The angle in degres the weapon should end on when turning
                    this.setRotation(weapon.rotation_amount + 135);
                    this.setX(this.getX()-48);
                    this.setY(this.getY()+32);
                    this.grid_ID-=1;
                    break;
                case Noodlearm.RIGHT:
                    this.setRotation(weapon.rotation_amount - 45);
                    this.setX(this.getX()+48);
                    this.setY(this.getY()-32);
                    this.grid_ID+=1;
                    break;
                case Noodlearm.DOWN:
                    this.setRotation(weapon.rotation_amount + 45);
                    this.setX(this.getX()+32);
                    this.setY(this.getY()+64);
                    this.grid_ID+=48;
                    break;
                case Noodlearm.UP:
                    this.setRotation(weapon.rotation_amount + 225);
                    this.setX(this.getX()-32);
                    this.setY(this.getY()-64);
                    this.grid_ID-=48;
                    break;
            }
        }
    }

    public void update(Noodlearm na, int delta){
        this.attacking_timer -= delta;
        if(this.weapon.type == "SPEAR")     updateSpear(delta);
        else                                updateSwordClub(delta);
        
    }
    
    /**
     * Update sprite position of swords or clubs to show the playing swinging it.
     * @param delta
     */
    private void updateSwordClub(int delta){
        //Adjust sprite location based on rotation
        switch(this.direction){
            case Noodlearm.LEFT:
                this.translate(new Vector(0,-(delta*64)/(this.weapon.speed)));
                break;
            case Noodlearm.RIGHT:
                this.translate(new Vector(0,(delta*64)/(this.weapon.speed)));
                break;
            case Noodlearm.DOWN:
                this.translate(new Vector(-(delta*64)/(this.weapon.speed), 0));
                break;
            case Noodlearm.UP:
                this.translate(new Vector((delta*64)/(this.weapon.speed), 0));
                break;
        }
        //Rotate sprite to make it look like it's being swung
        double rotation_amount=(delta*90.0) / (this.weapon.speed);
        this.rotate(rotation_amount);
        return;
    }

    /**
     * Update sprite position of spears. Spears move in a thrust motion instead of swinging.
     * @param delta
     */
    private void updateSpear(int delta){
        switch(this.direction){
            case Noodlearm.LEFT:
                this.translate(new Vector(-(delta*128)/(this.weapon.speed),0));
                this.setRotation(weapon.rotation_amount + 180);
                break;
            case Noodlearm.RIGHT:
                this.translate(new Vector((float)((delta*128)/(this.weapon.speed)),0));
                this.setRotation(weapon.rotation_amount);
                break;
            case Noodlearm.DOWN:
                this.translate(new Vector(0,(delta*128)/(this.weapon.speed)));
                this.setRotation(weapon.rotation_amount + 90);
                break;
            case Noodlearm.UP:
                this.translate(new Vector(0,-(delta*128)/(this.weapon.speed)));
                this.setRotation(weapon.rotation_amount + 270);
                break;
        }
        //Change grid ID to next tile halfway through animation
        if((this.attacking_timer <= this.weapon.speed/2) && !this.halfway_flag){
            this.halfway_flag=true;
            switch(this.direction){
                case Noodlearm.LEFT:
                    this.grid_ID-=1;
                    break;
                case Noodlearm.RIGHT:
                    this.grid_ID+=1;
                    break;
                case Noodlearm.DOWN:
                    this.grid_ID+=48;
                    break;
                case Noodlearm.UP:
                    this.grid_ID-=48;
                    break;
            }
        }
    }

    /**
     * Deals damage to the player standing on the weapon sprite tile
     * @param na
     * @param player
     * @return
     */
    public boolean dealDamage(Noodlearm na, Player player){
        //Weapons just laying on the ground should not deal damage.
        if(!this.attacking)      return false;
        if(this.dealt_damage)   return false;
        player.hit_points -= this.weapon.damage;
        //Only deal damage once per weapon sprite
        this.dealt_damage = true;
        return true;
    }
    /**
     * Deals damage to the enemy standing on the weapon sprite tile
     * @param na
     * @param enemy
     * @return
     */
    public boolean dealDamage(Noodlearm na, Enemy enemy){
        //Weapons just laying on the ground should not deal damage.
        if(!this.attacking)      return false;
        if(this.dealt_damage)   return false;
        enemy.hit_points -= this.weapon.damage;
        //Free up tile space the enemy is on
        if(enemy.hit_points <= 0)   {
            na.grid.get(enemy.grid_ID).walkable=true;
            enemy.removeImage(ResourceManager.getImage(enemy.texture));
        }
        //Only deal damage once per weapon sprite
        this.dealt_damage = true;
        return true;
    }
}
