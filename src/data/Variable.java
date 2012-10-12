package data;

import java.util.ArrayList;

/**
 * This class holds a Variable or a Function, it can also have local variables
 * if it is a function.
 * 
 * @author sander
 * 
 */
//TODO make this an abstract class Member and make subclasses Variable and Function..
public class Variable implements IAddVariable, IRenameLockable {
	private String _variableName;
	private String _oldName;
	private String _type;
	private ArrayList<Variable> _argVars;
	private boolean _renamed;
	private boolean _isOverride;
	private boolean _anonymous = false;

	public Variable(String variableName) {
		RenameObjectCounter.increaseCount(1);
		if (variableName == null) {
			System.err.println("Variable with null in constructor!!!");
			System.exit(0);
		}
		_renamed = false;
		this._variableName = variableName;
		_argVars = new ArrayList<Variable>();
		_oldName = variableName;
	}

	public String getName() {
		return _variableName;
	}

	public void rename(String newName) {
		if (newName == null) {
			System.err.println("RENAMED TO NULL NAME!!, " + _oldName);
			System.exit(0);
		}
		_renamed = true;
		_variableName = newName;
	}

	public String getOldName() {
		return _oldName;
	}

	public void SetType(String type) {
		this._type = type;
	}

	public String getType() {
		return _type;
	}

	@Override
	public String toString() {
		if (_variableName == null) {
			System.err.println("VARIABLE NAME IS NULL!!");
		}
		String out = "variable name: " + _variableName + " | " + _oldName + " Type : " + _type;
		if (!_argVars.isEmpty()) {
			for (Variable var : _argVars) {
				out += "\n\tArgvar: " + var;
			}
		}
		return out;
	}

	private void addArg(Variable argVar) {
		_argVars.add(argVar);
	}

	public ArrayList<Variable> getLocalVariables() {
		return _argVars;
	}

	public boolean isRenamed() {
		return _renamed;
	}

	public void setOverride(boolean b) {
		_isOverride = b;
	}

	public boolean isOverride() {
		return _isOverride;
	}

	public Variable getLocalVar(String varName) {
		for (Variable arg : _argVars) {
			if (arg.getOldName().equals(varName))
				return arg;
		}
		return null;
	}

	@Override
	public void addVariable(Variable variable) {
		addArg(variable);
	}

	@Override
	public void lockRename() {
		_renamed = true;
	}

	public void setAnonymous() {
		_anonymous = true;
		lockRename();
	}

	public boolean isAnomynous() {
		return _anonymous;
	}
}
