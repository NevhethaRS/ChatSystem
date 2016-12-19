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
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPasswordField;

public class NewUser extends JFrame {

	private JPanel contentPane;
	private JTextField fullname;
	private JTextField username;
	private JTextField port;
	private JButton btnBack;
	private JPasswordField passwordField;
	private JPasswordField passwordField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NewUser frame = new NewUser();
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
	public NewUser() {
		setTitle("Chat System- Create Account");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblFullName = new JLabel("Full Name");
		lblFullName.setBounds(88, 75, 100, 16);
		contentPane.add(lblFullName);

		fullname = new JTextField();
		fullname.setBounds(248, 70, 130, 26);
		contentPane.add(fullname);
		fullname.setColumns(10);

		JLabel lblUsername = new JLabel("Username");
		lblUsername.setBounds(88, 105, 100, 16);
		contentPane.add(lblUsername);

		username = new JTextField();
		username.setBounds(248, 100, 130, 26);
		contentPane.add(username);
		username.setColumns(10);

		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(88, 139, 85, 16);
		contentPane.add(lblPassword);

		JLabel lblPortFor = new JLabel("Port # For listening");
		lblPortFor.setBounds(88, 199, 130, 16);
		contentPane.add(lblPortFor);

		passwordField = new JPasswordField();
		passwordField.setBounds(248, 134, 130, 26);
		contentPane.add(passwordField);

		JLabel lblReenterPassword = new JLabel("Re-enter Password");
		lblReenterPassword.setBounds(88, 171, 130, 16);
		contentPane.add(lblReenterPassword);

		passwordField_1 = new JPasswordField();
		passwordField_1.setBounds(248, 166, 130, 26);
		contentPane.add(passwordField_1);

		port = new JTextField();
		port.setBounds(248, 194, 130, 26);
		contentPane.add(port);
		port.setColumns(10);

		JButton btnSubmit = new JButton("Create");
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String uname = username.getText().trim();
				char[] pass = passwordField.getPassword();
				String passwd1 = new String(pass);
				pass = passwordField_1.getPassword();
				String passwd2 = new String(pass);
				if(passwd1.contains("@")){
					JOptionPane.showMessageDialog(contentPane, "Password can not contain '@'");
					contentPane.setVisible(true);
					passwordField.setText("");
					passwordField_1.setText("");
				}
				else {
				if (passwd1.compareTo(passwd2) == 0) {
					boolean strong = passwordCheck(passwd1);
					if (strong) {
						String name = fullname.getText().trim();
						String portNo = port.getText().trim();
						if(!name.isEmpty() && !uname.isEmpty() && !passwd1.isEmpty() && !passwd2.isEmpty()
								&& !portNo.isEmpty()) {
							if (Integer.parseInt(portNo) > 65535 || Integer.parseInt(portNo) < 1024) {
								JOptionPane.showMessageDialog(contentPane, "Invalid port number");
								contentPane.setVisible(true);
								passwordField.setText("");
								passwordField_1.setText("");
								port.setText("");
							} else {
								try {
									Client c = new Client(uname, passwd1, name, portNo, contentPane);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						} else {
							JOptionPane.showMessageDialog(contentPane, "Empty Field not allowed");
							contentPane.setVisible(true);
						}
					} else {
						JOptionPane.showMessageDialog(contentPane,
								"password rules not met" + "\n" + "Password must contain 12 characters" + "\n"
										+ "Must include:" + "\n" + "A Special character" + "\n An uppercase letter"
										+ "\n A lower case letter" + " \n A digit" + "\n cannot conatin '@'");
						contentPane.setVisible(true);
						passwordField.setText("");
						passwordField_1.setText("");
					}
				} else {
					JOptionPane.showMessageDialog(contentPane, "passwords don't match");
					contentPane.setVisible(true);
					passwordField.setText("");
					passwordField_1.setText("");
				}
			}
			}
			private boolean passwordCheck(String passwd) {
				if (passwd.length() != 12)
					return false;
				if (!passwd.matches(".*[A-Z].*"))
					return false;
				if (!passwd.matches(".*[a-z].*"))
					return false;
				if (!passwd.matches(".*\\d.*"))
					return false;
				Pattern pattern = Pattern.compile("[a-zA-Z0-9]*");
				Matcher matcher = pattern.matcher(passwd);
				if (!matcher.matches()) {
					return true;
				} else {
					return false;
				}
			}
		});
		btnSubmit.setBounds(98, 232, 117, 29);
		contentPane.add(btnSubmit);

		btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Login login = new Login();
				login.setVisible(true);
				dispose();
			}
		});
		btnBack.setBounds(248, 232, 117, 29);
		contentPane.add(btnBack);

	}
}

class Client {
	public static String serverPort;

	Client(String u, String p, String name, String port, JPanel contentPane)
			throws NumberFormatException, UnknownHostException, IOException {
		new ChatClient(u, p, name, port, contentPane);
	}
}

class ChatClient {
	Socket client;
	ServerSocket server;
	Scanner stdReader;
	static String username;
	static String password;
	static String fullname;
	static int serverPort;
	boolean serverSend = true;
	String ip = "localhost";
	JPanel c;
	static String secretKey;

	public ChatClient(String uname, String passwd, String name, String port, JPanel contentPane)
			throws UnknownHostException, IOException {
		client = new Socket("localhost", 1123);
		username = uname;
		password = passwd;
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		secretKey = username + timeStamp;
		serverPort = Integer.parseInt(port);
		fullname = name;
		server = new ServerSocket(serverPort);
		c = contentPane;
		new Send(client, this).start();
		Socket listenClient = server.accept();
		new ListenClient(listenClient, this).start();
	}
}

class Send extends Thread {
	DataOutputStream out;
	Scanner sc;
	Socket sendClient;
	ChatClient c;

	Send(Socket client, ChatClient s) {
		sendClient = client;
		c = s;
	}

	public void run() {
		try {
			out = new DataOutputStream(sendClient.getOutputStream());

			String crcCode = Encryption.hash(message());
			char[] crc = new char[16];
			crcCode.getChars(0, 15, crc, 0);
			crcCode = new String(crc);
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(
					"MasterPublic.key"));
			final PublicKey publicKey = (PublicKey) inputStream.readObject();
			final byte[] cipherText = Encryption.rsaEncrypt(message() + " " + crcCode, publicKey);

			out.writeInt(cipherText.length);
			out.write(cipherText);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String message() {
		String startMessage = "ACCOUNT_CREATE@" + ChatClient.username + "@" + ChatClient.serverPort + "@"
				+ ChatClient.password + "@" + ChatClient.fullname + "@" + ChatClient.secretKey;
		return startMessage;
	}
}

class ListenClient extends Thread {
	Socket listenClient;
	DataInputStream ipReader;
	ChatClient c;

	ListenClient(Socket client, ChatClient s) {
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
					if (message[0].equals("success")) {
						JOptionPane.showMessageDialog(c.c, "Account Created Successfully");
						c.c.setVisible(false);
						Login l = new Login();
						l.setVisible(true);
					} else if (message[0].equals("error")) {
						JOptionPane.showMessageDialog(c.c, "Account Creation Failed");
						c.c.setVisible(false);
						Login l = new Login();
						l.setVisible(true);
					}
				} else {
					JOptionPane.showMessageDialog(c.c, "Status message compromised");
					c.c.setVisible(false);
					Login l = new Login();
					l.setVisible(true);
				}
			}
			listenClient.close();
			ipReader.close();
			c.server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
