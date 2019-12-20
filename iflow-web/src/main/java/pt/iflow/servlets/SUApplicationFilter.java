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

public class SUApplicationFilter extends IFlowFilter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String suApplication = httpRequest.getParameter(Const.SU_APPLICATION);
		UserInfoInterface userInfo = (UserInfoInterface) httpRequest.getSession().getAttribute(Const.USER_INFO);
		if(userInfo!=null && !StringUtils.isBlank(suApplication))
			userInfo.setApplication(suApplication);
		
		httpRequest.getSession().setAttribute(Const.USER_INFO, userInfo);
		chain.doFilter(request, response);
	}

}
