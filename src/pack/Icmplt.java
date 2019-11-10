package pack;

public class Icmplt extends Compare
{
	private int jump;

	public Icmplt(int j)
	{
		jump = j;
	}
	
	public String print()
	{
		return "icmplt " + jump;
	}
	
	public void refract()
	{
		jump = VM.jumpMap.get(jump);
	}
	
	@Override
	public Object getValue() 
	{
		return jump;
	}

}
