package Main;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import Main.DCNet.Mode;

public class Cryptographer
{
	static int cId = 0;
	int id;
	
	private class Handler extends NetworkThread
	{
		private boolean isCommandLine = false;
		private KeyStorage keyStorage;
		private int maxCryptographers;
		
		public Handler(KeyStorage keyStorage, int maxCryptographers, Socket socket, boolean isCommandLineBitch) throws IOException 
		{
			super(socket);
			this.isCommandLine = isCommandLineBitch;
			this.keyStorage = keyStorage;
			this.maxCryptographers = maxCryptographers;
			
			id = cId++;
		}
		
		@Override
		public void runSpecialized() throws IOException 
		{
			setName("Cryptographer "+id);

			System.out.println(getName()+ ": "+socket+" is connected.");

			while (true) 
			{
				byte[] decryptedMessage = null;
				
				if (isCommandLine && DCNet.mode == Mode.TASK_4) 
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
						String tmpdecryptedMessage = readData();//encryptDecrypt(readData());
						System.out.println(getName()+ ": Raw Data read: Size:"+tmpdecryptedMessage.length() / 2+ ", Message:"+tmpdecryptedMessage);
						
						if (i == 0) 
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
			
			System.out.println(getName()+ ": Data :"+bytesToHex(data));
			
			for (Iterator<String> iterator = otherKeys.iterator(); iterator.hasNext();) 
			{
				String otherKey = iterator.next();
				
				System.out.println(getName()+ ": XOR otherKey :"+otherKey);
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

