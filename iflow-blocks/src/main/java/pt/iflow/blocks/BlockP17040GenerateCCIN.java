package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileGeneratorUtils;

public class BlockP17040GenerateCCIN extends BlockP17040Generate {

	public BlockP17040GenerateCCIN(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	public String createFileContent(XMLStreamWriter writer, DataSource datasource, UserInfoInterface userInfo, Integer crcId) throws XMLStreamException, SQLException{
		writer.writeStartDocument("UTF-8", "1.0");
		writer.writeStartElement("crc");
		writer.writeAttribute("versao", "1.0");
		// controlo
		writer.writeStartElement("controlo");
		fillAtributtes(writer, datasource, userInfo, "select * from controlo where crc_id = {0} ",
				new Object[] { crcId });
		writer.writeEndElement();
		// conteudo
		writer.writeStartElement("conteudo");
		writer.writeStartElement("comCInst");
		// infInstList
		List<Integer> infInstIdList = retrieveSimpleField(datasource, userInfo,
				"select infInst.id from infInst, comCInst, conteudo where infInst.comCInst_id=comCInst.id and comCInst.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		for (Integer infInstId : infInstIdList) {
			writer.writeStartElement("infInst");
			fillAtributtes(writer, datasource, userInfo,
					"select * from infInst where id = {0} ", new Object[] { infInstId });
			
			//lstInfRiscoInst
			writer.writeStartElement("lstInfRiscoInst");
			List<Integer> infRiscoInstIdList = retrieveSimpleField(datasource, userInfo,
					"select id from infRiscoInst where infInst_id = {0} ",
					new Object[] { infInstId });
				//infRiscoInst
				for(Integer infRiscoInstId : infRiscoInstIdList){
					writer.writeStartElement("infRiscoInst");
					fillAtributtes(writer, datasource, userInfo,
							"select * from infRiscoInst where id = {0} ", new Object[] { infRiscoInstId });
					writer.writeEndElement();
				}
			writer.writeEndElement();
			
			//lstLigInst
			writer.writeStartElement("lstLigInst");
			List<Integer> ligInstIdList = retrieveSimpleField(datasource, userInfo,
					"select id from ligInst where infInst_id = {0} ",
					new Object[] { infInstId });
				//ligInst
				for(Integer ligInstId : ligInstIdList){
					writer.writeStartElement("ligInst");
					HashMap<String,Object> ligInstValues = fillAtributtes(writer, datasource, userInfo,
							"select * from ligInst where id = {0} ", new Object[] { ligInstId });
						//idEnt
						writer.writeStartElement("idEnt");
						FileGeneratorUtils.fillAtributtesIdEnt(writer, datasource, userInfo, ligInstValues.get("idEnt_id") );						
						writer.writeEndElement();
					writer.writeEndElement();
				}
			writer.writeEndElement();
			
			//lstEntSind
			writer.writeStartElement("lstCaractEsp");
			List<Integer> lstEntSindIdList = retrieveSimpleField(datasource, userInfo,
					"select id from entSind where infInst_id = {0} ",
					new Object[] { infInstId });
				//entSind
				for(Integer entSindId: lstEntSindIdList){
					writer.writeStartElement("entSind");
					HashMap<String,Object> entSindValues = fillAtributtes(writer, datasource, userInfo,
							"select * from entSind where id = {0} ", new Object[] { entSindId });
						//idEnt
						writer.writeStartElement("idEnt");
						FileGeneratorUtils.fillAtributtesIdEnt(writer, datasource, userInfo, entSindValues.get("idEnt_id") );						
						writer.writeEndElement();
					writer.writeEndElement();
				}
			writer.writeEndElement();
			
			//lstCaractEsp
			writer.writeStartElement("lstCaractEsp");
			List<Integer> lstCaractEspIdList = retrieveSimpleField(datasource, userInfo,
					"select id from caractEsp where infInst_id = {0} ",
					new Object[] { infInstId });
				//caractEsp
				for(Integer caractEspId : lstCaractEspIdList){
					writer.writeStartElement("caractEsp");
					fillAtributtes(writer, datasource, userInfo,
							"select * from caractEsp where id = {0} ", new Object[] { caractEspId });
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
