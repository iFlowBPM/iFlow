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

public class BlockP17040GenerateCINA extends BlockP17040Generate {

	public BlockP17040GenerateCINA(int anFlowId, int id, int subflowblockid, String filename) {
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
		writer.writeStartElement("comInfInst");
		HashMap<String,Object> comInfInstValues = fillAtributtes(writer, connection, userInfo, "select * from comInfInst where conteudo_id = ( select id from conteudo where crc_id  = {0} )",
				new Object[] { crcId });
		
			//infPerInst
			List<Integer> infPerInstIdList = retrieveSimpleField(connection, userInfo,
					"select infPerInst.id from infPerInst where comInfInst_id = {0} ",
					new Object[] { comInfInstValues.get("id") });
			for(Integer infPerInstId : infPerInstIdList){
				writer.writeStartElement("infPerInst");
				fillAtributtes(writer, connection, userInfo, "select * from infPerInst where id = {0} ",
						new Object[] { infPerInstId });
					
					//infFinInst
					HashMap<String,Object> infFinInstValues = fillAtributtes(null, connection, userInfo, "select * from infFinInst where infPerInst_id = {0} ",
						new Object[] { infPerInstId });
					if(!infFinInstValues.isEmpty()){
						
					List<Integer> infFinInstIdList = retrieveSimpleField(connection, userInfo,
							"select infFinInst.id from infFinInst where infPerInst_id = {0} ",
							new Object[] { infPerInstId });
					for(Integer infFinInstId : infFinInstIdList){
						writer.writeStartElement("infFinInst");
							fillAtributtes(writer, connection, userInfo, "select * from infFinInst where id = {0} ",
									new Object[] { infFinInstId });
							
							//lstRespEntInst
							List<Integer> respEntInstIdList = retrieveSimpleField(connection, userInfo,
									"select respEntInst.id from respEntInst where infFinInst_id = {0} ",
									new Object[] { infFinInstId });
							if(!respEntInstIdList.isEmpty()){
								writer.writeStartElement("lstRespEntInst");
									for(Integer respEntInstId : respEntInstIdList){
										//respEntInst
										writer.writeStartElement("respEntInst");
										HashMap<String,Object> respEntInstValues = fillAtributtes(writer, connection, userInfo, "select * from respEntInst where id = {0} ",
												new Object[] { respEntInstId });
											//idEnt
											writer.writeStartElement("idEnt");
												FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, respEntInstValues.get("idEnt_id") );
											writer.writeEndElement();
										writer.writeEndElement();
									}
								writer.writeEndElement();
							}
							//lstProtInst						
							List<Integer> protInstIdList = retrieveSimpleField(connection, userInfo,
									"select protInst.id from protInst where infFinInst_id = {0} ",
									new Object[] {infFinInstId });
							if(!protInstIdList.isEmpty()){
							writer.writeStartElement("lstProtInst");
								for(Integer protInstId : protInstIdList){
									//protInst
									writer.writeStartElement("protInst");
									fillAtributtes(writer, connection, userInfo, "select * from protInst where id = {0} ",
											new Object[] { protInstId });
									writer.writeEndElement();
								}
							writer.writeEndElement();
							}
						writer.writeEndElement();
					}
						
					
					}
					
					//infContbInst
					HashMap<String,Object> infContbInstValues = fillAtributtes(null, connection, userInfo, "select * from infContbInst where infPerInst_id = {0} ",
							new Object[] { infPerInstId });
					if(!infContbInstValues.isEmpty()){
						writer.writeStartElement("infContbInst");
							fillAtributtes(writer, connection, userInfo, "select * from infContbInst where infPerInst_id = {0} ",
									new Object[] { infPerInstId });
						writer.writeEndElement();
					}
					
					//lstIntRInst
					List<Integer> infRInstIdList = retrieveSimpleField(connection, userInfo,
							"select infRInst.id from infRInst where infPerInst_id = {0} ",
							new Object[] {infPerInstId});
					if(!infRInstIdList.isEmpty()){
					writer.writeStartElement("lstInfRInst");
					
						//infRInst
						for(Integer infRInstId: infRInstIdList){
							writer.writeStartElement("infRInst");
							fillAtributtes(writer, connection, userInfo, "select * from infRInst where id = {0} ",
									new Object[] { infRInstId });
							writer.writeEndElement();
						}
					writer.writeEndElement();
					}
				writer.writeEndElement();
			}
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CINA";
		}
}
