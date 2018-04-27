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

public class BlockP17040GenerateCERA extends BlockP17040Generate {

	public BlockP17040GenerateCERA(int anFlowId, int id, int subflowblockid, String filename) {
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
		writer.writeStartElement("comRiscoEnt");
		HashMap<String,Object> comRiscoEntValues = fillAtributtes(writer, datasource, userInfo, "select * from comRiscoEnt where conteudo_id = ( select id from conteudo where crc_id  = {0} )",
				new Object[] { crcId });
		
			// riscoEnt
			List<Integer> riscoEntIdList = retrieveSimpleField(datasource, userInfo,
					"select riscoEnt.id from riscoEnt where comRiscoEnt_id = {0} ",
					new Object[] { comRiscoEntValues.get("id") });
			for(Integer riscoEntId : riscoEntIdList){
				writer.writeStartElement("riscoEnt");
				HashMap<String,Object> riscoEntValues = fillAtributtes(writer, datasource, userInfo, "select * from riscoEnt where id = {0} ",
						new Object[] { riscoEntId });
				
					//idEnt
					writer.writeStartElement("idEnt");
					FileGeneratorUtils.fillAtributtesIdEnt(writer, datasource, userInfo, riscoEntValues.get("idEnt_id") );
					writer.writeEndElement();
				
					//infRiscoEnt
					writer.writeStartElement("infRiscoEnt");
					HashMap<String,Object> infRiscoEntValues = fillAtributtes(writer, datasource, userInfo,
							"select * from infRiscoEnt where riscoEnt_id = {0} ", new Object[] {riscoEntId});
						//avalRiscoEnt
						writer.writeStartElement("infRiscoEnt");
						fillAtributtes(writer, datasource, userInfo,
								"select * from avalRiscoEnt where infRiscoEnt_id = {0} ", new Object[] {infRiscoEntValues.get("id")});
						writer.writeEndElement();
					writer.writeEndElement();
					
					//lstClienteRel
					writer.writeStartElement("lstClienteRel");
						//clienteRel
						List<Integer> clienteRelIdList = retrieveSimpleField(datasource, userInfo,
								"select clienteRel.id from clienteRel where riscoEnt_id = {0} ",
								new Object[] {riscoEntId});
						for(Integer clienteRelId : clienteRelIdList){
							writer.writeStartElement("clienteRel");
							HashMap<String,Object> clienteRelValues = fillAtributtes(writer, datasource, userInfo,
									"select * from clienteRel where id = {0} ", new Object[] {clienteRelId});
								
								//idEnt
								writer.writeStartElement("idEnt");
								FileGeneratorUtils.fillAtributtesIdEnt(writer, datasource, userInfo, clienteRelValues.get("idEnt_id") );
								writer.writeEndElement();
							writer.writeEndElement();
						}
					writer.writeEndElement();
					
				writer.writeEndElement();
			}		
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CERA";
		}
}
