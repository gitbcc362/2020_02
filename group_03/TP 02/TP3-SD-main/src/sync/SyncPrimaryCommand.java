package sync;

import java.util.Set;
import java.util.SortedSet;

import core.Message;
import core.MessageImpl;
import core.PubSubCommand;

public class SyncPrimaryCommand implements PubSubCommand{

	@Override
	public Message execute(Message m, SortedSet<Message> log, Set<String> subscribers, boolean isPrimary,
			String secondaryServerAddress, int secondaryServerPort) {
	
		System.out.println("Sync Primary Command: " + secondaryServerAddress + " port: " + secondaryServerPort);
		Message response = new MessageImpl();
			
		response.setLogId(m.getLogId());
		
		
		log.add(m);
		
		response.setContent(secondaryServerAddress);
		response.setBrokerId(secondaryServerPort);
		response.setType("sync_primary_ack");
		
		return response;
	}

}
