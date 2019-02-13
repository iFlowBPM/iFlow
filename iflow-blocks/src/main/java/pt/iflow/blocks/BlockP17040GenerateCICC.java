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

public class BlockP17040GenerateCICC extends BlockP17040Generate {

	public BlockP17040GenerateCICC(int anFlowId, int id, int subflowblockid, String filename) {
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
		//comInfCompList
		List<Integer> comInfCompList = retrieveSimpleField(connection, userInfo,
				"select comInfComp.id from comInfComp, conteudo where comInfComp.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		writer.writeStartElement("comInfComp");

		for (Integer comInfCompId : comInfCompList) {
			
			List<Integer> infCompCidList = retrieveSimpleField(connection, userInfo,
					"select infCompC.id from infCompC, comInfComp, conteudo where infCompC.comInfComp_id = comInfComp.id and comInfComp.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",	
					new Object[] { crcId });
				for (Integer infCompCId : infCompCidList) {
					writer.writeStartElement("infCompC");
						fillAtributtes(writer, connection, userInfo,
								"select * from infCompC where id = {0} ", new Object[] { infCompCId });					
					//lstEntComp
					List<Integer> lstEntCompIdList = retrieveSimpleField(connection, userInfo,
							"select id from entComp where infCompC_id = {0} ",
							new Object[] { infCompCId });
					if(!lstEntCompIdList.isEmpty()){
						writer.writeStartElement("lstEntComp");				
							//entComp
							for(Integer entCompId: lstEntCompIdList){
								writer.writeStartElement("entComp");
								HashMap<String,Object> entCompValues = fillAtributtes(writer, connection, userInfo,
										"select * from entComp where id = {0} ", new Object[] { entCompId });
									//idEnt
									writer.writeStartElement("idEnt");
									FileGeneratorUtils.fillAtributtesIdEnt(writer, connection, userInfo, entCompValues.get("idEnt_id") );						
									writer.writeEndElement();
								writer.writeEndElement();
							}
						writer.writeEndElement();
						}
						//lstProtComp
						List<Integer> lstProtCompIdList = retrieveSimpleField(connection, userInfo,
								"select id from protComp where infCompC_id = {0} ",
								new Object[] { infCompCId });
						if(!lstProtCompIdList.isEmpty()){
							writer.writeStartElement("lstProtComp");
								//protComp
								//for(Integer protCompId : lstProtCompIdList){ PARA EVENTUALMENTE ESCREVER AS VARIAS LINHAS
									writer.writeStartElement("protComp");
									fillAtributtes(writer, connection, userInfo,
											"select * from protComp where protComp.infCompC_id= {0} ", new Object[] { infCompCId });
									writer.writeEndElement();
								//}
							writer.writeEndElement();
						}			
						
						//lstJustComp
						List<Integer> lstJustCompIdList = retrieveSimpleField(connection, userInfo,
								"select id from justComp where infCompC_id = {0} ",
								new Object[] { infCompCId });
						if(!lstJustCompIdList.isEmpty()){
							writer.writeStartElement("lstJustComp");
								//protComp
								//for(Integer justCompId : lstJustCompIdList){ PARA EVENTUALMENTE ESCREVER AS VARIAS LINHAS
									writer.writeStartElement("justComp");
									fillAtributtes(writer, connection, userInfo,
											"select * from justComp where justComp.infCompC_id = {0} ", new Object[] { infCompCId });
									writer.writeEndElement();
								}
							writer.writeEndElement();
						//}			
			
						writer.writeEndElement();
					}
			}
		
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		
		return "CICC";
		}
}
