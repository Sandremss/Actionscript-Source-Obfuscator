package classes.interpreters;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import asformat.CodeParser;
import classes.ActionScriptClass;
import data.IAddVariable;
import data.Variable;

/**
 * Calls other helper classes to interpret the parsed words from the
 * actionscript class
 * 
 * @author sander
 * 
 */
public class ClassParseInterpreter {
	private static final int STATE_OUTSIDE_PACKAGE = 0;
	private static final int STATE_IMPORTS = 1;
	private static final int STATE_INCLASS = 2;

	public static void parseClass(File file, ArrayList<String> _elements, ArrayList<Variable> _members,
			ActionScriptClass asClass, HashMap<String, Variable> _translationMap) {
		CodeParser parser = null;
		try {
			parser = new CodeParser(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("File not found that we've previously found... wtf, In ActionScriptClass::parseClass");
			System.exit(0);
		}

		String currentString = null;

		// parseState keeps track of where we are based on brackets ({, })
		int parseState = STATE_OUTSIDE_PACKAGE;

		Variable lastFunction = null;

		while (parser.hasNext()) {
			currentString = parser.next();

			parseState = ClassParseUtils.checkState(parseState, currentString);
			_elements.add(currentString);

			if (isOutsidePackage(parseState)) {
				ClassPropertiesInterpreter.checkPackage(currentString, parser, _elements, asClass);
			}

			if (isInImport(parseState)) {
				ClassPropertiesInterpreter.checkClass(parser, currentString, _elements, asClass);
				ClassPropertiesInterpreter.checkImport(parser, currentString, _elements, asClass);
			}
			if (isInClass(parseState)) {
				lastFunction = replaceIfNotNull_ParseMember(parser, currentString, lastFunction, _elements, asClass);
			}
			if (isInFunction(parseState)) {
				// When inside a function, new variables (which are local
				// variables) should be added to the function.
				MemberInterpreter.checkMember(currentString, parser, _elements, lastFunction);
			}
		}

		if (asClass.getPackageName() == null) {
			System.out.println("THIS CLASS HAS NO PACKAGE NAME!!! " + file);
			System.exit(0);
		}

		for (Variable var : _members) {
			if(!var.isAnomynous())
			_translationMap.put(var.getOldName(), var);
		}

		if (asClass.getClassName() != null)
		{
			Variable variable = new Variable("super");
			variable.SetType(asClass.getFullType(asClass.getClassName()));		
	
			_translationMap.put("super", variable);
		}

		asClass.setNewLines(parser.getNewLineList());
		asClass.setSpaces(parser.getSpaceList());
	}

	private static Variable replaceIfNotNull_ParseMember(CodeParser parser, String thisString, Variable lastFunction,
			ArrayList<String> elements, IAddVariable asClass) {
		Variable newFunct = MemberInterpreter.checkMember(thisString, parser, elements, asClass);
		if (newFunct != null)
			lastFunction = newFunct;
		return lastFunction;
	}

	private static boolean isOutsidePackage(int state) {
		return state == STATE_OUTSIDE_PACKAGE;
	}

	private static boolean isInImport(int state) {
		return state == STATE_IMPORTS;
	}

	private static boolean isInFunction(int state) {
		return state > STATE_INCLASS;
	}

	private static boolean isInClass(int state) {
		return state == STATE_INCLASS;
	}

}
