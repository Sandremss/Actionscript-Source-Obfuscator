package classes.renaming;

import java.util.ArrayList;
import java.util.HashMap;

import main.IGetClass;
import main.ObfuscationSettings;
import classes.ActionScriptClass;
import classes.IClassPropertyGetterSetter;
import data.Variable;

/**
 * Renames variables and functions of a class. In needs to make sure that it
 * obeys Interfaces and Superclasses.
 * 
 * @author sander
 * 
 */
public class MemberRenamer {

	/**
	 * Renames all the members of a class.
	 * 
	 * @param index
	 *            The index of the variables for when these variables cannot be
	 *            isolated with incremental renaming.
	 * @param members
	 *            a list of variables
	 * @param asClass
	 *            the calling class
	 * @param model
	 *            the model which other classes can be asked
	 * @return the index after all variables are renamed
	 */
	public static int renameAllMembers(int index, ArrayList<Variable> members, IClassPropertyGetterSetter asClass,
			IGetClass model) {
		// first get the full path of each type on each variable, for example
		// Sprite to flash.display.Sprite
		for (Variable variable : members) {
			variable.SetType(asClass.getFullType(variable.getType()));
			ArrayList<Variable> locals = variable.getLocalVariables();
			for (Variable localVar : locals) {
				localVar.SetType(asClass.getFullType(localVar.getType()));
				checkVectorException(localVar, asClass);
			}
			checkVectorException(variable, asClass);
		}

		ActionScriptClass superClass = model.getClass(asClass.getFullType(asClass.getSuperClassName()));

		// superclass has to rename first
		if (superClass != null)
			superClass.renameVariables();

		// collect all variables that are already renamed and we should not come
		// up with a create new name for
		ArrayList<Variable> preRenamed = new ArrayList<Variable>();
		for (String implemented : asClass.getInterfaces()) {
			ActionScriptClass myInterface = model.getClass(asClass.getFullType(implemented));
			if (myInterface == null)
				rageQuit("Interal error, Interface " + implemented + " is null in " + asClass.getPackageName() + "."
						+ asClass.getClassName());
			myInterface.getPreRenamed(preRenamed);
		}

		ArrayList<String> takenNames = new ArrayList<String>();

		HashMap<String, Variable> translationMap = asClass.getTranslationMap();

		for (Variable renamed : preRenamed) {
			Variable sameName = translationMap.get(renamed.getOldName());
			if (sameName != null) {
				sameName.rename(renamed.getName());
				takenNames.add(sameName.getName());
			}
		}
		if (superClass != null)
			for (Variable variable : superClass.getVariables()) {
				takenNames.add(variable.getName());
			}
		ArrayList<Variable> overrideFunctions = asClass.getOverrideFunctions();
		if (superClass != null) {
			for (Variable override : overrideFunctions) {
				override.rename(superClass.askVariable(override.getOldName()).getName());
				takenNames.add(override.getName());
			}
		} else {
			// the superclass of this class is not in the renamed list, so these
			// names should be left alone
			for (Variable override : overrideFunctions) {
				override.lockRename();
			}
		}

		/*
		 * Although this does make the obfuscation less intense, it resolves a
		 * collision where the interface member and superclass member have the
		 * same name, this could have been resolved by a second rename on that
		 * particular variable on the superclass and then updating that change
		 * to all of the subclasses (hoping no new collisions occur because of
		 * that), but this is easier; the difference wouldn't be worth the
		 * trouble. [ONLY FOR INCREMENTAL REMANING]
		 */
		String renamePrefix = (asClass.isInterFace() ? "i" : "_");

		int i = index;
		for (Variable variable : members) {
			if (variable.isRenamed())
				continue;
			String newName = null;

			if (!variable.getName().equals(asClass.getClassName())) {
				// loop used for incremental renaming only
				while (newName == null || takenNames.indexOf(newName) >= 0)
					newName = renamePrefix + i++;
				if (ObfuscationSettings.uniqueNames())
					variable.rename(UniqueStringCreator.getUniqueName(RenameType.VARIABLE));
				else
					variable.rename(newName);
			}

		}

		if (!ObfuscationSettings.doLocalVars())
			return i;

		for (Variable variable : members) {
			int p = 0;
			for (Variable local : variable.getLocalVariables()) {

				if (ObfuscationSettings.uniqueNames())
					local.rename(UniqueStringCreator.getUniqueName(RenameType.VARIABLE));
				else
					local.rename("l" + p);
				p++;
			}
		}

		return i;
	}

	/**
	 * If the variable is a Vector, it will get the full type of the full type
	 * of the type of the vector, and store it back in there.
	 * 
	 * @param variable
	 * @param asClass
	 */
	private static void checkVectorException(Variable variable, IClassPropertyGetterSetter asClass) {
		// This is acually sort of a bug... a type can be something like:
		// var a : flash.display.Sprite
		// and it has to find the complete name with dots. As far as I know is
		// Vector the only class that can have a '.' directly after it so we'll
		// use this to our advantage in this case.
		if (variable.getType().equals("Vector.<")) {
			variable.setVectorType(asClass.getFullType(variable.getVectorType()));
		}
	}

	/**
	 * exits the program with anger
	 */
	private static void rageQuit(String string) {
		System.out.println(string);
		System.exit(0);
	}

}
