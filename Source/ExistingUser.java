package login;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import keyGeneration.Encryption;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;

public class ExistingUser extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField;
	private JButton btnBack;
	private JLabel lblPort;
	private JTextField textField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExistingUser frame = new ExistingUser();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ExistingUser() {
		setTitle("Chat System - Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		textField = new JTextField();
		textField.setBounds(246, 97, 130, 26);
		contentPane.add(textField);
		textField.setColumns(10);

		JLabel lblUsername = new JLabel("Username");
		lblUsername.setBounds(97, 102, 88, 16);
		contentPane.add(lblUsername);

		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(97, 147, 61, 16);
		contentPane.add(lblPassword);

		passwordField = new JPasswordField();
		passwordField.setBounds(246, 135, 130, 26);
		contentPane.add(passwordField);

		lblPort = new JLabel("Port # ");
		lblPort.setBounds(97, 187, 61, 16);
		contentPane.add(lblPort);

		textField_1 = new JTextField();
		textField_1.setBounds(246, 182, 130, 26);
		contentPane.add(textField_1);
		textField_1.setColumns(10);

		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String uname = textField.getText();
				char[] pass = passwordField.getPassword();
				String portNo=textField_1.getText();
				String passwd = new String(pass);
				if(!uname.isEmpty() && !passwd.isEmpty() && !portNo.isEmpty()){
					try {
						int port = Integer.parseInt(portNo);
						Client1 c = new Client1(uname, passwd, port, contentPane);
					} catch (NumberFormatException e1) {
						e1.printStackTrace();
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				else {
					JOptionPane.showMessageDialog(contentPane, "Require all fields");
					contentPane.setVisible(true);
				}
			}
		});
		btnLogin.setBounds(97, 227, 117, 29);
		contentPane.add(btnLogin);

		btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Login l = new Login();
				l.setVisible(true);
				contentPane.setVisible(false);
			}
		});
		btnBack.setBounds(246, 227, 117, 29);
		contentPane.add(btnBack);

	}

}

class Client1 {
	Client1(String u, String p, int port, JPanel cont) throws NumberFormatException, UnknownHostException, IOException {
		new ChatClient1(u, p, port, cont);
	}
}

class ChatClient1 {
	static Socket client;
	static ServerSocket server;
	Scanner stdReader;
	static String username;
	static String password;
	static int serverPort;
	boolean serverSend = true;
	String ip = "localhost";
	String sessionKey = null;
	JPanel c;
	Socket friend;
	boolean connection;
	String secretKey;
	public boolean connected = false;

	public ChatClient1(String uname, String passwd, int port, JPanel cont) throws UnknownHostException, IOException {
		client = new Socket("localhost", 1123);
		username = uname;
		password = passwd;
		c = cont;
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		secretKey = username + timeStamp;
		serverPort = port;
		new Send1(this, "login").start();
		server = new ServerSocket(serverPort);
		Socket listenClient = server.accept();
		new ListenClient1(listenClient, this).start();
	}
}

// whenever client wants to send message to master server
class Send1 extends Thread {
	DataOutputStream out;
	Scanner sc;
	Socket sendClient;
	ChatClient1 c;
	String message;

	Send1(ChatClient1 s, String mess) {
		sendClient = s.client;
		c = s;
		message = mess;
	}

	public void run() {
		try {
			out = new DataOutputStream(sendClient.getOutputStream());

			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(
					"MasterPublic.key"));
			final PublicKey publicKey = (PublicKey) inputStream.readObject();

			/* start message- LOGIN */
			if (message.equals("login")) {
				String crcCode = Encryption.hash(message());
				char[] crc = new char[16];
				crcCode.getChars(0, 15, crc, 0);
				crcCode = new String(crc);
				final byte[] cipherText = Encryption.rsaEncrypt(message() + " " + crcCode, publicKey);
				out.writeInt(cipherText.length);
				out.write(cipherText);
			} else {
				String crcCode = Encryption.hash(message);
				char[] crc = new char[16];
				crcCode.getChars(0, 15, crc, 0);
				crcCode = new String(crc);
				final byte[] cipherText = Encryption.rsaEncrypt(message + " " + crcCode, publicKey);
				out.writeInt(cipherText.length);
				out.write(cipherText);
				String[] ip=message.split("@");
				if(ip[1].equals("LOGOUT")){
					c.client.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String message() {
		String startMessage = "LOGIN@" + c.username + "@" + c.password + "@" + c.serverPort + "@" + c.secretKey;
		return startMessage;
	}
}

class ListenClient1 extends Thread {
	Socket listenClient;
	DataInputStream ipReader;
	ChatClient1 c;

	ListenClient1(Socket client, ChatClient1 s) {
		listenClient = client;
		c = s;
	}

	public void run() {
		try {
			ipReader = new DataInputStream(listenClient.getInputStream());
			String readInput = ipReader.readUTF();
			if (readInput != null) {
				String text = Encryption.aesDecrypt(readInput, c.secretKey);
				String[] message = text.split(" ");
				String crcCode = Encryption.hash(message[0]);
				char[] crc = new char[16];
				crcCode.getChars(0, 15, crc, 0);
				crcCode = new String(crc);
				if (crcCode.compareTo(message[1]) == 0) {
					if (message[0].equals("login-success")) {
						JOptionPane.showMessageDialog(c.c, "login Successful");
						c.c.setVisible(false);
						ChatWindow l = new ChatWindow(c);
						l.setVisible(true);
						c.c.setVisible(false);
					} else if (message[0].equals("login-fail")) {
						JOptionPane.showMessageDialog(c.c, "login Failure");
						c.c.setVisible(false);
						ExistingUser l = new ExistingUser();
						l.setVisible(true);
						c.c.setVisible(false);
						c.server.close();
					}
				}
			}
			listenClient.close();
			ipReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
