package pt.iflow.udw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.common.lib.model.core.Application;
import com.uniksystem.common.lib.model.core.enums.ApplicationType;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.userdata.views.UserViewInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;

public class LegacyUserSync {

	public static void create(UserInfoInterface userInfo, Integer userid) {
		String createUserEndpoint = Setup.getProperty("CREATE_USER_ENDPOINT");
		if (StringUtils.isBlank(createUserEndpoint))
			return;
		Logger.debug(userInfo.getUtilizador(), "LegacyUserSync", "create",
				"will sync/create for userid " + userid + ", at " + createUserEndpoint);
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

	public static void changeProfile(UserInfoInterface userInfo, Integer userid) {
		String endpoint = Setup.getProperty("CHANGE_PROFILE_ENDPOINT");
		if (StringUtils.isBlank(endpoint))
			return;

		Logger.debug(userInfo.getUtilizador(), "LegacyUserSync", "changeProfile",
				"will sync/create for userid " + userid + ", at " + endpoint);
		Client client = Client.create();
		WebResource webResource = client.resource(endpoint);

		UserViewInterface uvi = BeanFactory.getUserManagerBean().getUser(userInfo, userid.toString());
		com.uniksystem.common.lib.model.core.LegacyUser lu = new com.uniksystem.common.lib.model.core.LegacyUser();
		lu.setUsername(uvi.getUsername());
		lu.setFirstName(uvi.getFirstName());
		lu.setLastName(uvi.getLastName());
		lu.setEmail(uvi.getEmail());

		DataSource ds = null;
		Connection db = null;
		ResultSet rs = null;

		PreparedStatement pst = null;
		try {
			ds = Utils.getDataSource();
			db = ds.getConnection();
			db.setAutoCommit(false);

			pst = db.prepareStatement("SELECT flow_settings.value, application.description "
					+ "FROM users,userprofiles, flow_roles,flow_settings, application "
					+ "WHERE users.userid = userprofiles.userid "
					+ "	AND userprofiles.profileid = flow_roles.profileid "
					+ "    AND flow_roles.flowid = flow_settings.flowid "
					+ "    AND flow_settings.value = application.name" + "    AND users.username=? "
					+ "    AND flow_roles.permissions like '%C%' "
					+ "    AND flow_settings.name= 'APPLICATION_SETTING' "
					+ "    AND flow_settings.value IS NOT NULL AND flow_settings.value !='' "
					+ "    GROUP BY flow_settings.value;");

			pst.setString(1, uvi.getUsername());
			rs = pst.executeQuery();
			while (rs.next()) {
				com.uniksystem.common.lib.model.core.Application application = new Application();
				application.setLabel(rs.getString(1));
				application.setDescription(rs.getString(2));
				application.setType(ApplicationType.LEGACY);
				lu.setApplication(application);

				ClientResponse response = webResource.type(MediaType.APPLICATION_JSON)
						.header("Authorization", "Bearer " + userInfo.getSAuthToken())
						.post(ClientResponse.class, new Gson().toJson(lu));

				if (response.getStatus() != 200) {
					Logger.error(userInfo.getUtilizador(), "LegacyUserSync", "changeProfile",
							"response  NOK: " + response.getStatus() + " " + response.getEntity(String.class));
				}
			}
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), "LegacyUserSync", "changeProfile", "Could not set application data",
					e);
		} finally {
			DatabaseInterface.closeResources(new Object[] { db, pst, rs });
		}
	}
}
