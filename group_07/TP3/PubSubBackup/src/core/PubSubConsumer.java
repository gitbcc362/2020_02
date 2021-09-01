package core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

//the useful socket consumer
public class PubSubConsumer<S extends Socket> extends GenericConsumer<S> {

    private int uniqueLogId;
    private final SortedSet<Message> log;
    private final Set<String> subscribers;
    private boolean isPrimary;
    private String secondaryServer;
    private int secondaryPort;

    public PubSubConsumer(GenericResource<S> re, boolean isPrimary, String secondaryServer, int secondaryPort) {
        super(re);
        uniqueLogId = 1;
        log = new TreeSet<>(new MessageComparator());
        subscribers = new TreeSet<>();

        this.isPrimary = isPrimary;
        this.secondaryServer = secondaryServer;
        this.secondaryPort = secondaryPort;
    }

    @Override
    protected void doSomething(S str) {
        try {
            ObjectInputStream in = new ObjectInputStream(str.getInputStream());
            Message msg = (Message) in.readObject();
            Message response = null;

            if(msg.getType().startsWith("syncNewPrimary")) {
                if (!isPrimary){
                    System.out.println("--- Upgrade to Primary ---");
                    response = commands.get(msg.getType()).execute(msg, log, subscribers, isPrimary, secondaryServer, secondaryPort);
                    this.isPrimary = true;
                    this.secondaryServer = null;
                    this.secondaryPort = -1;
                }
            } else if (!isPrimary && !msg.getType().startsWith("sync")) {
                response = new MessageImpl();
                response.setType("backup");
                response.setContent(secondaryServer + ":" + secondaryPort);
            } else {
                if (!msg.getType().equals("notify") && !msg.getType().startsWith("sync"))
                    msg.setLogId(uniqueLogId);

                response = commands.get(msg.getType()).execute(msg, log, subscribers, isPrimary, secondaryServer, secondaryPort);

                if (!msg.getType().equals("notify"))
                    uniqueLogId = msg.getLogId();
            }

            ObjectOutputStream out = new ObjectOutputStream(str.getOutputStream());
            out.writeObject(response);
            out.flush();
            out.close();
            in.close();
            str.close();
        } catch (Exception e) {
            try {
                str.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public List<Message> getMessages() {
        return new CopyOnWriteArrayList<>(log);
    }

}
