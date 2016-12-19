package login;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import keyGeneration.Encryption;

import javax.swing.JTextArea;
import java.io.*;
import java.net.*;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

public class ChatWindow extends JFrame {

	private JPanel contentPane;
	public boolean connected = false;
	JTextArea message;
	JTextArea display;
	JButton btnSend;
	ChatClient1 c;
	private JButton btnEnd;
	private JButton btnConnect;
	private JButton btnOnlineUsers;
	private JLabel lblMessage;
	private JLabel lblWindow;
	private JButton btnClear;

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public ChatWindow(ChatClient1 cc) throws UnknownHostException, IOException {
		c = cc;
		setTitle("Chat System "+cc.username);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		message = new JTextArea();
		message.setBounds(20, 34, 207, 117);
		contentPane.add(message);

		display = new JTextArea();
		display.setBounds(239, 34, 192, 208);
		contentPane.add(display);
		display.setEditable(false);

		btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connected = true;
				try {
					messageAction(e);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			private void messageAction(ActionEvent e) throws IOException {
				String mess = message.getText().trim();
				// System.out.println(mess);
			
					/* send message to friend */
					synchronized (c) {
						if (c.connection) {
							display.append(c.username + ":" + mess + "\n");
							if (mess.equals("end")) {
								c.connection = false;
								display.append("****SESSION END****" + "\n");
								message.setVisible(false);
								btnSend.setVisible(false);
							}
							new SendMess(c.username, c, mess, c.sessionKey).start();
						}
					}
				
				message.setText(" ");
			}


		});
		btnSend.setBounds(57, 193, 117, 29);
		contentPane.add(btnSend);
		display.setVisible(false);
		message.setVisible(false);
		btnSend.setVisible(false);


		btnEnd = new JButton("End");
		btnEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String mess=c.username+"@LOGOUT@"+c.serverPort;
				new Send1(c,mess).start();
				ExistingUser eu=new ExistingUser();
				eu.setVisible(true);
				dispose();
				if(c.connection){
					new SendMess(c.username,c,"end",c.sessionKey).start();
				}
				try {
					c.server.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnEnd.setBounds(57, 223, 117, 29);
		contentPane.add(btnEnd);
		btnEnd.setVisible(false);



		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String mess = message.getText().trim();
				mess="connect to "+mess;
				/* connection message to be sent to server */
				if (mess.startsWith("connect to")) {
					String[] ip=mess.split(" ");
					if(c.username.compareTo(ip[1])==0){
						JOptionPane.showMessageDialog(contentPane,"Invalid friend name");
						contentPane.setVisible(true);
					}
					else {
						new Send1(c, parseConnectionMessage(mess)).start();

						/* listen to connection reply from server */
						Socket fromServer;
						try {
							fromServer = c.server.accept();
							new LClient1(fromServer, c, display).start();
							btnConnect.setVisible(false);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					message.setText("");
				} 

			}
			/* <Username>@CONNECTION@<friendUser> */
			private String parseConnectionMessage(String mess) {
				String[] inp = mess.split(" ");
				String m = ChatClient1.username + "@CONNECTION@" + inp[2] + "@" + c.secretKey;
				return m;
			}
		});
		btnConnect.setBounds(57, 163, 117, 29);
		contentPane.add(btnConnect);
		btnConnect.setVisible(false);
		btnOnlineUsers = new JButton("Online Users");
		btnOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					String mess1=c.username+"@ONLINE@"+c.serverPort;
					new Send1(c,mess1).start();
					Socket fromServer;
					try {
						fromServer = c.server.accept();
						new LClient1(fromServer, c, display).start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			}
		});
		btnOnlineUsers.setBounds(314, 6, 117, 29);
		contentPane.add(btnOnlineUsers);
		btnOnlineUsers.setVisible(false);
		
		lblMessage = new JLabel("Message");
		lblMessage.setBounds(20, 11, 61, 16);
		contentPane.add(lblMessage);
		lblMessage.setVisible(false);
		
		lblWindow = new JLabel("Window");
		lblWindow.setBounds(239, 11, 61, 16);
		contentPane.add(lblWindow);
		lblWindow.setVisible(false);
		
		btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				display.setText("");
			}
		});
		btnClear.setBounds(283, 249, 117, 23);
		contentPane.add(btnClear);
		btnClear.setVisible(false);
		final JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				display.setVisible(true);
				message.setVisible(true);
				btnSend.setVisible(true);
				btnEnd.setVisible(true);
				btnConnect.setVisible(true);
				btnOnlineUsers.setVisible(true);
				lblMessage.setVisible(true);
				lblWindow.setVisible(true);
				btnClear.setVisible(true);
				new ListenFromFriend(c.server, c, display,message,btnSend,btnConnect).start();
				btnStart.setVisible(false);
			}
		});
		btnStart.setBounds(57, 6, 117, 29);
		contentPane.add(btnStart);
		
		

	}
}

class ListenFromFriend extends Thread {
	ServerSocket server;
	JTextArea display;
	ChatClient1 cc;
	JTextArea mesBox;
	JButton snd;
	JButton connect;
	ListenFromFriend(ServerSocket ser, ChatClient1 c, JTextArea disp,JTextArea mb,JButton sd,JButton connet) {
		server = ser;
		display = disp;
		cc = c;
		mesBox=mb;
		snd=sd;
		connect=connet;
	}


	public void run() {
		try {
			Socket listen = server.accept();
			DataInputStream ip = new DataInputStream(listen.getInputStream());
			String message;
			while (true) {
				if ((message = ip.readUTF()) != null) {
					if (message.startsWith("TICKET_TGT")) {
						String[] inp = message.split("@");
						/*
						 * TICKET@"+userWhoWishesToConnect+"@SESSION@
						 * "+sessionKey+"@MASTER@"+secretKey
						 */
						// System.out.print("received ticket");
						String ticket = inp[1];
						String crcCode = Encryption.hash(ticket);
						char[] crc = new char[16];
						crcCode.getChars(0, 15, crc, 0);
						crcCode = new String(crc);
						if (crcCode.compareTo(inp[2]) == 0) {
							String decryptedTicket = Encryption.aesDecrypt(ticket, cc.secretKey);
							String[] t = decryptedTicket.split("@");
							cc.sessionKey = t[3];
						} else {
							JOptionPane.showMessageDialog(cc.c, "Ticket Compromised");
						}
					} else {
						while (cc.sessionKey == null) {

						}
						String enMessage = Encryption.aesDecrypt(message, cc.sessionKey);
						String[] mess = enMessage.split(" ");
						String crcCode = Encryption.hash(mess[0]);
						char[] crc = new char[16];
						crcCode.getChars(0, 15, crc, 0);
						crcCode = new String(crc);
						if (crcCode.compareTo(mess[1]) == 0) {

							if (mess[0].startsWith("NEW_CONNECTION")) {
								String[] inp = enMessage.split("_");
								String[] in = inp[4].split(" ");
								// System.out.println(in[0]);
								cc.friend = new Socket("localhost", Integer.parseInt(in[0]));
								cc.connection = true;
								display.append("NEW SESSION FROM " + inp[3] + "\n");
								connect.setVisible(false);
							} else {
								mess[0]=mess[0].replace("`", " ");
								//System.out.println(mess[0]);
								display.append(mess[0] + "\n");
								if (mess[0].equals("end")) {
									cc.connection = false;
									display.append("****SESSION END****" + "\n");
									mesBox.setVisible(false);
									snd.setVisible(false);
								}
							}
						} else {
							JOptionPane.showMessageDialog(cc.c, "Message from friend compromised");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class SendMess extends Thread {
	ChatClient1 c;
	String message;
	String sendUser;
	String key;

	SendMess(String user, ChatClient1 cc, String m, String k) {
		sendUser = user;
		c = cc;
		message = m;
		key = k;
	}

	public void run() {
		DataOutputStream out;
		try {
			// System.out.print("jff");
			out = new DataOutputStream(c.friend.getOutputStream());
			// System.out.print(key);
			if (key.equals("nokey")) {
				// System.out.print("sending ticket");
				String crcCode = Encryption.hash(message);
				char[] crc = new char[16];
				crcCode.getChars(0, 15, crc, 0);
				crcCode = new String(crc);
				out.writeUTF("TICKET_TGT@" + message + "@" + crcCode);
				String m = "NEW_CONNECTION_FROM_" + c.username + "_" + c.serverPort;
				new SendMess(c.username, c, m, c.sessionKey).start();
			} else if (message.startsWith("NEW_CONNECTION")) {
				String crcCode = Encryption.hash(message);
				char[] crc = new char[16];
				crcCode.getChars(0, 15, crc, 0);
				crcCode = new String(crc);
				String cipherText = Encryption.aesEncrypt(message + " " + crcCode, key);
				out.writeUTF(cipherText);
			} else if (message.equals("end")) {
				String crcCode = Encryption.hash("end");
				char[] crc = new char[16];
				crcCode.getChars(0, 15, crc, 0);
				crcCode = new String(crc);
				String cipherText = Encryption.aesEncrypt("end"+" " + crcCode, key);
				out.writeUTF(cipherText);
			} else {
				message=message.replace(" ", "`");
				String crcCode = Encryption.hash(parse(message));
				//System.out.println(parse(message));
				char[] crc = new char[16];
				crcCode.getChars(0, 15, crc, 0);
				crcCode = new String(crc);
				String cipherText = Encryption.aesEncrypt(parse(message) + " " + crcCode, key);
				out.writeUTF(cipherText);
			}
		} catch (Exception e) {
			System.out.print(c.username);
			e.printStackTrace();
		}

	}

	private String parse(String message2) {
		return sendUser + "@" + message;
	}
}

// to listen from server
class LClient1 extends Thread {
	Socket client;
	ChatClient1 c;
	JTextArea dis;

	LClient1(Socket s, ChatClient1 cc, JTextArea d) {
		client = s;
		c = cc;
		dis = d;
	}

	public void run() {
		try {
			DataInputStream ipReader = new DataInputStream(client.getInputStream());
			String readInput = ipReader.readUTF();
			String message = Encryption.aesDecrypt(readInput, c.secretKey);
			String[] mess = message.split(" ");
			// System.out.println(readInput);
			String crcCode = Encryption.hash(mess[0]);
			char[] crc = new char[16];
			crcCode.getChars(0, 15, crc, 0);
			crcCode = new String(crc);
			if (crcCode.compareTo(mess[1]) == 0) {
				if(mess[0].startsWith("ONLINE")){
					dis.append("ONLINE USERS"+"\n");
					String[] ip=mess[0].split("@");
					for(int i=1;i<ip.length;i++){
						dis.append(ip[i]+"\n");
					}	
				}
				if (mess[0].startsWith("CONNECTION@")) {
					String[] inp = mess[0].split("@");
					/* for sending messages to friend */
					c.friend = new Socket(inp[2], Integer.parseInt(inp[3]));
					c.connection = true;
					dis.append("NEW SESSION TO " + inp[1] + "\n");
					c.sessionKey = inp[4];
					String ticket = inp[5];
					// System.out.println(ticket);
					new SendMess(c.username, c, ticket, "nokey").start();

				} else if (mess[0].equals("NO-CONNECTION")) {
					dis.append("NO SUCH USER OR USER IS OFFLINE");
				}
			} else {
				JOptionPane.showMessageDialog(c.c, "Connection info from Master compromised");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}