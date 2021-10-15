package tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

//==================================================================
//Diese ganze Klasse ist inspiriert von einem StackOverflow-Beitrag.
//Wir verstehen noch nicht 100%, wie das alles funktioniert
//==================================================================

public class CustomListRenderer extends JLabel implements ListCellRenderer{
	String statusList[] = null;
	
	public CustomListRenderer() {
		setOpaque(true);
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		//Hintergrund weiß
		setBackground(Color.WHITE);
		setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		//Nutzer offline, Schrift-Farbe: grau
		if(value.toString().contains("(offline)")) {
			setForeground(Color.LIGHT_GRAY);
			
		//Nutzer am Telefon, Schrift-Farbe: rot
		}else if(value.toString().contains("(talking)")) {
			setForeground(new Color(250, 49, 74)); //Wieder gleicher Rotton wie "Decline-Call-Button"
		
		//Nutzer online und nicht am Telefon, Schrift-Farbe: grün
		}else if(value.toString().contains("(online)")) {
			setForeground(new Color(38, 224, 127)); //Wieder gleicher Grünton wie "Accept-Call-Button
		}
		
		//Eigenen Text-Renderer, damit Name oben groß und Status unten klein angezeigt wird
		setText(this.textRenderer(value.toString()));
		
		//?
		return this;
	}
	
	
	//Benutzderfinierte Render-Funktion für den Text der JList-Klasse, umgesetzt mit HTML und CSS
	private String textRenderer(String text) {
		String res = text;
		
		//Wenn der Text zweiteilig ist (Name und Status vorhanden):
		if(res.split(" ").length >= 2) {
			res = "<html>\r\n"
					+ "<head>\r\n"
					+ "  <title>Java PE Server</title>\r\n"
					+ "  <style>\r\n"
					+ "  .text{\r\n"
					+ "    margin-bottom: 10px;\r\n"
					+ "    margin-right: 70px;\r\n"
					+ "  }\r\n"
					+ "  .sm {\r\n"
					+ "    font-style: italic;\r\n"
					+ "  }\r\n"
					+ "  </style>\r\n"
					+ "  <div class=\"text\">" + res.split(" ")[0] + "\r\n"
					+ "    <br />\r\n"
					+ "    <small class=\"sm\">" + res.split(" ")[1].replace('(', ' ').replace(')', ' ') + "</small>\r\n"
					+ "  </div>\r\n"
					+ "</html>";
		
		//Wenn nur der Name, aber Status nicht vorhanden
		}else {
			res = "<html>\r\n"
					+ "<head>\r\n"
					+ "  <title>Java PE Server</title>\r\n"
					+ "  <style>\r\n"
					+ "  .text{\r\n"
					+ "    margin-bottom: 10px;\r\n"
					+ "    margin-right: 70px;\r\n"
					+ "  }\r\n"
					+ "  .sm {\r\n"
					+ "    font-style: italic;\r\n"
					+ "  }\r\n"
					+ "  </style>\r\n"
					+ "</html>";
		}
		
		return res;
	}
}
