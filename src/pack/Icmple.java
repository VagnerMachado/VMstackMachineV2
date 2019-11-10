package pack;

public class Icmple extends Compare
{
	private int jump;

	public Icmple(int j)
	{
		jump = j;
	}
	
	public String print()
	{
		return "icmple " + jump;
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
