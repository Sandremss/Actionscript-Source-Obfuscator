package main;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import packages.PackageAS;
import packages.PackageManager;
import classes.ActionScriptClass;
import classes.renaming.UniqueStringCreator;
import data.RenameObjectCounter;

/**
 * This is the big class that makes it all happen, it calls the most abstract
 * functions that will determine the obfuscation process.<br>
 * The basic Problem Solving goes as follows:<br>
 * <br>
 * 1. Read all classes and get the information we need<br>
 * 2. Internally change this information<br>
 * 3. Write these changes back into the classes.<br>
 * <br>
 * The most important class that is used is {@link ActionScriptClass}, it stores
 * all needed info and makes the further calls to helperclasses to do the rest.
 * 
 * @author sander
 * 
 */
public class Obfuscate implements IGetClass {
	private PackageManager manager;
	private ArrayList<ActionScriptClass> classes;

	public static void main(String[] args) {
		new Obfuscate(args);
	}

	/**
	 * Constructor for command line access Creates an Obfuscate object in
	 * command line mode
	 * 
	 * @param args
	 *            the arguments..
	 */
	public Obfuscate(String[] args) {
		setArgs(args);
		new Obfuscate(false);
	}

	/**
	 * Creates an obfuscate, command line or GUI style
	 * 
	 * @param isGUI
	 *            whether the application is in GUI mode
	 */
	public Obfuscate(boolean isGUI) {
		File file = new File("./in");
		if (!file.exists())
			file.mkdir();

		System.out.println("Default Charset=" + Charset.defaultCharset());
//		System.setProperty("file.encoding", "Latin-1");
		System.out.println("file.encoding=" + System.getProperty("file.encoding"));
		System.out.println("Default Charset=" + Charset.defaultCharset());
		
		
		UniqueStringCreator.setupChars();
		if (UniqueStringCreator.length == -1)
			UniqueStringCreator.length = 4;
		if (!isGUI) {
			System.out.println("Welcome to AS Obfuscator!, put your files in the subfolder ./in");
			//System.out.println("To start press ENTER.....");
			//new Scanner(System.in).nextLine(); // block until new line so user
			// can put stuff in ./in
		}

		parseAllClasses(file);

		if (isGUI)
			return;

		if (renameEverything()) {
			notEnoughNamesError();
		}

		makeChangeNameAndOutput();
		
		UniqueStringCreator.writeDistToFile();
	}

	/**
	 * parses all the classes
	 * 
	 * @param file
	 */
	private void parseAllClasses(File file) {
		classes = new ArrayList<ActionScriptClass>();

		addClasses(classes, file);

		manager = new PackageManager();

		for (ActionScriptClass actionScriptClass : classes) {
			actionScriptClass.parseClass();
			manager.addClass(actionScriptClass);
		}
		// after all classes have been parsed and a package structure has been
		// made, we can safely resolve Asterisk imports
		for (ActionScriptClass actionScriptClass : classes) {
			actionScriptClass.resolveAsteriskImports();
		}
	}

	/**
	 * renames all the classes, first checks if there are enough possible names
	 * in case of unique name request.
	 * 
	 * @return whether there were problems renaming
	 */
	public boolean renameEverything() {
		if (ObfuscationSettings.uniqueNames() && checkPosibilities())
			return true;
		// interfaces have to rename its fields first because of
		// implementations.
		ArrayList<ActionScriptClass> interfaces = new ArrayList<ActionScriptClass>();
		int interfaceIndex = 0;
		int classIndex = 0;
		Iterator<ActionScriptClass> it = classes.iterator();
		while (it.hasNext()) {
			ActionScriptClass interF = it.next();
			if (interF.isInterFace()) {
				it.remove();
				interfaces.add(interF);

				if (ObfuscationSettings.renameClasses())
					interF.renameClassName(classIndex);

				if (ObfuscationSettings.doLocalVars())
					interfaceIndex = interF.renameVariables(interfaceIndex);
			}
		}

		// now the normal classes can do the same
		for (ActionScriptClass actionScriptClass : classes) {
			if (actionScriptClass.getClassName() == null)
			 	continue;

			if (ObfuscationSettings.doLocalVars())
				actionScriptClass.renameVariables();

			if (ObfuscationSettings.renameClasses())
				classIndex = actionScriptClass.renameClassName(classIndex);
		}
		// put the interfaces with the classes
		classes.addAll(interfaces);

		// rename package names
		int packageIndex = 0;
		for (PackageAS packageAS : manager.getPackages()) {
			if (ObfuscationSettings.renamePackages() && packageAS.getName().length() > 0)
				packageIndex = packageAS.renamePackage(packageIndex);
		}

		return false;
	}

	/**
	 * Makes the physical changes on the classes and writes them.
	 */
	public void makeChangeNameAndOutput() {
		System.out.println("start makechange and output!");
		for (ActionScriptClass actionScriptClass : classes) {
			actionScriptClass.renameReferences();
		}

		System.out.println("outputting the files!");

		File out = new File("./out/src");
		if (!out.exists())
			out.mkdirs();

		for (ActionScriptClass actionScriptClass : classes) {			
			actionScriptClass.outClass(out);
		}
	}

	/**
	 * Checks whether there are enough possibilities to rename all fields
	 * 
	 * @return if there is a problem with the available fields
	 */
	private boolean checkPosibilities() {
		System.out.println("check possibilities!");
		if (UniqueStringCreator.length > 10) {
			// if the length is > 10 there will be so many possibilities the
			// long will overflow
			// unless there will be more than 2^63-1 fields its safe to say
			// there are enough possible names
			return false;
		}
		long names = RenameObjectCounter.getCount();
		long space = UniqueStringCreator.getExpectedPossibilities();
		System.out.println("generating: " + space + " unique names for: " + names + " objects");
		return (names > space);
	}

	/**
	 * finds all .AS files in the subdirectories of the file, recursively this
	 * finds all the classes
	 * 
	 * @param classes
	 *            the List on which to add classes
	 * @param file
	 *            the file in which to search
	 */
	private void addClasses(List<ActionScriptClass> classes, File file) {
		if (file.isFile()
				&& file.getName().substring(file.getName().length() - 3, file.getName().length())
						.equalsIgnoreCase(".as")) {
			classes.add(new ActionScriptClass(file, this));
			return;
		}
		if (file.listFiles() == null) {
			return;
		}
		for (File tfile : file.listFiles()) {
			addClasses(classes, tfile);
		}
	}

	private void notEnoughNamesError() {
		System.out.println("ERROR: Cannot rename variables, there are not enough possibilities to create "
				+ RenameObjectCounter.getCount() + " names with only " + UniqueStringCreator.length
				+ " character(s)!!!");
		System.out.println("please retry with a larger number for unique variable length!");
		System.out.println("Press Enter to exit..");
		new Scanner(System.in).nextLine();
		System.exit(0);
	}

	/**
	 * Translates arguments to settings
	 */
	private void setArgs(String[] args) {
		List<String> a = Arrays.asList(args);

		if (a.indexOf("help") >= 0 || a.indexOf("?") >= 0 || a.indexOf("-help") >= 0) {
			System.out.println("---Commands for Obfuscator---");
			System.out.println("-nolocal | don't obfuscate local variables");
			System.out.println("-nopackages | don't obfuscate packages");
			System.out.println("-noclasses | don't obfuscate class names");
			System.out.println("-nouniquenames | don't give every field an unique name");
			System.out
					.println("-namelength <length> | the length of each unique name, you need to also NOT use -uniquenames");
			System.out.println("-help | display this message");
			System.out.println("press ENTER to exit");
			new Scanner(System.in).nextLine();
			System.exit(0);
		}

		int indexOf = a.indexOf("-namelength");
		if (indexOf >= 0) {
			System.out.println("found namelength argument!");
			// if (args.length <= indexOf + 1) {
			// 	System.out.println("missing length argument after -namelength argument!");
			// 	System.out.println("Press ENTER to exit");
			// 	new Scanner(System.in).nextLine();
			// 	System.exit(0);
			// }
			try {
				int length = Integer.parseInt(args[indexOf + 1]);
				UniqueStringCreator.length = length;
			} catch (NumberFormatException e) {
				System.out.println("Error after -namelength argument, cannot parse " + args[indexOf + 1]
						+ " to an integer!");
				System.out.println("Press ENTER to exit");
				new Scanner(System.in).nextLine();
				System.exit(0);
			}
		}
		else
		{
			UniqueStringCreator.length = 10;
		}

		boolean localVars = false; //a.indexOf("-nolocal") == -1;
		boolean packages = a.indexOf("-nopackages") == -1;
		boolean classNames = a.indexOf("-noclasses") == -1;
		boolean uniqueRenaming = a.indexOf("-nouniquenames") == -1;
		System.out.println("unique names!: " + uniqueRenaming);
		ObfuscationSettings.initSettings(new ObfuscationSettings(localVars, packages, classNames, uniqueRenaming));
	}

	@Override
	public ActionScriptClass getClass(String path) {
		return manager.getType(path);
	}

	public PackageManager getPackageManager() {
		return manager;
	}

	@Override
	public ArrayList<ActionScriptClass> getAllClassesFromPackage(String path) {
		for (PackageAS packageAS : manager.getPackages()) {
			if (packageAS.getName().equals(path))
				return packageAS.getClasses();
		}
		return null;
	}

	@Override
	public PackageAS getPackage(String substring) {
		return manager.getPackage(substring);
	}
}
