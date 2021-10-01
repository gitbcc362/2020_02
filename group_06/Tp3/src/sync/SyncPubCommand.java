package sync;

import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CopyOnWriteArrayList;
import core.client.Client;

import core.Message;
import core.MessageImpl;
import core.PubSubCommand;

public class SyncPubCommand implements PubSubCommand {

    @Override
    public Message execute(Message m, SortedSet<Message> log, Set<String> subscribers, boolean isPrimary,
                           String sencondaryServerAddress, int secondaryServerPort, boolean secActivity) {

        Message response = new MessageImpl();

        response.setLogId(m.getLogId());

        String[] secMessage = m.getContent().split(" ");
        
        if(secMessage[0].equals("notify")) {
            m.setContent(secMessage[1] + " " + secMessage[2]);

            Message msg = new MessageImpl();
            msg.setContent(m.getContent()); // conteudo == posso verificar o tipo de conteudo
            msg.setLogId(m.getLogId());
            msg.setType("notify");

            CopyOnWriteArrayList<String> subscribersCopy = new CopyOnWriteArrayList<String>(); 
            subscribersCopy.addAll(subscribers);    		
            Integer length = subscribersCopy.size()/2;
    
            for(int i = length; i < subscribersCopy.size(); i++){
                System.out.println("Enviando notify para: " + subscribersCopy.get(i));
				String aux = subscribersCopy.get(i);
				String[] ipAndPort = aux.split(":");
				Message cMsg = null;

				try {
					Client client = new Client(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
					msg.setBrokerId(m.getBrokerId()); // manda e espera
					cMsg = client.sendReceive(msg); // se não recebeu vai ser null e ele tira da lista de sub
				} catch (Exception e) { }
	
				if (cMsg == null) {
					System.out.println("Publish service is not proceed... " + msg.getContent());
					subscribers.remove(aux);
				}
            }

            System.out.println("Fim notify compartilhado");
        }
                
        log.add(m);

        System.out.println("LOG ADICIONADO: " + m.getContent());

        response.setContent("Message published on backup: " + m.getContent());
        response.setType("pubsync_ack");

        return response;
    }

}
