package engine.util;


import java.util.HashMap;
import java.util.Map;

public class Glass
{
	public Map<Integer,Integer> recipe;
	
	public Glass(String barcode)
	{
		recipe = new HashMap();
		if(barcode.equals("0"))
		{
			recipe.put(2, 0);
		}
		else if(barcode.equals("1"))
		{
			recipe.put(2, 1);
		}
	}
}