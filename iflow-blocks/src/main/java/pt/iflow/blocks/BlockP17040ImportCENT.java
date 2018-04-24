package pt.iflow.blocks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;

public class BlockP17040ImportCENT extends BlockP17040Import{

	public BlockP17040ImportCENT(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Integer importFile(DataSource datasource, InputStream inputDocStream, File tmpOutputErrorDocumentFile,
			File tmpOutputActionDocumentFile, UserInfoInterface userInfo) throws IOException, SQLException {
		
		Properties properties = Setup.readPropertiesFile("p1740/cent_import.properties");
		String separator = properties.getProperty("p17040_separator");		
		String dateFormat = properties.getProperty("p17040_dateFormat");	
		BufferedWriter errorOutput = new BufferedWriter(new FileWriter(tmpOutputErrorDocumentFile, true));
		BufferedWriter actionOutput = new BufferedWriter(new FileWriter(tmpOutputActionDocumentFile, true));	
		Integer crcIdResult=null;
		
		List<String> lines = IOUtils.readLines(inputDocStream);		
		for(int lineNumber=0; lineNumber<lines.size(); lineNumber++){
			HashMap<String,String> lineValues = null;
			//obter valores da linha
			try{
				lineValues = FileImportUtils.parseLine(lines.get(lineNumber), properties);
			} catch (Exception e){
				errorOutput.write(lineNumber + separator + separator  + separator + "Linha com número de campos errado" + separator);
				errorOutput.newLine();
				return null;
			}
			//validar Identificação da entidade 
			String idEnt = lineValues.get("idEnt");
			if(StringUtils.isBlank(idEnt)){
				errorOutput.write(lineNumber + separator + separator  + separator + "Identificação da entidade em falta" + separator);
				errorOutput.newLine();
				return null;
			}
			//determinar se é insert ou update
			String type = GestaoCrc.idEntAlreadyCreated(idEnt, "", datasource)?"EU":"EI";
			//adicionar acçao
			actionOutput.write(idEnt + separator + (StringUtils.equals(type, "EU")?"A":"C") );
			actionOutput.newLine();
			//inserir na bd
			crcIdResult = FileImportUtils.importCentLine(datasource, userInfo, crcIdResult, lineValues, properties, type);
		}														
		
		return crcIdResult;
	}	

}
