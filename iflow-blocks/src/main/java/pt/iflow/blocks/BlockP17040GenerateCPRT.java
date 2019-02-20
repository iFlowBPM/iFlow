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

public class BlockP17040GenerateCPRT extends BlockP17040Generate {

	public BlockP17040GenerateCPRT(int anFlowId, int id, int subflowblockid, String filename) {
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
		writer.writeStartElement("comProt");
		// infProtinfProtIdList
		List<Integer> infProtIdList = retrieveSimpleField(connection, userInfo,
				"select infProt.id from infProt, comProt, conteudo where infProt.comProt_id=comProt.id and comProt.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		for (Integer infProtId : infProtIdList) {
			writer.writeStartElement("infProt");
			HashMap<String, Object> infProtValues = fillAtributtes(writer, connection, userInfo,
					"select * from infProt where id = {0} ", new Object[] { infProtId });

			// idEnt
			HashMap<String, Object> idEntValue = FileGeneratorUtils.fillAtributtesIdEnt(null, connection, userInfo, infProtValues.get("idEnt_id") );
			if(!idEntValue.isEmpty()){
				writer.writeStartElement("idEnt");
				FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, infProtValues.get("idEnt_id") );
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CPRT";
		}
}
