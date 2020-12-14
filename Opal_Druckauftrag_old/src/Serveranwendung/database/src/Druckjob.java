import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.time.LocalDate;

/** 
 * Klasse Druckjob nach DBInterface, 
 * Wiedergabe und Verwendung von Opal-Druckauftrag-DB.Druckjob, 
 * Nichtbeschriebene Funktionen folgen dem Standardablauf beschrieben in DBInterface
 *
 * @author pauljannasch
 */

public class Druckjob implements DBInterface {

    /* Membervariablen */
    public int      Job_ID;
    public String   Auftraggeber_Bibliotheksnummer;
    public int      Seitenzahl;
    public double   Preis;
    public boolean  Druckstatus;
    public String   Auftragsdatum;
    public boolean  printed;

    /**
     * Default Konstruktor
     */
    Druckjob() {
        this.Job_ID = 0;
        this.Auftraggeber_Bibliotheksnummer = "";
        this.Seitenzahl = 0;
        this.Preis = 0;
        this.Druckstatus = false;
        this.Auftragsdatum = "";
        this.printed = false;
    }

    /**
     * Parameter Konstruktor
     * printed wird in der Serveranwendung immer auf false gesetzt
     * @param Auftraggeber_Bibliotheksnummer
     * @param Seitenzahl
     * @param Preis
     * @param Druckstatus
     * @param Auftragsdatum
     */
    Druckjob(String Auftraggeber_Bibliotheksnummer,
        int Seitenzahl, double Preis, boolean Druckstatus, String Auftragsdatum) {

        this.Auftraggeber_Bibliotheksnummer = Auftraggeber_Bibliotheksnummer;
        this.Seitenzahl = Seitenzahl;
        this.Preis = Preis;
        this.Druckstatus = Druckstatus;
        this.Auftragsdatum = Auftragsdatum;
        this.printed = false;
    }

    @Override
    public String toString() {
    	return new String("'" +
	        this.Auftraggeber_Bibliotheksnummer + "', " +
	        this.Seitenzahl + ", " +
	        this.Preis + ", " +
            this.Druckstatus + ", '" +
            this.Auftragsdatum + "' ," +
            this.printed);
    }

    @Override
    public int insert() {
        DBConnection connection = new DBConnection();
        Statement statement;

        try {

            statement = connection.connectToDB();
            statement.executeQuery("INSERT INTO Druckjob " + 
				"VALUES(NULL, " + this.toString() + ")");

            statement.close();
            connection.disconnectFromDB();
            
			return 0;

        } catch(SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Setzt die ID des Objekts this, welche durch die Datenbank
     * generiert wurde
     */
    public void setID() {

        DBConnection connection = new DBConnection();
        Statement statement;
        
        try {

            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT Job_ID FROM Druckjob WHERE " + 
            "Auftraggeber_Bibliotheksnummer = '" + this.Auftraggeber_Bibliotheksnummer + "'");

            if(resultSet.next()) {
                this.Job_ID = Integer.parseInt(resultSet.getString(1));
            }

            resultSet.close();
            statement.close();
            connection.disconnectFromDB();

        } catch(SQLException e) {
		    e.printStackTrace();
		}
    }

    /**
     * Gibt alle Bibliotheksnummern von heute zurueck
     * @return String[] Bibliotheksnummern
     */
    public String[] getBibNr() {

        DBConnection connection = new DBConnection();
        Statement statement;
        ArrayList<String> list = new ArrayList<String>();
        
        try {

            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT Auftraggeber_Bibliotheksnummer " +
                "FROM Druckjob WHERE Auftragsdatum = current_date");

            while(resultSet.next()) {
                list.add(resultSet.getString(1));
            }

            String[] array = new String[list.size()];
            array = list.toArray(array);

            resultSet.close();
            statement.close();
            connection.disconnectFromDB();

            return array;

        } catch(SQLException e) {
            e.printStackTrace();
            return null;
		}
    }

    /**
     * Gibt alle Druckjobs zurueck, welche seite ueber 7 Tagen nicht abgeholt wurden
     * @return String[] Bibliotheksnummern
     */
    public String[] getAllOverdueJobs() {

        DBConnection connection = new DBConnection();
        Statement statement;
        ArrayList<String> list = new ArrayList<String>();

        try {

            LocalDate date = LocalDate.now().minusDays(7);

            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT Auftraggeber_Bibliotheksnummer " +
                    "FROM Druckjob WHERE Auftragsdatum = '" + date.toString() + "'");

            while(resultSet.next()) {
                list.add(resultSet.getString(1));
            }

            String[] array = new String[list.size()];
            array = list.toArray(array);

            resultSet.close();
            statement.close();
            connection.disconnectFromDB();

            return array;

        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public int delete() {

        DBConnection connection = new DBConnection();
        Statement statement;

        try {

            statement = connection.connectToDB();
            statement.executeQuery("DELETE from Druckjob WHERE Job_ID = '" + this.Job_ID + "'");

            statement.close();
            connection.disconnectFromDB();
            
			return 0;

			} catch(SQLException e) {
		        e.printStackTrace();
				return 1;
			}
    }

    @Override
    public int update() {
    	return 0;
    }

    /**
     * Setzt den Druckstatus fuer einen Studenten auf TRUE
     * @param snr
     * @return 0 success 1 failure
     */
    public int update(String snr) {

        DBConnection connection = new DBConnection();
        Statement statement;
        
        try {

            statement = connection.connectToDB();
            statement.executeQuery("UPDATE Druckjob " +
                "SET Druckstatus = true WHERE " +
                "Auftraggeber_Bibliotheksnummer = '" + snr + "'");

            this.Druckstatus = true;

            statement.close();
            connection.disconnectFromDB();

            return 0;

        } catch(SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Aktualisiert die Seitenzahlen des aktuellen Druckjobs,
     * je nach den Druckeinstellungen
     * @return 0 success 1 failure
     */
    public int updateSeitenanzahl( ) {

        DBConnection connection = new DBConnection();
        Statement statement;
        
        try {

            statement = connection.connectToDB();
            statement.executeQuery("UPDATE Druckjob " +
                "SET Seitenzahl = " +this.Seitenzahl+ 
                " WHERE Job_ID = "+this.Job_ID);

            statement.close();
            connection.disconnectFromDB();

            return 0;

        } catch(SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Aktualisiert die Preise nach neuberechnen mit den
     * geforderten Einstellungen
     * @param einzelpreis
     * @return 0 success 1 failure
     */
    public int updatePreis(double einzelpreis) {

        DBConnection connection = new DBConnection();
        Statement statement;
        
        try {

            statement = connection.connectToDB();
            statement.executeQuery("UPDATE Druckjob " +
                "SET Preis = " +this.Seitenzahl*einzelpreis+ " WHERE Job_ID =" +
                this.Job_ID);

            this.Druckstatus = true;

            statement.close();
            connection.disconnectFromDB();

            return 0;

        } catch(SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }
    
    @Override 
    public Object getObject() {
    	return this;
    }
    
    @Override
    public void getAll() {
    	
    }
}
