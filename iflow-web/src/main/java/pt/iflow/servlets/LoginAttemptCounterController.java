package pt.iflow.servlets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Setup;

public class LoginAttemptCounterController {
	
	static final String LOGIN_ATTEMPT_COUNTER_MAP_NAME = "LOGIN_ATTEMPT_COUNTER_MAP_NAME";
	
	@SuppressWarnings("unchecked")
	public static void markFailedAttempt(ServletContext sc, HttpServletRequest req) throws UnknownHostException{
		LoginAttemptCounter lc = (LoginAttemptCounter) req.getSession().getAttribute(LOGIN_ATTEMPT_COUNTER_MAP_NAME);
		
		if(lc==null)
			lc = new LoginAttemptCounter();
		
		lc.setAddressAttempt(InetAddress.getByName(req.getLocalAddr()));
		lc.setFailedAttempt(lc.getFailedAttempt()+1);
		lc.setLastFailedAttempt(new Date());
		
		req.getSession().setAttribute(LOGIN_ATTEMPT_COUNTER_MAP_NAME, lc);
	}
	
	public static Boolean isOverFailureLimit(ServletContext sc, HttpServletRequest req) throws UnknownHostException{			
		LoginAttemptCounter lc = (LoginAttemptCounter) req.getSession().getAttribute(LOGIN_ATTEMPT_COUNTER_MAP_NAME);
			
		if(lc==null)
			return false;
		
		//excedeed attempts but reset time has come
		if(lc.getFailedAttempt() > Setup.getPropertyInt(Const.MAX_LOGIN_ATTEMPTS)
				&& lc.getLastFailedAttempt().getTime() < ((new Date()).getTime()- Setup.getPropertyInt(Const.MAX_LOGIN_ATTEMPTS_WAIT))){
			lc.setFailedAttempt(0);
			req.getSession().setAttribute(LOGIN_ATTEMPT_COUNTER_MAP_NAME, lc);
			
			return false;
		}
		
		//excedeed attempts and yet too soon
		if(lc.getFailedAttempt() > Setup.getPropertyInt(Const.MAX_LOGIN_ATTEMPTS) 
				&& lc.getLastFailedAttempt().getTime() > ((new Date()).getTime()- Setup.getPropertyInt(Const.MAX_LOGIN_ATTEMPTS_WAIT)))
			return true;
		
		return false;
	}
}
