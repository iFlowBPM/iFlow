package pt.iflow.blocks.P17040.utils;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;

public class FileImportUtils {

	public static HashMap<String, String> parseLine(String line, Properties properties) {
		HashMap<String, String> result = new HashMap<>();
		String[] lineValuesAux = line.split(properties.getProperty("p17040_separator"));
		Enumeration<?> collumns = properties.propertyNames();

		while (collumns.hasMoreElements()) {
			String name = collumns.nextElement().toString();
			String valueIndex = properties.getProperty(name);

			if (!StringUtils.startsWithIgnoreCase(name, "p17040"))
				result.put(name, lineValuesAux[Integer.parseInt(valueIndex)]);
		}
		return result;
	}

	public static Integer importCentLine(DataSource datasource, UserInfoInterface userInfo, Integer crcIdResult,
			HashMap<String, String> lineValues, Properties properties, String type) throws SQLException {
		if (crcIdResult == null)
			crcIdResult = createNewCrcCENT(datasource, properties);

		List<Integer> comEntIdList = retrieveSimpleField(datasource, userInfo,
				"select comEnt.id from comEnt, conteudo where comEnt.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcIdResult });

		//insert if not yet idEnt
		Integer idEnt_id;
		List<Integer> idEntList = retrieveSimpleField(datasource, userInfo,
				"select idEnt.id from idEnt where nif_nipc = ''{0}'' or codigo_fonte = ''{1}''",
				new Object[] { lineValues.get("idEnt"), lineValues.get("idEnt") });
		if(!idEntList.isEmpty())
			idEnt_id = idEntList.get(0);
		else
			idEnt_id = insertSimpleLine(datasource, userInfo,
					"insert into idEnt(type, nif_nipc, codigo_fonte) values(''{0}'',''{1}'',''{2}'')",
					new Object[] { properties.getProperty("p17040_idEnt_type"), lineValues.get("idEnt"), lineValues.get("idEnt") });
		
		//insert infEnt
		//insert docId
		//insert dadosEnt t1 or t2
		return crcIdResult;
	}

	private static Integer insertSimpleLine(DataSource datasource, UserInfoInterface userInfo, String string,
			Object[] objects) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Integer createNewCrcCENT(DataSource datasource, Properties properties) throws SQLException {
		Integer crcIdResult = 0;
		Connection db = datasource.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "insert into crc(versao) values('1.0')";
			pst = db.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			rs = pst.executeQuery();
			rs = pst.getGeneratedKeys();
			if (rs.next())
				crcIdResult = rs.getInt(1);
			else
				throw new Exception("Could not create new line in crc table");

			pst.close();
			rs.close();
			query = "insert into controlo(crc_id, entObserv, entReport, dtCriacao, idDest, idFichRelac) values(?,?,?,?,?,?)";
			pst = db.prepareStatement(query);
			pst.setInt(1, crcIdResult);
			pst.setString(2, properties.get("p17040_entObserv").toString());
			pst.setString(3, properties.get("p17040_entReport").toString());
			pst.setTimestamp(4, new Timestamp((new Date()).getTime()));
			pst.setString(5, properties.get("p17040_idDest").toString());
			pst.setString(6, properties.get("p17040_idFichRelac").toString());
			pst.executeQuery();

			pst.close();
			Integer conteudo_id;
			query = "insert into conteudo(crc_id) values(?)";
			pst = db.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pst.setInt(1, crcIdResult);
			pst.executeQuery();
			rs = pst.getGeneratedKeys();
			if (rs.next())
				conteudo_id = rs.getInt(1);
			else
				throw new Exception("Could not create new line in conteudo table");

			pst.close();
			query = "insert into comEnt(crc_id) values(?)";
			pst = db.prepareStatement(query);
			pst.setInt(1, conteudo_id);
			pst.executeQuery();
		} catch (Exception e) {
			Logger.error("ADMIN", "FileImportUtils", "createNewCrcCENT, check if cent_import.properties is complete!",
					e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return crcIdResult;
	}
}
