import java.lang.*;
import java.sql.*;
import com.informix.udr.*;

public class Zeropad {
	public static int DEFAULT_PADDING = 10;
	
/*	public static void main (String[] args) {
		String testStr = "5cb3a022";
		System.out.println("Before: " + testStr + " , After: " + zero_pad(testStr));

		} */

	public static String zero_pad(String before) {
		return zero_pad(before, DEFAULT_PADDING); 
//		return before;
	}
	
	
	public static String zero_pad(String before, int size) {
		int i;
		before = before.toLowerCase();
		String after = "";
		String subString = "";
		Character C;
		Boolean lastIsString = new Boolean(true); //"String" or "Integer"
		Boolean thisIsString = null; // ditto
		for (i = 0; i < before.length() ; i++) {

			if (Character.isDigit(before.charAt(i)))
				thisIsString = new Boolean(false);
			else
				thisIsString = new Boolean (true); 


			if ((thisIsString.booleanValue() == false) && ( lastIsString.booleanValue() == true)) {
				after = after + subString;
				subString = "" + before.charAt(i);
			} else if ((thisIsString.booleanValue() == true) && ( lastIsString.booleanValue() == false)) {
				after = after + pad_int(subString, size);
				subString = "" + before.charAt(i);
			} else {
				subString = subString + before.charAt(i);
			}

			if (i+1 == before.length()) {
				if (thisIsString.booleanValue() == true) 
					after = after + subString;
				else if (thisIsString.booleanValue() == false)
					after = after + pad_int(subString, size);
			}
			

			
//			System.out.println("subString: " + subString + ", after: " + after);
//			System.out.println("this: " + before.charAt(i) + ", thisType: " + thisCharType + ", lastType: " + lastCharType);
			
		    lastIsString = thisIsString;
			thisIsString = null;
//			System.out.println("\n");
		}

		
		return after;

		
	}
	

	public static String pad_int(String before, int size) {
		while (before.length() < size) 
			before = "0" + before;
			
		return before;
	}


}
