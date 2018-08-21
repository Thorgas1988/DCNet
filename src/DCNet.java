import java.util.Scanner;

public class DCNet 
{
	enum Mode
	{
		MODE_BASIC_DC_NET,
		MODE_TIMED_DC_NET,
		MODE_RANDOM_TIMED_CONFLICT_DC_NET,
		MODE_LARGER_DC_NET,
		MODE_DC_NET_FOR_BENCHMARKING
	}
	
	public static Mode mode = Mode.MODE_BASIC_DC_NET;
	
	public static void main(String[] args) 
	{
		Scanner in = new Scanner(System.in);
		
		System.out.print("Enter Mode:");
		int inputMode = in.nextInt();
		
		mode = Mode.values()[inputMode - 1];
		
		int inputAmountofCryptographer = 3;
		if (mode == Mode.MODE_LARGER_DC_NET || mode == Mode.MODE_DC_NET_FOR_BENCHMARKING)
		{
			System.out.print("Enter Amount of Cryptographer:");
			inputAmountofCryptographer = in.nextInt();
		}
		
		int inputKeyLength;
		if (mode == Mode.MODE_RANDOM_TIMED_CONFLICT_DC_NET) {
			inputKeyLength = 14;
		}
		else
		{
			System.out.print("Enter Keylength:");
			inputKeyLength = in.nextInt();
		}
		
		KeyStorage keyStorage = new KeyStorage(inputAmountofCryptographer, inputKeyLength);
		
		new SPoF();
		new Cryptographer(keyStorage, inputAmountofCryptographer, true);
		
		for (int i = 0; i < inputAmountofCryptographer - 1; i++) {
			new Cryptographer(keyStorage, inputAmountofCryptographer, false);
		}
		
	}

}
