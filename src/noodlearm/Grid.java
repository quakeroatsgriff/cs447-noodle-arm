package noodlearm;

import jig.Entity;
import jig.ResourceManager;
import jig.Vector;

public class Grid extends Entity{
    private String entity;
    private int type;
    private int ID;
    public boolean walkable;
    public float travel_cost;
    //Blank grid point to be a placeholder
    public Grid(int ID){
        this.ID=ID;
        this.travel_cost=Integer.MAX_VALUE;
    }
    //Regular grid tile in generation
    public Grid(int texture_type, int x_pos, int y_pos, int ID){
        //Multiply grid's position with the initializor iterator
        super(x_pos*64,y_pos*64);
        this.entity="";
        this.ID=ID;
        this.type=texture_type;
        this.setScale((float) 0.125);
        switch(texture_type){
            //Wall
            case 1:
            //Border
            case 2:
                addImageWithBoundingBox(ResourceManager.getImage(Noodlearm.WALL_RES));
                this.walkable=false;
                //The walkable checker makes sure you can't walk on a wall, so
                //a cost of -1 is ok.
                this.travel_cost=-1.0f;
                break;
            //Tile with no special properties, anything can move on it.
            default:
                addImageWithBoundingBox(ResourceManager.getImage(Noodlearm.BLANK_RES));
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
    
    public int getType(){
        return this.type;
    }

    /**
     * Removes the highlighted texture from the grid
     * @param pushover
     * @param highlight_flag
     */
    public void unhighlight(Noodlearm na){
        removeImage(ResourceManager.getImage(Noodlearm.PATH_HIGHLIGHT_RES));
    }
    
    public void highlight(){
        addImageWithBoundingBox(ResourceManager.getImage(Noodlearm.PATH_HIGHLIGHT_RES));
    }
}