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

public class BlockP17040GenerateCINA extends BlockP17040Generate {

	public BlockP17040GenerateCINA(int anFlowId, int id, int subflowblockid, String filename) {
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
		writer.writeStartElement("comInfInst");
		HashMap<String,Object> comInfInstValues = fillAtributtes(writer, datasource, userInfo, "select * from comInfInst where conteudo_id = ( select id from conteudo where crc_id  = {0} )",
				new Object[] { crcId });
		
			//infPerInst
			List<Integer> infPerInstIdList = retrieveSimpleField(datasource, userInfo,
					"select infPerInst.id from infPerInst where comInfInst_id = {0} ",
					new Object[] { comInfInstValues.get("id") });
			for(Integer infPerInstId : infPerInstIdList){
				writer.writeStartElement("infPerInst");
				fillAtributtes(writer, datasource, userInfo, "select * from infPerInst where id = {0} ",
						new Object[] { infPerInstId });
					
					//infFinInst
					writer.writeStartElement("infFinInst");
					HashMap<String,Object> infFinInstValues = fillAtributtes(writer, datasource, userInfo, "select * from infFinInst where infPerInst_id = {0} ",
							new Object[] { infPerInstId });
						
						//lstRespEntInst
						List<Integer> respEntInstIdList = retrieveSimpleField(datasource, userInfo,
								"select respEntInst.id from respEntInst where infFinInst_id = {0} ",
								new Object[] { infFinInstValues.get("id") });
						for(Integer respEntInstId : respEntInstIdList){
							//respEntInst
							writer.writeStartElement("respEntInst");
							HashMap<String,Object> respEntInstValues = fillAtributtes(writer, datasource, userInfo, "select * from respEntInst where id = {0} ",
									new Object[] { respEntInstId });
								//idEnt
								writer.writeStartElement("idEnt");
									FileGeneratorUtils.fillAtributtesIdEnt(writer, datasource, userInfo, respEntInstValues.get("idEnt_id") );
								writer.writeEndElement();
							writer.writeEndElement();
						}
						
						//lstProtInst
						writer.writeStartElement("lstProtInst");
						List<Integer> protInstIdList = retrieveSimpleField(datasource, userInfo,
								"select protInst.id from protInst where infFinInst_id = {0} ",
								new Object[] { infFinInstValues.get("id") });
							for(Integer protInstId : protInstIdList){
								//protInst
								writer.writeStartElement("protInst");
								fillAtributtes(writer, datasource, userInfo, "select * from protInst where id = {0} ",
										new Object[] { protInstId });
								writer.writeEndElement();
							}
						writer.writeEndElement();
					writer.writeEndElement();
				
					//infContbInst
					writer.writeStartElement("infContbInst");
					fillAtributtes(writer, datasource, userInfo, "select * from infContbInst where infPerInst_id = {0} ",
							new Object[] { infPerInstId });
					writer.writeEndElement();
					
					//lstIntRInst
					writer.writeStartElement("lstIntRInst");
					List<Integer> infRInstIdList = retrieveSimpleField(datasource, userInfo,
							"select infRInst.id from infRInst where infPerInst_id = {0} ",
							new Object[] {infPerInstId});
						//infRInst
						for(Integer infRInstId: infRInstIdList){
							writer.writeStartElement("infRInst");
							fillAtributtes(writer, datasource, userInfo, "select * from infRInst where id = {0} ",
									new Object[] { infRInstId });
							writer.writeEndElement();
						}
					writer.writeEndElement();
					
				writer.writeEndElement();
			}
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CINA";
		}
}
