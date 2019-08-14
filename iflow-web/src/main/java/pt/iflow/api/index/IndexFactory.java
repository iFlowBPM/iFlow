package pt.iflow.api.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;

public class IndexFactory {
	
	private static IndexFactory instance = null;
	private static Directory directory = null;
	private static Analyzer analyzer = null;
	private static IndexWriter indexWriter = null;
	private static IndexReader indexReader = null;
	private static String LUCENEDIR = "lucene";

	static IndexFactory getInstance() {
	    if(null == instance) {
	      try {
	    	//TODO add extra constructor configuration if eventually needed   
	        instance = new IndexFactory();
	        directory = null;//new NIOFSDirectory(new File(Const.IFLOW_HOME + File.separator + LUCENEDIR));
	        analyzer = new StandardAnalyzer();
	        	         	        
	      } catch (Exception e) {
	        Logger.error(null, IndexFactory.class, "getInstance", "Error creating IndexerFactory instance", e);
	        throw new Error("Could not instantiate IndexerFactory", e);
	      }
	    }
	    return instance;
	  }
	
	protected Directory doGetDirectory() {
	    return directory;
	  }
	
	protected Analyzer doGetAnalyzer() {
	    return analyzer;
	  }
	
	protected IndexWriter doGetIndexWriter() throws IOException {		
		if (indexWriter!=null)
			indexWriter.close();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(directory, config);
		return indexWriter;		
	  }
	
	protected IndexReader doGetIndexReader() throws IOException {		
		if (indexReader!=null)
			indexReader.close();
		indexReader = null;//IndexReader.open(doGetDirectory());
		return indexReader;		
	  }
}
