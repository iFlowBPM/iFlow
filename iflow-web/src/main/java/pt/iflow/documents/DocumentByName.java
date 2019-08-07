package pt.iflow.documents;


import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.PassImage;
import pt.iflow.api.documents.DocumentData;
import pt.iflow.api.documents.DocumentSessionHelper;
import pt.iflow.api.flows.FlowType;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;


public class DocumentByName extends HttpServlet {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public DocumentByName() {  }

  public void init() throws ServletException {  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  
  }
}

