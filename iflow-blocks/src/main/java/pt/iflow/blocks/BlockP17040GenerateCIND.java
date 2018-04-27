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

public class BlockP17040GenerateCIND extends BlockP17040Generate {

	public BlockP17040GenerateCIND(int anFlowId, int id, int subflowblockid, String filename) {
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
		writer.writeStartElement("comInfDia");
					
			//infDiaInst
			writer.writeStartElement("infDiaInst");
			List<Integer> infDiaInstFinIdList = retrieveSimpleField(datasource, userInfo,
					"select id from infDiaInstFin where comInfDia_id in ( select id from comInfDia where conteudo_id in ( select id from conteudo where crc_id = {0} )) ",
					new Object[] {crcId});
				//infDiaInstFin
				for(Integer infDiaInstFinId : infDiaInstFinIdList){
					writer.writeStartElement("infDiaInstFin");
					HashMap<String,Object> infDiaInstFinValues = fillAtributtes(writer, datasource, userInfo, "select * from infDiaInstFin where id = {0} ",
							new Object[] { infDiaInstFinId });
					
						//lstEntInstDia
						writer.writeStartElement("lstEntInstDia");
							//entInstDia
							List<Integer> entInstDiaIdList = retrieveSimpleField(datasource, userInfo,
									"select id from entInstDia where infDiaInstFin_id = {0} ",
									new Object[] {infDiaInstFinValues.get("id")});
							for(Integer entInstDiaId : entInstDiaIdList){
								writer.writeStartElement("entInstDia");
								HashMap<String,Object> entInstDiaValues = fillAtributtes(writer, datasource, userInfo, "select * from entInstDia where id = {0} ",
										new Object[] { entInstDiaId });
									//idEnt
									writer.writeStartElement("idEnt");
									FileGeneratorUtils.fillAtributtesIdEnt(writer, datasource, userInfo, entInstDiaValues.get("idEnt_id") );
									writer.writeEndElement();
								writer.writeEndElement();
							}								
						writer.writeEndElement();
					writer.writeEndElement();
				}
			writer.writeEndElement();
		
			//lstInfDiaEnt
			writer.writeStartElement("lstInfDiaEnt");
				//infDiaEnt
				List<Integer> infDiaEntIdList = retrieveSimpleField(datasource, userInfo,
						"select id from infDiaEnt where comInfDia_id in ( select id from comInfDia where conteudo_id in ( select id from conteudo where crc_id = {0} )) ",
						new Object[] {crcId});
				for(Integer infDiaEntId :  infDiaEntIdList){
					writer.writeStartElement("infDiaEnt");
					HashMap<String,Object> infDiaEntValues = fillAtributtes(writer, datasource, userInfo, "select * from infDiaEnt where id = {0} ",
							new Object[] { infDiaEntId });
						//idEnt
						writer.writeStartElement("idEnt");
						FileGeneratorUtils.fillAtributtesIdEnt(writer, datasource, userInfo, infDiaEntValues.get("idEnt_id") );
						writer.writeEndElement();
					writer.writeEndElement();
				}					
			writer.writeEndElement();
		
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CIND";
		}
}
