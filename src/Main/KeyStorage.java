package Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.ls.LSInput;

import Main.DCNet.Mode;

public class KeyStorage 
{
	Map<Integer, List<String>> keys = new LinkedHashMap<Integer, List<String>>();
	
	int keyLength = 0;
	
	public KeyStorage(int amountOfCryptographers, int size) 
	{
		//default setup
		int keysNeeded = amountOfCryptographers * (amountOfCryptographers - 1) / 2;
		
		if (DCNet.mode == Mode.TASK_5) 
		{
			keysNeeded = amountOfCryptographers;
		} 
		
		int valueNodeA = 0;
		int valueNodeB = 1;
		
		Map<String, String> keysRaw = new LinkedHashMap<String, String>();
		
		//generate keys
		for (int i = 0; i < keysNeeded; i++) 
		{
			String key = createKey(size);
			
			keysRaw.put(Integer.toString(valueNodeA).concat("/").concat(Integer.toString(valueNodeB)), key);
			
			if (DCNet.mode == Mode.TASK_5)
			{
				valueNodeB++;
				valueNodeA++; 
				
				if (valueNodeB == amountOfCryptographers) 
				{
					valueNodeB = 0; 
				}
			}
			else
			{
				valueNodeB++;
				
				if (valueNodeB == amountOfCryptographers) 
				{
					valueNodeA++; 
					valueNodeB = valueNodeA + 1; 
				}
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
		Collections.swap(keys.get(0), 0, 1);
		
		keyLength = size;
		
		System.out.println("KeyStorage:"+keys.toString());
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
