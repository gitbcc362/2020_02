package appl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import core.Message;

public class OneAppl {

    public OneAppl() {
        PubSubClient client = new PubSubClient();
        client.startConsole();
    }

    public OneAppl(int numberOfClients, int numberOfAccess, String clientAddress, int clientPort, String brokerAddress, int brokerPort) {
        // Criando os clientes
        List<PubSubClient> clients = new ArrayList<>();
        for (int i = 0; i < numberOfClients; ++i) {
            // Criando client
            clients.add(new PubSubClient("Client " + i, clientAddress, clientPort + i));
            // Subscrevendo client
            clients.get(i).subscribe(brokerAddress, brokerPort);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<Thread> threads = new ArrayList<>();
        for (int j = 0; j < numberOfAccess; ++j) {
            for (PubSubClient client : clients) {
                // Criando a thread que vai tentar dar lock no topico, publicar e então dar unlock
                threads.add(new ThreadWrapper(client, "varX", brokerAddress, brokerPort));
                threads.get(threads.size() - 1).start();
                threads.add(new ThreadWrapper(client, "varY", brokerAddress, brokerPort));
                threads.get(threads.size() - 1).start();
                threads.add(new ThreadWrapper(client, "varZ", brokerAddress, brokerPort));
                threads.get(threads.size() - 1).start();
            }
        }

        // Aguardando a conclusão da execução das threads
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Desinscrevendo os clients
        for (PubSubClient client : clients) {
            client.unsubscribe(brokerAddress, brokerPort);
            client.stopPubSubClient();
        }
    }

    public static void main(String[] args) {
        int numberOfClients = 5, numberOfAccess = 5, clientPort = 23000, brokerPort = 22000;
        String clientAddress = "localhost", brokerAddress = "localhost";
        Iterator<String> itArgs = Arrays.stream(args).iterator();
        while (itArgs.hasNext()) {
            String arg = itArgs.next();
            switch (arg) {
                case "-ip" -> clientAddress = itArgs.next();
                case "-p" -> clientPort = Integer.parseInt(itArgs.next());
                case "-bip" -> brokerAddress = itArgs.next();
                case "-bp" -> brokerPort = Integer.parseInt(itArgs.next());
                case "-nc" -> numberOfClients = Integer.parseInt(itArgs.next());
                case "-na" -> numberOfAccess = Integer.parseInt(itArgs.next());
                default -> {
                    System.out.println("Comando \"" + arg + "\" inválido! Execução abortada");
                    return;
                }
            }
        }
        new OneAppl(numberOfClients, numberOfAccess, clientAddress, clientPort, brokerAddress, brokerPort);
    }

    static class ThreadWrapper extends Thread {
        PubSubClient c;
        String var;
        String host;
        int op;
        int port;

        public ThreadWrapper(PubSubClient c, String var, String host, int port) {
            this.c = c;
            this.var = var;
            this.host = host;
            this.port = port;
            this.op = ThreadLocalRandom.current().nextInt(0, 100000);
        }

        public void run() {
            // No início da execução, todos os clients desejam escrever no topic, então todos apresentam a requisição de lock
            c.publish("intent_to_lock " + op + " " + var + " ", host, port);
            while (true) {
                List<Message> log = c.getLogMessages();
                while (log == null) {
                    try {
                        sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log = c.getLogMessages();
                }
                for (Message msg : log) {
                    String content = msg.getContent();
                    if (content.contains(var)) {
                        if (content.contains("unlock")) {
                            String[] parts = content.split(" ");
                            for (Message rem : log) {
                                String remContent = rem.getContent();
                                if (remContent.contains(parts[1])) {
                                    log.remove(rem);
                                }
                            }
                            log.remove(msg);
                        } else if (!content.contains("intent_to_lock") && !content.contains("lock")) {
                            log.remove(msg);
                        }
                    } else {
                        log.remove(msg);
                    }
                }
                if (log.get(0).getContent().contains("intent_to_lock " + op + " " + var)) {
                    break;
                }
                // Impressão do Debug
                /*StringBuilder debug = new StringBuilder("DEBUG " + c.getClientName() + " Op " + op + " " + var + " -> ");
                for (Message aux : c.getLogMessages()) {
                    debug.append(aux.getContent()).append(aux.getLogId()).append(" | ");
                }
                System.out.println(debug);*/
            }
            c.publish("lock " + op + " " + var, host, port);
            try {
                sleep(ThreadLocalRandom.current().nextInt(0, 5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            c.publish("publishing " + op + " " + var + " ", host, port);
            c.publish("unlock " + op + " " + var + " ", host, port);

            // Impressão do log
            StringBuilder debug = new StringBuilder("LOG " + c.getClientName() + " Op " + op + " " + var + " -> ");
            for (Message aux : c.getLogMessages()) {
                debug.append(aux.getContent()).append(aux.getLogId()).append(" | ");
            }
            System.out.println(debug);
        }
    }
}
