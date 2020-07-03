import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

// Serverul poate functiona in interfata grafica sau consola
public class Server {
	// ID unic pentru fiecare conexiune
	private static int uniqueId;
	// Lista cu clienti
	private ArrayList<ClientThread> al;
	private ServerGUI sg;
	// Afiseaza data/ora
	private SimpleDateFormat sdf;
	// Portul ce asteapta conexiune
	private int port;
	private boolean keepGoing;
	

	//Constructorul ce primeste portu pentru asteptarea conexiunii
	public Server(int port) {
		this(port, null);
	}
	
	public Server(int port, ServerGUI sg) {
		// Interfata grafica sau nu
		this.sg = sg;
		// Portul
		this.port = port;
		// Ora
		sdf = new SimpleDateFormat("HH:mm:ss");
		// Lista de clienti
		al = new ArrayList<ClientThread>();
	}
	
	public void start() {
		keepGoing = true;
		// Creeaza socket si asteapta conexiuni
		try 
		{
			// Socketul folosit de server
			ServerSocket serverSocket = new ServerSocket(port);

			// Loop in asteptare de conexiuni
			while(keepGoing) 
			{
				// format message saying we are waiting
				display("Serverul asteapta clienti pe portul " + port + ".");
				// Accepta conexiunea
				Socket socket = serverSocket.accept();  	
				// Opreste
				if(!keepGoing)
					break;
                                // Creeaza un Thread
				ClientThread t = new ClientThread(socket); 
                                // Salveaza in lista
				al.add(t);									
				t.start();
			}
			// Daca este oprit
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						
					}
				}
			}
			catch(Exception e) {
				display("Exceptie, se inchid clientii si serverul " + e);
			}
		}

		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exceptie pe noul socket: " + e + "\n";
			display(msg);
		}
	}		
    // Pentru inchiderea serverului pe GUI
	protected void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}
	}
	// Afisarea unui eveniment in consola sau GUI
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if(sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}
	// Transmiterea unui mesaj catre toti clientii
	private synchronized void broadcast(String message) {
		// Ora
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";
		// Afisare mesaj in consola sau GUI
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);     
		
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			if(!ct.writeMsg(messageLf)) {
				al.remove(i);
				display("Deconectare client " + ct.username);
			}
		}
	}

	// Pentru un client care se deconecteaza folosind LOGOUT
	synchronized void remove(int id) {
		// Scaneaza lista pana gaseste ID
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}
	
	
	public static void main(String[] args) {
		// Porneste serverul pe portul 1500 in cazul in care nu este specificat altul 
		int portNumber = 1500;
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Numar port gresit.");
					System.out.println("Folositi: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Folositi: > java Server [portNumber]");
				return;
				
		}
		// Creaza obiectul server si il porneste
		Server server = new Server(portNumber);
		server.start();
	}

	// Fiecare client va rula o instanta a Threadului
	class ClientThread extends Thread {
		// Socketul unde se asteapta sau se trimite
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// ID unic
		int id;
		String username;
		ChatMessage cm;
		// Data conectarii
		String date;

		// Constructoare
		ClientThread(Socket socket) {
			// ID unic
			id = ++uniqueId;
			this.socket = socket;
			// Creeaza ambele streamuri de date
			System.out.println("Threadul incearca sa creeze Input/Output");
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// Citeste username
				username = (String) sInput.readObject();
				display(username + " s-a conectat.");
			}
			catch (IOException e) {
				display("Exceptie in creearea a noi streamuri Input/Output: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}

		public void run() {
			// Loop pana la LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// Citeste un String
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exceptie citire streamuri: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// Mesajul care face parte din ChatMessage
				String message = cm.getMessage();

				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case ChatMessage.LOGOUT:
					display(username + " deconectare prin LOGOUT.");
					keepGoing = false;
					break;
				case ChatMessage.WHOISIN:
					writeMsg("Lista de useri conectati la " + sdf.format(new Date()) + "\n");
					// Scaneaza userii
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				}
			}
			remove(id);
			close();
		}
		
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		// Scrie string catre Client
		private boolean writeMsg(String msg) {
			// Daca clientul e conectat scrie mesajul
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// Scrie mesajul catre stream
			try {
				sOutput.writeObject(msg);
			}
			catch(IOException e) {
				display("Eroare trimetere mesaj catre " + username);
				display(e.toString());
			}
			return true;
		}
	}
}


