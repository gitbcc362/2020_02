package appl;

import java.util.Iterator;
import java.util.List;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import core.Message;


public class OneAppl {
    public static String managerIp = "10.128.0.2";
    public static int    managerPort = 8080;
    public        String varName;
    public        String identifier;

    public        String internalIp;


    public OneAppl() {
        PubSubClient client = new PubSubClient();
        client.startConsole();
    }

    public OneAppl(boolean flag) {

        Scanner reader = new Scanner(System.in);
        System.out.print("Enter the Client port number: ");
        int clientPort = reader.nextInt();

        System.out.print("Enter the identifier name: ");
        identifier = reader.next();

        varName = "";
        System.out.print("Enter the var name: ");
        varName = reader.next();

        try {
            internalIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PubSubClient client = client = new PubSubClient(internalIp, clientPort);
        client.subscribe(managerIp, managerPort);

        while (!varName.toLowerCase().equals("exit")) {
            Thread accessOne = new ThreadWrapper(client, "lock:" + identifier + ":var:" + varName, managerIp, managerPort, identifier, varName);
            accessOne.start();

            try {
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

            System.out.println("Enter the var name: ");
            varName = reader.next();
        }

        client.unsubscribe(managerIp, managerPort);

        client.stopPubSubClient();
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new OneAppl(true);
    }

    class ThreadWrapper extends Thread {
        PubSubClient c;
        String msg;
        String host;
        String identifier;
        String varName;
        int port;

        public ThreadWrapper(PubSubClient c, String msg, String host, int port, String identifier, String varName) {
            this.c = c;
            this.msg = msg;
            this.host = host;
            this.port = port;
            this.identifier = identifier;
            this.varName = varName;
        }

        public void run() {
            c.publish(msg, host, port);

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
                        log = c.getLogMessages();
                        it = log.iterator();
                    } catch (Exception e) {
                        continue;
                    }

                    while (it.hasNext()) {
                        Message aux = it.next();
                        String message = aux.getContent();

                        boolean showLogMessage = Character.isDigit(message.charAt(0)) || message.startsWith("localhost");
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
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}