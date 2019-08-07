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

public class BlockP17040GenerateCIND extends BlockP17040Generate {

	public BlockP17040GenerateCIND(int anFlowId, int id, int subflowblockid, String filename) {
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
		writer.writeStartElement("comInfDia");
					
			//infDiaInst
			writer.writeStartElement("infDiaInst");
			List<Integer> infDiaInstFinIdList = retrieveSimpleField(connection, userInfo,
					"select id from infDiaInstFin where comInfDia_id in ( select id from comInfDia where conteudo_id in ( select id from conteudo where crc_id = {0} )) ",
					new Object[] {crcId});
				//infDiaInstFin
				for(Integer infDiaInstFinId : infDiaInstFinIdList){
					writer.writeStartElement("infDiaInstFin");
					HashMap<String,Object> infDiaInstFinValues = fillAtributtes(writer, connection, userInfo, "select * from infDiaInstFin where id = {0} ",
							new Object[] { infDiaInstFinId });
					
						//lstEntInstDia
						writer.writeStartElement("lstEntInstDia");
							//entInstDia
							List<Integer> entInstDiaIdList = retrieveSimpleField(connection, userInfo,
									"select id from entInstDia where infDiaInstFin_id = {0} ",
									new Object[] {infDiaInstFinValues.get("id")});
							for(Integer entInstDiaId : entInstDiaIdList){
								writer.writeStartElement("entInstDia");
								HashMap<String,Object> entInstDiaValues = fillAtributtes(writer, connection, userInfo, "select * from entInstDia where id = {0} ",
										new Object[] { entInstDiaId });
									//idEnt
									writer.writeStartElement("idEnt");
									FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, entInstDiaValues.get("idEnt_id") );
									writer.writeEndElement();
								writer.writeEndElement();
							}								
						writer.writeEndElement();
					writer.writeEndElement();
				}
			writer.writeEndElement();
		
			//lstInfDiaEnt
			List<Integer> infDiaEntIdList = retrieveSimpleField(connection, userInfo,
					"select id from infDiaEnt where comInfDia_id in ( select id from comInfDia where conteudo_id in ( select id from conteudo where crc_id = {0} )) ",
					new Object[] {crcId});
			if(!infDiaEntIdList.isEmpty()){
				writer.writeStartElement("lstInfDiaEnt");
					//infDiaEnt				
					for(Integer infDiaEntId :  infDiaEntIdList){
						writer.writeStartElement("infDiaEnt");
						HashMap<String,Object> infDiaEntValues = fillAtributtes(writer, connection, userInfo, "select * from infDiaEnt where id = {0} ",
								new Object[] { infDiaEntId });
							//idEnt
							writer.writeStartElement("idEnt");
							FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, infDiaEntValues.get("idEnt_id") );
							writer.writeEndElement();
						writer.writeEndElement();
					}					
				writer.writeEndElement();
			}
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CIND";
		}
}
