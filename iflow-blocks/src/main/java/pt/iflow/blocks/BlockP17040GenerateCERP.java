package pt.iflow.blocks;

import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import pt.iflow.api.utils.UserInfoInterface;

public class BlockP17040GenerateCERP extends BlockP17040GenerateCERA {

	public BlockP17040GenerateCERP(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	public String createFileContent(XMLStreamWriter writer, Connection connection, UserInfoInterface userInfo, Integer crcId) throws XMLStreamException, SQLException{
		super.createFileContent(writer, connection, userInfo, crcId);
		return "CERP";
		}
}
