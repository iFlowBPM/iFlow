package pt.iflow.blocks.P17040.utils;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.ImportAction.ImportActionType;
import pt.iflow.connector.document.Document;

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

	public static Integer markAsImported(Integer crcId, Integer originalInputDocumentId, Integer originalDocId2, Integer originalDocId3, String username,
			Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "insert into u_gestao(out_id, status_id, importdate, importuser, original_docid, original_docid2, original_docid3) values(?,?,?,?,?,?,?)";
			pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pst.setInt(1, crcId);
			pst.setInt(2, Status.IMPORTED.getValue());
			pst.setTimestamp(3, new Timestamp((new Date()).getTime()));
			pst.setString(4, username);
			//pst.setInt(5, originalInputDocumentId);
			if(originalInputDocumentId!=null)
				pst.setInt(5, originalInputDocumentId);
			else
				pst.setNull(5, java.sql.Types.INTEGER);
			if(originalDocId2!=null)
				pst.setInt(6, originalDocId2);
			else
				pst.setNull(6, java.sql.Types.INTEGER);
			if(originalDocId3!=null)
				pst.setInt(7, originalDocId3);
			else
				pst.setNull(7, java.sql.Types.INTEGER);
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

	public static ImportAction checkInfEntType(String idEntValue, Date dtRefEnt, String username,
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
				"    comEnt.id = infEnt.comEnt_id and " +
				"    infEnt.idEnt_id = idEnt.id and " +
				"    u_gestao.status_id= 4 and " +
				"	 infEnt.dtRefEnt <= ? and " +
				"    conteudo.id not in (select conteudo_id from comRiscoEnt) and " +
				"	((idEnt.nif_nipc = ? and idEnt.type='i1') or (idEnt.codigo_fonte = ? and idEnt.type='i2')) " +     
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setDate(1, new java.sql.Date(dtRefEnt.getTime()));
			pst.setString(2, idEntValue);
			pst.setString(3, idEntValue);
			rs = pst.executeQuery();
			
			if(!rs.next())
				return new ImportAction(ImportActionType.CREATE);
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefEntAux = rs.getDate(2);
			Integer idEnt_id = rs.getInt(3);
			pst.close();
			rs.close();
			
			query = "select fichAce.id, u_gestao.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg, msg "+
				"			where regMsg.id=msg.regMsg_id "+
				"			and  msg.codMsg!='EN004' " +
				"			and  msg.nvCrit=0 " +
				"			and regMsg.idEnt_id = ? and (operOrig='EI' or operOrig='EU'))" ;
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setInt(2, idEnt_id);
			rs = pst.executeQuery();
			
			if(rs.next() && dtRefEntAux.getTime()<=dtRefEnt.getTime())
				return new ImportAction(ImportActionType.UPDATE, rs.getInt(2));						
			else 
				return new ImportAction(ImportActionType.CREATE);
						
		} catch (Exception e) {
			Logger.error(username, "GestaoCrc", "checkInfEntType", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}

	public static ImportAction checkInfProtType(String idProt, Date dtRefProt, String utilizador,
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
				"    infProt.dtRefProt <= ? and "+
				"    u_gestao.status_id= 4  "+
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idProt);
			pst.setDate(2, new java.sql.Date(dtRefProt.getTime()));
			rs = pst.executeQuery();
			
			if(!rs.next())
				return new ImportAction(ImportActionType.CREATE);
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefProtAux = rs.getDate(2);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id, u_gestao.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg, msg "+
				"			where regMsg.id=msg.regMsg_id "+
				"			and  msg.codMsg!='PT003' " +
				"			and  msg.nvCrit=0 " +
				"			and regMsg.idProt = ?); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setString(2, idProt);
			rs = pst.executeQuery();
			
			if(rs.next() && dtRefProtAux.getTime()<=dtRefProt.getTime())
				return new ImportAction(ImportActionType.UPDATE, rs.getInt(2));		
			else 
				return new ImportAction(ImportActionType.CREATE);
			
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "checkInfProtType", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}

	public static ImportAction checkInfInstType(String idCont, String idInst, Date dtRefInst, String utilizador,
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
				"    infInst.dtRefInst <= ? and "+
				"    u_gestao.status_id= 4  "+
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idCont);
			pst.setString(2, idInst);
			pst.setDate(3, new java.sql.Date(dtRefInst.getTime()));			
			rs = pst.executeQuery();
			
			if(!rs.next())
				return new ImportAction(ImportActionType.CREATE);
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefInstAux = rs.getDate(2);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id, u_gestao.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg, msg "+
				"			where regMsg.id=msg.regMsg_id "+
				"			and  msg.codMsg!='CI003' " +
				"			and  msg.nvCrit=0 " +
				"			and regMsg.idCont = ? and regMsg.idInst = ? " +
				"			and (operOrig='CII' or operOrig='CIU')); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setString(2, idCont);
			pst.setString(3, idInst);
			rs = pst.executeQuery();
			
			if(rs.next() && dtRefInstAux.getTime()<=dtRefInst.getTime())
				return new ImportAction(ImportActionType.UPDATE, rs.getInt(2));		
			else 
				return new ImportAction(ImportActionType.CREATE);
			
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

	public static ImportAction checkRiscoEntType(String idEnt, Date dtRef, String utilizador,
			Connection connection) throws SQLException {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select u_gestao.id, comRiscoEnt.dtRef, idEnt.id "+
				"from u_gestao, crc, conteudo, comRiscoEnt, riscoEnt, idEnt, infRiscoEnt "+
				"where u_gestao.out_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"    conteudo.id = comRiscoEnt.conteudo_id and "+
				"    comRiscoEnt.id = riscoEnt.comRiscoEnt_id and "+
				"    riscoEnt.idEnt_id = idEnt.id and "+
				"    riscoEnt.id = infRiscoEnt.riscoEnt_id and "+
				"    u_gestao.status_id= 4  and "+
				"    (infRiscoEnt.type='ERI' or infRiscoEnt.type='ERU') and " +
				"    comRiscoEnt.dtRef = ?  and "+
				"	((idEnt.nif_nipc = ? and idEnt.type='i1') or (idEnt.codigo_fonte = ? and idEnt.type='i2')) "+
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setDate(1, new java.sql.Date(dtRef.getTime()));
			pst.setString(2, idEnt);
			pst.setString(3, idEnt);
			rs = pst.executeQuery();
			
			if(!rs.next())
				return new ImportAction(ImportActionType.CREATE);
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefAux = rs.getDate(2);
			Integer idEnt_id= rs.getInt(3);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id, u_gestao.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg, msg "+
				"			where regMsg.id=msg.regMsg_id "+
				"			and  msg.codMsg!='RE002' " +
				"			and  msg.nvCrit=0 " +
				"			and regMsg.idEnt_id = ? and (operOrig='ERI' or operOrig='ERU')); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setInt(2, idEnt_id);
			rs = pst.executeQuery();
			
			if(rs.next() && dtRefAux.getTime()<=dtRef.getTime())
				return new ImportAction(ImportActionType.UPDATE, rs.getInt(2));			
			else 
				return new ImportAction(ImportActionType.CREATE);
			
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "checkRiscoEntType", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}

	public static ImportAction checkInfPerInstType(String idCont, String idInst, Date dtRef, String[] types,
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
					"    comInfInst.dtRef = ? and " +
					"    infPerInst.idCont = ? and "+
					"    infPerInst.idInst = ? and "+
					"    u_gestao.status_id= 4  "+
					"    order by u_gestao.receivedate desc, u_gestao.id desc;";
			
			pst = connection.prepareStatement(query);
			pst.setDate(1, new java.sql.Date(dtRef.getTime()));
			pst.setString(2, idCont);
			pst.setString(3, idInst);
			rs = pst.executeQuery();
			
			if(!rs.next())
				return new ImportAction(ImportActionType.CREATE);
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefAux = rs.getDate(2);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id, u_gestao.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+				
				"		( select regMsg.fichAce_id from regMsg, msg "+
				"			where regMsg.id=msg.regMsg_id "+
				"			and !(msg.codMsg = 'IP002' and regMsg.operOrig='IFI') " +
                "           and !(msg.codMsg = 'IP035' and operOrig='ICI') " +
                "           and !(msg.codMsg = 'IP058' and operOrig='IRI') " +
				"			and  msg.nvCrit=0 " +
				"			and regMsg.idCont = ? and regMsg.idInst = ? " + 
				"           and (operOrig=? or operOrig=?) ); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setString(2, idCont);
			pst.setString(3, idInst);
			pst.setString(4, types[0]);
			pst.setString(5, types[1]);
			rs = pst.executeQuery();
			
			if(rs.next() && dtRefAux.getTime()<=dtRef.getTime())
				return new ImportAction(ImportActionType.UPDATE, rs.getInt(2));			
			else 
				return new ImportAction(ImportActionType.CREATE);
			
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "checkInfPerInstType", e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}

	public static ImportAction checkInfDiaInstFin(Date dtRefInfDia, String idCont, String idInst, String utilizador,
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
				"    infDiaInstFin.dtRefInfDia <= ? and "+
				"    u_gestao.status_id= 4  "+
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idCont);
			pst.setString(2, idInst);
			pst.setDate(3, new java.sql.Date(dtRefInfDia.getTime()));
			rs = pst.executeQuery();
			
			if(!rs.next())
				return new ImportAction(ImportActionType.CREATE);
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefInfDiaAux = rs.getDate(2);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id, u_gestao.id "+
				"from u_gestao, crc, conteudo, avisRec, fichAce, regMsg "+ 
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg "+
				"			where regMsg.idCont = ? and regMsg.idInst = ? "+
				"			and  msg.nvCrit=0 " +
				"           and (operOrig='DII' or operOrig='DIU')); ";
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, u_gestao_id);
			pst.setString(2, idCont);
			pst.setString(3, idInst);
			rs = pst.executeQuery();
			
			if(rs.next() && dtRefInfDiaAux.getTime()<=dtRefInfDia.getTime())
				return new ImportAction(ImportActionType.UPDATE, rs.getInt(2));			
			else 
				return new ImportAction(ImportActionType.CREATE);
			
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

	public static void markAsGenerated(Integer crcId, int docId, String utilizador, Connection connection) throws Exception {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "update u_gestao set out_docid=? where out_id = ?";
			pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pst.setInt(1, docId);
			pst.setInt(2, crcId);
			pst.executeUpdate();
	
		} catch (Exception e) {
			Logger.error(utilizador, "GestaoCrc", "markAsGenerated", e.getMessage(), e);
			throw e;
		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
	}
	
	public static Boolean checkForChangedValues(Connection connection, UserInfoInterface userInfo, Integer u_gestao_id, 
			ProcessData procData, Properties properties, HashMap<String, Object> lineValues,
			 HashMap<String, Object> keysToIdentify, List<String> keysToRemove) throws SQLException, IOException{
		return checkForChangedValues( connection,  userInfo,  u_gestao_id, 
				 procData,  properties,  lineValues,
				keysToIdentify, keysToRemove, null);
	}
	
	public static Boolean checkForChangedValues(Connection connection, UserInfoInterface userInfo, Integer u_gestao_id, 
			ProcessData procData, Properties properties, HashMap<String, Object> lineValues,
			 HashMap<String, Object> keysToIdentify, List<String> keysToRemove, Integer originalDocIdIndex) throws SQLException, IOException{
		Documents docBean = BeanFactory.getDocumentsBean();
		
		HashMap<String, Object> u_gestaoValues = fillAtributtes(null, connection, userInfo,
				"select * from u_gestao where id = {0} ", new Object[] {u_gestao_id});
		
		String auxOriginalDocIdIndex = "original_docid";
		if(originalDocIdIndex==null || originalDocIdIndex<2 || originalDocIdIndex>3)
			auxOriginalDocIdIndex = "original_docid";
		else if(originalDocIdIndex == 2 )
			auxOriginalDocIdIndex = "original_docid2";
		else if(originalDocIdIndex == 3 )
			auxOriginalDocIdIndex = "original_docid3";
		
		Document txtImportedOriginally = docBean.getDocument(userInfo, procData, (Integer) u_gestaoValues.get(auxOriginalDocIdIndex));
		String separator = properties.getProperty("p17040_separator", "|");
		HashMap<String, Object> oldLineValues = new HashMap<String, Object>();
		List<String> oldLines = IOUtils.readLines(new ByteArrayInputStream(txtImportedOriginally.getContent()),"UTF-8");					
		for (int i=0; i < oldLines.size(); i++) {
			if(StringUtils.isBlank(oldLines.get(i)))
				continue;			
			try {
				oldLineValues = FileImportUtils.parseLine(i, oldLines.get(i), properties, separator,
						new ArrayList<ValidationError>(),"");
			} catch (Exception e) {
				continue;
			}		
			
			Boolean allKeysMatch=true;
			for(String key: keysToIdentify.keySet()){
				if(!keysToIdentify.get(key).equals(oldLineValues.get(key)))
					allKeysMatch=false;
			}
			if(allKeysMatch)
				break;
			
		}
		
		HashMap<String, Object> newLineValues = (HashMap<String, Object>) lineValues.clone();
		for(String value: keysToRemove){
			newLineValues.remove(value);		
			oldLineValues.remove(value);
		}
		if(oldLineValues.equals(newLineValues))
			return false;
		
		return true;
	}

	public static ImportAction checkinfCompC(Date dtRef, String idCont, String idInst, String utilizador,
			Connection connection) {
		Connection db = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String query = "select u_gestao.id, infCompC.dtRef "+
				"from u_gestao, crc, conteudo, comInfComp, infCompC "+
				"where u_gestao.out_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"    conteudo.id = comInfComp.conteudo_id and "+
				"	comInfComp.id = infCompC.comInfComp_id and"+
				"    infCompC.idCont = ? and "+
				"    infCompC.idInst = ? and "+
				"    infCompC.dtRef <= ? and "+
				"    u_gestao.status_id= 4  "+
				"    order by u_gestao.receivedate desc;";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idCont);
			pst.setString(2, idInst);
			pst.setDate(3, new java.sql.Date(dtRef.getTime()));
			rs = pst.executeQuery();
			
			if(!rs.next())
				return new ImportAction(ImportActionType.CREATE);
			
			Integer u_gestao_id = rs.getInt(1);
			Date dtRefInfDiaAux = rs.getDate(2);
			
			pst.close();
			rs.close();
			
			query = "select fichAce.id, u_gestao.id, infCompC.type "+
				"from u_gestao, crc, conteudo, avisRec, fichAce "+ 
				"LEFT JOIN infCompC ON (infCompC.comInfComp_id = (SELECT id FROM comInfComp WHERE comInfComp.conteudo_id = (SELECT conteudo.id FROM conteudo WHERE crc_id=u_gestao.out_id)) AND infCompC.idCont=? AND infCompC.idInst=? AND infCompC.dtRef=?)  "+	
				"where  u_gestao.in_id = crc.id and "+
				"	crc.id = conteudo.crc_id and "+
				"	conteudo.id = avisRec.conteudo_id and "+
				"	avisRec.id = fichAce.avisRec_id and "+
				"   u_gestao.id = ? and "+
				"   fichAce.id not in  "+
				"		( select regMsg.fichAce_id from regMsg, msg "+
				"			where regMsg.idCont = ? and regMsg.idInst = ? "+
				"			and (msg.nvCrit=0 or msg.codMsg!='CC003')" +	
				"           and (operOrig='CCI' or operOrig='CCU' or operOrig = 'CCD')); ";
			
			pst = connection.prepareStatement(query);
			pst.setString(1, idCont);
			pst.setString(2, idInst);
			pst.setDate(3, new java.sql.Date(dtRefInfDiaAux.getTime()));
			pst.setInt(4, u_gestao_id);
			pst.setString(5, idCont);
			pst.setString(6, idInst);
			rs = pst.executeQuery();float[]a =new float[0];
			
			if(rs.next() && dtRefInfDiaAux.getTime()<=dtRef.getTime())
				if (StringUtils.equalsIgnoreCase(rs.getString("type"),"CCD"))
					return new ImportAction(ImportActionType.CREATE);
				else 
					return new ImportAction(ImportActionType.UPDATE, rs.getInt(2));			
			else 
				return new ImportAction(ImportActionType.CREATE);
			
		} catch (Exception e) {

			Logger.error(utilizador, "GestaoCrc", "checkinfCompC", e.getMessage(), e);

			Logger.error(utilizador, "GestaoCrc", "infCompC", e.getMessage(), e);

		} finally {
			DatabaseInterface.closeResources(db, pst, rs);
		}
		return null;
		}
}
