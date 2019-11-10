package pack;

public class Fconst extends Instruction
{
	double constant; //must work for float and float E

	public Fconst(double d)
	{
		constant = d;
	}
	
	public String print() 
	{	
		return "fconst " + constant ;
	}
	
	@Override
	public Object getValue() 
	{
		return constant;
	}

	public double getDataValue() {
		
		return constant;
	}

}
