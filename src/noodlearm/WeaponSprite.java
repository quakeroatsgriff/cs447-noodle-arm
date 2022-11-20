package noodlearm;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import java.util.Random;
import java.io.*;

//This is like the weapon class but extends the entity class. It is used to have 
//a sprite image in the overworld
public class WeaponSprite extends Entity{
    public Weapon weapon;
    public WeaponSprite(Grid grid_point, String type){
        super(grid_point.getX(), grid_point.getY());
        weapon = new Weapon(type);
        addImageWithBoundingBox(ResourceManager.getImage(weapon.texture));
    }
}
