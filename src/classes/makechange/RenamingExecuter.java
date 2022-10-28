package classes.makechange;

import java.util.ArrayList;
import java.util.Arrays;

import main.IGetClass;
import packages.PackageAS;
import classes.ActionScriptClass;
import classes.interpreters.ClassParseUtils;
import data.Variable;

/**
 * We've read all the info, and safely internally renamed them; now it is time
 * to let these changes have some physical effect on the class. This class makes
 * sure that the changes get executed correctly.
 * 
 * @author sander
 * 
 */
public class RenamingExecuter {

	/**
	 * Checks and replaces imports
	 */
	public static int checkImports(ArrayList<String> elements, int i, IGetClass classManager,
			Iterable<String> asteriskImports) {
		String word = elements.get(i);

		if (word.equals("import")) {
			i++;
			int clearIndex = i;
			String list = getDotList(elements, i);
			i = res;
			ActionScriptClass actionScriptClass = classManager.getClass(list);

			for (String string : asteriskImports) {
				if (string.equals(list)) {
					// get rid of the last 2 for example flash.display(.*)
					PackageAS packageAS = classManager.getPackage(string.substring(0, string.length() - 2));
					if (packageAS != null) {
						elements.set(i - 1, packageAS.getNewName() + ".*");
						for (int j = clearIndex; j < i - 1; j++) {
							elements.set(j, "");
						}
					}
				}
			}

			if (actionScriptClass != null) {
				list = actionScriptClass.getFullNewName();
				elements.set(i - 1, list);
				for (int j = clearIndex; j < i - 1; j++) {
					elements.set(j, "");
				}
			}
		}

		return i;
	}

	/**
	 * Gets a complete list, the dot ('.') splits strings like
	 * flash.display.Sprite but we want the entire list in one string
	 * 
	 * @param elements
	 *            the list of words
	 * @param i
	 *            the index of the word
	 * @return the new index
	 */
	private static String getDotList(ArrayList<String> elements, int i) {
		String completeList = "";
		boolean word = true;

		while (true) {
			String string = elements.get(i++);
			if (string.isEmpty()) {
				System.out.println("eof after sequence!");
				System.exit(0);
				// TODO make ragequit
			}
			if (!word || string.equals("{")) {
				if (!string.equals(".")) {
					i--;
					break;
				}
			}
			completeList += string;
			word = !word;
		}
		res = i;
		return completeList;
	}

	/**
	 * Checks the package name and replaces it with the new package name
	 */
	public static int checkPackageName(ArrayList<String> elements, int i, String packageName, String newPackageName) {
		String word = elements.get(i);
		if (word.equals("package")) {
			if (packageName.isEmpty()) {
				// if this used to be the default package, append the new name
				// to the word!
				word += " " + newPackageName;
				elements.set(i, word);
				return i;
			}
			i++;
			int clearIndex = i;
			String list = getDotList(elements, i);
			i = res;
			if (list.equals(packageName)) {
				elements.set(i - 1, newPackageName);
				for (int j = clearIndex; j < i - 1; j++) {
					elements.set(j, "");
				}
				i--;
			}
		}
		return i;
	}

	// TODO fix lazy coding >___>
	private static int res;
	private static int ref;

	/**
	 * Checks for a class, and replaces it with the new class name
	 */
	// TODO improve, there should be only one class to check in this phase
	public static void checkOnlyClasses(ArrayList<String> elements, int i, IGetClass _classManager,
			ActionScriptClass caller) {
		String word = elements.get(i);
		ActionScriptClass asClass = _classManager.getClass(caller.getFullType(word));
		if (asClass != null) {
			word = asClass.getNewName();
			elements.set(i, word);
		}
	}

	// TODO complete the list.. performance related
	public static final String[] invalidVariableName = { "break", "{", "}", ")", "(" };

	/**
	 * This function deals with renaming members of the class. It should also be
	 * able to deal with calls to other functions like:
	 * this.a.getB().doSomething(c,d).e
	 * 
	 * A recursive approach is chosen for this problem.
	 * 
	 * @param baseType
	 *            The original class where this code is in, used for function
	 *            parameters.
	 * @param type
	 *            The type of the class we are currently in, for example we
	 *            should look at the properties of class B, while this code is
	 *            in class A.
	 * @param elements
	 *            the list of words
	 * @param i
	 *            the index of the current word
	 * @param inFunction
	 *            the function we are in, used to access local variables and
	 *            parameters
	 * @param isInChain
	 *            whether we are in a nested call, this means there is a
	 *            variable + "." before the current word
	 * @param totalChain
	 *            The total string of the chain, used to rename things like
	 *            flash.display.Sprite
	 * @param classManager
	 *            The manager of classes which we can ask new names of members
	 * @param superClassName
	 *            The name of the superclass of the calling Actionscriptclass,
	 *            used to access variables
	 * @return
	 */
	public static int getReferences(ActionScriptClass baseType, ActionScriptClass type, ArrayList<String> elements,
			int i, Variable inFunction, boolean isInChain, String totalChain, IGetClass classManager,
			String superClassName) {
		// TODO this is a BIG method and got more bloated over time, needs some
		// refactoring.
		// TODO could probably need some cleaning too, there is some redundancy
		String varName = null;
		String string = null;
		ActionScriptClass nextType = null;

		if (i < elements.size())
			varName = elements.get(i);

		totalChain += varName;

		if (Arrays.asList(invalidVariableName).indexOf(varName) >= 0)
			return i;

		Variable var = null;
		// TODO make check if superclass == null better!
		if (varName.equals("super") && classManager.getClass(baseType.getFullType(superClassName)) != null) {
			var = classManager.getClass(baseType.getFullType(superClassName)).askVariable("super");
			var.lockRename();
			//var = classManager.getClass(baseType.getFullType(superClassName));
		}

		if (type != null && var == null && isInChain) {
			var = type.askVariable(varName);
		}

		if (type != null && type.equals(baseType) && inFunction != null && var == null) {
			var = inFunction.getLocalVar(varName);
		}

		if (type != null && var == null) {
			var = type.askVariable(varName);
		}

		if (type != null && type.equals(baseType) && varName.equals("this")) {
			nextType = baseType;
		} else if (type != null && type.equals(baseType)
				&& classManager.getClass(baseType.getFullType(varName)) != null) {
			nextType = classManager.getClass(baseType.getFullType(varName));
			if (nextType.equals(baseType)) { // if this is the Constructor
												// function,
												// we don't want a package to
												// precede it
				string = nextType.getNewName();
			} else {
				string = nextType.getFullNewName();
			}
			elements.set(i, string);
		}

		if (type == null && isInChain) {
			ActionScriptClass chainClass = classManager.getClass(baseType.getFullType(totalChain));
			if (chainClass != null) {
				nextType = chainClass;
				// not fast, but its a one-liner :D
				int matchesCount = (totalChain.split("\\.").length - 1) * 2;
				if (nextType.equals(baseType)) { // this would be weird code :s
					string = nextType.getNewName();
				} else {
					string = nextType.getFullNewName();
				}
				elements.set(i, string);

				for (int j = i - matchesCount; j < i; j++) {
					elements.set(j, "");
				}
			}
		}

		if (var != null && !var.getName().equals(baseType.getClassName())) {
			string = var.getName();
			elements.set(i, string);

			// Vector Exception
			if (var.isVector()) {
				nextType = checkVector(var, i, elements, classManager, baseType, inFunction, superClassName);
				if (nextType != null) {
					//TODO fix lazy coding
					i = ref;
				}
			} else {
				nextType = classManager.getClass(var.getType());
			}

		}

		i++;
		if (i >= elements.size())
		{
			//System.out.println("Something wrong with the file");
			i--;
			return i;
		}
		string = elements.get(i);

		if (string.equals("(") || string.equals("[")) {
			//this way arrays with [] won't reset the chain
			String close = string.equals("(") ? ")" : "]";
			while (true) {
				i++;
				string = elements.get(i);
								
				if (string.equals(close)) {
					i++;
					string = elements.get(i);
					break;
				}
				i = getReferences(baseType, baseType, elements, i, inFunction, false, "", classManager, superClassName);
			}
		}

		if (string.equals(".")) {
			i++;
			totalChain += ".";
			i = getReferences(baseType, nextType, elements, i, inFunction, true, totalChain, classManager,
					superClassName);
		} else {
			i--;
		}
		return i;
	}

	private static ActionScriptClass checkVector(Variable var, int i, ArrayList<String> elements, IGetClass classManager, ActionScriptClass baseType, Variable inFunction, String superClassName) {
		i++;
		String next = elements.get(i);
		//System.out.println("point: " + next);
		if (next.equals("[")) {
			while (true) {
				i++;
				String string = elements.get(i);
				if (string.equals("]")) {
					ActionScriptClass out = classManager.getClass(var.getVectorType());
					//TODO fix lazy Coding
					ref = i;
					return out;
				}
				i = getReferences(baseType, baseType, elements, i, inFunction, false, "", classManager, superClassName);
			}
		}
		return null;

	}

	// Constants to indicate in what phase we are in the class
	private static final int STATE_OUTSIDE_PACKAGE = 0;
	private static final int STATE_IMPORTS = 1;
	private static final int STATE_INCLASS = 2;

	private static final String[] instanceDeclarationKeywords = { "var", "const", "function" };

	/**
	 * The main function that goes through all the words of the class and makes
	 * the physical changes to it.
	 * 
	 * @param elements
	 * @param caller
	 * @param classManager
	 * @param asteriskImports
	 */
	public static void createChanges(ArrayList<String> elements, ActionScriptClass caller, IGetClass classManager,
			Iterable<String> asteriskImports) {
		int state = STATE_OUTSIDE_PACKAGE;
		Variable currentFunction = null;

		for (int i = 0; i < elements.size(); i++) {
			String string = elements.get(i);
			state = ClassParseUtils.checkState(state, string);
			if (state == STATE_OUTSIDE_PACKAGE) {
				i = RenamingExecuter.checkPackageName(elements, i, caller.getPackageName(), caller.getNewPackageName());
			}
			if (state == STATE_IMPORTS) {
				RenamingExecuter.checkOnlyClasses(elements, i, classManager, caller);
				i = RenamingExecuter.checkImports(elements, i, classManager, asteriskImports);
			}
			if (state == STATE_INCLASS) {
				if (Arrays.asList(instanceDeclarationKeywords).indexOf(string) >= 0) {
					i++;
					string = elements.get(i);
					currentFunction = caller.askVariable(string);
				}
			}
			if (state >= STATE_INCLASS) {
				i = RenamingExecuter.getReferences(caller, caller, elements, i, currentFunction, false, "",
						classManager, caller.getSuperClassName());
			}
		}
	}

}
