import java.lang.*;
import java.sql.*;
import com.informix.udr.*;

public class Zeropad {
	public static int DEFAULT_PADDING = 5;
	
/*	public static void main (String[] args) {
		String testStr = "cb3a022";
		System.out.println("Before: " + testStr + " , After: " + zero_pad(testStr, 5));

		} */

	public static String zero_pad(String before) {
		return zero_pad(before, DEFAULT_PADDING); 
//		return before;
	}
	
	
	public static String zero_pad(String before, int size) {
		int i;
		String after = "";
		String subString = "";
		Character C;
		String lastCharType = null; //"String" or "Integer"
		String thisCharType = null; // ditto
		for (i = 0; i < before.length() ; i++) {

			if (Character.isDigit(before.charAt(i)))
				thisCharType = "Integer";
			else
				thisCharType = "String"; 


			if ((thisCharType == "Integer") && ( lastCharType == "String")) {
				after = after + subString;
				subString = "" + before.charAt(i);
			} else if ((thisCharType == "String") && ( lastCharType == "Integer")) {
				after = after + pad_int(subString, size);
				subString = "" + before.charAt(i);
			} else {
				subString = subString + before.charAt(i);
			}

			if (i+1 == before.length()) {
				if (thisCharType == "String") 
					after = after + subString;
				else if (thisCharType == "Integer")
					after = after + pad_int(subString, size);
			}
			

			
//			System.out.println("subString: " + subString + ", after: " + after);
//			System.out.println("this: " + before.charAt(i) + ", thisType: " + thisCharType + ", lastType: " + lastCharType);
			
		    lastCharType = thisCharType;
			thisCharType = null;
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
