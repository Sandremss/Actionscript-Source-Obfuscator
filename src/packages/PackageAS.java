package packages;

import java.util.ArrayList;

import main.ObfuscationSettings;
import classes.ActionScriptClass;
import classes.renaming.RenameType;
import classes.renaming.UniqueStringCreator;
import data.IRenameLockable;
import data.RenameObjectCounter;
import java.util.Random;

/**
 * Holds the information that needs to be stored for each package.
 * @author sander
 *
 */
public class PackageAS implements IRenameLockable {
	private ArrayList<ActionScriptClass> _classes;
	private final String _name;
	private String _newName;
	private boolean _renamed = false;

	public PackageAS(String name) {
		RenameObjectCounter.increaseCount(1);
		this._name = name;
		_newName = name;
		_classes = new ArrayList<ActionScriptClass>();
		if (ObfuscationSettings.isIgnorePackage(name))
		{
			this.lockRename();
		}
	}

	public void addClass(ActionScriptClass classToAdd) {
		//DuongTC
		// if (classToAdd.getClassName() == null)
		// 	return;
		_classes.add(classToAdd);
		if (_renamed)
			classToAdd.setNewPackageName(_name);
	}

	public String getName() {
		return _name;
	}

	public ActionScriptClass getActionScriptClass(String className) {
		for (ActionScriptClass a : _classes) {
			if (a != null)
			{
				String fClassName = a.getClassName();

				if (fClassName != null)
				{
					if (fClassName.equals(className))
						return a;
				}
				// else
				// {
				// 	System.out.println("Found file without class: " + a.toString());
				// 	System.out.println("Please correct it");
				// 	System.exit(0);
				// }
			}
		}
		return null;
	}

	public ArrayList<ActionScriptClass> getClasses() {
		return _classes;
	}

	public String getNewName() {
		return _newName;
	}

	public int renamePackage(int i) {
		if (_renamed)
			return i;

		if (ObfuscationSettings.uniqueNames())
			_newName = UniqueStringCreator.getUniqueName(RenameType.PACKAGENAME, getName());
		else
		{
			Random rand = new Random();
			i += rand.nextInt(10);
			_newName = "P" + i;
		}


		for (ActionScriptClass asClass : _classes) {
			asClass.setNewPackageName(_newName);
		}
		return i;
	}

	@Override
	public void lockRename() {
		_renamed = true;
		for (ActionScriptClass asClass : _classes) {
			asClass.setNewPackageName(_name);
		}
	}

}
