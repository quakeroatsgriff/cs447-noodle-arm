package noodlearm;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import java.util.Random;
import java.io.*;
//A weapon that the player can equip. The player has a field for a weapon object.
//This is NOT to be confused with WeaponSprite, which is an entity object on the
//screen that has a weapon object (this class) associated with it.
public class Weapon{
    public int damage;
    public int speed;
    public int range;
    public int rotation_amount;
    public String type;
    public String texture;
    //Constructor. This basically acts as a factory constructor
    public Weapon(String type){
        this.type=type;
        switch(type){
            case "SWORD":
                this.createWeapon(2,250,1, -45, Noodlearm.SWORD_RES);
                break;
            case "SPEAR":
                this.createWeapon(1, 500, 2, 45, Noodlearm.SPEAR_RES);
                break;
            case "CLUB":
                this.createWeapon(5,750,1, -45, Noodlearm.CLUB_RES);
                break;
        }

    }
    /**
     * Loads the weapon object's properties with the according stats
     * @param damage in hit points
     * @param speed in milliseconds
     * @param range in tiles
     * @param rotation_amount in degrees to turn the weapon to face right
     * @param texture name of weapon texture 
     * 
     */
    private void createWeapon(int damage, int speed, int range, int rotation_amount, String texture){
        this.damage = damage;
        this.speed = speed;
        this.range = range;
        this.rotation_amount = rotation_amount;
        this.texture = texture;
    }

    static public boolean pickupWeapon( WeaponSprite ws, Noodlearm na, Player player ) {
        //If player is on the same tile as a weapon on the ground, equip and remove the weapon from the world
        if((ws.grid_ID == player.grid_ID && !ws.attacking)){
            player.pickupWeapon(ws);
            //Remove weapon sprite from world
            na.weapons_on_ground.remove(ws);
            return true;
        }
        return false;
    }

    static public boolean attackTimer( WeaponSprite ws, Noodlearm na, int delta ) {
        if (ws.attacking) {
            ws.update(na, delta);
            //If an attacking weapon's timer has reached 0, remove the weapon from the world
            if (ws.attacking_timer <= 0) {
                na.weapons_on_ground.remove(ws);
                return true;
            }
        }
        return false;
    }
}