import java.util.*;

public class Roulette {
    // I don't know how roulette works. I wiki'd it.
    // Theoretical RTP = 97.297% = 16/37 * 2 <- bets cancel out
    // My simulation takes like less than a second to run @ 10 mil simulations o.o
    public static void main(String[] args) {
        long start = System.nanoTime();
        int i = Integer.parseInt(args[0]);
        RTP rtp = new RTP();
        ArrayList<RouletteThread> mySims = new ArrayList<>();
        int numSims = 10000000;
        int numSimsPerThread = numSims/i;
        while(i > 0) {
            mySims.add(new RouletteThread(numSimsPerThread, rtp));
            i--;
        }
        for(RouletteThread rt : mySims) {
            rt.start();
        }
        for(RouletteThread rt : mySims) {
            try {
                rt.join();
            }
            catch (InterruptedException ie) {
                
            }
        }
        float calculatedRTP = rtp.calculateRTP();
        long end = System.nanoTime();
        System.out.println("Simulation Time: " + (float)(end - start)/1000000000 + " seconds");
        System.out.println("RTP: " + calculatedRTP + "%");
    }
}

class RTP {
    int winnings = 0;
    int wagered = 0;

    RTP(){
        // place your bets
    }

    public float calculateRTP(){
        return (float) this.winnings / this.wagered * 100;
    }
}


class RouletteThread extends Thread {
    Random rand;
    int numSims;
    RTP rtp;
    long winnings = 0;
    long wagered = 0;
    int bet = 1;
    ArrayList<Integer> blacks = new ArrayList<>(Arrays.asList(
                    2, 4, 6, 8, 10,
                    11, 13, 15, 17,
                    20, 22, 24, 26, 28,
                    29, 31, 33, 35));

    RouletteThread(Integer numSims, RTP rtp) {
        this.numSims = numSims;
        this.rtp = rtp;
        this.rand = new Random();
    }

    public void run() {
        while (this.numSims > 0) {
            int wheel = this.rand.nextInt(37);
            if (this.blacks.contains(wheel)) {
                this.winnings += this.bet * 2;
            }
            this.wagered += this.bet;
            this.numSims -= 1;
        }
        synchronized (rtp) {
            rtp.winnings += this.winnings;
            rtp.wagered += this.wagered;
        }
    }
}
