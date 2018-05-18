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
	
	public static final String UTF8_BOM = "\uFEFF";

	public static HashMap<String, Object> parseLine(Integer lineNumber, String line, Properties properties, String separator,
		ArrayList<ValidationError> errorList, String errorExtraInfo) throws IOException {
		HashMap<String, Object> result = new HashMap<>();
		String[] lineValuesAux = StringUtils.splitPreserveAllTokens(line, separator);
		Enumeration<?> collumns = properties.propertyNames();

		while (collumns.hasMoreElements()) {
			String name = collumns.nextElement().toString();

			if (!StringUtils.startsWithIgnoreCase(name, "p17040")) {
				String valueIndex = properties.getProperty(name);
				String type = properties.getProperty("p17040_type_" + name);
				String valueAux = checkForDefaultValue(lineValuesAux,valueIndex);
				
				if (StringUtils.equalsIgnoreCase(type, "VARCHAR")) {
					if(StringUtils.isBlank(valueAux))
						result.put(name, null);
					else
						result.put(name, valueAux);
				} else if (StringUtils.equalsIgnoreCase(type, "DATE")) {			
					SimpleDateFormat sdf = new SimpleDateFormat(properties.getProperty("p17040_dateFormat"));
					sdf.setLenient(false);
					try{
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, sdf.parse(valueAux));
					} catch (Exception e){
						errorList.add(new ValidationError("Data inválida", errorExtraInfo, name, lineNumber));
					}
				} else if (StringUtils.equalsIgnoreCase(type, "TIMESTAMP")) {			
					SimpleDateFormat sdf = new SimpleDateFormat(properties.getProperty("p17040_dateTimeFormat"));
					sdf.setLenient(false);
					try{
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, sdf.parse(valueAux));
					} catch (Exception e){
						errorList.add(new ValidationError("DataTempo inválida",errorExtraInfo, name, lineNumber));
					}
				} else if (StringUtils.equalsIgnoreCase(type, "DECIMAL")) {			
					try{
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, Double.parseDouble(valueAux));
					} catch (Exception e){
						errorList.add(new ValidationError("Valor decimal inválido", errorExtraInfo, name, lineNumber));
					}
				} else if (StringUtils.equalsIgnoreCase(type, "BOOLEAN")) {								
					if(StringUtils.equals("1", valueAux) || StringUtils.equalsIgnoreCase("true", valueAux))
						result.put(name,Boolean.TRUE);
					else if(StringUtils.equals("0", valueAux) || StringUtils.equalsIgnoreCase("false", valueAux))
						result.put(name,Boolean.FALSE);
					else if(StringUtils.isBlank(valueAux)){
						result.put(name, null);
					} else {
						errorList.add(new ValidationError("Valor booleano inválido", errorExtraInfo, name, lineNumber));
					}				
				} else if (StringUtils.equalsIgnoreCase(type, "INTEGER")) {			
					try{
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, Integer.parseInt(valueAux));						
					} catch (Exception e){						
						errorList.add(new ValidationError("Valor inteiro inválido", errorExtraInfo, name, lineNumber));
					}
				}
				
			}
		}
		return result;
	}	
	
	private static String checkForDefaultValue(String []lineValuesAux, String index){
		String aux=null;
		if((StringUtils.startsWith(index, "\"") && StringUtils.endsWith(index, "\"")) || (StringUtils.startsWith(index, "'") && StringUtils.endsWith(index, "'"))){
			aux = StringUtils.removeStart(index,  "\"");
			aux = StringUtils.removeStart(aux, "'");
			aux = StringUtils.removeEnd(aux,  "\"");
			aux = StringUtils.removeEnd(aux, "'");
		} else
			aux = removeUTF8BOM(lineValuesAux[Integer.parseInt(index)]);		
		
		return aux;
	}

	private static String removeUTF8BOM(String s) {
        s = StringUtils.removeStart(s, UTF8_BOM);
        s = StringUtils.removeStart(s, "ï»¿");
        return s;
    }
	
	public static Integer insertSimpleLine(Connection connection, UserInfoInterface userInfo, String query,
			Object[] parameters) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String filledQuery = null;
		Integer resultAux = null;
		try {
			db = null;
			filledQuery = MessageFormat.format(query, parameters);
			pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
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
