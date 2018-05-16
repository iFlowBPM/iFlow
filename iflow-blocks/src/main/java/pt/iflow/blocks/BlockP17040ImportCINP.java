package pt.iflow.blocks;

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
