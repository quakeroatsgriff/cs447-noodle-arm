package noodlearm;

import org.newdawn.slick.util.pathfinding.*;

import java.util.ArrayList;
import java.util.Random;

public class MapGenerator {

    int width, height;
    Random rand;
    TileBasedMap tile_map;
    AStarPathFinder path_finder;
    ArrayList<Integer> map;

    public MapGenerator( int width, int height ) {
        this.width = width;
        this.height = height;
        this.rand = new Random();
        this.tile_map = new TileBasedMap() {
            @Override
            public int getWidthInTiles() { return width; }
            @Override
            public int getHeightInTiles() { return height; }
            @Override
            public void pathFinderVisited(int i, int i1) {}
            @Override
            public boolean blocked(PathFindingContext pathFindingContext, int x, int y) { return map.get( ( y * height ) + x ) == 1; }
            @Override
            public float getCost(PathFindingContext pathFindingContext, int i, int i1) { return 1.0f; }
        };
        this.path_finder = new AStarPathFinder(
                this.tile_map,
                width * height,
                false
        );
    }

    public String generate_map() {

        // flip a coin for each tile in the grid to see if it's a wall or floor
        map = new ArrayList<Integer>( this.width * this.height );
        for ( int i = 0; i < this.height * this.width; i++ ) {
            map.add( i, rand.nextInt( 2 ) );
        }

        // run cellular automata rules 10 times to make cave-like map
        for ( int i = 0; i < 10; i++ )
            map = new ArrayList<Integer>( cellular_automata_method( map ) );

        // place the players in the middle of the map
        map.set( ( ( this.height / 2 ) * this.height ) + ( this.width / 2 ) , 2 );
        map.set( ( ( this.height / 2 ) * this.height ) + ( this.width / 2 ) + 1 , 3 );

        // add border around map
        for ( int x = 0; x < this.width; x++ ) {
            map.set( x , 1 );
            map.set( ( ( this.height - 1)  * this.height ) + x , 1 );
        }
        for ( int y = 0; y < this.height * ( this.height - 1 ); y += this.height ) {
            map.set( y , 1 );
            map.set( ( this.width - 1 ) + y , 1 );
        }

        // place three swords, spears, and clubs randomly around the map
        for ( int i = 0; i < 3; i++ ) {
            map.set( this.reachable_location(), 4 ); // sword
            map.set( this.reachable_location(), 5 ); // spear
            map.set( this.reachable_location(), 6 ); // club
        }

        // convert the map from an array to a string
        StringBuilder string_builder = new StringBuilder();
        for ( int y = 0; y < this.height; y++ ) {
            for ( int x = 0; x < this.width; x++ ) {
                if ( x != this.width - 1 )
                    string_builder.append( map.get( ( y * this.height ) + x ) + " " );
                else
                    string_builder.append( map.get( ( y * this.height ) + x ) );
            }
            if ( y != this.height - 1 )
                string_builder.append( "\n" );
        }
        return string_builder.toString();
    }

    // method that runs rules on an array_list to change the terrain of a map, returns an altered version
    private ArrayList<Integer> cellular_automata_method( ArrayList<Integer> map ) {
        Integer north, east, south, west, this_cell, this_cell_index;
        for ( int y = 0; y < this.height; y++ ) {
            for ( int x = 0; x < this.width; x++ ) {
                this_cell_index = ( y * this.height ) + x;
                this_cell = map.get( this_cell_index );

                north = ( y != 0 )               ? map.get( ( ( y - 1 ) * this.height ) + ( x ) )     : 0;
                east  = ( x != this.width - 1 )  ? map.get( ( ( y )     * this.height ) + ( x + 1 ) ) : 0;
                south = ( y != this.height - 1 ) ? map.get( ( ( y + 1 ) * this.height ) + ( x ) )     : 0;
                west  = ( x != 0 )               ? map.get( ( ( y )     * this.height ) + ( x - 1 ) ) : 0;

                // if this cell is alive
                if ( this_cell == 1 ) {
                    // if this cell has less than two living neighbors
                    if ( north + east + south + west < 2 ) {
                        // then this cell dies
                        map.set( this_cell_index, 0 );
                    // if this cell has more than three living neighbors
                    } else if ( north + east + south > 3 ) {
                        // then this cell dies
                        map.set( this_cell_index, 0 );
                    // if this cell has two or three living neighbors
                    }
                    // then this cell stays alive
                // if this cell is dead
                } else {
                    if ( north + east + south + west == 3 ) {
                        // then this cell becomes alive
                        map.set( this_cell_index, 1 );
                    }
                    // otherwise it stays dead
                }
            }
        }
        return map;
    }

    private int reachable_location() {
        // we choose a random point
        int x = this.rand.nextInt( this.width );
        int y = this.rand.nextInt( this.height );

        // we look for a path from player start to that point
        Path path = this.path_finder.findPath( null, this.width / 2, this.height / 2, x, y );

        // if and when there is no path, we choose random points until we choose a spot where there is a path
        while ( path == null ) {
            x = this.rand.nextInt( this.width );
            y = this.rand.nextInt( this.height );
            path = this.path_finder.findPath( null, this.width / 2, this.height / 2, x, y );
        }

        // we return a coordinate that's guaranteed to be reachable
        return ( y * this.height ) + x;
    }
}
