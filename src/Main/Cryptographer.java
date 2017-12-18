package Main;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Cryptographer
{
	static int cId = 0;
	
	private class Handler extends NetworkThread
	{
		private boolean isCommandLineBitch = false;
		private KeyStorage keyStorage;
		private String key;
		private int maxCryptographers;
		
		public Handler(KeyStorage keyStorage, int maxCryptographers, Socket socket, boolean isCommandLineBitch) throws IOException 
		{
			super(socket);
			this.isCommandLineBitch = isCommandLineBitch;
			this.keyStorage = keyStorage;
			this.key = keyStorage.getKey();
			this.maxCryptographers = maxCryptographers;
		}
		
		@Override
		public void runSpecialized() throws IOException 
		{
			setName("Cryptographer "+cId++);

			System.out.println(getName()+ ": "+socket+" is connected.");

			while (true) 
			{
				byte[] decryptedMessage = null;
				
				if (isCommandLineBitch) 
				{
					Scanner in = new Scanner(System.in);
					System.out.println(getName()+ ": Gimme input:");

					String s = in.nextLine();

					byte[] encryptedMessage = encryptDecrypt(s.getBytes());
					
					System.out.println(getName()+ ": Original Message:"+s);
					System.out.println(getName()+ ": Encrypted Message to send:"+bytesToHex(encryptedMessage));
					
					send(encryptedMessage);
					
					decryptedMessage = encryptedMessage;
					
					for (int i = 0; i < maxCryptographers-1; i++) 
					{
						byte[] tmpdecryptedMessage = encryptDecrypt(readData());
						decryptedMessage = xor(decryptedMessage, tmpdecryptedMessage);
						
						System.out.println(getName()+ ": Data read: Size:"+decryptedMessage.length+ ", Message:"+bytesToHex(decryptedMessage));
					}
					System.out.println(getName()+ ": Finished Data read: Size:"+decryptedMessage.length+ ", Decrypted Message:"+new String(decryptedMessage));
				}
				else
				{
					for (int i = 0; i < maxCryptographers-1; i++) 
					{
						byte[] tmpdecryptedMessage = encryptDecrypt(readData());
						
						if (i == 0) 
						{
							byte[] emptyMessage = new byte[key.length()/2];
							byte[] encryptedMessage = encryptDecrypt(emptyMessage);
							decryptedMessage = encryptedMessage;
							
							send(encryptedMessage);
						}
						
						decryptedMessage = xor(decryptedMessage, tmpdecryptedMessage);
						
						System.out.println(getName()+ ": Data read: Size:"+decryptedMessage.length+ ", Message:"+bytesToHex(decryptedMessage));
					}
					System.out.println(getName()+ ": Finished Data read: Size:"+decryptedMessage.length+ ", Decrypted Message:"+new String(decryptedMessage));
				}
			}
		}
		
		public byte[] encryptDecrypt(byte[] initialEncryptedData)
		{
			List<String> otherKeys = keyStorage.getOtherKeys(key);
			
			for (Iterator<String> iterator = otherKeys.iterator(); iterator.hasNext();) 
			{
				String otherKey = iterator.next();
				
				byte[] otherKeyAsBytes = hexStringToByteArray(otherKey);
				
				initialEncryptedData = xor(initialEncryptedData, otherKeyAsBytes);
				
			}
			return initialEncryptedData;
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
	
	
	public Cryptographer(KeyStorage keyStorage, int maxCryptographers, boolean commandLineBitch)
	{
		try
		{
			Socket socket = new Socket("127.0.0.1", 1338);
			socket.getInputStream();
			new Handler(keyStorage, maxCryptographers, socket, commandLineBitch).start();
		}
		catch(IOException e)
		{
			System.out.println("Could not get input stream");
		}
	}
}

