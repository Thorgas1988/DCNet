package Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class NetworkThread extends Thread
{
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public Socket socket;
	private BufferedReader br;
	private PrintWriter pw;
	
	public NetworkThread(Socket socket) throws IOException
	{
		this.socket = socket;
		pw = new PrintWriter(socket.getOutputStream(), true);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public void send(String data) throws IOException
	{
		if (data.length() > 0) 
		{
			System.out.println("ID:"+getName()+":Send to:"+socket.getOutputStream().toString()+": "+data);
			pw.println(data + "$$$EOF$$$");
		}
		else
		{
			System.out.println("ID:"+getName()+": trys to send empty content");
		}
	}
	
	public void send(byte[] data) throws IOException
	{
		send(bytesToHex(data));
	}
	
	public String readData() throws IOException
	{
		 String inputLine;
		 StringBuilder out = new StringBuilder();
		 
		 while ((inputLine = br.readLine()) != null) 
		 {
			 System.out.println("ID:"+getName()+":Read from:"+socket.getInputStream().toString()+": "+inputLine);
			 if (inputLine.endsWith("$$$EOF$$$")) 
			 {
				 out.append(inputLine.substring(0, inputLine.indexOf("$$$EOF$$$")));
				 if (out.length() != 0) 
				 {
					 break;
				 }
				 else
				 {
					 System.out.println("ID:"+getName()+": Suspisous empty content read");
				 }
			 }
			 else
			 {
				 out.append(inputLine);
			 }
         }
		 
		//System.out.println("ID:"+getName()+": Data returned:"+out.toString());
		 
		 return out.toString();
	}
	
	@Override
	public void run() 
	{
		super.run();
		
		try 
		{
			runSpecialized();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		finally 
		{
			if (br != null) 
			{
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (pw != null) 
			{
				pw.close();
			}
			if (socket != null) 
			{
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String bytesToHex(byte[] bytes) 
	{
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static byte[] hexStringToByteArray(String s) 
	{
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public abstract void runSpecialized() throws IOException;
	
}
