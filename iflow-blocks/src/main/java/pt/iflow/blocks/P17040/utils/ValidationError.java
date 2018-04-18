package pt.iflow.blocks.P17040.utils;

public class ValidationError {
	private String code;
	private String table;
	private String field;
	private Integer id;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public ValidationError(String code, String table, String field, Integer id) {
		super();
		this.code = code;
		this.table = table;
		this.field = field;
		this.id = id;
	}	
}
