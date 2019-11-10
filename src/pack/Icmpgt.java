package pack;

public class Icmpgt extends Compare
{
	private int jump;

	public Icmpgt(int j)
	{
		jump = j;
	}
	
	public String print()
	{
		return "icmpgt " + jump;
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
