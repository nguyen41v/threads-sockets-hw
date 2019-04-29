import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;
    public String name;
    public String[][] board = new String[10][10];
    public String[][] enemyBoard = new String[10][10];
    public Boolean disconnected = false;
    public Boolean playGame;
    public HashMap<Integer, ArrayList<Integer[]>> ships = new HashMap<>();

    Client(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ie) {
        }
    }

    // close in/out streams and client's socket
    public void close() {
        try {
            this.in.close();
        } catch (IOException ie) {
        }
        this.out.close();
        try {
            this.socket.close();
        } catch (IOException ie) {
        }
    }

    // add ships to the player's board and to HashMap ships
    void addShip(int size, int x1, int y1, int x2, int y2) {
        ArrayList<Integer[]> temp = new ArrayList<>();
        if (y1 < y2) {
            while (y1 <= y2) {
                temp.add(new Integer[] { x1, y1 });
                board[y1][x1] = "O";
                y1++;
            }
        } else if (y2 < y1) {
            while (y2 <= y1) {
                temp.add(new Integer[] { x1, y2 });
                board[y2][x1] = "O";
                y2++;
            }
        } else if (x1 < x2) {
            while (x1 <= x2) {
                temp.add(new Integer[] { x1, y1 });
                board[y1][x1] = "O";
                x1++;
            }
        } else if (x2 < x1) {
            while (x2 <= x1) {
                temp.add(new Integer[] { x2, y1 });
                board[y1][x2] = "O";
                x2++;
            }
        }
        this.ships.put(size, temp);
    }

    // check if player has lost the game
    Boolean checkShips() {
        return this.ships.isEmpty();
    }
    
    // returns a string of the player's board and the player's view of the enemy's board
    public String toString() {
        String boards = "\n";
        for (int y = 0; y < 10; y++) {
            if (y == 0) {
                boards += Integer.toString(10 - y) + " |";
            } else {
                boards += "\n " + Integer.toString(10 - y) + " |";
            }
            for (int x = 0; x < 10; x++) {
                boards += " " + this.enemyBoard[y][x];
            }

            boards += " |  "; 

            if (y == 0) {
                boards += Integer.toString(10 - y) + " |";
            } else {
                boards += " " + Integer.toString(10 - y) + " |";
            }
            for (int x = 0; x < 10; x++) {
                boards += " " + this.board[y][x];
            }
            boards += " |";
        }
        boards += "\n   +---------------------+     +---------------------+\n     1 2 3 4 5 6 7 8 9 10        1 2 3 4 5 6 7 8 9 10\n     Enemy board                 Your board";
        return boards;
    }

    // returns a string of the player's board
    String printYourBoard() {
        String boards = "\n";
        for (int y = 0; y < 10; y++) {
            if (y == 0) {
                boards += Integer.toString(10 - y) + " |";
            } else {
                boards += "\n " + Integer.toString(10 - y) + " |";
            }
            for (int x = 0; x < 10; x++) {
                boards += " " + this.board[y][x];
            }
            boards += " |  ";
        }
        boards += "\n   +---------------------+\n     1 2 3 4 5 6 7 8 9 10\n     Your board";
        return boards;
    }
}
