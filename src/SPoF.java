import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class SPoF extends Thread
{
	private static final int PORT = 1338;
	private ServerSocket serverSocket;
	private Socket socket;
	private ArrayList<SPoFHandler> cryptographer = new ArrayList<SPoFHandler>();
	
	private static int sId = 0;
	
	public SPoF()
	{
		setName("SPoF");
		try
		{
			serverSocket = new ServerSocket(PORT);
		}
		catch(IOException e)
		{
			System.out.println(getName()+ ": Could not open server socket.");
			return;
		}
		// Announce the socket creation
		System.out.println(getName()+ ": Socket "+serverSocket+" created.");
		
		start();
	}
	
	public void run()
	{
		System.out.println(getName()+ ": Server has been started.");
		
		while(true)
		{
			try
			{
				socket = serverSocket.accept();
				
				System.out.println(getName()+ ": Cryptographer "+socket+" is connected.");
				
				SPoFHandler handler = new SPoFHandler(socket);
				
				cryptographer.add(handler);
				
				handler.start();
			}
			catch(IOException e)
			{
				System.out.println(getName()+ ": Could not get a client.");
			}
			
		}
	}
	
	private class SPoFHandler extends NetworkThread
	{
		private int id;
		
		public SPoFHandler(Socket socket) throws IOException
		{
			super(socket);
			id = sId++;
			setName("SPoFHandler:"+id);
		}
		
		@Override
		public void runSpecialized() throws IOException 
		{
			while (true) 
			{
				//System.out.println(getName()+ ": SpoFHandler waiting for data");
				
				String dataReceived = readData();
				
				if (DCNet.mode == DCNet.Mode.MODE_DC_NET_FOR_BENCHMARKING)
				{
					//get next in arraylist
					int sendTo = id + 1;
					
					if (sendTo == cryptographer.size())
					{
						sendTo = 0;
					}

					//only send to neighbor
					cryptographer.get(sendTo).send(dataReceived);
				}
				else
				{
					for (Iterator<SPoFHandler> iterator = cryptographer.iterator(); iterator.hasNext();) 
					{
						SPoFHandler cryptographer = iterator.next();
						
						if (cryptographer != this) 
						{
							cryptographer.send(dataReceived);
						}
					}
				}
			}
		}
	}
}

