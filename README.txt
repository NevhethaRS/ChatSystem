CS 6349 :NETWORK SECURITY-FALL’16 
Project-Secure Internet Messaging Application

Contents of the folder
**************************
JAR
Source
Report.pdf
README.txt

nxr153230/JAR contains
	Master.jar
	Client.jar
	GenerateMasterKeys.jar

nxr153230/Source contains
	MasterKeyGenerator.java	:	Class that generates RSA public key and private key for the master
	Encryption.java			:	Class that contains encryption and decryption functions for RSA, AES and SHA-256
	Master.java			:	Master Server that accepts connections from clients and helps them communicate with each other by generating session keys.
	Login.java			:	Entry Point for Client
	NewUser.java			:	Frame that helps in creating a client account with the master
	ExistingUser.java		:	Frame that helps in logging into the chat system  
	ChatWindow.java			:	Frame that forms the chat
	SQLConnection.java		:	Class that connects to database

Instructions for Running
******************************
———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————
The following instructions are to be followed in sequential order for running the project:
	1.Generate Keys
	2.Run the master
	3.Create a Database at the Server machine
	4.Run the client/client instances

1.Generate keys
****************
	Double click on GenerateMasterKeys.jar file contained in nxr153230/JAR

Function:
	This produces MasterPublic.key and MasterPrivate.key files in nxr153230/JAR which are RSA public and private keys respectively for the Master Server.


3.Run the master
****************
	From Terminal,
		1.change the current working directors to nxr153230/JAR
		2.java -jar Master.jar 1123

 	***Command for Running Master*** java -jar Master.jar <port#>

Function:
	This sets up the server up and running listening to connections on the specified port#

3.Create a Database at the Server machine
******************************************
	***Important*** mysql server has to be running at port 3306 with password “radha” at the server ***Important***
	commands for creating database
		create database chatSystem;
		use chatSystem;
		create table user (username varchar(20),port integer,password varchar(256),fullname varchar(50),secretKey varchar(128),online integer);
Function:
	This creates the database required at the server for storing client’s information.


4.Run the client/client instances
**********************************
	Double click on Client.jar file contained in nxr153230/JAR

Function:
	This opens the window for registering new users or to log in to the chat system by using client’s credentials

	***To run the second instance of the client*** Double click on Client2.jar file contained in nxr153230/JAR

TO RUN MORE INSTANCES OF CLIENTS-CREATE A COPY OF CLIENT AND DOUBLE CLICK ON THE COPY

***********************************************************************************************************************************************************************************
***********************************************************************************************************************************************************************************
———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————

