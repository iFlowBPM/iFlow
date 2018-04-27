package pt.iflow.blocks.P17040.utils;

public class ImportAction {
	private ImportActionType action;
	private String id;

	public enum ImportActionType {
		CREATE, UPDATE, DELETE
	}

	public ImportAction(ImportActionType action, String id) {
		super();
		this.action = action;
		this.id = id;
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
