package pack;

public class Icmpeq extends Compare
{
	private int jump;

	public Icmpeq(int j)
	{
		jump = j;
	}
	
	public String print()
	{
		return "icmpeq " + jump;
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
