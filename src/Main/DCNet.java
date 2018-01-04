package Main;

import java.util.Scanner;

public class DCNet 
{
	enum Mode
	{
		TASK_1,
		TASK_2,
		TASK_3,
		TASK_4,
		TASK_5
	}
	
	public static Mode mode = Mode.TASK_1;
	
	public static void main(String[] args) 
	{
		Scanner in = new Scanner(System.in);
		
		System.out.print("Enter Task:");
		int inputTask = in.nextInt();
		
		mode = Mode.values()[inputTask - 1];
		
		int inputAmountofCryptographer = 3;
		if (mode == Mode.TASK_4) {
			System.out.print("Enter Amount of Cryptographer:");
			inputAmountofCryptographer = in.nextInt();
		}
		
		System.out.print("Enter Keylength:");
		int inputKeyLength = in.nextInt();
		
		KeyStorage keyStorage = new KeyStorage(inputAmountofCryptographer, inputKeyLength);
		
		new SPoF();
		new Cryptographer(keyStorage, inputAmountofCryptographer, true);
		
		for (int i = 0; i < inputAmountofCryptographer - 1; i++) {
			new Cryptographer(keyStorage, inputAmountofCryptographer, false);
		}
		
	}

}
