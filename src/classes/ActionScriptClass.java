package classes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import main.IGetClass;
import main.ObfuscationSettings;
import asformat.CodeParser;
import classes.interpreters.ClassParseInterpreter;
import classes.makechange.RenamingExecuter;
import classes.renaming.MemberRenamer;
import classes.renaming.RenameType;
import classes.renaming.UniqueStringCreator;
import data.IAddVariable;
import data.IRenameLockable;
import data.RenameObjectCounter;
import data.Variable;
import java.util.Random;
import java.io.File;

/**
 * Stores all the data that is to be stored on an Actionscript class. Also calls
 * helperclasses to parse, interpret and rename this data.
 * 
 * @author sander
 * 
 */
public class ActionScriptClass implements IAskVariableName, IAddVariable, IClassPropertyGetterSetter, IRenameLockable {

	/**
	 * Contains all the core Classes that are in Actionscript 3. These classes
	 * don't require import and of course are not renamable, so we'll store
	 * these and check for them and replace them with the * as to indicate
	 * renaming is not required
	 * 
	 * For a list of core classes see the documentation:
	 * http://help.adobe.com/en_US
	 * /FlashPlatform/reference/actionscript/3/package-detail.html#classSummary
	 * 
	 * Note that these classes are replaced with '*' as type only for
	 * performance reasons, otherwise it would look for a Number class on the
	 * package of the current class for example.
	 */
	// TODO move logic to helperclass
	private static final String[] coreTypes = { "ArgumentError", "Array", "Boolean", "Class", "Date",
			"DefinitionError", "Error", "EvalError", "Function", "int", "Math", "NameSpace", "Number", "Object",
			"QName", "RangeError", "ReferenceError", "RegExp", "SecurityError", "String", "SyntaxError", "TypeError",
			"uint", "URIError", "Vector", "VerifyError", "XML", "XMLList", "void" };

	/** the name of the class */
	private String _className;

	/** The new name of the class, after renaming */
	private String _newName;

	/** the package name, "" if default package */
	private String _packageName;

	/** the renamed package name */
	private String _newPackageName;

	/**
	 * contains a mapping of the imports, for example MovieClip to
	 * flash.display.MovieClip.
	 */
	private HashMap<String, String> _importMap;

	/**
	 * Contains all the chopped up code word for word as parsed by
	 * {@link CodeParser}.
	 */
	private ArrayList<String> _elements;

	/** The file that points to the class .AS file */
	private final File _file;
	/**
	 * The map that gets an old variable name and gives the {@link Variable}
	 * object.
	 */
	private HashMap<String, Variable> _translationMap;
	/** The arrayList that contains all the variables and functions */
	private ArrayList<Variable> _members;
	/** Functions that are overridden on this class are stored here */
	private ArrayList<Variable> _overrideFunctions;
	/**
	 * The parent class which to ask for references to other classes based on
	 * package, and class name
	 */
	private final IGetClass _classManager;
	/** The name of the super class */
	private String _superClassName;
	/** The list of Interfaces this class implements */
	private ArrayList<String> _implemantations;
	/** whether this .AS file is an interface */
	private boolean _isInterFace = false;
	/** whether this class has renamed its members already or not */
	private boolean _renamed;
	/** an ArrayList that stores the positions of the newlines */
	private ArrayList<Integer> _newLines;
	/** an ArrayList that stores the positions of the spaces */
	private ArrayList<Integer> _spaces;
	private ArrayList<String> _asteriskImports;
	private boolean _classNameRenamed = false;

	/**
	 * Creates a new Actionscript class object that stores all the things a
	 * class needs to store about itself, it will parse the class and store all
	 * data needed for obfuscation of it.
	 * 
	 * @param file
	 *            The File that is to be parsed as Actionscript file
	 * @param model
	 *            The manager of all classes which can be asked information
	 *            about other classes
	 */
	public ActionScriptClass(File file, IGetClass model) {
		RenameObjectCounter.increaseCount(1);
		this._file = file;
		this._classManager = model;
		_implemantations = new ArrayList<String>();
		_superClassName = "";
		_overrideFunctions = new ArrayList<Variable>();
		_translationMap = new HashMap<String, Variable>();
		_importMap = new HashMap<String, String>();
		_asteriskImports = new ArrayList<String>();
		//_renamed = true;
		//_classNameRenamed = true;
	}

	/**
	 * Parses the .AS file and stores the relevant information
	 * 
	 */
	public void parseClass() {
		_elements = new ArrayList<String>();
		_members = new ArrayList<Variable>();
		ClassParseInterpreter.parseClass(_file, _elements, _members, this, _translationMap);
	}

	/**
	 * Resolves any Asterisk (*) imports, if any so flash.display.* would become
	 * flash.display.Sprite AND flash.display.MovieClip ect.
	 */
	public void resolveAsteriskImports() {
		for (String path : _asteriskImports) {
			System.out.println("asterisk import detected: " + path);
			String packagePath = path.substring(0, path.length() - 2);
			ArrayList<ActionScriptClass> classes = _classManager.getAllClassesFromPackage(packagePath);
			if (classes == null)
				continue;
			System.out.println("getting classes..");
			for (ActionScriptClass actionScriptClass : classes) {
				_importMap.put(actionScriptClass._className, packagePath + "." + actionScriptClass._className);
			}
		}
	}

	/**
	 * Renames the name of this class
	 * 
	 * @param i
	 *            The index of the class
	 * @return the index of the next class that is to renamed
	 */
	public int renameClassName(int i) {
		if (_classNameRenamed) {
			return i;
		}
		if (ObfuscationSettings.uniqueNames())
			_newName = UniqueStringCreator.getUniqueName(RenameType.CLASSNAME, this.getClassName());
		else
		{
			Random rand = new Random();
			i += rand.nextInt(10);
			_newName = "C" + i;
		}

		lockRename();
			
		return i;
	}

	/**
	 * Adds an import to the import map If its an Asterisk import it will be
	 * stored additionally
	 * 
	 * @param completePath
	 *            the entire import
	 */
	public void addImport(String completePath) {
		if (completePath.indexOf(".") >= 0) {
			String className = completePath.substring(completePath.lastIndexOf(".") + 1);
			if (className.equals("*")) {
				_asteriskImports.add(completePath);
			} else {
				_importMap.put(className, completePath);
			}
		}
	}

	/**
	 * renames variables which can be contained to the class
	 */
	public void renameVariables() {
		renameVariables(0);
	}

	/**
	 * renames variables with an index as to avoid collisions with the variables
	 * of another class, for instance it would be bad if 2 interfaces have the
	 * same variable name and a class implements them both.
	 * 
	 * @param index the index
	 * @return the index after renaming
	 */
	public int renameVariables(int index) {
		if (_renamed)
			return index;
		int i = MemberRenamer.renameAllMembers(index, _members, this, _classManager);
		_renamed = true;
		return i;
	}

	/**
	 * If this Actionscript file is an interface or is being subclassed by
	 * another .AS file, it needs to give information about variables that are
	 * already renamed or get higher rename priority.<br>
	 * <br>
	 * 
	 * For example an interface with function giveName():String renamed to
	 * var_1():String It would be bad if this was then in a class that
	 * implements the interface renamed to something else.<br>
	 * <br>
	 * 
	 * 
	 * @param preRenamed
	 *            The arrayList in where to add the prerenamed variables
	 */
	public void getPreRenamed(ArrayList<Variable> preRenamed) {
		System.out.println("giving them the prerenamed");
		for (Variable variable : preRenamed) {
			System.out.println(variable);
		}
		preRenamed.addAll(_members);
	}

	/**
	 * Based on a class name or type it will return the full path based on
	 * imports for example getType("MovieClip") will return
	 * "flash.display.MovieClip" if it is in the import list This full path may
	 * then be used to access other classes
	 * 
	 * @param arg
	 *            the class' name or full path
	 * @return the full path
	 */
	public String getFullType(String arg) {
		if (Arrays.asList(coreTypes).indexOf(arg) >= 0) {
			// core types are treated as '*' because we can't rename anything from those
			return "*";
		}
		String fullPath = _importMap.get(arg);

		// TODO : change if path is complete path with '.'
		if (fullPath == null) {
			if (arg.indexOf(".") >= 0) {
				return arg;
			}
			return _packageName + "." + arg;
		}
		return fullPath;
	}

	/**
	 * Other classes may ask this class the new name of a variable, this
	 * function will look for this old name, then return the {@link Variable}
	 * that is linked to this old name.<br>
	 * <br>
	 * If it cannot find the name it will ask the superclass if present.
	 * 
	 */
	public Variable askVariable(String variable) {
		Variable out = _translationMap.get(variable);
		ActionScriptClass superClass = _classManager.getClass(getFullType(_superClassName));
		if (out == null && superClass != null) {
			return superClass.askVariable(variable);
		}
		return out;
	}

	/**
	 * Returns the class name of this class DERP DIE DERP
	 * 
	 * @return the name of the class
	 */
	public String getClassName() {
		return _className;
	}

	// TODO implement this in another class!

	/**
	 * Renames all references it makes, this includes nested references. In fact
	 * after internally renaming the variables its now making these changes in
	 * the list of elements
	 */
	public void renameReferences() {
		RenamingExecuter.createChanges(_elements, this, _classManager, _asteriskImports);
	}

	/**
	 * Goes through and renames any references to properties and methods of
	 * external classes. If there are brackets it will handle any arguments
	 * recursively and finally it will look for a dot sign as to indicate a call
	 * on the return type or new property
	 * 
	 * 
	 * @param baseType
	 *            The type of the original caller in case of function arguments
	 * @param type
	 *            the type of the last chain so it knows what class to ask for
	 *            the new name of a property
	 * @param elements
	 *            The array that holds all the strings of the class
	 * @param i
	 *            The index to access what string to check
	 * @return the index if there were any jumps during the check
	 */
	//TODO offload logic to helper class
	public void outClass(File out) {
		System.out.println("Write file " + this._file.toString());

		File myOut = new File(out.getAbsolutePath() + "/" + getPackageStructure(out.getAbsolutePath()) + _newName + ".as");

		if (_newName == null)
		{
			myOut = new File(out.getAbsolutePath() + "/" + getPackageStructure(out.getAbsolutePath()) + this._file.getName());
		}

		int tabCount = 0;
		try {
			myOut.createNewFile();
			// Create file
			FileWriter fstream = new FileWriter(myOut);
			BufferedWriter outw = new BufferedWriter(fstream);

			for (int i = 0; i < _elements.size(); i++) {
				String string = _elements.get(i);

				if (string.equals("}")) {
					tabCount--;
				}
				if (_newLines.indexOf(i) >= 0) {
					for (int j = 0; j < tabCount; j++) {
						string = "\t" + string;
					}
					string = System.getProperty("line.separator") + string;
				}

				if (string.contains("{")) {
					tabCount++;
				}

				if (_spaces.indexOf(i) >= 0) {
					string = string + " ";
				}
				outw.write(string);
			}
			outw.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage() + " out: " + myOut);
		}

	}

	private String getPackageStructure(String path) {
		String[] p = _newPackageName.split("\\.");
		if (p.length == 0) {
			if (_newPackageName.isEmpty())
				return _newPackageName + "/";
			File dir = new File(path + "/" + _newPackageName);
			dir.mkdir();
			return _newPackageName + "/";
		}
		String out = "";
		for (int i = 0; i < p.length; i++) {
			File dir = new File(path + "/" + p[i]);
			dir.mkdir();
			path += "/" + p[i];
			out += p[i] + "/";
		}
		return out;
	}

	// -----------------------------------------------------------------------
	// ---------------------- Begin Getters and Setters ----------------------
	// -----------------------------------------------------------------------

	public ArrayList<Variable> getVariables() {
		return _members;
	}
	
	public String getFullNewName() {
		if (_newPackageName.isEmpty())
			return _newName;
		return _newPackageName + "." + _newName;
	}

	public boolean isInterFace() {
		return _isInterFace;
	}

	public String getNewName() {
		return _newName;
	}

	public void setNewPackageName(String newName) {
		_newPackageName = newName;
	}

	public String getNewPackageName() {
		return _newPackageName;
	}

	@Override
	public void addVariable(Variable variable) {
		_members.add(variable);
		if (variable.isOverride())
			_overrideFunctions.add(variable);
	}

	@Override
	public void setPackageName(String packageName) {
		this._packageName = packageName;
		_newPackageName = packageName;
	}

	@Override
	public void setClassName(String className) {
		this._className = className;
		_newName = _className;
		
		if (ObfuscationSettings.isIgnoredClass(className))
		{
			//_renamed = false;
			_classNameRenamed = true;
		}

		if (ObfuscationSettings.isIgnoredMemberInClass(className))
		{
			_renamed = true;
		}
	}

	@Override
	public void setIsInterFace(boolean isInterFace) {
		this._isInterFace = isInterFace;
	}

	@Override
	public void setSuperClassName(String superClassName) {
		this._superClassName = superClassName;
	}

	@Override
	public void addInterFace(String implemented) {
		_implemantations.add(implemented);
	}

	@Override
	public String getSuperClassName() {
		return _superClassName;
	}

	@Override
	public ArrayList<String> getInterfaces() {
		return _implemantations;
	}

	@Override
	public HashMap<String, Variable> getTranslationMap() {
		return _translationMap;
	}

	@Override
	public ArrayList<Variable> getOverrideFunctions() {
		return _overrideFunctions;
	}

	@Override
	public String toString() {
		return "File " + _file.getName() + " 'Class : " + _className + "'";
	}

	@Override
	public void lockRename() {
		_classNameRenamed = true;
	}

	@Override
	public String getPackageName() {
		return _packageName;
	}

	public void setNewLines(ArrayList<Integer> newLines) {
		_newLines = newLines;
	}

	public void setSpaces(ArrayList<Integer> spaces) {
		_spaces = spaces;
	}

}
