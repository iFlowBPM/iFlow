package pt.iflow.blocks.P17040.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Logger;

public class GestaoCrc {

	static enum Status {
		IMPORTED(0), VALID(1), NOT_VALID(2), BDP_SENT(3), BDP_RECEIVED(4);
		private int value;

		private Status(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public static Integer markAsImported(Integer crcId, Integer originalInputDocumentId, String username,
			DataSource datasource) throws SQLException {
		Connection db = datasource.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "insert into u_gestao(out_id, status_id, importdate, importuser, out_docid) values(?,?,?,?,?)";
			pst = db.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pst.setInt(1, crcId);
			pst.setInt(2, Status.IMPORTED.getValue());
			pst.setTimestamp(3, new Timestamp((new Date()).getTime()));
			pst.setString(4, username);
			pst.setInt(5, originalInputDocumentId);
			pst.executeUpdate();
			rs = pst.getGeneratedKeys();
			if(rs.next())
				return rs.getInt(1);
			else 
				return null;
		} catch (Exception e) {
			Logger.error(username, "GestaoCrc", "markAsImported", e.getMessage(), e);
			throw e;
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
	}

	public static Boolean idEntAlreadyCreated(String idEntValue, String username, DataSource datasource)
			throws SQLException {
		Connection db = datasource.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select idEnt.id " 
					+ "from u_gestao, conteudo, avisRec, fichAce, regMsg, idEnt "
					+ "where idEnt.id = regMsg.idEnt_id   " 
					+ "	and regMsg.fichAce_id = fichAce.id "
					+ "	and fichAce.avisRec_id = avisRec.id " 
					+ "    and avisRec.conteudo_id = conteudo.id "
					+ "    and conteudo.id = u_gestao.in_id " 
					+ "    and u_gestao.status_id = 4 "
					+ "    and regMsg.id not in (select regMsg_id from msg) "
					+ "    and (idEnt.nif_nipc = ? or idEnt.codigo_fonte = ?)";
			
			pst = db.prepareStatement(query);
			pst.setString(1, idEntValue);
			pst.setString(2, idEntValue);
			rs = pst.executeQuery();
			
			if(rs.next())
				return true;
			else 
				return false;
		} catch (Exception e) {
			Logger.error(username, "GestaoCrc", "markAsImported", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return false;
		}

	public static void markAsValidated(Integer crcId, String utilizador, DataSource datasource) throws SQLException {
		Connection db = datasource.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "update u_gestao set status_id = ?, validationdate=?, validationuser=? where out_id = ?";
			pst = db.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pst.setInt(1, Status.VALID.getValue());
			pst.setTimestamp(2, new Timestamp((new Date()).getTime()));
			pst.setString(3, utilizador);
			pst.setInt(4, crcId);
			pst.executeUpdate();
	
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "markAsValidated", e.getMessage(), e);
			throw e;
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
	}

	public static boolean idProtAlreadyCreated(String idProt, String username, DataSource datasource) throws SQLException {
		Connection db = datasource.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select regMsg.idProt " 
					+ "from u_gestao, conteudo, avisRec, fichAce, regMsg "
					+ "where regMsg.idProt = ?   " 
					+ "	and regMsg.fichAce_id = fichAce.id "
					+ "	and fichAce.avisRec_id = avisRec.id " 
					+ "    and avisRec.conteudo_id = conteudo.id "
					+ "    and conteudo.id = u_gestao.in_id " 
					+ "    and u_gestao.status_id = 4 "
					+ "    and regMsg.id not in (select regMsg_id from msg) ";
			
			pst = db.prepareStatement(query);
			pst.setString(1, idProt);
			rs = pst.executeQuery();
			
			if(rs.next())
				return true;
			else 
				return false;
		} catch (Exception e) {
			Logger.error(username, "GestaoCrc", "markAsImported", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return false;
		}
}
