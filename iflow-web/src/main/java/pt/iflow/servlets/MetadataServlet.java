package pt.iflow.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.ProcessManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;

public class MetadataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Integer flowid;
		try {
			flowid = Integer.valueOf(request.getParameter("flowid"));			
		} catch(Exception e){
			Logger.error("", this, "doGet", "Bad request params", e);
			response.sendError(400);
			return;
		}
		
		HttpSession session = request.getSession();
		UserInfoInterface userInfo = (UserInfoInterface) session.getAttribute(Const.USER_INFO);
		ProcessManager pm = BeanFactory.getProcessManagerBean();		

		if (userInfo == null) {
			Logger.debug("", this, "doGet", "No permission");
			response.sendError(401);
			return;
		}

		Connection db = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        HashMap<String,List> result = new HashMap<>();
        try {
            db = Utils.getDataSource().getConnection();
            
            for(int i=0;i<20;i++){
            	String stmp = "SELECT name_idx" + i + " AS name, idx" + i + " AS value FROM process, flow " +
					"WHERE flow.flowid = process.flowid " +
					"AND flow.flowid =  ? " +
					"AND name_idx" + i + " IS NOT NULL " +
					"GROUP BY value";
            	st = db.prepareCall(stmp);
            	st.setInt(1, flowid);
            	rs = st.executeQuery();
            	
            	String key=null;
            	ArrayList<String> options=new ArrayList<>();
            	while(rs.next()){
            		key = rs.getString(1);
            		options.add(rs.getString(2));
            	}
            	if(key!=null)
            		result.put(key, options);
            	
            	rs.close();
                st.close();
            }                                    
        } catch (Exception e) {
        	Logger.error("", this, "doGet", "Error querying data", e);
			response.sendError(500);
			return;
        } finally {
            DatabaseInterface.closeResources(db, st, rs);
        }

        response.setStatus(200);
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.print(new Gson().toJson(result));
		writer.flush();
		writer.close();		
	}

}
