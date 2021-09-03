package appl;

import core.Server;

import java.util.Scanner;

public class Broker {

    public Broker() {

        int primaryPort = 9090;
        int secondaryPort = 9091;
        String primaryAddress = "localhost";
        String secondaryAddress = "localhost";

        Scanner reader = new Scanner(System.in);  // Reading from System.in

        System.out.print("Is the broker primary?: Y/N");
        String respYN = reader.next();

        boolean isPrimary = respYN.equalsIgnoreCase("Y");


        int currentPort;
        String currentAddress;
        int otherPort;
        String otherAddress;

        if (isPrimary) {
            currentPort = primaryPort;
            currentAddress = primaryAddress;
            otherPort = secondaryPort;
            otherAddress = secondaryAddress;
        } else {
            currentPort = secondaryPort;
            currentAddress = secondaryAddress;
            otherPort = primaryPort;
            otherAddress = primaryAddress;
        }

        Server s = new Server(currentPort, isPrimary, currentAddress, otherAddress, otherPort);

        ThreadWrapper brokerThread = new ThreadWrapper(s);
        brokerThread.start();

        System.out.print("Shutdown the broker (Y|N)?: ");
        String resp = reader.next();
        if (resp.equals("Y") || resp.equals("y")) {
            System.out.println("Broker stopped...");
            s.stop();
            brokerThread.interrupt();

        }

        //once finished
        reader.close();
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new Broker();
    }

    class ThreadWrapper extends Thread {
        Server s;

        public ThreadWrapper(Server s) {
            this.s = s;
        }

        public void run() {
            s.begin();
        }
    }

}
