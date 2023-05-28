package ca.artemis;

import java.util.ArrayList;
import java.util.List;

public class Util {
    
	public static String[] RemoveEmptyStrings(String[] data) {
		List<String> result = new ArrayList<String>();
		
		for(int i = 0; i < data.length; i++)
			if(!data[i].equals(""))
				result.add(data[i]);
		
		String[] res = new String[result.size()];
		result.toArray(res);
		
		return res;
	}
}
