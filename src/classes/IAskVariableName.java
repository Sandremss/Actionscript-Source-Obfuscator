package classes;
import data.Variable;

/**This interface represents every object that can turn a varible name into a {@link Variable} object.
 * 
 * @author sander
 *
 */
public interface IAskVariableName {
	Variable askVariable(String variableName);
}
