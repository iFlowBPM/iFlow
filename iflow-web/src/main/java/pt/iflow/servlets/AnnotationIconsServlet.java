package pt.iflow.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.Repository;
import pt.iflow.api.core.RepositoryFile;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.applet.StringUtils;
import pt.iknow.utils.StringUtilities;

public class AnnotationIconsServlet
  extends HttpServlet
{
  private static final long serialVersionUID = -9101755201777404343L;
  private static final String REQUEST_PARAMETER_ICON_NAME = "icon_name";
  private static final String REQUEST_PARAMETER_LABEL_NAME = "label_name";
  
  public void init() {}
  
  private static void copyTo(InputStream in, OutputStream out)
    throws IOException
  {
    byte[] b = new byte[8092];
    int r = -1;
    while ((r = in.read(b)) != -1) {
      out.write(b, 0, r);
    }
  }
  
  protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    InputStream input = null;
    try
    {
      Repository rep = BeanFactory.getRepBean();
      int size = 0;
      
      UserInfoInterface userInfo = (UserInfoInterface)request.getSession().getAttribute("UserInfo");
      
      String iconName = request.getParameter("icon_name");
      if (StringUtilities.isEmpty(iconName))
      {
        String labelName = request.getParameter("label_name");
        if (!StringUtilities.isEmpty(labelName))
        {
          if (labelName.charAt(0) == '\'') {
            labelName = labelName.substring(1, labelName.length() - 1);
          }
          iconName = getIconFileName(userInfo, labelName);
        }
      }
      else if (iconName.charAt(0) == '\'')
      {
        iconName = iconName.substring(1, iconName.length() - 1);
      }
      if ((null != userInfo) && (!StringUtils.isEmpty(iconName)))
      {
        RepositoryFile repFile = rep.getAnnotationIcon(userInfo, iconName);
        input = repFile.getResourceAsStream();
        size = repFile.getSize();
      }
      if (input == null)
      {
        response.sendError(404, "File not found");
        return;
      }
      response.setHeader("Content-Disposition", "inline;filename=" + iconName + ".png");
      OutputStream out = response.getOutputStream();
      response.setContentLength(size);
      copyTo(input, out);
      out.flush();
      out.close();
      if (null != input) {
        try
        {
          input.close();
        }
        catch (IOException localIOException1) {}
      }
      return;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      if (null != input) {
        try
        {
          input.close();
        }
        catch (IOException localIOException2) {}
      }
    }
    finally
    {
      if (null != input) {
        try
        {
          input.close();
        }
        catch (IOException localIOException3) {}
      }
    }
  }
  
  private String getIconFileName(UserInfoInterface userInfo, String labelName)
  {
    Connection db = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    
    String iconFileName = null;
    String LABEL_ICON_COLLUM = "icon";
    try
    {
      db = DatabaseInterface.getConnection(userInfo);
      db.setAutoCommit(false);
      
      StringBuffer query = new StringBuffer();
      query.append("SELECT L.icon ");
      query.append(" FROM label L ");
      query.append(" WHERE L.name like ?");
      
      pst = db.prepareStatement(query.toString());
      pst.setString(1, labelName);
      
      rs = pst.executeQuery();
      if (rs.next())
      {
        iconFileName = rs.getString(LABEL_ICON_COLLUM);
        Logger.debug(userInfo.getUtilizador(), this, "getIconFileName", "Found Icon name: Label [" + labelName + "], icon [" + iconFileName + "]");
      }
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "getIconFileName", "Unable to get label icon file name", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return iconFileName;
  }
}
