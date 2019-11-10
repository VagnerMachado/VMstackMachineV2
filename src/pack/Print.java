package pack;

public class Print extends Instruction
{
	private int value;

	public Print(int v)
	{
		value = v;
	}
	
	public String print()
	{
		return "print " + value;
	}
	
	@Override
	public Object getValue() 
	{
		return value;
	}
}
