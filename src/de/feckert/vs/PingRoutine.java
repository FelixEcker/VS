package de.feckert.vs;

import java.io.IOException;
import java.net.InetAddress;

/**
 * The PingRoutine is an optional Routine
 * which checks if all registered Servers are still
 * reachable. It can be activated in the VS Config.
 * The Timeout-Time, Maximum amounts of Timeouts and
 * Wait Time before running again are also definable
 * within the config.
 * 
 * @author Felix Eckert
 * */
public class PingRoutine implements Runnable {
	public static int MAX_NOREPLIES;
	public static int TIMEOUT;
	public static int SLEEP_LENGTH;

	public void start() {
		new Thread(this).start();
	}
	
	public void run() {
		while (true) {
			Main.SERVER_REGISTRY.forEach((id,entry) -> {
	            try {
					InetAddress address = InetAddress.getByName(entry.stringAddress);
					if (!address.isReachable(TIMEOUT)) {
						if (entry.timesUnreachable == MAX_NOREPLIES) {
							Main.SERVER_REGISTRY.remove(id);
							System.out.printf("%s  Eintrag %s entfernt (Keine Antwort)",Main.getDateTimeString(), id);
						} else {
							entry.timesUnreachable++;
						}
					}
				} catch (java.net.UnknownHostException e) {
					Main.SERVER_REGISTRY.remove(id);
					System.out.printf("%s  Eintrag %s entfernt (Unbekannte Addresse)",Main.getDateTimeString(), id);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			
			try {
				Thread.sleep(SLEEP_LENGTH);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
