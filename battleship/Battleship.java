import java.net.*;
import java.io.*;
import java.util.*;


public class Battleship extends Thread{
    Client[] clients;
    int turnNumber = 0;
    HashMap<Integer, Client[]> rooms;
    int room;
    final String unchecked = "-";
    final String miss = "*";
    final String unhitShip = "O";
    final String hit = "x";

    Battleship(Client[] clients, HashMap<Integer, Client[]> rooms, int room) {
        this.clients = clients;
        this.rooms = rooms;
        this.room = room;
    }

    // checks to see if a player's ships are all sunken
    Boolean win(){
        for (Client client : this.clients) {
            if (client.checkShips()) {
                for (Client otherClients : this.clients)
                if (client != otherClients) {
                    otherClients.out.println("You sunk all of the enemy's ships. You won the game!");
                } else {
                    otherClients.out.println("All your ships sunk. You lost the game!");
                }
                return true;
            }
        }
        return false;
    }

    // currentPlayer's input will be used to shoot the enemy
    void takeTurns(Client currentPlayer, Client enemy) {
        String[] xy;
        int x = -1;
        int y = -1;
        String input;

        // make players take turns
        enemy.out.println(currentPlayer.name + " is taking their turn.");
        currentPlayer.out.println("It's your turn. Enter in coordinates in the form of #,#.");
        try {
            // while input is not valid or coordinates are spots already hit, ask for input
            while (true) {
                // if other player disconnected, end game
                if ((input = currentPlayer.in.readLine()) == null) {
                    currentPlayer.disconnected = true;
                    return;
                }
                try {
                    xy = input.split(",");
                    x = Integer.parseInt(xy[0]);
                    y = Integer.parseInt(xy[1]);
                    if (x < 1 || x > 10 || y < 1 || y > 10) {
                        currentPlayer.out.println("Your coordinates were out of the baord range.\nRe-enter your coordinate with the numbers in between 1 and 10.");
                        continue;
                    }
                    x --;
                    y = 10 - y;
                } catch (Exception e) {
                    // NumberFormatException and IndexOutofBounds
                    currentPlayer.out.println("Your coordinates were invalid.\nRe-enter your coordinates in the form of #,#.");
                    continue;
                }
                if (enemy.board[y][x] == hit || enemy.board[y][x] == miss) {
                    currentPlayer.out.println("You hit that spot already! Enter in another coordinate.");
                    continue;
                } else {
                    break;
                }
            }
            // if coordinates hit nothing, it's a miss
            if (enemy.board[y][x] == unchecked) {
                enemy.board[y][x] = miss;
                currentPlayer.enemyBoard[y][x] = miss;
                for (Client client : this.clients) {
                    client.out.println(client.toString());
                }
                currentPlayer.out.println("It's a miss!");
                enemy.out.println(currentPlayer.name + " shot at " + Integer.toString(x + 1) + ","
                        + Integer.toString(10 - y) + " missed.");
            }
            // if coordinates hit a ship, they hit a ship (LOL)
            if (enemy.board[y][x] == unhitShip) {
                enemy.board[y][x] = hit;
                currentPlayer.enemyBoard[y][x] = hit;
                for (Client client : this.clients) {
                    client.out.println(client.toString());
                }
                currentPlayer.out.println("You hit an enemy ship!");
                enemy.out.println(currentPlayer.name + " hit your ship at " + Integer.toString(x + 1) + ","
                        + Integer.toString(10 - y) + ".");
                // remove coordinates of hit ship to keep track of which ships have totally sunken
                Boolean shipFound = false;
                for (Map.Entry<Integer, ArrayList<Integer[]>> ship : enemy.ships.entrySet()) {
                    // for every coordinates for that specific ship
                    for (int i = 0; i < ship.getValue().size(); i++) {
                        // if the x,y coordinates are the same as the current one, remove the coordinate from the list
                        if (ship.getValue().get(i)[0] == x && ship.getValue().get(i)[1] == y) {
                            ship.getValue().remove(i);
                            // if the list is now empty, let the players know that the ship of that size has sunken
                            if (ship.getValue().size() == 0) {
                                int shipSize = ship.getKey();
                                currentPlayer.out.println(
                                        "An enemy ship of length " + Integer.toString(shipSize) + " has been sunken!");
                                enemy.out.println("Your ship of length " + Integer.toString(shipSize) + " sunk!");
                                enemy.ships.remove(shipSize);
                                try {
                                    // let players read
                                    sleep(1000);
                                } catch (InterruptedException ie) {
                                }
                            }
                            shipFound = true;
                            break;
                        }
                    }
                    // break out of outer for loop once the ship is found
                    if (shipFound) {
                        break;
                    }
                }
                try {
                    // let players read
                    sleep(700);
                } catch (InterruptedException ie) {
                }
            }                
        } catch (IOException ie) {
        }
        try {
            // let players read
            sleep(1000);
        } catch (InterruptedException ie) {

        }
        this.turnNumber++;
    }

    // check players' connections
    // if they're disconnected, ask other player (if still present) if they want to continue playing
    // returns true if someone is disconnected
    Boolean disconnected() {
        // close all sockets and get rid of room from HashMap if both players are disconnected
        if (this.clients[0].disconnected && this.clients[1].disconnected) {
            for (Client client : this.clients) {
                client.close();
            }
            this.clients = null;
            this.rooms.remove(this.room);
        }
        if (this.clients[0].disconnected) {
            this.clients[1].out.println(this.clients[0].name
                    + " has disconnected.\nWould you like to wait in the current room for another player? Y/N?");
            try {
                String input;
                // while input is invalid, ask for input
                while (true) {
                    // get rid of room if player disconnects while in the middle of loop
                    if ((input = this.clients[1].in.readLine()) == null) {
                        for (Client client : this.clients) {
                            client.close();
                        }
                        this.clients = null;
                        this.rooms.remove(this.room);
                        return true;
                    }
                    if (input.equals("Y")) {
                        this.clients[0].close();
                        this.clients[0] = this.clients[1];
                        this.clients[1] = null;
                        this.clients[0].out.println("Waiting for another player to join the room. Your room number is "
                                + Integer.toString(room) + ".");
                        return true;
                    }
                    if (input.equals("N")) {
                        this.clients[0].close();
                        Client leavingClient = this.clients[1];
                        this.clients = null;
                        this.rooms.remove(this.room);
                        leavingClient.out.println("Which room would you like to connect to?");
                        Connect connect = new Connect(leavingClient, this.rooms);
                        connect.start();
                        return true;
                    }
                    this.clients[1].out.println(
                            "Your input was invalid. Please enter in \"Y\" to continue playing or \"N\" to stop playing.");
                }
            } catch (IOException ie) {
            }
        }
        if (this.clients[1].disconnected) {
            this.clients[0].out.println(this.clients[1].name
                    + " has disconnected. \nWould you like to wait in the current room for another player? Y/N?");
            try {
                String input;
                // while input is invalid, ask for input
                while (true) {
                    // get rid of room if player disconnects while in the middle of loop
                    if ((input = this.clients[0].in.readLine()) == null) {
                        for (Client client : this.clients) {
                            client.close();
                        }
                        this.clients = null;
                        this.rooms.remove(this.room);
                        return true;
                    }
                    if (input.equals("Y")) {
                        this.clients[1].close();
                        this.clients[1] = null;
                        this.clients[0].out.println("Waiting for another player to join the room. Your room number is " + Integer.toString(room) + ".");
                        return true;
                    }
                    if (input.equals("N")) {
                        this.clients[1].close();
                        Client leavingClient = this.clients[0];
                        this.clients = null;
                        this.rooms.remove(this.room);
                        leavingClient.out.println("Which room would you like to connect to?");
                        Connect connect = new Connect(leavingClient, this.rooms);
                        connect.start();
                        return true;
                    }
                    this.clients[0].out.println(
                            "Your input was invalid. Please enter in \"Y\" to continue playing or \"N\" to stop playing.");
                }
            } catch (IOException ie) {
            }
        }
        return false;
    }


    public void run(){
        // send messages to player about their opponent and the rules
        this.clients[0].out.println("\n" + this.clients[1].name + " joined the room. They are your opponent.");
        this.clients[1].out.println("\n" + this.clients[0].name + " is your opponent.");
        for (Client client : this.clients) {
            client.playGame = true; // reset value
            client.out.println("The rules of this game is simple." + 
            "\nYour board consists of \"" + this.unchecked + "\", \"" + this.miss + "\", \"" + this.unhitShip + "\", and \"" + this.hit + "\" pieces." + 
            "\n    " + this.unchecked + " are unhit spots" +
            "\n    " + this.miss + " are hit spots that did not hit a ship" +
            "\n    " + this.unhitShip + " are spots with ship parts that have not been hit" +
            "\n    " + this.hit + " are spots with ship parts that have been hit" +
            "\nThe first thing to do is to place your ships. You have 3 ships with a length of 2, 3, and 4." +
            "\nYour board and your opponent's board will both be shown once ships have been placed." +
            "\nThe boards will update as the game progresses." +
            "\nPlayers will take turns shooting at a specific x,y coordinate." + 
            "\nIn this version of the game, players do not go again if they successfully hit an enemy ship." +
            "\nIf an opponent's ship has sunk, both players will be notified." +
            "\nA player wins when all of the opponent's ships have sunk." +
            "\nGood luck!\n");
        }
        // while both players want to play the game
        while (this.clients[0].playGame && this.clients[1].playGame) {
            // start game only if both players are available
            ArrayList<Begin> afk = new ArrayList<>();
            for (Client client : this.clients) {
                Begin testConnection = new Begin(client);
                afk.add(testConnection);
                testConnection.start();
            }
            try {
                for (Begin connection : afk) {
                    connection.join();
                }
            } catch (InterruptedException ie) {

            }
            // if a player disconnected, stop running (look at this.disconnected() code)
            if (this.disconnected()) {
                return;
            }
            // initialize/reset board for first/every game
            for (Client client : this.clients) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        client.board[i][j] = "-";
                        client.enemyBoard[i][j] = "-";
                    }
                }
            }
            // add ships
            ShipChooser c1ships = new ShipChooser(this.clients[0], this.clients[1]);
            ShipChooser c2ships = new ShipChooser(this.clients[1], this.clients[0]);
            c1ships.start();
            c2ships.start();
            // wait for ships to be added before starting game
            try {
                c1ships.join();
                c2ships.join();
            } catch (InterruptedException ie) {
            }
            // if a player disconnected, stop running (look at this.disconnected() code)
            if (this.disconnected()) {
                return;
            }
            // while no one has won
            while (!this.win()) {
                if (this.turnNumber % 2 == 0) {
                    this.takeTurns(this.clients[0], this.clients[1]);
                } else {
                    this.takeTurns(this.clients[1], this.clients[0]);
                }
                // if a player disconnected, stop running (look at this.disconnected() code)
                if (this.disconnected()) {
                    return;
                }
            }
            // check if players want to play again
            ArrayList<PlayAgain> temp = new ArrayList<>();
            for (Client client : this.clients) {
                PlayAgain playAgain = new PlayAgain(client, rooms);
                temp.add(playAgain);
            }
            for (PlayAgain pa : temp) {
                pa.start();
            }
            for (PlayAgain pa : temp) {
                try {
                    pa.join();
                } catch (InterruptedException ie) {
                }
            }
            // if a player disconnected, stop running (look at this.disconnected() code)
            if (this.disconnected()) {
                return;
            }
        }
        // if one player wants to play when the other player doesn't, let them know that the room is waiting for another player to join
        // close other client's socket and remove them from list
        if (this.clients[0].playGame) {
            this.clients[0].out.println(this.clients[1].name + " stopped playing the game in this room.\nYour room is "
                    + Integer.toString(this.room) + ". Waiting for another player to join.");
            this.clients[1] = null;
            return;
        }
        if (this.clients[1].playGame) {
            this.clients[1].out.println(this.clients[0].name + " stopped playing the game in this room.\nYour room is "
                    + Integer.toString(this.room) + ". Waiting for another player to join.");
            this.clients[0] = this.clients[1];
            this.clients[1] = null;
            return;
        }
        // if no one wants to play in current room, remove room from HashMap
        this.clients = null;
        this.rooms.remove(this.room);
    }
}

// make sure both players are present 
class Begin extends Thread {
    Client client;

    Begin(Client client) {
        this.client = client;
    }

    public void run() {
        try {
            this.client.out.println("Click enter to begin once you are ready.");
            String input = this.client.in.readLine();
            this.client.out.println("Waiting for the other player to click enter.");
            if (input == null) {
                this.client.disconnected = true;
            }
        } catch (IOException ie) {
        }
    }
}


// let client choose ship locations
class ShipChooser extends Thread {
    final String[] ships = new String[]{"2","3","4"};
    Client client;
    Client otherClient;

    ShipChooser(Client client, Client otherClient) {
        this.client = client;
        this.otherClient = otherClient;
    }

    public void run() {
        try {
            this.client.out.println("It's time to place your ships. You have 3 ships with a length of 2, 3, and 4.");
            this.client.out.println("You can only place your ship horizontally and vertically.");
            this.client.out.println("Please enter in your start and end coordinates as #,# #,#");
            try {
                sleep(2000); // give player time to read
            } catch (InterruptedException ie) {
            }
            String input;
            String[] coordinates;
            String[] coordinate1 = new String[2];
            String[] coordinate2 = new String[2];
            int x1 = -1;
            int x2 = -1;
            int y1 = -1;
            int y2 = -1;
            // place all ships
            for (String ship : this.ships) {
                this.client.out.println(client.printYourBoard());
                this. client.out.println("Pick your coordinates for your ship with a length of " + ship  + ".");
                Boolean notvalid = true;
                // while coordinates are not valid for specific ship length, keep asking for coordinates
                while (notvalid) {
                    // stop running if other client disconnected
                    if (this.otherClient.disconnected) {
                        return;
                    }
                    if ((input = this.client.in.readLine()) == null) {
                        this.client.disconnected = true;
                        return;
                    }
                    // make sure input follows correct format
                    try {
                        coordinates = input.split(" ");
                        coordinate1 = coordinates[0].split(",");
                        coordinate2 = coordinates[1].split(",");
                        x1 = Integer.parseInt(coordinate1[0]);
                        y1 = Integer.parseInt(coordinate1[1]);
                        x2 = Integer.parseInt(coordinate2[0]);
                        y2 = Integer.parseInt(coordinate2[1]);
                    } catch (Exception e) {
                        // NumberFormatException and IndexOutofBounds (splitting)
                        this.client.out.println(this.client.printYourBoard());
                        this.client.out.println("Your coordinates were invalid.\nRe-enter your coordinates in the form of #,# #,#.");
                        continue;       
                    }            
                    // make sure input is in board range
                    if (x1 < 1 || x1 > 10 || y1 < 1 || y1 > 10 || x2 < 1 || x2 > 10 || y2 < 1 || y2 > 10) {
                        this.client.out.println(this.client.printYourBoard());
                        this.client.out.println("Your coordinates are out of range.\nPlease re-enter your coordinates with the numbers in between 1 and 10.");
                        continue;
                    }
                    // make sure input is for a horizontal/vertical position
                    if (x1 != x2 && y1 != y2) {
                        this.client.out.println(this.client.printYourBoard());
                        this.client.out.println("You can only place your ship horizontally and vertically.\nPlease re-enter your coordinates.");
                        continue;
                    }
                    // make sure input is correct for current ship length
                    if (Math.abs(x1 - x2) + 1 != Integer.parseInt(ship) && Math.abs(y1 - y2) + 1 != Integer.parseInt(ship)) {
                        this.client.out.println(this.client.printYourBoard());
                        this.client.out.println("Your coordinates were invalid for the current ship with a length of " + ship + ".\nPlease re-enter your coordinates.");
                        continue;
                    }
                    // make sure possible ship location does not overlap with other existing ships
                    notvalid = false;
                    if (y1 < y2) {
                        for (int tempy = y1; tempy < y2; tempy ++) {
                            if (this.client.board[10 - tempy][x1 - 1] == "O") {
                                this.client.out.println(this.client.printYourBoard());
                                this.client.out.println("You cannot place your ship on another ship.\nPlease re-enter your coordinates.");
                                notvalid = true;
                                break;
                            }                   
                        }
                    } else if (y2 < y1) {
                        for (int tempy = y2; tempy < y1; tempy++) {
                            if (this.client.board[10 - tempy][x1 - 1] == "O") {
                                this.client.out.println(this.client.printYourBoard());
                                this.client.out.println("You cannot place your ship on another ship.\nPlease re-enter your coordinates.");
                                notvalid = true;
                                break;
                            }
                        }
                    } else if (x1 < x2) {
                        for (int tempx = x1; tempx < x2; tempx++) {
                            if (this.client.board[10 - y1][tempx - 1] == "O") {
                                this.client.out.println(this.client.printYourBoard());
                                this.client.out.println("You cannot place your ship on another ship.\nPlease re-enter your coordinates.");
                                notvalid = true;
                                break;
                            }
                        }
                    } else if (x2 < x1) {
                        for (int tempx = x2; tempx < x1; tempx++) {
                            if (this.client.board[10 - y1][tempx - 1] == "O") {
                                this.client.out.println(this.client.printYourBoard());
                                this.client.out.println("You cannot place your ship on another ship.\nPlease re-enter your coordinates.");
                                notvalid = true;
                                break;
                            }
                        }
                    }
                }
                // add ship to board
                this.client.addShip(Integer.parseInt(ship), x1 - 1, 10 - y1, x2 - 1, 10 - y2);
            }
        } catch (IOException ie) {
        }
        this.client.out.println(this.client.printYourBoard());
        this.client.out.println("Both players must be done placing ships for the game to start.");
        this.client.out.println(this.client.toString());
    }
}

class PlayAgain extends Thread {
    Client client;
    HashMap<Integer, Client[]> rooms;

    PlayAgain(Client client, HashMap<Integer, Client[]> rooms) {
        this.client = client;
        this.rooms = rooms;
    }

    public void run() {
        this.client.playGame = false;
        this.client.out.println("Would you like to play the game again with the same person? Y/N?");
        try {
            String input;
            // keep asking for inputs until input is valid or player disconnected
            while(true) {
                input = this.client.in.readLine();
                if (input == null) {
                    this.client.disconnected = true;
                    return;
                }
                if (input.equals("Y")) {
                    this.client.playGame = true;
                    return;
                }
                if (input.equals("N")) {
                    this.client.out.println("Which room would you like to connect to?");
                    Connect connect = new Connect(this.client, this.rooms);
                    connect.start();
                    return;
                }
                this.client.out.println("Your input was invalid. Please enter in \"Y\" to continue playing or \"N\" to stop playing.");
            }
        } catch (IOException ie) {
        }
    }
}

