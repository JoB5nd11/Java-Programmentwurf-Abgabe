import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;

public class OptionWindow {

	private JFrame frmOptions;

	public static void OptionScreen() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OptionWindow window = new OptionWindow();
					window.frmOptions.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public OptionWindow() {
		initialize();
	}

	private void initialize() {
		
		//Fenster-Einstellungen
		frmOptions = new JFrame();
		frmOptions.setResizable(false);
		frmOptions.setTitle("Options");
		frmOptions.setBounds(100, 100, 287, 362);
		frmOptions.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmOptions.setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "\\res\\" + "jf_phone_service.png"));
		frmOptions.getContentPane().setLayout(new BoxLayout(frmOptions.getContentPane(), BoxLayout.X_AXIS));

		JPanel ringtonePanel = new JPanel();
		frmOptions.getContentPane().add(ringtonePanel);
		ringtonePanel.setLayout(new BorderLayout(0, 0));
		
		JPanel ringtoneListPanel = new JPanel();
		ringtonePanel.add(ringtoneListPanel, BorderLayout.CENTER);
		ringtoneListPanel.setLayout(new BorderLayout(0, 0));
		
		//Liste mit allen Klingeltönen
		JList ringtoneList = new JList(getRingtones());
		ringtoneList.setFont(new Font("Segoe UI", Font.PLAIN, 20));
		JScrollPane scrollPane = new JScrollPane(ringtoneList);
		ringtoneListPanel.add(scrollPane);
		
		JPanel ringtoneHeaderPanel = new JPanel();
		ringtonePanel.add(ringtoneHeaderPanel, BorderLayout.NORTH);
		ringtoneHeaderPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel ringtoneHeader = new JLabel("ringtone");
		ringtoneHeader.setAlignmentY(Component.TOP_ALIGNMENT);
		ringtoneHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
		ringtoneHeaderPanel.add(ringtoneHeader, BorderLayout.NORTH);
		ringtoneHeader.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel statusLabel = new JLabel(" ");
		ringtoneHeaderPanel.add(statusLabel, BorderLayout.CENTER);
		statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		//Neuen Klingelton einstellen
		JButton applyRingtoneButton = new JButton("Apply");
		applyRingtoneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selected = (String) ringtoneList.getSelectedValue();
				SoundLoader.ringtone = selected;
				//Durch grünen Text anzeigen, dass die Änderungen gespeichert wurde
				if(!statusLabel.getForeground().equals(new Color(38, 224, 127))) {
					statusLabel.setForeground(new Color(38, 224, 127)); 
				//Falls gründer Text schon vorhanden, ändere die Farbe leicht, um trotzdem visuell Feedback zu bekommen
				}else {
					statusLabel.setForeground(new Color(0, 182, 135)); 
				}
				
				statusLabel.setText("Settings saved!");
			}
		});
		ringtonePanel.add(applyRingtoneButton, BorderLayout.SOUTH);
	}
	
	//Lade alle Klingeltöne aus dem Ordner
	public String[] getRingtones(){
		File f = new File(System.getProperty("user.dir") + "\\res\\" + "ringtones");
		return f.list();
	}
}
