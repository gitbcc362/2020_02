package appl;

import java.util.Random;

public class OneAppl {

	private static String serverIp;
	private static int serverPort;

	private static String secundaryServerIp;
	private static int secundaryServerPort;

	private static String clientIp;
	private static int clientPort;

	public static void setServerIp(String ip) { serverIp = ip; }
	public static String getServerIp() { return serverIp; }
	public static void setServerPort(int port) { serverPort = port; }
	public static int getServerPort() { return serverPort; }

	public static void setSecServerIp(String ip) { secundaryServerIp = ip; }
	public static String getSecServerIp() { return secundaryServerIp; }
	public static void setSecServerPort(int port) { secundaryServerPort = port; }
	public static int getSecServerPort() { return secundaryServerPort; }

	public static void setClientIp(String ip) { clientIp = ip; }
	public static String getClientIp() { return clientIp; }
	public static void setClientPort(int port) { clientPort = port; }
	public static int getClientPort() { return clientPort; } 

	public static void main(String[] args) throws InterruptedException {
		setServerIp(args[0]);
		setServerPort(Integer.parseInt(args[1]));

		setSecServerIp(args[2]);
		setSecServerPort(Integer.parseInt(args[3]));

		setClientIp(args[4]);
		setClientPort(Integer.parseInt(args[5]));
		
		new OneAppl(true);
	}
	
	public OneAppl(boolean flag){
		PubSubClient client = 
			new PubSubClient(clientIp, clientPort, serverIp, 
				serverPort, secundaryServerIp, secundaryServerPort);

		String resources[] = {"W", "X", "Y", "Z"};

		client.subscribe("");
		
		try {
			for(int i = 0;i < 99999; i++){
				Random rand = new Random();
				int r = rand.nextInt(resources.length);
				String var = resources[r];

				String result = client.lock(var);		//acquire lock
				client.consume(result);
				client.unlock(var);				//release unlock
			}

			client.unsubscribe();
			client.stopPubSubClient();
		} catch (InterruptedException e) {
			System.out.println("thread error");
		}
	}

}
