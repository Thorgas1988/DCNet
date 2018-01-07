package Main;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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

	private boolean isSending = false;
	
	private static long timeStampStart = 0;

	private class Handler extends NetworkThread
	{
		private boolean isCommandLine = false;
		private KeyStorage keyStorage;
		private int maxCryptographers;

		Runnable continuesReader = new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					System.out.println(getName() + ": Gimme input, allowed length:" + (keyStorage.keyLength) + ":");
					Scanner in = new Scanner(System.in);

					String s = in.nextLine();

					lastEnteredMessage = s;
				}
			}
		};

		public Handler(KeyStorage keyStorage, int maxCryptographers, Socket socket, boolean isCommandLine)
				throws IOException
		{
			super(socket);
			this.isCommandLine = isCommandLine;
			this.keyStorage = keyStorage;
			this.maxCryptographers = maxCryptographers;

			id = cId++;

			if (DCNet.mode == Mode.TASK_2 && isCommandLine)
			{
				new Thread(continuesReader).start();

				start5STimer();
			}

			if (DCNet.mode == Mode.TASK_3)
			{
				startTimerRandomly();
			}
		}

		private void start5STimer()
		{
			String initalMessage = new String();
			for (int i = 0; i < keyStorage.keyLength; i++)
			{
				initalMessage = initalMessage.concat("0");
			}

			lastEnteredMessage = initalMessage;

			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask()
			{
				@Override
				public void run()
				{
					try
					{
						isSending = true;
						send(encryptDecrypt(hexStringToByteArray(asciiToHex(lastEnteredMessage))));
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}, INTERVAL_5S, INTERVAL_5S);
		}

		private void startTimerRandomly()
		{
			String dataToSend = "" + id + System.currentTimeMillis();
			String hexAscii = asciiToHex(dataToSend);

			Random r = new Random();

			int timerValue = 3 + r.nextInt(22);
			System.out.println( getName() + ": Sending:" + dataToSend + ", Data:" + hexAscii + " after " + timerValue + " s");

			Timer timer = new Timer();

			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					try
					{
						lastEnteredMessage = dataToSend;
						isSending = true;
						startTimerRandomly();
						send(encryptDecrypt(hexStringToByteArray(hexAscii)));
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}, timerValue * 1000, timerValue * 1000);
		}

		@Override
		public void runSpecialized() throws IOException
		{
			setName("Cryptographer " + id);

			System.out.println(getName() + ": " + socket + " is connected.");

			while (true)
			{
				byte[] decryptedMessage = null;

				if (isCommandLine
						&& (DCNet.mode == Mode.TASK_1 || DCNet.mode == Mode.TASK_4 || DCNet.mode == Mode.TASK_5))
				{
					Scanner in = new Scanner(System.in);
					System.out.println(getName() + ": Gimme input, allowed length:" + (keyStorage.keyLength) + ":");

					String s = "";
					
					while (s.length() != keyStorage.keyLength)
					{
						s = in.nextLine();
						if (s.length() != keyStorage.keyLength)
						{
							System.out.println(getName() + ": Input not valid, it must be as same length as keys are. Input length:" + s.length() + ", Needed length:"+keyStorage.keyLength);
						}
					}

					timeStampStart = System.currentTimeMillis();
					
					byte[] encryptedMessage = encryptDecrypt(s.getBytes());

					System.out.println(getName() + ": Original Message:" + s);
					System.out.println(getName() + ": Encrypted Message to send:" + bytesToHex(encryptedMessage));

					send(encryptedMessage);

					if (DCNet.mode == Mode.TASK_5)
					{
						String firstRound = readData();
						
						decryptedMessage = encryptDecrypt(hexStringToByteArray(firstRound));

						send(decryptedMessage);
						
						String messageRound = readData();

						System.out.println(getName() + ": Message Data read: Size:" + messageRound.length() / 2
								+ ", Message:" + messageRound);
						
						decryptedMessage = hexStringToByteArray(messageRound);

						send(decryptedMessage);
					}
					else
					{
						decryptedMessage = encryptedMessage;
						
						for (int i = 0; i < maxCryptographers - 1; i++)
						{
							String tmpdecryptedMessage = readData();
							System.out.println(getName() + ": Raw Data read: Size:" + tmpdecryptedMessage.length() / 2
									+ ", Message:" + tmpdecryptedMessage);
							decryptedMessage = xor(decryptedMessage, hexStringToByteArray(tmpdecryptedMessage));
							System.out.println(getName() + ": Data read: Size:" + decryptedMessage.length + ", Message:"
									+ bytesToHex(decryptedMessage));
						}
					}
					System.out.println(getName() + ": Finished Data read: Size:" + decryptedMessage.length
							+ ", Decrypted Message:" + new String(decryptedMessage));
					
					System.out.println(getName() +" finished after:"+ (System.currentTimeMillis() - timeStampStart));
				}
				else
				{
					if (DCNet.mode == Mode.TASK_5)
					{
						for (int i = 0; i < 2; i++)
						{
							String forwardRound = readData();

							System.out.println(getName() + ": Raw Data read: Size:" + forwardRound.length() / 2
									+ ", Message:" + forwardRound);

							byte[] encryptedPayload = encryptDecrypt(hexStringToByteArray(forwardRound));

							System.out.println(
									getName() + ": Send Data: Size:" + bytesToHex(encryptedPayload).length() / 2
											+ ", Message:" + bytesToHex(encryptedPayload));

							send(encryptedPayload);
						}

						String resultRound = readData();

						System.out.println(getName() + ": Message Data read: Size:" + resultRound.length() / 2
								+ ", Message:" + resultRound);

						decryptedMessage = hexStringToByteArray(resultRound);

						send(decryptedMessage);
					}
					else
					{
						for (int i = 0; i < maxCryptographers - 1; i++)
						{
							String tmpdecryptedMessage = readData();
							System.out.println(getName() + ": Raw Data read: Size:" + tmpdecryptedMessage.length() / 2
									+ ", Message:" + tmpdecryptedMessage);

							if (isSending)
							{
								decryptedMessage = encryptDecrypt(lastEnteredMessage.getBytes());
								isSending = false;
							}
							else if (i == 0)
							{
								byte[] emptyMessage = new byte[keyStorage.keyLength];
								byte[] encryptedMessage = encryptDecrypt(emptyMessage);
								decryptedMessage = encryptedMessage;

								System.out.println(getName() + ": Send: Message:" + bytesToHex(decryptedMessage));
								send(encryptedMessage);
							}

							decryptedMessage = xor(decryptedMessage, hexStringToByteArray(tmpdecryptedMessage));
							System.out.println(getName() + ": Data read: Size:" + decryptedMessage.length + ", Message:"
									+ bytesToHex(decryptedMessage));
						}
					}
					System.out.println(getName() + ": Finished Data read: Size:" + decryptedMessage.length
							+ ", Decrypted Message:" + new String(decryptedMessage));
					
					System.out.println(getName() +" finished after:"+ (System.currentTimeMillis() - timeStampStart));
				}
			}
		}

		public byte[] encryptDecrypt(byte[] data)
		{
			List<String> otherKeys = keyStorage.getOtherKeys(id);

			if (DCNet.mode == Mode.TASK_5)
			{
				String otherKey = otherKeys.get(1);

				byte[] otherKeyAsBytes = hexStringToByteArray(otherKey);

				data = xor(data, otherKeyAsBytes);
			}
			else
			{
				for (Iterator<String> iterator = otherKeys.iterator(); iterator.hasNext();)
				{
					String otherKey = iterator.next();

					byte[] otherKeyAsBytes = hexStringToByteArray(otherKey);

					data = xor(data, otherKeyAsBytes);

				}
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

		private String asciiToHex(String asciiValue)
		{
			char[] chars = asciiValue.toCharArray();
			StringBuffer hex = new StringBuffer();
			for (int i = 0; i < chars.length; i++)
			{
				hex.append(Integer.toHexString((int) chars[i]));
			}
			return hex.toString();
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
		catch (IOException e)
		{
			System.out.println("Could not get input stream");
		}
	}
}
