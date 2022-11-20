package noodlearm;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import java.util.Random;
import java.io.*;
//A weapon that the player can equip. The player has a field for a weapon object.
//This is NOT to be confused with WeaponOnGround, which is an entity object on the
//screen that has a weapon object (this class) associated with it.
public class Weapon{
    public int damage;
    public int speed;
    public int range;
    public String type;
    public String texture;
    //Constructor. This basically acts as a factory constructor
    public Weapon(String type){
        this.type=type;
        switch(type){
            case "SWORD":
                this.createWeapon(2,500,1, Noodlearm.SWORD_RES);
                break;
            case "SPEAR":
                this.createWeapon(1, 1000, 3, Noodlearm.SPEAR_RES);
                break;
            case "CLUB":
                this.createWeapon(5,2000,1, Noodlearm.CLUB_RES);
                break;
        }

    }
    /**
     * Loads the weapon object's properties with the according stats
     * @param damage in hit points
     * @param speed in milliseconds
     * @param range in tiles
     * @param texture name of weapon texture 
     * 
     */
    private void createWeapon(int damage, int speed, int range, String texture){
        this.damage=damage;
        this.speed=speed;
        this.range=range;
        this.texture=texture;
    }
}