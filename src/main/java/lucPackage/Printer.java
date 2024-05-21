package lucPackage;

import java.io.IOException;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.store.Directory;

public class Printer {
	
	private String source_id = null;
	private String year = null;
	private String title = null;
	private String abstr = null;
	private String full_text = null;
	private String analyzer;
	public Printer(String analyzer) {
		this.analyzer = analyzer;
	}
	
	
	public void printer(CSVParser csvParser,Directory directory) throws IOException {
		MyAnalyzer addDoc = new MyAnalyzer(directory,analyzer);
		int j = 0;
	    for (CSVRecord record : csvParser){
	    	
		    	int i = 0;
		    	for (String field : record) {
		            if(i == 0) {
		            	source_id = field;
		            }
		            else if(i == 1) {
		            	year = field;
		            }	
		            else if(i == 2) {
		            	title = field;
		            }
		            else if(i == 3) {
		            	abstr = field;
		            }
		            else if(i == 4) {
		            	full_text = field;
		            }
		            i+= 1;
		        }
  
	    	addDoc.addDoc(source_id, year, title, abstr, full_text);
	        j++;
	        if(j == 200) {
	            break;
	        }
	    }
	}
}


