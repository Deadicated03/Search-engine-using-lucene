package lucPackage;

import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.Reader;


public class Parser {
   
	  public CSVParser parser(String csvFilePath) throws IOException {  
	        
	        FileReader reader = new FileReader(csvFilePath);
	        CSVFormat csvFormat = CSVFormat.DEFAULT;  
	        CSVParser csvParser = new CSVParser(reader, csvFormat);
	        return csvParser; 
	    }
}
