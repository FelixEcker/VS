package de.feckert.vs;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import de.hexagonsoftware.pg.util.PGProperties;

public class Main {
	// Config Holder
	public static PGProperties CONFIG;
	private static boolean pingEnable = true;
	
	// Server vars
	private static int port = 8823;
	private static ServerSocket sock;
	
	public static ConcurrentHashMap<Short, ServerEntry> SERVER_REGISTRY = new ConcurrentHashMap<>();
	
	
	public static void main(String[] args) {
		System.out.println("VermittlungsServer 1.0 //\n");
		loadConfig();
		
		System.out.println("Port: "+port);
		System.out.println("Ping-Aktiv: "+ (pingEnable ? "ja" : "nein")+"\n");
		
		if (pingEnable)
			new PingRoutine().start();
		
		try {
			sock = new ServerSocket(port);
			System.out.printf("%s  Socket geöffnet.\n", getDateTimeString());
			System.out.printf("%s  VS ist bereit!\n", getDateTimeString());
            
			while (true) {
				Socket client = sock.accept();
				DataInputStream reader = new DataInputStream(new BufferedInputStream(client.getInputStream()));
	            
                System.out.printf("%s  Neu Client Verbindung (%s)\n", getDateTimeString(), ((InetSocketAddress)client.getRemoteSocketAddress()).getHostString());
	            
	            byte[] bytes = new byte[1024];
                reader.read(bytes);
                
                if (bytes[0] == 0x0) { // Register
                	System.out.printf("%s  Klient startet Registrierung (%s)\n",getDateTimeString(),((InetSocketAddress)client.getRemoteSocketAddress()).getHostString());
    				new RegisterRoutine(client).start();
                } else if (bytes[0] == 0x1) { // Get Server
                	System.out.printf("%s  Klient startet Abfrage (%s)\n",getDateTimeString(),((InetSocketAddress)client.getRemoteSocketAddress()).getHostString());
    				new RequestRoutine(client).start();
                } else {
                	client.close();
                }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void loadConfig() {
		System.out.println("Lade Konfiguration...");
		CONFIG = new PGProperties();
		
		// Load Config File
		BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(new File("./settings.properties")));
            CONFIG.load(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Load config into variables
        Main.port 				  = CONFIG.getPropertyAsInt("port");
        Main.pingEnable			  = CONFIG.getPropertyAsBool("ping_enable");
        if (Main.pingEnable) {
	        PingRoutine.MAX_NOREPLIES = CONFIG.getPropertyAsInt("ping.max_noreplies");
	        PingRoutine.TIMEOUT       = CONFIG.getPropertyAsInt("ping.timeout");
	        PingRoutine.SLEEP_LENGTH  = CONFIG.getPropertyAsInt("ping.sleep_length");
	        PingRoutine.MAX_NOREPLIES = CONFIG.getPropertyAsInt("ping.max_noreplies");
        }
	}

	public static String getDateTimeString() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  
		return dtf.format(now);
	}
	
	public static class ErrorCodes {
		public static final byte ERROR_UNKNOWN      = 0x00;
		public static final byte ERROR_ID_TAKEN     = 0x01;
		public static final byte ERROR_INVALID_DATA = 0x02;
		public static final byte ERROR_INVALID_ID   = 0x03;
		public static final byte ERROR_NO_SPACE     = 0x04;
	}
}
