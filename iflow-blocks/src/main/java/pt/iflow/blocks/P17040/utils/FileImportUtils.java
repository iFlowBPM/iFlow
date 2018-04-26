package pt.iflow.blocks.P17040.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
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
		BufferedWriter errorOutput) throws IOException {
		HashMap<String, Object> result = new HashMap<>();
		String[] lineValuesAux = StringUtils.splitPreserveAllTokens(line, separator);
		Enumeration<?> collumns = properties.propertyNames();

		while (collumns.hasMoreElements()) {
			String name = collumns.nextElement().toString();

			if (!StringUtils.startsWithIgnoreCase(name, "p17040")) {
				String valueIndex = properties.getProperty(name);
				String type = properties.getProperty("p17040_type_" + name);

				if (StringUtils.equalsIgnoreCase(type, "VARCHAR")) {
					result.put(name, lineValuesAux[Integer.parseInt(valueIndex)]);
				} else if (StringUtils.equalsIgnoreCase(type, "DATE")) {			
					SimpleDateFormat sdf = new SimpleDateFormat(properties.getProperty("p17040_dateFormat"));
					try{
						String valueAux = lineValuesAux[Integer.parseInt(valueIndex)];
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, sdf.parse(valueAux));
					} catch (Exception e){
						errorOutput.write(lineNumber + separator + separator + separator
								+ "Data inválida" + separator + name);
						errorOutput.newLine();
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
						errorOutput.write(lineNumber + separator + separator + separator
								+ "DataTempo inválida" + separator + name);
						errorOutput.newLine();
					}
				} else if (StringUtils.equalsIgnoreCase(type, "DECIMAL")) {			
					try{
						String valueAux = lineValuesAux[Integer.parseInt(valueIndex)];
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, Double.parseDouble(valueAux));
					} catch (Exception e){
						errorOutput.write(lineNumber + separator + separator + separator
								+ "Valor decimal inválido" + separator + name);
						errorOutput.newLine();
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
					errorOutput.write(lineNumber + separator + separator + separator
							+ "Valor booleano inválido" + separator + name);
					errorOutput.newLine();
					}				
				} else if (StringUtils.equalsIgnoreCase(type, "INTEGER")) {			
					try{
						String valueAux = lineValuesAux[Integer.parseInt(valueIndex)];
						if(StringUtils.isBlank(valueAux))
							result.put(name, null);
						else
							result.put(name, Integer.parseInt(valueAux));						
					} catch (Exception e){
						errorOutput.write(lineNumber + separator + separator + separator
								+ "Valor inteiro inválido" + separator + name);
						errorOutput.newLine();
					}
				}
				
			}
		}
		return result;
	}	

	public static Integer insertSimpleLine(DataSource datasource, UserInfoInterface userInfo, String query,
			Object[] parameters) {
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
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), "FileImportUtils", "insertSimpleLine", filledQuery + e.getMessage(),
					e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return resultAux;
	}
	
}
