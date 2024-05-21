package lucPackage;

import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.Reader;


public class Parser {
   
	  public CSVParser parser(String csvFilePath) throws IOException {  
	        // Directly parse the file using CSVParser
	        FileReader reader = new FileReader(csvFilePath);
	        CSVFormat csvFormat = CSVFormat.DEFAULT;  // Assuming the first record is a header
	        CSVParser csvParser = new CSVParser(reader, csvFormat);
	        return csvParser; 
	    }
}