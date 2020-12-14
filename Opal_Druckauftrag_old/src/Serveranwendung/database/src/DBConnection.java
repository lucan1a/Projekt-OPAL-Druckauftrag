import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connection Klasse um die Verbindung zur MariaDB Opal-Druckauftrag-DB herzustellen oder abzubauen
 * @author pauljannasch
 */

public class DBConnection {

	public Statement statement;
	public Connection connection;

	DBConnection() {
		
	};

	/**
	 * Connection zur MariaDB Opal-Druckauftrag-DB aufbauen
	 * @return statement
	 */
	public Statement connectToDB() {

		try {

			/**
			 * Connection zur MariiaDB aufbauen
			 */
			Class.forName("org.mariadb.jdbc.Driver");
			this.connection = DriverManager.getConnection(
				"jdbc:mariadb://localhost/Opal-Druckauftrag-DB?user=root&password=Opal-Druckauftrag2020");

			/**
			 * aus der Connection das Statement generieren
			 * Rueckgabe fuer die weitere Nutzung
			 */
			this.statement = connection.createStatement();

			/**
			 * Testausgabe um die momentane Connection zu testen
			 */
			//System.out.println("connected." + " | " + this.connection + " | " + this.statement);

			return this.statement;
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
		}

		return null;
	}

	/**
	 * Connection zur MariaDB Opal-Druckauftrag-DB wieder abbauen
	 * Reihenfolge ist wichtig
	 * erst ResultSet(extern), dann statement, dann connection
	 */
	public void disconnectFromDB() {
		try {

			this.statement.close();
			this.connection.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}
	
}
