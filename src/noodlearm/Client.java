package noodlearm;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

/*
 Class for handling client networking logic. Operates on it's own thread,
 when kill_thread is called, both this thread and the server it's talking
 to exit.
* */
public class Client extends Thread {

    public String current_map = "";
    public Integer current_server_player_location = -1;
    public Integer current_client_player_location = -1;
    public boolean lock_weapon_array = false;

    // socket for communicating to server
    Socket socket;
    // output_stream for sending information to server
    PrintWriter output_stream;
    // input_stream for recieving information from server
    Scanner input_stream;
    Noodlearm na;

    public Client( Noodlearm na ) {

        this.na = na;

        // here we loop while we're attempting to connect to a server
        boolean scanning = true;
        while ( scanning ) {

            // we attempt connection
            try {
                this.socket = new Socket();
                this.socket.connect( new InetSocketAddress( "localhost", 1234 ) );
                scanning=false;
            // if we fail due to ConnectException,
            // we wait two seconds and try again
            } catch(ConnectException e) {
                System.out.println("Connect failed, waiting and trying again");
                try {
                    Thread.sleep(2000);//2 seconds
                } catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            // if we fail due to an IOException,
            // we crash
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // now that we have a connection we open I/O streams with server
        try {
            this.output_stream = new PrintWriter(socket.getOutputStream());
            this.input_stream = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // gets called when start() is invoked
    public void run() {

        // accept input from the server until our socket closes
        try {
            while ( this.input_stream.hasNextLine() ) {

                // we store next line
                String next_line = this.input_stream.nextLine();
                Integer weapon_grid_id;

                // switch statement for matching different networking keywords
                switch ( next_line ) {

                    // we were sent a map from the server
                    case "MAP_START":

                        // we build a string from incoming map data
                        StringBuilder map = new StringBuilder();
                        next_line = this.input_stream.nextLine();
                        while ( !next_line.equals( "MAP_END" ) ) {
                            map.append( next_line + "\n" );
                            next_line = this.input_stream.nextLine();
                        }

                        // store the map that was sent
                        current_map = map.toString();
                        break;

                    case "SERVER_PLAYER_LOC_START":
                        // we get new player location from the server
                        current_server_player_location = Integer.parseInt(this.input_stream.nextLine());
                        // we get rid of next line (should be PLAYER_LOC_END)
                        this.input_stream.nextLine();
                        break;

                    case "CLIENT_PLAYER_LOC_START":
                        // we get new player location from the server
                        current_client_player_location = Integer.parseInt(this.input_stream.nextLine());
                        // we get rid of next line (should be PLAYER_LOC_END)
                        this.input_stream.nextLine();
                        break;

                    case "SERVER_WEAPON_PICKUP_START":
                        weapon_grid_id = Integer.parseInt( this.input_stream.nextLine() );

                        while ( lock_weapon_array )
                            Thread.sleep( 5 );
                        for ( WeaponSprite ws : this.na.weapons_on_ground ) {
                            if ( ws.grid_ID == weapon_grid_id ) {
                                na.server_player.pickupWeapon(ws);
                                na.weapons_on_ground.remove(ws);
                                break;
                            }
                        }
                        this.input_stream.nextLine();
                        break;

                    case "CLIENT_WEAPON_PICKUP_START":
                        weapon_grid_id = Integer.parseInt( this.input_stream.nextLine() );

                        while ( lock_weapon_array )
                            Thread.sleep( 5 );
                        for ( WeaponSprite ws : this.na.weapons_on_ground ) {
                            if ( ws.grid_ID == weapon_grid_id ) {
                                na.client_player.pickupWeapon(ws);
                                na.weapons_on_ground.remove(ws);
                                break;
                            }
                        }
                        this.input_stream.nextLine();
                        break;

                    case "PLAYER_LIGHT_ATTACK_START":
                        na.server_player.lightAttack( na );
                        this.input_stream.nextLine();
                        break;

                    default:
                        System.out.println( "From Server: " + this.input_stream.nextLine() );
                }
            }
        } catch ( IllegalStateException e ) { // occurs when kill_thread is called
            return;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // clean up and print debug message
        System.out.println( "Client Quitting" );
        this.kill_thread();
    }

    // for sending a request to move the client player
    public void send_move_request( String old_location, String new_location ) {
        // we write the message to the output stream
        this.output_stream.println( "PLAYER_MOVE_START\n" + old_location + "\n" + new_location + "\nPLAYER_MOVE_END" );
        this.output_stream.flush();
    }

    public void send_light_attack() {
        this.output_stream.println( "PLAYER_LIGHT_ATTACK_START\nPLAYER_LIGHT_ATTACK_END" );
        this.output_stream.flush();
    }

    // to be called when we want to kill the thread, and break
    // networking connection
    public void kill_thread() {
        this.input_stream.close();
        this.output_stream.close();
        try {
            this.socket.close();
        } catch ( IOException e ) {
            throw new RuntimeException(e);
        }
    }
}
