package appl;

import core.Server;
import java.util.Scanner;

public class Broker {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Broker();
	}
	
	public Broker(){		
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		
		System.out.print("Is the broker primary?: Y/N");
		String respYN = reader.next();
		
		boolean respBol;
		if(respYN.equalsIgnoreCase("Y")) respBol = true;
		else respBol = false;
		
		String primary_broker = "10.182.0.2";
		String secondary_broker = "10.182.0.3";
		String local_broker = "localhost";
		
		Server s;	
		if (respBol == true) {
			s = new Server(8080, true, secondary_broker, 8081);
		}
		else {
			s = new Server(8081, false, primary_broker, 8080);
		}
		ThreadWrapper brokerThread = new ThreadWrapper(s);
		brokerThread.start();
		
		System.out.print("Shutdown the broker (Y|N)?: ");
		String resp = reader.next(); 
		if (resp.equals("Y") || resp.equals("y")){
			System.out.println("Broker stopped...");
			s.stop();
			brokerThread.interrupt();
		}
		
		//once finished
		reader.close();
	}
	
	class ThreadWrapper extends Thread{
		Server s;
		public ThreadWrapper(Server s){
			this.s = s;
		}
		public void run(){
			s.begin();
		}
	}

}
