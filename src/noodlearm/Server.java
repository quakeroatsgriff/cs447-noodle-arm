package noodlearm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/*
 Class for handling server networking logic. Operates on it's own thread,
 when kill_thread is called, both this thread and the client it's talking
 to exit.
* */
public class Server extends Thread {

    // server_socket for networking initialization
    ServerSocket server_socket;
    // socket for I/O with client
    Socket socket;
    // stream for input from client
    Scanner input_stream;
    // stream for output to client
    PrintWriter output_stream;

    Noodlearm na;

    public Server( Noodlearm na ) {
        // setup server_socket, socket, and I/O streams
        try {
            this.server_socket = new ServerSocket( 1234 );
            this.socket = this.server_socket.accept();
            this.input_stream = new Scanner( socket.getInputStream() );
            this.output_stream = new PrintWriter( socket.getOutputStream() );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        this.na = na;
    }

    // gets called when start() is invoked
    public void run() {

        // loop while we're still receiving information from the client
        // right now we're just echoing back to the client
        while ( input_stream.hasNextLine() ) {

            // we store next line
            String next_line = this.input_stream.nextLine();

            // switch statement for matching different networking keywords
            switch ( next_line ) {

                case "PLAYER_MOVE_START":
                    Grid old_location = na.grid.get( Integer.parseInt( this.input_stream.nextLine() ) );
                    Grid new_location = na.grid.get( Integer.parseInt( this.input_stream.nextLine() ) );
                    if (na.client_player.move( new_location, old_location)) {
                        na.server.send_client_player_location( Integer.toString( new_location.getID() ) );
                    }
                    this.input_stream.nextLine();
                    break;

                default:
                    System.out.println( "From Client: " + this.input_stream.nextLine() );
            }
        }

        // clean up and print debug message
        System.out.println( "Server Quitting" );
        this.kill_thread();
    }

    public void send_map( String map ) {

        // create a new scanner to run on input string
        Scanner sc = new Scanner( map );
        // split it by space and newline
        sc.useDelimiter( " \n" );
        // we send MAP_START and MAP_END as keywords for parsing map data
        this.output_stream.println( "MAP_START\n" + sc.next() + "\nMAP_END" );
        this.output_stream.flush();
        sc.close();
    }

    public void send_server_player_location( String location ) {
        this.output_stream.println( "SERVER_PLAYER_LOC_START\n" + location + "\nSERVER_PLAYER_LOC_END" );
        this.output_stream.flush();
    }

    public void send_client_player_location( String location ) {
        this.output_stream.println( "CLIENT_PLAYER_LOC_START\n" + location + "\nCLIENT_PLAYER_LOC_END" );
        this.output_stream.flush();
    }

    public void send_server_weapon_pickup_notice( String grid_ID ) {
        this.output_stream.println( "SERVER_WEAPON_PICKUP_START\n" + grid_ID + "\nSERVER_WEAPON_PICKUP_END" );
        this.output_stream.flush();
    }

    public void send_client_weapon_pickup_notice( String grid_ID ) {
        this.output_stream.println( "CLIENT_WEAPON_PICKUP_START\n" + grid_ID + "\nCLIENT_WEAPON_PICKUP_END" );
        this.output_stream.flush();
    }

    // to be called when we want to kill this thread and stop networking
    public void kill_thread() {
        input_stream.close();
        output_stream.close();
        try {
            socket.close();
            server_socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
