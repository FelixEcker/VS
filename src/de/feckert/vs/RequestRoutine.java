package de.feckert.vs;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class RequestRoutine implements Runnable {
	private Socket sock;
	
	public RequestRoutine(Socket client) {
		this.sock = client;
	}
	
	public void run() {
		try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            DataInputStream reader = new DataInputStream(new BufferedInputStream(sock.getInputStream()));

            // Send ready for Request (0x01)
            writer.write(0x01);
            writer.flush();
            
            short id = Short.MAX_VALUE;
            
            while (!sock.isClosed()) {
                byte[] bytes = new byte[1024];
                reader.read(bytes);
                
                if (id == Short.MAX_VALUE) {
                	byte[] buffer = new byte[2];
                	for (int i = 0; i < 2; i++) {
                		buffer[i] = bytes[i];
                	}
                	id = ByteBuffer.wrap(buffer).getShort();
                }
                
                if (!Main.SERVER_REGISTRY.containsKey(id)) {
                	System.out.printf("%s  Klient fragte nach nicht registrieten server (ID "+id+") (%s)\n",Main.getDateTimeString(),((InetSocketAddress)sock.getRemoteSocketAddress()).getHostString());
                	writer.print(new byte[] {0x0,0x0,0x0,0x0});
                    writer.flush();
                    writer.print((short) 0);
                    writer.flush();
                   
                    writer.print(0x0);
                    writer.flush();
                    writer.print(Main.ErrorCodes.ERROR_INVALID_ID);
                    writer.flush();
                    break;
                }
                
                ServerEntry entry = Main.SERVER_REGISTRY.get(id);
                writer.print(entry.address);
                writer.flush();
                writer.print(entry.port);
                writer.flush();
                writer.print(0x1);
                writer.flush();
                System.out.printf("%s  Abfrage von Klient %s abgeschlossen!\n",Main.getDateTimeString(),((InetSocketAddress)sock.getRemoteSocketAddress()).getHostString());
                break;
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
