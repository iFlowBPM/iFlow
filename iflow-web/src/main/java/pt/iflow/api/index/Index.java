package pt.iflow.api.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;

import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessHeader;
import pt.iflow.api.utils.Logger;

public class Index {
	static final String FID = "FID";
	static final String PID = "PID";
	static final String SUBPID = "SUBPID";
	static final String FULL_PROC_INFO = "FULL_PROC_INFO";
	static final String FID_PID_SUBPID = "FID_PID_SUBPID";
	
	public static String createProcessTerm(ProcessData procdata){
		return procdata.getFlowId()+"_"+procdata.getPid()+"_"+procdata.getSubPid();
	}
		
	public static void updateProcessIndexing(ProcessData procdata){		
		try {
			Document doc = new ProcessInformationCompounder(procdata).getDocument();
			Term updateTerm = new Term(FID_PID_SUBPID,createProcessTerm(procdata));
			IndexFactory.getInstance().doGetIndexWriter().updateDocument(updateTerm,doc);
			IndexFactory.getInstance().doGetIndexWriter().close();
		} catch (Exception e) {
			Logger.adminError("ADMIN", "updateProcessIndexing", procdata.getSignature() + " error updating index", e);
		}
	}
	
	public static Collection<ProcessHeader> search(String querystr) throws ParseException{		
		try {
			Logger.adminDebug("ADMIN", "search", querystr);
			ArrayList<ProcessHeader> result = new ArrayList<>();
			Analyzer analyzer = IndexFactory.getInstance().doGetAnalyzer();
			Query q = new QueryParser(FULL_PROC_INFO, analyzer).parse(querystr);		
			IndexReader reader = IndexFactory.getInstance().doGetIndexReader();
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(500);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			
			for(int i=0;i<hits.length;++i) {
			    int docId = hits[i].doc;
			    Document d = searcher.doc(docId);
			    ProcessHeader ph = new ProcessHeader(Integer.parseInt(d.get(FID)), Integer.parseInt(d.get(PID)), Integer.parseInt(d.get(SUBPID)));
			    result.add(ph);
			}
			
			return result;
		} catch (Exception e) {
			Logger.adminError("ADMIN", "search", querystr + " error searching index", e);
			return new ArrayList<ProcessHeader>();
		} 
	}
}
