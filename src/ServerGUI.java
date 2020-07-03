import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// Interfata grafica a serverului
public class ServerGUI extends JFrame implements ActionListener, WindowListener {
	
	private static final long serialVersionUID = 1L;
	// Butonul de start / stop
	private JButton stopStart;
	// Casuta pentru chat si evenimente
	private JTextArea chat, event;
	// Portul
	private JTextField tPortNumber;
	// Server
	private Server server;
	
	
	// Constructorul serverului ce primeste portul pentru a asculta conexiunile
	ServerGUI(int port) {
		super("Chat Server");
		server = null;
		// Numar port si butoanele Start/Stop
		JPanel north = new JPanel();
		north.add(new JLabel("Numar port: "));
		tPortNumber = new JTextField("  " + port);
		north.add(tPortNumber);
		// Pentru pornirea sau oprirea serverului
		stopStart = new JButton("Start");
		stopStart.addActionListener(this);
		north.add(stopStart);
		add(north, BorderLayout.NORTH);
		
		// Chat/Event room
		JPanel center = new JPanel(new GridLayout(2,1));
		chat = new JTextArea(80,80);
		chat.setEditable(false);
		appendRoom("Chat room.\n");
		center.add(new JScrollPane(chat));
		event = new JTextArea(80,80);
		event.setEditable(false);
		appendEvent("Event log.\n");
		center.add(new JScrollPane(event));	
		add(center);
		addWindowListener(this);
		setSize(400, 600);
		setVisible(true);
	}		

	// Afiseaza mesajele in cele doua casute
	void appendRoom(String str) {
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1);
	}
	void appendEvent(String str) {
		event.append(str);
		event.setCaretPosition(chat.getText().length() - 1);
		
	}
	
	// Start/stop
	public void actionPerformed(ActionEvent e) {
		if(server != null) {
			server.stop();
			server = null;
			tPortNumber.setEditable(true);
			stopStart.setText("Start");
			return;
		}
      	// Start server	
		int port;
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
		}
		catch(Exception er) {
			appendEvent("Numar port gresit");
			return;
		}
		// Creaza server nou
		server = new Server(port, this);
		// Porneste ca Thread
		new ServerRunning().start();
		stopStart.setText("Stop");
		tPortNumber.setEditable(false);
	}
	
	public static void main(String[] arg) {
		new ServerGUI(1500);
	}

	// Daca utilizatorul foloseste butonu X pentru a inchide fereasta, elibereaza portul
	public void windowClosing(WindowEvent e) {
		// if my Server exist
		if(server != null) {
			try {
				server.stop();			
			}
			catch(Exception eClose) {
			}
			server = null;
		}
		dispose();
		System.exit(0);
	}
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	// Thread pentru a rula serverul
	class ServerRunning extends Thread {
		public void run() {
			server.start();        
			stopStart.setText("Start");
			tPortNumber.setEditable(true);
			appendEvent("Server a cazut\n");
			server = null;
		}
	}

}

