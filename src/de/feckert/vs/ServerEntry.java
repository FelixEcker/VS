package de.feckert.vs;

public class ServerEntry {
	public byte[] address;
	public String stringAddress;
	public short  port;
	public String key;
	public int    timesUnreachable;
	
	public ServerEntry(byte[] address, short port, String key) {
		this.address 		  = address;
		this.port 	 		  = port;
		this.key 	 		  = key;
		this.timesUnreachable = 0;
		this.stringAddress    = "";
		
		for (byte b : address) {
			this.stringAddress += (b & 0xFF) + ".";
		}
		this.stringAddress = stringAddress.substring(0,stringAddress.length()-1);
	}
}
