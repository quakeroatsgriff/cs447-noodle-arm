package noodlearm;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/*
 Class for handling client networking logic. Operates on it's own thread,
 when kill_thread is called, both this thread and the server it's talking
 to exit.
* */
public class Client extends Thread {

    // socket for communicating to server
    Socket socket;
    // output_stream for sending information to server
    PrintWriter output_stream;
    // input_stream for recieving information from server
    Scanner input_stream;

    public Client() {}

    // gets called when start() is invoked
    public void run() {

        // create connection socket, then create I/O objects from that
        try {
            this.socket = new Socket( "localhost", 1234 );
            this.output_stream = new PrintWriter( socket.getOutputStream() );
            this.input_stream = new Scanner( socket.getInputStream() );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // accept input from the server until our socket closes
        try {
            while ( this.input_stream.hasNextLine() ) {
                System.out.println( "Response: " + this.input_stream.nextLine() );
            }
        } catch ( IllegalStateException e ) { // occurs when kill_thread is called
            return;
        }

        // clean up and print debug message
        System.out.println( "Client Quitting" );
        this.kill_thread();
    }

    // for sending a string to the server
    public void send( String message ) {
        // we write the message to the output stream
        this.output_stream.println( message );
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
