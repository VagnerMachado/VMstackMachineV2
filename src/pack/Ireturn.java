package pack;

public class Ireturn extends Instruction
{
	public Ireturn()
	{
		
	}
	
	public String print()
	{
		return "ireturn";
	}
	
	@Override
	public Object getValue() 
	{
		return "ireturn";
	}
}
