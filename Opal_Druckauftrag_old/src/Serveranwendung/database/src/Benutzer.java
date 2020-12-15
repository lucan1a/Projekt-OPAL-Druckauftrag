import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** 
* Klasse Benutzer nach DBInterface, 
* Wiedergabe und Verwendung von Opal-Druckauftrag-DB.Benutzer, 
* Nichtbeschriebene Funktionen folgen dem Standardablauf beschrieben in DBInterface
* 
* @author pauljannasch
*/

public class Benutzer implements DBInterface {

    /* Membervariablen */
    public String  Bibliotheksnummer;
    public boolean Sperrung;
    public String  Vorname;
    public String  Nachname;

    /**
    * default Kontruktor fuer Standardnutzer
    * v.a. Nutzung fuer Testcases
    */
    Benutzer() {
        this.Bibliotheksnummer = "s99999";
        this.Sperrung = false;
        this.Vorname = "";
        this.Nachname = "";
    }

    /** 
    * Konstruktor fuer neues Objekt
    * erstellt benutzerdefinierten Nutzer
    */
    Benutzer(String Bibliotheksnummer, boolean Sperrung,
        String Vorname, String Nachname) {

        this.Bibliotheksnummer = Bibliotheksnummer;
        this.Sperrung = Sperrung;
        this.Vorname = Vorname;
        this.Nachname = Nachname;
    }

    /** 
    * Konstruktor fuer neues Objekt
    * gibt den Benutzer aus der Datenbank.Benutzer
    * nach Bibliotheksnummer zurueck
    */
    Benutzer(String Bibliotheksnummer) {
        DBConnection connection = new DBConnection();
		Statement statement;

		try {

			statement = connection.connectToDB();
			this.Bibliotheksnummer = Bibliotheksnummer;

            this.Sperrung = statement.execute("SELECT Sperrung FROM Benutzer WHERE" +  
				"Bibliotheksnummer = '" + Bibliotheksnummer + "'");
            this.Vorname = statement.executeQuery("SELECT Vorname FROM Benutzer WHERE" +
                "Bibliotheksnummer = '" + Bibliotheksnummer + "'").getString(1);
            this.Nachname = statement.executeQuery("SELECT Nachname FROM Benutzer WHERE" +
                "Bibliotheksnummer = '" + Bibliotheksnummer + "'").getString(1);

            statement.close();
            connection.disconnectFromDB();

		} catch(SQLException e) {
			e.printStackTrace();
            System.out.println("Nutzer nicht gefunden.");
		}
    }

    /** 
    * check if dieser Nutzer bereits in der Tabelle Benutzer vermerkt ist
    * @return if User exists
    */
    public boolean exists() {
        DBConnection connection = new DBConnection();
		Statement statement;

        try {

            statement = connection.connectToDB();

            ResultSet resultSet = statement.executeQuery("SELECT Vorname FROM Benutzer WHERE Bibliotheksnummer = '" +
                this.Bibliotheksnummer + "'");

            if(resultSet.next() == false) {
                    statement.close();
                    connection.disconnectFromDB();
                    return false;
            }

            statement.close();
            connection.disconnectFromDB();

            return true;

        } catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
    }
    
    @Override
    public String toString() {
    	return new String("'" + this.Bibliotheksnummer + "', " + this.Sperrung + ", '" +
    			this.Vorname + "', '" + this.Nachname + "'");
    }

    @Override
    public int insert() {
    	DBConnection connection = new DBConnection();
        Statement statement;

        try {

            statement = connection.connectToDB();
            statement.executeQuery("INSERT INTO Benutzer VALUES(" + this.toString() + ")");

            System.out.println("inserted: " + this.toString());

            statement.close();
            connection.disconnectFromDB();
            
			return 0;

			} catch(SQLException e) {
		        e.printStackTrace();
				return 1;
			}
	}

    @Override
    public int delete() {
		DBConnection connection = new DBConnection();
		Statement statement;

		try {

			statement = connection.connectToDB();
			statement.executeQuery("DELETE FROM Benutzer WHERE Bibliotheksnummer = '" + 
				this.Bibliotheksnummer + "'");

			statement.close();
			connection.disconnectFromDB();

			return 0;

		} catch(SQLException e) {
			e.printStackTrace();
			return 1;
		}
    }

    /** Beschreibung in DBInterface */
    @Override
    public int update() {

        DBConnection connection = new DBConnection();
		Statement statement;

		try {

			statement = connection.connectToDB();
			int r = statement.executeUpdate("UPDATE Benutzer SET " +
                "Sperrung = " + this.Sperrung + 
                ", Vorname = '" + this.Vorname +
                "', Nachname = '" + this.Nachname +
                "' WHERE Bibliotheksnummer = '" + this.Bibliotheksnummer + 
                "'");

            statement.close();
			connection.disconnectFromDB();

		} catch(SQLException e) {
			e.printStackTrace();
			return 1;
		}

    	return 0;
    }

    @Override 
    public Object getObject() {
    	return this;
    }
    
    @Override
    public void getAll() {
    	DBConnection connection = new DBConnection();
    	Statement statement;
    	
    	try {

			statement = connection.connectToDB();

			System.out.format("%-20s%-10s%-20s%-20s", "Bibliotheksnummer", "Sperrung", "Vorname", "Nachname");
			System.out.println("");

			ResultSet resultSet1 = statement.executeQuery("SELECT Bibliotheksnummer FROM Benutzer");
			ResultSet resultSet2 = statement.executeQuery("SELECT Sperrung FROM Benutzer");
			ResultSet resultSet3 = statement.executeQuery("SELECT Vorname FROM Benutzer");
			ResultSet resultSet4 = statement.executeQuery("SELECT Nachname FROM Benutzer");

			while(resultSet1.next() && resultSet2.next() && resultSet3.next() && resultSet4.next()) {
				System.out.format("%-20s", resultSet1.getString(1));
				System.out.format("%-10s", resultSet2.getString(1));
				System.out.format("%-20s", resultSet3.getString(1));
				System.out.format("%-20s", resultSet4.getString(1));
				System.out.println("");
			}

			statement.close();
			resultSet1.close();
			resultSet2.close();
			resultSet3.close();
			resultSet4.close();
			connection.disconnectFromDB();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * check ob der Nutzer gesperrt ist
     * @return if User is blocked
     */
    public boolean getSperrstatus() {
        DBConnection connection = new DBConnection();
        Statement statement;

        try {

            statement = connection.connectToDB();

            ResultSet resultSet = statement.executeQuery("SELECT Sperrung FROM Benutzer WHERE Bibliotheksnummer = '" +
                    this.Bibliotheksnummer + "'");

            resultSet.next();
            if(resultSet.getString(1).equals("0")) {
                System.out.println("Nutzer ist nicht gesperrt.");
                statement.close();
                connection.disconnectFromDB();
                return false;
            }

            System.out.println("Nutzer ist gesperrt.");
            statement.close();
            connection.disconnectFromDB();

            return true;

        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
    * Sperren des Nutzers this 
    */
    public void sperren() {
        this.Sperrung = true;
        this.update();

        System.out.println("Nutzer: " + this.Bibliotheksnummer + " gesperrt.");
    }
}
