import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;


//import com.sun.java.swing.plaf.windows.resources.windows;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.border.BevelBorder;

public class PopUpWindow extends MainWindow {

	private JFrame PopUpWindow;
	public static PopUpWindow window = new PopUpWindow();
	JLabel incommingCallLabel = new JLabel("incoming call from");


	public static void OptionScreen() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window.PopUpWindow.setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	public PopUpWindow() {
		initialize();
	}


	private void initialize() {
		//Fenster-Einstellungen
		PopUpWindow = new JFrame();
		PopUpWindow.setAlwaysOnTop(true);
		PopUpWindow.setResizable(false);
		PopUpWindow.setTitle("PopUp Window");
		PopUpWindow.setBounds(100, 100, 375, 157);
		PopUpWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		PopUpWindow.getContentPane().setLayout(new BorderLayout(0, 0));
		PopUpWindow.setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "\\res\\" + "jf_phone_service.png"));
		
		JPanel identityPanel = new JPanel();
		PopUpWindow.getContentPane().add(identityPanel, BorderLayout.CENTER);
		identityPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel header = new JPanel();
		identityPanel.add(header, BorderLayout.NORTH);
		
		incommingCallLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
		header.add(incommingCallLabel);
		
		JPanel panel_1 = new JPanel();
		identityPanel.add(panel_1, BorderLayout.SOUTH);
		
		JPanel body = new JPanel();
		body.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		identityPanel.add(body, BorderLayout.CENTER);
		body.setLayout(new BoxLayout(body, BoxLayout.X_AXIS));
		
		JPanel panel = new JPanel();
		panel.setAlignmentX(0.0f);
		panel.setAlignmentY(Component.TOP_ALIGNMENT);
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		body.add(panel);
		
		JButton acceptCall = new JButton("");
		panel.add(acceptCall);
		acceptCall.setPreferredSize(new Dimension(60, 60));
		acceptCall.setIcon(MainWindow.resizeIcon(new ImageIcon(System.getProperty("user.dir") + "\\res\\" + "accept.png"), 50, 50));
		
		JButton declineCall = new JButton("");
		declineCall.setVerticalAlignment(SwingConstants.TOP);
		//Telefonat ablehnen
		declineCall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow.pop = true;
				MainWindow.frame.declineCall();
			}
		});
		panel.add(declineCall);
		declineCall.setPreferredSize(new Dimension(60, 60));
		declineCall.setIcon(MainWindow.resizeIcon(new ImageIcon(System.getProperty("user.dir") + "\\res\\" + "decline.png"), 50, 50));
		//Telefonat annehmen
		acceptCall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow.pop = true;
				MainWindow.frame.acceptCall();
			}
		});
	}
	
	//Fenster sichtbar machen
	public void visible() {
		window.PopUpWindow.setVisible(true);
		window.incommingCallLabel.setText("incomming call from: " + MainWindow.callerInformation[1]);
	}
	//Fenster wieder verstecken
	public void hidden() {
		window.PopUpWindow.setVisible(false);
	}
}
