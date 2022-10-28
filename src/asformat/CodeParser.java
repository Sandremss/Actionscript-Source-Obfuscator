package asformat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * This class takes an .AS file, reads it, and chops it up in little pieces for
 * the rest of the application to read. It will ignore comments, leave strings
 * intact and count newlines and whitespaces.
 * 
 * @author sander
 * 
 */
public class CodeParser {
	private static final int FIRST_ITERATION = 0;
	private static final int STRINGMODE = 1;
	private static final int NORMAL_PROCESS = 2;
	private static final int COMMENTBLOCK = 3;
	private static final int COMMENTLINE = 4;
	private BufferedReader _reader;
	private boolean _hasEnded;
	private String _next;
	private int _cachedChar;
	private String _last;
	private boolean _steppedBack;
	private int _lines; // TODO make error logging more accurate with line
						// number
	private int _charIndex;
	private int _elements;
	private ArrayList<Integer> _newLines;
	private ArrayList<Integer> _spaces;
	private String _fileName;

	public CodeParser(File file) throws FileNotFoundException {
		_fileName = file.getPath();
		System.out.println("Processing file " + _fileName);
		FileInputStream inputStream = new FileInputStream(file);
		DataInputStream dataInput = new DataInputStream(inputStream);
		try {
			_reader = new BufferedReader(new InputStreamReader(dataInput, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_lines = 0;
		_elements = -1;
		_charIndex = 0;
		_hasEnded = false;
		_cachedChar = (char) -1;
		_newLines = new ArrayList<Integer>();

		// TODO : make HashMap with Integer and Boolean
		_spaces = new ArrayList<Integer>();
		_next = preNext();
		// TODO LAST minute fix for windows, there is some weird bug with the
		// first character not working out...
		// A class should start with a package word anyway so I'll leave it at
		// this for the time being.
		while (!_next.isEmpty() && _next.charAt(0) != 'p') {
			if (_next.length() == 1)
				_next = "";
			else
				_next = _next.substring(1);
		}
	}

	public boolean hasNext() {
		return !_hasEnded;
	}

	public String next() {
		if (_steppedBack) {
			_steppedBack = false;
			return _last;
		}
		String out = _next;
		_last = _next;
		_next = preNext();

		for (int i = 0; i < out.length(); i++) {
			if (out.charAt(i) == '\n' || out.charAt(i) == '\r' || out.charAt(i) == '\t') {
				System.out.println("NEW LINE IN THE FUCKING OUT !!!!");
				System.out.println("word : " + out);
				System.out.println((int) out.charAt(0));
				System.out.println(out.length());
				System.exit(0);
			}
		}
		return out;
	}

	private String preNext() {
		try {
			_elements++;
			return getNext();
		} catch (IOException e) {
			_hasEnded = true;
		}
		try {
			_reader.close();
		} catch (IOException e) {
			System.out.println("couldn't close reader!");
			e.printStackTrace();
		}
		return null;
	}

	private String getNext() throws IOException {
		String out = "";
		boolean searching = true;
		_charIndex = 0;

		// a list of characters that we want separated by separators like space
		// and newline
		// in code this may not be done already so we got to do this
		// see
		// http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/operators.html
		// for a list of operators
		char[] separationChars = { ')', '(', ';', '{', '}', ',', '-', '+', '%', '&', '*', '<', '>', '^', '|', '~', '!',
				'/', '?', ':', '[', ']', '.', '=' };

		// some pairs of separation characters need to stay paired to have the
		// same meaning, store them as well
		// the '//' combo is not included because this should already be
		// filtered by the parser
		char[] separationDoubles = { '-', '-', '+', '+', '+', '=', '/', '=', '%', '=', '*', '=', '-', '=', '<', '<',
				'>', '>', '&', '=', '|', '=', '^', '=', '=', '=', '>', '=', '!', '=', '<', '=', '&', '&', '|', '|',
				':', ':', '.', '.', '/', '/', '/', '*' };

		// there are even triple combinations which we cannot break without
		// altering the syntaxes meaning, luckily they're all build upon the
		// pairs
		// EDIT [ADDED ...] for argument of functions!

		char[] separationTriples = { '>', '>', '>', '<', '<', '=', '>', '>', '=', '=', '=', '=', '!', '=', '=', '&',
				'&', '=', '|', '|', '=', '.', '.', '.' };

		// whoopiedoo there even is a quadruple character operator combination!
		char[] separationQuadruples = { '>', '>', '>', '=' };

		char[] commentBlockBegin = { '/', '*' };

		char[] commentLineBegin = { '/', '/' };

		char[] commentBlockEnd = { '*', '/' };

		char[][] sets = { separationChars, separationDoubles, separationTriples, separationQuadruples };

		int state = FIRST_ITERATION;

		// when checking for strings things like "\\" or "\\\"" are both 2 valid
		// strings, we need to count the slashes and check if its even or
		// uneven
		int slashCharacterChain = 0;
		int stringEndSearch = 0;
		boolean whiteSpace = false;

		mainLoop: while (searching) {
			int c;
			if (_cachedChar != -1) {
				c = _cachedChar;
				_cachedChar = -1;
			} else {
				c = _reader.read();
				_charIndex++;
			}

			if (c == -1) {
				_hasEnded = true;
				return "";
			}
			stateSwitch: switch (state) {
			case FIRST_ITERATION: // first character, rather important

				if (isWhiteSpace(c)) {
					if (isNewLine(c))
						out = "\n";
					else if (isWhiteSpace(c)) {
						whiteSpace = true;
					} else
						out = "";
					break mainLoop;
				}

				// first check the double combinations, if they're not a combo
				// the first char is always a single character and the next
				// character can be 'cached'

				char[] varSet = new char[4];
				varSet[0] = (char) c;

				for (int i = 1; i < 5; i++) {
					if (i == sets.length)
						return new String(varSet);

					int nextChar = _reader.read();
					if (nextChar == -1)
						return Character.toString((char) c);
					varSet[i] = (char) nextChar;

					if (checkSet(sets[i], varSet, i + 1)) {
						// there is one exception where the double combo can be
						// the beginning of a comment block (/*) or comment line
						// (//)
						if (i == 1) {
							if (checkSet(varSet, commentBlockBegin, 2)) {
								state = COMMENTBLOCK;
								break stateSwitch;
							} else if (checkSet(varSet, commentLineBegin, 2)) {
								state = COMMENTLINE;
								break stateSwitch;
							}
						}
						continue;
					}
					// (else)
					// if there is no double, proceed to normal calculations
					_cachedChar = nextChar;
					if (i == 1) {
						break;
					}

					char[] combo = new char[i];
					for (int j = 0; j < i; j++) {
						combo[j] = varSet[j];
					}
					return new String(combo);
				}
				// check if the character is an break character like (
				if (checkSet(separationChars, varSet, 1)) {
					return Character.toString((char) c);
				}

				if (c == '\"' || c == '\'') {
					// if the character starts with a " or ' it should maintain
					// that string
					stringEndSearch = c;
					state = STRINGMODE;
				} else {
					// otherwise continue normal operations
					state = NORMAL_PROCESS;
				}
				out += (char) c;
				break;
			case STRINGMODE:
				if (c == stringEndSearch && even(slashCharacterChain)) {
					out += (char) c;
					return out;
				}
				out += (char) c;
				break;
			case NORMAL_PROCESS:
				// check if there is a separation character
				for (int i = 0; i < separationChars.length; i++) {
					// if it is true stop the search but remember this character
					// for later
					char s = separationChars[i];
					if (c == s) {
						searching = false;
						_cachedChar = c;
						break mainLoop;
					}
				}

				if (!isWhiteSpace(c) && c != 65279) {
					out += (char) c;
				} else {
					if (c != 65279)
						_cachedChar = c;
					searching = false;
				}
				break;
			case COMMENTLINE:
				if (isNewLine(c)) {
					searching = false;
					out = "";
				}
				break;
			case COMMENTBLOCK:
				int nextChar = _reader.read();
				if (nextChar == -1)
					return "";

				char[] twoChars = { (char) c, (char) nextChar };
				if (checkSet(commentBlockEnd, twoChars, 2)) {
					searching = false;
					out = "";
				} else {
					_cachedChar = nextChar;
				}
				break;
			default:
				System.err.println("Unhandled State :( " + state);
				System.exit(0);
			}

			if (c == '\\') {
				slashCharacterChain++;
			} else {
				slashCharacterChain = 0;
			}
		}

		if (whiteSpace) {
			_spaces.add(_elements - 1);
		}
		if (out.length() == 1 && isNewLine(out.charAt(0))) {
			_newLines.add(_elements);
		}

		if (out == "" || isWhiteSpace(out.charAt(0))) {
			return getNext();
		}
		return out;
	}

	private boolean isNewLine(int c) {
		return c == '\n' || c == '\r';
	}

	private boolean isWhiteSpace(int c) {
		return c == '\n' || c == '\r' || c == '\t' || c == ' ';
	}

	public ArrayList<Integer> getNewLineList() {
		return _newLines;
	}

	public ArrayList<Integer> getSpaceList() {
		return _spaces;
	}

	private boolean checkSet(char[] charSet, char[] varSet, int setLength) {
		// assert that input is OK.

		// compare the two sets to each other
		charLoop: for (int i = 0; i < charSet.length; i += setLength) {
			for (int j = 0; j < setLength; j++) {
				if (charSet[i + j] != varSet[j]) {
					continue charLoop;
				}
			}
			// if this point is reached we know there is a match!
			return true;
		}

		// all combinations have been checked without result; return false
		return false;
	}

	/**
	 * returns true if the integer is even
	 * 
	 * @param i
	 *            MERPDERPW
	 * @return if i is even
	 */
	private boolean even(int i) {
		return i / 2 == (i + 1) / 2;
	}

	/**
	 * Makes the Parser take a step back, so it will give the same word twice
	 * 
	 */
	public void stepBack() {
		_steppedBack = true;
	}

	/**
	 * Gives additional error info about the occurrence of the parsing error
	 * such as line and character and file.
	 * 
	 * @return
	 */
	public String errorInfo() {
		return "In " + _fileName + " line : " + _newLines.size() + " character : " + _charIndex;
	}
}
