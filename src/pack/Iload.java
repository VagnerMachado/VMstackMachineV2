package pack;

public class Iload extends Instruction
{
	int address; //must not be negative and in range 0 to 999
	
	public Iload(int a)
	{
		address = a;
	}
	
	public String print()
	{
		return "iload " + address;
	}
	
	@Override
	public Object getValue() 
	{
		return address;
	}
	
	public int getDataValue()
	{
		return address;
	}
}
