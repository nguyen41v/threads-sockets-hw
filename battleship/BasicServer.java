
import java.net.*;
import java.io.*;
import java.util.*;

public class BasicServer {
    public static void main(String[] args) throws IOException {
        HashMap<Integer, Client[]> rooms = new HashMap<>();
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
        int portNumber = Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0])); // Listen to port on Server for incoming connections
        ) {
            while (true) { // Loop for continuously listening for new incoming connections and make new rooms/start games
                Socket clientSocket = serverSocket.accept(); // Connection is handed over from listening port to a different port
                InitialConnection newConnection = new InitialConnection(clientSocket, rooms);
                newConnection.start();
            }
        } catch (IOException e) {
            System.out.println(
                    "Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}

class InitialConnection extends Thread {
    Socket clientSocket;
    HashMap<Integer, Client[]> rooms;

    InitialConnection(Socket clientSocket, HashMap<Integer, Client[]> rooms) {
        this.clientSocket = clientSocket;
        this.rooms = rooms;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream())); // Grab input stream from socket
            PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            String input;
            Client client;
            int room;
            out.println(
                    "Please enter in the room you want to connect to and your username in the form of <room number> <username>.");
            while (true) {
                input = in.readLine();
                if (input == null) {
                    out.println("You have disconnected from the server.");
                    return;
                }
                if (input.equals("N")) {
                    out.println("Thank you for coming, even though you didn't get to play.");
                    return;
                }
                try {
                    // first line contains info about the room and name of client
                    String[] information = input.split(" ");
                    room = Integer.parseInt(information[0]);
                    client = new Client(clientSocket, information[1]);
                    break;
                } catch (Exception e) {
                    out.println(
                            "Your input was invalid.\nPlease enter in the room you want to connect to and your username in the form of <room number> <username>.");
                }
            }
            // check if room exists (aka, if another player(s) is in the room)
            Client[] roomList;
            synchronized(this.rooms) {
                if (this.rooms.containsKey(room)) {
                    roomList = this.rooms.get(room);
                    // if no second player, current player becomes the second player
                    if (roomList[1] == null) {
                        roomList[1] = client;
                        client.out.println("You joined room " + Integer.toString(room) + " with " + roomList[0].name + ".");
                        Battleship game = new Battleship(roomList, rooms, room); // make new Battleship thread and start
                        game.start();
                    // if room is full, current player goes through new thread to connect to another room
                    } else {
                        Connect connect = new Connect(client, this.rooms, false);
                        connect.start();
                    }
                // if room doesn't exist (aka no players in room), make room
                } else {
                    roomList = new Client[2];
                    roomList[0] = client;
                    this.rooms.put(room, roomList);
                    client.out.println("Your room number is " + Integer.toString(room) + ". Waiting for another player to join to play Battleship.");
                } 
            }
        } catch (IOException ie) {
        }
    }
}