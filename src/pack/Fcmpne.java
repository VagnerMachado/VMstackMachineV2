package pack;

public class Fcmpne extends Compare
{
	private int jump;

	public Fcmpne(int j)
	{
		jump = j;
	}
	
	public String print() 
	{	
		return "fcmpne " + jump;
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
