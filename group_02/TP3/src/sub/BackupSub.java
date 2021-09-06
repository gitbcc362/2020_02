package sub;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import core.Message;
import core.MessageImpl;
import core.PubSubCommand;
import core.client.Client;

public class BackupSub implements PubSubCommand{
    @Override
    public Message execute(Message m, SortedSet<Message> log, Set<String> subscribers, boolean isPrimary, String secondaryServerAddress, int secondaryServerPort) {
        Message response = new MessageImpl();

        int logId = m.getLogId();
        logId++;

        response.setLogId(logId);
        m.setLogId(logId);

        log.add(m);

        String[] ipAndPort = m.getContent().split(":");
        secondaryServerAddress = ipAndPort[0];
        secondaryServerPort = Integer.parseInt(ipAndPort[1]);

        response.setContent("Backup added: " + m.getContent());

        System.out.println("Backup Broker found! Start to synchronize...");
        //start a client to send all existing log messages
        //to the backup
        if(!log.isEmpty()){
            for(Message msg : log) {
                try {
                    Client client = new Client(secondaryServerAddress, secondaryServerPort);
                    Message aux = new MessageImpl();
                    aux.setType("msgSync");
                    aux.setContent(msg.getType() + "=>" + msg.getContent());
                    aux.setLogId(msg.getLogId());
                    aux.setBrokerId(msg.getBrokerId());
                    client.sendReceive(aux);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        response.setType("backupSub_ack");

        return response;
    }
}