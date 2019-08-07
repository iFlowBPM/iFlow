package pt.iflow.blocks.P17040.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private Object idBdpValue;
	private Integer id;
	private Object value;

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

	public ValidationError(String code, String table, String field, Integer id, Object value) {
		super();
		this.code = code;
		this.table = table;
		this.field = field;
		this.idBdp = field;
		this.id = id;
		this.value=value;
	}

	public ValidationError(String code, String table, String field, String idBdp, Integer id, Object value) {
		super();
		this.code = code;
		this.table = table;
		this.field = field;
		this.idBdp = idBdp;
		this.id = id;
		this.value=value;
	}

	@Override
	public String toString() {
		return id + ";" + code + ";" + table + ";" + field;
	}
	
	public String toString(Integer idPlus) {
		return (id + idPlus) + ";" + code + ";" + table + ";" + field;
	}

	public String getIdBdp() {
		return idBdp;
	}

	public void setIdBdp(String idBdp) {
		this.idBdp = idBdp;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getValueFormatted() {
		Object code = this.value;
		if(code==null)
			return "";
		
		else if (code instanceof String) {
			return code.toString();
		} else if (code instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			return  sdf.format(code);
		} else if (code instanceof Timestamp) {
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
			String timeAux = sdfDate.format(code)+"T"+sdfTime.format(code);
			return timeAux;
		} else if (code instanceof Float || code instanceof BigDecimal) {
			DecimalFormat df = new DecimalFormat(
					"##################################################.############################");
			return df.format(code);
		} else if (code instanceof Boolean) {
			return (boolean) code?"1":"0";
		} else if (code instanceof Integer) {
			return String.format("%d", code);
		}
		else
			return "N/A";
	}

	public Object getIdBdpValue() {
		return idBdpValue;
	}

	public void setIdBdpValue(Object idBdpValue) {
		this.idBdpValue = idBdpValue;
	}

}
