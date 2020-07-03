
import java.io.*;
//Defineste corpul mesajului
public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// Tipurile de mesaje trimise de Client.
	// WHOISIN = Ce user s-a conectat.
	// MESSAGE = Mesajul propriu zis.
	// LOGOUT = Disconnect de la Server
	static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;
	private int type;
	private String message;
	
	// constructor
	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	// getters
	int getType() {
		return type;
	}
	String getMessage() {
		return message;
	}
}

