/** 
 * DBInterface gibt die zu nutzenden Datenbankfunktionen wieder, 
 * insert():    INSERT des Objekts in die jeweilige Tabelle, 
 * delete():    DELETE des Objekts aus der Tabelle, 
 *              Sonst extra kommentiert, 
 * update():    UPDATE das Objekt in der Tabelle, 
 *
 * getObject(): RETURN das aktuelle Objekt, 
 * getAll():    DISPLAY die Tabelle, 
 * toString():  gibt String fuer Datenbankmethoden zurueck, 
 *
 * Alle Funktionen des Interfaces werden nicht nochamal, 
 * extra in den Files kommentiert, 
 *
 * Ablauf der Datenbankfunktionen: 
 * - mittels DBConnection.class eine Datenbankverbindung aufbauen, 
 * - statement.execute(QUERY) fuehrt die DB-Anweisung aus, 
 * - return 0 : success, return 1 : failure
 *
 * @author pauljannasch
*/

public interface DBInterface {
	/**
	 * Datenbankfunktionen INSERT into Table
	 */
    public int insert();

    /**
	 * Datenbankfunktionen DELETE [FROM] Table
	 */
    public int delete();

    /**
	 * Datenbankfunktionen UPDATE Table
	 */
    public int update();
    
    /**
     * ein bestimmtes Objekt zurueckgeben
     * @return object
     */
    public Object getObject();
    
    /**
     * alle Daten der Tabelle ausgeben
     */
    public void getAll();
}
