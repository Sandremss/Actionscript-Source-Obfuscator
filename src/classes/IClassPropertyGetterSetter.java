package classes;

import java.util.ArrayList;
import java.util.HashMap;

import data.Variable;

/**This interface contains some of the getters and setters of {@link ActionScriptClass}
 * 
 * @author sander
 *
 */
public interface IClassPropertyGetterSetter {
	public void setPackageName(String packageName);

	public void setClassName(String className);

	public void setIsInterFace(boolean isInterFace);

	public void setSuperClassName(String extended);

	public void addInterFace(String implementation);

	public String getFullType(String type);

	public String getSuperClassName();

	public ArrayList<String> getInterfaces();

	public String getPackageName();

	public String getClassName();

	public HashMap<String, Variable> getTranslationMap();

	public ArrayList<Variable> getOverrideFunctions();

	public boolean isInterFace();
}
