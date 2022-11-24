package noodlearm;

import java.util.ArrayList;
import java.util.Random;

public class MapGenerator {

    int width, height;
    Random rand;

    public MapGenerator( int width, int height ) {
        this.width = width;
        this.height = height;
        this.rand = new Random();
    }

    public String generate_map() {

        ArrayList<Integer> map = new ArrayList<Integer>( this.width * this.height );
        for ( int i = 0; i < this.height * this.width; i++ ) {
            map.add( i, rand.nextInt( 2 ) );
        }

        map = new ArrayList<Integer>( cellular_automata_method( map ) );
        map = new ArrayList<Integer>( cellular_automata_method( map ) );
        map = new ArrayList<Integer>( cellular_automata_method( map ) );

        map.set( 0, 2 );
        map.set( 1, 3 );

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
                    // otherwise it stays alive
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
}
