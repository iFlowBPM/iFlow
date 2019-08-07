package pt.iflow.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import pt.iflow.api.core.FolderManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.folder.Folder;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.applet.StringUtils;


public class FolderManagerBean implements FolderManager {

	  private static FolderManagerBean instance = null;
	  
	  public static FolderManagerBean getInstance() {
		    if (null == instance)
		      instance = new FolderManagerBean();
		    return instance;
		  }
	
	  public List<Folder> getUserFolders(UserInfoInterface userInfo){
		    String userid = userInfo.getUtilizador();
		    List<Folder> retObj = new ArrayList<Folder>();		    
		    
		    Connection db = null;
		    PreparedStatement pst = null;
		    ResultSet rs = null;
		    int last = 0;

		    try {
		      db = DatabaseInterface.getConnection(userInfo);
		      pst = db.prepareStatement("select id, name, color from folder where userid = ?");
		      pst.setString(1, userid);
		      rs = pst.executeQuery();
		      
		      while (rs.next()) {
		    	  last = rs.getInt("id");
		    	  retObj.add(new Folder(last,rs.getString("name"), rs.getString("color")));
		      }
		      
		      rs.close();
		      rs = null;
		    } catch (SQLException sqle) {
		    	Logger.error(userid, this, "getUserFolders","caught sql exception: " + sqle.getMessage(), sqle);
		    } catch (Exception e) {
		    	Logger.error(userid, this, "getUserFolders","caught exception: " + e.getMessage(), e);
		    } finally {
		    	DatabaseInterface.closeResources(db, pst, rs);
		    }
	  return retObj;
	  }
	  
	  public String getFolderColor(int folderid, List<Folder> folders){  
		  for(int i = 0; i < folders.size(); i++){
			  if(folders.get(i).getFolderid() == folderid)
				  return folders.get(i).getColor();
		  }		  
		  return "";
	  }
	  
	 public String getFolderName(int folderid, List<Folder> folders){  
         for(int i = 0; i < folders.size(); i++){
             if(folders.get(i).getFolderid() == folderid)
                 return folders.get(i).getName();
         }       
         return "";
     }
	  
	  public void setActivityToFolder(UserInfoInterface userInfo, String folderid, String activities){
		  String[] actividades = new String[0];
		  String[] dadosAct = new String[0];
		  
		  if(activities != null)
			  actividades = activities.split(";");
		  
		  Connection db = null;
		  PreparedStatement pst = null;
		  String queryFinal ="update activity set folderid=? where flowid=? and pid=? and subpid=?";
		  try {
			  db = DatabaseInterface.getConnection(userInfo);
			  pst = db.prepareStatement(queryFinal);
			  			  			  
			  for(int i = 0; i < actividades.length; i++){
				  dadosAct = actividades[i].split("_");
				  pst.setInt(1, Integer.valueOf(folderid));
				  pst.setInt(2, Integer.valueOf(dadosAct[0]));
				  pst.setInt(3, Integer.valueOf(dadosAct[1]));
				  pst.setInt(4, Integer.valueOf(dadosAct[2]));
				  pst.executeUpdate();
			  }	   	  		      
		   } catch (SQLException sqle) {
		    	Logger.error(userInfo.getUtilizador(), this, "setActivityToFolder","caught sql exception: " + sqle.getMessage(), sqle);
		   } finally {
		    	DatabaseInterface.closeResources(db, pst);
		   }
	  }
	  
	  public void editFolder(UserInfoInterface userInfo, String folderid, String foldername, String color){
		  Connection db = null;
		  PreparedStatement pst = null;
	 
	   	  try {
		      db = DatabaseInterface.getConnection(userInfo);
		      pst = db.prepareStatement("Update folder set name=?, color=? where id=?");
		      pst.setString(1, foldername);
			  pst.setString(2, color);
			  pst.setInt(3, Integer.valueOf(folderid));
		      pst.executeUpdate();
		   } catch (SQLException sqle) {
		    	Logger.error(userInfo.getUtilizador(), this, "editFolder","caught sql exception: " + sqle.getMessage(), sqle);
		   } finally {
		    	DatabaseInterface.closeResources(db, pst);
		   }
	  }
	  
	  public void createFolder(UserInfoInterface userInfo, String foldername, String color){
		  Connection db = null;
		  PreparedStatement pst = null;
		  final String query = "insert into folder (name, color, userid) values (?,?,?)";;
		  try {
			  db = DatabaseInterface.getConnection(userInfo);			  
			  pst = db.prepareStatement(query);
			  pst.setString(1, foldername);
			  pst.setString(2, color);
			  pst.setString(3, userInfo.getUtilizador());			  
			  pst.executeQuery();
			  
		  } catch (SQLException sqle) {
			  Logger.error(userInfo.getUtilizador(), this, "createFolder","caught sql exception: " + sqle.getMessage(), sqle);
		  } finally {
			  DatabaseInterface.closeResources(db, pst);
		  }
	  }

	  public void deleteFolder(UserInfoInterface userInfo, String folderid){
		  Connection db = null;
		  PreparedStatement pst = null;
	 
	   	  try {
		      db = DatabaseInterface.getConnection(userInfo);
		      pst = db.prepareStatement("Update activity set folderid=NULL where folderid=?");
		      pst.setString(1, folderid);
		      pst.executeUpdate();
		      pst.close();
		      pst = db.prepareStatement("delete from folder where id=?");
		      pst.setString(1, folderid);
		      pst.executeUpdate();		      
		   } catch (SQLException sqle) {
		    	Logger.error(userInfo.getUtilizador(), this, "deleteFolder","caught sql exception: " + sqle.getMessage(), sqle);
		   } finally {
		    	DatabaseInterface.closeResources(db, pst);
		   }
	  }
	  
	   public void setActivityToFolderByName(UserInfoInterface userInfo, String foldername, int flowid, int pid, int subpid){
         Connection db = null;
         PreparedStatement pst = null;
         PreparedStatement pst2 = null;
         ResultSet rs = null;
         String nextUser = "";
         try {
           String query = "select userid from activity where flowid =? and pid=? and subpid=?";
           db = DatabaseInterface.getConnection(userInfo);
           pst = db.prepareStatement(query);
           pst.setInt(1, flowid);
           pst.setInt(2, pid);
           pst.setInt(3, subpid);
           rs = pst.executeQuery();
           
             while(rs.next()){
               nextUser = rs.getString("userid");
               
               if(!StringUtils.isEmpty(nextUser)){
                 query ="update activity set folderid = (select id from folder where name like ? and userid = ?) where flowid =? and pid=? and subpid=? and userid=?";
                 pst2 = db.prepareStatement(query);
                 pst2.setString(1, foldername);
                 pst2.setString(2, nextUser);
                 pst.setInt(3, flowid);
                 pst.setInt(4, pid);
                 pst.setInt(5, subpid);
                 pst2.setString(6, nextUser);
                 pst2.executeUpdate();
               }
             }
          } catch (SQLException sqle) {
               Logger.error(userInfo.getUtilizador(), this, "setActivityToFolderByName","caught sql exception: " + sqle.getMessage(), sqle);
          } finally {
               DatabaseInterface.closeResources(db, pst, pst2);
          }
     }
}
