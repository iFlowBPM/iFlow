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

public class BlockP17040GenerateCPRT extends BlockP17040Generate {

	public BlockP17040GenerateCPRT(int anFlowId, int id, int subflowblockid, String filename) {
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
		writer.writeStartElement("comProt");
		// infProtinfProtIdList
		List<Integer> infProtIdList = retrieveSimpleField(datasource, userInfo,
				"select infProt.id from infProt, comProt, conteudo where infProt.comProt_id=comProt.id and comProt.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		for (Integer infProtId : infProtIdList) {
			writer.writeStartElement("infProt");
			HashMap<String, Object> infProtValues = fillAtributtes(writer, datasource, userInfo,
					"select * from infProt where id = {0} ", new Object[] { infProtId });

			// idEnt
			writer.writeStartElement("idEnt");
			FileGeneratorUtils.fillAtributtesIdEnt(writer, datasource, userInfo, infProtValues.get("idEnt_id") );
			writer.writeEndElement();

			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CPRT";
		}
}
