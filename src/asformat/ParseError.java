package asformat;
/**
 * Lets user know of parsing error and can exit the program.
 * @author sander
 *
 */
public class ParseError {
	public static void rageQuit(String string, CodeParser parser){
		System.out.println(string);
		System.out.println(parser.errorInfo());
		System.exit(0);
	}

}
