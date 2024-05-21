package lucPackage;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.CharArraySet;


import java.util.Arrays;

public class stemAnalyzer extends Analyzer {

	 private final CharArraySet stopWordsSet;

	    public stemAnalyzer() {
	        stopWordsSet = new CharArraySet(Arrays.asList(
	                "a", "an", "the", "and", "or", "but", "if", "then", "else", "when",
	                "at", "by", "from", "for", "with", "about", "against", "between",
	                "into", "through", "during", "before", "after", "above", "below",
	                "to", "from", "up", "down", "in", "out", "on", "off", "over",
	                "under", "again", "further", "then", "once", "here", "there",
	                "all", "any", "both", "each", "few", "more", "most", "other",
	                "some", "such", "no", "nor", "not", "only", "own", "same", "so",
	                "than", "too", "very", "can", "will", "just",
	                "should", "now"
	        ), true);
	    }
	    protected TokenStreamComponents createComponents(String fieldName) {
	        Tokenizer source = new WhitespaceTokenizer();
	        TokenStream filter = new LowerCaseFilter(source);
	        filter = new StopFilter(filter, stopWordsSet);
	        filter = new PorterStemFilter(filter);
	        return new TokenStreamComponents(source, filter);
	    }
	}
