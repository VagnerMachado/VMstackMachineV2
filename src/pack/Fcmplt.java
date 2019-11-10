package pack;

public class Fcmplt extends Compare
{
	private int jump;

	public Fcmplt(int j)
	{
		jump = j;
	}
	
	public String print() 
	{	
		return "fcmplt "+ jump;
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
