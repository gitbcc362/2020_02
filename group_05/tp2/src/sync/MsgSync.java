package sync;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.Set;

import core.Message;
import core.MessageImpl;
import core.PubSubCommand;
import core.client.Client;

public class MsgSync implements PubSubCommand{

    @Override
    public Message execute(Message m, SortedSet<Message> log, Set<String> subscribers, boolean isPrimary, String secondaryServerAddress, int secondaryServerPort) {

        String[] content = m.getContent().split("=>");

        m.setType(content[0]);
        m.setContent(content[1]);
        System.out.println("content[1]: " + content[1] + " content[0]: " + content[0]);

        log.add(m);

        if (m.getType().equals("sub"))
            subscribers.add(m.getContent());

        if (m.getType().equals("unsub")){
            subscribers.remove(m.getContent());
        }

        Message response = new MessageImpl();

        response.setContent("Message synchronized: " + m.getContent());
        response.setType("sync_ack");

        System.out.println("Message synchronized: (" + m.getType() + ") " +  m.getContent());

        return response;

    }
}