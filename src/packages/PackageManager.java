package packages;
import java.util.ArrayList;
import java.util.HashMap;

import classes.ActionScriptClass;

/**
 * Manages the packages
 * @author sander
 *
 */
public class PackageManager {
	/**
	 * The HashMap which returns a {@link PackageAS} object with the package path.
	 */
	private HashMap<String, PackageAS> packageMap;
	/**an ArrayList used to iterate over the packages, added later for GUI visualization of classes */
	private ArrayList<PackageAS> packageList;

	public PackageManager() {
		packageMap = new HashMap<String, PackageAS>();
		packageList = new ArrayList<PackageAS>();
	}

	public void addClass(ActionScriptClass classToAdd) {
		String packageName = classToAdd.getPackageName();
		PackageAS p = packageMap.get(packageName);
		if (p == null) {
			p = new PackageAS(packageName);
			packageMap.put(packageName, p);
		}
		p.addClass(classToAdd);
		if(packageList.indexOf(p) == -1) {
			packageList.add(p);
		}
	}
	
	public ArrayList<PackageAS> getPackages() {
		return packageList;
	}

	public ActionScriptClass getType(String pathName) {
		String packageName = "";
		if (pathName.indexOf(".") >= 0) {
			packageName = pathName.substring(0, pathName.lastIndexOf("."));
		}
		String className = pathName.substring(pathName.lastIndexOf(".") + 1);
		PackageAS p = packageMap.get(packageName);
		if (p == null) {
			return null;			
		}
		return p.getActionScriptClass(className);
	}

	public PackageAS getPackage(String packageName) {
		return packageMap.get(packageName);
	}
}
