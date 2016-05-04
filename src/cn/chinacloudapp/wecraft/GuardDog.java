package cn.chinacloudapp.wecraft;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

/**
 * Servlet implementation class GuardDog
 */
@WebServlet("/GuardDog")
public class GuardDog extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GuardDog() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		Enumeration<String> param = request.getParameterNames();
		String sourceType = "", sourceId = "", targetType = "", targetId = "";
		try {
			List<String> list = Collections.list(param);
			sourceType = list.get(0);
			sourceId = request.getParameter(sourceType).toString();
			sourceType = sourceType.contains("AA") ? "AA.AuId" : "Id";
				
			targetType = list.get(1);
			targetId = request.getParameter(targetType).toString();
			targetType = targetType.contains("AA") ? "AA.AuId" : "Id";
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		}
		
		long start = System.currentTimeMillis();
		
		PathFetch path = new PathFetch();
	  	List<ArrayList<String>> ret = path.doDistribute(sourceType, sourceId, targetType, targetId);
	  	long end = System.currentTimeMillis();	
	 	System.out.println(end - start);
	 	System.out.println("api_coutn: " + PathFetch.api_count);
	 	System.out.println("path_count: " + PathFetch.count);
	 	
	 	ret  = new ArrayList(new HashSet(ret));
	 	JSONArray arrayObj = new JSONArray(ret);
	 	//out.print("sourceType:" + sourceType + "<br>sourceId:" + sourceId);
	 	//out.print("<br>");
	 	//out.print("targetType:" + targetType + "<br>targetId:" + targetId);
	 	//out.print("<br>");
	 	//out.print(arrayObj);
	 	out.print(ret);
	 	out.flush();
	 	out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doGet(request, response);
	}


}
