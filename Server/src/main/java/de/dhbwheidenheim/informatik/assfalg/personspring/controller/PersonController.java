package de.dhbwheidenheim.informatik.assfalg.personspring.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class PersonController {
	private int roomNr =-1;
	private String[] usersPasswords = readUserFile();
	private String updatedUsers;
	private String callingUser;
	private String calledUser;
	private String participants = "m";
	private Random rand = new Random();
	
	//Bool-Array welches Festhält, welche BBB-Räume besetzt sind
	static boolean[] isRoomEmpty = {true, true, true};

	//HashMaps mit allen aktiven Nutzern und aktive Calls
	//Wenn man nicht weiß, wie mans lösen soll -> HashMap
	HashMap<String, String> activeUsers = new HashMap<String, String>();
	HashMap<Integer, String[]> activeCalls = new HashMap<Integer, String[]>();
	
	
	//Beim Starten des Servers
	public PersonController() {
		//Erstellt Logbuch und machte erste Eintragung
		initLog();
		//Lädt alle User in die Aktiven User Hashmap
		getUsers();
	}
	
	
	//Funktion, die alle User aus users.txt lädt und in der HashMap speichert
	void getUsers() {
		String[] tmpusers = readUserFile();
		
		//Jeden User in Name und Status splitten
		for(int i = 0; i < tmpusers.length; i++) {
			String tmpuser[] = tmpusers[i].split("-");
			if(tmpuser[0] != "") {
				activeUsers.put(tmpuser[0],"offline");
			}
		}	
	}
	
	
	//Telefonat-Anfrage findet statt, weißt dem Call eine zufällige ID zu
	@RequestMapping(value = "/receiveCall/{id}/{caller}/{room}", method = RequestMethod.GET)
	public @ResponseBody int receiveCall(@PathVariable("id") String id, @PathVariable("caller") String caller, @PathVariable("room") String room) {
		int callID = rand.nextInt(10000);
		
		activeCalls.put(callID, new String []{caller, id, participants});
		roomNr = Integer.parseInt(room);
		
		return callID;
	}
	
	
	//Telefonat wir angenommen, Aktive-Calls-HashMap wird aktualisiert
	@RequestMapping(value = "/acceptCall/{id}", method = RequestMethod.GET)
	public @ResponseBody boolean receiveCall(@PathVariable("id") int id) {
		String tmp[] = activeCalls.get(id);
		tmp[2] = tmp[2]+"m";
		activeCalls.put(id, tmp);
		
		return true;
	}
	
	
	//Löscht ein Telefonat mit gegebener ID
	@RequestMapping(value = "/removeCall/{id}", method = RequestMethod.GET)
	public @ResponseBody boolean removeCall(@PathVariable("id") int id) {
		//Telefonat wird aus der Hashmap gelöscht
		activeCalls.remove(id);
		return true;
	}
	
	
	//Kontrolliert aktive Nutzer und gibt einen String mit diesen Nutzern zurück
	@RequestMapping(value = "/updateUsers/{username}/{status}", method = RequestMethod.GET)
	public @ResponseBody String updateUsers(@PathVariable("username") String user, @PathVariable("status") String status) {
		activeUsers.put(user, status);	

		updatedUsers = "";
		for (String s : activeUsers.keySet()) {
			updatedUsers +=s +" ("+activeUsers.get(s)+");";
		}
		return updatedUsers;
	}
	
	
	
	//Entfernt einen User von der HashMap der aktiven Nutzer
	@RequestMapping(value = "/removeUser/{username}", method = RequestMethod.GET)
	public @ResponseBody void removeUser(@PathVariable("username") String user) {
			activeUsers.remove(user);
			log("Removing user: " + user);
	}
	
	
	//Gibt Raum mit gegebener Nummer wieder frei
	@RequestMapping(value = "/freeRoom/{room}", method = RequestMethod.GET)
	public @ResponseBody void removeUser(@PathVariable("room") int room) {
		isRoomEmpty[room] = true;
	}
	
	
	@RequestMapping(value = "/getCall/{callid}/{username}", method = RequestMethod.GET)
	public @ResponseBody String getCall(@PathVariable("callid") String callID, @PathVariable("username") String username) {
		
		String tp1 = callID;
		for (int i : activeCalls.keySet()) {
			String tmp[] = activeCalls.get(i);
			
			if(username.equals(tmp[1])) {
				//Black Magic?
				return i + ";" + tmp[0] + ";" + tmp[1] + ";" + roomNr + ";" + tmp[2];
			}
		}
		
		for (int i : activeCalls.keySet()) {
			String tmp[] = activeCalls.get(i);
			
			if(username.equals(tmp[0])) {
				return i + ";" + tmp[0] + ";" + tmp[1] + ";" + roomNr + ";" + tmp[2];
			}
		}
		
		//?!?! :)
		return "-1;0;0;0;0;";
	}
	
	
	//Login-Vorgang für gegebene Nutzernamen mit Passwort, wird mit Nutzerliste abgeglichen
	@RequestMapping("/login/{username}/{password}")
	@ResponseBody boolean login(@PathVariable("username") String username, @PathVariable("password") String password) {
		String user = username;
		String pw = password;
		
		//Gehe alle Einträge des Dokuemnts durch, wenn es eine Überstimmung gibt, war der Login-Prozess erforlgreich
		for(int i = 0; i < usersPasswords.length;i++ ) {
			if((user + "-" + pw).equals(usersPasswords[i])) {
				//Festhalten in logs
				log("User logged in: " + username);
				return true;
			}
		}	
		return false;
	}
	
	
	//Alle Accounts neu laden, falls es neue Accounts gibt
	@RequestMapping("/reloadAccounts")
	public void relaodAccounts() {
		usersPasswords = readUserFile();
		log("Accounts reloaded");
	}
	
	
	//Liest alle Nutzer und deren Passwörter aus users.txt und gibt eine String-Liste zurück
	private String[] readUserFile() {
		//Langer String mit allen Nutzern und Passwörtern
		String all_users = "";
		
		//Versuche die Datei zu öffnen
		try {
			Path f = Paths.get(System.getProperty("user.dir"));
			f = Paths.get(f.toString() + "\\users.txt");
			
			File myObj = new File(f.toString());
			Scanner myReader = new Scanner(myObj);
			
			//Lies Zeile für Zeile und füge jedes Namen-Passwort-Paar dem String hinzu
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				all_users += " " + data;
			}
			
			//Log, dass die Daten geladen wurden
			log("All users accounts loaded");
			
			//Datei wieder schließen, wird ja nicht mehr benötigt
			myReader.close();
		
		} catch (FileNotFoundException e) {
			//beim Laden der Nutzerdaten ist ein Fehler passiert, wird ins Logbuch geschrieben
			log("Fail: " + e.toString());
		}
		
		//Der lange String mit allen Nutzern wird in ein String-Array aufgesplittet
		//Jedes Element beinhaltet nun ein Nutzer-Passwort-Paar
		String result[] = all_users.split(" ");
		
		return result;
	}
	
	
	//Erstes Log initialisieren
	private void initLog() {
		//Formatierung des Datums und der Uhreit für Dateinamen und Logbuch-Einträge
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();
		String date = dtf.format(now).split(" ")[0];
		String time = dtf.format(now).split(" ")[1];
		   
		//Log-Datei von heute öffnen oder erstellen
		Path f = Paths.get(System.getProperty("user.dir"));
		f = Paths.get(f.toString() + "\\logs\\" + date + ".txt");
		
		//Versuche die Datei zu öffen und zu beschreiben
		try {
			FileWriter myWriter = new FileWriter(f.toString(), true);
			myWriter.write("[" + time + "]" + " Server started..." + "\n");
			myWriter.close();
		} catch (IOException e) {
			//Falls beim initialisieren der Logs entwas schief geht -> schreibe ins Log -> Hä? xD
			log("Fail: " + e.toString());
		}
	}
	
	
	//Trägt mitgegebenen Text in die Logs ein -> Text kommt aus MainWindow.java oder so
	@RequestMapping(value = "/log/{text}/", method = RequestMethod.GET)
	@ResponseBody void log(@PathVariable("text") String text) {
		//Wieder die Formatierung festlegen
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();
		String date = dtf.format(now).split(" ")[0];
		String time = dtf.format(now).split(" ")[1];
		
		//Versuche die URL zu entschlüsseln, um auch mögliche Sonderzeichen zu schreiben
		try {
			text = URLDecoder.decode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//Fehler bei der Entschlüsselung
			System.out.println("Something went wrong decoding a URL for Log-Entry");
			e.printStackTrace(); //<- Warum wird das nicht in die Logs geschrieben?
		}
		
		Path f = Paths.get(System.getProperty("user.dir"));
		f = Paths.get(f.toString() + "\\logs\\" + date + ".txt");
		
		//Beschreibe die Log-Datei mit dem Text
		try {
			FileWriter myWriter = new FileWriter(f.toString(), true);
			myWriter.write("[" + time + "] " + text + "\n");
			myWriter.close();
		} catch (IOException e) {
			//Fehler beim Beschreiben der Datei -> Schreibe den Fehler in die Datei -> ? xD
			log("Fail: " + e.toString());
		}
	}
	
	
	//Kontrolliertl, welche Räume frei sind
	@RequestMapping("/checkRooms")
	@ResponseBody public int checkRooms() {
		//Geht alle räume durch und gibt die Nummer des ersten zurück, der frei ist,
		//Die Funktion ist extra so aufgebaut, dass einfach neue BBB-Räume ergänzt werden können und trotzdem alles funktioniert
		for(int i = 0; i < isRoomEmpty.length; i++) {
			if(isRoomEmpty[i]) {
				return i;
			}
		}
		//Kein freier Raum wurde gefunden
		return -1;
	}
	
	
	//Reserviere einen Raum
	@RequestMapping(value = "/occupyRoom/{nr}", method = RequestMethod.GET)
	@ResponseBody void log(@PathVariable("nr") int nr) {
		isRoomEmpty[nr] = false;
		System.out.println(Arrays.toString(isRoomEmpty) + " " + nr);
	}
	
	
	//Verschickt Chat-Text zwischen 2 Usern
	@RequestMapping(value = "/chat/{text}/{user1}/{user2}", method = RequestMethod.GET)
	@ResponseBody void chat(@PathVariable("text") String textInput, @PathVariable("user1") String user1, @PathVariable("user2") String user2) {
		String text = textInput.replaceAll("\\+", " ");
		Path f = Paths.get(System.getProperty("user.dir"));
		//Initialisiere 2 Variablen mit den Möglichen Dateinamen 
		//Entweder User1-User2 oder User2-User1
		File f1 = new File(f.toString() + "\\chats\\" + user1 + "-" + user2 + ".txt");
		File f2 = new File(f.toString() + "\\chats\\" + user2 + "-" + user1 + ".txt");
		
		//Kontrolliere, ob die erste Dateiname-Möglichkeit exisitiert
		if(f1.exists()) {
			f = Paths.get(f.toString() + "\\chats\\" + user1 + "-" + user2 + ".txt");
		//Wenn die erste Dateinamen-Möglichkeit nicht exisistiert, muss entweder die andere oder keine Datei vorhanden sein
		}else {
			f = Paths.get(f.toString() + "\\chats\\" + user2 + "-" + user1 + ".txt");
		}
		
		//Beschreibe die Chat-Datei der 2 User
		try {
			FileWriter myWriter = new FileWriter(f.toString(), true);
			myWriter.write(user1 + ": " + text + "\n");
			myWriter.close();
		} catch (IOException e) {
			//Beim Beschreiben der Datei ist ein Fehler aufgetreten
			log("Fail: " + e.toString());
		}
		//Alle versendeten Nachrichten werden auch im Log festgehalten
		log(user1 + " chattet to " + user2 + ": " + text);
	}
	
	
	//Funktion, die den ganzen Chat-Verlauf zwischen 2 Usern lädt und an den Client schickt
	@RequestMapping(value = "/getChat/{user1}/{user2}", method = RequestMethod.GET)
	@ResponseBody String getChat(@PathVariable("user1") String user1, @PathVariable("user2") String user2) {
		//gleiche Namensfindung wie bei chat()
		Path f = Paths.get(System.getProperty("user.dir"));
		File f1 = new File(f.toString() + "\\chats\\" + user1 + "-" + user2 + ".txt");
		File f2 = new File(f.toString() + "\\chats\\" + user2 + "-" + user1 + ".txt");
		String filename = null;
		if(f1.exists()) f = Paths.get(f.toString() + "\\chats\\" + user1 + "-" + user2 + ".txt");
		else if(f2.exists()) f = Paths.get(f.toString() + "\\chats\\" + user2 + "-" + user1 + ".txt");
		else f = Paths.get(f.toString() + "\\chats\\" + user1 + "-" + user2 + ".txt");
		
		//String mit allen Zeilen
		String chat = null;
		try {
			chat = readFile(f.toString(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			//Kein chat vorhanden
			//blöd, ne?
		}
		
		//Wenn Chat vorhanden, wird dieser URL-Gerecht verschlüsselt und verschickt
		try {
			if(chat != null) {
				chat = URLEncoder.encode(chat, "UTF-8");	
			}
		} catch (UnsupportedEncodingException e) {
			//Eventuelle Fehler werden ausgegeben
			System.out.println("Something went wrong converting chat text to URL");
			e.printStackTrace(); // <- könnte auch im Log gespeichert werden, ne?
		}
		return chat;
	}
	
	
	//Lädt die 404 Website im Browser
	@RequestMapping("/404")
	public String error() {
		return Websites.error();
	}
	
	//Alle Räume sind besetzt, deshalb wird diese Seite geöffnet
	@RequestMapping("/occupied")
	public String occupied() {
		return Websites.occupied();
	}
	
	//Anzeige, welche Räume belegt sind, und welche frei -> eig für Debug gedacht, da keiner ne Ahnung hatte was eig im
	//Hintergrund abgeht
	@RequestMapping("/debug/rooms")
	public String rooms() {
		return Websites.rooms(isRoomEmpty);
	}
	
	//zeigt die Homepage des Call-Services
	@RequestMapping("/")
	public String home() {
		return Websites.home();
	}
	
	
	//Funktion, die eine gegeben Datei ausliest und als String zurückgibt
	private static String readFile(String path, Charset encoding) throws IOException{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
		
}
