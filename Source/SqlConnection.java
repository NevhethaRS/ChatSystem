package sqlconnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlConnection {
	public static Connection getConnection(){
		Connection conn = null;
		try { 
			Class.forName("com.mysql.jdbc.Driver").newInstance(); 
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatSystem","root","radha"); 
		} 
		catch (Exception e) 
		{ 
			e.printStackTrace();
		}
		return conn;
	}

	public static void closeSql(Connection conn) throws SQLException{
		conn.close();
	}
	public static void main(String[] args) throws SQLException{

		Connection c = SqlConnection.getConnection();
		System.out.println(c);
		SqlConnection.closeSql(c);

	}
}
