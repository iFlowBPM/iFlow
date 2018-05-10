package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ImportCINP extends BlockP17040ImportCINA {
	
	public BlockP17040ImportCINP(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	static enum ReportType {
		IF("IFI","IFU","cinp_if_import.properties"), IC("ICI","ICU","cinp_ic_import.properties"), IR("IRI","IRU","cinp_ir_import.properties");
		
		private String create;
		private String update;
		private String properties;
		
		ReportType(String create, String update, String properties){
			this.create = create;
			this.update=update;
			this.properties=properties;
		}

		public String getCreate() {
			return create;
		}

		public String getUpdate() {
			return update;
		}
		
		public String getProperties() {
			return properties;
		}
	}		

}
