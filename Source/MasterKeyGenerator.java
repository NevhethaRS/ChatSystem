package keyGeneration;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class MasterKeyGenerator {
	public static  String ALGORITHM = "RSA";
	public static  String PRIVATE_KEY_FILE="";
	public static  String PUBLIC_KEY_FILE="";
	public static  String FOR_CLIENT="";
	MasterKeyGenerator(){
		PRIVATE_KEY_FILE="MasterPrivate.key";
		PUBLIC_KEY_FILE="MasterPublic.key";
		FOR_CLIENT="MasterPublic.key";
		try {
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
			keyGen.initialize(1024);
			final KeyPair key = keyGen.generateKeyPair();

			File privateKeyFile = new File(PRIVATE_KEY_FILE);
			File publicKeyFile = new File(PUBLIC_KEY_FILE);
			File clientKeyFile=new File(FOR_CLIENT);
			
			File pathFile=new File("path.txt");
			if(pathFile.getParentFile()!=null){
				pathFile.getParentFile().mkdirs();
			}
		
			// Create files to store public and private key
			if (privateKeyFile.getParentFile() != null) {
				privateKeyFile.getParentFile().mkdirs();
			}
			privateKeyFile.createNewFile();

			if (publicKeyFile.getParentFile() != null) {
				publicKeyFile.getParentFile().mkdirs();
			}
			publicKeyFile.createNewFile();

			if(clientKeyFile.getParentFile()!=null){
				clientKeyFile.getParentFile().mkdirs();
			}
			clientKeyFile.createNewFile();
			// Saving the Public key in a file
			ObjectOutputStream publicKeyOS = new ObjectOutputStream(
					new FileOutputStream(publicKeyFile));
			publicKeyOS.writeObject(key.getPublic());
			publicKeyOS.close();

			// Saving the Private key in a file
			ObjectOutputStream privateKeyOS = new ObjectOutputStream(
					new FileOutputStream(privateKeyFile));
			privateKeyOS.writeObject(key.getPrivate());
			privateKeyOS.close();

			//Saving public key for client access
			ObjectOutputStream pKeyOS = new ObjectOutputStream(
					new FileOutputStream(clientKeyFile));
			pKeyOS.writeObject(key.getPublic());
			pKeyOS.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args){

			MasterKeyGenerator g=new MasterKeyGenerator();

	}
}
