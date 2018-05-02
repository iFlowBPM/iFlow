package pt.iflow.blocks.P17040.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;

public class FileImportUtils {

	public static HashMap<String, Object> parseLine(Integer lineNumber, String line, Properties properties, String separator,
		ArrayList<ValidationError> errorList) throws IOException {
		HashMap<String, Object> result = new HashMap<>();
		String[] lineValuesAux = StringUtils.splitPreserveAllTokens(line, separator);
		Enumeration<?> collumns = properties.propertyNames();

		while (collumns.hasMoreElements()) {
			String name = collumns.nextElement().toString();

			if (!StringUtils.startsWithIgnoreCase(name, "p17040")) {
				String valueIndex = properties.getProperty(name);
				String type = properties.getProperty("p17040_type_" + name);

				if (StringUtils.equalsIgnoreCase(type, "VARCHAR")) {
					String valueAux = lineValuesAux[Integer.parseInt(valueIndex)]; 
					if(StringUtils.isBlank(valueAux))
						result.put(name, null);
					else
						result.put(name, valueAux);
				} else if (StringUtils.equalsIgnoreCase(type, "DATE")) {			
					SimpleDateFormat sdf = new SimpleDateFormat(properties.getProperty("p17040_dateFormat"));
					try{
						String valueAux = lineValuesAux[Integer.parseInt(valueIndex)];
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, sdf.parse(valueAux));
					} catch (Exception e){
						errorList.add(new ValidationError("Data inválida", "", name, lineNumber));
					}
				} else if (StringUtils.equalsIgnoreCase(type, "TIMESTAMP")) {			
					SimpleDateFormat sdf = new SimpleDateFormat(properties.getProperty("p17040_dateTimeFormat"));
					try{
						String valueAux = lineValuesAux[Integer.parseInt(valueIndex)];
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, sdf.parse(valueAux));
					} catch (Exception e){
						errorList.add(new ValidationError("DataTempo inválida", "", name, lineNumber));
					}
				} else if (StringUtils.equalsIgnoreCase(type, "DECIMAL")) {			
					try{
						String valueAux = lineValuesAux[Integer.parseInt(valueIndex)];
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, Double.parseDouble(valueAux));
					} catch (Exception e){
						errorList.add(new ValidationError("Valor decimal inválido", "", name, lineNumber));
					}
				} else if (StringUtils.equalsIgnoreCase(type, "BOOLEAN")) {								
					String aux = lineValuesAux[Integer.parseInt(valueIndex)];
					if(StringUtils.equals("1", aux))
						result.put(name,Boolean.TRUE);
					else if(StringUtils.equals("0", aux))
						result.put(name,Boolean.FALSE);
					else if(StringUtils.isBlank(aux)){
						result.put(name, null);
					} else {
						errorList.add(new ValidationError("Valor booleano inválido", "", name, lineNumber));
					}				
				} else if (StringUtils.equalsIgnoreCase(type, "INTEGER")) {			
					try{
						String valueAux = lineValuesAux[Integer.parseInt(valueIndex)];
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, Integer.parseInt(valueAux));						
					} catch (Exception e){						
						errorList.add(new ValidationError("Valor inteiro inválido", "", name, lineNumber));
					}
				}
				
			}
		}
		return result;
	}	

	public static Integer insertSimpleLine(DataSource datasource, UserInfoInterface userInfo, String query,
			Object[] parameters) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String filledQuery = null;
		Integer resultAux = null;
		try {
			db = datasource.getConnection();
			filledQuery = MessageFormat.format(query, parameters);
			pst = db.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			for (int i = 0; i < parameters.length; i++)
				pst.setObject((i + 1), parameters[i]);
			pst.executeUpdate();
			rs = pst.getGeneratedKeys();

			if (rs.next())
				resultAux = rs.getInt(1);
		} catch (SQLException e) {
			Logger.error(userInfo.getUtilizador(), "FileImportUtils", "insertSimpleLine", filledQuery + e.getMessage(),
					e);
			throw e;
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return resultAux;
	}
	
}
