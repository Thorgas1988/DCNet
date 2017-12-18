package Main;

public class DCNet 
{
	public static void main(String[] args) 
	{
		KeyStorage keyStorage = new KeyStorage(3, 6);
		
		new SPoF();
		new Cryptographer(keyStorage, 3, true);
		new Cryptographer(keyStorage, 3, false);
		new Cryptographer(keyStorage, 3, false);
	}

}
