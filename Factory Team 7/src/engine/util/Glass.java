package engine.util;


import java.util.HashMap;
import java.util.Map;

public class Glass
{
	public Map<Integer,Boolean> recipe;
	
	public Glass(String barcode)
	{
		recipe = new HashMap<Integer, Boolean>();
		recipe.put(2, true);
		recipe.put(3, true);
	}
}