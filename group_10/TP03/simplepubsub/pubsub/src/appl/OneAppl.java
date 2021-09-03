package appl;

import java.util.*;

import core.Message;

public class OneAppl {

    String[] vars = {"Abacaxi", "Mamao", "NASDAQ", "Passagem de Aviao", "Compra na Amazon"};

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new OneAppl(true);
    }

    public OneAppl(boolean flag) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Endereço do broker");
        String brokerAddr = "localhost";// scanner.next();

        System.out.println("Porta do broker");
        int brokerPort = 9090;// scanner.nextInt();

        int machineId;
        System.out.println("Enter Machine Id");
        machineId = scanner.nextInt();

        System.out.println("Machine Id: " + machineId);
        int totalClients = 3;


        PubSubClient client = new PubSubClient("localhost", 8080 + machineId);
        client.subscribe(brokerAddr, brokerPort);

        Thread accessOne = new ThreadWrapper(client, machineId + ": " + "Abacate", brokerAddr, brokerPort, machineId,
                totalClients);
        accessOne.start();

        try {

            accessOne.join();

        } catch (Exception e) {

        }

        client.unsubscribe(brokerAddr, brokerPort);

        client.stopPubSubClient();

    }

    class ThreadWrapper extends Thread {
        PubSubClient c;
        String msg;
        String host;
        int port;

        int machineId;
        int totalClients;

        public ThreadWrapper(PubSubClient c, String msg, String host, int port, int id, int totalClients) {
            this.c = c;
            this.msg = msg;
            this.host = host;
            this.port = port;
            this.machineId = id;
            this.totalClients = totalClients;
        }

        public void run() {

            boolean firstPost = true;
            Random randGen = new Random();
            int lastId = Integer.MIN_VALUE;
            long lastMsgTime = System.currentTimeMillis();

            while (true) {

                try {
                    this.sleep(4000);
                } catch (InterruptedException e) {

                }

                List<Message> log = c.getLogMessages();
                if (log.size() > 0)
                    Collections.reverse(log);
                Iterator<Message> it = log.iterator();

                String newMsg = this.machineId + ": " + vars[randGen.nextInt(vars.length)];

                if (firstPost && this.machineId == 1) {
                    c.publish(newMsg, host, port);
                    firstPost = false;
                }

                if (System.currentTimeMillis() - lastMsgTime > totalClients * 8000) {
                    if (lastId + 2 <= totalClients && lastId + 2 == this.machineId) {
                        System.out.println("ID: " + (lastId + 1) + " didnt respond");
                        System.out.println(machineId + " publish " + newMsg);
                        System.out.println("----------------------------------");
                        c.publish(newMsg, host, port);
                    } else if (lastId + 2 > totalClients && -(totalClients - (lastId + 2)) == this.machineId) {
                        System.out.println("ID: " + (-(totalClients - (lastId + 2)) - 1) + " didnt respond");
                        System.out.println(machineId + " publish " + newMsg);
                        System.out.println("----------------------------------");
                        c.publish(newMsg, host, port);
                    }
                }

                while (it.hasNext()) {
                    Message aux = it.next();

                    try {
                        if(aux.getType().startsWith("changeBrokers")){
                            c.changeBroker();
                        }
                        int id = Integer.parseInt(aux.getContent().split(": ")[0]);
                        if (lastId != id) {
                            lastMsgTime = System.currentTimeMillis();
                            lastId = id;
                        }
                        if (true) {
                            aux.getLogId();
                        }
                        if (id + 1 == this.machineId || (this.machineId == 1 && id == this.totalClients)) {
                            System.out.println("Received from " + aux.getContent());
                            System.out.println(machineId + " publish " + newMsg);
                            System.out.println("----------------------------------");
                            c.publish(newMsg, host, port);
                        }
                        break;
                    } catch (NumberFormatException e) {
                        continue;
                    }

                }
            }

        }
    }

}
