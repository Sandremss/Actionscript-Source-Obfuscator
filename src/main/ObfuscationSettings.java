package main;

import java.util.List;
import java.util.Map;
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
	private final List<String> needReCorrect;
	
	
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
		this.needReCorrect = new ArrayList<String>();

		this.ignorePackage.add("Logics");
		this.ignorePackage.add("Logging");
		this.ignorePackage.add("Externals");
		this.ignorePackage.add("Logics.Agent");
		this.ignorePackage.add("Logics.Battle");
		this.ignorePackage.add("LocalStorages");
		this.ignorePackage.add("Foundation.Sound");
		this.ignorePackage.add("Foundation.Fonts");
		this.ignorePackage.add("Foundation.Timing");
		this.ignorePackage.add("Foundation.Worker");
		this.ignorePackage.add("Foundation.Network");
		this.ignorePackage.add("Foundation.Resources");
		this.ignorePackage.add("Foundation.LoaderQueue");
		this.ignorePackage.add("Foundation.SensitiveWord");
		this.ignorePackage.add("Rendering.Overlayers.HelpTips");
		this.ignorePackage.add("Processors.Game.Lobby.SocketSpeed");

		this.ignoreClassList.add("TApplication");
		this.ignoreClassList.add("TUIComponent");
		this.ignoreClassList.add("TBaseActivity");
		this.ignoreClassList.add("Oper");
		this.ignoreClassList.add("TransitionLayerBase");
		this.ignoreClassList.add("TFont");
		this.ignoreClassList.add("TCoordinate");
		this.ignoreClassList.add("THyperStringElement");

		this.ignoreClassList.add("Back");
		this.ignoreClassList.add("TProcessorBaseActivity");
		this.ignoreClassList.add("TUnstreamizer");
		this.ignoreClassList.add("TOverlayerHelpTips");
		this.ignoreClassList.add("TProcessorLobbyWindow");
		this.ignoreClassList.add("TUIBaseWindow");
		this.ignoreClassList.add("TActive");
		this.ignoreClassList.add("TStringOperator");		
		this.ignoreClassList.add("TStringGeometer");
		this.ignoreClassList.add("TStringParagrapher");
		this.ignoreClassList.add("TUIRoleCanMove");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		//this.ignoreClassList.add("");
		
		// this.ignoreMemberName.add("ShortcutModesReset");
		// this.ignoreMemberName.add("FreeInsideItems");
		// this.ignoreMemberName.add("FreeOutsideItems");
		// this.ignoreMemberName.add("Low");
		// this.ignoreMemberName.add("High");
		// this.ignoreMemberName.add("ConstructShortcutModes");
		// this.ignoreMemberName.add("OnTaskClick");
		// this.ignoreMemberName.add("KillHeroId");
		// this.ignoreMemberName.add("Level");
		// this.ignoreMemberName.add("NeedExp");
		// this.ignoreMemberName.add("execute");
		// this.ignoreMemberName.add("end");
		// this.ignoreMemberName.add("WAIT");
		// this.ignoreMemberName.add("NONE");
		// this.ignoreMemberName.add("commit");
		// this.ignoreMemberName.add("halt");
		// this.ignoreMemberName.add("IsOn");
		// this.ignoreMemberName.add("EndTime");
		// this.ignoreMemberName.add("DayTime");
		// this.ignoreMemberName.add("CurStatus");
		// this.ignoreMemberName.add("Status");
		// this.ignoreMemberName.add("Count");
		// this.ignoreMemberName.add("Max");
		// this.ignoreMemberName.add("Min");
		// this.ignoreMemberName.add("THyperStringElement");
		// this.ignoreMemberName.add("Desc");
		// this.ignoreMemberName.add("RenderingPerform_Gauge");
		// this.ignoreMemberName.add("createTo");
		// this.ignoreMemberName.add("state");
		// this.ignoreMemberName.add("destory");
		// this.ignoreMemberName.add("StartTime");
		// this.ignoreMemberName.add("Dereference");
		// this.ignoreMemberName.add("Reference");
		// this.ignoreMemberName.add("GetInventoryByIndex");
		// this.ignoreMemberName.add("Value");
		//this.ignoreMemberName.add("");
		//this.ignoreMemberName.add("");
		//this.ignoreMemberName.add("");
		//this.ignoreMemberName.add("");
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

	public static boolean isIgnorePackage(String packagename)
	{
		if (packagename == null || packagename.length() == 0)
			return true;
		return settings.ignorePackage.indexOf(packagename) > -1;		
	}

}
