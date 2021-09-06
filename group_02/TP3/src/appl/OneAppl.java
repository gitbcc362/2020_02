package appl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import core.Message;


public class OneAppl {
    public static String managerIp = "localhost";
    public static int    managerPort = 8080;
    public        String[] resources = {"var X", "var Y", "var Z"};
    public        String identifier;

    public        String internalIp;
    private       Scanner reader;


    public OneAppl() throws Exception {
        PubSubClient client = new PubSubClient();
        client.startConsole();
    }

    public OneAppl(boolean flag) throws Exception {
        reader = new Scanner(System.in);
        System.out.print("Enter the Client port number: ");
        int clientPort = reader.nextInt();

        System.out.print("Enter the identifier name: ");
        identifier = reader.next();

        ArrayList<String> vars = new ArrayList<String>();

        for (int i = 0; i < 10; i++) {
            Random rand = new Random();
            int r = rand.nextInt(resources.length);
            vars.add(resources[r]);
        }

        try {
            internalIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PubSubClient client = new PubSubClient(internalIp, clientPort);
        client.subscribe(managerIp, managerPort);

        while (!vars.isEmpty()) {
            String varName = vars.remove(vars.size() - 1);

            try {
                Thread accessOne = new ThreadWrapper(
                        client,
                        "lock:" + identifier + ":var:" + varName,
                        managerIp,
                        managerPort,
                        identifier,
                        varName
                );

                accessOne.start();
                accessOne.join();
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<Message> logClient = client.getLogMessages();

            Iterator<Message> it = logClient.iterator();
            System.out.print("Log client items: ");
            while (it.hasNext()) {
                Message message = it.next();
                System.out.print(message.getContent() + " " + message.getLogId() + " | ");
            }
            System.out.println();
        }

        client.unsubscribe(managerIp, managerPort);

        client.stopPubSubClient();
    }

    public static void main(String[] args) throws Exception {
        new OneAppl(true);
    }

    class ThreadWrapper extends Thread {
        public PubSubClient c;
        public String msg;
        public String host;
        public String identifier;
        public String varName;
        public int port;

        public String backupId = "localhost";
        public int backupPort = 8081;

        public ThreadWrapper(PubSubClient c, String msg, String host, int port, String identifier, String varName) {
            this.c = c;
            this.msg = msg;
            this.host = host;
            this.port = port;
            this.identifier = identifier;
            this.varName = varName;
        }

        public void run() {
            try {
                c.publish(msg, host, port);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            boolean access = false;
            boolean iteration = true;
            String lastLock = "";
            String lastLockVar = "";
            int lastLockId = -1;
            int firstLockIteration = 0;
            try {
                while (!access) {
                    List <Message> log;
                    Iterator<Message> it;
                    try {
                        c.publish("ping", host, port);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    try {
                        log = c.getLogMessages();
                        it = log.iterator();
                    } catch (Exception e) {
                        continue;
                    }

                    while (it.hasNext()) {
                        Message aux = it.next();
                        String message = aux.getContent();

                        boolean showLogMessage = Character.isDigit(message.charAt(0)) || message.startsWith("localhost") || message.startsWith("ping");
                        if (showLogMessage) {
                            continue;
                        }

                        String[] messageSplit = message.split(":");
                        String messageType = messageSplit[0];
                        String messageIdentifier = messageSplit[1];
                        String messageVarName = messageSplit[3];

                        if (!messageVarName.equals(varName)) {
                            continue;
                        }

                        boolean isALockMessageToMyVar = messageType.equals("lock") && messageVarName.equals(varName);
                        boolean isMyTurnToAccess = messageType.equals("unlock") && messageIdentifier.equals(lastLock) && messageVarName.equals(lastLockVar);

                        if (iteration) {
                            if (isALockMessageToMyVar) {
                                firstLockIteration++;
                            }
                            boolean itIsMe = messageIdentifier.equals(identifier);
                            boolean isIsALockMessage = messageType.equals("lock");

                            if (!itIsMe && isIsALockMessage) {
                                lastLock = messageIdentifier;
                                lastLockVar = messageVarName;
                                lastLockId = aux.getLogId();
                                System.out.println("LAST LOCK: " + lastLock + "-" + lastLockVar);
                            }
                        }
                        else if(firstLockIteration == 1) {
                            access = true;
                            System.out.println("FIRST ACCESS");
                            break;
                        }
                        else if (isMyTurnToAccess && aux.getLogId() > lastLockId) {
                            access = true;
                            System.out.println("MY TURN TO ACCESS");
                            break;
                        }
                        else if (lastLock.equals("")) {
                            access = true;
                            System.out.println("MY ACCESS AGAIN");
                            break;
                        }
                    }
                    iteration = false;
                }

                System.out.println("access var " + varName);

                int randomTime = ThreadLocalRandom.current().nextInt(5000, 10000 + 1);
                Thread.sleep(randomTime);

                msg = "unlock:" + identifier + ":var:" + varName;
                c.publish(msg, host, port);

                System.out.println("Release var " + varName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}