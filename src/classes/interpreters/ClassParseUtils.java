package classes.interpreters;

import java.util.ArrayList;

import asformat.CodeParser;
import asformat.ParseError;

/**
 * Contains some methods that are required by multiple Helper Classes
 * 
 * @author sander
 * 
 */
public class ClassParseUtils {

	/**
	 * Because a . separates 2 words something like 'flash.display.Sprite' would
	 * be divided into 5 array elements, when those elements need to be assembled
	 * again this method can be used.
	 * 
	 * @param parser The parser which will give litte pieces of what we want
	 * @param elements The element array it which we should store everything
	 * @return
	 */
	public static String getDotList(CodeParser parser, ArrayList<String> elements) {
		String completeList = "";
		boolean word = true;

		while (true) {
			String string = parser.hasNext() ? parser.next() : "";
			if (string.isEmpty())
				ParseError.rageQuit("eof after sequence!", parser);
			if (!word || string.equals("{")) {
				if (!string.equals(".")) {
					parser.stepBack();
					break;
				}
			}
			elements.add(string);
			completeList += string;
			word = !word;
		}
		return completeList;
	}

	/**
	 * Simply adds 1 to the state if its a { or minus 1 if its a } It is
	 * important to keep track of the amount of brackets because it can
	 * determine whether we are in the import phase, variable and function
	 * definition, or inside functions
	 * 
	 * @param parseState
	 *            the state before the process
	 * @param thisString
	 *            the string that is to be checked
	 * @return the new updated state
	 */
	public static int checkState(int parseState, String thisString) {
		if (thisString.equals("{")) {
			parseState++;
		} else if (thisString.equals("}")) {
			parseState--;
		}
		return parseState;
	}
	/**
	 * Gets the next string, adds it to the element array and displays error
	 * message if fail.
	 * 
	 * @param parser
	 *            The parser.
	 * @param elements
	 *            The elements.
	 * @param errorMsg
	 *            The error message.
	 * @return The next word.
	 */
	public static String getOrFail(CodeParser parser,
			ArrayList<String> elements, String errorMsg) {
		if (!parser.hasNext()) {
			ParseError.rageQuit("End Of File After " + errorMsg, parser);
		}
		String out = parser.next();
		elements.add(out);
		return out;
	}
}
