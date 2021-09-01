package appl;

import core.Message;
import core.MessageImpl;
import core.Server;
import core.client.Client;
import java.util.List;

public class PubSubClient {

    private Server observer;
    private ThreadWrapper clientThread;

    private String clientName;
    private String clientAddress;
    private int clientPort;

    private String brokerAddress;
    private int brokerPort;
    private String backupAddress;
    private int backupPort;

    public PubSubClient() {
        //this constructor must be called only when the method
        //startConsole is used
        //otherwise the other constructor must be called
    }

    public PubSubClient(String clientName, String clientAddress, int clientPort) {
        this.clientName = clientName;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        observer = new Server(clientPort);
        clientThread = new ThreadWrapper(observer);
        clientThread.start();
    }

    public void subscribe(String brokerAddress, int brokerPort, String backupAddress, int backupPort) {
        try {
            Message msgBroker = new MessageImpl();
            msgBroker.setBrokerId(brokerPort);
            msgBroker.setType("sub");
            msgBroker.setContent(clientAddress + ":" + clientPort);
            Client subscriber = new Client(brokerAddress, brokerPort);
            Message response = subscriber.sendReceive(msgBroker);
            if (response.getType().equals("backup")) {
                brokerAddress = response.getContent().split(":")[0];
                brokerPort = Integer.parseInt(response.getContent().split(":")[1]);
                subscriber = new Client(brokerAddress, brokerPort);
                subscriber.sendReceive(msgBroker);
            }
            this.brokerAddress = brokerAddress;
            this.brokerPort = brokerPort;
            this.backupAddress = backupAddress;
            this.backupPort = backupPort;
        } catch (Exception e) {
            Message msgBroker = new MessageImpl();
            msgBroker.setBrokerId(brokerPort);
            msgBroker.setContent("Backup becomes primary: " + backupAddress + ":" + backupPort);
            msgBroker.setType("syncNewPrimary");

            Client clientBackup = new Client(backupAddress, backupPort);
            clientBackup.sendReceive(msgBroker);

            // Troca os endereços do broker
            this.brokerAddress = backupAddress;
            this.brokerPort = backupPort;
            this.backupAddress = null;
            this.backupPort = -1;

            // Espera um tempo para poder reenviar o sub
            try {
                Thread.sleep(100);
            } catch (InterruptedException se) {
                se.printStackTrace();
            }

            msgBroker = new MessageImpl();
            msgBroker.setBrokerId(backupPort);
            msgBroker.setType("sub");
            msgBroker.setContent(clientAddress + ":" + clientPort);
            Client subscriber = new Client(backupAddress, backupPort);
            Message response = subscriber.sendReceive(msgBroker);
            if (response.getType().equals("backup")) {
                brokerAddress = response.getContent().split(":")[0];
                brokerPort = Integer.parseInt(response.getContent().split(":")[1]);
                subscriber = new Client(brokerAddress, brokerPort);
                subscriber.sendReceive(msgBroker);
            }
        }
    }

    public void unsubscribe() {
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

    public void publish(String message) {
        try{
            Message msgPub = new MessageImpl();
            msgPub.setBrokerId(brokerPort);
            msgPub.setType("pub");
            msgPub.setContent(message);

            Client publisher = new Client(brokerAddress, brokerPort);
            Message response = publisher.sendReceive(msgPub);

            if(response.getType().equals("backup")){
                brokerAddress = response.getContent().split(":")[0];
                brokerPort = Integer.parseInt(response.getContent().split(":")[1]);
                publisher = new Client(brokerAddress, brokerPort);
                publisher.sendReceive(msgPub);
            }
        } catch(Exception e) {
            Message msgBroker = new MessageImpl();
            msgBroker.setBrokerId(brokerPort);
            msgBroker.setContent("Backup becomes primary: " + backupAddress + ":" + backupPort);
            msgBroker.setType("syncNewPrimary");

            Client clientBackup = new Client(backupAddress, backupPort);
            clientBackup.sendReceive(msgBroker);

            // Troca os endereços do broker
            brokerAddress = backupAddress;
            brokerPort = backupPort;

            // Espera um tempo para poder reenviar o pub
            try {
                Thread.sleep(100);
            } catch (InterruptedException se) {
                se.printStackTrace();
            }

            Message msgPub = new MessageImpl();
            msgPub.setBrokerId(brokerPort);
            msgPub.setType("pub");
            msgPub.setContent(message);

            Client publisher = new Client(brokerAddress, brokerPort);
            Message response = publisher.sendReceive(msgPub);

            if (response != null) {
                if(response.getType().equals("backup")){
                    brokerAddress = response.getContent().split(":")[0];
                    brokerPort = Integer.parseInt(response.getContent().split(":")[1]);
                    publisher = new Client(brokerAddress, brokerPort);
                    publisher.sendReceive(msgPub);
                }
            }
        }
    }

    public String getClientName() {
        return this.clientName;
    }

    public List<Message> getLogMessages() {
        return observer.getLogMessages();
    }

    public void stopPubSubClient() {
        System.out.println("Client stopped...");
        observer.stop();
        clientThread.interrupt();
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
