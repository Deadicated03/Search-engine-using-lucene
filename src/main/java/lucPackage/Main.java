package lucPackage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Main {
    
    public static void main(String[] args) throws IOException {
    	
    	if (args.length != 2) {
            System.out.println("Usage: java lucPackage.Main <Analyzer> <Articles to read>");
            return;
        }
    	
    	System.out.println("Launching...");
        String userDir = System.getProperty("user.dir");
        Path csvFilePath = Paths.get(userDir, "infiles", "papers.csv");
        Path dirPath = Paths.get(userDir, "files");
        Directory directory = FSDirectory.open(dirPath);
        SearchEngineGUI GUI = new SearchEngineGUI();
        Parser parse = new Parser();
        Printer print = new Printer(args[0],args[1]);
        CSVParser csvParser = parse.parser(csvFilePath.toString());
        print.printer(csvParser, directory);
        GUI.createAndShowGUI(directory,args[0],args[1]);
        System.out.println("Program is ready");
        System.out.println("Current config: Analyzer: " + args[0] + " | articles loaded: " + args[1]);
    }
}
