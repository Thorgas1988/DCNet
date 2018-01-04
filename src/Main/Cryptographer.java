package Main;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import Main.DCNet.Mode;

public class Cryptographer
{
	private static final int INTERVAL_5S = 5000;
	
	static int cId = 0;
	int id;
	
	private String lastEnteredMessage = "";
	
	private class Handler extends NetworkThread
	{
		private boolean isCommandLine = false;
		private KeyStorage keyStorage;
		private int maxCryptographers;
		
		public Handler(KeyStorage keyStorage, int maxCryptographers, Socket socket, boolean isCommandLine) throws IOException 
		{
			super(socket);
			this.isCommandLine = isCommandLine;
			this.keyStorage = keyStorage;
			this.maxCryptographers = maxCryptographers;
			
			id = cId++;
			
			if (DCNet.mode == Mode.TASK_2 && isCommandLine) {
				Timer timer = new Timer();
				String initalMessage = new String();
				for (int i = 0; i < keyStorage.keyLength; i++) {
					initalMessage = initalMessage.concat("30");
				}
				lastEnteredMessage = initalMessage;
				
				System.out.println(lastEnteredMessage);
				timer.scheduleAtFixedRate(new TimerTask() {
					  @Override
					  public void run() {
						  try {
							send(encryptDecrypt(hexStringToByteArray(lastEnteredMessage)));
						} catch (IOException e) {
							e.printStackTrace();
						}
					  }
					},INTERVAL_5S, INTERVAL_5S);
			}
		}
		
		@Override
		public void runSpecialized() throws IOException 
		{
			setName("Cryptographer "+id);

			System.out.println(getName()+ ": "+socket+" is connected.");

			while (true) 
			{
				byte[] decryptedMessage = null;
				
				if (isCommandLine && (DCNet.mode == Mode.TASK_1 || DCNet.mode == Mode.TASK_4)) 
				{
					Scanner in = new Scanner(System.in);
					System.out.println(getName()+ ": Gimme input, allowed length:"+(keyStorage.keyLength)+":");

					String s = in.nextLine();

					byte[] encryptedMessage = encryptDecrypt(s.getBytes());
					
					System.out.println(getName()+ ": Original Message:"+s);
					System.out.println(getName()+ ": Encrypted Message to send:"+bytesToHex(encryptedMessage));
					
					send(encryptedMessage);
					
					decryptedMessage = encryptedMessage;
					
					for (int i = 0; i < maxCryptographers-1; i++) 
					{
						String tmpdecryptedMessage = readData();
						System.out.println(getName()+ ": Raw Data read: Size:"+tmpdecryptedMessage.length() / 2+ ", Message:"+tmpdecryptedMessage);
						decryptedMessage = xor(decryptedMessage, hexStringToByteArray(tmpdecryptedMessage));
						System.out.println(getName()+ ": Data read: Size:"+decryptedMessage.length+ ", Message:"+bytesToHex(decryptedMessage));
					}
					System.out.println(getName()+ ": Finished Data read: Size:"+decryptedMessage.length+ ", Decrypted Message:"+new String(decryptedMessage));
				}
				else
				{
					for (int i = 0; i < maxCryptographers-1; i++) 
					{
						String tmpdecryptedMessage = readData();
						System.out.println(getName()+ ": Raw Data read: Size:"+tmpdecryptedMessage.length() / 2+ ", Message:"+tmpdecryptedMessage);
						
						decryptedMessage = hexStringToByteArray(tmpdecryptedMessage);
						
						if (i == 0 && !isCommandLine) 
						{
							byte[] emptyMessage = new byte[keyStorage.keyLength];
							byte[] encryptedMessage = encryptDecrypt(emptyMessage);
							decryptedMessage = encryptedMessage;
							
							System.out.println(getName()+ ": Send: Message:"+bytesToHex(decryptedMessage));
							send(encryptedMessage);
						}
						
						decryptedMessage = xor(decryptedMessage, hexStringToByteArray(tmpdecryptedMessage));		
						System.out.println(getName()+ ": Data read: Size:"+decryptedMessage.length+ ", Message:"+bytesToHex(decryptedMessage));
					}
					System.out.println(getName()+ ": Finished Data read: Size:"+decryptedMessage.length+ ", Decrypted Message:"+new String(decryptedMessage));
				}
			}
		}
		
		public byte[] encryptDecrypt(byte[] data)
		{
			List<String> otherKeys = keyStorage.getOtherKeys(id);
			
			for (Iterator<String> iterator = otherKeys.iterator(); iterator.hasNext();) 
			{
				String otherKey = iterator.next();
				
				byte[] otherKeyAsBytes = hexStringToByteArray(otherKey);
				
				data = xor(data, otherKeyAsBytes);
				
			}
			return data;
		}
		
		public byte[] xor(byte[] input1, byte[] input2)
		{
			byte[] result = new byte[input1.length];
			
			int i = 0;
			for (byte b : input1)
				result[i] = (byte) (b ^ input2[i++]);
			return result;
		}
			
	}
	
	public Cryptographer(KeyStorage keyStorage, int maxCryptographers, boolean commandLine)
	{
		try
		{
			Socket socket = new Socket("127.0.0.1", 1338);
			socket.getInputStream();
			new Handler(keyStorage, maxCryptographers, socket, commandLine).start();
		}
		catch(IOException e)
		{
			System.out.println("Could not get input stream");
		}
	}
}

