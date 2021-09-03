package appl;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Collections;

import core.Message;

public class OneAppl {

	String[] vars = { "Abacaxi", "Mamao", "NASDAQ", "Passagem de Aviao", "Compra na Amazon" };

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new OneAppl(true);
	}

	public OneAppl() {
		PubSubClient client = new PubSubClient();
		client.startConsole();
	}

	public OneAppl(boolean flag) {

		Scanner scanner = new Scanner(System.in);

		System.out.println("Endereço do broker");
		String brokerAddr = "localhost";// scanner.next();

		System.out.println("Porta do broker");
		int brokerPort = 8080;// scanner.nextInt();

		int totalClients = 1;
		int machineId = 1;

		PubSubClient client = new PubSubClient("localhost", 8082);
		client.subscribe(brokerAddr, brokerPort);

		Thread accessOne = new ThreadWrapper(client, machineId + ": " + "Abacate", brokerAddr, brokerPort, machineId,
				totalClients);
		accessOne.start();

		try {

			accessOne.join();

		} catch (Exception e) {

		}

		client.unsubscribe("localhost", 8080);

		client.stopPubSubClient();

	}

	class ThreadWrapper extends Thread {
		PubSubClient c;
		String msg;
		String host;
		int port;

		int machineId;
		int totalClients;

		public ThreadWrapper(PubSubClient c, String msg, String host, int port, int id, int totalClients) {
			this.c = c;
			this.msg = msg;
			this.host = host;
			this.port = port;
			this.machineId = id;
			this.totalClients = totalClients;
		}

		public void run() {

			Random randGen = new Random();

			while (true) {

				try {
					this.sleep(1000);
				} catch (InterruptedException e) {

				}

				List<Message> log = c.getLogMessages();
				if (log.size() > 0)
					Collections.reverse(log);
				Iterator<Message> it = log.iterator();

				String newMsg = this.machineId + ": " + vars[randGen.nextInt(vars.length)];

				if (!it.hasNext()) {
					c.publish(newMsg, host, port);
				}

				while (it.hasNext()) {
					Message aux = it.next();

					try {
						int id = Integer.parseInt(aux.getContent().split(": ")[0]);
						if (id + 1 == this.machineId || (this.machineId == 1 && id == this.totalClients)) {
							System.out.println("Received from " + aux.getContent());
							System.out.println(machineId + " publish " + newMsg);
							System.out.println("----------------------------------");
							c.publish(newMsg, host, port);
							break;
						}
					} catch (NumberFormatException e) {
						continue;
					}

				}
			}

		}
	}

}
