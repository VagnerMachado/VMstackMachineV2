package pack;

public class Fcmpgt extends Compare
{
	private int jump;

	public Fcmpgt(int j)
	{
		jump = j;
	}
	
	public String print() 
	{	
		return "fcmpgt " + jump;
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
