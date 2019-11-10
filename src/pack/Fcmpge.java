package pack;

public class Fcmpge extends Compare
{
	private int jump;

	public Fcmpge(int j)
	{
		jump = j;
	}
	
	public String print() 
	{	
		return "fcmpge " + jump;
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
