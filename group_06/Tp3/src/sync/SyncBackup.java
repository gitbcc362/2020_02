package sync;

import java.util.Set;
import java.util.SortedSet;
import java.util.Iterator;

import core.Message;
import core.MessageImpl;
import core.PubSubCommand;
import core.client.Client;

public class SyncBackup implements PubSubCommand {

    @Override
    public Message execute(Message m, SortedSet<Message> log, Set<String> subscribers, boolean isPrimary,
            String sencondaryServerAddress, int secondaryServerPort, boolean secActivity) {

        // secActivity = true;
        System.out.println("entrei no sync backup");

        // start many clients to send all existing log messages
        // for the subscribed user
        if (!log.isEmpty()) {
            Iterator<Message> it = log.iterator(); 
            while (it.hasNext()) {
                Message cMsg = null;
                try {
                    Client client = new Client(sencondaryServerAddress, secondaryServerPort);
                    Message msg = it.next();
                    Message aux = new MessageImpl();
                    System.out.println("logs: " + msg.getType());

                    if(msg.getType().equals("sub") || msg.getType().equals("syncSub"))
                        aux.setType("syncSub");
                    else if(msg.getType().equals("pub") || msg.getType().equals("syncPub"))
                        aux.setType("syncPub");
                    else if(msg.getType().equals("unlock"))
                        aux.setType("syncPub");

                    aux.setContent(msg.getContent());
                    aux.setLogId(msg.getLogId());
                    aux.setBrokerId(m.getBrokerId());
                    cMsg = client.sendReceive(aux);
                } catch (Exception e) { }
            }
        }

        Message response = new MessageImpl();
        response.setLogId(m.getLogId());
        response.setContent("Message published on backup: " + m.getContent());
        response.setType("backup_ack");

        return response;
    }

}
