package main;

import java.util.ArrayList;

import packages.PackageAS;
import classes.ActionScriptClass;

/**
 * Originally used as interface for the main Obfuscator class to get
 * actionscript classes, now also used for asterisk imports with
 * getAllClassesFromPackage and getPackage
 * 
 * @author sander
 * 
 */
public interface IGetClass {
	public ActionScriptClass getClass(String path);

	public ArrayList<ActionScriptClass> getAllClassesFromPackage(String path);

	public PackageAS getPackage(String substring);
}
