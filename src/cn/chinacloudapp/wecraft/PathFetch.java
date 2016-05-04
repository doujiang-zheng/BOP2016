package cn.chinacloudapp.wecraft;
import java.util.*;
import java.io.*;
import java.net.*;

import org.json.*;


public class PathFetch {
	//	https://oxfordhk.azure-api.net/academic/v1.0/evaluate?expr=
	//	Id=1966517895 || 
	//	如果使用AA.AuId, AA.AfId, F.FId, J.JId, C.Id, RId，需要加上composite(AA.AuId=2096482110) 
	//	&attributes=Id,C.CId,RId,AA.AuId,AA.AfId,F.FId,J.JId
	//	&subscription-key=f7cc29509a8443c5b3a5e56b0e38b5a6
	public static int count = 0;
	public static int api_count = 0;
	public static Map<String, String> cache = new HashMap<String, String>();
	private long start = System.currentTimeMillis();
	private static List<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
	private static Map<String, String> vis = new HashMap<String, String>();
	private String urlHead = "https://oxfordhk.azure-api.net/academic/v1.0/";
	private String urlAction = "evaluate";
	private String urlExpression = "?expr=";
	private String urlAttribute = "&attributes=Id,C.CId,RId,AA.AuId,AA.AfId,F.FId,J.JId";
	private String urlKey = "&subscription-key=f7cc29509a8443c5b3a5e56b0e38b5a6";
	
	public static <T> ArrayList<T> deepCopy(ArrayList<T> src) {  
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();  
		    ObjectOutputStream out = new ObjectOutputStream(byteOut);  
		    out.writeObject(src);  
		  
		    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());  
		    ObjectInputStream in = new ObjectInputStream(byteIn);  
		    @SuppressWarnings("unchecked")  
		    ArrayList<T> dest = (ArrayList<T>) in.readObject();  
		    return dest;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    
	    return null;  
	}  
	
	public List<ArrayList<String>> doDistribute(String sourceType, String sourceId, String targetType,String targetId) 
	{
		ret = new ArrayList<ArrayList<String>>();
		ArrayList<String> list = new ArrayList<String>();
		list.add(sourceId);
		vis.put(sourceId, "0");
		doCalculate(list, sourceType, sourceId, targetType, targetId, 0);
		
		return PathFetch.ret;
	}
	
	public void doCalculate(ArrayList<String> parent, String sourceType, String sourceId, String targetType, String targetId, int depth)
	{ 
		if (depth >= 3 || System.currentTimeMillis() - this.start > 4 * 60 *1000)
			return;
		
		String source = this.fetchContent(sourceType, sourceId);
		if(source.contains("statusCode") || source.contains("error"))
			return;
		
		try {
			Map<String, String> total = JsonHelper.toMap(source);
			
			if (total.containsKey("entities")) {
				ArrayList<String> entity = JsonHelper.toList(total.get("entities"));
				for (String e : entity) {
					Map<String, String> en = JsonHelper.toMap(e);
					String Id = "";
					if (en.containsKey("Id")) {
						 Id = en.get("Id");
						this.justify(parent, "Id", Id, sourceId, targetType, targetId, depth);
					}
					if(!sourceType.equals("Id")) {
						depth ++; parent.add(Id);
					}
					
					if (en.containsKey("RId")) {
						ArrayList<String> rid = JsonHelper.toList(en.get("RId"));
						for(String r : rid) {
							this.justify(parent, "RId", r, sourceId, targetType, targetId, depth);
						}
					}
					if (en.containsKey("AA")) {
						ArrayList<String> aa = JsonHelper.toList(en.get("AA"));
						for(String aaDetail : aa) {
							Map<String, String> detail = JsonHelper.toMap(aaDetail);
							if (detail.containsKey("AuId")) {								
								this.justify(parent, "AA.AuId", detail.get("AuId"), sourceId, targetType, targetId, depth);
							}
							if (detail.containsKey("AfId")) {								
								this.justify(parent, "AA.AfId", detail.get("AfId"), sourceId, targetType, targetId, depth);
							}	
						}
					}
					if (en.containsKey("C")) {
						Map<String, String> c = JsonHelper.toMap(en.get("C"));
						if (c.containsKey("CId")) {
							this.justify(parent, "C.CId", c.get("CId"), sourceId, targetType, targetId, depth);
						}							
					}
					if (en.containsKey("F")) {
						ArrayList<String> f = JsonHelper.toList(en.get("F"));
						for (String d : f) {
							Map<String, String> ff = JsonHelper.toMap(d);
							if (ff.containsKey("FId"))  {
								this.justify(parent, "F.FId", ff.get("FId"), sourceId, targetType, targetId, depth);
							}		
						}
					}
					if (en.containsKey("J")) {
						Map<String, String> j = JsonHelper.toMap(en.get("J"));
						if (j.containsKey("JId")) {
							this.justify(parent, "J.JId", j.get("JId"), sourceId, targetType, targetId, depth);
						}
					}
					if (!sourceType.equals("Id")) {
						depth --; parent.remove(Id);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println(source);
		}
		return;
	}
	
	public void justify(ArrayList<String> param, String cType,String cId,String sourceId, String targetType,String targetId, int depth)
	{		
		if(!cId.equals(sourceId)) {
			ArrayList<String> cur = PathFetch.deepCopy(param);
			cur.add(cId);
			if (cId.equals(targetId)) {
				this.syncRet(cur);
			}
			if (depth <= 2)
				this.doCalculate(cur, cType, cId, targetType, targetId, depth + 1);
		}
	}
	
	public synchronized void syncRet(ArrayList<String> param)
	{
		ArrayList<String> cur = PathFetch.deepCopy(param);
		PathFetch.ret.add(cur);
		PathFetch.count ++;
		System.out.println(cur);
		return;
	}
	
	public String fetchContent(String type, String id) 
	{
		if(PathFetch.cache.containsKey(id))
			return PathFetch.cache.get(id);
		
		StringBuilder json = new StringBuilder();  
	    try {  
	    	String url = this.urlHead + this.urlAction + this.queryParam(type, id);
	    	PathFetch.api_count ++;
	    	//System.out.println(url);
	    	URL urlObject = new URL(url);  
	        URLConnection uc = urlObject.openConnection();  
	        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));  
	        String inputLine = null;  
	        while ( (inputLine = in.readLine()) != null) {  
	        	json.append(inputLine);  
	        }  
	        in.close();
	        cache.put(id, json.toString());
	    } catch (MalformedURLException e) {  
	    	e.printStackTrace();  
	    } catch (IOException e) {  
	    	e.printStackTrace();  
	    }
		
		return json.toString();
	}
	
	public String queryParam(String type, String id)
	{
		String ret = "";
		if(type.equals("Id") || type.equals("RId"))
			ret = "Id" + "=" + id;
		else
			ret = "composite(" + type + "=" + id + ")";
		ret =  this.urlExpression + ret + this.urlAttribute + this.urlKey;
		return ret;
	}

}
