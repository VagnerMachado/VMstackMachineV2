package pack;

public class Fcmple extends Compare
{
	private int jump;

	public Fcmple(int j)
	{
		jump = j;
	}
	
	public String print() 
	{	
		return "fcmple "+ jump;
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
