package pt.iflow.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.UserManager;
import pt.iflow.api.userdata.views.UserViewInterface;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;

public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		UserManager userManager = BeanFactory.getUserManagerBean();
		String username,name,email;
		StringBuffer jb = new StringBuffer();
		String line = null;
		Map<String, Object> flattenJson = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);

			System.out.println(jb.toString());
			flattenJson = JsonFlattener.flattenAsMap(jb.toString());
			System.out.println(flattenJson);
			username = flattenJson.get("username").toString();
			name = flattenJson.get("name").toString();		
			email = flattenJson.get("email").toString();	
		} catch (Exception e) {
			Logger.error("", this, "doPost", "Invalid content received,perhaps not JSON? " + e.getMessage(), e);
			response.sendError(400);
			return;
		}
		
		if(StringUtils.isBlank(username) || StringUtils.isBlank(name) || StringUtils.isBlank(email)){
			Logger.error("", this, "doPost", "Blank/null content received");
			response.sendError(400);
			return;
		}
		
		HttpSession session = request.getSession();
		UserInfoInterface userInfo = (UserInfoInterface) session.getAttribute(Const.USER_INFO);
		if (userInfo == null) {
			Logger.debug("", this, "doPost", "No valid user authenticated");
			response.sendError(401);
			return;
		}

		if (!userInfo.isOrgAdmin() && !userInfo.isOrgAdminUsers()) {
			Logger.debug("", this, "doPost", "No permission to create users");
			response.sendError(403);
			return;
		}

		try {
			UserViewInterface uvi = userManager.findUser(userInfo, username);
			if(StringUtils.isBlank(uvi.getUserId()))
				userManager.createUser(userInfo, username, "M", userInfo.getUserData().getUnitId(), email, name, null, null, null, null, null, userInfo.getOrganization(), null, "xpto456", null, null, " ");
		
		
			uvi = userManager.findUser(userInfo, username);
			HashMap<String,String> result = new HashMap<String,String>();
			result.put("userid", uvi.getUserId());
			result.put("username", uvi.getUsername());
			result.put("name", uvi.getFirstName());
			result.put("email", uvi.getEmail());
			sendJsonResponse(response, new Gson().toJson(result));
			response.setStatus(200);
		} catch (IllegalAccessException e) {
			Logger.error("", this, "doPost", "Invalid access" + e.getMessage(), e);
			response.sendError(500);
			return;
		}
				
	}

	private void sendJsonResponse(HttpServletResponse response, String json) throws IOException {
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.print(json);
		writer.flush();
		writer.close();
	}

}
