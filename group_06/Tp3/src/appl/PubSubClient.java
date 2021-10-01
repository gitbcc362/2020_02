package appl;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import core.Message;
import core.MessageImpl;
import core.Server;
import core.client.Client;
import java.util.Set;
import java.util.SortedSet;

public class PubSubClient { // recebe e publica

	private Server observer;
	private ThreadWrapper clientThread;

	private String clientAddress;
	private int clientPort;

	private boolean primaryActivity = true;

	private String brokerIp;
	private int brokerPort;

	private String primaryBrokerIp;
	private int primaryBrokerPort;

	private String secundaryBrokerIp;
	private int secundaryBrokerPort;

	public PubSubClient() {
		// this constructor must be called only when the method
		// startConsole is used
		// otherwise the other constructor must be called
	}

	public PubSubClient(String clientAddress, int clientPort,
		String brokerAddress, int brokerPort,
		String secBrokerAddress, int secBrokerPort) {
		this.clientAddress = clientAddress;
		this.clientPort = clientPort;

		this.primaryBrokerIp = brokerAddress;
		this.primaryBrokerPort = brokerPort;
		
		this.secundaryBrokerIp = secBrokerAddress;
		this.secundaryBrokerPort = secBrokerPort;

		this.brokerPort = brokerPort;
		this.brokerIp = brokerAddress;

		observer = new Server(clientPort);
		clientThread = new ThreadWrapper(observer);
		clientThread.start();
	}

	public void subscribe(String sec) {
		Message msgBroker = new MessageImpl();
		msgBroker.setBrokerId(this.brokerPort);
		msgBroker.setType(sec + "sub");
		msgBroker.setContent(clientAddress + ":" + clientPort);

		Client subscriber = null;
		Message response = null;

		try {
			subscriber = new Client(this.brokerIp, this.brokerPort);
			response = subscriber.sendReceive(msgBroker);
		} catch (Exception e) {
			System.out.println("entrei");
			try {
				toSecundary(msgBroker.getType(), msgBroker.getContent());
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		//System.out.println(response.getContent());

		if (response.getType().equals("backup")) {
			String brokerAddress = response.getContent().split(":")[0];
			int brokerPort = Integer.parseInt(response.getContent().split(":")[1]);

			try {
				subscriber = new Client(brokerAddress, brokerPort);
				subscriber.sendReceive(msgBroker);				
			} catch (Exception e) { }
		}
	}

	public void unsubscribe() throws InterruptedException{
		Message msgBroker = new MessageImpl();
		msgBroker.setBrokerId(this.brokerPort);
		msgBroker.setType("unsub");
		msgBroker.setContent(clientAddress + ":" + clientPort);

		Client subscriber = null;
		Message response = null;

		try {
			subscriber = new Client(this.brokerIp, this.brokerPort);
			response = subscriber.sendReceive(msgBroker);
		} catch (Exception e) {
			System.out.println("entrei");
			try {
				toSecundary(msgBroker.getType(), msgBroker.getContent());
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}


		if (response.getType().equals("backup")) {
            String brokerAddress = response.getContent().split(":")[0];
            int brokerPort = Integer.parseInt(response.getContent().split(":")[1]);
			
 			try {
				subscriber = new Client(brokerAddress, brokerPort);
				subscriber.sendReceive(msgBroker);				
			} catch (Exception e) {	}
        }
	}

	public void consume(String resource) throws InterruptedException {
		System.out.println("usando recurso = " + resource);
		Thread.sleep(8000);
		System.out.println("parei de usar = " + resource);
	}

	public String verificaVez(int myLogId, String myVar) throws InterruptedException {
		SortedSet<Message> logs = this.observer.getLogMessages();

		synchronized (logs) {
			while (verifyLogs(logs, myLogId, myVar) == false) {
				System.out.println("esperando");
				logs.wait();
			}
		}

		return myVar;
	}

	public void unlock(String message) {
		Message msgUnlock = new MessageImpl();
		msgUnlock.setBrokerId(this.brokerPort);
		msgUnlock.setType("unlock");
		msgUnlock.setContent("Unlock " + message);

		Client publisher = null;
		Message received = null;

		try {
			publisher = new Client(this.brokerIp, this.brokerPort);
			received = publisher.sendReceive(msgUnlock);			
		} catch (Exception e) {
			System.out.println("entrei");
			try {
				toSecundary(msgUnlock.getType(), msgUnlock.getContent());
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

	
		// Message received = publisher.sendReceive(msgUnlock);
	}

	public boolean verifyLogs(SortedSet<Message> logs, int myLogId, String myVar) { // verifico a quantidade de locks e
																				// unlocks no log para verificar se
																				// posso usar
		// System.out.println("entrei verify logs");
		Iterator<Message> it = logs.iterator();
		Integer locks = 0;
		Integer unlocks = 0;

		while (it.hasNext()) {
			Message aux = it.next();
			int id = aux.getLogId();
			String content = aux.getContent();

			if (id != myLogId) {
				String[] divideMessage = content.split(" ");

				if (divideMessage.length == 2) {
					// System.out.println("mensagem: " + divideMessage[0] + " " + divideMessage[1]);
					if (divideMessage[0].equals("Lock") && divideMessage[1].equals(myVar) && id < myLogId) {
						locks += 1;
					}
					if (divideMessage[0].equals("Unlock") && divideMessage[1].equals(myVar)) {
						unlocks += 1;
					}
				}
			}
		}

		System.out.println("numero de locs: " + locks + " numero de unlocks: " + unlocks);

		if (locks == unlocks)
			return true;

		return false;
	}

	public String lock(String var) throws InterruptedException {
		Message msgPub = new MessageImpl();
		msgPub.setBrokerId(this.brokerPort);
		msgPub.setType("pub");
		msgPub.setContent("Lock " + var);
		
		Client publisher = null;
		Message received = null;
		try {
			publisher = new Client(this.brokerIp, this.brokerPort);
			received = publisher.sendReceive(msgPub);			
		} catch (Exception e) {
			System.out.println("entrei");
			try {
				toSecundary(msgPub.getType(), msgPub.getContent());
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}


		//nao mecho
		if (received.getType().equals("backup")) {
            String brokerAddress = received.getContent().split(":")[0];
            int brokerPort = Integer.parseInt(received.getContent().split(":")[1]);

			try {
				publisher = new Client(brokerAddress, brokerPort);
				publisher.sendReceive(msgPub);				
			} catch (Exception e) {	}

        }

		// System.out.println(received.getLogId()); //id na minha maquina
		// System.out.println(received.getType());
		System.out.println(received.getContent());

		return verificaVez(received.getLogId(), var);
	}

	public void toSecundary(String typeMessage, String contentMessage) throws InterruptedException {
		if(primaryActivity) {
			primaryActivity = false;
			this.brokerIp = secundaryBrokerIp;
			this.brokerPort = secundaryBrokerPort;
		} else {
			primaryActivity = true;
			this.brokerIp = primaryBrokerIp;
			this.brokerPort = primaryBrokerPort;
		}

		//stopPubSubClient();

		Thread.sleep(1000);

		//observer = new Server(this.clientPort);
		//clientThread = new ThreadWrapper(observer);
		//clientThread.start();

		//subscribe("sec ");

		Message msgToSec = new MessageImpl();
		msgToSec.setBrokerId(this.brokerPort);
		msgToSec.setType("sec " + typeMessage);
		msgToSec.setContent(contentMessage);

		try {
			Client publisher = new Client(this.brokerIp, this.brokerPort);
			publisher.sendReceive(msgToSec);
		} catch (Exception e) {
			System.out.println("Broker cannot sendReceive");;
		}
	}

    public SortedSet<Message> getLogMessages() {
        return observer.getLogMessages();
    }

	public void stopPubSubClient() {
		System.out.println("Client stopped...");
		observer.stop();
		clientThread.interrupt();
	}

	class ThreadWrapper extends Thread {
		Server s;

		public ThreadWrapper(Server s) {
			this.s = s;
		}

		public void run() {
			s.begin();
		}
	}

}
