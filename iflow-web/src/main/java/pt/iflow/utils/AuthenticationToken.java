package pt.iflow.utils;


import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.backend.model.dto.core.UnikUserMeDTO;

import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;

public class AuthenticationToken {
	
	public static String getUserByAuthenticationToken(String token){
		Client client = Client.create();
		Logger.info("ADMIN","AuthenticationToken", "getUserByAuthenticationToken", "checking token: " + token);
		WebResource webResource = client
		   .resource(Const.AUTHENTICATION_TOKEN_ENDPOINT);
		
		webResource.header(Const.AUTHENTICATION_TOKEN_PARAM_NAME, token);
		Logger.info("ADMIN","AuthenticationToken", "getUserByAuthenticationToken", "set header : " + Const.AUTHENTICATION_TOKEN_PARAM_NAME + " = " + token);

		ClientResponse response = webResource.accept("application/json")
                   .get(ClientResponse.class);

		if (response.getStatus() != 200) {
			 Logger.info("ADMIN","AuthenticationToken", "getUserByAuthenticationToken", "response status NOK: " + Const.AUTHENTICATION_TOKEN_ENDPOINT + " - " + response.getStatus());
		}

		String output = response.getEntity(String.class);
		Gson gson = new Gson();
		String username = gson.fromJson(output, UnikUserMeDTO.class).getUsername();
		Logger.info("ADMIN","AuthenticationToken", "getUserByAuthenticationToken", "username returned: " + username);
		return username;
	}

}
