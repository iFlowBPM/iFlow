package pt.iflow.blocks.P17040.utils;

/**
 * code - codigo do erro, corresponde aos codigos usados pelo BDP na validação dos ficheiros<br>
 * table - tabela, a tabela onde o campo errado se encontra<br>
 * field - campo, o campo que contém o erro<br>
 * idBdp - id BDP, o id dos objectos nos ficheiros enviados ao BDP, ex: idEnt, idProt, idCont, idInst...<br>
 * id - id interno, a chave primária usada para identificar a linha com erro na tabela<br>
 * @author pussman
 *
 */
public class ValidationError {
	private String code;
	private String table;
	private String field;
	private String idBdp;
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
		this.idBdp = field;
		this.id = id;
	}

	public ValidationError(String code, String table, String field, String idBdp, Integer id) {
		super();
		this.code = code;
		this.table = table;
		this.field = field;
		this.idBdp = idBdp;
		this.id = id;
	}

	@Override
	public String toString() {
		return id + ";" + code + ";" + table + ";" + field;
	}

	public String getIdBdp() {
		return idBdp;
	}

	public void setIdBdp(String idBdp) {
		this.idBdp = idBdp;
	}
}
