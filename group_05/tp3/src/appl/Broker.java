package appl;

import core.Server;
import core.Message;
import core.MessageImpl;
import core.client.Client;

import java.util.Scanner;

public class Broker {

    public Broker() {

        Scanner reader = new Scanner(System.in);  // Reading from System.in

        System.out.print("Is the broker primary?: Y/N");
        String respYN = reader.next();


        ThreadWrapper brokerThread;
        boolean respBol = respYN.equalsIgnoreCase("Y") ? true : false;

        Server s;
        boolean successBackup = false;
        if (respBol){
            s = new Server(8080, true, "localhost", 8081);
            brokerThread = new ThreadWrapper(s);
            brokerThread.start();
        } else {
            s = new Server(8081, false, "localhost", 8080);
            brokerThread = new ThreadWrapper(s);
            brokerThread.start();

            Message msgBroker = new MessageImpl();
            msgBroker.setBrokerId(8081);
            msgBroker.setType("backupSub");
            msgBroker.setContent("localhost"+ ":" + 8081);

            Message status = null;

            try {
                Client subscriber = new Client("localhost", 8080);
                status = subscriber.sendReceive(msgBroker);

            } catch (Exception e) {
                successBackup = false;
            }

            if (status != null && status.getType().equals("backupSub_ack"))
                successBackup = true;
        }

        if (successBackup || respBol) {
            System.out.print("Shutdown the broker (Y|N)?: ");
            String resp = reader.next();
            if (resp.equals("Y") || resp.equals("y")){
                System.out.println("Broker stopped...");
                s.stop();
                brokerThread.interrupt();
            }
        }
        else {
            System.out.println("O backup não foi concluido ou o broker não existe.");
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
