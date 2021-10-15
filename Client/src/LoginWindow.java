import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.Cursor;

public class LoginWindow extends JFrame {
	//Erstes Initialisieren von Swing-Komponenten. Manche der Komponenten werden genutzt (zum Beispiel in 
	//Button-Actions). Deshalb werden diese hier deklariert.
	//Außerdem kann über die ganze Klasse auf diese Komponenten zugegriffen werden, nicht nur in einzelnen Funktionen
	private JPanel contentPane;
	private JLabel responseLabel;
	private JTextField serverTextfield;
	private JTextField usernameTextfield;
	private JTextField passwordTextfield;
	
	//Variable, die angibt, ob das Fenster geschlossen ist
	//Ist "closed = true" bedeutet das, dass das Fenster geschlossen wurde, und das Hauptfenster weiter machen kann
	public boolean closed; 
	
	//Erstellung des eigenen Fenster-Objekts
	private static LoginWindow loginFrame = new LoginWindow();

	
	//Start der Applikation / des Fensters
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//Fenster wird sichtbar gestellt
					loginFrame.setVisible(true);
				} catch (Exception e) {
					//Sollte beim sichtbar stellen ein Fehler auftreten, wird dieser in der Konsole ausgegeben
					e.printStackTrace();
				}
			}
		});
	}

	//Erstellung des Fenster + Kompenten im Konstruktor der Klasse
	public LoginWindow() {
		//Festlegen des Fenster-Icons -> im Ordner \res\ in dem alle Bilddateien sind
		setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "\\res\\" + "jf_phone_service.png"));
		
		//Im Konstruktor (beim Erstellen der Klasse) ist das Fenster geöffnet
		closed = false;
		
		
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		
		JPanel headerPanel = new JPanel();
		contentPane.add(headerPanel, BorderLayout.NORTH);

		JLabel loginHeadlineLabel = new JLabel("Configure Login");
		loginHeadlineLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
		headerPanel.add(loginHeadlineLabel);

		
		JPanel bottomPanel = new JPanel();
		contentPane.add(bottomPanel, BorderLayout.SOUTH);


		JButton registerButton = new JButton("Register");
		registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		bottomPanel.add(registerButton);
		//Wenn der Knopf geklickt wird...
		registerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Es wird versucht ein neuen Account anzulegen, ob das geklappt hat wird in dieser Variablen gespeichert
				Boolean success = registerAccount();
				if(success) {
					try {
						//Kommunikation mit dem Server, dieser soll alle Accounts neu laden, damit der neue hinzugefügt wird
						URL myurl = new URL("http://localhost:8080/reloadAccounts/");
						URLConnection connection = myurl.openConnection();
						final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						//Label unter der Eingabe gibt (in grün) an, dass der neue Account registiert wurde
						responseLabel.setForeground(new Color(38, 224, 127)); //Grüne Farbe, die exakt dem Grünwert des "Accept-Call-Buttons" entspricht
						responseLabel.setText("Account registered!");
					} catch ( IOException ioe) {
						//Der Account konnte registriert werden, aber bei der Server-Kommunikation ist ein Fehler aufgetreten -> Fehlerausgabe in der Konsole
						ioe.printStackTrace();
					}
				}else {
					//wenn der Account nicht registriert wurd, ist dieser schon vorhanden -> entsprechende Ausgabe
					responseLabel.setForeground(new Color(250, 49, 74)); //Rote Farbe, die exakt dem Rotwert des "Decline-Call-Buttons" entspricht
					responseLabel.setText("Account already registered!");
				}
			}
		});


		JButton applyButton = new JButton("Login");
		applyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		//Wird dieser Knopf betätigt, schließt sich das Fenster automatisch
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Variable, in der gespeichert wird, ob der Login-Vorgang erfolgreich war
				//ist Standardmäßig auf false, nur wenn der Vorgang einwandfrei funktioniert hat, wird der Wert geändert
				boolean loginSuccess = false;
				
				try {
					//Kommunikation mit dem Server, dieser soll kontrollieren, ob die eingegebenen Daten mit ein registrierten User übereinstimmen
					URL myurl = new URL("http://" + serverTextfield.getText() + "/login/" + usernameTextfield.getText() + "/" + passwordTextfield.getText());
					URLConnection connection = myurl.openConnection();
					final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					//Speichern in Variable, ob Login geklappt hat
					loginSuccess = Boolean.valueOf(in.readLine());
				} catch ( IOException ioe) {
					//Etwas ist bei der Server-Kommunikation schief gelaufen -> Ausgabe in der Konsole
					ioe.printStackTrace();
				}
				
				if(loginSuccess) {
					responseLabel.setForeground(new Color(38, 224, 127)); //Grüne Farbe, die exakt dem Grünwert des "Accept-Call-Buttons" entspricht
					responseLabel.setText("login successful");
					
					//Fenster nicht direkt schließen, damit User angezeigt werden kann, dass alles funktioniert hat
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						//Beim warten ist ein Fehler aufgetreten -> Ausgabe in der Konsole
						ie.printStackTrace();
					}
					
					//Speichern der Daten im MainWindow -> damit gilt der User als angemeldet
					MainWindow.serverAddress = serverTextfield.getText();
					MainWindow.username = usernameTextfield.getText();
					MainWindow.password = passwordTextfield.getText();
					//Fenster wird geschlossen und variable entsprechend gesetzt. Die Nutzung des MainWindow ist damit freigegeben
					closed = true;
					dispose();
				
				//Login hat nicht funktioniert
				}else{
					responseLabel.setForeground(new Color(250, 49, 74)); //Rote Farbe, die exakt dem Rotwert des "Decline-Call-Buttons" entspricht
					responseLabel.setText("login failed");
				}
			}
		});
		bottomPanel.add(applyButton);

		
		//Mittleres Panel für die Eingabe der Daten
		JSplitPane mainSplitpanel = new JSplitPane();
		contentPane.add(mainSplitpanel, BorderLayout.CENTER);

		JPanel inputPanel = new JPanel();
		mainSplitpanel.setRightComponent(inputPanel);

		serverTextfield = new JTextField();
		serverTextfield.setText(MainWindow.serverAddress); //standard entry
		serverTextfield.setHorizontalAlignment(SwingConstants.CENTER);
		serverTextfield.setColumns(10);

		usernameTextfield = new JTextField();
		usernameTextfield.setText(MainWindow.username);
		usernameTextfield.setHorizontalAlignment(SwingConstants.CENTER);
		usernameTextfield.setColumns(10);

		passwordTextfield = new JPasswordField(); //make input "invisible"
		passwordTextfield.setText(MainWindow.password);
		passwordTextfield.setHorizontalAlignment(SwingConstants.CENTER);
		passwordTextfield.setColumns(10);

		responseLabel = new JLabel();
		responseLabel.setHorizontalAlignment(SwingConstants.CENTER);
		responseLabel.setFont(new Font("Tahoma", Font.BOLD, 15));

		//Layout-Konfiguration der Text-Felder
		GroupLayout gl_inputPanel = new GroupLayout(inputPanel);
		gl_inputPanel.setHorizontalGroup(
			gl_inputPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_inputPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_inputPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, gl_inputPanel.createSequentialGroup()
							.addGap(1)
							.addComponent(serverTextfield, GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, gl_inputPanel.createSequentialGroup()
							.addGap(1)
							.addComponent(usernameTextfield, GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, gl_inputPanel.createSequentialGroup()
							.addGap(1)
							.addComponent(passwordTextfield, GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE))
						.addComponent(responseLabel, GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_inputPanel.setVerticalGroup(
			gl_inputPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_inputPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(serverTextfield, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(usernameTextfield, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(passwordTextfield, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(26)
					.addComponent(responseLabel, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(29, Short.MAX_VALUE))
		);
		inputPanel.setLayout(gl_inputPanel);

		JPanel labelPanel = new JPanel();
		mainSplitpanel.setLeftComponent(labelPanel);

		JLabel serverLabel = new JLabel("Server Address");
		serverLabel.setHorizontalAlignment(SwingConstants.CENTER);
		serverLabel.setFont(new Font("Tahoma", Font.BOLD, 15));

		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		usernameLabel.setFont(new Font("Tahoma", Font.BOLD, 15));

		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
		passwordLabel.setFont(new Font("Tahoma", Font.BOLD, 15));

		//Layout-Konfiguration der Text-Feld-Beschriftungen
		GroupLayout gl_labelPanel = new GroupLayout(labelPanel);
		gl_labelPanel.setHorizontalGroup(
			gl_labelPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_labelPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_labelPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(serverLabel)
						.addComponent(usernameLabel, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
						.addComponent(passwordLabel, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_labelPanel.setVerticalGroup(
			gl_labelPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_labelPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(serverLabel)
					.addGap(18)
					.addComponent(usernameLabel, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(passwordLabel, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(77, Short.MAX_VALUE))
		);
		labelPanel.setLayout(gl_labelPanel);
	}
	
	
	///Funktion für die Registierung eines neuen Accounts
	private Boolean registerAccount() {
		//Wenn in der Text-Feldern sowohl Name als auch Passwort angegeben wurden
		if(usernameTextfield.getText() != "" && passwordTextfield.getText() != "") {
			//Generiert einen String der die Accountinformationen beinhaltet
			String acc = usernameTextfield.getText() + "-" + passwordTextfield.getText();
			//Lade die Datei "users.txt"
			Path f = Paths.get(System.getProperty("user.dir")).getParent();
			f = Paths.get(f.toString() + "\\Server\\users.txt");

			//versuche den neuen Account in die Datei zu schreiben
			try {
				//Wenn der User noch nicht in der Datei zu finden ist
				if(!checkForUserInUsers(acc, f)) {
					FileWriter myWriter = new FileWriter(f.toString(), true);
				    myWriter.write(acc + "\n");
				    myWriter.close();
				    MainWindow.toLog("Registered new user: " + acc);
				    return true; //Erfolg!
				}else {
					MainWindow.toLog("User already registered: " + acc + ". No new user created");
				}
			} catch (IOException ioe) {
				//Beim Beschreiben der Datei ist ein Fehler aufgetreten -> Ausgabe in der Konsole
				ioe.printStackTrace();
			}
		}
		return false; //hat nicht geklappt
	}
	
	//Funktion, die kontrolliert, ob in einer angegeben Datei ein Account schon vorhanden ist
	private Boolean checkForUserInUsers(String acc, Path file) {
		try {
			Scanner scanner = new Scanner(file);
			Boolean found = false;
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.equals(acc)) {
					//Gleicher Account wurde im Dokument schon gefunden
					return true;
				}
			}
		} catch (IOException ioe) {
			//Fehler beim Laden und Lesen der Datei
			ioe.printStackTrace();
		}
		//Wenn nicht gefunden -> noch nicht in der Liste
		return false;
	}
}
