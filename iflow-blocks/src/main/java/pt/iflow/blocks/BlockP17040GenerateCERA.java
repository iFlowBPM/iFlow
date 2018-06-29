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

public class BlockP17040GenerateCERA extends BlockP17040Generate {

	public BlockP17040GenerateCERA(int anFlowId, int id, int subflowblockid, String filename) {
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
		writer.writeStartElement("comRiscoEnt");
		HashMap<String,Object> comRiscoEntValues = fillAtributtes(writer, connection, userInfo, "select * from comRiscoEnt where conteudo_id = ( select id from conteudo where crc_id  = {0} )",
				new Object[] { crcId });
		
			// riscoEnt
			List<Integer> riscoEntIdList = retrieveSimpleField(connection, userInfo,
					"select riscoEnt.id from riscoEnt where comRiscoEnt_id = {0} ",
					new Object[] { comRiscoEntValues.get("id") });
			for(Integer riscoEntId : riscoEntIdList){
				writer.writeStartElement("riscoEnt");
				HashMap<String,Object> riscoEntValues = fillAtributtes(writer, connection, userInfo, "select * from riscoEnt where id = {0} ",
						new Object[] { riscoEntId });
				
					//idEnt
					writer.writeStartElement("idEnt");
					FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, riscoEntValues.get("idEnt_id") );
					writer.writeEndElement();
				
					//infRiscoEnt
					writer.writeStartElement("infRiscoEnt");
					HashMap<String,Object> infRiscoEntValues = fillAtributtes(writer, connection, userInfo,
							"select * from infRiscoEnt where riscoEnt_id = {0} ", new Object[] {riscoEntId});
						//avalRiscoEnt
						List<Integer> avalRiscoEntIdList = retrieveSimpleField(connection, userInfo,
								"select id from avalRiscoEnt where infRiscoEnt_id = {0} ", new Object[] {infRiscoEntValues.get("id")});
						if(!avalRiscoEntIdList.isEmpty()){
							writer.writeStartElement("lstAvalRiscoEnt");
								for(Integer avalRiscoEnt : avalRiscoEntIdList){
								writer.writeStartElement("avalRiscoEnt");
									fillAtributtes(writer, connection, userInfo,
											"select * from avalRiscoEnt where infRiscoEnt_id = {0} ", new Object[] {avalRiscoEnt});
									writer.writeEndElement();
								}
							writer.writeEndElement();
						}
					writer.writeEndElement();
					
					//lstClienteRel
					writer.writeStartElement("lstClienteRel");
						//clienteRel
						List<Integer> clienteRelIdList = retrieveSimpleField(connection, userInfo,
								"select clienteRel.id from clienteRel where riscoEnt_id = {0} ",
								new Object[] {riscoEntId});
						for(Integer clienteRelId : clienteRelIdList){
							writer.writeStartElement("clienteRel");
							HashMap<String,Object> clienteRelValues = fillAtributtes(writer, connection, userInfo,
									"select * from clienteRel where id = {0} ", new Object[] {clienteRelId});
								
								//idEnt
								writer.writeStartElement("idEnt");
								FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, clienteRelValues.get("idEnt_id") );
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
