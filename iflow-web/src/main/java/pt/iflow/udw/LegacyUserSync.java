package pt.iflow.udw;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.userdata.views.UserViewInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;

public class LegacyUserSync {

	public static void create(UserInfoInterface userInfo, Integer userid) {
		String createUserEndpoint = Setup.getProperty("CREATE_USER_ENDPOINT");
		if(StringUtils.isBlank(createUserEndpoint))
			return;
		Logger.debug(userInfo.getUtilizador(), "LegacyUserSync", "create", "will sync/create for userid " + userid + ", at " + createUserEndpoint);
		Client client = Client.create();
		WebResource webResource = client.resource(createUserEndpoint);

		UserViewInterface uvi = BeanFactory.getUserManagerBean().getUser(userInfo, userid.toString());
		LegacyUser createdLegacyUser = new LegacyUser();
		createdLegacyUser.setActive(true);
		createdLegacyUser.setId(0);
		createdLegacyUser.setUsername(uvi.getUsername());		
		
		ClientResponse response = webResource.type(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + userInfo.getSAuthToken())
				.post(ClientResponse.class, new Gson().toJson(createdLegacyUser));

		if (response.getStatus() != 200) {
			Logger.error(userInfo.getUtilizador(), "LegacyUserSync", "create",
					"response  NOK: " + response.getStatus() + " " + response.getEntity(String.class));
		} else
			Logger.debug(userInfo.getUtilizador(), "LegacyUserSync", "create", "OK sync/create for userid " + userid);
	}

}
