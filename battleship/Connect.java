import java.util.*;
import java.io.*;

public class Connect extends Thread {
    Client client;
    HashMap<Integer, Client[]> rooms;
    Boolean firstConnection;

    Connect(Client client, HashMap<Integer, Client[]> rooms) {
        this.client = client;
        this.rooms = rooms;
        this.firstConnection = true;
    }
    Connect(Client client, HashMap<Integer, Client[]> rooms, Boolean firstConnection) {
        this.client = client;
        this.rooms = rooms;
        this.firstConnection = firstConnection;
    }

    public void run() {
        String input;
        if (firstConnection) {
            try {
                int room;
                while (true) {
                    input = this.client.in.readLine();
                    if (input == null) { // client disconnected
                        client.close();
                        return;
                    }
                    if (input.equals("N")) {
                        this.client.out.println("Thank you for coming.");
                        this.client.close();
                        return;
                    }
                    try {
                        room = Integer.parseInt(input);
                        break;
                    } catch (NumberFormatException n) {
                        this.client.out.println("That was not a valid number. Please re-enter your number.");
                    }
                }
                // checking room number
                Client[] roomList;
                // if room exists
                if (this.rooms.containsKey(room)) {
                    roomList = this.rooms.get(room);
                    // if room is not full, add player to room
                    if (roomList[1] == null) {
                        roomList[1] = this.client;
                        Battleship game = new Battleship(roomList, this.rooms, room);
                        game.start();
                        return;
                    }
                    // if room doesn't exist, make new room
                } else {
                    roomList = new Client[2];
                    roomList[0] = this.client;
                    this.rooms.put(room, roomList);
                    this.client.out.println(
                            "Your room is " + Integer.toString(room) + ". Waiting for another player to join.");
                    return;
                }
            } catch (IOException ie) {
            }
        }
        // until player successfully connects to a room or disconnects
        while (true) {
            this.client.out.println(
                    "That room is already occupied by two people. Would you like to connect to another room? Y/N?");
            try {
                while (true) {
                    input = this.client.in.readLine();
                    if (input == null) { // client disconnected
                        client.close();
                        return;
                    }
                    if (input.equals("Y")) {
                        break;
                    }
                    if (input.equals("N")) {
                        this.client.out.println("Thank you for coming, even though you didn't get to play.");
                        this.client.close();
                        return;
                    }
                    this.client.out.println(
                            "Your input was invalid. Please enter in \"Y\" to connect to another room or \"N\" to disconnect.");
                }
                // if player wants to connect to another room, get a room number
                this.client.out.println(
                        "Which room would you like to connect to? Enter in a number. Enter in \"N\" if you'd like to stop connecting");
                int room;
                while (true) {
                    input = this.client.in.readLine();
                    if (input == null) { // client disconnected
                        client.close();
                        return;
                    }
                    if (input.equals("N")) {
                        this.client.out.println("Thank you for coming, even though you didn't get to play.");
                        this.client.close();
                        return;
                    }
                    try {
                        room = Integer.parseInt(input);
                        break;
                    } catch (NumberFormatException n) {
                        this.client.out.println("That was not a valid number. Please re-enter your number.");
                    }
                }
                // checking room number again
                Client[] roomList;
                // if room exists
                if (this.rooms.containsKey(room)) {
                    roomList = this.rooms.get(room);
                    // if room is not full, add player to room
                    if (roomList[1] == null) {
                        roomList[1] = this.client;
                        Battleship game = new Battleship(roomList, this.rooms, room);
                        game.start();
                        return;
                        // else continue asking player if they want to keep looking for a room
                    } else {
                        continue;
                    }
                    // if room doesn't exist, make new room
                } else {
                    roomList = new Client[2];
                    roomList[0] = this.client;
                    this.rooms.put(room, roomList);
                    this.client.out.println(
                            "Your room is " + Integer.toString(room) + ". Waiting for another player to join.");
                    return;
                }
            } catch (IOException ie) {
            }
        }

    }
}