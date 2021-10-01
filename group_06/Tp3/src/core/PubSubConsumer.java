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
    private SortedSet<Message> log;
    private Set<String> subscribers;
    private boolean isPrimary;
    private String secondaryServer;
    private int secondaryPort;
    public boolean secActivity = false;

    public PubSubConsumer(GenericResource<S> re, boolean isPrimary, String secondaryServer, int secondaryPort) {
        super(re);
        uniqueLogId = 1;
        log = new TreeSet<Message>(new MessageComparator());
        subscribers = new TreeSet<String>();

        this.isPrimary = isPrimary;
        this.secondaryServer = secondaryServer;
        this.secondaryPort = secondaryPort;
    }


    @Override
    protected void doSomething(S str) {
        try {
            // TODO Auto-generated method stub
            ObjectInputStream in = new ObjectInputStream(str.getInputStream());

            Message msg = (Message) in.readObject();

            Message response = null;

            String[] secMessage = msg.getType().split(" ");

            if(secMessage[0].equals("sec")){
                System.out.println("entrei sec");
                if(this.isPrimary == false){
                    System.out.println("entrei false");
                    this.isPrimary = true;
                } 

                msg.setType(secMessage[1]);
            }

            if (!isPrimary && !msg.getType().startsWith("sync")) {

                //Client client = new Client(secondaryServer, secondaryPort);
                //response = client.sendReceive(msg);

                response = new MessageImpl();
                response.setType("backup");
                response.setContent(secondaryServer + ":" + secondaryPort);

            } else {
                if (!msg.getType().equals("notify") && !msg.getType().startsWith("sync"))
                    msg.setLogId(uniqueLogId);
                    
                if(secMessage[0].equals("wakeup")){
                    System.out.println("entrei aqui");
                    this.secActivity = true;
                    msg.setType(secMessage[1]);
                    System.out.println(msg.getType());
                }

               response = commands.get(msg.getType()).execute(msg, log, subscribers, isPrimary, secondaryServer, secondaryPort, secActivity);

                if (!msg.getType().equals("notify")) //&& !msg.getType().startsWith("sync"))
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
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

    }

    public SortedSet<Message> getMessages() {
        return log;
    }

}
