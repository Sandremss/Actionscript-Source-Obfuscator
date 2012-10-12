package main;

/**Stores some settings about the obfuscation process that are globally accessible
 * 
 * @author sander
 *
 */
public class ObfuscationSettings {
	
	private static ObfuscationSettings settings;
	private final boolean obfuscateLocalVars;
	private final boolean obfuscateClassNames;
	private final boolean obfuscatePackages ;
	private final boolean uniqueRenaming;
	
	public static void initSettings(ObfuscationSettings settings) {
		ObfuscationSettings.settings = settings;
	}
	
	 
	public ObfuscationSettings(boolean obfuscateLocalVars, boolean obfuscatePackages, boolean obfuscateClassNames, boolean uniqueRenaming) {
		this.obfuscateLocalVars = obfuscateLocalVars;
		this.obfuscateClassNames = obfuscateClassNames;
		this.obfuscatePackages = obfuscatePackages;
		this.uniqueRenaming = uniqueRenaming;
	}
	
	public static boolean doLocalVars(){
		return settings.obfuscateLocalVars;
	}


	public static boolean uniqueNames() {
		return settings.uniqueRenaming;
	}


	public static boolean renameClasses() {
		return settings.obfuscateClassNames;
	}


	public static boolean renamePackages() {
		return settings.obfuscatePackages;
	}
	
	

}
