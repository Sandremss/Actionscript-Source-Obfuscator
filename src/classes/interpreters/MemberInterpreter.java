package classes.interpreters;

import java.util.ArrayList;
import java.util.Arrays;

import asformat.CodeParser;
import asformat.ParseError;
import data.IAddVariable;
import data.Variable;

/**
 * This class interprets the parsed words from the {@link CodeParser}, it looks
 * for information that we seek; namely variable names and function names. It
 * will also store local variables and parameters of functions
 * 
 * @author sander
 * 
 */
public class MemberInterpreter {

	private static final String[] instanceDeclarationKeywords = { "var", "const", "function" };

	/**
	 * This function is part of the interpretation phase of the parsing. Since
	 * our main goal is to rename variables and function names, we should look
	 * for these and capture them. The word 'Member' is used here in the context
	 * as in it can either be a Variable or a Function. <br>
	 * <br>
	 * A better implementation in its current form for the data storage would be
	 * a Member class with Variable and Function as its subclasses.<br>
	 * <br>
	 * 
	 * If it finds a variable or a constant it will save these variables to the
	 * desired target through the interface IAddVariable. The return variable is
	 * mostly used for functions, because in functions local variables need to
	 * be linked to them.<br>
	 * 
	 * This function also will request extra data from the parser, namely future
	 * words to help with data collection, it may also request previous words
	 * for instance to look for the override word to mark a function as
	 * override. All words occured are to be added to the elements ArrayList to
	 * maintain a complete list of the word occerences.
	 * 
	 * @param word
	 *            The word that is to be interpreted.
	 * @param parser
	 *            The parser that will be used to get debug data and to request
	 *            additional data.
	 * @param elements
	 *            The elements that can be used to read previous words as well
	 *            as add new ones.
	 * @param addVarsTo
	 *            . The interface where new variables and functions are to be
	 *            added to.
	 * @return A function (with type Variable) to add local variables to.
	 */
	public static Variable checkMember(String word, CodeParser parser, ArrayList<String> elements,
			IAddVariable addVarsTo) {
		// if the word is not 'var', 'function', or 'const' there is no member
		// to be found here
		if (!isInstanceDeclaration(word))
			return null;

		boolean isFunction = word.equals("function");

		String variableName = getOrFail(parser, elements, "instance declaration word " + word + " !");

		if (isFunction) {
			return parseFunction(variableName, parser, elements, addVarsTo);
		} else {
			return parseProperty(variableName, parser, elements, addVarsTo);
		}
	}

	/**
	 * Parses a property
	 * 
	 * @param variableName
	 *            The name of the variable.
	 * @param parser
	 *            The parser.
	 * @param elements
	 *            The elements.
	 * @param addVarsTo
	 *            The interface in which to add the variables to
	 * @return NULL, only functions have to be stored because of local
	 *         variables.
	 */
	private static Variable parseProperty(String variableName, CodeParser parser, ArrayList<String> elements,
			IAddVariable addVarsTo) {
		// parseProperty(parser, elements, variableName, var);

		Variable variable = new Variable(variableName);

		checkType(variableName, parser, elements, variable);

		if (addVarsTo != null)
			addVarsTo.addVariable(variable);

		// var lol:Number = getMaiNumber(lolol, lolo.o(oe(oe.oe), 2)), barry:int
		// = 3;
		// var a:int;

		extractVariableSummation(parser, elements, addVarsTo);
		return null;
	}

	/**
	 * This method will extract extra variables after a definition, variables
	 * are separated by commas, and can also be initialized with the help of
	 * functions even.
	 * 
	 * @param parser
	 *            The parser.
	 * @param elements
	 *            The elements.
	 * @param addVarsTo
	 *            The interface in which to store the variables.
	 */
	private static void extractVariableSummation(CodeParser parser, ArrayList<String> elements, IAddVariable addVarsTo) {
		String variableName;
		int bracketChain = 0;
		while (true) {

			variableName = getOrFail(parser, "parsing variables in chain");
			if (variableName.equals(";") || isReservedWord(variableName) || bracketChain < 0)
				break;

			// var barry:Number = addNumbers(3, 12, 9, 1), b = 0;
			// in between the brackets we don't want the numbers to be
			// interpreted as variables
			bracketChain = increaseDecrease(variableName, bracketChain, "(", ")");

			// var aap:Array = [barry, 3, "hi"]; we don't want what is in
			// between to be interpreted as variables
			bracketChain = increaseDecrease(variableName, bracketChain, "[", "]");
			elements.add(variableName);
			if (variableName.equals(",") && bracketChain == 0) {
				variableName = getOrFail(parser, elements, "parsing variables in chain");
				if (variableName.equals("...")) {
					return;
				}
				Variable nextVariable = new Variable(variableName);
				checkType(variableName, parser, elements, nextVariable);
				if (addVarsTo != null)
					addVarsTo.addVariable(nextVariable);
			}
		}
		// this loop stops at ';' or when the bracket chain is lower than 0
		// the last thing will occur after function a(b,c) : Number
		// we do not want to take information away from the last string
		parser.stepBack();
	}

	private static boolean isReservedWord(String variableName) {
		// TODO Implement this you lazy bitch!
		return (variableName.equals("..."));
	}

	/**
	 * Increases the number or decreases it based on the String, for example if
	 * you want to count in what scope the parser is by adding for every '{' and
	 * decreasing 1 for every '}' this method should be used.
	 * 
	 * @param string
	 *            The string which to check with.
	 * @param chain
	 *            The chain that is in place.
	 * @param up
	 *            The string in which case to add 1.
	 * @param down
	 *            The string in which case to decrease 1.
	 * @return the new chain index
	 */
	private static int increaseDecrease(String string, int chain, String up, String down) {
		if (string.equals(up)) {
			chain++;
		} else if (string.equals(down)) {
			chain--;
		}
		return chain;
	}

	/**
	 * Checks the type for the current member, it will look for the ':' sign
	 * which indicates a type to follow to define the type of the member.<br>
	 * It is important that this process happens because it allows for
	 * references to other custom classes to have effective name changes.
	 * Dynamically typed variables don't mix well with renaming and custom
	 * classes.
	 * 
	 * @param memberName
	 *            The name of the member for debugging.
	 * @param parser
	 *            The parser.
	 * @param elements
	 *            The elements.
	 * @param member
	 *            The member in which to store the potential type.
	 */
	private static void checkType(String memberName, CodeParser parser, ArrayList<String> elements, Variable member) {
		String expectColon = getOrFail(parser, "function " + memberName + ", expected :");
		if (expectColon.equals(":")) {
			elements.add(expectColon);
			String type = ClassParseUtils.getDotList(parser, elements);
			member.SetType(type);

			// Exception for Vector objects
			checkVector(parser, elements, member, type);

		} else {
			member.SetType("*");
			parser.stepBack();
		}
	}

	/**
	 * Checks if the variable is a Vector object, if so it stores the extra
	 * information needed to obfuscate with type safety of the Vector object.
	 * 
	 * @param parser
	 * @param elements
	 * @param member
	 * @param type
	 */
	private static void checkVector(CodeParser parser, ArrayList<String> elements, Variable member, String type) {
		if (type.equals("Vector.<")) {
			System.out.println("type is a vector!");
			String vectorType = getOrFail(parser, elements, "Vector.<");
			member.setVectorType(vectorType);
		}

	}

	/**
	 * Parses a function, it will store information about the function, it will
	 * ignore the function names 'get' and 'set' as they indicate a getter or
	 * setter function.<br>
	 * Its a good thing that getters and setters are merely a change in syntax
	 * that is not vital for this process so we may ignore it.
	 * 
	 * @param functionName
	 *            The name of the function
	 * @param parser
	 *            The parser
	 * @param elements
	 *            The array to add future strings to
	 * @param addVarsTo
	 *            The interface to add the function to
	 * @return the resulting function
	 */
	private static Variable parseFunction(String functionName, CodeParser parser, ArrayList<String> elements,
			IAddVariable addVarsTo) {

		// ignore getters and setters
		if (functionName.equals("get") || functionName.equals("set")) {
			functionName = getOrFail(parser, elements, "getter / setter function");
		}

		boolean isAnonymous = false;
		if (functionName.equals("(")) {
			System.out.println("anonymous function detected! Result could be unstable!");
			isAnonymous = true;
			parser.stepBack();
			elements.remove(elements.size() - 1);
		}

		Variable function = null;

		if (isAnonymous) {
			function = new Variable("");
			function.setAnonymous();
		} else
			function = new Variable(functionName);

		// store whether this function overrides
		function.setOverride(occursBefore(elements, "override", 5));

		// function definition should be followed by a '('
		String validAfterFunction = getOrFail(parser, elements, "function definition! " + functionName);
		if (!validAfterFunction.equals("("))
			rageQuit("Function: " + functionName + " is not followed by '('", parser);

		// store arguments
		checkFunctionParameters(parser, elements, function);

		// store the type
		checkType(functionName, parser, elements, function);

		if (addVarsTo != null)
			addVarsTo.addVariable(function);

		return function;

	}

	/**
	 * Collects and stores function arguments to the specified function.<br>
	 * These will be treated no different from local variables.
	 * 
	 * @param parser
	 *            The parser.
	 * @param elements
	 *            The element array.
	 * @param function
	 *            The function which to add parameters to.
	 */
	private static void checkFunctionParameters(CodeParser parser, ArrayList<String> elements, Variable function) {
		String argument = getOrFail(parser, elements, "function arguments parse!");
		// if its a function startGame(), there are no parameters!
		if (argument.equals(")"))
			return;
		if (argument.equals("...")) {
			return;
		}
		Variable parameterVariable = new Variable(argument);
		checkType(argument, parser, elements, parameterVariable);
		function.addVariable(parameterVariable);
		// there may be more than one variable
		extractVariableSummation(parser, elements, function);
	}

	/**
	 * Gets the next string or exits the program.
	 */
	private static String getOrFail(CodeParser parser, ArrayList<String> elements, String errorMsg) {
		return ClassParseUtils.getOrFail(parser, elements, errorMsg);
	}

	/**
	 * Checks to see if in the recent history in the parsing process a certain
	 * word occurred. For example we have 'public function aap' and we are at
	 * 'aap' and we want to know if the scope of the function is 'public', this
	 * function does just that.
	 * 
	 * @param elements
	 *            The element array in which to search back
	 * @param stringToSearch
	 *            The string we are looking for
	 * @param howFarBack
	 *            An integer representing how far back we should look
	 * @return a boolean value representing if there was match
	 */
	private static boolean occursBefore(ArrayList<String> elements, String stringToSearch, int howFarBack) {
		int a = elements.size() - 1;
		for (int i = 0; i < howFarBack && elements.size() > i; i++) {
			String possibleOverride = elements.get(a - i);
			if (possibleOverride.equals(stringToSearch)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tries to get the next string from the ActionScript parser, if it fails
	 * due to End Of File, it will end the program with the specified debug
	 * message.<br>
	 * <br>
	 * <strong> NOT TO BE CONFUSED WITH getOrElse(CodeParser parser,
	 * ArrayList<String> elements, String string) WHICH ADDS THE WORD TO THE
	 * ELEMENT ARRAY!</strong>
	 * 
	 * @param parser
	 *            The parser which to attempt to get a file from.
	 * @param errorMsg
	 *            The error message which to display in case of EoF.
	 * @return the next word of the parser.
	 */
	private static String getOrFail(CodeParser parser, String errorMsg) {
		if (!parser.hasNext()) {
			rageQuit("End Of File After " + errorMsg, parser);
		}
		return parser.next();
	}

	/**
	 * checks to see if the word is a word which begins the declaration of a
	 * member, these words can be 'var', 'const', and 'function'.
	 * 
	 * @param word
	 *            The word which to check
	 * @return whether it is an instance declaration word
	 */
	private static boolean isInstanceDeclaration(String word) {
		return Arrays.asList(instanceDeclarationKeywords).indexOf(word) >= 0;
	}

	/**
	 * Leaves the program with an error attached.
	 * 
	 * @param string
	 *            The error message.
	 * @param parser
	 *            The parser which provides additional information.
	 */
	private static void rageQuit(String string, CodeParser parser) {
		ParseError.rageQuit(string, parser);
	}
}
