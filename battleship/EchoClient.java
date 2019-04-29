import java.io.*;
import java.net.*;

public class EchoClient {
    public static void main(String[] args) throws IOException {

        if (args.length != 4) {
            System.err.println("Usage: java EchoClient <host name> <port number> <username> <room number>");
            System.exit(1);
        }

        String hostName = args[0]; // get host name
        int portNumber = Integer.parseInt(args[1]); // get port number
        String username = args[2];
        String room = args[3];
        try (Socket echoSocket = new Socket(hostName, portNumber); // Connect socket to given host name and port number
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true); // Get output stream of socket
                                                                                       // pass it into a PrintWriter so
                                                                                       // we can write to the server
                BufferedReader stdIn = // Get input stream from standard in (Keyboard input from console)
                        new BufferedReader(new InputStreamReader(System.in))) {
            String userInput;
            int channel;
            try {
                channel = Integer.parseInt(room);
            } catch (NumberFormatException nfe) {
                System.out.println("Your room was not a valid number. Please re-enter your room number.");
                while (true) {
                    userInput = stdIn.readLine();
                    if (userInput == null) {
                        out.println(userInput);
                        System.out.println("You have disconnected from the server.");
                        System.exit(0);
                    }
                    if (userInput.equals("N")) {
                        System.out.println("Thank you for coming, even though you didn't get to play.");
                        System.exit(0);
                    }
                    try {
                        channel = Integer.parseInt(userInput);
                        break;
                    } catch (NumberFormatException nfex) {
                    }
                }
            }
            ServerListener serverListener = new ServerListener(echoSocket); // Initialize a thread to listen for input
                                                                            // from server
            out.println(channel + " " + username);
            serverListener.start();
            while ((userInput = stdIn.readLine()) != null) { // Wait for input from standard in
                out.println(userInput);
            }
            out.println(userInput); // print out one null line to let server know user disconnected


        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
}

/*
 * Extended thread class to handle a while loop waiting for input from server"
 */
class ServerListener extends Thread {
    Socket socket;

    ServerListener(Socket socket) {
        this.socket = socket;
    }
    public void run() {
        // Input stream from server
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String serverInput;
            while ((serverInput = in.readLine()) != null) {
                System.out.println(serverInput); // Print input from server into console
            }
            // if input is null, client is no longer connected with server
            System.out.println("You have disconnected from the server.");
            System.exit(0);
        } catch (IOException ie) {
        }
    }
}