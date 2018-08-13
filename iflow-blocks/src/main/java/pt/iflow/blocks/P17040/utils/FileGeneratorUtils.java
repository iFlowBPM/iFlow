package pt.iflow.blocks.P17040.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;

public class FileGeneratorUtils {
	
	public static List<Integer> retrieveSimpleField(Connection connection, UserInfoInterface userInfo, String query,
			Object[] parameters) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String filledQuery = null;
		List<Integer> resultAux = new ArrayList<Integer>();
		try {
			db =null;
			for(int i=0; i<parameters.length; i++)
				if(parameters[i] instanceof Integer)
					parameters[i] = StringUtils.remove(StringUtils.remove(parameters[i].toString(),','),'.');
			filledQuery = MessageFormat.format(query, parameters);
			pst = connection.prepareStatement(filledQuery);
			rs = pst.executeQuery();

			while (rs.next())
				resultAux.add(rs.getInt(1));
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), "FileGeneratorUtils", "retrieveSimpleField",
					filledQuery + e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return resultAux;
	}

	public static HashMap<String, Object> fillAtributtes(XMLStreamWriter writer, Connection connection,
			UserInfoInterface userInfo, String query, Object[] parameters) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String filledQuery = null;
		HashMap<String, Object> resultAux = new HashMap<>();
		try {
			db = null;
			for(int i=0; i<parameters.length; i++)
				if(parameters[i] instanceof Integer)
					parameters[i] = StringUtils.remove(StringUtils.remove(parameters[i].toString(),','),'.');
			filledQuery = MessageFormat.format(query, parameters);
			pst = connection.prepareStatement(filledQuery);
			rs = pst.executeQuery();
			ResultSetMetaData rsm = rs.getMetaData();
			
			if(rs.next())
				for (int i = 1; i < (rsm.getColumnCount() + 1); i++) {
					resultAux.put(rsm.getColumnName(i), rs.getObject(i));
		
					if (rsm.getColumnName(i).endsWith("_id") || rsm.getColumnName(i).equals("id") || writer==null)
						continue;
					else if(rs.getObject(i)==null)
						continue;
					else if(StringUtils.equalsIgnoreCase(rsm.getColumnName(i),"type"))
						writer.writeAttribute("xsi","",rsm.getColumnName(i), rs.getString(i));
					else if (rsm.getColumnType(i) == java.sql.Types.VARCHAR) {
						writer.writeAttribute(rsm.getColumnName(i), rs.getString(i));
					} else if (rsm.getColumnType(i) == java.sql.Types.DATE) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						writer.writeAttribute(rsm.getColumnName(i), sdf.format(rs.getDate(i)));
					} else if (rsm.getColumnType(i) == java.sql.Types.TIMESTAMP) {
						SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
						String timeAux = sdfDate.format(rs.getTimestamp(i))+"T"+sdfTime.format(rs.getTimestamp(i));
						writer.writeAttribute(rsm.getColumnName(i), timeAux);
					} else if (rsm.getColumnType(i) == java.sql.Types.DECIMAL) {
						DecimalFormat df = new DecimalFormat(
								"##################################################.############################");
						writer.writeAttribute(rsm.getColumnName(i), df.format(rs.getDouble(i)));
					} else if (rsm.getColumnType(i) == java.sql.Types.BOOLEAN || rsm.getColumnType(i) == java.sql.Types.TINYINT) {
						int valAux = rs.getBoolean(i) ? 1 : 0;
						writer.writeAttribute(rsm.getColumnName(i), "" + valAux);
					} else if (rsm.getColumnType(i) == java.sql.Types.INTEGER) {
						writer.writeAttribute(rsm.getColumnName(i), String.format("%d", rs.getInt(i)));
					}
				}
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), "FileGeneratorUtils", "fillAtributtes",
					filledQuery + e.getMessage(), e);
			try {
				writer.writeAttribute("nome", "nome correcto");
			} catch (XMLStreamException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}

		return resultAux;
	}
	
	public static HashMap<String, Object> fillAtributtesIdEnt(XMLStreamWriter writer, Connection connection,
			UserInfoInterface userInfo, Object idEnt_id) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String filledQuery = null;
		HashMap<String, Object> resultAux = new HashMap<>();
		String query = "select * from idEnt where id = ? ";
		try {
			if (idEnt_id==null)
				return resultAux;
			db = null;
			filledQuery = MessageFormat.format(query, new Object[]{idEnt_id});
			pst = connection.prepareStatement(query);
			pst.setObject(1, idEnt_id);
			rs = pst.executeQuery();			
			
			if(rs.next()){
				resultAux.put("id", rs.getInt("id"));
				resultAux.put("type", rs.getString("type"));
				resultAux.put("nif_nipc", rs.getString("nif_nipc"));
				resultAux.put("codigo_fonte", rs.getString("codigo_fonte"));
				
				writer.writeAttribute("xsi","","type", rs.getString("type"));
				if(StringUtils.equals("i1", rs.getString("type")))
					writer.writeCharacters(rs.getString("nif_nipc"));
				else
					writer.writeCharacters(rs.getString("codigo_fonte"));
			}
				
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), "FileGeneratorUtils", "fillAtributtesIdEnt",
					filledQuery + e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}

		return resultAux;
	}
	
	public static String CleanInvalidXmlChars(String text, String replacement) {
		return text;
//	    String re = "[^\\x09\\x0A\\x0D\\x20-\\xD7FF\\xE000-\\xFFFD\\x10000-x10FFFF]";
//	    return text.replaceAll(re, replacement);
	}
}
