package pack;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;


/*************************************************************************************
 The objective of Project 3 is to build upon project 2, which purpose was to
 implement a top-down parser and an instruction store for our VM language. 

 Project 3 will contains a runtime Stack that holds Frame Objects. These contain
 an operand Stack and memory locations that will be used for processing the output given by
 project 2 via a stack based virtual machine. 

 *************************************************************************************

 Please see documentations for class Frame for semantics and virtual machine documentation
 used to implement the project 3.

 *************************************************************************************
 The EBNF for project 2 and other information is given below.

<instruction list> -> { <instruction unit> }+
<instruction unit> -> [ <label> ] <instruction>
<label> -> <unsigned int> ":"
<instruction> ->
       "iconst" <unsigned int> | "iconst" <signed int> | "iload" <unsigned int> |
        "istore" <unsigned int> | "fconst" <float> | "fconst" <floatE> | 
        "fload" <unsigned int> | "fstore" <unsigned int> |
       "iadd" | "isub" | "imul" | "idiv" | "fadd" | "fsub" | "fmul" | "fdiv" |
       "intToFloat" |
       	<cmp inst name> <unsigned int> |
       "goto" <unsigned int> |
       "invoke" <unsigned int> "," <unsigned int> "," <unsigned int> |
       "return" | "ireturn" | "freturn" |
       "print" <unsigned int>
	        <cmp inst name> ->
       "icmpeq" | "icmpne" | "icmplt" | "icmple" | "icmpgt" | "icmpge" |
       "fcmpeq" | "fcmpne" | "fcmplt" | "fcmple" | "fcmpgt" | "fcmpge"

 **************************************************************************************
    Some parameters for the program implementation:

    1. Implementation for the signed/unsigned integers is 32-bit int type, floating-point numbers
       is double-precision 64-bit double type.

    2. Labels may be any non-negative integers and may occur in any order in the instruction list. 
       They must, however, obey the following "semantic rules":
	       a. No label may occur more than once in an instruction list.
           b. The target label of each comparison-jump, goto, and invoke instruction must occur in 
              the instruction list.
           c. If these rules are violated, your program issues appropriate error messages.

    3. The labels used in the instruction list, including the target labels in comparison-jump, goto, 
       and invoke instructions, are mapped to the corresponding indexes of the instruction array.

 ********************************************* INPUT / OUTPUT **********************************************
 *  
 *  This program requires input as command line arguments in order to run properly:
 *  
 *  argv[0] - must be a valid file name with input to be lexically analyzed, parsed and executed
 *  argv[1] - a valid file name for the parsed output to be written to
 *  argv[2] - OPTIONAL: a file with expected parsing output to be compared to generated output
 *            for details, check the Javadocs for comapareOutput() in Stream.java
 *            
 ****************************************************************************************************************
 *WARNING: Using argv[2] checks for EXACT match and HALTS program when first space delimited mismatch is found
 *****************************************************************************************************************
 *
 *************************************************************************************************************
 *                      @author Vagner Machado - QC ID 23651127 - Fall 2019
 *************************************************************************************************************
 */
public class VM extends LexVM
{
	//Project 2 variables
	protected static HashMap<Integer, Integer> jumpMap = new HashMap<Integer, Integer>();
	protected static Instruction [] instructionArray = new Instruction[1000];
	protected static int arrayLocation = 0; //for printing the array
	private static HashMap<Integer, String> compareTarget = new HashMap<Integer,String>(); //used to check for label existence
	private static ArrayList<Integer> gotoTarget = new ArrayList<Integer>(); //used to check for label existence
	private static ArrayList<Integer> invokeTarget = new ArrayList<Integer>(); //used to check for label existence

	//Project 3 variables
	protected static Stack <Frame> runtimeStack = new Stack<Frame>();
	protected static int maxRuntimeStack = 0;
	protected static Stack<Data> operandStack = new Stack<Data>();
	protected static int maxOperandStack = 0;
	protected static int programCounter = 0;


	/**
	 * main - parses and executed lexically correct input in file argument args[0] and saves the parsed 
	 * output in args[1]
	 * @param args - an array of Strings
	 */
	public static void main(String[] inputFile) 
	{	
		//System.out.println("\n************ LEXICAL ANALYSIS ************\n");
		if(parseInput(inputFile))
		{
			//System.out.println("\n************ VIRTUAL MACHINE EXECUTION ************\n");
			Frame main = new Frame(new Data[50], null);
			runtimeStack.push(main);
			if(runtimeStack.size() > maxRuntimeStack)
				maxRuntimeStack = runtimeStack.size();
			main.run();
			System.out.println("Operand Stack Peak Size = " + maxOperandStack);
			System.out.println("Runtime Stack Peak Size = " + maxRuntimeStack);
			//System.out.println("\n** Virtual machine execution has ended **\n");	
		}
		else
		{
			System.out.println("\n** The virtual machine cannot execute because of error(s) on input **\n");
		}
	}

	/**
	 * parseInput - performs all actions needed to accomplish Project 2:
	 * It reads from input file, validates input, instantiates Instructions
	 * and maps the jump targets to file locations.
	 * @param args - argument array passed as param to program.
	 * @return - false if parsing failed due to syntax error, true otherwise
	 */
	public static boolean parseInput(String [] args)
	{
		boolean labelError = false; // output 7 expects multiple repeated labels to be printed, this boolean controls that
		boolean invokeError = false; //used to detect undefined target for invoke
		boolean gotoError = false; //used to detect undefined target for goto
		boolean compareError = false; //used to detect undefined target for goto

		//checks for the minimum number of arguments
		if(args.length < 2)
		{
			paramWarning();
			return false;
		}
		// argv[0]: input file containing tokens defined above
		// argv[1]: output file displaying a list of the tokens and categories
		//initialize a Stream with files passed as arguments
		Stream.setStream(args[0], args[1]);

		String val = getToken();
		String colon = "";

		// no input or bad first token
		if (val == null)
		{
			Stream.close();
			return false; 

		}
		//iterates through all the tokens and instantiates instruction objects based on DFA state
		while(val != null)
		{
			/* *************************************************************************************
			 * switch on all valid states, default handles invalid states. Inside each state, if needed,
			 * extra tokens are extracted and validated. If validation fails, an error message is printed
			 * to the output file, otherwise an Instruction is instantiated and added to the array.
			 * each case is self explanatory based on the print statements, so comments won't be
			 * added. All cases follow the structure just mentioned. Cases end on line 590.
			 **************************************************************************************/
			switch(state) 
			{
			//it says in instructions that labels can be any non negative, so coded to accept <signed int>  >=  0.
			case SignedInt:  //both signed unsigned can be deal in same case
			case UnsignedInt:
			{
				int target = Integer.parseInt(val);
				if (target < 0)
				{
					Stream.displayln("Syntax Error: Label cannot be negative, parser extracted \"" + target + "\"\n");
					Stream.close();
					displayErrorsOnConsole(args[1]);

					return false;
				}
				else
				{
					colon = getToken();
					if(colon == null || !LexVM.state.equals(State.Colon) )
					{
						Stream.displayln("Syntax Error: Label expects \":\", parser extracted \"" + colon + "\"\n");
						Stream.close();
						displayErrorsOnConsole(args[1]);
						return false;
					}
					else
					{
						if(jumpMap.containsKey(target))
						{
							Stream.displayln("Syntax Error: The label \"" + val + "\" appears more than once");
							labelError = true;
						}
						else
							jumpMap.put(target, arrayLocation);
					}
				}
				break;
			}
			case iconst:
			{
				val = getToken();
				if(state.equals(State.SignedInt) || state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Iconst(Integer.parseInt(val));
				}
				else
				{
					Stream.displayln("Syntax Error: iconst expects an integer, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}
			case iload:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Iload(Integer.parseInt(val));
				}
				else
				{
					Stream.displayln("Syntax Error: iload expects an unsigned integer, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}
			case istore:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Istore(Integer.parseInt(val));
				}
				else
				{
					Stream.displayln("Syntax Error: istore expects an unsigned integer, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}
			case fconst:
			{
				val = getToken();
				if(state.equals(State.FloatE) || state.equals(State.Float))
				{
					instructionArray[arrayLocation++] = new Fconst(Double.parseDouble(val));
				}
				else
				{
					Stream.displayln("Syntax Error: fconst expects a float, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}
			case fload:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Fload(Integer.parseInt(val));
				}
				else
				{
					Stream.displayln("Syntax Error: fload expects an unsigned integer, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}
			case fstore:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Fstore(Integer.parseInt(val));
				}
				else
				{
					Stream.displayln("Syntax Error: fstore expects an unsigned integer, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}
			case iadd:
			{
				instructionArray[arrayLocation++] = new Iadd();
				break;
			}
			case isub:
			{
				instructionArray[arrayLocation++] = new Isub();
				break;
			}
			case imul:
			{
				instructionArray[arrayLocation++] = new Imul();
				break;
			}
			case idiv:
			{
				instructionArray[arrayLocation++] = new Idiv();
				break;
			}
			case fadd:
			{
				instructionArray[arrayLocation++] = new Fadd();
				break;
			}
			case fsub:
			{
				instructionArray[arrayLocation++] = new Fsub();
				break;
			}
			case fmul:
			{
				instructionArray[arrayLocation++] = new Fmul();
				break;
			}
			case fdiv:
			{
				instructionArray[arrayLocation++] = new Fdiv();
				break;
			}
			case intToFloat:
			{
				instructionArray[arrayLocation++] = new IntToFloat();
				break;
			}
			case ireturn:
			{
				instructionArray[arrayLocation++] = new Ireturn();
				break;
			}
			case Return:
			{
				instructionArray[arrayLocation++] = new Return();
				break;
			}
			case freturn:
			{
				instructionArray[arrayLocation++] = new Freturn();
				break;
			}
			case Goto:
			{
				val = getToken();
				if( state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Goto(Integer.parseInt(val));
					gotoTarget.add(Integer.parseInt(val));
				}
				else
				{
					Stream.displayln("Syntax Error: Goto expects an unsigned integer, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}
			case print:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Print(Integer.parseInt(val));
				}
				else
				{
					Stream.displayln("Syntax Error: Print expects an unsigned integer, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}
			case invoke:
			{
				int [] params = new int[3];
				int index = 0;
				val = getToken();
				while(index < 3)
				{

					if(state.equals(State.UnsignedInt))
					{
						params[index++] = Integer.parseInt(val);
					}
					else
					{
						Stream.displayln("Syntax Error: Invoke expects unsigned integer parameters, parser extracted \"" + val + "\"");
						Stream.close();
						displayErrorsOnConsole(args[1]);
						return false;
					}
					if(index != 3)
					{
						val = getToken();
						if(!state.equals(State.Comma))
						{
							Stream.displayln("Syntax Error: Invoke expects \",\", parser extracted \"" + val + "\"");
							Stream.close();
							displayErrorsOnConsole(args[1]);
							return false;
						}
						val = getToken();
					}

				}
				instructionArray[arrayLocation++] = new Invoke(params[0], params[1], params[2]);
				invokeTarget.add(params[0]);
				break;
			}
			case icmpeq:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Icmpeq(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "icmpeq");		
				}
				else
				{
					Stream.displayln("Syntax Error: icmpeq expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}

			case icmpne:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Icmpne(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "icmpne");	
				}
				else
				{
					Stream.displayln("Syntax Error: icmpne expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}				
				break;
			}

			case icmplt:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{						
					instructionArray[arrayLocation++] = new Icmplt(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "icmplt");
				}
				else
				{
					Stream.displayln("Syntax Error: icmplt expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}

			case icmple:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Icmple(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "icmple");
				}
				else
				{
					Stream.displayln("Syntax Error: icmple expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}

			case icmpgt:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Icmpgt(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "icmpgt");
				}
				else
				{
					Stream.displayln("Syntax Error: icmpgt expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}

			case icmpge:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Icmpge(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "icmpge");
				}
				else
				{
					Stream.displayln("Syntax Error: icmpge expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}
			case fcmpeq:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Fcmpeq(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val),"fcmpeq");
				}
				else
				{
					Stream.displayln("Syntax Error: fcmpeq expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}

				break;
			}

			case fcmpne:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Fcmpne(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "fcmpne");
				}
				else
				{
					Stream.displayln("Syntax Error: fcmpne expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}				
				break;
			}

			case fcmplt:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Fcmplt(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "fcmplt");
				}
				else
				{
					Stream.displayln("Syntax Error: fcmplt expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}

			case fcmple:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Fcmple(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "fcmpeq");
				}
				else
				{
					Stream.displayln("Syntax Error: fcmple expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}

			case fcmpgt:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Fcmpgt(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "fcmpgt");
				}
				else
				{
					Stream.displayln("Syntax Error: fcmpgt expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}

			case fcmpge:
			{
				val = getToken();
				if(state.equals(State.UnsignedInt))
				{
					instructionArray[arrayLocation++] = new Fcmpge(Integer.parseInt(val));
					compareTarget.put(Integer.parseInt(val), "fcmpge");
				}
				else
				{
					Stream.displayln("Syntax Error: fcmpge expects an unsigned integer jump target, parser extracted \"" + val + "\"");
					Stream.close();
					displayErrorsOnConsole(args[1]);
					return false;
				}
				break;
			}

			default:
				Stream.displayln("Syntax Error:  Unexpected state \"" + state.toString() +"\" reached while parsing \"" +  val + "\"");
				Stream.close();
				displayErrorsOnConsole(args[1]);
				return false;
			}
			val = getToken();
		}

		//returns false if there are lexical errors
		if(Stream.IsStreamAvailable())
			{/*System.out.println("** The input does not have lexical errors **\n");*/}
		else
		{
			System.out.println("** The input has lexical errors **\n");
			return false;
		}
		//System.out.println("** Lexical Analysis has ended **\n");
			
		//System.out.println("\n************ PARSING ************\n");
		
		//System.out.println("** Parsed the input **\n");
		//checks if all targets are valid
		invokeError = checkTarget(invokeTarget,jumpMap,"invoke");
		compareError = checkTarget(compareTarget,jumpMap);
		gotoError = checkTarget(gotoTarget,jumpMap,"goto");

		//checks for the existence of target or label errors
		if (labelError || invokeError || compareError || gotoError)
		{
			Stream.close();
			displayErrorsOnConsole(args[1]);
			return false;
		}
		
		//System.out.println("** Printing Map to console **\n");
		//for (HashMap.Entry<Integer,Integer> entry : jumpMap.entrySet())  
		//System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue()); 
		//System.out.println();

		//System.out.println("** Retargeting jump labels using the Map **\n");
		refractJumpTargets();

		//System.out.println("** Printing the instruction array to file **\n");
		printInstructionArray();
		Stream.close();

		if(args.length == 3)
		{
			//System.out.println("** Comparing generated parsing output to expected parsing output**\n");
			Stream.compareOutputs(args[1], args[2]);
		}
		else
			System.out.println("** File with expected parsing output not provided as args[2], check " + 
					args[1] + " for program generated parsing output **\n");

		//System.out.println("** Parsing has ended **\n");
		return true;
	}

	/**
	 * refractJumpTargets - changes the label for Goto, Compare and Invoke objects
	 * based on the map created during parsing of input file.
	 */
	public static void refractJumpTargets()
	{
		for (int i = 0; i < arrayLocation; i++)
		{
			if (instructionArray[i] instanceof Goto)
				((Goto) instructionArray[i]).refract();
			else if (instructionArray[i] instanceof Compare)
				((Compare) instructionArray[i]).refract();
			else if (instructionArray[i] instanceof Invoke)
				((Invoke) instructionArray[i]).refract();
		}
	}

	/**
	 * displayErrorsOnConsole - displays the error information printed to output file
	 * onto the console.
	 * @param file - the program output file to serve as input 
	 */
	private static void displayErrorsOnConsole(String file) 
	{
		System.out.println("\n**The program has the following syntax errors **\n");
		Stream.close();
		Scanner scan = null;
		try 
		{
			scan = new Scanner(new File(file));
			while(scan.hasNextLine())
				System.out.println(scan.nextLine());
			scan.close();
		} catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			System.out.println("\n**Error: Not able to open output file to display Errors**\n");
		}
	}


	/**
	 * printInstructionArray - prints all the program instructions to output file.
	 */
	public static void printInstructionArray()
	{ 
		int place = 0;
		for (int i = 0; i < arrayLocation; i++)
			Stream.displayln(place++ + ": " + instructionArray[i].print());

	}

	/**
	 * paramWarning - prints to the console a warning and tips on how to run the program
	 */
	public static void  paramWarning()
	{
		System.out.println("\n\n ******************************* INPUT / OUTPUT ERROR ***********************************\n\n" + 
				" This program requires input as command line arguments in order to run properly:\n" +
				" argv[0] - must be a valid file name with input to be parsed\r\n" + 
				" argv[1] - a valid file name for the parsed output to be written to\r\n" + 
				" argv[2] - OPTIONAL: a file with expected parsing output to be compared to generated parsing output.\r\n" + 
				"           For details, check the Javadocs for compareOutput() in Stream.java\n" + 
				"           NOTE: argv[2] is used only if input does not have syntax errors\n\n" +
				"****************************************************************************************************************\n"+
				"  WARNING: Using argv[2] checks for EXACT match and HALTS program when first space delimiter mismatch is found\n"+
				"****************************************************************************************************************\n\n"+
				" ** If running project in Eclipse, please import project as described here: \n" +
				" \t https://www.codejava.net/ides/eclipse/import-existing-projects-into-eclipse-workspace \n" +
				" \t For the Eclipse option, the input files and matching expected output files are zipped in.\n" +
				" ** If running on command line, enter the following commands inside folder 'pack' **\n" +
				" \t javac *.java <enter>\n\t cd .. <enter>\n\t java pack.VM <inputFile.txt> <outputFile.txt> " +
				"<expectedOutput.txt> <enter> \n  \n\t\t******* SUBSTITUTE THE FILE NAMES ACCORDINGLY ********\n\n" +
				"NOTE: For command line, the parameter text files, MUST be at the same level as 'pack' not inside it\n\n" +
				" *****************************************************************************************");
	}

	/**
	 * checkTarget - checks for the existence of goto and invoke jump targets in a program
	 * @param list - a list of jump targets
	 * @param map - a map of valid jump targets
	 * @param name -  either goto or invoke passed as parameter
	 * @return - true if an invalid target is found, false otherwise
	 */
	public static boolean checkTarget(ArrayList<Integer> list, HashMap<Integer, Integer> map, String name)
	{
		boolean temp = false;
		for(Integer x : list)
			if(!map.containsKey(x))
			{
				temp = true;
				Stream.displayln("Syntax Error: The jump target \"" + x + "\" is not defined for function " + name );
			}
		return temp;
	}

	/**
	 * checkTarget - checks for the existence of compare jump targets in a program
	 * @param list - a hash map of <jump targets, calling function>
	 * @param map - a map of valid jump targets
	 * @return - true if an invalid target is found, false otherwise
	 */
	public static boolean checkTarget(HashMap<Integer, String> list, HashMap<Integer, Integer> map)
	{
		boolean temp = false;
		for(HashMap.Entry<Integer, String> x : list.entrySet())
			if(!map.containsKey(x.getKey()))
			{
				temp = true;
				Stream.displayln("Syntax Error: The jump target \"" + x.getKey() + "\" is not defined for function " + x.getValue());
			}
		return temp;
	}

}
