package pack;

public class Return extends Instruction
{
	public Return()
	{
		
	}
	
	public String print()
	{
		return "return";
	}
	
	@Override
	public Object getValue() 
	{
		return "return";
	}
}
