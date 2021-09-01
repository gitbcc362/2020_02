package core.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import core.Message;
import core.MessageImpl;

public class Client {

    private Socket s;

    public Client(String ip, int port) {
        try {
            s = new Socket(ip, port);
        } catch (Exception e) {
            System.out.println("Cannot connect with "+ ip + " on " + port);
        }
    }

    public Client(List<String> ip, List<Integer> port){
        boolean connected = false;
        int reseted = 0;
        for (int i = 0;i<ip.size() && !connected && reseted != ip.size();i++) {
            try {
                s = new Socket(ip.get(i), port.get(i));
                connected = true;
            }catch (Exception e){
                System.out.println("Client cannot connect with " + ip.get(i) + " on port: " + port.get(i) +
                        "\nTrying to connect with another broker");
                String auxip = ip.get(i);
                int auxport = port.get(i);
                ip.remove(i);
                port.remove(i);
                ip.add(auxip);
                port.add(auxport);
                i=-1;
                reseted++;
            }
        }
        if(!connected)
            System.out.println("No brokers available.");
    }


    public Message sendReceive(Message msg) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
            out.writeObject(msg);
            out.flush();

            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
            Message response = (Message) in.readObject();

            in.close();
            out.close();
            s.close();
            return response;
        } catch (Exception e) {
            return null;
        }
    }

}
