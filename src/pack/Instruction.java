package pack;

/**
 * Instruction - abstract class used to derive program functions
 *
 ***************************************************************************************************
 *                      @author Vagner Machado - QC ID 23651127 - Fall 2019
 ***************************************************************************************************
 *
 */
public abstract class Instruction 
{
	/**
	 * print - used to display the instruction data
	 * @return - a string with function data
	 */
	public abstract String print();	
	
	/**
	 * getValue - accessor for value stored in Instruction object
	 * @return - the value stored in object.
	 */
	public abstract Object getValue();
	
}
