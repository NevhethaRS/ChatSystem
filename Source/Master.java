package chatsystem;

import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import keyGeneration.Encryption;
import sqlconnection.SqlConnection;

class MasterServer extends Thread {
	DataInputStream ipReader;
	Socket client;
	Master master;

	MasterServer(Socket c, Master m) {
		master = m;
		client = c;

	}

	public void run() {
		try {
			ipReader = new DataInputStream(client.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			int size;
			byte[] cipherMessage;
			ObjectInputStream inputStream;
			String readInput;
			while ((size = ipReader.readInt()) != 0) {
				cipherMessage = new byte[size];
				ipReader.readFully(cipherMessage, 0, cipherMessage.length);
				inputStream = new ObjectInputStream(new FileInputStream(
						"MasterPrivate.key"));
				final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
				readInput = Encryption.rsaDecrypt(cipherMessage, privateKey);
				String[] message = readInput.split(" ");
				String crcCode = Encryption.hash(message[0]);
				char[] crc = new char[16];
				crcCode.getChars(0, 15, crc, 0);
				crcCode = new String(crc);
				if (message[1].compareTo(crcCode) == 0) {
					//System.out.println(message[0]);
					parseInput(message[0]);
				} else {
					String[] m = message[0].split("@");
					new MClient("localhost", Integer.parseInt(m[2]), "error", m[5]).start();

				}
			}
			client.close();
			ipReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseInput(String readInput) throws UnknownHostException, IOException, SQLException {
		String[] input = readInput.split("@");
		String clientname = input[0];
		// System.out.print(readInput);
		if (input.length == 3 && input[1].compareTo("ONLINE") == 0) {
			// System.out.println("online");
			String users = "ONLINE";
			String key = "";
			for (String user : master.clientInfo.keySet()) {
				users = users + "@" + user;
				if (user.compareTo(clientname) == 0) {
					key = master.clientInfo.get(user).key;
				}
			}
			new MClient("localhost", Integer.parseInt(input[2]), users, key).start();
		}
		if (input.length == 3 && input[1].compareTo("LOGOUT") == 0) {
			//System.out.println("logout");
			SqlConnection db = new SqlConnection();
			Connection con = db.getConnection();
			System.out.println(input[0]);
			String query="SELECT * from USER where username=\"" + input[0]
					+ "\";";
			Statement s = con.createStatement();
			ResultSet rs=s.executeQuery(query);
			if(rs.next()){
				int online=0;
				query="UPDATE user SET online="+online+" where username=\""+input[0]+"\";";
				s.executeUpdate(query);
			}
			for (String user : master.clientInfo.keySet()) {
				if (user.compareTo(input[0]) == 0) {
					 master.clientInfo.remove(user);
				}
			}
			con.close();
		}
		if (input.length == 6 && input[0].startsWith("ACCOUNT_")) {
			try {
				SqlConnection db = new SqlConnection();
				Connection con = db.getConnection();

				String query="SELECT * from USER where username=\"" + input[1]
						+ "\";";
				Statement s = con.createStatement();
				ResultSet rs=s.executeQuery(query);
				System.out.println("Creation");
				System.out.println(clientname);
				if(rs.next()){
					System.out.println("INside");
					new MClient("localhost", Integer.parseInt(input[2]), "error", input[5]).start();
				}
				else {
					int online=0;
					query = "INSERT into USER values('" + input[1] + "'," + input[2] + ",'" + input[3] + "','"
							+ input[4] + "','" + input[5] + "',"+online+");";
					s = con.createStatement();
					s.executeUpdate(query);
					con.close();
					new MClient("localhost", Integer.parseInt(input[2]), "success", input[5]).start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (input.length == 5 && input[0].startsWith("LOGIN")) {
			try {
				SqlConnection db = new SqlConnection();
				Connection con = db.getConnection();

				String query = "SELECT * from USER where username=\"" + input[1] + "\"and password=\"" + input[2]
						+ "\";";
				System.out.println(query);
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery(query);
				if (rs.next()) {
					int online=rs.getInt("online");
					if(online==0){
						master.clientInfo.put(input[1], new Info("localhost", Integer.parseInt(input[3]), input[4]));
						master.clientCount++;
						new MClient("localhost", Integer.parseInt(input[3]), "login-success", input[4]).start();
						online=1;
						query="UPDATE user SET online="+online+"  where username=\"" + input[1] + "\";";
						s.executeUpdate(query);
					}
					else 
						new MClient("localhost", Integer.parseInt(input[3]), "login-fail", input[4]).start();
				} else {
					new MClient("localhost", Integer.parseInt(input[3]), "login-fail", input[4]).start();
				}
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (input.length == 4 && input[1].equals("CONNECTION")) {
			System.out.println("Connection message");
			String friendUser = input[2];
			String uIp = master.clientInfo.get(clientname).ip;
			int uport = master.clientInfo.get(clientname).port;
			String key = input[3];
			boolean status = false;
			for (String u : master.clientInfo.keySet()) {

				if (u.equals(friendUser)) {
					String ip = master.clientInfo.get(u).ip;
					int port = master.clientInfo.get(u).port;
					// System.out.println("getting here");
					String message = "CONNECTION@" + friendUser + "@" + ip + "@" + port;
					status = true;
					/* generating session key */
					String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
					String session = clientname + timeStamp + friendUser;

					/* generating ticket to friend */
					String skey = null;
					for (String user : master.clientInfo.keySet()) {
						if (friendUser.equals(user)) {
							skey = master.clientInfo.get(user).key;
						}
					}
					// System.out.print(skey);
					String ticket = "TICKET@" + clientname + "@SESSION@" + session + "@MASTER@" + skey;
					String encryptedTicket = Encryption.aesEncrypt(ticket, skey);
					message = message + "@" + session + "@" + encryptedTicket;
					new MClient(uIp, uport, message, key).start();
				}
			}
			if (!status)
				new MClient(uIp, uport, "NO-CONNECTION", key);
		}
	}
}

class MClient extends Thread {
	String ip;
	int port;
	String message;
	String key;

	MClient(String uip, int uport, String m, String k) {
		ip = uip;
		port = uport;
		message = m;
		key = k;
	}

	public void run() {
		try {
			Socket c = new Socket(ip, port);
			DataOutputStream out = new DataOutputStream(c.getOutputStream());
			String crcCode = Encryption.hash(message);
			char[] crc = new char[16];
			crcCode.getChars(0, 15, crc, 0);
			crcCode = new String(crc);
			System.out.println(message);
			String cipherText = Encryption.aesEncrypt(message + " " + crcCode, key);
			out.writeUTF(cipherText);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class MasterClient extends Thread {
	DataOutputStream out;
	Scanner sc;
	Socket client;
	Master master;

	MasterClient(Master m) throws UnknownHostException, IOException {
		master = m;
		sc = new Scanner(System.in);
	}

	public void run() {
		try {
			while (true) {
				synchronized (master) {
					if (master.clientCount > 0 && sc.hasNext()) {
						String input = sc.nextLine();
						for (String u : master.clientInfo.keySet()) {
							String ip = master.clientInfo.get(u).ip;
							int port = master.clientInfo.get(u).port;
							client = new Socket(ip, port);
							out = new DataOutputStream(client.getOutputStream());
							out.writeUTF(input);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Info {
	String ip;
	int port;
	String key;

	Info(String i, int p, String k) {
		ip = i;
		port = p;
		key = k;
	}
}

public class Master {
	Map<String, Info> clientInfo = new ConcurrentHashMap<String, Info>();
	static Scanner sc = new Scanner(System.in);
	public static int portNo;
	int clientCount;
	ServerSocket masterServer;
	BufferedReader ipReader;
	Socket client;
	private static String ip = "localhost";

	Master(int port) throws IOException {
		portNo = port;
		clientCount = 0;
		masterServer = new ServerSocket(port);
		new MasterClient(this).start();
		while (true) {
			client = masterServer.accept();
			new MasterServer(client, this).start();
		}
	}

	public static void main(String[] args) throws NumberFormatException, IOException, SQLException {

		if (args.length < 1) {
			System.out.println("Required Port number for the server");
		} else {
			new Master(Integer.parseInt(args[0]));
		}
	}

	public static String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}