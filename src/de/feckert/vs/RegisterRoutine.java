package de.feckert.vs;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RegisterRoutine implements Runnable {
	private Socket sock;
	
	public RegisterRoutine(Socket client) {
		this.sock = client;
	}
	
	public void run() {
		try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            DataInputStream reader = new DataInputStream(new BufferedInputStream(sock.getInputStream()));

            // Send ready for Registry (0x01)
            writer.write(0x01);
            writer.flush();
            
            byte[] ip = null;
            short port = 0;
            short id = 0;
            
            while (!sock.isClosed()) {
                byte[] bytes = new byte[1024];
                int length = reader.read(bytes);
                
                // Read the bytes into the right variable
                // NOTE: VALUES MUST BE SENT IN ODER
                // 		 IP, PORT, ID
                try {
	                if (ip == null) {
	                	ip = new byte[4];
	                	if (length < 4) {
	                		throw new IndexOutOfBoundsException();
	                	}
	                	for (int i = 0; i < length; i++) {
	                		ip[i] = bytes[i];
	                	}
	                	writer.write(0x1);
	                	writer.flush();
	                } else if (port == 0) {
	                	if (length < 2) {
	                		throw new IndexOutOfBoundsException();
	                	}
	                	try {
		                	byte[] buffer = new byte[2];
		                	for (int i = 0; i < length; i++) {
		                		buffer[i] = bytes[i];
		                	}
		                	port = ByteBuffer.wrap(buffer).getShort();
		                } catch (BufferUnderflowException | BufferOverflowException e) {
	    	            	System.out.println(Main.getDateTimeString()+"  Server Registrierung fehlgeschlagen! (InvalidData)");
	    	            	writer.print(0x0);
	    	            	writer.flush();
	    	            	writer.print(Main.ErrorCodes.ERROR_INVALID_DATA);
	    	            	writer.flush();
	    	            	break;
	    	            }
	                	writer.write(0x1);
	                	writer.flush();
	                } else if (id == 0) {
	                	if (length < 2) {
	                		throw new IndexOutOfBoundsException();
	                	}
	                	try {
		                	byte[] buffer = new byte[2];
		                	for (int i = 0; i < length; i++) {
		                		buffer[i] = bytes[i];
		                	}
		                	id = ByteBuffer.wrap(buffer).getShort();
	                	} catch (BufferUnderflowException | BufferOverflowException e) {
	    	            	System.out.printf("%s  Server Registrierung fehlgeschlagen! (InvalidID) (%s)\n",Main.getDateTimeString(),((InetSocketAddress)sock.getRemoteSocketAddress()).getHostString());
	    	            	writer.print(0x0);
	    	            	writer.flush();
	    	            	writer.print(Main.ErrorCodes.ERROR_INVALID_ID);
	    	            	writer.flush();
	    	            	break;
	    	            }
	                	writer.write(0x1);
	                	writer.flush();
                	} else { // If all data is given
	                		 // Create a new Server Entry
	                	ServerEntry entry = new ServerEntry(ip, port, "a");
	                	
	                	// Check if there is still space in the registry
	                	if (Main.SERVER_REGISTRY.size() == Short.MAX_VALUE) {
	                		System.out.printf("%s  Server Registrierung fehlgeschlagen! (NoSpace) (%s)\n",Main.getDateTimeString(),((InetSocketAddress)sock.getRemoteSocketAddress()).getHostString());
	                		writer.print(0x0);
	                		writer.flush();
	                		writer.print(Main.ErrorCodes.ERROR_NO_SPACE);
	                		writer.flush();
	                    	break;
	                	}
	                	
	                	// Check if the id is still inside the ID-Space
	                	if (id > Short.MAX_VALUE || id < Short.MIN_VALUE) {
	                		System.out.printf("%s  Server Registrierung fehlgeschlagen! (InvalidID) (%s)\n",Main.getDateTimeString(),((InetSocketAddress)sock.getRemoteSocketAddress()).getHostString());
	                		writer.print(0x0);
	                		writer.flush();
	                		writer.print(Main.ErrorCodes.ERROR_INVALID_ID);
	                		writer.flush();
	                    	break;
	                	}
	                	
	                	// Check if this ID is already registered
	                	if (Main.SERVER_REGISTRY.containsKey(id)) {
	                		System.out.printf("%s  Server Registrierung fehlgeschlagen! (IDTaken) (%s)\n",Main.getDateTimeString(),((InetSocketAddress)sock.getRemoteSocketAddress()).getHostString());
	                		writer.print(0x0);
	                		writer.flush();
	                		writer.print(Main.ErrorCodes.ERROR_ID_TAKEN);
	                		writer.flush();
	                    	break;
	                	}
	                	Main.SERVER_REGISTRY.put(id, entry);
	                	System.out.println(Main.getDateTimeString()+"  NEUER SERVER REGISTRIERT:");
	                	System.out.println("			IP:   "+Arrays.toString(ip));
	                	System.out.println("			PORT: "+port);
	                	System.out.println("			ID:   "+id);
	                	writer.print(0x1);
	            		writer.flush();
	                	break;
	                }
	            } catch (IndexOutOfBoundsException e) {
	            	System.out.printf("%s  Server Registrierung fehlgeschlagen! (InvalidData) (%s)\n",Main.getDateTimeString(),((InetSocketAddress)sock.getRemoteSocketAddress()).getHostString());
	            	writer.print(0x0);
	            	writer.flush();
	            	writer.print(Main.ErrorCodes.ERROR_INVALID_DATA);
	            	writer.flush();
	            	sock.close();
	            	break;
	            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		System.out.printf("%s  Verbindung zu Klient %s getrennt/verloren\n",Main.getDateTimeString(),((InetSocketAddress)sock.getRemoteSocketAddress()).getHostString());
	}

	public void start() {
		Thread t = new Thread(this);
		t.start();
	}
}
