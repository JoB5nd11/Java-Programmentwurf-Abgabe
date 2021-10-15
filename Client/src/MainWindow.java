import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Desktop;
import java.awt.Image;
import javax.swing.SwingConstants;
import java.awt.Font;
import net.miginfocom.swing.MigLayout;
import tools.CustomListRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.event.MouseAdapter;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

public class MainWindow extends JFrame implements ActionListener {
	//Alle verf�gbaren BBB-R�ume, kann auch noch nachtr�glich erg�nzt werden
	private static String[] rooms = {"https://bbb.dhbw-heidenheim.de/?M=xzNW8jI0M2U3Mu862pnC",
								"https://bbb.dhbw-heidenheim.de/?M=eBtgjI0NDNlMushX31Zj",
								"https://bbb.dhbw-heidenheim.de/?M=MwIJjI0MmQwMgkoqcKIf"};
	public static String serverAddress;
	public static String username;
	public static String password;
	public static String[] callerInformation = {};
	public static boolean pop = true;
	
	private int ID;
	private static int rings;
	private static int roomNr;
	private static String callID;
	private static String status = "online";
	private static String last_contact = "";
	private static String OnlineClients[] = {""};
	private static boolean browser = true;
	
	private JPanel header;
	private JTextPane chatPane;
	private JTextField chatTextField;
	private JLabel clientHeaderLabel;
	private JList contactList;
	private JLabel contactLabel = new JLabel("");
	
	private SoundLoader sound = new SoundLoader();
	public static MainWindow frame = new MainWindow();
	public static PopUpWindow pw = new PopUpWindow();
	
	
	public static void main(String[] args) {
		//Versuche die Login-Daten von letztem Mal zu laden
		Path f = Paths.get(System.getProperty("user.dir"));
		f = Paths.get(f.toString() + "\\config\\settings.txt");
		
		//Variable, die alle Einstellungen von letztem Mal enth�lt
		String settings = null;
		
		try {
			settings = readFile(f.toString(), StandardCharsets.UTF_8);
		} catch (IOException ioe) {
			//Beim laden der Einstellungs-Datei ist ein Fehler aufgetreten -> Ausgabe in der Konsole
			ioe.printStackTrace();
		}
		
		//Wenn vorherige Einstellungen gefunden wurden:
		if(settings != null) {
			//Aufteilung in String-Array -> jedes Element enh�lt eine Einstellung
			String setting[] = settings.split(";");
			
			//Diese Daten (v) werden von LoginWindow.java ausgelesen um die Textfelder zu f�llen
			serverAddress = setting[0];
			username = setting[1];
			password = setting[2];
			SoundLoader.ringtone = setting [3];
		}
		
		//Login-Fenster �ffnen
		LoginWindow lg = new LoginWindow();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//Hauptfenster anzeigen
					frame.setVisible(true);
					//Loginfenster anzeigen
					lg.setVisible(true);
					PopUpWindow.OptionScreen(); //?
				} catch (Exception e) {
					//Beim �ffnen des Login-Fensters ist ein Fehler aufgetreten -> Ausgabe in der Konsole
					e.printStackTrace();
				}
			}
		});
		
		//Schleife, die so lange l�uft, bis das Login-Fenster geschlossen wurde
		//MainWindow.java steckt hier also fest, LoginWindow.java l�uft in einem anderen Thread und kann somit noch agieren
		//
		//Das wurde deshalb umgesetzt, da man sonst schon wegen den Daten von letztem Mal man als eingeloggt gilt.
		//W�rde man sich dann nochmal mit einem anderen Account anmelden, w�re der Server verwirrt.
		// --> Deshalb erstmal warten :)
		do {
			System.out.print(""); //<-- Nichts tun
		}while(!lg.closed);

		
		Timer t = new Timer();
		int heartbeats = 500; //Variable welche die zeitlichen Abst�nde zwischen den Server-Kommunikationen bestimmt, in [ms]
		
		//Regelm��ige Anfragen an den Server, um zu kontrollieren, ob ein Anruf eingeht oder sich die Kontakt-Liste und Status der 
		//Teilnehmer ge�ndert hat...
		t.schedule(new TimerTask(){
			@Override
			public void run() {
				frame.setTitle("Phone Client");
				frame.clientHeaderLabel.setText("Logged in as: " + username);
	
				String tmp;
				String activeUsers = null;
	
				//1. Wenn der Nutzer eingeloggt ist, und einen Kontakt ausgew�hlt hat, lade den Chatverlauf (wenn vorhanden)
				if(username != "" && frame.contactLabel.getText() != "") {
					//Versuche die Chat-Daten vom server zu holen
					try {
						URL myurl = new URL("http://" + serverAddress + "/getChat/" + username + "/" + frame.contactLabel.getText());
						URLConnection connection = myurl.openConnection();
						final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
						
						//Schreibe den gesamten Chat-Verlauf in einen String
						String line;
						String fullChat = "";
						
						//Zeile f�r Zeile die Datei auslesen und je mit \n im gesamten Chat-String speichern
						while ((line = in.readLine())!= null) {
							//URL enth�lt Verschl�sselung mit %xyz f�r Sonderzeichen, die wieder aufgel�st werden muss
							fullChat += URLDecoder.decode(line, "utf-8") + "\n";
						}
	
						//Gesamten Chat in das Textfeld laden
						frame.chatPane.setText(fullChat);
	
					} catch ( Exception e) {
						//Fehler bei der Kommunikation mit dem Server -> keine Ausgabe, da sonst Spam in der Konsole,
						//wenn noch kein Kontakt ausgew�hlt ist.
		        	}
				}
	
				
				//2. Versuche die Nutzerliste des Servers zu aktualisieren
				try {
					URL myurl = new URL("http://" + serverAddress + "/updateUsers/" + username +"/"+status);
					URLConnection connection = myurl.openConnection();
					final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					activeUsers = in.readLine();
					
					//Alle Nutzer des Servers in einem Array speichern
					OnlineClients = activeUsers.split(";");
					//Entfernen des eigenen Nutzers aus der Liste, da dieser sonst als Kontakt angezeigt werden w�rde
					frame.contactList.setListData(removeUser(username+" (" + status + ")"));
					
				} catch ( Exception e) {
					//Fehler bei der Kommunikation mit dem Server -> keine Ausgabe, da sonst Spam in der Konsole,
					//wenn noch kein Kontakt ausgew�hlt ist.
				}
	
				
				//3. Versuche zu erfragen, ob man gerade angerufen wird
				try {
					URL myurl = new URL("http://" + serverAddress + "/getCall/" + callID +"/"+username);
					URLConnection connection = myurl.openConnection();
					final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					
					//Informationen werden tempor�r zwischengespeichert
					tmp = in.readLine();
					if(tmp != ";") {
						callerInformation= tmp.split(";");
					}
				} catch ( IOException e) {
					//Fehler bei der Kommunikation mit dem Server -> keine Ausgabe, da sonst Spam in der Konsole,
					//wenn noch kein Kontakt ausgew�hlt ist.
				}
	
				
				//4. Wenn man selbst der angerufene Client ist
				if(callerInformation.length > 0 && username.equals(callerInformation[2]) && status=="online" && callerInformation[0] != "-1") {
					callID = callerInformation[0];
					
					//Klingele 15 mal, bei jedem Heartbeat an den Server wird diese Variable um 1 erh�ht
					if(rings <= 15) {
						//Wenn das PopUp-Fenster noch nicht ge�ffnet ist, �ffne es
						if(pop) { 
							pop = false;
							pw.visible();
						}
						
						frame.ring();
						//Hintergrund leuchtet gr�n, wenn man angerufen wird
						frame.header.setBackground(new Color(38, 224, 127)); //Gr�ner Farbwert des "Accept-Call-Buttons"
						
						if(!frame.contactLabel.getText().contains("incomming call from:")) {
							last_contact = frame.contactLabel.getText();
						}
						frame.contactLabel.setText("incomming call from: " + callerInformation[1]);
						
						//Blinkende gr�ne Farbe als Hintergrund
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						frame.header.setBackground(null);
	
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						frame.getContentPane().setBackground(null);
						
						rings++; //s.o.
					
					//Nach 15 mal Klingeln wird der Anruf-Versuch abgebrochen
					}else{
						frame.stopRinging();
						rings = 0;
						pop = true;
						frame.contactLabel.setText(last_contact);
						MainWindow.resetCall();
						}
					}
				
				
				//5. Wenn ich der Client bin, von dem das Telefonat ausgeht
				if(callerInformation.length > 0 && username.equals(callerInformation[1]) && callerInformation[0] != "-1") {
					if(("mm".equals(callerInformation[4]) || "mmm".equals(callerInformation[4])) && browser == true){
						//wird iwi nie auferufen -> vllt unn�tig / unbenutzt?
						frame.contactLabel.setText("Connected to: " + callerInformation[2]);
						browser = false;
						browser(translateRoom(roomNr));
					}
				}
				
				
				String[] tmp2 = frame.contactLabel.getText().split(" ");
				//Wenn telefonat vorbei / nicht stattfindet
				if(callerInformation[0].equals("-1") && (tmp2[0].equals("dialing") || tmp2[0].equals("Connected") ||tmp2[0].equals("incomming"))) {
					frame.stopRinging();
					pop = true;
					browser = true;
					status = "online";
					frame.contactLabel.setText(last_contact);
					rings = 0;
				}
			}
		}, 0, heartbeats);
		
		
		//Beim Schlie�en der Anwendung meldet der Client sich beim Server ab, die Kontaktlisten der anderen Teilnehmen werden
		//hinsichtlich dem Status upgedatet
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				status = "offline";
				
				//Versuche beim Server abzumelden -> User upzudaten
				try {
					URL myurl = new URL("http://" + serverAddress + "/updateUsers/" + username +"/"+status);
					URLConnection verbindung = myurl.openConnection();
					final BufferedReader in = new BufferedReader(new InputStreamReader(verbindung.getInputStream()));
				}catch(IOException ioe){
					//Bei der Serverkommunikation zum Nutzer-Udate ist etwas schief gelaufen -> Ausgabe in der Konsole
					ioe.printStackTrace();
				}

				//Speichere Login-Informationen in der config-Datei f�r den n�chsten Login
				try {
					Path f = Paths.get(System.getProperty("user.dir"));
					f = Paths.get(f.toString() + "\\config\\" + "settings" + ".txt");

					FileWriter myWriter = new FileWriter(f.toString(), false);
					myWriter.write(serverAddress + ";" + username + ";" + password + ";" + SoundLoader.ringtone);
					myWriter.close();
				} catch (IOException ioe) {
					//Beim Beschreiben der Config-Datei ist ein Fehler aufgetreten -> Ausgabe in der Konsole
					ioe.printStackTrace();
				}
				//Die ganzen Exceptions-Prints bringen ja gar nichts, weil man eh das Programm schlie�t, naja, egal, bleibt drin :)
			}
		}));
	}


	//Konstruktor f�r das Hauptfenster -> Komponenten werden initialisiert
	public MainWindow() {
		//Fenster-Icon
		setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "\\res\\" + "jf_phone_service.png"));
		
		//Fenstereigenschaften
		setTitle("Phone Client - Client "+ID);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 630, 445);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		header = new JPanel();
		clientHeaderLabel = new JLabel();
		clientHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
		header.add(clientHeaderLabel);
		getContentPane().add(header, BorderLayout.NORTH);

		JPanel contactPanel = new JPanel();
		contactPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		getContentPane().add(contactPanel, BorderLayout.WEST);
		contactPanel.setLayout(new BorderLayout(0, 0));
		contactList = new JList(OnlineClients);
		contactList.setPreferredSize(new Dimension(150, 2));
		contactList.setBorder(new EmptyBorder(10, 10, 10, 10));
		contactList.setCellRenderer(new CustomListRenderer());
		JScrollPane jsp = new JScrollPane(contactList);
		jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contactPanel.add(jsp);

		JPanel callPanel = new JPanel();
		getContentPane().add(callPanel, BorderLayout.CENTER);
		callPanel.setLayout(new MigLayout("", "[99999.00px]", "[21.00px][99999.00px]"));

		JPanel dialPanel = new JPanel();
		dialPanel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		dialPanel.setMaximumSize(new Dimension(32767, 40));
		dialPanel.setPreferredSize(new Dimension(10, 35));
		dialPanel.setAlignmentY(Component.TOP_ALIGNMENT);

		callPanel.add(dialPanel, "cell 0 0,growx,aligny center");
		dialPanel.setLayout(new BorderLayout(0, 0));
		contactLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		contactLabel.setHorizontalAlignment(SwingConstants.CENTER);
		dialPanel.add(contactLabel, BorderLayout.WEST);
		//Wenn ein Kontakt angeklickt wurde, schreibe seinen Namen �ber das Chat-Fenster (ohne Status)
		contactList.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				String userwithstatus = (String) contactList.getSelectedValue();
				if(userwithstatus != null) {
					String withoutstatus[] = userwithstatus.split(" ");
					contactLabel.setText(withoutstatus[0]);
				}
			}
		});

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(150, 35));
		panel.setMaximumSize(new Dimension(32767, 50));
		dialPanel.add(panel, BorderLayout.EAST);
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		JButton acceptCallButton = new JButton("");
		acceptCallButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		acceptCallButton.setPreferredSize(new Dimension(33, 33));
		//Knopf mit Bild-Datei statt Beschriftung
		acceptCallButton.setIcon(resizeIcon(new ImageIcon(System.getProperty("user.dir") + "\\res\\" + "accept.png"), 30, 30));
		panel.add(acceptCallButton);
		acceptCallButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				acceptCall();
			}
		});

		JButton declineCallButton = new JButton("");
		declineCallButton.setIcon(resizeIcon(new ImageIcon(System.getProperty("user.dir") + "\\res\\" + "decline.png"), 30, 30));
		declineCallButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				declineCall();
			}
		});
		declineCallButton.setPreferredSize(new Dimension(33, 33));
		declineCallButton.setAlignmentX(1.0f);
		panel.add(declineCallButton);

		JButton optionsButton = new JButton("");
		optionsButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		optionsButton.setPreferredSize(new Dimension(33, 33));
		panel.add(optionsButton);
		//Zeige Optionen, wenn der Button angeklickt wurde
		optionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OptionWindow.OptionScreen();
			}
		});
		optionsButton.setIcon(resizeIcon(new ImageIcon(System.getProperty("user.dir") + "\\res\\" + "settings.png"), 30, 30));

		JPanel chatPanel = new JPanel();
		chatPanel.setPreferredSize(new Dimension(10, 99999));
		callPanel.add(chatPanel, "cell 0 1, grow");
		chatPanel.setLayout(new BorderLayout(0, 0));

		chatPane = new JTextPane();
		chatPane.setEditable(false);
		chatPane.setText("");
		chatPanel.add(new JScrollPane(chatPane), BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		chatPanel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		chatTextField = new JTextField();
		chatTextField.setMaximumSize(new Dimension(2147483647, 33));
		chatTextField.setMinimumSize(new Dimension(7, 33));
		chatTextField.setPreferredSize(new Dimension(7, 33));
		chatTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_1.add(chatTextField);
		chatTextField.setColumns(10);

		JButton sendChatButton = new JButton("");
		sendChatButton.setIcon(resizeIcon(new ImageIcon(System.getProperty("user.dir") + "\\res\\" + "\\send.png"), 30, 30));
		sendChatButton.setPreferredSize(new Dimension(33, 33));
		sendChatButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		//Sende eine Chat-Nachricht, wenn auf "Senden" geklickt wurde
		sendChatButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendChat(chatTextField.getText(), username, contactLabel.getText());
				//Eingabezeile wird automtische wieder geleert, damit die n�chste Nachricht eingegeben werden kann
				chatTextField.setText("");
			}
		});
		panel_1.add(sendChatButton, BorderLayout.EAST);
	}
	
	
	//Funktion, die eine Datei einliest, und deren Inhalt als String geb�ndelt zur�ckgibt
	private static String readFile(String path, Charset encoding) throws IOException{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	
	//Funktion, die den Browser mit der mitgegebenen URL �ffnet
	//Wird genutzt um den BBB-Raum zu �ffnen
	public static void browser(String url) {
		//Verschieden M�glichkeiten je nach System-Unterst�tzung
		if(Desktop.isDesktopSupported()){
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				System.out.println("Failed to open browser (support):");
				e.printStackTrace();
			}
		}else{
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("xdg-open " + url);
			} catch (IOException e) {
				System.out.println("Failed to open browser (not supported):");
				e.printStackTrace();
			}
		}
	}

	
	//Funktion, die ein mitgegebenes Bild auf ein Feste Gr��e zu skalieren
	//Wird beispielsweise genutzt um die Bilder auf den Telefonier-Kn�pfen auf die richtige Gr��e zu bringen
	//Dabei wird die Bilddatei und die Gr��e des Knopfes mitgegeben
	public static Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
		Image img = icon.getImage();
		Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight,  java.awt.Image.SCALE_SMOOTH);
		return new ImageIcon(resizedImage);
	}

	
	//Spielt den eingestellten Klingelton ab
	public void ring() {
		sound.load();
		sound.play(SoundLoader.sound);
	}

	
	//Stoppt das Klingeln bei etwahigem Ablehnen oder ausklingeln des Telefons
	public void stopRinging() {
		//PopUp-Fenster wird geschlossen
		pw.hidden();
		//sound kann nur gestoppt werden, wenn er angefangen hat (also nach min. 1xKlingeln)
		if(rings > 0) {
			sound.stop();
		}
	}

	
	//Schickt Informationen an den Server, damit dieser die Infos in den Log-Dateien speichert
	//Ist public da auch Dateien wie LoginWindow.java darauf zugreifen m�ssen
	public static void toLog(String inputText) {
		String text = null;
		
		try {
			//Alle Sonderzeichen m�ssen umgewandelt und URL-f�hig gemacht werden
			text = URLEncoder.encode(inputText, "UTF-8");
			URL myurl = new URL("http://" + serverAddress + "/log/" + text + "/");
			URLConnection connection = myurl.openConnection();
			final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch ( IOException e) {
			//Bei der Kommunikation mit dem Server ist ein Fehler aufgetreten -> Ausgabe in der Konsole
			System.out.println("Failed to log from client:");
			e.printStackTrace();
		}
	}

	
	//Entfernt Nutzer aus der Liste der Teilnehmer
	private static String[] removeUser(String user) {
		int i, index = 0;
		//Neue Liste mit genau einem Element weniger
		String[] Clients = new String[OnlineClients.length - 1];

		//Finde das Element der eigenen Users
		for(i = 0; i < OnlineClients.length; i++) {
			if(OnlineClients[i].equals(user)) {
				index = i;
				//breche Schleife ab, nachdem User gefunden wurde, um Zeit zu sparen xD
				break;
			}else {
				//Handelt es sich um einen anderen User, f�gen ihn zu der neuen Liste hinzu
				Clients[i] = OnlineClients[i];
			}
		}

		//�berspringe den Index, an dem der eigene User ist, und f�lle die restliche Neue Liste mit dem restlichen Usern
		for(i = index; i < OnlineClients.length - 1; i++){
			Clients[i] = OnlineClients[i + 1];
		}

		//return die neue Liste
		return Clients;
	}
	
	
	//Setzt den Call komplett zur�ck
	//Beispielsweise nach einem beendeten oder abgelehnten Telefonat
	static void resetCall() {
		rings = 0;
		if(callID=="none") {
			return;
		}
		
		//Call von Server entfernen
		try {
			URL myurl = new URL("http://" + serverAddress + "/removeCall/"+callID);
			URLConnection connection = myurl.openConnection();
			final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch ( IOException e) {
			//Call konnte beim Server nicht abgemeldet werden
			System.out.println("Failed to remove call:");
			e.printStackTrace();
		}
		
		callID = "none";

		//Gebe den Raum des Telefonats wieder frei (i guess)
		if(roomNr!=-1) {
			try {
				URL myurl = new URL("http://" + serverAddress + "/freeRoom/"+roomNr);
				URLConnection connection = myurl.openConnection();
				final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} catch ( IOException e) {
				System.out.println("Failed to free Room:");
				e.printStackTrace();
			}
		}else{
			System.out.println("No RoomNr Selected");
		}
	}
	
	
	//Reserviere einen Raum f�r das kommende Telefonat
	private int getRoom() {
		try {
			//Verbinde mit dem ersten freien BBB-Raum
			URL myurl = new URL("http://" + serverAddress + "/checkRooms");
			URLConnection connection = myurl.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			//Server gibt die Nummer des ersten Raums zur�ck, der frei ist
			roomNr = Integer.parseInt(in.readLine());
			//ist die zur�ckkommende Raumnummer = -1, ist kein Raum mehr frei
			

			//if roomNr is between 0 and 2, get ID and return corresponding URL from array
			//Wenn Raum frei ist, gibt reseviere und gebe die Raumnummer zur�ck
			if(roomNr >= 0) {
				URL meineurl2 = new URL("http://" + serverAddress + "/occupyRoom/" + roomNr); //TODO erst verbinden, wenn Anruf zugesagt
				URLConnection verbindung2 = meineurl2.openConnection();
				BufferedReader in2 = new BufferedReader(new InputStreamReader(verbindung2.getInputStream()));
				return roomNr;
			}
		} catch (IOException e) {
			//Beim Reservieren eines Raums ist ein Fehler aufgetreten (nicht der Fehler, dass kein Raum frei ist!)
			System.out.println("Failed to check and occupy rooms:");
			e.printStackTrace();
		}
		return -1;
	}


	//gibt die Passende URL zu einer Raumnummer zur�ck
	private static String translateRoom(int room) { 
		if(room == -1) {
			return "http://" + serverAddress + "/occupied";
		}else {
			return rooms[room];
		}
	}
	
	
	//Kommunikation mit dem Server um eine Textnachricht zu versenden
	//Ben�tigt den Text, den Sender und den Empf�nger
	private void sendChat(String message, String sender, String reciever) {
		String text = null;
		
		try {
			text = URLEncoder.encode(message, "UTF-8");
			URL myurl = new URL("http://" + serverAddress + "/chat/" + text + "/" + sender + "/" + reciever);
			URLConnection connection = myurl.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch (IOException e) {
			//Fehler beim Versenden einer Chat-Nachricht -> Ausgabe in der Konsole
			System.out.println("Failed to send chat message:");
			e.printStackTrace();
		}
	}

	
	//Macht ein Telefonat
	public void acceptCall() {
		//ist kein User aus der Kontaktliste ausgew�hlt, kann gleich wieder abgebrochen werden
		if(contactLabel.getText() == "") {
			return;
		}
		
		//setze den Status auf "im Gespr�ch"!
		status = "talking";
		
		
		if(rings > 0) {
			try {
				URL myurl = new URL("http://" + serverAddress + "/acceptCall/" +callID);
				URLConnection connection = myurl.openConnection();
				final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			}catch( IOException e){
				//Probleme bei Kommunikation mit dem Server ... -> Ausgabe
			}
			
			//�ffne die URL f�r den BBB-Raum
			browser(translateRoom(Integer.parseInt(callerInformation[3])));
			contactLabel.setText("Connected to: "+callerInformation[1]);
			//Stoppt das Klingeln
			stopRinging();
			rings = 0;
		}else{
			//Beim ersten klick, i guess, keine Ahnung was das macht ... ausgehender Anruf oder so ... 
			try {
				toLog(username + " called " + contactLabel.getText());
				URL myurl = new URL("http://" + serverAddress + "/receiveCall/" + contactLabel.getText() + "/" + username + "/" + getRoom());
				URLConnection connection = myurl.openConnection();
				final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				callID =  in.readLine();
				contactLabel.setText("dialing " + contactLabel.getText() + " ...");
			} catch ( IOException e) {
				//Ausf�hren eines Telefonats ist schief gelaufen, Mist! -> Ausgabe in der Konsole
				System.out.println("Failed to call:");
				e.printStackTrace();
			}
		}
	}
	
	
	//Lehne ein einkommendes Telefonat ab, oder beende es ein f�r alle mal!
	public void declineCall() {
		browser = true;
		status = "online";
		toLog(username + " rejected incoming call");
		stopRinging();
		resetCall();
		frame.contactLabel.setText(MainWindow.last_contact);
	}

	public void actionPerformed(ActionEvent e) {
		//Braucht man das?
	}
}
