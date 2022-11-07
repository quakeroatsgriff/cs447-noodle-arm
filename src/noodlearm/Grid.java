package noodlearm;

import jig.Entity;
import jig.ResourceManager;
import jig.Vector;

public class Grid extends Entity{
    private String entity;
    private String type;
    private int ID;
    public boolean walkable;
    public float travel_cost;
    //Blank grid point to be a placeholder
    public Grid(int ID){
        this.ID=ID;
        this.travel_cost=Integer.MAX_VALUE;
    }
    //Regular grid tile in generation
    public Grid(String texture_type, int x_pos, int y_pos, int ID){
        //Multiply grid's position with the initializor iterator plus a screen offset.
        super(x_pos*32+24,y_pos*32+64);
        this.entity="";
        this.ID=ID;
        this.type=texture_type;
        switch(texture_type){
            case "WALL":
                //addImageWithBoundingBox(ResourceManager.getImage(Pushover.WALL_RES));
                this.walkable=false;
                //The walkable checker makes sure you can't walk on a wall, so
                //a cost of -1 is ok.
                this.travel_cost=-1.0f;
                break;
            //Tile with no special properties, anything can move on it.
            default:
                // addImageWithBoundingBox(ResourceManager.getImage(Pushover.BLANK_RES));
                this.walkable=true;
                this.travel_cost=1.0f;
                break;
        }
    }

    public int getID(){
        return this.ID;
    }
    public void setID(int ID){
        this.ID=ID;
    }

    /**
     * Sets Sets the entity of the grid tile
     * @param entity
     */
    public void setEntity(String entity){
        this.entity=entity;
    }

    public String getEntity(){
        return this.entity;
    }
    
    public String getType(){
        return this.type;
    }
}