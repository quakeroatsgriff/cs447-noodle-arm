package noodlearm;
import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import java.util.Random;
import java.io.*;
public class Weapon extends Entity{
    public Weapon(Grid grid_point, String type){
        super(grid_point.getX(), grid_point.getY());
    }
}
