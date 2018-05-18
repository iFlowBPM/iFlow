package pt.iflow.blocks.P17040.utils;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.ImportAction.ImportActionType;

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
			Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "insert into u_gestao(out_id, status_id, importdate, importuser, original_docid) values(?,?,?,?,?)";
			pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
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

	public static Boolean idEntAlreadyCreated(String idEntValue, String username, Connection connection)
			throws SQLException {
		Connection db = null;
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
			
			pst = connection.prepareStatement(query);
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

	public static void markAsValidated(Integer crcId, String utilizador, Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "update u_gestao set status_id = ?, validationdate=?, validationuser=? where out_id = ?";
			pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
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

	public static boolean idProtAlreadyCreated(String idProt, String username, Connection connection) throws SQLException {
		Connection db = null;
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
			
			pst = connection.prepareStatement(query);
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

	public static boolean idContIdInstAlreadyCreated(String idCont, String idInst, String username,
			Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select regMsg.idProt " 
					+ "from u_gestao, conteudo, avisRec, fichAce, regMsg "
					+ "where regMsg.idCont = ? "
					+ " and regMsg.idInst = ? " 
					+ "	and regMsg.fichAce_id = fichAce.id "
					+ "	and fichAce.avisRec_id = avisRec.id " 
					+ "    and avisRec.conteudo_id = conteudo.id "
					+ "    and conteudo.id = u_gestao.in_id " 
					+ "    and u_gestao.status_id = 4 "
					+ "    and regMsg.id not in (select regMsg_id from msg) ";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idCont);
			pst.setString(2, idInst);
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

	public static ImportActionType checkInfEntType(String idEntValue, Date dtRefEnt, String username,
			Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select u_gestao.id, infEnt.dtRefEnt, idEnt.id, infEnt.type " +
				"from u_gestao, crc, conteudo, comEnt, infEnt, idEnt " +
				"where u_gestao.out_id = crc.id and " +
				"	crc.id = conteudo.crc_id and " +
				"    conteudo.id = comEnt.conteudo_id and " +
				"    comEnt.id = infEnt.id and " +
				"    infEnt.idEnt_id = idEnt.id and " +
				"    u_gestao.status_id= 4 and " +
				"	((idEnt.nif_nipc = ? and idEnt.type='i1') or (idEnt.codigo_fonte = ? and idEnt.type='i2')) " +     
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idEntValue);
			pst.setString(2, idEntValue);
			rs = pst.executeQuery();
			
			if(!rs.next())
				return ImportActionType.CREATE;
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefEntAux = rs.getDate(2);
			Integer idEnt_id = rs.getInt(3);
			pst.close();
			rs.close();
			
			query = "select fichAce.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg "+
				"			where regMsg.idEnt_id = ? and (operOrig='EI' or operOrig='EU')); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setInt(2, idEnt_id);
			rs = pst.executeQuery();
			
			if(!rs.next() && dtRefEntAux.before(dtRefEnt))
				return ImportActionType.UPDATE;			
			else 
				return null;
						
		} catch (Exception e) {
			Logger.error(username, "GestaoCrc", "checkInfEntType", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}

	public static ImportActionType checkInfProtType(String idProt, Date dtRefProt, String utilizador,
			Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select u_gestao.id, infProt.dtRefProt "+
				"from u_gestao, crc, conteudo, comProt, infProt "+
				"where u_gestao.out_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"    conteudo.id = comProt.conteudo_id and "+
				"    comProt.id = infProt.comProt_id and "+
				"    infProt.idProt = ? and "+
				"    u_gestao.status_id= 4  "+
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idProt);
			rs = pst.executeQuery();
			
			if(!rs.next())
				return ImportActionType.CREATE;
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefProtAux = rs.getDate(2);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg "+
				"			where regMsg.idProt = ?); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setString(2, idProt);
			rs = pst.executeQuery();
			
			if(!rs.next() && dtRefProtAux.before(dtRefProt))
				return ImportActionType.UPDATE;			
			else 
				return null;
			
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "checkInfProtType", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}

	public static ImportActionType checkInfInstType(String idCont, String idInst, Date dtRefInst, String utilizador,
			Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select u_gestao.id, infInst.dtRefInst "+
				"from u_gestao, crc, conteudo, comCInst, infInst "+
				"where u_gestao.out_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"    conteudo.id = comCInst.conteudo_id and "+
				"    comCInst.id = infInst.comCInst_id and "+
				"    infInst.idCont = ? and "+
				"    infInst.idInst = ? and "+
				"    u_gestao.status_id= 4  "+
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idCont);
			pst.setString(2, idInst);
			rs = pst.executeQuery();
			
			if(!rs.next())
				return ImportActionType.CREATE;
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefInstAux = rs.getDate(2);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg "+
				"			where regMsg.idCont = ? and regMsg.idInst = ? " +
				"			and (operOrig='CII' or operOrig='CIU')); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setString(2, idCont);
			pst.setString(3, idInst);
			rs = pst.executeQuery();
			
			if(!rs.next() && dtRefInstAux.before(dtRefInst))
				return ImportActionType.UPDATE;			
			else 
				return null;
			
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "checkinfInstType", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}
	
	public static Integer findIdEnt(String idEnt, UserInfoInterface userInfo,Connection connection) throws SQLException{
		List<Integer> idEntList = retrieveSimpleField(connection, userInfo,
				"select idEnt.id from idEnt where ((idEnt.nif_nipc = ''{0}'' and idEnt.type=''i1'') or (idEnt.codigo_fonte = ''{1}'' and idEnt.type=''i2''))", new Object[] {idEnt, idEnt});
		Integer idEnt_id = idEntList.size()>0?idEntList.get(0):null;
		
		return idEnt_id;
	}

	public static ImportActionType checkRiscoEntType(String idEnt, Date dtRef, String utilizador,
			Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select u_gestao.id, comRiscoEnt.dtRef, idEnt.id "+
				"from u_gestao, crc, conteudo, comRiscoEnt, riscoEnt, idEnt "+
				"where u_gestao.out_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"    conteudo.id = comRiscoEnt.conteudo_id and "+
				"    comRiscoEnt.id = riscoEnt.comRiscoEnt_id and "+
				"    riscoEnt.idEnt_id = idEnt.id and "+
				"    u_gestao.status_id= 4  and "+
				"	((idEnt.nif_nipc = ? and idEnt.type='i1') or (idEnt.codigo_fonte = ? and idEnt.type='i2')) " +
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idEnt);
			pst.setString(2, idEnt);
			rs = pst.executeQuery();
			
			if(!rs.next())
				return ImportActionType.CREATE;
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefAux = rs.getDate(2);
			Integer idEnt_id= rs.getInt(3);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg "+
				"			where regMsg.idEnt_id = ? and (operOrig='ERI' or operOrig='ERU')); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setInt(2, idEnt_id);
			rs = pst.executeQuery();
			
			if(!rs.next() && dtRefAux.before(dtRef))
				return ImportActionType.UPDATE;			
			else 
				return null;
			
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "checkRiscoEntType", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}

	public static ImportActionType checkInfPerInstType(String idCont, String idInst, Date dtRef, String[] types,
			String utilizador, Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select u_gestao.id, comInfInst.dtRef "+
				"from u_gestao, crc, conteudo, comInfInst, infPerInst "+
				"where u_gestao.out_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"    conteudo.id = comInfInst.conteudo_id and "+
				"    comInfInst.id = infPerInst.comInfInst_id and "+
				"    infPerInst.idCont = ? and "+
				"    infPerInst.idInst = ? and "+
				"    u_gestao.status_id= 4  "+
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idCont);
			pst.setString(2, idInst);
			rs = pst.executeQuery();
			
			if(!rs.next())
				return ImportActionType.CREATE;
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefAux = rs.getDate(2);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg "+
				"			where regMsg.idCont = ? and regMsg.idInst = ? " + 
				"           and (operOrig=? or operOrig=?) ); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setString(2, idCont);
			pst.setString(3, idInst);
			pst.setString(4, types[0]);
			pst.setString(5, types[1]);
			rs = pst.executeQuery();
			
			if(!rs.next() && dtRefAux.before(dtRef))
				return ImportActionType.UPDATE;			
			else 
				return null;
			
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "checkInfPerInstType", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}

	public static ImportActionType checkInfDiaInstFin(Date dtRefInfDia, String idCont, String idInst, String utilizador,
			Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select u_gestao.id, infDiaInstFin.dtRefInfDia "+
				"from u_gestao, crc, conteudo, comInfDia, infDiaInstFin "+
				"where u_gestao.out_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"    conteudo.id = comInfDia.conteudo_id and "+
				"    comInfDia.id = infDiaInstFin.comInfDia_id and "+
				"    infDiaInstFin.idCont = ? and "+
				"    infDiaInstFin.idInst = ? and "+
				"    u_gestao.status_id= 4  "+
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idCont);
			pst.setString(2, idInst);
			rs = pst.executeQuery();
			
			if(!rs.next())
				return ImportActionType.CREATE;
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefInfDiaAux = rs.getDate(2);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg "+
				"			where regMsg.idCont = ? and regMsg.idInst = ? "+
				"           and (operOrig='DII' or operOrig='DIU')); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setString(2, idCont);
			pst.setString(3, idInst);
			rs = pst.executeQuery();
			
			if(!rs.next() && dtRefInfDiaAux.before(dtRefInfDia))
				return ImportActionType.UPDATE;			
			else 
				return null;
			
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "checkInfDiaInstFin", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}

	public static void markAsIntegrated(Integer originalCrcId, Integer newCrcId, int receivedDocId, String utilizador,
			Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "update u_gestao set status_id = ?, receivedate=?, receiveuser=?, in_id=?, in_docid=? where out_id = ?";
			pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pst.setInt(1, Status.BDP_RECEIVED.getValue());
			pst.setTimestamp(2, new Timestamp((new Date()).getTime()));
			pst.setString(3, utilizador);
			pst.setInt(4, newCrcId);
			pst.setInt(5, receivedDocId);
			pst.setInt(6, originalCrcId);
			pst.executeUpdate();
	
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "markAsIntegrated", e.getMessage(), e);
			throw e;
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
	}
}
