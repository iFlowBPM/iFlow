package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileGeneratorUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;

public class BlockP17040GenerateCICC extends BlockP17040Generate {

	public BlockP17040GenerateCICC(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
		
	}

	public String createFileContent(XMLStreamWriter writer, Connection connection, UserInfoInterface userInfo, Integer crcId) throws XMLStreamException, SQLException{
		long start = Runtime.getRuntime().freeMemory();
		Logger.debug("admin", this, "createFileContent", "start free memory:" + start);
		writer.writeStartDocument("UTF-8", "1.0");		
		writer.writeStartElement( "crc");
		writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		writer.writeAttribute("versao", "1.0");
		// controlo
		writer.writeStartElement("controlo");
		fillAtributtes(writer, connection, userInfo, "select entObserv,entReport,dtCriacao,idDest from controlo where crc_id = {0} ",
				new Object[] { crcId });
		writer.writeEndElement();
		// conteudo
		writer.writeStartElement("conteudo");
		//comInfCompList
		List<Integer> comInfCompList = retrieveSimpleField(connection, userInfo,
				"select comInfComp.id from comInfComp, conteudo where comInfComp.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		writer.writeStartElement("comInfComp");

//		for (Integer comInfCompId : comInfCompList) {
			
			List<Integer> infCompCidList = retrieveSimpleField(connection, userInfo,
					"select infCompC.id from infCompC, comInfComp, conteudo where infCompC.comInfComp_id = comInfComp.id and comInfComp.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",	
					new Object[] { crcId });
				int i = 0;
				
				for (Integer infCompCId : infCompCidList) {
					Logger.debug("admin", this, "createFileContent", "infCompCId :" + infCompCId);
					HashMap<String, Object> infCompCValues = fillAtributtes(null, connection, userInfo,
							"select * from infCompC where id = {0} ", new Object[] { infCompCId });					
					i++;
					if (i%LOGCYCLE == 0) Logger.debug(userInfo.getUserId(), "BlockP17040GenerateCICC", "createFileContent", null, null);
					Logger.debug("admin", this, "createFileContent", "infCompC free memory:" + Runtime.getRuntime().freeMemory());
					
					//if UPDATE validate values actually changed, if not goto next
					if(StringUtils.equalsIgnoreCase("CCU", (String) infCompCValues.get("type"))){
						ImportAction actionOnLine = GestaoCrc.checkinfCompC((Date)infCompCValues.get("dtRef"), (String)infCompCValues.get("idCont"), (String)infCompCValues.get("idInst"),
								userInfo.getUtilizador(), connection);
						
						Integer uGestaoId = actionOnLine.getU_gestao_id();
						if(!changedValues(crcId , uGestaoId, (String)infCompCValues.get("idCont"), (String)infCompCValues.get("idInst"), userInfo, connection)){
							Logger.info(userInfo.getUtilizador(), this, "createFileContent", "Skipping on unchangedvalues, currentCrcId: " + crcId + ", uGestaoId: " + uGestaoId + ", idCont: " + (String)infCompCValues.get("idCont") + ", idInst: "+ (String)infCompCValues.get("idInst"));
							continue;
						}
							
					}
					
					writer.writeStartElement("infCompC");
					
					if(StringUtils.equalsIgnoreCase((String) infCompCValues.get("type"),"CCD")){
						fillAtributtes(writer, connection, userInfo,
								"select type,dtRef,idCont,idInst from infCompC where id = {0} ", new Object[] { infCompCId });	
					} else {
						fillAtributtes(writer, connection, userInfo,
								"select * from infCompC where id = {0} ", new Object[] { infCompCId });																						
					//lstEntComp
					List<Integer> lstEntCompIdList = retrieveSimpleField(connection, userInfo,
							"select id from entComp where infCompC_id = {0} ",
							new Object[] { infCompCId });
					if(!lstEntCompIdList.isEmpty()){
						writer.writeStartElement("lstEntComp");				
							//entComp
							for(Integer entCompId: lstEntCompIdList){
								writer.writeStartElement("entComp");
								HashMap<String,Object> entCompValues = fillAtributtes(writer, connection, userInfo,
										"select * from entComp where id = {0} ", new Object[] { entCompId });
									//idEnt
									writer.writeStartElement("idEnt");
									FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, entCompValues.get("idEnt_id") );						
									writer.writeEndElement();
								writer.writeEndElement();
							}
						writer.writeEndElement();
						}
						//lstProtComp
						List<Integer> lstProtCompIdList = retrieveSimpleField(connection, userInfo,
								"select id from protComp where infCompC_id = {0} ",
								new Object[] { infCompCId });
						if(!lstProtCompIdList.isEmpty()){
							writer.writeStartElement("lstProtComp");
							//protComp
							for(Integer protCompId : lstProtCompIdList){
								writer.writeStartElement("protComp");
								fillAtributtes(writer, connection, userInfo,
										"select * from protComp where protComp.id= {0} ", new Object[] { protCompId });
								writer.writeEndElement();
							}																	
							writer.writeEndElement();
						}			
						
						//lstJustComp
						List<Integer> lstJustCompIdList = retrieveSimpleField(connection, userInfo,
								"select id from justComp where infCompC_id = {0} ",
								new Object[] { infCompCId });
						if(!lstJustCompIdList.isEmpty()){
							writer.writeStartElement("lstJustComp");
							//protComp
							for(Integer justCompId : lstJustCompIdList){
								writer.writeStartElement("justComp");
								fillAtributtes(writer, connection, userInfo,
										"select * from justComp where justComp.id = {0} ", new Object[] { justCompId });
								writer.writeEndElement();
							}																	
							writer.writeEndElement();
						}								
					}
						writer.writeEndElement();
					}				
//			}
		
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CICC";
		}

	private boolean changedValues(Integer currentCrcId, Integer uGestaoId, String idCont, String idInst,
			UserInfoInterface userInfo, Connection connection) {
		
		PreparedStatement pst = null;
		ResultSet rs = null;
		final String query = 
				"SELECT LTV, prestOp, prestOpChoq, DSTIChoq, " +
				"	idEnt_id, rendLiq, rendLiqChoq, " +
				"	tpJustif, justif, " +
				"	idProt, imoInst, dtAq " + 
				"FROM infCompC  " +
				"	LEFT JOIN entComp ON infCompC.id = entComp.infCompC_id " +
				"    LEFT JOIN  justComp ON infCompC.id = justComp.infCompC_id " +
				"    LEFT JOIN protComp ON infCompC.id = protComp.infCompC_id  " +
				"    LEFT JOIN comInfComp ON infCompC.comInfComp_id = comInfComp.id " +
				"    LEFT JOIN conteudo ON comInfComp.conteudo_id = conteudo.id " +
				"    LEFT JOIN crc ON conteudo.crc_id = crc.id " +
				"WHERE infCompC.idCont=?  " +
				"    AND infCompC.idInst=? " +
				"	AND crc.id = ? " +
				"ORDER BY LTV, prestOp, prestOpChoq, DSTIChoq, " +
				"	idEnt_id, rendLiq, rendLiqChoq, " +
				"	tpJustif, justif, " +
				"	idProt, imoInst, dtAq";
		
		try {
			Integer previousCrcIdResults=0;
			Integer previousCrcId = retrieveSimpleField(connection, userInfo,
					"select out_id from u_gestao where id = {0} ",	
					new Object[] { uGestaoId }).get(0);
			
			pst = connection.prepareStatement(query);
			pst.setInt(1, Integer.valueOf(idCont));
			pst.setString(2, idInst);
			pst.setInt(3, previousCrcId);
			rs = pst.executeQuery();
			while(rs.next()) previousCrcIdResults++;
			rs.close();pst.close();
			
			Integer currentCrcIdResults=0;
			pst = connection.prepareStatement(query);
			pst.setInt(1, Integer.valueOf(idCont));
			pst.setString(2, idInst);
			pst.setInt(3, currentCrcId);
			rs = pst.executeQuery();
			while(rs.next()) currentCrcIdResults++;
			rs.close();pst.close();
			
			Integer unionResults=0;
			pst = connection.prepareStatement("(" + query + ") UNION (" + query + ")");
			pst.setInt(1, Integer.valueOf(idCont));
			pst.setString(2, idInst);
			pst.setInt(3, currentCrcId);
			pst.setInt(4, Integer.valueOf(idCont));
			pst.setString(5, idInst);
			pst.setInt(6, previousCrcId);
			rs = pst.executeQuery();
			while(rs.next()) unionResults++;
			rs.close();pst.close();
			
			if(previousCrcIdResults.equals(currentCrcIdResults) && currentCrcIdResults.equals(unionResults))
				return false;
			else 
				return true;
			
			
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), this, "insertSimpleLine", "currentCrcId: " + currentCrcId + ", uGestaoId: " + uGestaoId + ", idCont: " + idCont + ", idInst: "+ idInst 
					+ e.getMessage(),e);
			return true;
		} finally {
			DatabaseInterface.closeResources(pst, rs);
		}
		
	}
}
