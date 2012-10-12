package classes.renaming;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;

/**
 * This class creates unique strings used to name variables
 * @author sander
 *
 */
public class UniqueStringCreator {

	private static HashMap<String, Boolean> usedWordsMap;
	
	//lol lazy: http://stackoverflow.com/questions/2578233/how-do-i-get-the-set-of-all-letters-in-java-clojure
	private static String getAllLetters(String charsetName)
	{
	    CharsetEncoder ce = Charset.forName(charsetName).newEncoder();
	    StringBuilder result = new StringBuilder();
	    for(char c=0; c<Character.MAX_VALUE; c++)
	    {
	        if(ce.canEncode(c) && Character.isLetter(c))
	        {
	            result.append(c);
	        }
	    }
	    return result.toString();
	}
	
	private static char[] firstCharValidClass;
	private static char[] firstCharValidVariable;
	private static char[] firstCharValidPackage;
	private static char[] validCharClass;
	private static char[] validCharVariable;
	private static char[] validCharPackage;
	
	public static int length = -1;
	
	/**
	 * inits the character arrays.
	 */
	public static void setupChars() {
		String allLetters = getAllLetters("US-ASCII");
		String allNumbers = "0123456789"; //for the sake of consistency

		firstCharValidClass = (allLetters + "_").toCharArray();
		firstCharValidPackage = allLetters.toCharArray();
		firstCharValidVariable = (allLetters + "_$").toCharArray();
		
		validCharClass = (allLetters + allNumbers + "_").toCharArray();
		validCharPackage = validCharClass;
		validCharVariable = (allLetters + allNumbers + "_$").toCharArray();
		usedWordsMap = new HashMap<String, Boolean>();
	}
	
	/**gets the posibilities based on the length of the variable name.
	 * 
	 * @return
	 */
	public static long getExpectedPossibilities() {
		int nameLength = length;
		long out = 0;
		if(nameLength > 0) {
			out = firstCharValidPackage.length;
			nameLength--;
		}
		while(nameLength > 0) {
			out *= validCharPackage.length;
			nameLength--;
		}
		return out;
	}
	
	/**Returns a unique name.
	 * 
	 * @param rename the type of name that is wanted
	 * @return the name
	 */
	public static String getUniqueName(RenameType rename) {
		char[] first = null;
		char[] norm = null;
		
		switch (rename) {
		case VARIABLE:
			first = firstCharValidVariable;
			norm = validCharVariable;
			break;
		case CLASSNAME:
			first = firstCharValidClass;
			norm = validCharClass;
			break;
		case PACKAGENAME:
			first = firstCharValidPackage;
			norm = validCharPackage;
			break;
		default:
			System.out.println("this is impossible! HHAHAHAHAHAHAH");
			break;
		}
		
		char[] out = new char[length];
		out[0] = first[(int) (first.length * Math.random())];
		
		for (int i = 1; i < out.length; i++) {
			out[i] = norm[(int) (norm.length * Math.random())];
		}
		
		if(usedWordsMap.get(new String(out)) == null){
			usedWordsMap.put(new String(out), true);
		}
		else{
			//collision detected TODO log?
			return getUniqueName(rename);
		}
		
		return new String(out);
		
	}
}
