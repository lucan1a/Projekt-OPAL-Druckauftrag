import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

/** 
 * Klasse Druckauftrag nach DBInterface, 
 * Wiedergabe und Verwendung von Opal-Druckauftrag-DB.Druckauftrag, 
 * Nichtbeschriebene Funktionen folgen dem Standardablauf beschrieben in DBInterface
 *
 * @author pauljannasch
 */

public class Druckauftrag implements DBInterface {

    /* Membervariablen geben die Druckauftragsattribute wieder */
    public int      Auftrags_ID;
    public String   Auftraggeber_Bibliotheksnummer;
    public boolean  Bearbeitungsstatus;
    public int      Seitenzahl_roh;
    public int      Seitenzahl_bearbeitet;
    public int      Druckeinstellung;
    public String   Dateiname_original;
    public int      FileSize;

    /**
     * Defaultkonstruktor
     */
    Druckauftrag() {
        this.Auftrags_ID = 0;
        this.Auftraggeber_Bibliotheksnummer = "";
        this.Bearbeitungsstatus = false;
        this.Seitenzahl_roh = 0;
        this.Seitenzahl_bearbeitet = 0;
        this.Druckeinstellung = 0;
        this.Dateiname_original = "";
        this.FileSize = 0;
    }

    /**
     * Konstruktor fuer neues Objekt mit gesetzten Daten
     * @param Auftrags_ID
     * @param Auftraggeber_Bibliotheksnummer
     * @param Bearbeitungsstatus
     * @param Seitenzahl_roh
     * @param Seitenzahl_bearbeitet
     * @param Druckeinstellung
     * @param Dateiname_original
     * @param FileSize
     */
    Druckauftrag(int Auftrags_ID, String Auftraggeber_Bibliotheksnummer,
        boolean Bearbeitungsstatus, int Seitenzahl_roh, int Seitenzahl_bearbeitet,
        int Druckeinstellung, String Dateiname_original, int FileSize) {

        this.Auftrags_ID = Auftrags_ID;
        this.Auftraggeber_Bibliotheksnummer = Auftraggeber_Bibliotheksnummer;
        this.Bearbeitungsstatus = Bearbeitungsstatus;
        this.Seitenzahl_roh = Seitenzahl_roh;
        this.Seitenzahl_bearbeitet = Seitenzahl_bearbeitet;
        this.Druckeinstellung = Druckeinstellung;
        this.Dateiname_original = Dateiname_original;
        this.FileSize = FileSize;
    }

    /**
     * toString() als insert Methode
     * gibt alle Objektattribute als String fuer die Verarbeitung in der Datenabnk wieder
     * @return string
     */
    @Override
    public String toString() {
    	return new String("'" +
			this.Auftraggeber_Bibliotheksnummer + "', " +
			this.Bearbeitungsstatus + ", " +
		    this.Seitenzahl_roh + ", " +
		    this.Seitenzahl_bearbeitet + ", " +
		    this.Druckeinstellung + ", '" +
		    this.Dateiname_original + "', " +
		    this.FileSize);
    }

    /**
     * fuegt einen neuen Druckauftrag in die entsprechende Tabelle ein
     * @return 0 success 1 failure
     */
    @Override
    public int insert() {

        DBConnection connection = new DBConnection();
        Statement statement;

        try {

            statement = connection.connectToDB();
            statement.executeQuery("INSERT INTO Druckauftrag " + 
				"VALUES(NULL, " + this.toString() + ")");

            System.out.println("inserted: " + this.toString());

            statement.close();
            connection.disconnectFromDB();
            
			return 0;

			} catch(SQLException e) {
		        e.printStackTrace();
				return 1;
			}
    }

    /**
     * setzt die Objekt ID, welche von der Datenbank generiert wurde
     */
    public void setID() {

        DBConnection connection = new DBConnection();
        Statement statement;
        
        try {

            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT Auftrags_ID FROM Druckauftrag WHERE " + 
            "Auftraggeber_Bibliotheksnummer = '" + this.Auftraggeber_Bibliotheksnummer + "' AND " + 
            "Dateiname_original = '" + this.Dateiname_original + "'");

            if(resultSet.next()) {
                this.Auftrags_ID = Integer.parseInt(resultSet.getString(1));
            }
            
            System.out.println("neue AID: " + this.Auftrags_ID);

            resultSet.close();
            statement.close();
            connection.disconnectFromDB();

        } catch(SQLException e) {
		    e.printStackTrace();
		}
    }

    /**
     * Gibt die Anzahl Dateien eines Studenten wieder
     * Parameter ist die Bibliotheksnummer
     * @param snr
     * @return int Dateianzahl
     */
    public int DateiCount(String snr) {

        DBConnection connection = new DBConnection();
        Statement statement;
        
        try {

            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(Dateiname_original) " +
            "FROM Druckauftrag WHERE " +
            "Auftraggeber_Bibliotheksnummer = '" + snr + "'");

            resultSet.next();
            int count = Integer.parseInt(resultSet.getString(1));

            resultSet.close();
            statement.close();
            connection.disconnectFromDB();

            return count;

        } catch(SQLException e) {
            e.printStackTrace();
        }
        return 0;

    }

    /**
     * Gibt die Summe der Seitenzahlen aller Dateien eines Studenten zurueck
     * Parameter ist die Bibliotheksnummer
     * @param snr
     * @return int Seitenzahl
     */
    public int SeitenzahlSumme(String snr) {

        DBConnection connection = new DBConnection();
        Statement statement;
        
        try {

            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT " +
            "SUM(Seitenzahl_bearbeitet) FROM Druckauftrag WHERE " +
            "Auftraggeber_Bibliotheksnummer = '" + snr + "'");

            resultSet.next();
            int sum = Integer.parseInt(resultSet.getString(1));

            resultSet.close();
            statement.close();
            connection.disconnectFromDB();

            return sum;

        } catch(SQLException e) {
            e.printStackTrace(); 
        }
        return 0;
    }

    /**
     * Gibt die Seitenzahl eines Auftrags eines Studenten zurueck
     * Parameter ist die AuftragsID
     * @param auftrags_id
     * @return int Seitenzahl
     */
    public int getSeitenzahl(int auftrags_id) {

        DBConnection connection = new DBConnection();
        Statement statement;
        
        try {
            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT " +
            "Seitenzahl_bearbeitet FROM Druckauftrag WHERE " +
            "Auftrags_ID =" + auftrags_id );

            resultSet.next();
            int anzahl = Integer.parseInt(resultSet.getString(1));

            resultSet.close();
            statement.close();
            connection.disconnectFromDB();

            return anzahl;

        } catch(SQLException e) {
            e.printStackTrace(); 
        }
        return 0;
    }

    /**
     * Gibt den Dateinamen eines Auftrags zurueck
     * Parameter ist die AuftragsID
     * @param auftrags_ID
     * @return String Dateiname
     */
    public String getDateiname(int auftrags_ID) {

        DBConnection connection = new DBConnection();
        Statement statement;
          
        try {
            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT " +
            "Dateiname_original FROM Druckauftrag WHERE " +
            "Auftrags_ID = "+ auftrags_ID);

            resultSet.next();
            String dateiname = resultSet.getString(1);
            

            resultSet.close();
            statement.close();
            connection.disconnectFromDB();

            return dateiname;

        } catch(SQLException e) {
            e.printStackTrace(); 
        }
        return null;
    }

     /**
     * Gibt die Anzahl einer bestimmten Datei von einem Studenten zurueck.
     * @return int count
     */
    public int countDateinameBenutzer() {

        DBConnection connection = new DBConnection();
        Statement statement;
        
        try {
            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT " +
            "COUNT (Auftrags_ID) FROM Druckauftrag WHERE " +
            "Auftraggeber_Bibliotheksnummer = '" + this.Auftraggeber_Bibliotheksnummer  + 
            "' AND Dateiname_original = '" + this.Dateiname_original + "'");

            resultSet.next();
            int count = Integer.parseInt(resultSet.getString(1));

            resultSet.close();
            statement.close();
            connection.disconnectFromDB();

            return count;

        } catch(SQLException e) {
            e.printStackTrace(); 
        }
        return 0;
    }

    /**
     * Delete loescht in dieser Klasse die gesamte Tabelle Druckauftrag
     * @return 0 success 1 failure
     */
    @Override
    public int delete() {

        DBConnection connection = new DBConnection();
        Statement statement;

        try {

            statement = connection.connectToDB();
            statement.executeQuery("DELETE from Druckauftrag");

            statement.close();
            connection.disconnectFromDB();
            
			return 0;

			} catch(SQLException e) {
		        e.printStackTrace();
				return 1;
			}
    }

    /**
     * DeleteElement loescht nur den Druckauftrag, welchen das
     * Objekt referenziert
     * @return 0 success 1 failure
     */
    public int deleteElement() {

        DBConnection connection = new DBConnection();
        Statement statement;

        try {

            statement = connection.connectToDB();
            statement.executeUpdate("DELETE from Druckauftrag where Auftrags_ID =" +this.Auftrags_ID);

            statement.close();
            connection.disconnectFromDB();
            
			return 0;

			} catch(SQLException e) {
		        e.printStackTrace();
				return 1;
			}
    }

    /**
     * DeleteElement loescht den Auftrag, welcher von einer bestimmten
     * AuftragsID referenziert wird
     * @param auftrag
     * @return 0 success 1 failure
     */
    public int deleteElement(int auftrag) {

        DBConnection connection = new DBConnection();
        Statement statement;

        try {

            statement = connection.connectToDB();
            statement.executeUpdate("DELETE from Druckauftrag where Auftrags_ID =" +auftrag);

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
    
    @Override 
    public Object getObject() {
    	return this;
    }
    
    @Override
    public void getAll() {
    
    }

    /**
     * Gibt alle Bibliotheksnummern aus Druckauftrag zurueck
     * @return String[] der Bibliotheksnummern
     */
    public String[] getAuftragBibNr() {

        DBConnection connection = new DBConnection();
        Statement statement;
        ArrayList<String> list = new ArrayList<String>();
        
        try {

            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT  + " +
                    "Auftraggeber_Bibliotheksnummer FROM Druckauftrag " +
                    "GROUP BY Auftraggeber_Bibliotheksnummer");
            
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
     * Gibt die IDs aller Auftraege eines Studenten zurueck
     * @param sNr
     * @return int[] der IDs
     */
    public int [] getAuftragID(String sNr) {

        DBConnection connection = new DBConnection();
        Statement statement;
        ArrayList<Integer> list = new ArrayList<Integer>();
        

        try {

            statement = connection.connectToDB();
            ResultSet resultSet = statement.executeQuery("SELECT Auftrags_ID FROM Druckauftrag "+ 
            "WHERE Auftraggeber_Bibliotheksnummer ='" +sNr+ "'");
            
            while(resultSet.next()) {
                list.add(Integer.parseInt(resultSet.getString(1)));
            }

            int [] array = new int [list.size()];
            array = list.stream().mapToInt(Integer::intValue).toArray();
            
            resultSet.close();
            statement.close();
            connection.disconnectFromDB();
            System.out.println(Arrays.toString(array));
            return array;

        } catch(SQLException e) {
		    e.printStackTrace();
            return null;
		}
    }

}
