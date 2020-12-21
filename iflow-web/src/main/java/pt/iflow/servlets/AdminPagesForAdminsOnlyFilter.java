package pt.iflow.servlets;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.UserInfoInterface;

public class AdminPagesForAdminsOnlyFilter extends IFlowFilter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		UserInfoInterface userInfo = (UserInfoInterface) httpRequest.getSession().getAttribute(Const.USER_INFO);
		
		if(!userInfo.isOrgAdmin()){
			request.getRequestDispatcher("logout.jsp").forward(request, response);	    	
	    } else 		
	    	chain.doFilter(request, response);
	}

}
