package core.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import core.Message;
import core.MessageImpl;

public class Client {
	
	private Socket s;
	
	public Client(String ip, int port) throws Exception{
		s = new Socket(ip, port);
	
	}

	public Message sendReceive(Message msg){
		try{
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
			out.writeObject(msg);
			out.flush();
			
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
			Message response = (Message) in.readObject();
			
			in.close();
			out.close();			
			s.close();
			return response;
		}catch(Exception e){
			return null;
		}
	}

}
