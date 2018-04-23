package pt.iflow.blocks;

import java.io.File;
import java.io.InputStream;

import javax.sql.DataSource;

public class BlockP17040ImportCENT extends BlockP17040Import{

	public BlockP17040ImportCENT(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Boolean importFile(DataSource datasource2, InputStream inputDocStream, File tmpOutputErrorDocumentFile,
			File tmpOutputActionDocumentFile) {
		
		//obter lista de linhas minimamente tipificado...num hash?
		//determinar se é insert ou update
		//adicionar acçao
		//inserir na bd
		//adicionar erros
		//registar na tabela de controlo
		//retornar sucesso ou nao
		
		return null;
	}

}
