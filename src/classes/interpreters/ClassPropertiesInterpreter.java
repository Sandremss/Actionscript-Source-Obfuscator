package classes.interpreters;

import java.util.ArrayList;

import asformat.CodeParser;
import asformat.ParseError;
import classes.ActionScriptClass;
import classes.IClassPropertyGetterSetter;

/**
 * This class interprets all the parsed words from CodeParser, and searches for
 * Class-properties to store such as packagename, classname and imports.
 * 
 * @author sander
 * 
 */
public class ClassPropertiesInterpreter {

	/**
	 * Checks to see if there is a package present.
	 */
	public static void checkPackage(String thisString, CodeParser parser, ArrayList<String> elements,
			IClassPropertyGetterSetter asClass) {
		if (thisString.equals("package")) {
			String packageName = ClassParseUtils.getDotList(parser, elements);
			if (packageName == null) {
				rageQuit("Internal error: packageName is null!", parser);
			}
			asClass.setPackageName(packageName);
		}
	}

	/**
	 * Exits the program in style.
	 */
	private static void rageQuit(String string, CodeParser parser) {
		ParseError.rageQuit(string, parser);
	}

	/**
	 * Checks to see if the class is defined here and if it is also stores
	 * information about superclass and possible interfaces.
	 */
	public static void checkClass(CodeParser parser, String thisString, ArrayList<String> elements,
			IClassPropertyGetterSetter asClass) {
		if (thisString.equals("class") || thisString.equals("interface")) {
			boolean isInterFace = thisString.equals("interface");
			String className = getOrFail(parser, "eof after class definition!?!");
			elements.add(className);
			asClass.setClassName(className);
			asClass.setIsInterFace(isInterFace);
			checkExtension(parser, elements, asClass);
			checkImplementations(parser, elements, asClass);
		}
	}
	
	
	/**
	 * Checks if this class extends any superclass
	 */
	private static void checkExtension(CodeParser parser, ArrayList<String> elements, IClassPropertyGetterSetter asClass) {
		String next = getOrFail(parser, "error after class name");
		if (next.equals("extends")) {
			elements.add(next);
			String extended = getOrFail(parser, "eof after extends !");
			elements.add(extended);
			asClass.setSuperClassName(extended);
		} else {
			parser.stepBack();
		}
	}

	/**
	 * gets the next word or stops the program.
	 */
	private static String getOrFail(CodeParser parser, String errorMsg) {
		if(parser.hasNext()) return parser.next();
		ParseError.rageQuit(errorMsg, parser);
		return "";
	}

	/**
	 * checks to see if this class has any interfaces and if so, adds them to the class
	 */
	private static void checkImplementations(CodeParser parser, ArrayList<String> elements,
			IClassPropertyGetterSetter asClass) {
		String next = getOrFail(parser, "error after class name!");
		if (next.equals("implements")) {
			do {
				elements.add(next);
				String implementation = parser.hasNext() ? parser.next() : "";
				asClass.addInterFace(implementation);
				System.out.println("ADDING INTERFACE : " + implementation);
				elements.add(implementation);
				next = getOrFail(parser, "OMG empty after implementation definition!");
			} while (next.equals(","));
		}
		parser.stepBack();
	}

	/**
	 * Checks the imports of the class
	 */
	public static void checkImport(CodeParser parser, String thisString, ArrayList<String> elements,
			ActionScriptClass asClass) {
		if (thisString.equals("import")) {
			String completeList = ClassParseUtils.getDotList(parser, elements);
			asClass.addImport(completeList);
		}
	}

}
