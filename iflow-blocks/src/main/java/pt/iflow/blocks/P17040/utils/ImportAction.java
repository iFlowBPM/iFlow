package pt.iflow.blocks.P17040.utils;

public class ImportAction {
	public void setAction(ImportActionType action) {
		this.action = action;
	}

	public void setId(String id) {
		this.id = id;
	}

	private ImportActionType action;
	private String id;
	private Integer u_gestao_id;

	public enum ImportActionType {
		CREATE, UPDATE, DELETE
	}

	public ImportAction(ImportActionType action, String id) {
		super();
		this.action = action;
		this.id = id;
	}
	
	public ImportAction(ImportActionType action) {
		super();
		this.action = action;
	}
	
	public ImportAction(ImportActionType action,Integer u_gestao_id) {
		super();
		this.action = action;
		this.u_gestao_id = u_gestao_id;
	}

	public Integer getU_gestao_id() {
		return u_gestao_id;
	}

	public void setU_gestao_id(Integer u_gestao_id) {
		this.u_gestao_id = u_gestao_id;
	}

	public ImportActionType getAction() {
		return action;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return id + ";" + action;
	}	
	
	
}
