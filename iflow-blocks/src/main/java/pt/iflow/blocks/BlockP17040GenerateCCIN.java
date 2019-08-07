package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileGeneratorUtils;

public class BlockP17040GenerateCCIN extends BlockP17040Generate {

	public BlockP17040GenerateCCIN(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	public String createFileContent(XMLStreamWriter writer, Connection connection, UserInfoInterface userInfo, Integer crcId) throws XMLStreamException, SQLException{
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
		writer.writeStartElement("comCInst");
		// infInstList
		List<Integer> infInstIdList = retrieveSimpleField(connection, userInfo,
				"select infInst.id from infInst, comCInst, conteudo where infInst.comCInst_id=comCInst.id and comCInst.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		for (Integer infInstId : infInstIdList) {
			writer.writeStartElement("infInst");
			fillAtributtes(writer, connection, userInfo,
					"select * from infInst where id = {0} ", new Object[] { infInstId });									
			
			//lstEntSind
			List<Integer> lstEntSindIdList = retrieveSimpleField(connection, userInfo,
					"select id from entSind where infInst_id = {0} ",
					new Object[] { infInstId });
			if(!lstEntSindIdList.isEmpty()){
				writer.writeStartElement("lstEntSind");				
					//entSind
					for(Integer entSindId: lstEntSindIdList){
						writer.writeStartElement("entSind");
						HashMap<String,Object> entSindValues = fillAtributtes(writer, connection, userInfo,
								"select * from entSind where id = {0} ", new Object[] { entSindId });
							//idEnt
							writer.writeStartElement("idEnt");
							FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, entSindValues.get("idEnt_id") );						
							writer.writeEndElement();
						writer.writeEndElement();
					}
				writer.writeEndElement();
			}
			//lstCaracEsp
			writer.writeStartElement("lstCaracEsp");
			List<Integer> lstCaracEspIdList = retrieveSimpleField(connection, userInfo,
					"select id from caractEsp where infInst_id = {0} ",
					new Object[] { infInstId });
				//caractEsp
				for(Integer caractEspId : lstCaracEspIdList){
					writer.writeStartElement("caractEsp");
					fillAtributtes(writer, connection, userInfo,
							"select * from caractEsp where id = {0} ", new Object[] { caractEspId });
					writer.writeEndElement();
				}
			writer.writeEndElement();
			
			//lstLigInst
			List<Integer> ligInstIdList = retrieveSimpleField(connection, userInfo,
					"select id from ligInst where infInst_id = {0} ",
					new Object[] { infInstId });
			if(!ligInstIdList.isEmpty()){
				writer.writeStartElement("lstLigInst");			
					//ligInst
					for(Integer ligInstId : ligInstIdList){
						writer.writeStartElement("ligInst");
						HashMap<String,Object> ligInstValues = fillAtributtes(writer, connection, userInfo,
								"select * from ligInst where id = {0} ", new Object[] { ligInstId });
							//idEnt
							writer.writeStartElement("idEnt");
							FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, ligInstValues.get("idEnt_id") );						
							writer.writeEndElement();
						writer.writeEndElement();
					}
				writer.writeEndElement();
			}
			
			//lstInfRiscoInst
			writer.writeStartElement("lstInfRiscoInst");
			List<Integer> infRiscoInstIdList = retrieveSimpleField(connection, userInfo,
					"select id from infRiscoInst where infInst_id = {0} ",
					new Object[] { infInstId });
				//infRiscoInst
				for(Integer infRiscoInstId : infRiscoInstIdList){
					writer.writeStartElement("infRiscoInst");
					fillAtributtes(writer, connection, userInfo,
							"select * from infRiscoInst where id = {0} ", new Object[] { infRiscoInstId });
					writer.writeEndElement();
				}
			writer.writeEndElement();

			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CCIN";
		}
}
