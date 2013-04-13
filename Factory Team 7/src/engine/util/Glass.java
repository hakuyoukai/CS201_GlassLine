package engine.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Glass
{
	public Map<Integer,Boolean> recipe;
	
	public Glass(String barcode)
	{
		System.out.println("BARCODE " + barcode);
		if (barcode == null) {
			recipe = new HashMap<Integer, Boolean>();
			recipe.put(2, true);
			recipe.put(3, true);
		}
		else {
			System.out.println("Custom recipe");
			ArrayList<String> rcp = new ArrayList<String>(Arrays.asList(barcode.split(" ")));
			recipe = new HashMap<Integer,Boolean>();
			for (int i = 0; i < rcp.size();i++) {
				if (rcp.get(i).equals("1"))
					recipe.put(i, true);
				 else
					recipe.put(i, false);
			}

		}
	}
}