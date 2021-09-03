package sync;

import java.util.Set;
import java.util.SortedSet;

import core.Message;
import core.MessageImpl;
import core.PubSubCommand;

public class SyncAddrCommand implements PubSubCommand {

    @Override
    public Message execute(Message m, SortedSet<Message> log, Set<String> subscribers, boolean isPrimary,
                           String secondaryServerAddress, int secondaryServerPort) {
        Message response = new MessageImpl();

        response.setLogId(m.getLogId());

        response.setContent(secondaryServerAddress);
        response.setBrokerId(secondaryServerPort);

        return response;
    }
}