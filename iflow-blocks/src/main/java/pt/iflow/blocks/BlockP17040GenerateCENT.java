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

public class BlockP17040GenerateCENT extends BlockP17040Generate {

	public BlockP17040GenerateCENT(int anFlowId, int id, int subflowblockid, String filename) {
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
		writer.writeStartElement("comEnt");
		// infEnt
		List<Integer> infEntIdList = retrieveSimpleField(connection, userInfo,
				"select infEnt.id from infEnt, comEnt, conteudo where infEnt.comEnt_id=comEnt.id and comEnt.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		for (Integer infEntId : infEntIdList) {
			writer.writeStartElement("infEnt");
			HashMap<String, Object> infEntValues = fillAtributtes(writer, connection, userInfo,
					"select * from infEnt where id = {0} ", new Object[] { infEntId });

			// idEnt
			writer.writeStartElement("idEnt");
			FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, infEntValues.get("idEnt_id") );
			writer.writeEndElement();

			// dadosEnt
			writer.writeStartElement("dadosEnt");
			if (retrieveSimpleField(connection, userInfo, "select id from dadosEntt1 where infEnt_id = {0} ",
					new Object[] { infEntId }).size() == 1) {
				fillAtributtes(writer, connection, userInfo, "select * from dadosEntt1 where infEnt_id = {0} ",
						new Object[] { infEntId });
			} else if (retrieveSimpleField(connection, userInfo, "select id from dadosEntt2 where infEnt_id = {0} ",
					new Object[] { infEntId }).size() == 1){
				HashMap<String, Object> dadosEntt2Values = fillAtributtes(writer, connection, userInfo,
						"select * from dadosEntt2 where infEnt_id = {0} ", new Object[] { infEntId });
				writer.writeStartElement("morada");
					fillAtributtes(writer, connection, userInfo, "select * from morada where id = {0} ",
							new Object[] { dadosEntt2Values.get("morada_id") });
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// lstDocId
			writer.writeStartElement("lstDocId");
			List<Integer> docIdList = retrieveSimpleField(connection, userInfo,
					"select docId.id from docId where infEnt_id = {0} ", new Object[] { infEntId });
			for (Integer docIdId : docIdList){
				writer.writeStartElement("docId");
				fillAtributtes(writer, connection, userInfo, "select * from docId where id = {0} ",
						new Object[] { docIdId });
				writer.writeEndElement();
			}
			writer.writeEndElement();

			// altIdEnt
			HashMap altIdEntValues = fillAtributtes(null, connection, userInfo, "select * from altIdEnt where id = {0} ",
					new Object[] { infEntValues.get("altIdEnt_id") });
			if(!altIdEntValues.isEmpty()){
				writer.writeStartElement("altIdEnt");
				fillAtributtes(writer, connection, userInfo, "select * from altIdEnt where id = {0} ",
						new Object[] { infEntValues.get("altIdEnt_id") });
				writer.writeEndElement();
			}

			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CENT";
		}
}
