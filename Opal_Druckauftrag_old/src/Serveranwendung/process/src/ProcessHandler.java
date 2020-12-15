import java.lang.ProcessBuilder;
import java.io.*;
import java.io.File;

/**
 * Klasse zur Ausfuehrung und Ueberwachung der Shell-Skripte 
 * Mittels des ProcessBuilders werden die Skripte ausgefuehrt und der Rueckgabewert wird abgefangen
 * @author pauljannasch
 */
public class ProcessHandler {

	/** Skript Vorverarbeitung */
	public static String checkPassword = "/home/druckauftrag/src/pdf_handling/check_password";
	/** Skript Vorverarbeitung */
   	public static String convertGrey = "/home/druckauftrag/src/pdf_handling/convert_grey";
	/** Skript Vorverarbeitung */
   	public static String convertPages = "/home/druckauftrag/src/pdf_handling/convert_pages";

	/** Skript Nachverarbeitung */
	public static String combine = "/home/druckauftrag/src/pdf_handling/final/combine";
	/** Skript Nachverarbeitung */
	public static String generateBarcode = "/home/druckauftrag/src/pdf_handling/final/barcode/generate_barcode";
	/** Skript Nachverarbeitung */
	public static String deckblattErstellen = "/home/druckauftrag/src/pdf_handling/final/deckblatt/generate_deckblatt";

	public static int exitValue;
	
	private static String fileName;
	private static String path;

	/**
	 * Ausfuehrung des gewuenschten Skripts mit den zu uebergebenden Parametern
	 * @param script Das auszufuehrende Shell-Skript
	 * @param parameter Die fuer das Skript bestimmten Parameter als String Array
	 */
	public static void runScript(String script, String[] parameter) {

		/* Anzahl der Argumente fuer den Aufruf bestimmen */
		int arguments = parameter.length;
		ProcessBuilder processBuilder = new ProcessBuilder();

		/* Script mit der richtigen Anzahl an Parametern aufrufen */
		switch(arguments) {
			case 1: processBuilder.command(script, parameter[0]);
					break;

			case 2: processBuilder.command(script, parameter[0], parameter[1]);
					break;

			case 3: processBuilder.command(script, parameter[0], parameter[1], parameter[2]);
					break;

			case 4: processBuilder.command(script, parameter[0], parameter[1], parameter[2], parameter[3]);
			        break;

			case 5: processBuilder.command(script, parameter[0], parameter[1], parameter[2], parameter[3], parameter[4]);
					break;

			case 7: processBuilder.command(script, 	parameter[0], parameter[1], parameter[2], parameter[3], parameter[4], parameter[5], parameter[6]);
					break;

			default: break;
		}
		
		/* Build Process */
		try {

			Process process = processBuilder.start();
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while((line = reader.readLine()) != null) {
				output.append(line + '\n');
			}

			exitValue = process.waitFor();
			if(exitValue == 0) {
				System.out.println("run complete: " + script);
			}
			else {
				System.out.println("run not 0: " + script);
				System.out.println("exit code: " + exitValue + " send error mail...");
			}
		} catch (IOException e){
			e.printStackTrace();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}

}
