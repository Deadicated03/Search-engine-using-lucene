package lucPackage;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.analysis.Analyzer;

public class Searcher {
	
	private String analyzer;
	private String articles;
	
	public Searcher(String analyzer,String articles) {
		this.analyzer = analyzer;
		this.articles = articles;
	}


	 public ScoreDoc[] searcher(Directory directory,String fieldName,String queryString) throws IOException, ParseException {
		 DirectoryReader ireader = DirectoryReader.open(directory);
		 IndexSearcher isearcher = new IndexSearcher(ireader);
		 
		 if(analyzer.equals("Standard")) {
			 StandardAnalyzer analyzer = new StandardAnalyzer();
			 QueryParser parser = new QueryParser(fieldName, analyzer);
			 Query query = parser.parse(queryString);
			 ScoreDoc[] hits = isearcher.search(query,Integer.parseInt(articles)).scoreDocs;
			 ireader.close();
			 return hits;
		 }
		 else if(analyzer.equals("Custom")) {
			 Analyzer analyzer = new stemAnalyzer();
			 QueryParser parser = new QueryParser(fieldName, analyzer);
			 Query query = parser.parse(queryString);
			 ScoreDoc[] hits = isearcher.search(query,Integer.parseInt(articles)).scoreDocs;
			 ireader.close();
			 return hits;
		 }	
		 else{
			 System.out.println("Analyzer options:Standard or Custom");
			 System.exit(1);
		 }
		
		 ireader.close();
		 return null;
	 }
}
