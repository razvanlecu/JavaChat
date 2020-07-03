
import java.net.*;
import java.io.*;
import java.util.*;

// Clientul poate rula in consola sau GUI.
public class Client  {

	// Citirea din socket si scrierea in socket
	private ObjectInputStream sInput;		
	private ObjectOutputStream sOutput;		
	private Socket socket;

	// Pentru utilizarea interfatei grafice
	private ClientGUI cg;
	
	// Pentru server, username, port
	private String server, username;
	private int port;

	// Constructorul apelat din consola
	Client(String server, int port, String username) {
		// Apeleaza constructorul comun cu interfata grafica null
		this(server, port, username, null);
	}

	// Constructorul apelat din interfata grafica
        //In consola ClientGUI este null
	Client(String server, int port, String username, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		// Salveaza daca suntem in GUI
		this.cg = cg;
	}
	
	//Pornirea dialogului
	public boolean start() {
		// Conectarea la server
		try {
			socket = new Socket(server, port);
		} 
		// Eroare de conectare
		catch(Exception ec) {
			display("Eroare conectare la server" + ec);
			return false;
		}
		
		String msg = "Conexiune acceptata " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		// Crearea ambelor streamuri de date
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exceptie creare noi Input/output Streams: " + eIO);
			return false;
		}

		// Creaza threadul pentru ascultarea serverului 
		new ListenFromServer().start();
		// Trimiterea username-ului catre server
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exceptie login : " + eIO);
			disconnect();
			return false;
		}
		return true;
	}

	// Trimiterea mesajului catre consola sau GUI
	private void display(String msg) {
		if(cg == null)
			System.out.println(msg);      // println pentru consola
		else
			cg.append(msg + "\n");		// append pentru GUI
	}
	
	// Trimiterea mesajului catre server
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exceptie scriere catre server: " + e);
		}
	}

	// Daca ceva nu este in regula inchide Input/Output si deconecteaza
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} 
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} 
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} 
		
		
		if(cg != null)
			cg.connectionFailed();
			
	}
	
	// In consola, daca exista o eroare, programul se opreste
	// In GUI, daca exista o eroare, se afiseaza in interfata
	public static void main(String[] args) {
		// Valorile default
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonim";

		// In functie de numarul de argumente introduse
		switch(args.length) {
			// > javac Client username portNumber serverAddr
			case 3:
				serverAddress = args[2];
			// > javac Client username portNumber
			case 2:
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid numar port.");
					System.out.println("Folositi: > java Client [username] [portNumber] [serverAddress]");
					return;
				}
			// > javac Client username
			case 1: 
				userName = args[0];
			// > java Client
			case 0:
				break;
			// Numar incorect de argumente
			default:
				System.out.println("Folositi: > java Client [username] [portNumber] {serverAddress]");
			return;
		}
		// Creare obiect Client
		Client client = new Client(serverAddress, portNumber, userName);
		// Testare daca putem porni conexiunea catre server
		if(!client.start())
			return;
		
		// Asteptare mesaj user
		Scanner scan = new Scanner(System.in);
		while(true) {
			System.out.print("> ");
			// Citeste mesajul de la user
			String msg = scan.nextLine();
			// Logout daca mesajul este LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
				// Break pentru disconect
				break;
			}
			// Trimite mesajul
			else if(msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
			}
			else {				
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		client.disconnect();	
	}

	// Clasa care asteapta mesajul de la server
        // append pentru GUI
        // system.out.println() pentru consola
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					// Daca suntem in consola printeaza mesajul si se intoarce la prompt
					if(cg == null) {
						System.out.println(msg);
						System.out.print("> ");
					}
					else {
						cg.append(msg);
					}
				}
				catch(IOException e) {
					display("Server a inchis conexiunea: " + e);
					if(cg != null) 
						cg.connectionFailed();
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}

