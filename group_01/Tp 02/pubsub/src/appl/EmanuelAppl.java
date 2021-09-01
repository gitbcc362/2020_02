package appl;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import core.Message;

public class EmanuelAppl {

    //Broker info
    public final int BROKER_PORT = 8080;
    public final String BROKER_ADDRESS = "localhost";//"54.227.20.6";
    //Client info
    public final int CLIENT_PORT = 8083;
    public final String CLIENT_ADDRESS = "localhost";
    public final String CLIENT_NAME = "Emanuel";
    //message contents
    public int myLogId;
    public final String ESTADO_1 = "QueroAssistir";
    public final String ESTADO_2 = "Assistindo";
    public final String ESTADO_3 = "AcabeiDeAssistir";
    public final String SEPARATOR = "/";
    public final String[] CATALOGUE = {
            "Tropa de Elite",
            "Homem Aranha: de volta ao bar",
            "El camino",
            "Army of the Dead",
            "Batman o Cavaleiro do mato",
            "Shrek",
            "BumbleBee",
            "Barraca do Beijo",
            "Escola do Rock",
            "A baba",
            "Jumanji",
            "Warcraft",
            "Coracoes de Ferro",
            "Minha mae e uma peca",
            "Truque de Mestre"
    };

    public EmanuelAppl() {
        PubSubClient client = new PubSubClient();
        client.startConsole();
    }

    
    public EmanuelAppl(boolean flag) {
        // Constrói um Client
        PubSubClient usuarioNetflix = new PubSubClient(CLIENT_ADDRESS, CLIENT_PORT);
        // Se inscreve no broker
        usuarioNetflix.subscribe(BROKER_ADDRESS, BROKER_PORT);
        // Simulando o tempo de inscrição
        sleep(3000,"");
        // Recupera o log
        List<Message> log = usuarioNetflix.getLogMessages();
        log = removeTrash(log);
        // Imprime o log
        System.out.println("Log List (" + CLIENT_NAME + "):");
        printLog(log);
        // Verificar se o log está vazio
        boolean flag_log_vazio = true;
        Iterator<Message> it = log.iterator();
        while(it.hasNext() && flag_log_vazio) {
            Message aux = it.next();
            String content = aux.getContent();
            if(
                    content.contains("QueroAssistir") ||
                            content.contains("Assistindo") ||
                            content.contains("AcabeiDeAssistir")
            ) {
                flag_log_vazio = false;
            }
        }
        //template do conteudo da mensagem:
        //"nomeUsuario/estado/filme/logID(estado1)/logID(estado2)/logID(estado3)"
        for(int k=0; k<3; k++) {
            // 1- fazer o primeiro publish de QUERO ASSISTIR um filme aleatorio do catalogo
            int movieIndex = new Random().nextInt(CATALOGUE.length);
            //Se o log estiver vazio, ele publicará a mensagem e será setado para ser o próximo que quer assistir
            //Nota: estamos considerando log vazio aquele que ninguem tentou assistir algo
            if (flag_log_vazio) {
                String msg =
                        CLIENT_NAME + SEPARATOR +
                                ESTADO_1 + SEPARATOR +
                                CATALOGUE[movieIndex] + SEPARATOR +
                                (log.size()+1) + SEPARATOR +
                                "0" + SEPARATOR +
                                "0";
                usuarioNetflix.publish(
                        msg,
                        BROKER_ADDRESS,
                        BROKER_PORT
                );
                flag_log_vazio = false;
                myLogId = log.size()+1;
            } else {
                // Se o log não estiver vazio ele irá encontrar o ultimo conteudo do
                // log que contenha uma mensagem com os estados
                log = usuarioNetflix.getLogMessages();
                log = removeTrash(log);
                String[] contentSplit = getLastContentSplited(log);

                // Após encontrar o ultimo conteudo que tenha algum estado na mensagem
                // se a posição que representa quem é o proximo a assistir for maior que zero
                // ou seja, já temos um próximo a assistir definido, apenas manteremos o valor das posições finais do conteudo
                if (Integer.parseInt(contentSplit[3]) > 0) {
                    String msg =
                            CLIENT_NAME + SEPARATOR +
                                    ESTADO_1 + SEPARATOR +
                                    CATALOGUE[movieIndex] + SEPARATOR +
                                    contentSplit[3] + SEPARATOR +
                                    contentSplit[4] + SEPARATOR +
                                    contentSplit[5];
                    usuarioNetflix.publish(
                            msg,
                            BROKER_ADDRESS,
                            BROKER_PORT
                    );
                    flag_log_vazio = false;
                    boolean flag_achou = false;
                    log = usuarioNetflix.getLogMessages();
                    log = removeTrash(log);

                    for(int z=log.size()-1; z>=0 && !flag_achou; z--) {
                        if (log.get(z).getContent().equals(msg)) {
                            myLogId = z+1;
                            flag_achou = true;
                        }
                    }

                }
                // se a posição que representa quem é o proximo a assistir for zero
                // então podemos definir que o próximo a assistir será ele mesmo
                else {
                    String msg =
                            CLIENT_NAME + SEPARATOR +
                                    ESTADO_1 + SEPARATOR +
                                    CATALOGUE[movieIndex] + SEPARATOR +
                                    (log.size() + 1) + SEPARATOR +
                                    contentSplit[4] + SEPARATOR +
                                    contentSplit[5];
                    usuarioNetflix.publish(
                            msg,
                            BROKER_ADDRESS,
                            BROKER_PORT
                    );
                    flag_log_vazio = false;
                    boolean flag_achou = false;
                    log = usuarioNetflix.getLogMessages();
                    log = removeTrash(log);

                    for(int z=log.size()-1; z>=0 && !flag_achou; z--) { //get
                        if (log.get(z).getContent().equals(msg)) {
                            myLogId = z+1;
                            flag_achou = true;
                        }
                    }
                }
            }

            // Verificar se posso assistir agora
            boolean myTurn = false;
            while (!myTurn) {
                log = usuarioNetflix.getLogMessages();
                log = removeTrash(log);
                String[] lastContent = getLastContentSplited(log);
                if (Integer.parseInt(lastContent[3]) == myLogId && Integer.parseInt(lastContent[4]) == 0) {
                    myTurn = true;
                    boolean achei = false;
                    int nextId = 0;
                    for (int i = myLogId ; i < log.size() && !achei; i++) { //era +1
                        if (log.get(i).getContent().contains("QueroAssistir")) {
                            nextId = i+1;
                            achei = true;
                        }
                    }
                    String msg =
                            CLIENT_NAME + SEPARATOR +
                                    ESTADO_2 + SEPARATOR +
                                    CATALOGUE[movieIndex] + SEPARATOR +
                                    nextId + SEPARATOR +
                                    myLogId + SEPARATOR +
                                    lastContent[5];
                    usuarioNetflix.publish(
                            msg,
                            BROKER_ADDRESS,
                            BROKER_PORT
                    );
                    flag_log_vazio = false;
                    sleep(6000, CLIENT_NAME + " esta Assistindo " + CATALOGUE[movieIndex]);
                    System.out.println(CLIENT_NAME + " acabou de Assitir " + CATALOGUE[movieIndex]);

                    log = usuarioNetflix.getLogMessages();
                    log = removeTrash(log);
                    lastContent = getLastContentSplited(log);

                    msg =
                            CLIENT_NAME + SEPARATOR +
                                    ESTADO_3 + SEPARATOR +
                                    CATALOGUE[movieIndex] + SEPARATOR +
                                    lastContent[3] + SEPARATOR +
                                    "0" + SEPARATOR +
                                    myLogId;
                    usuarioNetflix.publish(
                            msg,
                            BROKER_ADDRESS,
                            BROKER_PORT
                    );
                    flag_log_vazio = false;
                    sleep(3000,"");
                }
                log = usuarioNetflix.getLogMessages();
                log = removeTrash(log);
                printLog(log);
                sleep(3000, "Aguardando ...");
            }
        }

        usuarioNetflix.unsubscribe(BROKER_ADDRESS, BROKER_PORT);
        usuarioNetflix.stopPubSubClient();

    }

    public void sleep(int time, String msg) {
        try {
            System.out.println(msg);
            Thread.sleep(time);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void printLog(List<Message> logList) {
        Iterator<Message> it = logList.iterator();
        int id = 1;
        System.out.println("[");
        while (it.hasNext()) {
            Message aux = it.next();
            System.out.println("=====(LogID: " + id + ") " + aux.getContent() + ", ");
            id++;
        }
        System.out.println("]");
    }

    public String[] getLastContentSplited(List<Message> log){
        ListIterator<Message> lit = log.listIterator(log.size());
        int id = log.size();
        boolean foundLastLog = false;
        String lastContent = "";
        while(lit.hasPrevious() && !foundLastLog) {
            Message aux = lit.previous();
            String content = aux.getContent();
            if(
                    content.contains("QueroAssistir") ||
                            content.contains("Assistindo") ||
                            content.contains("AcabeiDeAssistir")
            ) {
                foundLastLog = true;
                lastContent = content;
            }
            id--;
        }
        return lastContent.split("/");
    }

    public List<Message> removeTrash(List<Message> log){
        log.removeIf(msg -> msg.getContent().contains("localhost"));
        return log;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new EmanuelAppl(true);
    }

}
