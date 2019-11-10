package pack;

public class Fcmpeq extends Compare
{
	private int jump;

	public Fcmpeq(int j)
	{
		jump = j;
	}
	
	public String print() 
	{	
		return "fcmpeq " + jump;
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
