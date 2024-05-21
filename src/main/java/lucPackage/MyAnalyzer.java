package lucPackage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;



public class MyAnalyzer {

    private Directory directory;
    private String analyzer;

    public MyAnalyzer(Directory directory,String analyzer) throws IOException {
        this.directory = directory;
        this.analyzer = analyzer;
    }

    public IndexWriter analyzer() throws IOException {
        List<String> stopWordsList = Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "then", "else", "when", 
            "at", "by", "from", "for", "with", "about", "against", "between", 
            "into", "through", "during", "before", "after", "above", "below", 
            "to", "from", "up", "down", "in", "out", "on", "off", "over", 
            "under", "again", "further", "then", "once", "here", "there", 
            "all", "any", "both", "each", "few", "more", "most", "other", 
            "some", "such", "no", "nor", "not", "only", "own", "same", "so", 
            "than", "too", "very", "can", "will", "just",
            "should", "now"
        );

        CharArraySet stopWords = new CharArraySet(stopWordsList, true);
        if(analyzer.equals("Standard")) {
        	StandardAnalyzer analyzerMode = new StandardAnalyzer(stopWords);
        	IndexWriterConfig config = new IndexWriterConfig(analyzerMode);
        	return new IndexWriter(directory, config);
        }
        else if(analyzer.equals("Custom")) {
        	Analyzer analyzerMode = new stemAnalyzer();
        	IndexWriterConfig config = new IndexWriterConfig(analyzerMode);
        	return new IndexWriter(directory, config);
        }
        else{
        	 System.out.println("Analyzer options:Standard or Custom");
			 System.exit(1);
		}
        return null;
    }

    public void addDoc(String source_id, String year, String title, String abstr, String full_text) throws IOException {
        IndexWriter iwriter = analyzer();
        Document doc = new Document();

        doc.add(new StringField("id", source_id, Field.Store.YES));
        doc.add(new StringField("year", year, Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("abstract", abstr, Field.Store.YES));
        doc.add(new TextField("body", full_text, Field.Store.YES));

        iwriter.addDocument(doc);
        iwriter.close();
    }
}
