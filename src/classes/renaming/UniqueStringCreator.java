package classes.renaming;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileWriter;

/**
 * This class creates unique strings used to name variables
 * @author sander
 *
 */
public class UniqueStringCreator {

	private static HashMap<String, Boolean> usedWordsMap;
	
	private static Map<String, String> renamedDict;
	private static Map<String, String> renamedClassDict;
	private static Map<String, String> renamedPackageDict;
	
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

		renamedDict = new HashMap<String, String>();
		renamedClassDict = new HashMap<String, String>();
		renamedPackageDict = new HashMap<String, String>();
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
	
	public static String getDictUniqueName(String oldname){
		return renamedDict.getOrDefault(oldname.toLowerCase(), null);
	}

	/**Returns a unique name.
	 * 
	 * @param rename the type of name that is wanted
	 * @return the name
	 */
	public static String getUniqueName(RenameType rename, String oldname) {
		char[] first = null;
		char[] norm = null;

		if (rename == RenameType.VARIABLE)
		{
			String indist = renamedDict.getOrDefault(oldname.toLowerCase(), null);
			if (indist != null)
			{
				return indist;
			}
		}
	
		switch (rename) {
			case VARIABLE:
			case LOCALVARIABLE:
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
			return getUniqueName(rename, oldname);
		}
		
		String strOut = new String(out);

		renamedDict.put(new String(oldname), strOut);

		switch (rename) {
			case VARIABLE:
			case LOCALVARIABLE:
				break;
			case CLASSNAME:
				renamedClassDict.put(oldname, strOut);
				break;
			case PACKAGENAME:
				renamedPackageDict.put(oldname, strOut);
				break;
			default:
				break;
		}

		return strOut;
	}

	public static void resetDict(){
		//renamedDict.clear();
	}

	public static void writeDistToFile(){
		try {
			FileWriter writer = new FileWriter("output.txt"); 

			for(String str: renamedDict.keySet()) {
			  writer.write(str + "\t" + renamedDict.get(str) + System.lineSeparator());
			}
			writer.close();		
				
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
