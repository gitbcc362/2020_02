package appl;

import core.Server;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

public class Broker {

    public Broker(int port, boolean isPrimary, String secondAddress, int secondPort) {
        Server s = new Server(port, isPrimary, secondAddress, secondPort);

        ThreadWrapper brokerThread = new ThreadWrapper(s);
        brokerThread.start();

        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.print("Shutdown the broker (Y|N)?: ");
        String resp = reader.next();
        if (resp.equalsIgnoreCase("Y")) {
            System.out.println("Broker stopped...");
            s.stop();
            brokerThread.interrupt();
        }
        // once finished
        reader.close();
    }

    public static void main(String[] args) {
        int brokerPort = 0, secondPort = 0;
        String secondAddress = null;
        boolean isPrimary = false;
        Iterator<String> itArgs = Arrays.stream(args).iterator();
        while (itArgs.hasNext()) {
            String arg = itArgs.next();
            switch (arg) {
                case "-bp" -> brokerPort = Integer.parseInt(itArgs.next());
                case "-p" -> isPrimary = true;
                case "-sb" -> secondAddress = itArgs.next();
                case "-sbp" -> secondPort = Integer.parseInt(itArgs.next());
                default -> {
                    System.out.println("Comando \"" + arg + "\" inválido! Execução abortada");
                    return;
                }
            }
        }
        Scanner reader = new Scanner(System.in);
        if (brokerPort == 0) {
            System.out.print("Enter the Broker port number: ");
            brokerPort = reader.nextInt();
            System.out.print("Is the broker primary?(Y/N): ");
            isPrimary = reader.next().equalsIgnoreCase("Y");
        }
        if (secondAddress == null) {
            System.out.print("Enter the secondary Broker address: ");
            secondAddress = reader.next();
        }
        if (secondPort == 0) {
            System.out.print("Enter the secondary Broker port number: ");
            secondPort = reader.nextInt();
        }
        new Broker(brokerPort, isPrimary, secondAddress, secondPort);
    }

    static class ThreadWrapper extends Thread {
        Server s;

        public ThreadWrapper(Server s) {
            this.s = s;
        }

        public void run() {
            s.begin();
        }
    }

}
