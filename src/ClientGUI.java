
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


// Clientul cu interfata grafica
public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// Prima data va retine "Username:" apoi "Scrieti mesaj"
	private JLabel label;
	// Prima data va retine username-ul apoi mesajul
	private JTextField tf;
	// Pentru a retine server address si port number
	private JTextField tfServer, tfPort;
	// Pentru logout si lista de useri
	private JButton login, logout, whoIsIn;
	// Pentru chat
	private JTextArea ta;
	// Pentru conectare
	private boolean connected;
	// Obiectul Client
	private Client client;
	// Portul standard
	private int defaultPort;
	private String defaultHost;

	// Primeste un numar de socket
	ClientGUI(String host, int port) {

		super("Chat Client");
		defaultPort = port;
		defaultHost = host;
		
		
		JPanel northPanel = new JPanel(new GridLayout(3,1));
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		// Casutele cu adresa serverului si portul standard
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Adresa Server  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		// Adauga casutele in interfata
		northPanel.add(serverAndPort);

		// Casuta pentru mesaje
		label = new JLabel("Introduceti username", SwingConstants.CENTER);
		northPanel.add(label);
		tf = new JTextField("Anonim");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);

		// Casuta pentru chat
		ta = new JTextArea("Bun venit in Chat\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// Butoanele
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);		
		whoIsIn = new JButton("Who is in");
		whoIsIn.addActionListener(this);
		whoIsIn.setEnabled(false);		

		JPanel southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(whoIsIn);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();

	}

	// Apelat de Client pentru vizualiza mesajele
	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	// Apelat de interfata in cazul unei erori de conexiune
	// Reseteaza butoanele si casuta de mesaje
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		whoIsIn.setEnabled(false);
		label.setText("Introduceti username");
		tf.setText("Anonim");
		// Reseteaza portul si host name
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		// Permite utilizatorului sa le schimbe
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		tf.removeActionListener(this);
		connected = false;
	}
		
	
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
			return;
		}
		if(o == whoIsIn) {
			client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
			return;
		}

		if(connected) {
			client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));				
			tf.setText("");
			return;
		}
		

		if(o == login) {
			// ok it is a connection request
			String username = tf.getText().trim();
			// empty username ignore it
			if(username.length() == 0)
				return;
			// empty serverAddress ignore it
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			// empty or invalid port numer, ignore it
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;   
			}

			client = new Client(server, port, username, this);
			// Testeaza daca putem porni Client nou
			if(!client.start()) 
				return;
			tf.setText("");
			label.setText("Scrieti mesajul");
			connected = true;
			
			// Opreste butonu login
			login.setEnabled(false);
			// Porneste butoanele
			logout.setEnabled(true);
			whoIsIn.setEnabled(true);
			// Opreste casutele Server si Port
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			tf.addActionListener(this);
		}

	}

	public static void main(String[] args) {
		new ClientGUI("localhost", 1500);
	}

}

