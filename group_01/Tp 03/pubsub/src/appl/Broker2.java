package appl;

import core.Server;

import java.util.Scanner;

public class Broker2 {

    public Broker2() {



        Scanner reader = new Scanner(System.in);  // Reading from System.in
       /* System.out.print("Enter the Broker address: ");
        String address = reader.next();

        System.out.print("Enter the Broker port number: ");
        int port = reader.nextInt(); // Scans the next token of the input as an int.

        System.out.print("Is the broker primary?: Y/N");
        String respYN = reader.next();

        boolean respBol;
        respBol = respYN.equalsIgnoreCase("Y");

        String secondAddress;
        int secondPort;

        if (respBol) {
            System.out.print("Enter the secondary Broker address: ");
            secondAddress = reader.next();

            System.out.print("Enter the secondary Broker port number: ");
            secondPort = reader.nextInt();

        } else {
            System.out.print("Enter the primary Broker address: ");
            secondAddress = reader.next();

            System.out.print("Enter the primary Broker port number: ");
            secondPort = reader.nextInt();

        }*/

        //Server s = new Server(port, respBol, address, secondAddress, secondPort);
        Server s = new Server(8081, true, "localhost", "localhost", 8080);
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
        new Broker2();
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
