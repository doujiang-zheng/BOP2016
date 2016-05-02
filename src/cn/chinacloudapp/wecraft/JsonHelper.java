package cn.chinacloudapp.wecraft;

import java.util.*;
import org.json.*;

public class JsonHelper {
	
	public static Map<String, String> toMap(String json) throws JSONException
	{		
		//System.out.println("Hello, JsonHelper.toMap(json)!");
		JSONObject jsonObject = new JSONObject(json);
		Map result = new HashMap();
		Iterator iterator = jsonObject.keys();
		String key = null;
		String value = null;
		
		while(iterator.hasNext()) {
			key = (String) iterator.next();
			value = jsonObject.getString(key);
			result.put(key, value);
		}
		return result;
	}

	public static ArrayList<String> toList(String json) throws JSONException
	{
		//System.out.println("Hello, JsonHelper.toList(json)!");
		ArrayList<String> list = new ArrayList<String>();
		JSONArray jsonArray = new JSONArray(json);
		if (jsonArray != null) {
			for(int i = 0; i < jsonArray.length(); i ++)
				list.add(jsonArray.getString(i));
		}
			
		return list;
		//return new ArrayList<String>(JsonHelper.toMap(json).values());
	}
}
