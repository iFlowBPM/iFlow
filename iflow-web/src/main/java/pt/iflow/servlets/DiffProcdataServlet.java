package pt.iflow.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.ProcessCatalogue;
import pt.iflow.api.core.ProcessManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.flows.Flow;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessXml;
import pt.iflow.api.transition.FlowRolesTO;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;

public class DiffProcdataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
//http://localhost:8080/iFlow/diffProcdata?flowid=166&pid=2490&subpid=1&begin=2010-05-04&end=2030-05-04
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Integer flowid;
		Integer pid;
		Integer subpid;
		Date begin;
		Date end;
		ArrayList<ChangeInTime> changes = new ArrayList<>();
		
		try {
			flowid = Integer.valueOf(request.getParameter("flowid"));
			pid = Integer.valueOf(request.getParameter("pid"));
			subpid = Integer.valueOf(request.getParameter("subpid"));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			begin = sdf.parse(request.getParameter("begin"));
			end = sdf.parse(request.getParameter("end"));
		} catch (Exception e) {
			Logger.error("", this, "doGet", "Invalid parameters received" + e.getMessage(), e);
			response.sendError(400);
			return;
		}


		HttpSession session = request.getSession();
		UserInfoInterface userInfo = (UserInfoInterface) session.getAttribute(Const.USER_INFO);
		Flow flow = BeanFactory.getFlowBean();
		ProcessManager pm = BeanFactory.getProcessManagerBean();
		Documents docBean = BeanFactory.getDocumentsBean();

		if (userInfo == null) {
			Logger.debug("", this, "doGet", "No valid user authenticated");
			response.sendError(401);
			return;
		}

		if (!flow.checkUserFlowRoles(userInfo, flowid, "" + FlowRolesTO.READ_PRIV)) {
			Logger.debug("", this, "doGet", "No permission to alter Flow");
			response.sendError(403);
			return;
		}
		ProcessCatalogue catalogue = flow.getFlowCatalogue(userInfo, flowid);
		
		Connection db = null;
	    PreparedStatement pst = null;
	    ResultSet rs = null;
	    try {
	        db = DatabaseInterface.getConnection(userInfo);
	        pst = db.prepareStatement("SELECT mid, lastupdate, currentuser, procdata FROM iflow.process_history "+ 
	        							"where flowid=? and pid=? and subpid=? "+
	        							"and lastupdate>=? and lastupdate<=? "+ 
	        							"order by mid;");
	        pst.setInt(1, flowid);
	        pst.setInt(2, pid);
	        pst.setInt(3, subpid);
	        pst.setTimestamp(4, Timestamp.from(begin.toInstant()));
	        pst.setTimestamp(5, Timestamp.from(end.toInstant()));
	        
	        rs = pst.executeQuery();
	        ArrayList<ProcessHistory> procHistList =  new ArrayList<ProcessHistory>();
	        while(rs.next()){
	        	Integer mid = rs.getInt(1);
	        	Date lastUpdate = rs.getDate(2);
	        	String currentUser = rs.getString(3);
	        	ProcessXml reader = new ProcessXml(catalogue, rs.getCharacterStream("procdata"));
	        	ProcessData procData = reader.getProcessData();	        	
	        	procHistList.add(new ProcessHistory(mid, lastUpdate, currentUser, procData));
	        }
	        
	        for(int n=0; n<(procHistList.size()-1);n++){
	        	ProcessHistory phOld = procHistList.get(n);
	        	ProcessHistory phNew = procHistList.get(n+1);
	        	
	        	Collection<String> simpleVariableNames = phOld.procData.getSimpleVariableNames();
	        	for(String simpleVariableName: simpleVariableNames)
	        		if (!StringUtils.equals(phOld.procData.get(simpleVariableName).getRawValue(), phNew.procData.get(simpleVariableName).getRawValue()))
	        			changes.add(new ChangeInTime(phNew.lastUpdate, phNew.mid, phNew.currentUser, simpleVariableName, "" + phOld.procData.get(simpleVariableName).getValue(), "" + phNew.procData.get(simpleVariableName).getValue()));	        			
	        	
	        	Collection<String> listVariableNames = phOld.procData.getListVariableNames();
	        	for(String listVariableName: listVariableNames){
	        		int longestSize = (phOld.procData.getList(listVariableName).size()>phNew.procData.getList(listVariableName).size()?phOld.procData.getList(listVariableName).size():phNew.procData.getList(listVariableName).size());
	        		
	        		for(int m=0; m<longestSize; m++)
	        			try{
	        				if(!StringUtils.equals(phOld.procData.getList(listVariableName).getItem(m).getRawValue(), phNew.procData.getList(listVariableName).getItem(m).getRawValue()))
	        					changes.add(new ChangeInTime(phNew.lastUpdate, phNew.mid, phNew.currentUser, listVariableName + "["+m+"]", "" + phOld.procData.getList(listVariableName).getItem(m).getValue(), "" + phNew.procData.getList(listVariableName).getItem(m).getValue()));	        					
	        			} catch (Exception e){
	        				if(phOld.procData.getList(listVariableName).getItem(m)==null && phNew.procData.getList(listVariableName).getItem(m)!=null )
	        					changes.add(new ChangeInTime(phNew.lastUpdate, phNew.mid, phNew.currentUser, listVariableName + "["+m+"]", "null", "" + phNew.procData.getList(listVariableName).getItem(m).getValue()));
	        				else if(phOld.procData.getList(listVariableName).getItem(m)!=null && phNew.procData.getList(listVariableName).getItem(m)==null )
	        					changes.add(new ChangeInTime(phNew.lastUpdate, phNew.mid, phNew.currentUser, listVariableName + "["+m+"]", "" + phOld.procData.getList(listVariableName).getItem(m).getValue(), "null"));	        				
	        			}	        		
	        	}
	        		
	        }
	    } catch (Exception e){
	    	Logger.error("", this, "doGet", "Error", e);
	    	response.sendError(500);
	    } finally {
	    	DatabaseInterface.closeResources(db, pst, rs);
	    }
		
		
		// devolve pid
	    sendTextResponse(response, changes);
		response.setStatus(200);
	}

	private void sendTextResponse(HttpServletResponse response,  ArrayList<ChangeInTime> changes) throws IOException {
		response.setContentType("text/plain");
		PrintWriter writer = response.getWriter();
		for(ChangeInTime change: changes)
			writer.println(change);
		writer.flush();
		writer.close();
	}
	
	class ChangeInTime{
		Date moment;
		Integer mid;
		String user;
		String varname;
		String oldValue;
		String newValue;
		
		@Override
		public String toString() {
			return moment+"|"+mid+"|"+user+"|"+varname+"|"+oldValue+"|"+newValue;
		}
		
		public ChangeInTime(Date moment, Integer mid, String user, String varname, String oldValue, String newValue) {
			super();
			this.moment = moment;
			this.mid = mid;
			this.user = user;
			this.varname = varname;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}				
	}
	
	class ProcessHistory{
		Integer mid;
		Date lastUpdate;
		String currentUser;
		ProcessData procData;
		
		public ProcessHistory(Integer mid, Date lastUpdate, String currentUser, ProcessData procData) {
			super();
			this.mid = mid;
			this.lastUpdate = lastUpdate;
			this.currentUser = currentUser;
			this.procData = procData;
		}				
	}

}
