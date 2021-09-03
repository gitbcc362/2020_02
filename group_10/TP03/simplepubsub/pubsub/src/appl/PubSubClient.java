package appl;

import java.util.*;

import core.Message;
import core.MessageImpl;
import core.Server;
import core.client.Client;

public class PubSubClient {

    private Server observer;
    private ThreadWrapper clientThread;

    private String clientAddress;
    private int clientPort;

    Map<String, Integer> brokers = new LinkedHashMap<>();

/*    public PubSubClient() {
        //this constructor must be called only when the method
        //startConsole is used
        //otherwise the other constructor must be called
        this.brokerPrimaryPort= 0;
        this.brokerPrimaryAddr = "";
        this.brokerSecondaryAddr = this.brokerPrimaryAddr;


    }*/

    public PubSubClient(String clientAddress, int clientPort) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        observer = new Server(clientPort);
        clientThread = new ThreadWrapper(observer);
        clientThread.start();
        this.brokers.put("activeIndex", 1);
    }

    public void subscribe(String brokerAddress, int brokerPort) {

        Message msgBroker = new MessageImpl();
        msgBroker.setBrokerId(brokerPort);
        msgBroker.setType("sub");
        msgBroker.setContent(clientAddress + ":" + clientPort);
        try {
            Client subscriber = new Client(brokerAddress, brokerPort);
            System.out.println("SUB " + subscriber.sendReceive(msgBroker).getContent());
        } catch (Exception error) {
            System.out.println("Error in subscribe");
        }
        try {
            System.out.println("syncing");
            Message msgAddrs = new MessageImpl();
            Client subscriber = new Client(brokerAddress, brokerPort);
            msgAddrs.setBrokerId(brokerPort);
            msgAddrs.setType("syncAddr");
            msgAddrs.setContent("Give me address");
            //Envio da mensagem para receber o endereço do backup
            Message responseAddr = subscriber.sendReceive(msgAddrs);
            brokers.put(brokerAddress + brokers.size(), brokerPort); // TODO: remover na gcp
            brokers.put(responseAddr.getContent() + brokers.size(), responseAddr.getBrokerId()); // TODO: remover na gcp
        } catch (Exception error) {

        }

    }

    public void unsubscribe(String brokerAddress, int brokerPort) {

        Message msgBroker = new MessageImpl();
        msgBroker.setBrokerId(brokerPort);
        msgBroker.setType("unsub");
        msgBroker.setContent(clientAddress + ":" + clientPort);
        Client subscriber = new Client(brokerAddress, brokerPort);
        Message response = subscriber.sendReceive(msgBroker);

        if (response.getType().equals("backup")) {
            brokerAddress = response.getContent().split(":")[0];
            brokerPort = Integer.parseInt(response.getContent().split(":")[1]);
            subscriber = new Client(brokerAddress, brokerPort);
            subscriber.sendReceive(msgBroker);
        }
    }

    public void publish(String message, String a, int b) {

        int active = brokers.get("activeIndex");
        String brokerAddress = (String) brokers.keySet().toArray()[active];
        int brokerPort = brokers.get(brokerAddress);
        brokerAddress = brokerAddress.substring(0, brokerAddress.length() - 1); // TODO: remover na gcp

        Message msgPub = new MessageImpl();
        msgPub.setBrokerId(brokerPort);
        msgPub.setType("pub");
        msgPub.setContent(message);


        try {

            Client publisher = new Client(brokerAddress, brokerPort);
            Message response = publisher.sendReceive(msgPub);
            if (response.getType().equals("backup")) {
                brokerAddress = response.getContent().split(":")[0];
                brokerPort = Integer.parseInt(response.getContent().split(":")[1]);
                publisher = new Client(brokerAddress, brokerPort);
                publisher.sendReceive(msgPub);
            }
        } catch (Exception err) {
            Message brokerDown = new MessageImpl();
            brokerDown.setType("changeBroker");
            brokerDown.setContent(brokerAddress + ":" + brokerPort); // endereço do primario
            brokerDown.setBrokerId(brokerPort);

            System.out.println("Changing brokers!");
            if (brokers.get("activeIndex") == brokers.size() - 1)
                brokers.put("activeIndex", 1);
            else
                brokers.put("activeIndex", brokers.get("activeIndex") + 1);

            active = brokers.get("activeIndex");
            brokerAddress = (String) brokers.keySet().toArray()[active];
            brokerPort = brokers.get(brokerAddress);
            brokerAddress = brokerAddress.substring(0, brokerAddress.length() - 1); // TODO: remover na gcp


            Client publisher = new Client(brokerAddress, brokerPort);
            publisher.sendReceive(brokerDown);

            msgPub.setBrokerId(brokerPort);
            publisher.sendReceive(msgPub);

        }


    }

    public void changeBroker(){
        System.out.println("Notified to change brokers!");
        if (brokers.get("activeIndex") == brokers.size() - 1)
            brokers.put("activeIndex", 1);
        else
            brokers.put("activeIndex", brokers.get("activeIndex") + 1);
    }

    public List<Message> getLogMessages() {
        return observer.getLogMessages();
    }

    public void stopPubSubClient() {
        System.out.println("Client stopped...");
        observer.stop();
        clientThread.interrupt();
    }

    public void startConsole() {
        Scanner reader = new Scanner(System.in);  // Reading from System.in

        System.out.print("Enter the client port (ex.8080): ");
        int clientPort = reader.nextInt();
        System.out.println("Now you need to inform the broker credentials...");
        System.out.print("Enter the broker address (ex. localhost): ");
        String brokerAddress = reader.next();
        System.out.print("Enter the broker port (ex.8080): ");
        int brokerPort = reader.nextInt();

        observer = new Server(clientPort);
        clientThread = new ThreadWrapper(observer);
        clientThread.start();

        subscribe(brokerAddress, brokerPort);

        System.out.println("Do you want to subscribe for more brokers? (Y|N)");
        String resp = reader.next();

        if (resp.equals("Y") || resp.equals("y")) {
            String message = "";

            while (!message.equals("exit")) {
                System.out.println("You must inform the broker credentials...");
                System.out.print("Enter the broker address (ex. localhost): ");
                brokerAddress = reader.next();
                System.out.print("Enter the broker port (ex.8080): ");
                brokerPort = reader.nextInt();
                subscribe(brokerAddress, brokerPort);
                System.out.println(" Write exit to finish...");
                message = reader.next();
            }
        }

        System.out.println("Do you want to publish messages? (Y|N)");
        resp = reader.next();
        if (resp.equals("Y") || resp.equals("y")) {
            String message = "";

            while (!message.equals("exit")) {
                System.out.println("Enter a message (exit to finish submissions): ");
                message = reader.next();

                System.out.println("You must inform the broker credentials...");
                System.out.print("Enter the broker address (ex. localhost): ");
                brokerAddress = reader.next();
                System.out.print("Enter the broker port (ex.8080): ");
                brokerPort = reader.nextInt();

                publish(message, brokerAddress, brokerPort);

                List<Message> log = observer.getLogMessages();

                Iterator<Message> it = log.iterator();
                System.out.print("Log itens: ");
                while (it.hasNext()) {
                    Message aux = it.next();
                    System.out.print(aux.getContent() + aux.getLogId() + " | ");
                }
                System.out.println();

            }
        }

        System.out.print("Shutdown the client (Y|N)?: ");
        resp = reader.next();
        if (resp.equals("Y") || resp.equals("y")) {
            System.out.println("Client stopped...");
            observer.stop();
            clientThread.interrupt();

        }

        //once finished
        reader.close();
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
