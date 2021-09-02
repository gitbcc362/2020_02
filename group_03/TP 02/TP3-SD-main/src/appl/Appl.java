package appl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import appl.Appl.SynchronizationProtocol;
import core.Message;
import core.MessageComparator;

public class Appl {
	public static List<Message> lastAcquiresLog = new ArrayList<Message>();
	public static String lastResourceLog = new String();
	
	public static void main(String[] args) {
		
		String clientName = new String();
		String brokerHost = new String();
		String clientHost = new String();
		
		int brokerPort=8080, clientPort=0;
		
		Scanner input = new Scanner(System.in);
		
		int option = -1;
		
		String address_machine1 = "10.182.0.4";
		String address_machine2 = "10.182.0.5";
		int client_machine_port1 = 8084;
		int client_machine_port2 = 8086;
		
		brokerHost = "10.182.0.2";
		
		System.out.println("----- Configurações para Aplicações ----------");
		System.out.println("\nEntre com o nome do cliente: ");
		clientName = input.nextLine();
		
		System.out.println("\n[1] Connect on machine: 10.182.0.4\n[2] Connect on machine: 10.182.0.5\n ");
		option = input.nextInt();
		switch (option) {
		case 1:
			clientHost = address_machine1;
			clientPort = client_machine_port1;
			break;
		case 2:
			
			clientHost = address_machine2;
			clientPort = client_machine_port2;
			break;
		
		default:
			break;
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		new Appl(clientName, brokerHost, brokerPort, clientHost, clientPort);
		input.close();
	}
	
	public Appl(){
		PubSubClient client = new PubSubClient();
		client.startConsole();
	}
	
	public Appl(String clientName, String brokerHost, int brokerPort, String clientHost, int clientPort){
		
		PubSubClient client = new PubSubClient(clientHost, clientPort);
		
		
		
		client.subscribe(brokerHost, brokerPort);
		Random rnd = new Random();
		
		String [] resources = {"X", "Y"};
		String lastSrc = "";
		for(int i=0; i<9; i++) {
			String src = resources[rnd.nextInt(2)];
			
			Thread accessOne = new SynchronizationProtocol(client, clientName.concat("_acquire_X"),
					brokerHost, brokerPort);
			
			
			accessOne.start();
			try{
				accessOne.join();	
			}catch (Exception e){}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		List<Message> logclient = client.getLogMessages();
	
		//Removendo mensagens de localhost
		List<Message> toRemove = removeWrongMessages(logclient);
		logclient.removeAll(toRemove);
		toRemove.clear();
		printLogMessages(logclient, "Log client Itens");
		
	
		Iterator<Message> it = logclient.iterator();
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
			System.out.print(m.getContent() + m.getLogId() +  " | ");
		}
		System.out.println("\n");
		
		System.out.print("Releases: ");
		for(Message m: releases) {
			System.out.print(m.getContent() + m.getLogId() + " | ");
		}
		System.out.println("\n");
		
		client.unsubscribe(brokerHost, brokerPort);
		
		
		client.stopPubSubClient();
		
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
			
			
			List<Message> replicatedMessages = new ArrayList<Message>();
			
			for(Message m : logClient) {
				for (Message m1 : lastAcquiresLog) {
					if(m.getContent().contentEquals(m1.getContent()) &&
							m.getLogId() == m1.getLogId()) {
						replicatedMessages.add(m);
					}
				}
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
				String src = firstClientMessage.getContent().split("_")[2];
				boolean stop = false;

				while(!stop){
					if(firstClient.contains(clientName)
							&& firstClient.contains("acquire")){
						try {		
							
							System.out.println("---------------------------");
							System.out.println(firstClient.split("_")[0] +
									" acessou o recurso X");
							System.out.println();
							System.out.print("-----------------------------");
							
							lastAcquiresLog.add(firstClientMessage);
							
							System.out.println("\n");
							Thread.currentThread().sleep(800);
							
							access = new ThreadWrapper(client, clientName.concat("_release_X"),
									host, port);
							
							access.start();
							try {
								access.join();
								Thread.currentThread().sleep(800);
								
							} catch (Exception ignored) {}

						}catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
					
					try{
						Thread.currentThread().sleep(800);
						stop = true;
						
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
				if (!acquires.isEmpty()){
					acquires.remove(0);
				}
				else {
					
					//break;
				}
			}
			
		}		
	}
}