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
    public Server() {}

    // gets called when start() is invoked
    public void run() {

        // setup server_socket, socket, and I/O streams
        try {
            this.server_socket = new ServerSocket( 1234 );
            this.socket = this.server_socket.accept();
            this.input_stream = new Scanner( socket.getInputStream() );
            this.output_stream = new PrintWriter( socket.getOutputStream() );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

        // loop while we're still receiving information from the client
        try {
            // right now we're just echoing back to the client
            while ( input_stream.hasNextLine() ) {
                output_stream.println( input_stream.nextLine() );
                output_stream.flush();
            }
        } catch ( IllegalStateException e ) { // called when socket closes
            return;
        }

        // clean up and print debug message
        System.out.println( "Server Quitting" );
        this.kill_thread();
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
