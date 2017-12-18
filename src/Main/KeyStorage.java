package Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KeyStorage 
{
	List<String> keys = new ArrayList<String>();
	
	int i = 0;
	
	public KeyStorage(int neededKeys, int size) 
	{
		for (int i = 0; i < neededKeys; i++) 
		{
			keys.add(createKey(size));
		}
	}
	
	public String createKey(int size)
	{
		byte[] data = new byte[size];
		
		Random r = new Random();
		r.nextBytes(data);
		
		return NetworkThread.bytesToHex(data);
	}
	
	public String getKey()
	{
		String key = keys.get(i);
		i++;
		return key; 
	}
	
	public List<String> getOtherKeys(String key)
	{
		List<String> otherKeys = new ArrayList<String>();
		otherKeys.addAll(keys);
		otherKeys.remove(key);
		return otherKeys;
	}
}
