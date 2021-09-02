package appl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



import core.Message;
import core.MessageComparator;

public class OneAppl {
	public static List<Message> lastAcquiresLog = new ArrayList<Message>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new OneAppl(true);
	}
	
	public OneAppl(){
		PubSubClient client = new PubSubClient();
		client.startConsole();
	}
	
	public OneAppl(boolean flag){
		
		String brokerHostIntern = "10.182.0.3";
		
		String brokerHostExtern = "34.125.47.148";
		
		String brokerHost = "localhost";
		String clientHost = "localhost";
		String clientHostGCP = "10.182.0.4";
		int DEFAULT_PORT = 8080;
		
		
		
		PubSubClient lucas = new PubSubClient(clientHostGCP, 8081);

		
		lucas.subscribe(brokerHostIntern, DEFAULT_PORT);
		
		Thread accessOne = new SynchronizationProtocol(lucas, "lucas_acquire_X", brokerHostIntern, DEFAULT_PORT);				
		
		accessOne.start();
		
		
		try{
			accessOne.join();
			
		}catch (Exception e){
			
		}
		
		Thread accessTwo = new SynchronizationProtocol(lucas, "lucas_acquire_X", brokerHostIntern, DEFAULT_PORT);				
		
		accessTwo.start();
		
		
		try{
			accessTwo.join();
			
		}catch (Exception e){
			
		}
		
	
		List<Message> loglucas = lucas.getLogMessages();
	
		
		//Removendo mensagens de localhost
		List<Message> toRemove = removeWrongMessages(loglucas);
		loglucas.removeAll(toRemove);
		toRemove.clear();
		printLogMessages(loglucas, "Log Lucas Itens");
		
	
		Iterator<Message> it = loglucas.iterator();
		List<Message> acquires = new ArrayList<Message>();
		List<Message> releases = new ArrayList<Message>();
		while(it.hasNext()) {
			Message m = it.next();
			if(m.getContent().contains("acquire")) {
				acquires.add(m);
			}
			else {
				if(m.getContent().contains("release")) {
					releases.add(m);
				}
				
			}
		}
		System.out.println("\n");
		System.out.print("Acquires: " );
		for(Message m: acquires) {
			System.out.print(m.getContent() + m.getLogId()+ " | ");
		}
		System.out.println("\n");
		
		System.out.print("Releases: ");
		for(Message m: releases) {
			System.out.print(m.getContent() + m.getLogId()+  " | ");
		}
		System.out.println("\n");
		
		lucas.unsubscribe(brokerHostIntern, DEFAULT_PORT );
		
		
		lucas.stopPubSubClient();
		
	}
	
	private List<Message> removeWrongMessages(List<Message> logClient){
		List<Message> log = new ArrayList<Message>();
		for(Message message : logClient) {
			if(!message.getContent().contains("acquire") && 
					!message.getContent().contains("release"))
			{
				log.add(message);
			}
			
		}
		
		return log;
	}
	
	private void printLogMessages(List<Message> messages, String baseText) {
		Iterator<Message> it = messages.iterator();
		System.out.print(baseText + ":");
		while(it.hasNext()) {
			Message message = it.next();
			System.out.print(message.getContent() + message.getLogId()+ " | ");
		}
		System.out.println("\n");
	}
	class ThreadWrapper extends Thread{
		PubSubClient c;
		String msg;
		String host;
		int port;
		
		public ThreadWrapper(PubSubClient c, String msg, String host, int port){
			this.c = c;
			this.msg = msg;
			this.host = host;
			this.port = port;
		}
		public void run(){
			c.publish(msg, host, port);
		}
	}
	class SynchronizationProtocol extends Thread {
		PubSubClient client;
		String clientName;
		String host;
		String resource;
		String action;
		String message;
		int port;
		
		public SynchronizationProtocol(PubSubClient client,String message, 
				String host, int port) {
			this.client = client;
			this.message = message;
			this.clientName = message.split("_")[0];
			this.action = message.split("_")[1];
			this.resource = message.split("_")[2];
			this.port = port;
			this.host = host;
			
		}
		public void run() {
			try {
				Thread.currentThread().sleep(8000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Thread access = new ThreadWrapper(client, message, host, port);
			
			access.start();

			try {
				access.join();
			}
			catch (Exception error) {
				error.printStackTrace();
			}
			
			List<Message> logClient = client.getLogMessages();
			List<Message> acquires = new ArrayList<Message>(); 
			List<Message> toRemove = new ArrayList<Message>();
			List<Message> currentLog = new ArrayList<Message>();
			
			toRemove = removeWrongMessages(logClient);
			logClient.removeAll(toRemove);
			toRemove.clear();
			
			System.out.print("Log: ");
			for(Message m : logClient) {
				System.out.print(m.getContent()+m.getLogId()+" | ");
			}
			
			/*
			 * A cada "rodada" de acessos, verificamos se o log atual possui os acquires
			 * da rodada anterior e, dessa forma, evitamos que eles sejam novamente processados. 
			 */
			List<Message> replicatedMessages = new ArrayList<Message>();
			for(Message m : logClient) {
				for (Message m1 : lastAcquiresLog) {
					if(m.getContent().contentEquals(m1.getContent()) &&
							m.getLogId() == m1.getLogId()) {
						replicatedMessages.add(m);
					}
				}
			}
			
			System.out.println("\n");
			System.out.print("LastAcquiresLog: ");
			for(Message m : lastAcquiresLog) {
				System.out.print(m.getContent()+m.getLogId()+" | ");
			}
			
			
			logClient.removeAll(replicatedMessages);
			currentLog.addAll(logClient);
			
			
			for(Message m : logClient) {
				if(m.getContent().contains("acquire")) {
					acquires.add(m);
				}						
			}
			System.out.println("\n");
			
			System.out.print("Current Log: ");
			for(Message m : currentLog)
			{
				System.out.print(m.getContent() + m.getLogId()+  " | ");
			}
			System.out.println("\n");
			
			while (!acquires.isEmpty()){
				Message firstClientMessage = acquires.get(0);
				String firstClient = firstClientMessage.getContent();
				boolean stop = false;

				while(!stop){
					if(firstClient.contains(clientName) && firstClient.contains("acquire")){
						try {		
							
							System.out.println("---------------------------");
							System.out.println(firstClient.split("_")[0] + " acessou o recurso X\n");
							System.out.print("-----------------------------");
							
							lastAcquiresLog.add(firstClientMessage);
							
							System.out.println("\n");
							Thread.currentThread().sleep(5000);
							
							access = new ThreadWrapper(client, clientName.concat("_release_X"), host, port);
							
							access.start();
							try {
								access.join();
								Thread.currentThread().sleep(6000);
								
							} catch (Exception ignored) {}

						}catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					try{
						Thread.currentThread().sleep(3000);
						stop = true;
						
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
				if (!acquires.isEmpty()){
					try {
						Thread.currentThread().sleep(8000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					acquires.remove(0);
				}	
			}
			
		}		
	}
}