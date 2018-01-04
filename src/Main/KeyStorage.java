package Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class KeyStorage 
{
	HashMap<Integer, List<String>> keys = new HashMap<Integer, List<String>>();
	
	int keyLength = 0;
	
	public KeyStorage(int amountOfCryptographers, int size) 
	{
		int keysNeeded = amountOfCryptographers * (amountOfCryptographers - 1) / 2;
		
		int valueNodeA = 0;
		int valueNodeB = 1;
		
		HashMap<String, String> keysRaw = new HashMap<String, String>();
		
		//generate keys
		for (int i = 0; i < keysNeeded; i++) 
		{
			String key = createKey(size);
			
			keysRaw.put(Integer.toString(valueNodeA).concat("/").concat(Integer.toString(valueNodeB)), key);
			
			valueNodeB++;
			
			if (valueNodeB == amountOfCryptographers) 
			{
				valueNodeA++; 
				valueNodeB = valueNodeA + 1; 
			}
		}
		
		//assign keys to nodes
		for (Iterator<String> iterator = keysRaw.keySet().iterator(); iterator.hasNext();) {
			String identifier = iterator.next();
			String key = keysRaw.get(identifier);
			
			String[] identifierSplitted = identifier.split("/");
			
			int identifierForA = Integer.parseInt(identifierSplitted[0]);
			int identifierForB = Integer.parseInt(identifierSplitted[1]);
			
			List<String> keySetForA = keys.get(identifierForA);
			List<String> keySetForB = keys.get(identifierForB);
			
			if (keySetForA == null) {
				keySetForA = new ArrayList<String>();
				keys.put(identifierForA, keySetForA);
			}
			
			if (keySetForB == null) {
				keySetForB = new ArrayList<String>();
				keys.put(identifierForB, keySetForB);
			}
			
			keySetForA.add(key);
			keySetForB.add(key);
		}
		
		keyLength = size;
		
		System.out.println(keys.toString());
	}
	
	public String createKey(int size)
	{
		byte[] data = new byte[size];
		
		Random r = new Random();
		r.nextBytes(data);
		
		return NetworkThread.bytesToHex(data);
	}
	
	public List<String> getOtherKeys(int cryptographer)
	{
		return keys.get(cryptographer);
	}
}
