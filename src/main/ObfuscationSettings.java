package main;

import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

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
	private final List<String> ignoreClassList;
	private final List<String> ignoreMemberName;
	private final List<String> ignorePackage;
	private final List<String> ignoreMemberInClass;
	
	
	public static void initSettings(ObfuscationSettings settings) {
		ObfuscationSettings.settings = settings;		
	}
	
	 
	public ObfuscationSettings(boolean obfuscateLocalVars, boolean obfuscatePackages, boolean obfuscateClassNames, boolean uniqueRenaming) {
		this.obfuscateLocalVars = obfuscateLocalVars;
		this.obfuscateClassNames = obfuscateClassNames;
		this.obfuscatePackages = obfuscatePackages;
		this.uniqueRenaming = uniqueRenaming;
		

		this.ignoreClassList = new ArrayList<String>();
		this.ignoreMemberName = new ArrayList<String>();
		this.ignorePackage = new ArrayList<String>();
		this.ignoreMemberInClass = new ArrayList<String>();

		BufferedReader br;
		File file = new File("ignorepackage.txt");
		
		if (file.exists())
		{
			try {
				br = new BufferedReader(new FileReader(file));
				String st;

				while ((st = br.readLine()) != null)
				{
					st = st.trim();
					this.ignorePackage.add(st);
				}				
				br.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		// this.ignorePackage.add("Logics");
		// this.ignorePackage.add("Logging");
		// this.ignorePackage.add("Externals");
		// this.ignorePackage.add("Logics.Agent");
		// this.ignorePackage.add("Logics.Battle");
		// this.ignorePackage.add("LocalStorages");
		// this.ignorePackage.add("Foundation.Sound");
		// this.ignorePackage.add("Foundation.Fonts");
		// this.ignorePackage.add("Foundation.Timing");
		// this.ignorePackage.add("Foundation.Worker");
		// this.ignorePackage.add("Foundation.Network");
		// this.ignorePackage.add("Foundation.Resources");
		// this.ignorePackage.add("Foundation.LoaderQueue");
		// this.ignorePackage.add("Foundation.SensitiveWord");
		// this.ignorePackage.add("Rendering.Overlayers.HelpTips");
		// this.ignorePackage.add("Processors.Game.Lobby.SocketSpeed");

		file = new File("ignoreclass.txt");
		
		if (file.exists())
		{
			try {
				br = new BufferedReader(new FileReader(file));
				String st;

				while ((st = br.readLine()) != null)
				{
					st = st.trim();
					this.ignoreClassList.add(st);
				}				
				br.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}		
		// this.ignoreClassList.add("TApplication");
		// this.ignoreClassList.add("TUIComponent");
		// this.ignoreClassList.add("TBaseActivity");
		// this.ignoreClassList.add("Oper");
		// this.ignoreClassList.add("TransitionLayerBase");
		// this.ignoreClassList.add("TFont");
		// this.ignoreClassList.add("TCoordinate");
		// this.ignoreClassList.add("THyperStringElement");
		// this.ignoreClassList.add("Back");
		// this.ignoreClassList.add("TProcessorBaseActivity");
		// this.ignoreClassList.add("TUnstreamizer");
		// this.ignoreClassList.add("TOverlayerHelpTips");
		// this.ignoreClassList.add("TProcessorLobbyWindow");
		// this.ignoreClassList.add("TUIBaseWindow");
		// this.ignoreClassList.add("TActive");
		// this.ignoreClassList.add("TStringOperator");		
		// this.ignoreClassList.add("TStringGeometer");
		// this.ignoreClassList.add("TStringParagrapher");
		// this.ignoreClassList.add("TUIRoleCanMove");
		

		file = new File("ignorevarname.txt");
		
		if (file.exists())
		{
			try {
				br = new BufferedReader(new FileReader(file));
				String st;

				while ((st = br.readLine()) != null)
				{
					st = st.trim();
					this.ignoreMemberName.add(st);
				}				
				br.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}		

		file = new File("ignorerenamememberinclass.txt");
		
		if (file.exists())
		{
			try {
				br = new BufferedReader(new FileReader(file));
				String st;

				while ((st = br.readLine()) != null)
				{
					st = st.trim();
					this.ignoreMemberInClass.add(st);
				}				
				br.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}		

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
	
	
	public static boolean isIgnoredClass(String classname)
	{
		if (classname == null)
			return true;
		return settings.ignoreClassList.indexOf(classname) > -1;
	}
	
	public static boolean isIgnoredMemberName(String classname)
	{
		return settings.ignoreMemberName.indexOf(classname) > -1;
	}
	
	public static boolean isIgnoredMemberInClass(String classname)
	{
		return settings.ignoreMemberInClass.indexOf(classname) > -1;
	}

	public static boolean isIgnorePackage(String packagename)
	{
		if (packagename == null || packagename.length() == 0)
			return true;
		return settings.ignorePackage.indexOf(packagename) > -1;		
	}

}
