----------------------------------------
Actionscript 3.0 Source Code Obfuscator
----------------------------------------

Obfuscate your actionscript project with this Obfuscator. This Actionscript Obfuscator obfuscates:
- package names
- class names
- variable names
- function names

These changes will be updated on any other Actionscript files you include in the Obfuscation process!



------------------------
How to run the .jar file
------------------------

You need have a Java runtime installed. This Project uses JavaSE1-6

Double click (Windows)


Run from Terminal:

**First go to the Directory where Obfuscator.jar is at!!**
For example if your Obfuscator.jar is at /home/username/Downloads/obfustest/ you should run:

	$ cd /home/username/Downloads/obfustest/

When you are at the right directory you can run the commands below.


GUI Mode:

	$ java -jar Obfuscator.jar 

Terminal Mode:

	$ java -cp Obfuscator.jar main.Obfuscate <args>

Arguments:

1. -nolocal | don't obfuscate local variables
2. -nopackages | don't obfuscate packages
3. -noclasses | don't obfuscate class names
4. -uniquenames | give every field an unique name
5. -namelength <length> | the length of each unique name, you need to also use -uniquenames
6. -help | display available commands

Settings files: Not required, MUST be stored in the working dir
* ignoreclass.txt: List of class won't be renamed
* ignorepackage.txt: List of package won't be renamed, all sub package and class inside this package still be renamed.
* ignorerenamememberinclass.txt: List of class which all public and protected function, public variable won't be renamed. Private function, private and protected properties still be ranmed.
* ignorevarname.txt: List of variable name, function name which won't be renamed. This affact for all public, protected and private functions/porperties.

**Tips n Tricks:**

- When using unique names you don't need a large number for the name length, a length of 4 already gives 13 million possibilities.

- If you want individual classes, packages, or variables not obfuscated you have to use the GUI mode and deselect them.

- The more .AS files it has the more it can obfuscate, as it works with references. If you make use of a library and have the source code of it, you should include those .as files to get better obfuscation.

- The obfuscation will break on dynamically typed fields, always type those things or exclude them from the renaming process.
**!!!NOTE!!!** THIS WILL GENERATE RUNTIME ERRORS OTHERWISE!

so change:

	array[0].unsafeCall();

to:

	MyClass( array[0] ).safeCall

- **Local / anymous functions explained:**

Local and anymous functions won't have their variables renamed, this is not a problem as your swf will not store these names. However it may generate name conflicts. Take a look at the following example:

	class A {
		var a:int, b:int;
		public function A() {
			var a:String;
			a = "a"; //won't be renamed (in nolocalvar mode)
			this.a = 4; //will be renamed
			var c = function() {
				var b:String = "hi"; //!!!WILL BE RENAMED!!!
			}
		}
	}
Keep in mind that when local variables have the same name as global variables, it is OK as long as these local variables are in a normal function.

- If you receive an error try running the program with the command line in order to see debug traces and get an error report.

----------
Updates

- 27 October 2022:  
					+ Support Namespaces, support file without having any class
					+ Support regular expression
					+ Support load ignore list from:
						ignoreclass.txt: All class in this file will be keep it's name
						ignorepackage.txt: All packages in this file will be keep as is
						ignorerenamememberinclass.txt: All member (methods, properties, local variable) will be keep as is
						ignorevarname.txt:	All methods, properties, local variable name in this file will be keep as is
					+ Fix many other errors
- 12 October 2012:	Make the Obfuscator ignore Anonymous functions instead of terminate.
- 14 October 2012:	Added Vector Type safety support.

------------
Unsupported:
------------

1.	The 'with' statement, will generate errors
		Its slow and unhandy to read because of the extra { it creates
2.	mxml, will not get parsed; only .as files
		This is an actionscript obfuscator, not a UI obfuscator.
3.	Local functions and anymous functions, may generate errors
		there is a chance where there can be collisions with names.
4.	Dynamically typed members, may generate errors
		Without a type the variable or function cannot be referenced correctly, look out for warnings when you compile your original code for this.
5.	Global function or field in separate .as file (global to the package)
6.	Reflection, variable names will get changed.
7.	Public/protected get magic method, such as public function get Width()
8.	Multi level methods/properties calling in some case
		Such as: 
			ret = SLogicsCore.Character.Appliances.GetAll()
		You should convert it to:
			var ch: CCharacter = SLogicsCore.Character;
			var app: CAppliances = ch.Appliances();
			ret = app.getAll();


