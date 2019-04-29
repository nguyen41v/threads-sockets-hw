import java.util.concurrent.*;

public class DiningPhilosophers {
    public static void main(String[] args) {
        // two or three permits is the best, need more math/thinking to determine which is the best though :/
        // think maybe two is better because of probability of a neighbor getting their left fork is lower if only two philosophers can get their left forks!?
        Semaphore s = new Semaphore(2);
        Fork f1 = new Fork();
        Fork f2 = new Fork();
        Fork f3 = new Fork();
        Fork f4 = new Fork();
        Fork f5 = new Fork();
        Philosopher p1 = new Philosopher("Philosopher 1", s, f5, f1);
        Philosopher p2 = new Philosopher("Philosopher 2", s, f1, f2);
        Philosopher p3 = new Philosopher("Philosopher 3", s, f2, f3);
        Philosopher p4 = new Philosopher("Philosopher 4", s, f3, f4);
        Philosopher p5 = new Philosopher("Philosopher 5", s, f4, f5);
        p1.start();
        p2.start();
        p3.start();
        p4.start();
        p5.start();
    }
}
class Philosopher extends Thread {
    String name;
    Semaphore s;
    Fork leftFork;
    Fork rightFork;

    Philosopher(String name, Semaphore s, Fork leftFork, Fork rightFork) {
        this.name = name;
        this.s = s;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    public void run(){
        try {
            s.acquire();
            // probably could make a better solution where it makes one philosopher drop their fork if their neighbor needs it but this works :3
            synchronized (leftFork) {
                // System.out.println(this.name + " has obtained a left fork.");
                synchronized (rightFork) {
                    // System.out.println(this.name + " has obtained a right fork.");
                    try {
                        // System.out.println(this.name + " has started eating.");
                        sleep(1000);
                    }
                    catch (InterruptedException ie) {

                    }
                }
            }
        // System.out.println(this.name + " has finished eating.");
        s.release();
        System.out.println(this.name);
        }
        catch (InterruptedException ie) {

        }
    }
}

class Fork {
    // om nom nom
}